

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class trim {
    
    private static String DELIMITED_CHAR = "\\|";

    private String dsvFile;

	public trim(String dELIMITED_CHAR, String dsvFile) {
		super();
		DELIMITED_CHAR = dELIMITED_CHAR;
		this.dsvFile = dsvFile;
	}


	public String getDELIMITED_CHAR() {
		return DELIMITED_CHAR;
	}

	public void setDELIMITED_CHAR(String dELIMITED_CHAR) {
		DELIMITED_CHAR = dELIMITED_CHAR;
	}

	public void setDsvFile(String dsvFile) {
		this.dsvFile = dsvFile;
	}
	
	public static void main(String[] args) throws IOException {
		
		ZoneId z = ZoneId.systemDefault();
		
		ZoneId sg = ZoneId.of("Asia/Singapore");
		
		if( z.equals(sg)) {
			
			System.out.println("TimeZone correct ");
			
		}
		
		String delimitedChar = DELIMITED_CHAR;
		
		if ( args.length > 1 ) {
			
			System.out.println("DELIMITED_CHAR = " + DELIMITED_CHAR ) ;
			
			System.out.println("DELIMITED_CHAR changed to = " + args[1] ) ;
			
			DELIMITED_CHAR = args[1];
			
		}
		
		String glob = "glob:**/TBL_*.txt";
		
		String path = System.getProperty("user.dir"); 
		
		List<String> fileList = listFilesfromFolder(path,glob);
		
		System.out.println("Trim all files from " + path.toString() + " with DELIMITED_CHAR =" + DELIMITED_CHAR);
		
		for ( String f : fileList) {
		
			trim dsv = new trim( DELIMITED_CHAR, f);
			
			dsv.trim();
			
		}
		
	}
	
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
							System.out.println("Processing Batch File " + path.toString());
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


	public void trim() throws IOException {

		BufferedWriter bw = null;
		BufferedReader br = null;
		long nano_startTime = System.currentTimeMillis(); 
		
		try  
		{  
			File file=new File(dsvFile);  
			
			File outputfile = new File( dsvFile + ".dsv");
			
			if (!file.exists()) {
				throw new IOException("Input DSV File " + file + " does not exist ");
			}else {
				System.out.println(file + " exist.  Size of file = " + file.length() + " bytes" );
			}
			  
			outputfile.createNewFile();

			FileWriter fw = new FileWriter(outputfile);
			bw = new BufferedWriter(fw);

			FileReader fr=new FileReader(file); 
			br= new BufferedReader(fr);

			String line; 
			
			while((line=br.readLine())!=null)  
			{  
				String[] lineSplittedArray = line.split(DELIMITED_CHAR);
				String linebw = "";
				for ( String s : lineSplittedArray) {
				
					linebw = linebw + s.trim() + "|";			
				
				}
				
				 linebw = linebw.substring(0, linebw.length()-1);
				 bw.write(linebw);
				 bw.newLine();
			}  		
			System.out.println( outputfile + " Size = " + outputfile.length() + " bytes" );
			
		}catch(IOException e){  
			e.printStackTrace();  
		}  
		finally
		{ 
			 System.out.println("Completed");
		   try{
		     if(bw!=null) bw.close();
		     if(br!=null) br.close();
		   }catch(Exception ex){
		       System.out.println("Error in closing the BufferedWriter"+ex);
		    }
		}
		
        long processing_time = ( System.currentTimeMillis() - nano_startTime );

        System.out.println("Total Time : " + String.valueOf( processing_time ) + " ms" );


    }

	

}
