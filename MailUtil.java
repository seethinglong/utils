
/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 19 OCT 2020
 * 
 */

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg.com.ncs.paymentgateway.common.constant.CommonConstants;
import sg.com.ncs.paymentgateway.common.constant.EmailConstants;
import sg.com.ncs.paymentgateway.common.exception.CpgException;
import sg.com.ncs.paymentgateway.common.util.CryptoUtil;
import sg.com.ncs.paymentgateway.common.util.StringUtil;
import sg.com.ncs.paymentgateway.common.util.Validator;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MailUtil {

    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

    private static MailUtil emailUtil;

    private static String emailUser;
    private static String emailPasswd;
    private static String emailPasswdSalt;
    private static String emailPasswdSeed;
    private static Properties properties;
    private static String unencrypt_emailPasswd;
    
    public static final String EMAIL_PASSWD_UNENCRYPT = "email.password.unencrypt";

    public static MailUtil getInstance() {
    	
        if (emailUtil == null) {
        	
            emailUtil = new MailUtil();
           
            String emailHost = PropertiesUtil.getProperty(EmailConstants.EMAIL_HOST);
            String emailPort = PropertiesUtil.getProperty(EmailConstants.EMAIL_PORT);
            
        	Validator.isTrue(StringUtil.isNotEmpty(emailHost), "SMTP host not configured ");
        	Validator.isTrue(StringUtil.isNotEmpty(emailPort), "SMTP port not configured "); 
        	
        	properties = new Properties();                   
        	properties.put("mail.smtp.port", emailPort);
            properties.put("mail.smtp.host", emailHost);
            
            String emailEnabled = StringUtils.isNotEmpty(PropertiesUtil.getProperty(EmailConstants.EMAIL_ENABLED)) ? PropertiesUtil.getProperty(EmailConstants.EMAIL_ENABLED) : CommonConstants.BOOLEAN_NO;
            String useTlsEmail = StringUtils.isNotEmpty(PropertiesUtil.getProperty(EmailConstants.USE_TLS_EMAIL)) ? PropertiesUtil.getProperty(EmailConstants.USE_TLS_EMAIL) : CommonConstants.BOOLEAN_NO;
            String useSslEmail = StringUtils.isNotEmpty(PropertiesUtil.getProperty(EmailConstants.USE_SSL_EMAIL)) ? PropertiesUtil.getProperty(EmailConstants.USE_SSL_EMAIL) : CommonConstants.BOOLEAN_NO;
            
            emailUser = PropertiesUtil.getProperty(EmailConstants.EMAIL_USER);
            emailPasswd = PropertiesUtil.getProperty(EmailConstants.EMAIL_PASSWD);
            emailPasswdSalt = PropertiesUtil.getProperty(EmailConstants.EMAIL_PASSWD_SALT);
            emailPasswdSeed = PropertiesUtil.getProperty(EmailConstants.EMAIL_PASSWD_SEED);

            /**
             * Email properties
             * 
             * https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
             */
            
            if (emailEnabled.equalsIgnoreCase(CommonConstants.BOOLEAN_YES)) {
                
                // Setup mail server

                if (useTlsEmail.equalsIgnoreCase(CommonConstants.BOOLEAN_YES)) {
                	properties.put("mail.smtp.starttls.enable", "true");
            	}
                if (useSslEmail.equalsIgnoreCase(CommonConstants.BOOLEAN_YES)) {
                	logger.debug("@@ SMTPS TRANSPORT = " + EmailConstants.SSL_EMAIL_TRANSPORT);
                    properties.put("mail.smtp.ssl.enable", "true");
                    properties.put("mail.transport.protocol", EmailConstants.SSL_EMAIL_TRANSPORT);
                    properties.put("mail.smtps.port", emailPort);
                    properties.put("mail.smtps.host", emailHost);
                }else {
                	logger.debug("@@ SMTP TRANSPORT = " + EmailConstants.DEFAULT_EMAIL_TRANSPORT);
                	properties.put("mail.smtp.ssl.enable", "false");
                	properties.put("mail.transport.protocol", EmailConstants.DEFAULT_EMAIL_TRANSPORT);
                }
            }
            properties.put("mail.smtp.auth", "true");
            logger.debug("@@ SMTP HOST = " + emailHost);
            logger.debug("@@ SMTP PORT = " + emailPort);
            logger.debug("@@ SMTP OVERWRITE = " + emailEnabled);
            logger.debug("@@ SMTP FROM = " + emailUser);
            logger.debug("@@ SMTP TLS = " + useTlsEmail + " 465 (SSL required) or 587 (TLS required)");                
            logger.debug("@@ SMTP SSL = " + useSslEmail);
        }

        return emailUtil;
    }
    
    public static MailUtil getInstance4GCC() {
    	MailUtil emu = MailUtil.getInstance();
        String emailHostGcc = PropertiesUtil.getProperty(EmailConstants.EMAIL_HOST_GCC);
        if( emailHostGcc != null && emailHostGcc.trim().length()>0 ) {
        	MailUtil.properties.setProperty(EmailConstants.MAIL_HOST, emailHostGcc);
        }
        return emu;
    }

    /***
     * Sends an email using the parameters listed.
     * @param subject
     * @param content
     * @param emailTo
     * @param emailFrom
     */
    public void sendEmail(String subject, String content, String emailFrom, String... emailTo) {

        isTrue(StringUtils.isNotEmpty(subject), "No Email Subject defined");
        isTrue(StringUtils.isNotEmpty(content), "No Email Content defined");
        isTrue(ArrayUtils.isNotEmpty(emailTo), "No email recipient defined");
        isTrue(StringUtils.isNotEmpty(emailFrom), "No from address defined");

        logger.info( "@@ " + emailFrom + "Sending email to " + Arrays.toString(emailTo));

        Session session = Session.getInstance(properties);
        if (StringUtils.isNotEmpty(emailPasswd) && StringUtils.isNotEmpty(emailPasswdSalt) && StringUtils.isNotEmpty(emailPasswdSeed)) {
            final String decodedPasswd = CryptoUtil.decrypt(emailPasswd, emailPasswdSalt, emailPasswdSeed);
            isTrue(StringUtils.isNotEmpty(decodedPasswd), "Unable to send email. Invalid credentials!!");
            // Get the default Session object.
            	session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailUser, decodedPasswd);
                        }
                    });
        }

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(emailFrom));
            

            // Set To: header field of the header.
            for (String email : emailTo) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(content, "utf-8", "html");

            // Send message
            Transport.send(message);
        } catch (MessagingException mex) {
            throw new CpgException("Error sending email", mex);
        }
    }

    /***
     * Sends email and attaches the log files as part of the email.
     * @param subject
     * @param content
     * @param emailTo
     * @param emailFrom
     * @param attachment
     */
    public void sendEmail(String subject, String content, String emailTo, String emailFrom, ArrayList<File> fileToAttach, String emailBcc) {

        isTrue(StringUtils.isNotEmpty(subject), "No Email Subject defined");
        isTrue(StringUtils.isNotEmpty(content), "No Email Content defined");
        isTrue(StringUtils.isNotEmpty(emailTo), "No Send to address defined");
        isTrue(StringUtils.isNotEmpty(emailFrom), "No from address defined");
        

        Session session = Session.getInstance(properties);

        unencrypt_emailPasswd = PropertiesUtil.getProperty(EmailConstants.EMAIL_PASSWD_UNENCRYPT);
        session = null; // cleanup the previous session object
        if ( StringUtils.isNotEmpty(unencrypt_emailPasswd)) {
        	logger.debug("@@ SMTP USING ENENCRYPTED PASSWORD = " + unencrypt_emailPasswd);
        	String encrypt_password = CryptoUtil.encrypt(unencrypt_emailPasswd, emailPasswdSalt, emailPasswdSeed);
        	logger.debug("@@ ENCRYPT PASSWORD                = " + encrypt_password);
        	logger.debug("@@ DECRYPT PASSWORD                = " + CryptoUtil.decrypt(encrypt_password, emailPasswdSalt, emailPasswdSeed));
        	logger.debug("@@ SMTP emailFrom = " + emailFrom);
        	logger.debug("@@ SMTP emailUser = " + emailUser);
        	logger.debug("@@ SMTP emailBcc = " + emailBcc);
        	session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailUser, unencrypt_emailPasswd);
                    }
                });
		}else if (StringUtils.isNotEmpty(emailPasswd) && StringUtils.isNotEmpty(emailPasswdSalt) && StringUtils.isNotEmpty(emailPasswdSeed)) {
            final String decodedPasswd = CryptoUtil.decrypt(emailPasswd, emailPasswdSalt, emailPasswdSeed);
            isTrue(StringUtils.isNotEmpty(decodedPasswd), "Unable to send email. Invalid credentials!!");
            // Get the default Session object.
            session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailUser, decodedPasswd);
                    }
                });
        }
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            Multipart mailContent = new MimeMultipart();

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(emailFrom));

            // Set To: header field of the header.
            
            if (emailTo.contains(","))
            {
            	message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
            }
            else
            {
            	message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            }
            
            logger.debug("@@ To " + emailTo);
            
            if ( emailBcc != null && StringUtils.isNotEmpty(emailBcc) ) {
            	logger.debug("@@ Bcc " + emailBcc);
                if (emailBcc.contains(","))
                {
                	message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(emailBcc));
                }
                else
                {
                	message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailBcc));
                }
            }else {
            	logger.debug("@@ Bcc Skipped ");
            }
            // Set Subject: header field
            message.setSubject(subject);

            // Now create the message content
            MimeBodyPart msgPart = attachContent(content);
            
            mailContent.addBodyPart(msgPart);

            mailContent = attachFileList( mailContent , fileToAttach) ;
            
            message.setContent(mailContent);
 
            // Send message
            Transport.send(message);
        } catch (MessagingException mex) {
            throw new CpgException("Error sending email with attached files", mex);
        }
    }

    /***
     * Add attachments with size cap at 15MB
     * @param fileName
     * @return
     */
    
    private Multipart attachFileList( Multipart multiPart, List<File> fileToAttach) throws MessagingException{
        long size = 0;
    	if ( fileToAttach != null && fileToAttach.size() > 0 ) {
    		logger.info("@@ Attach File Total " + fileToAttach.size() );
            for (File file : fileToAttach) {
            	
            	if (file.exists() ) {
            		size += file.length();
            		logger.debug("@@ Attach File " + file.toString());
            		MimeBodyPart attachementPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(file) {
                    	@Override
                    	public String getContentType()
                    	{
                    		return "application/octet-stream";
                    	}
                    };
                    attachementPart.setDataHandler(new DataHandler(source));
                    attachementPart.setFileName(file.getName());
                    multiPart.addBodyPart(attachementPart);
            	}
            }
        }else {
        	logger.debug("No attachment");
        }
    	logger.debug("@@@ Total file size = " + String.valueOf(size));
        if ( size > 15000000 ) {
        	MessagingException mex = new MessagingException();
        	throw new CpgException("Error attached files size exceeding 15MB", mex);
        }
        return multiPart;
    	
    }

    /***
     * Generates a Mimebodypart and attaches the email content.
     * @param content
     * @return
     */
    private MimeBodyPart attachContent(String content) throws MessagingException {
        if (StringUtils.isEmpty(content)) {
            logger.debug("No content to add to the message.");
            return null;
        }
        MimeBodyPart bodyPart = new MimeBodyPart();

        if( content.contains("DOCTYPE html")) {
        	logger.debug("@@@ EMAIL HTML body ");
        	bodyPart.setContent(content, "text/html;charset=UTF-8");
        }else{
        	bodyPart.setText(content);
        }
        return bodyPart;
    }
    
    public static boolean isTrue(boolean expression, String message) {
        if (!expression) {
            throw new CpgException(message);
        } else {
            return true;
        }
    }


}
