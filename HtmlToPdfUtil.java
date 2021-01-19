
/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 19 OCT 2020
 * 
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import com.itextpdf.html2pdf.HtmlConverter;


public class HtmlToPdfUtil 
{
	private static final Logger logger = LoggerFactory.getLogger(HtmlToPdfUtil.class);

    
	@Value("html/*.htm")
    private Resource[] inputResources;
	
	
    public void html2pdf() throws IOException {
    	String HTML = "<h1>Hello</h1>"
			+ "<p>This was created using iText</p>"
			+ "<a href='hmkcode.com'>hmkcode.com</a>"; 
    	HTML = Files.lines(Paths.get("manifest.mf"), StandardCharsets.UTF_8).toString();
    	
        for( Resource rss : inputResources ) {     
    		logger.debug("Generating PDF " + rss.getFilename() + ".pdf");
    		HtmlConverter.convertToPdf(HTML, new FileOutputStream( rss.getFilename() + ".pdf"));
        	rss.getFile();
		}
    }
    
    public void main( String[] args ) throws FileNotFoundException, IOException  
    {
       html2pdf();
    }
}

