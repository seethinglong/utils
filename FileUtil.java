


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import sg.com.ncs.paymentgateway.batch.giro.GiroBatchConstant;
import sg.com.ncs.paymentgateway.common.util.DateUtil;
import sg.com.ncs.paymentgateway.common.util.PropertiesUtil;
import sg.com.ncs.paymentgateway.common.util.StringUtil;
import sg.com.ncs.paymentgateway.common.util.Validator;

/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 19 OCT 2020
 * 
 */

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    
    
    //Loading by matching file pattern csv with FileList
	public static List<String> listFilesfromFolder(String location , String glob) throws IOException {
		
		List<String> fileList = new ArrayList<String>();
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
		
		Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path,
					BasicFileAttributes attrs) throws IOException {
				String separator = "/";
				String OS = System.getProperty("os.name").toLowerCase();
			    if (OS.indexOf("win") >= 0) {
			    	separator = "\\";
			    }
			    File directory = new File(location);
			    if (! directory.exists()){
			    	directory.mkdir();
			    } else {
				    File fail_directory = new File(location + separator + "Fail");
				    if (! fail_directory.exists()){directory.mkdir();}
					if (pathMatcher.matches(path)) {
						log.info("Processing Batch File " + path.toString());
						fileList.add(path.toString());
						directory = new File(path.toString());
					}
			    }
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});//End Loop Folder
		return fileList;
	}
	
	public static void createDirectoryIfNotExists(String path) {
		
		File directory = new File(path);
		
	    if (! directory.exists()){
	    	directory.mkdir();
	    }

	}

	
    public static String genDirectoryName(Date rptDate, String type, boolean checkFolderExists, String batchId){
        String folder= PropertiesUtil.getProperty(GiroBatchConstant.GIRO_FILE_DIR);
        
        Validator.isTrue(StringUtil.isNotEmpty(folder), "No base directory specified. Pls check the configuration");

        StringBuilder downloadDir = new StringBuilder(4000);
        downloadDir.append(folder).append(File.separator);
        downloadDir.append(batchId).append(File.separator);
        downloadDir.append(DateUtil.format(rptDate, DateUtil.YYYYMMDD)).append(File.separator);
        
        try{
            File downloadFolder =new File(downloadDir.toString());
            log.debug("@@@ Checking if ["+downloadDir+"] exists");
            if (!downloadFolder.exists()){
                log.debug("["+downloadDir.toString()+"] does not exist. Trying to create");
                downloadFolder.mkdirs();
            }else{
                //folder exists, check if its empty. If not empty, backup the folder first.
                if (checkFolderExists && downloadFolder.isDirectory() && downloadFolder.listFiles() != null && downloadFolder.listFiles().length >0){
                    String dtBackup = DateUtil.format(Date.from(java.time.ZonedDateTime.now().toInstant()), DateUtil.YYYYMMDDHHMMSS);
                    log.debug("[" + downloadDir.toString() + "] exists, backing up to [" + folder + File.separator + "_" + dtBackup + "]");
                    File backupDownloadFolder = new File(folder + File.separator + "_" + dtBackup);
                    if (!downloadFolder.renameTo(backupDownloadFolder)) {
                        log.error("Failed to backup [" + downloadFolder.toString() + "] to [" + backupDownloadFolder.toString());
                        return downloadDir.toString();
                    }
                    log.debug("Trying to create ["+downloadDir.toString()+"].");
                    downloadFolder.mkdirs();
                }
            }
        }catch(SecurityException se){
            log.error("Unable to create giro download directory", se);
            return null;
        }
        return downloadDir.toString();
    } 
    
    public boolean sftp(
    		String username, 
    		String sftpHost, 
    		int  sftpPortInt, 
    		String password, 
    		String remoteDir, 
    		String localdDir,
    		List<String> globList) {
    	
    	long nano_startTime = System.currentTimeMillis(); 
    	

    	
    	JSch jsch = new JSch();
    	

    	try {
    		Session jschSession;
    		jschSession = jsch.getSession(username, sftpHost, sftpPortInt);
			jschSession.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			jschSession.setConfig(config);
			jschSession.connect();
    	    
    	    ChannelSftp channelSftp =  (ChannelSftp) jschSession.openChannel("sftp");
    	    channelSftp.connect();
    	    
    	    
    	    
    	    
    	    
    	    String presentWorkingDirectory=channelSftp.pwd();
    	    remoteDir = presentWorkingDirectory + remoteDir;
       	    SftpATTRS attrs=null;
       	    
    	    try {
    	        attrs = channelSftp.stat(remoteDir);
    	    } catch (Exception e) {
    	    	log.info(remoteDir+" not found");
    	    }
    	    
    	    
    	    if (attrs != null) {
    	    	
    	    	log.info("       @@ SFTP TO   = " + remoteDir +" validated");
    	    	
    	    } else {
    	    	
    	    	log.info("Creating dir "+remoteDir);
    	    	
    	        channelSftp.mkdir(remoteDir);
    	        
    	        attrs = channelSftp.stat(remoteDir);
    	        
    	    }
    	    
    	    for( String glob : globList ) {
    	    
	        	List<String> fileList;
	        	
	        	log.info("@@ Folder "+ localdDir + " GLOB = " + glob);
	        	
				fileList = FileUtil.listFilesfromFolder(localdDir, glob);    
				
				log.info("@@ SFTP Session open.  Transfering total " + fileList.size() + " files ");
	
				for( String filename : fileList ) {
					
					channelSftp.put(filename, remoteDir );
				}
    	    }
			
            channelSftp.exit();
            
            log.info("SFTP Time : " + String.valueOf(System.currentTimeMillis() - nano_startTime ) + " ms" );
            
		} catch (JSchException e) {
			log.error("GIRO SFTP JSchException");
			e.printStackTrace();
			return false;
		} catch (SftpException e) {
			log.error("GIRO SFTP SftpException ");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("GIRO SFTP IOException ");
			e.printStackTrace();
			return false;
		}
    	
    	return true;
    	
    }
    
    public static Boolean ping(String ipAddress) throws IOException {

    	Boolean ping;
        InetAddress inet = InetAddress.getByName(ipAddress);        
        
        log.info("");
        log.info("");
        log.info("Sending Ping Request to " + ipAddress);
        log.info(inet.isReachable(5000) ? "Host is reachable" : "Host is NOT reachable");
        log.info("");
        log.info("");
        
        ping = inet.isReachable(5000)? true: false;
        
        return ping;
    }
    
    
}
