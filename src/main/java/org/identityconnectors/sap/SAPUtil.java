/**                 ***COPYRIGHT STARTS HERE***
 * Copyright (C) 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * SUN PROPRIETARY/CONFIDENTIAL.
 *
 * U.S. Government Rights - Commercial software. Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and applicable
 * provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.Sun,
 * Sun Microsystems, the Sun logo, Java, Solaris and Sun Identity Manager
 * are trademarks or registered trademarks of Sun Microsystems, Inc.
 * or its subsidiaries in the U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively
 * licensed through X/Open Company, Ltd.
 *                  ***COPYRIGHT ENDS HERE***                                */
package org.identityconnectors.sap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoTable;

/**
 * Utility class to supply multiple SAP operations with functionality.
 * 
 * @author bfarrell
 * @version 1.0
 * @since 1.0
 */
public class SAPUtil {
	private static final Log log = Log.getLog(SAPUtil.class);

	/**
	 * Creates a date from a string
	 * 
	 * @param date
	 * @param format
	 * @return Date
	 * @throws ConnectorException
	 *             - wrapper for ParseException
	 */
	public static Date stringToDate(String date, String format)
			throws ConnectorException {
		log.info("BEGIN");
		if (format == null)
			format = "MM/dd/yyyy";
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException pe) {
			log.error(pe.getMessage());
			throw new ConnectorException(pe);
		}
		log.info("RETURN");
		return d;
	}

	/**
	 * Method to covert dats to strings.
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		String formattedDate;
		log.info("BEGIN");
		if (format == null)
			format = "MM/dd/yyyy";
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		formatter.setTimeZone(TimeZone.getDefault());
		formattedDate = formatter.format(date);
		log.info("RETURN");
		return formattedDate;
	}


	/**
	 * Creates a long from a Date
	 * 
	 * @param String
	 * @return Date
	 * @throws ConnectorException
	 *             - wrapper for ParseException
	 */
	public static Date longToDate(String value)
	throws ConnectorException {
		Date d = null;
		 //new Long object
		  Long lObj = Long.parseLong(value);
		  d = new Date(lObj);
		return d;
	}

	/**
	 * This method translates the complex string into a list.
	 * 
	 * @param str
	 * @param delim
	 * @param returnUpdateFlag
	 *            - determine whether the update flag should be part of the
	 *            return list
	 * @return
	 */
	public static List<String> getComplexFormat(String str, String delim,
			boolean returnUpdateFlag) {
		log.info("BEGIN");
		String[] format = str.split(delim);
		int formatLength = format.length;
		if (!returnUpdateFlag) {
			String[] tempFormat = format;
			format = new String[--formatLength];
			System.arraycopy(tempFormat, 1, format, 0, formatLength);
		}
		log.info("RETURN");
		return Arrays.asList(format);
	}

	/**
	 * Get subsystems assigned to the given user.
	 * 
	 * @param accountId
	 * @param function
	 * @return subsystems - ArrayList<String>
	 * @throws ConnectorException
	 */
	public static ArrayList<String> getSubSystems(String accountId,
			Function function, SAPConnection conn, SAPConfiguration config)
			throws ConnectorException {
		log.info("BEGIN");
		ArrayList<String> subSystems = null;
		if (accountId != null && config.getEnableCUA()) {
			// set cuaSystems from function
			try {
				if (function == null) {
					function = new Function("BAPI_USER_GET_DETAIL", accountId,
							conn, config.getConnectorMessages(), true, config
									.getRetryWaitTime());
					function.execute();
				}
				JCoTable systems = function.getTable("SYSTEMS");
				if (systems != null) {
					int rows = systems.getNumRows();
					subSystems = new ArrayList<String>(rows);
					for (int i = 0; i < rows; i++) {
						systems.setRow(i);
						String sysName = systems.getString("SUBSYSTEM");
						if (sysName != null) {
							subSystems.add(sysName);
						}
					}
				}
			} catch (JCoException e) {
				String message = config.getMessage("SAP_ERR_CUASYSTEMS",
						accountId);
				log.error(e, message);
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
		}
		log.info("RETURN");
		return subSystems;
	}

	/**
	 * This method retrieves the profiles/local profiles for the user.
	 * 
	 * @param accountId
	 * @param func
	 * @param conn
	 * @param config
	 * @param tableFormats
	 * @return
	 * @throws ConnectorException
	 */
	public static List<String> getProfiles(String accountId, Function func,
			SAPConnection conn, SAPConfiguration config,
			Map<String, String> tableFormats) throws ConnectorException {
		log.info("BEGIN");
		boolean cua = config.getEnableCUA();
		List<String> profs = null;
		if (accountId != null) {
			try {
				Function function;
				if (cua) {
					// if it CUA environment, we need to get user the local
					// profiles function
					function = new Function("BAPI_USER_LOCPROFILES_READ",
							accountId.toUpperCase(), conn, config
									.getConnectorMessages(), cua, config
									.getRetryWaitTime());
					function.executeWithRetry(config.getMaxBAPIRetries());
					function.jcoErrorCheck();
				} else {
					function = func;
				}
				String tableName = "PROFILES";
				String format = tableFormats.get(tableName);
				if (format != null) {
					JCoTable returnTable = function.getTable(tableName);
					int rows = returnTable.getNumRows();
					// get the profile format defined by user
					List<String> profRows = getComplexFormat(format,
							SAPConnector.ESCAPED_COMPLEX_DELIMITER, false);
					StringBuffer sb = null;
					for (int i = 0; i < rows; i++) {
						sb = new StringBuffer();
						returnTable.setRow(i);
						// retrieve the systems
						if (cua) {
							sb.append(returnTable.getString("SUBSYSTEM"));
							sb.append(SAPConnector.COMPLEX_DELIMITER);
						}
						int numProfRows = profRows.size();
						for (String profRow : profRows) {
							String rowValue = returnTable.getString(profRow);
							log.info("{0} : {1}", profRow, rowValue);
							sb.append(rowValue);
							if (--numProfRows > 0) {
								sb.append(SAPConnector.COMPLEX_DELIMITER);
							}
						}
						if (profs == null) {
							profs = new ArrayList<String>();
						}
						profs.add(sb.toString());
					}
				} else {
					String message = config.getMessage("SAP_ERR_TABLE_FORMAT",
							tableName);
					log.error(message);
					throw new ConnectorException(message);
				}

			} catch (JCoException e) {
				String message = config.getMessage("SAP_ERR_LOC_PROFILE_FETCH",
						accountId);
				ConnectorException ce = new ConnectorException(message, e);
				log.error(ce, message);
				throw ce;
			}
		}
		log.info("RETURN");
		return profs;
	}

	/**
	 * This method returns the activity groups/local activity groups for the
	 * user.
	 * 
	 * @param accountId
	 * @param func
	 * @param conn
	 * @param config
	 * @param tableFormats
	 * @return
	 * @throws ConnectorException
	 */
	public static List<String> getActivityGroups(String accountId,
			Function func, SAPConnection conn, SAPConfiguration config,
			Map<String, String> tableFormats) throws ConnectorException {
		log.info("BEGIN");
		boolean cua = config.getEnableCUA();
		List<String> actgrps = null;
		if (accountId != null) {
			try {
				Function function;
				if (cua) {
					// if it CUA environment, we need to get user the local
					// activity group function
					function = new Function("BAPI_USER_LOCACTGROUPS_READ",
							accountId, conn, config.getConnectorMessages(),
							config.getEnableCUA(), config.getRetryWaitTime());
					function.executeWithRetry(config.getMaxBAPIRetries());
					function.jcoErrorCheck();
				} else {
					function = func;
				}

				String tableName = "ACTIVITYGROUPS";
				String format = tableFormats.get(tableName);
				if (format != null) {
					JCoTable returnTable = function.getTable(tableName);
					int rows = returnTable.getNumRows();
					// get the activity group format defined by the user
					List<String> actgrpRows = getComplexFormat(format,
							SAPConnector.ESCAPED_COMPLEX_DELIMITER, false);
					StringBuffer sb = null;
					for (int i = 0; i < rows; i++) {
						sb = new StringBuffer();
						returnTable.setRow(i);
						// retrieve the systems
						if (cua) {
							sb.append(returnTable.getString("SUBSYSTEM"));
							sb.append(SAPConnector.COMPLEX_DELIMITER);
						}
						int numActGrpRows = actgrpRows.size();
						for (String actgrpRow : actgrpRows) {
							String rowValue;
							if (actgrpRow.equals("TO_DAT")
									|| actgrpRow.equals("FROM_DAT")) {
								Date dat = returnTable.getDate(actgrpRow);
								rowValue = dateToString(dat, "MM/dd/yyyy");
							} else {
								rowValue = returnTable.getString(actgrpRow);
							}

							// get space for org flag instead of empty string
							// so that we equal what is passed in
							if (actgrpRow.equals("ORG_FLAG")
									&& rowValue.equals("")) {
								rowValue = " ";
							}

							log.info("{0} : {1}", actgrpRow, rowValue);
							sb.append(rowValue);
							if (--numActGrpRows > 0) {
								sb.append(SAPConnector.COMPLEX_DELIMITER);
							}
						}
						if (actgrps == null) {
							actgrps = new ArrayList<String>();
						}
						actgrps.add(sb.toString());
					}
				} else {
					String message = config.getMessage("SAP_ERR_TABLE_FORMAT",
							tableName);
					log.error(message);
					throw new ConnectorException(message);
				}
			} catch (JCoException e) {
				String message = config.getMessage("SAP_ERR_ACTGRP_FETCH",
						accountId);
				ConnectorException ce = new ConnectorException(message, e);
				log.error(ce, message);
				throw ce;
			}
		}
		log.info("RETURN");
		return actgrps;
	}

	/**
	 * Build a multimessage ConnectorException.
	 * 
	 * @param ce
	 * @param message
	 * @return
	 */
	public static ConnectorException addConnectorExceptionMessage(Exception e,
			String message) {
		log.info("BEGIN");
		StringBuffer sb = new StringBuffer();
		if (e != null) {
			sb.append(e.getMessage());
			sb.append('\n');
		}
		log.error(message);
		sb.append(message);
		log.info("RETURN");
		return new ConnectorException(message);
	}
	
    /**
	 * Description: Populates the HashMap of the attribute map lookup
	 * definitions used for user Management reconciliation.Returns the HashMap
	 * with Code Key value as BAPI Structure Name and Decode value as ArrayList
	 * of AttributeMapBean with each AttributeMapBean containing OIMFieldName,
	 * SAP Field Name, SAP Structure, and Child Table Name for attributes
	 * specified in the lookup definition
	 * 
	 * @param sAttributeMapLookupName
	 *            Name of the lookup definition containing target attribute
	 *            mapping fields
	 * @param lookIntf
	 *            Lookup interface to get all lookup values mentioned in the
	 *            lookup definition
	 * @param isCUAEnabled
	 *            Boolean field that specifies whether the target system is SAP
	 *            R/3 or SAP CUA
	 * @return HashMap
	 * 		   Returns the HashMap with Code Key value as BAPI Structure Name
	 *         and Decode value as ArrayList of AttributeMapBean with each
	 *         AttributeMapBean containing OIMFieldName, SAP Field Name, SAP
	 *         Structure, and Child Table Name for attributes specified in the
	 *         lookup definition
	 * 
	 * @throws ConnectorException
	 * 
	 */
	public static HashMap<String,ArrayList<SAPUMAttributeMapBean>> initializeTargetReconAttrMap(ArrayList attributesToget,
		 boolean isCUAEnabled)
			throws ConnectorException {
		log.info("BEGIN");
		String sStructure = null;
		HashMap<String,ArrayList<SAPUMAttributeMapBean>> dataMap = new HashMap<String,ArrayList<SAPUMAttributeMapBean>>();
		ArrayList<SAPUMAttributeMapBean> userDataList = null;	
		try {				
			String sDecode = null;
			/*
			 * Loop through the Arraylist and place the decode values in the Bean after splitting it
			 * and return Hashtable containing segment name as key and all its
			 * attributes as value
			 */		
			int lenght=attributesToget.size();
			 for (int i=0;i<lenght;i++) {
				sDecode=(String)attributesToget.get(i);
				String[] keyArr = sDecode.split(";");	
				int keyArrLength=keyArr.length;
				SAPUMAttributeMapBean oAttributeMapBean = new SAPUMAttributeMapBean();				
				 if(!StringUtil.isEmpty(sDecode)) {	
					sStructure=keyArr[1];
					if (sStructure.equalsIgnoreCase("UCLASS|UCLASSSYS")) {
						if (!isCUAEnabled) {
							sStructure = sStructure.substring(0, sStructure
									.indexOf('|'));
						} else {
							sStructure = sStructure.substring(sStructure
									.indexOf('|') + 1);
						}
					}					
					// If dataMap contains a key with Structure
					if ((dataMap != null) && (sStructure != null)
							&& dataMap.containsKey(sStructure)) {
						userDataList = (ArrayList<SAPUMAttributeMapBean>) dataMap.get(sStructure);
					} else {
						userDataList = new ArrayList<SAPUMAttributeMapBean>();
					}

					oAttributeMapBean.setOIMfieldName(sDecode);					
					oAttributeMapBean.setBapiFieldName(keyArr[0]);									
					oAttributeMapBean.setBapiStructure(sStructure);
					if(keyArrLength>4){
						oAttributeMapBean.setSBAPINAME(keyArr[4]);
						oAttributeMapBean.setUserIDKeyField(keyArr[5]);	
					}
					userDataList.add(oAttributeMapBean);
					dataMap.put(sStructure, userDataList);
				}
			}
		} catch (Exception e) {
			throw new ConnectorException(
					"Error occured during initializeTargetReconFieldMap");
		}
		log.info("RETURN");
		return dataMap;
	}	
	
	
	
	/**
	 * Description: Populates the HashMap of the custom attribute map lookups
	 * used for reconciliation.Returns the HashMap with the Code Key value as
	 * the BAPI Name and Decode value as ArrayList of AttributeMapBean with each
	 * AttributeMapBean containing OIMFieldName, SAP Field Name, SAP Structure,
	 * and User ID Field for attributes specified in the lookup definition
	 * 
	 * 
	 * @param sAttributeMapLookupName
	 *            Name of the lookup definition containing the custom target
	 *            attribute mapping fields
	 * @param lookIntf
	 *            Lookup Interface to get all lookup values mentioned in the
	 *            lookup definition
	 * 
	 * @return HashMap Returns the HashMap with the Code Key value as the BAPI
	 *         Name and Decode value as ArrayList of AttributeMapBean with each
	 *         AttributeMapBean containing OIMFieldName, SAP Field Name, SAP
	 *         Structure, and User ID Field for attributes specified in the
	 *         lookup definition
	 * 
	 * @throws ConnectorException
	 * 
	 */
	public static HashMap<String,ArrayList<SAPUMAttributeMapBean>> initializeCustomAttrMap(ArrayList<String> attributesToget,
			 boolean isCUAEnabled) throws ConnectorException {	
		log.info("BEGIN");
		HashMap<String,ArrayList<SAPUMAttributeMapBean>> dataMap = new HashMap<String,ArrayList<SAPUMAttributeMapBean>>();
		ArrayList<SAPUMAttributeMapBean> userDataList = null;
		String sBAPIName = null;
		try {
			String sDecode = null;
			/*
			 * Loop through the Arraylist and place the decode values in the Bean after splitting it
			 * and return Hashtable containing segment name as key and all its
			 * attributes as value
			 */		
			int lenght=attributesToget.size();
			 for (int i=0;i<lenght;i++) {
				 sDecode=(String)attributesToget.get(i);
				 String[] keyArr = sDecode.split(";");	
				 int keyArrLength=keyArr.length;
					
				 SAPUMAttributeMapBean oAttributeMapBean = new SAPUMAttributeMapBean();
				if (!StringUtil.isEmpty(sDecode)) {					
					sBAPIName = keyArr[4];
					// If dataMap contains a key with Structure
					if ((dataMap != null) && (sBAPIName != null)
							&& dataMap.containsKey(sBAPIName)) {
						userDataList = (ArrayList<SAPUMAttributeMapBean>) dataMap.get(sBAPIName);
					} else {
						userDataList = new ArrayList<SAPUMAttributeMapBean>();
					}
					oAttributeMapBean.setOIMfieldName(sDecode);					
					oAttributeMapBean.setBapiFieldName(keyArr[0]);
					oAttributeMapBean.setBapiStructure(keyArr[1]);
					if(keyArrLength>4){
						oAttributeMapBean.setSBAPINAME(sBAPIName);
						oAttributeMapBean.setUserIDKeyField(keyArr[5]);	
					}
					userDataList.add(oAttributeMapBean);
					dataMap.put(sBAPIName, userDataList);
				}
			}
		} catch (Exception e) {
			throw new ConnectorException(
					"Error occured during initializeTargetReconFieldMap");
		}
		log.info("RETURN");
		return dataMap;
	}
	/**
     * For multi-valued attributes, get the values from the table specified.
     * @param tableName
     * @param function
     * @param connectorMessages
     * @param tableFormats
     * @param cua - set to True if SUBSYSTEM is expected as the first column
     * @return
     * @throws JCoException
     */
    public static List<String> getTableRows(String tableName, Function function, ConnectorMessages connectorMessages,
                                               Map<String, String> tableFormats, boolean cua) throws JCoException {
    	log.info("BEGIN");
        List<String> rowVals = null;
        if (tableFormats != null && function != null) {
            String tableFormat = tableFormats.get(tableName);
            if (tableFormat != null) {
                JCoTable returnTable = function.getTable(tableName);
                int rows = returnTable.getNumRows();
                List<String> tableRows = SAPUtil.getComplexFormat(tableFormat, SAPConnector.ESCAPED_COMPLEX_DELIMITER, false);
                StringBuffer sb = null;
                for (int i = 0; i < rows; i++) {
                    sb = new StringBuffer();
                    returnTable.setRow(i);
                    if (cua) { // retrieve subsystem if applicable
                        sb.append(returnTable.getString("SUBSYSTEM"));
                        sb.append(SAPConnector.COMPLEX_DELIMITER);
                    }
                    int numTableRows = tableRows.size();
                    for (String tableRow : tableRows) {
                        String rowValue = returnTable.getString(tableRow);
                        log.info("{0} : {1}", tableRow, rowValue);
                        sb.append(rowValue);
                        if (--numTableRows > 0) {
                            sb.append(SAPConnector.COMPLEX_DELIMITER);
                        }
                    }
                    if (rowVals == null) {
                        rowVals = new ArrayList<String>();
                    }
                    rowVals.add(sb.toString()); 
                }
            } else {
                throwFormatError(connectorMessages, "SAP_ERR_TABLE_FORMAT", tableName);;
            }
        } else {
            throwFormatError(connectorMessages, "SAP_ERR_TABLE_FORMAT", tableName);
        }
        log.info("RETURN");
        return rowVals;
    }
    
    /**
     * Method to throw a quick exception
     * @param connectorMessages
     * @param tableName
     */
    private static void throwFormatError(ConnectorMessages connectorMessages, String errorMsg, Object... args) {
    	log.info("BEGIN");
        String message = null;
        if (connectorMessages != null)
            message = connectorMessages.format(errorMsg, errorMsg, args);
        else 
            message = "Problem with connector messages in SAPUtil";
        log.error(message);
        log.info("RETURN");
        throw new ConnectorException(message);
    }
    
    
    /**
	 * Description: Runs the custom query specified for each user record.
	 * Returns true if the query is valid, otherwise, it returns false..
	 * 
	 * @param hmQueryDetails
	 *            Hashmap containing details of user records to be reconciled to
	 *            Oracle Identity Manager. This contains results of parent data
	 *            fields reconciled from the target system.
	 * 
	 * @param sQuery
	 *            Result Set of the user for getting values from Oracle Identity
	 *            Manager. For example: First Name=John & Last Name=Doe
	 * @return boolean
	 * 			  Returns true if the query condition is met, otherwise, returns
	 *            false
	 * @throws ConnectorException
	 * 
	 */
	public static boolean executeCustomQuery(HashMap<String, String> hmQueryDetails,
			String sQuery) throws ConnectorException {
		log.info("BEGIN");
		boolean isValid = false;		
		try {
			String AND_SPLIT_REGEX = "\\s[&]\\s";
			String OR_SPLIT_REGEX = "\\s[|]\\s";
			int iNoOfOR;
			int iNoOfAnd;
			String sKey;
			String sValue;
			// exp : regular expression variable for spliting the query
			// according to "|"
			String sArrORExp[] = sQuery.split(OR_SPLIT_REGEX);
			iNoOfOR = sArrORExp.length - 1;			
			for (int i = 0; i <= iNoOfOR; i++) {
				// exp : regular expression variable for splitting the query
				// according
				// to "&"
				String sArrANDExp[] = sArrORExp[i].split(AND_SPLIT_REGEX);
				iNoOfAnd = sArrANDExp.length - 1;
				for (int j = 0; j <= iNoOfAnd; j++) {				
					int iEquals = 0;
					// Get the key and value by checking first index of '='
					// Throw exception if query does not have '=' operator b/w
					// key and
					// value
					iEquals = sArrANDExp[j].indexOf('=');

					sKey = sArrANDExp[j].substring(0, iEquals).trim();
					sValue = sArrANDExp[j].substring(iEquals + 1).trim();
					/*
					 * Check if the query condition gets satisfied.If
					 * satisfied,then continue and check if any more and
					 * condition need to be validated. If all AND conditions are
					 * satisfied,then return true If query condition fails for
					 * AND,then check if any more OR conditions are to be
					 * validated. If all query condition for OR fails,return
					 * false
					 */
					// Below condition updated for filter enhancement -  Bug 18998725
					if (Uid.NAME.equalsIgnoreCase(sKey) || Name.NAME.equalsIgnoreCase(sKey) 
							|| ((String) hmQueryDetails.get(sKey)).equalsIgnoreCase(sValue)) {
						isValid = true;
					} else {
						isValid = false;
						iNoOfAnd = -1;
					}
				}
				if (isValid) {
					iNoOfOR = -1;
				}
			}			
		} catch (Exception e) {
			isValid = false;			
		}
		log.info("RETURN");
		return isValid;
	}
	
	/**
	 * Description: Gets the BAPI name for multi-valued attributes based on
	 * the structure name
	 * 
	 * @param sStructureName
	 *            Structure name in the BAPI. The BAPI field is part of the
	 *            structure. For example: ADDRESS
	 * @param isCUA
	 *            Specifies whether the target is SAP R/3 or SAP CUA
	 * @return String String containing the BAPI name
	 * 
	 */
	public static String getAddMultiValueDataBAPIName(String sStructureName,
			boolean isCUA) {
		log.info("BEGIN");
		String sReturnBAPIName = "BAPI_USER_CHANGE";
		if (isCUA) {
			if (sStructureName
					.equalsIgnoreCase("roles")) {
				sReturnBAPIName = "BAPI_USER_LOCACTGROUPS_ASSIGN";
			} else if (sStructureName.equalsIgnoreCase("PROFILES")) {
				sReturnBAPIName = "BAPI_USER_LOCPROFILES_ASSIGN";
			}
		} else {
			if (sStructureName
					.equalsIgnoreCase("roles")) {
				sReturnBAPIName = "BAPI_USER_ACTGROUPS_ASSIGN";
			} else if (sStructureName.equalsIgnoreCase("PROFILES")) {
				sReturnBAPIName = "BAPI_USER_PROFILES_ASSIGN";
			}
		}
		log.info("RETURN");
		return sReturnBAPIName;
	}
	
	/*
	 * Utility to convert GuardedString to String
	 * 
	 * @return String
	 */
	public static String decode(final GuardedString string) {
		log.info("BEGIN");
		if (string == null) {
			return null;
		}
		final char[] _array = new char[50];
		string.access(new GuardedString.Accessor() {
        	public void access(char[] clearChars) {
				try {
			        System.arraycopy(clearChars, 0, _array, 0, clearChars.length); 
					//destProps.put(JCO_PASSWD, new String(clearChars));	
				} catch (Exception sException) {
					log.error(sException.getMessage());
				}
        	}
        });
		String decoded = new String(_array).trim(); 
		/*GuardedStringAccessor accessor = new GuardedStringAccessor();
		string.access(accessor);
		String decoded = new String(accessor.getArray());
		accessor.clear();*/
		log.info("RETURN");
		return decoded;
	}
	
	
}
