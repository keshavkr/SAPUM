package org.identityconnectors.sap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.common.logging.Log;



/**
 * Description: Contains Date utility methods that are used by the connector
 *
 */
public class DateUtil {
    
	private static final Log log = Log.getLog(Create.class);

    /**
     * Description: Creates a DateUtil object
     *
     * @param logger 
     * 			logger instance
     */
    public DateUtil() {
      
    }

    /**
      * Description: Converts one date format to another
      *
      * @param dateValue 
      * 			Date value to be converted to other format 			
      * @param sourceDateFormat 
      * 			Format of dateValue
      * @param targetDateFormat 
      * 			Targeted date format
      * @return String
      * 			String with converted date format
      * 
      * @throws ParseException
      */
    public String dateFormatConversion(String dateValue,
                                       String sourceDateFormat,
                                       String targetDateFormat)
                                throws ParseException {
    	log.info("BEGIN");
        String date = null;
        DateFormat dateFormat = new SimpleDateFormat(sourceDateFormat);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(targetDateFormat);
        if (dateValue != null) {
            date = outputDateFormat.format((java.util.Date) (dateFormat.parse(dateValue)));
        }
        log.info("RETURN");
        return date;
    }

    	/**
         * Description: Converts String to Date depending on the DateFormat specified
         *
         * @param sDate 
         * 			Date value.
         * 		    For example:  20090102
         * @param dateFormat
         *           Contains the date string that needs to be parsed.
         *           For example: yyyyMMdd
         * @return Date 
         * 			Returns a Date object that is parsed in the format specified.
         *			 For example:  Fri Jan 02 00:00:00 GMT+05:30 2009
         * @throws ConnectorException
         *
         */
    public Date returnDate(String sDate, String dateFormat)
                    throws ConnectorException {
    	log.info("BEGIN");
        DateFormat formatter = new SimpleDateFormat(dateFormat);
        Date date = null;

        //Format the date based on Date format passed 
        try {
            date = formatter.parse(sDate);
        } catch (ParseException e) {
            throw new ConnectorException(e.getMessage());
        }
        log.info("RETURN");
        return date;
    }
    
    
    /**
	 * Description: Converts Date to String depending on the DateFormat
	 * specified
	 * 
	 * @param date
	 *            The date value. For example: Tue Aug 11 12:04:56 GMT+05:30
	 *            2009
	 * @param dateFormat
	 *            Contains the date string that needs to be parsed. For example:
	 *            yyyyMMddHHmmss
	 * @return String Returns the date object that is parsed in the format
	 *         specified as a String. For example: 20090810233825
	 * 
	 */
    public String parseTime(Date date, String dateFormat) {
    	log.info("BEGIN");
        DateFormat formatter = new SimpleDateFormat(dateFormat);
        String sDate = formatter.format(date);
        log.info("RETURN");
        return sDate;
    } 
    
    
    /**
	 * Description: Converts Date to String depending on the DateFormat and
	 * time zone format specified
	 * 
	 * @param date
	 *            The date value. For example: Tue Aug 11 12:04:56 GMT+05:30 2009
	 * @param dateFormat
	 *            Contains the date string that needs to be parsed. For example:
	 *            yyyyMMddHHmmss
	 * @param sTimeZoneFormat
	 *            Time zone format to the date that needs to be formatted. For example: PST
	 * @return String Returns the date object that is parsed in the format
	 *         specified as a String. For example: 20090810233825
	 * 
	 */
    public String parseTime(Date date, String dateFormat,String sTimeZoneFormat) {
    	log.info("BEGIN");
        DateFormat formatter = new SimpleDateFormat(dateFormat);
        TimeZone localGMTTimeZone = TimeZone.getTimeZone(sTimeZoneFormat);
		formatter.setTimeZone(localGMTTimeZone);
        String sDate = formatter.format(date);
        log.info("RETURN");
        return sDate;		
    } 
}
