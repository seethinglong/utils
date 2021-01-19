/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 17 JAN 2021
 * 
 */

import java.io.File; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;

import ooo.connector.BootstrapSocketConnector;
import sg.com.ncs.paymentgateway.common.exception.CpgException;
import sg.com.ncs.paymentgateway.common.util.StringUtil;


public class OdtToPdfUtil {
	private static String templateFileName = "First.odt";
	private static String filePath ="";
	private static String filePDF = "";
	private static final Logger logger = LoggerFactory.getLogger(OdtToPdfUtil.class);
    
    protected static void createExampleData(
        com.sun.star.text.XTextDocument xTextDocument )
    {

        com.sun.star.text.XTextCursor xTextCursor = null;
        
        try {
            xTextCursor = (com.sun.star.text.XTextCursor)
                xTextDocument.getText().createTextCursor();
            com.sun.star.text.XText xText = (com.sun.star.text.XText)
                xTextDocument.getText();

            com.sun.star.beans.XPropertySet xCPS = (com.sun.star.beans.XPropertySet)
                UnoRuntime.queryInterface(
                    com.sun.star.beans.XPropertySet.class, xTextCursor);
        }
        catch( Exception e) {
            e.printStackTrace(System.err);
        }
        
    }
    
    protected static com.sun.star.lang.XComponent CreateNewDocument(
        com.sun.star.frame.XDesktop xDesktop,
        String sDocumentType )
    {
        String sURL = "private:factory/" + sDocumentType;
        
        sURL =  System.getProperty( "os.name" ).startsWith( "Windows" ) ? "file:///" + filePath + templateFileName:  "file://" + filePath + templateFileName ;
        
        logger.info("@@@ Converting template " + sURL );
        
        com.sun.star.lang.XComponent xComponent = null;
        com.sun.star.frame.XComponentLoader xComponentLoader = null;
        com.sun.star.beans.PropertyValue xValues[] =
            new com.sun.star.beans.PropertyValue[1];
        com.sun.star.beans.PropertyValue xEmptyArgs[] =
            new com.sun.star.beans.PropertyValue[0];
        
        try {
            xComponentLoader = (com.sun.star.frame.XComponentLoader)
                UnoRuntime.queryInterface(
                    com.sun.star.frame.XComponentLoader.class, xDesktop);
        
            xComponent  = xComponentLoader.loadComponentFromURL(
                sURL, "_blank", 0, xEmptyArgs);
        }
        catch( Exception e) {
            e.printStackTrace(System.err);
        }
        
        return xComponent ;
    }
    
    public static com.sun.star.text.XTextDocument createTextdocument(
        com.sun.star.frame.XDesktop xDesktop )
    {
        com.sun.star.text.XTextDocument aTextDocument = null;
        
        try {
            com.sun.star.lang.XComponent xComponent = CreateNewDocument(xDesktop,
                                                                        "swriter");
            aTextDocument = (com.sun.star.text.XTextDocument)
                UnoRuntime.queryInterface(
                    com.sun.star.text.XTextDocument.class, xComponent);
        }
        catch( Exception e) {
            e.printStackTrace(System.err);
        }
        
        return aTextDocument;
    }
    
    public static com.sun.star.frame.XDesktop getDesktop() {
        com.sun.star.frame.XDesktop xDesktop = null;
        com.sun.star.lang.XMultiComponentFactory xMCF = null;
        
        try {
            com.sun.star.uno.XComponentContext xContext = null;

            String oooExeFolder =
                    System.getProperty( "os.name" ).startsWith( "Windows" ) ?
                    "c:/Program Files (x86)/OpenOffice 4/program/" : "/opt/openoffice4/program";       
            String ExeFolder = PropertiesUtil.getProperty("giro.AOO.folder");
            oooExeFolder = StringUtil.isNotEmpty(ExeFolder)? ExeFolder : oooExeFolder;
	        File directory = new File(oooExeFolder);
		    
	        if (! directory.exists()){
	        	
		    	throw new CpgException("AOO (Apache Open Office ) executable " + oooExeFolder + " not found");
		    	
		    }
		    
	        logger.info("@@@ ==> Connecting AOO = " + oooExeFolder);

        	xContext = BootstrapSocketConnector.bootstrap(oooExeFolder);
            
            xMCF = xContext.getServiceManager();
            
            
            if( xMCF != null ) {
            	
                logger.info("Connected to a running office ...");

                Object oDesktop = xMCF.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);
                xDesktop = (com.sun.star.frame.XDesktop) UnoRuntime.queryInterface(
                    com.sun.star.frame.XDesktop.class, oDesktop);
            }
            else
            	logger.error( "Can't create a desktop. No connection, no remote office servicemanager available!" );
        }
        catch( Exception e) {
            e.printStackTrace(System.err);
        }
        
        
        return xDesktop;
    } 
    
    public static String process(String[] searchWords, String[] replaceWords, String templateFile, String path, String pdf) {
        // You need the desktop to create a document
        // The getDesktop method does the UNO bootstrapping, gets the
        // remote servie manager and the desktop object.
    	//bootstrapconnector
    	templateFileName = templateFile;

        com.sun.star.frame.XDesktop xDesktop = null;
        xDesktop = getDesktop();
        filePath = path;
        filePDF = System.getProperty( "os.name" ).startsWith( "Windows" ) ? "file:///" + path + pdf: "file://" + path + pdf;
        com.sun.star.text.XTextDocument xTextDocument =
            createTextdocument( xDesktop );

        //createExampleData( xTextDocument );
        
        
        try {
            com.sun.star.util.XReplaceDescriptor xReplaceDescr = null;
            //com.sun.star.util.XSearchDescriptor xSearchDescriptor = null;
            com.sun.star.util.XReplaceable xReplaceable = null;
            
            xReplaceable = (com.sun.star.util.XReplaceable)
                UnoRuntime.queryInterface(
                    com.sun.star.util.XReplaceable.class, xTextDocument);
            
            // You need a descriptor to set properies for Replace
            xReplaceDescr = (com.sun.star.util.XReplaceDescriptor)
                xReplaceable.createReplaceDescriptor();

            for( int iArrayCounter = 0; iArrayCounter < searchWords.length;
                 iArrayCounter++ )
            {
            	logger.info(searchWords[iArrayCounter] +
                    " -> " + replaceWords[iArrayCounter]);
                // Set the properties the replace method need
                xReplaceDescr.setSearchString(searchWords[iArrayCounter] );
                xReplaceDescr.setReplaceString(replaceWords[iArrayCounter] );
                
                // Replace all words
                xReplaceable.replaceAll( xReplaceDescr );
            }
            writeToPDF(xTextDocument, filePDF);
        	// File (or directory) with old name

            xTextDocument.dispose();
        }
        catch( Exception e) {
            e.printStackTrace(System.err);
            logger.error(e.toString());
        }            
        logger.info("PDF Creation Done");       
        xDesktop.terminate();
        return filePDF;
        
    }

    protected static void writeToPDF(XTextDocument xTextDocument, String filePathPDF) throws IOException {
    	PropertyValue[] filterData = new PropertyValue[5];

    	filterData[0] = new PropertyValue();

    	filterData[0].Name = "UseLosslessCompression";

    	filterData[0].Value = Boolean.FALSE;

    	filterData[1] = new PropertyValue();

    	filterData[1].Name = "Quality";

    	filterData[1].Value = new Integer(50);

    	filterData[2] = new PropertyValue();

    	filterData[2].Name = "ReduceImageResolution";

    	filterData[2].Value = Boolean.TRUE;

    	filterData[3] = new PropertyValue();

    	filterData[3].Name = "MaxImageResolution";

    	filterData[3].Value = new Integer(150);

    	filterData[4] = new PropertyValue();

    	filterData[4].Name = "ExportFormFields";

    	filterData[4].Value = Boolean.FALSE;


    	PropertyValue[] pdfStoreProps = new PropertyValue[4];

    	pdfStoreProps[0] = new PropertyValue();

    	pdfStoreProps[0].Name = "FilterName";

    	pdfStoreProps[0].Value = "writer_pdf_Export";

    	pdfStoreProps[1] = new PropertyValue();

    	pdfStoreProps[1].Name = "Pages";

    	pdfStoreProps[1].Value = "All";

    	pdfStoreProps[2] = new PropertyValue();

    	pdfStoreProps[2].Name = "Overwrite";

    	pdfStoreProps[2].Value = Boolean.TRUE;

    	pdfStoreProps[3] = new PropertyValue();

    	pdfStoreProps[3].Name = "FilterData";

    	pdfStoreProps[3].Value = filterData;

    	; // or XComponent

    	XStorable xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, xTextDocument);
    	logger.info("@@@ Create PDF writeToPDF " + filePathPDF);
    	xStorable.storeToURL(filePathPDF, pdfStoreProps);

    }
}
