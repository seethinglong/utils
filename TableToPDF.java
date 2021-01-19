/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 24 NOV 2020
 * 
 */
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import sg.com.ncs.paymentgateway.common.util.DateUtil;
import sg.com.ncs.paymentgateway.giro.app.model.GiroReport;
public class TableToPDF { 
	private static final Logger L = LoggerFactory.getLogger(TableToPDF.class);
	private static Font ARIAL = new Font(FontFamily.TIMES_ROMAN, 10, Font.BOLD, new BaseColor(0, 0, 0));
	private static Font HELVETICA = new Font(FontFamily.HELVETICA, 8, Font.BOLDITALIC, new BaseColor(0, 0, 255));
   
   public static void tableToPdf(String file, List<GiroReport> giroList) { 
       
	      Document document = new Document();

	      try {
	          PdfWriter.getInstance(document,
	              new FileOutputStream(file));

	          document.open();
	          PdfPTable table = new PdfPTable(9);
	          
	          // Set Table Total Width
	          table.setTotalWidth(600);

	          // Set Each Column Width - Make Sure Array is the same number specified in constructor
	          table.setWidths(new int[]{70, 150, 70, 50, 70, 50, 70, 70});
	          
	          PdfPCell sn = new PdfPCell(addHeader("No"));	          
	          PdfPCell cell0 = new PdfPCell(addHeader("Reference"));
	          PdfPCell cell1 = new PdfPCell(addHeader("Company"));
	          PdfPCell cell2 = new PdfPCell(addHeader("Billing Date"));
	          PdfPCell cell3 = new PdfPCell(addHeader("Giro     Status"));
	          PdfPCell cell4 = new PdfPCell(addHeader("Account  Status"));
	          PdfPCell cell5 = new PdfPCell(addHeader("Previous Balance"));
	          PdfPCell cell6 = new PdfPCell(addHeader("Outstand Amount"));
	          PdfPCell cell7 = new PdfPCell(addHeader("Total    Amount"));
	          
	          table.addCell(sn);
	          table.addCell(cell0);
	          table.addCell(cell1);
	          table.addCell(cell2);
	          table.addCell(cell3);
	          table.addCell(cell4);
	          table.addCell(cell5);
	          table.addCell(cell6);
	          table.addCell(cell7);


	          L.info("Total rows " + giroList.size() );
	          
	          int i = 1;
	          
	          for( GiroReport giroReport : giroList ) {
	        	  
		          String Reference = giroReport.getBlCuCustRefNo() == null?"NULL":giroReport.getBlCuCustRefNo();
		          String Company = giroReport.getCompanyName() == null?"NULL":giroReport.getCompanyName();
		          String BillingDate = formatDateToString(giroReport.getBlDate(),DateUtil.YYYYMMDD, "");
		          String GiroStatus = giroReport.getGiStatus()== null?"NULL":String.valueOf(giroReport.getGiStatus());
		          String AccStatus = giroReport.getAccStatus()== null?"NULL":String.valueOf(giroReport.getAccStatus());
		          String PreviousBalance = giroReport.getPyPreviousBalance() == null?"NULL":giroReport.getPyPreviousBalance().toString();
		          String TotalAmount = giroReport.getBlTotalAmount() == null? "NULL": giroReport.getBlTotalAmount().toString();
		          String OutstandingAmount = giroReport.getPyOutstanding() == null? "NULL": String.valueOf(giroReport.getPyOutstanding());
      
		          sn = new PdfPCell(addString(String.valueOf(i)));
	        	  cell0 = new PdfPCell(addString(Reference));
		          cell1 = new PdfPCell(addString(Company));
		          cell2 = new PdfPCell(addString(BillingDate));
		          cell3 = new PdfPCell(addString(GiroStatus));
		          cell4 = new PdfPCell(addString(AccStatus));
		          cell5 = new PdfPCell(addString(PreviousBalance));
		          cell6 = new PdfPCell(addString(OutstandingAmount));
		          cell7 = new PdfPCell(addString(TotalAmount));
		          table.addCell(sn);
		          table.addCell(cell0);
		          table.addCell(cell1);
		          table.addCell(cell2);
		          table.addCell(cell3);
		          table.addCell(cell4);
		          table.addCell(cell5);
		          table.addCell(cell6);
		          table.addCell(cell7);
		          i ++;
	          }

	          document.add(table);

	          document.close();

	        } catch(Exception e){
	          e.printStackTrace();
	        }
	   }
   
   private static Paragraph addString(String s) {
	 	  Chunk q = new Chunk(s, HELVETICA);
	 	  Phrase o = new Phrase(q);
	 	  Paragraph p = new Paragraph();
	 	  p.add(o);
	 	  return p;	   
   }
   
   private static Paragraph addHeader(String s) {
	 	  Chunk q = new Chunk(s, ARIAL);
	 	  Phrase o = new Phrase(q);
	 	  Paragraph p = new Paragraph();
	 	  p.add(o);
	 	  return p;	   
   }
   
	public static String formatDateToString(Date date, String format,
			String timeZone) {
		// null check
		if (date == null) return null;
		// create SimpleDateFormat object with input format
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		// default system timezone if passed null or empty
		if (timeZone == null || "".equalsIgnoreCase(timeZone.trim())) {
			timeZone = Calendar.getInstance().getTimeZone().getID();
		}
		// set timezone to SimpleDateFormat
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		// return Date in required format with timezone as String
		return sdf.format(date);
	}
   

}
