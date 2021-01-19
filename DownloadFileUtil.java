/*
 * 
 * Author			: SEET HING LONG
 * DATE				: 19 OCT 2020
 * 
 * */

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class DownloadFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(DownloadFileUtil.class);

    @Autowired
    ESvcMgr eSvcMgr;
    
    public static boolean downloadReconFile (MerchantTO merchant, GatewayTO gateway, Date rptDate,
            String filename, String downloadDir){
    		return DownloadFileUtil.downloadReconFile(filename, GiroBatchConstant.SETTLEMENT_TYPE_GIRO,
           downloadDir,merchant, gateway );
    }

    private static boolean downloadReconFile(String filename, String type, String downloadDir,
                                                       MerchantTO merchant, GatewayTO gateway){
        String batchSalt = PropertiesUtil.getProperty(GiroBatchConstant.GIRO_BATCH_SALT);
        String batchSeed = PropertiesUtil.getProperty(GiroBatchConstant.GIRO_BATCH_SEED);
        Validator.isTrue(StringUtil.isNotEmpty(batchSalt), "Salt for the batch job is empty. Pls check the configuration");
        Validator.isTrue(StringUtil.isNotEmpty(batchSeed), "Seed for the batch job is empty. Pls check the configuration");

        if (StringUtil.isEmpty(merchant.getSettlementReportUser()) ){
            logger.error("No Report User ID defined for [" + merchant.getMerchantName() +"]");
            return false;
        }
        if (StringUtil.isEmpty(merchant.getSettlementReportUserPasswd()) ){
            logger.error("No Report User Password defined for [" + merchant.getMerchantName() +"]");
            return false;
        }
        if (StringUtil.isEmpty(gateway.getGatewayRptUrl()) ){
            logger.error("No Report URL defined for [" + merchant.getMerchantName() +"]");
            return false;
        }

        String passwd = CryptoUtil.decrypt(merchant.getSettlementReportUserPasswd(), batchSalt, batchSeed);
        if (StringUtil.isEmpty(passwd) ){
            logger.error("Failed to decrypt password for [" + merchant.getMerchantName() +"]");
            return false;
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(BatchConstants.UID, merchant.getSettlementReportUser()));
        params.add(new BasicNameValuePair(BatchConstants.PASSWD, passwd));
        params.add(new BasicNameValuePair(BatchConstants.FILENAME, filename));
        params.add(new BasicNameValuePair(BatchConstants.MERCHANT_ID, merchant.getMerchantId()));
        params.add(new BasicNameValuePair(BatchConstants.SETTLEMENT_TYPE, type));

        String settlementFileName = downloadDir + File.separator + filename;
        if (new File(settlementFileName).exists()){
            // file exists.. archive and try again.
            logger.warn("File already exists. Archiving to ["+downloadDir + File.separator + filename+ BatchConstants.UNDERSCORE +
                         DateUtil.format(Date.from(java.time.ZonedDateTime.now().toInstant()), DateUtil.YYYYMMDDHHMMSS)+"]");
            new File(settlementFileName).renameTo(new File(downloadDir + File.separator + filename+ BatchConstants.UNDERSCORE +
                    DateUtil.format(Date.from(java.time.ZonedDateTime.now().toInstant()), DateUtil.YYYYMMDDHHMMSS)));
        }
        try {
          HttpUtil.download(PropertiesUtil.getProperty( GiroBatchConstant.AWS_GET_GIRO_SETTLEMENT_RESULT_FILE_URL), params, settlementFileName);
        }catch (CpgException cpge){
            logger.warn("Failed to download the specified file!!",cpge );
            return false;
        }
        return (new File(settlementFileName)).exists();
    }
}
