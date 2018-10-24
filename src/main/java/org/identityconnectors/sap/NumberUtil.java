package org.identityconnectors.sap;

/**
 * Description: Contains Number utility methods that are used by the connector
 *
 */
public class NumberUtil {
     /**
       * Description: Check whether or not the value is numeric 
       *
       * @param args - String value to check. For example:: 10 
       *
       * @return true if the string is a valid number, otherwise, returns false.
       *
       *
       */
    public boolean isNumeric(String args) {
        boolean isNumber = false;
        long value = 0;

        if (!(args == null) && !"".equals(args.trim())) {
            try {
                value = Long.parseLong(args);
                isNumber = true;
            } catch (NumberFormatException e) {
                isNumber = false;
            }
        }

        if (value < 0) {
            isNumber = false;
        }

        return isNumber;
    }

}
