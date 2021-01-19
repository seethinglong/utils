import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.odftoolkit.odfdom.converter.core.ODFConverterException;
import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;


import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.OptionsHelper;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.utils.StringUtils;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import sg.com.ncs.paymentgateway.batch.giro.model.GiroEmailReplaceText;




public class OdtFreemarkerUtil {
	
	private static final OdtFreemarkerUtil INSTANCE = new OdtFreemarkerUtil();

    public static OdtFreemarkerUtil getInstance() {
        return INSTANCE;
    }

	public static void main(String[] args) {
		try {
			// 1) Load ODT file by filling Velocity template engine and cache
			// it to the registry
			
			String templateFileName = "d:\\w\\Sources\\cpg\\batch\\src\\main\\resources\\template\\First.XdocReport.odt"; 
			InputStream in = new FileInputStream(templateFileName);
			IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
					in, TemplateEngineKind.Freemarker);

			// 2) Create context Java model
			IContext context = report.createContext();
			GiroEmailReplaceText replaceText = new GiroEmailReplaceText("SHL","BLK 668 Chander Road", "#12-06","210668", "$123.34","AAA ORG", "11/01/2021", "Jan 2021", "$999.99");
			context.put("replaceTextOdt", replaceText);

			// 3) Generate report by merging Java model with the ODT
			
			String odtFile = "d:\\w\\Sources\\cpg\\batch\\src\\main\\resources\\template\\First_Out.odt";
			OutputStream out = new FileOutputStream(new File(
					odtFile));
			report.process(context, out);
			
			System.out.println("Tempalate file " + templateFileName );
			
			System.out.println("Completed created file " + odtFile );
			
			String pdfFile = "d:\\w\\Sources\\cpg\\batch\\src\\main\\resources\\template\\First_Out.pdf";
			
			process(replaceText, templateFileName, odtFile, pdfFile);
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XDocReportException e) {
			e.printStackTrace();
		}
	}

	public static void process( GiroEmailReplaceText replaceText , String templateFileName, String odt,String pdf) {
		try {
			// 1) Load ODT file by filling Velocity template engine and cache
			// it to the registry
//			InputStream in = new FileInputStream(templateFileName);
//			IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
//					in, TemplateEngineKind.Freemarker);
//
//			// 2) Create context Java model
//			IContext context = report.createContext();
//			//{"MMM YYYY", "XX.XX", "Company Name", "Address line 1", "Address line 2", "Postal Code", "Date", "ABC Pte Ltd"}
//			context.put("replaceTextOdt", replaceText);
//
//			// 3) Generate report by merging Java model with the ODT
//			OutputStream out = new FileOutputStream(odt);
//			
//			report.process(context, out);
//			
//			out.close();
			
			Options o = Options.getTo(ConverterTypeTo.PDF).via(
                    ConverterTypeVia.ODFDOM);
			
			PdfOptions options = getInstance().toPdfOptions(o);	
			
			new OdtFreemarkerUtil().convert( odt, pdf, options );
			

		} catch (XDocReportException e) {
			e.printStackTrace();
		}
		
	}
	
	public void convert( String odt, String pdf, PdfOptions options  ) throws XDocConverterException {
        try {
        	
        	OutputStream out = new FileOutputStream(new File (pdf) );
        	
            OdfTextDocument.newTextDocument();
            
			OdfDocument odfDocument = OdfTextDocument.loadDocument( new File (odt) );

            
            PdfConverter.getInstance().convert( odfDocument, out, options);
            
            
            odfDocument.close();
            
        }catch ( ODFConverterException e ){
        	
                throw new XDocConverterException( e );
            
        }catch ( IOException e ){
        	
            throw new XDocConverterException( e );
            
        }catch ( Exception e ){
        	
            throw new XDocConverterException( e );
            
        }
        
    }

    public PdfOptions toPdfOptions( Options options )
    {
        if ( options == null ){
            return null;
        }
        
        Object value = options.getSubOptions( PdfOptions.class );
        if ( value instanceof PdfOptions ){
            return (PdfOptions) value;
        }
        
        PdfOptions pdfOptions = PdfOptions.create();
        // Populate font encoding
        
        String fontEncoding = OptionsHelper.getFontEncoding( options );
        
        if ( StringUtils.isNotEmpty( fontEncoding ) ){
            pdfOptions.fontEncoding( fontEncoding );
        }
        
        return pdfOptions;
    }
    
}
