/**
 * @author P1330080
 * 
 *         Author : SEET HING LONG DATE : 24 NOV 2020
 * 
 */

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.http.client.utils.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sg.com.ncs.paymentgateway.common.util.DateUtil;
import sg.com.ncs.paymentgateway.giro.app.model.GiroReport;

public class TableToXLS {
   public static void printXLS(String file, List<GiroReport> giroList) throws Exception {

      //Create blank workbook
      XSSFWorkbook workbook = new XSSFWorkbook();
      
      //Create a blank sheet
      XSSFSheet spreadsheet = workbook.createSheet( " Giro Billing Summary ");

      //Create row object
      XSSFRow row;

      //This data needs to be written (Object[])
      Map < String, Object[] > giroBillingSummaryMap = new TreeMap < String, Object[] >();
      //9 columns
      giroBillingSummaryMap.put( "00", new Object[] {
    		 "SN", "Reference", "Company", "Billing Date", "Giro Status", "Account Status", "Previous Balance", "Outstanding Amount", "Total Amount" });
      
      int i = 1;
      
      int previousBalance = 0;
      int outstandingAmount = 0;
      int totalAmount = 0;
      
      String index = "";
      
      int length = giroList.size();
      
      for ( GiroReport giroReport : giroList ) {
    	  
          String Reference = giroReport.getBlCuCustRefNo() == null?"NULL":giroReport.getBlCuCustRefNo();
          String Company = giroReport.getCompanyName() == null?"NULL":giroReport.getCompanyName();
          String PreviousBalance = giroReport.getPyPreviousBalance() == null?"0":giroReport.getPyPreviousBalance().toString();
          String giStatus = giroReport.getGiStatus()  == null?"NULL": giroReport.getGiStatus().toString();
          String accStatus = giroReport.getAccStatus() == null?"NULL": giroReport.getAccStatus().toString();
          String BillingDate = formatDateToString(giroReport.getBlDate(),DateUtil.YYYYMMDD, "");
          
          previousBalance = previousBalance + Integer.valueOf(PreviousBalance);
          
          String TotalAmount = giroReport.getBlTotalAmount() == null? "0": giroReport.getBlTotalAmount().toString();
          
          totalAmount = totalAmount + Integer.valueOf(TotalAmount);
          
          String OutstandingAmount = giroReport.getPyOutstanding() == null? "0": giroReport.getPyOutstanding().toString();
          
          outstandingAmount = outstandingAmount + Integer.valueOf(OutstandingAmount);
          
          index = String.valueOf(i);
          
          while ( index.length() < length) {
        	  
        	  index = "0" + index ;
          }
    	  
          giroBillingSummaryMap.put( index , new Object[] {
    			  String.valueOf(i),Reference, Company, BillingDate, giStatus, accStatus, PreviousBalance, OutstandingAmount, TotalAmount });
      
    	  i ++;
    	  
      }
      index = String.valueOf(i);
      
      while ( index.length() < length) {
    	  
    	  index = "0" + index ;
    	  
      }
      giroBillingSummaryMap.put( index, new Object[] {
    		  " "," ", " ", " ", " ", "", String.valueOf(previousBalance), String.valueOf(outstandingAmount), String.valueOf(totalAmount) });


      Set <String> keyid = giroBillingSummaryMap.keySet();
      
      List<String> keyidSorted = keyid.stream().collect(Collectors.toList());      
      
      Collections.sort(keyidSorted, (o1, o2) -> o1.compareTo(o2));
      
      int rowid = 0;
      
      for (String key : keyidSorted) {
         row = spreadsheet.createRow(rowid++);
         Object [] objectArr = giroBillingSummaryMap.get(key);
         int cellid = 0;
         
         for (Object obj : objectArr){
            Cell cell = row.createCell(cellid++);
            cell.setCellValue((String)obj);
         }
      }
      //Write the workbook in file system
      FileOutputStream out = new FileOutputStream(
         new File(file));
      
      workbook.write(out);
      out.close();
      workbook.close();
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