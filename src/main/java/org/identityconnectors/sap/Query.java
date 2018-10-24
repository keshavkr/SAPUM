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

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.EmbeddedObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SyncDeltaBuilder;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.sap.Function;
import org.identityconnectors.sap.LockStatus;
import org.identityconnectors.sap.SAPConfiguration;
import org.identityconnectors.sap.SAPConnection;
import org.identityconnectors.sap.SAPUMAttributeMapBean;
import org.identityconnectors.sap.SAPUtil;
import org.identityconnectors.sap.DateUtil;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

/**
 * Implementor of the executeQuery operation for SAP
 * 
 * @author bfarrell
 * @version 1.0
 */
public class Query extends AbstractSAPOperation{
	private static final Log log = Log.getLog(Query.class);
	private HashMap<String,ArrayList<SAPUMAttributeMapBean>> hmCustomAttrMap;     
    ArrayList<String> alEmbeddedAttributes= new ArrayList<String>();
    private String subSystem=null;
    private String sBatchSize=null;
    private String sTimeZone=null;
    private String sIncrementalAttrName=null;
    private String sCustomQuery=null;
 // Start:: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
    private String [] roleParam = new String[2];
 // END :: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
    SAPCreateUpdateBase sSAPCreateUpdateBase;
 
	public Query(SAPConnection conn, SAPConfiguration config,
			Map<String, AttributeInfo> accountAttributes,
			Set<String> filteredAccounts, Map<String, String> tableFormats) {
		super(conn, config, accountAttributes, filteredAccounts, tableFormats);
		//Start:: Bug 17575026
		sSAPCreateUpdateBase = new SAPCreateUpdateBase(conn, config, accountAttributes, filteredAccounts, tableFormats);
		//End:: Bug 17575026
	}

	ObjectClass CUA_SYSTEMS = new ObjectClass("cuaSystems");
	ObjectClass ACTIVITY_GROUPS = new ObjectClass("activityGroups");
	ObjectClass PROFILES = new ObjectClass("profiles");
	ObjectClass TIME_ZONES = new ObjectClass("timeZones");
	ObjectClass USER_TYPE = new ObjectClass("usertype");
	ObjectClass TABLE = new ObjectClass("table");
	ObjectClass COMPANY = new ObjectClass("company");
	ObjectClass DATEFORMAT = new ObjectClass("dateformat");
	ObjectClass DECIMALNOTATION = new ObjectClass("decimalnotation");
	ObjectClass LANGUAGECOMM = new ObjectClass("languagecommunication");
	ObjectClass LOGONLANGUAGE = new ObjectClass("logonlanguage");
	ObjectClass TITLE = new ObjectClass("title");
	ObjectClass COMMTYPE = new ObjectClass("commtype");
	ObjectClass CONTRACTUALUSERTYPE = new ObjectClass("contractualusertype");
	ObjectClass PARAMETER = new ObjectClass("parameters");

	String company[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "COMPANY",
			"COMPANY", "COMPANY", "COMPANY", "USCOMPANY_ADDR", "SH" };
	String dateFormat[] = { "BAPI_HELPVALUES_GET", "GETDETAIL","DEFAULTS",
			"DATFM", "_LOW", "_TEXT" };
	String decimalNotation[] = { "BAPI_HELPVALUES_GET", "GETDETAIL",
			"DEFAULTS", "DCPFM", "_LOW", "_TEXT" };
	String languageComm[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "ADDRESS",
			"LANGU_P", "SPRAS", "SPTXT" };
	String timeZone[] = { "BAPI_HELPVALUES_GET", "CHANGE", "ADDRESS",
			"TIME_ZONE", "TZONE", "DESCRIPT" };
	String userGroups[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "GROUPS",
			"USERGROUP", "USERGROUP", "TEXT" };
	String title[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "ADDRESS",
			"TITLE_P", "TITLE_MEDI", "TITLE_MEDI", "ADDR2_SH_TITLE", "SH",
			"PERSON", "I", "EQ", "X" };
	String roles[] = { "RFC_READ_TABLE", "USRSYSACTT", "AGR_NAME", "TEXT",
			"SUBSYSTEM", "USRSYSACT", "LANGU = 'EN'" };
	String profiles[] = { "RFC_READ_TABLE", "USRSYSPRFT", "PROFN", "PTEXT",
			"SUBSYSTEM", "USRSYSPRF", "LANGU = 'EN'" };
	String commType[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "ADDRESS",
			"COMM_TYPE", "COMM_TYPE", "COMM_TEXT" };
	String system[] = { "RFC_READ_TABLE", "USZBVLNDRC", "RCVSYSTEM",
			"RCVSYSTEM" };
	String contUserType[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "UCLASSSYS",
			"LIC_TYPE", "USERTYP", "UTYPTEXT", "LANGU", "I", "EQ", "EN" };
	
	String usertype[] = {"BAPI_HELPVALUES_GET","GETDETAIL","LOGONDATA","USTYP","_LOW","_TEXT"};
	String parameter[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "PARAMETER",
			"PARID", "PARAMID", "PARTEXT" };

	
	/**
	 * Execute the actual executeQuery command.
	 * 
	 * @param objClass
	 * @param query
	 * @param handler
	 * @param options
	 */
	public void executeQuery(ObjectClass objClass, String query,
			ResultsHandler handler, OperationOptions options) { 
		log.info("BEGIN");
		HashMap allObjects = null;
		ArrayList<String> allObjectsaks = null;		
		Map<String, Object> opts = new HashMap<String, Object>();
		opts.putAll(options.getOptions());
		ArrayList<String> attrsToGet = new ArrayList<String>();
						
		if (ObjectClass.ACCOUNT.equals(objClass)) {
			executeQueryAccount(query, handler, options);
		} else if (COMPANY.equals(objClass)) {
			allObjects = getLookupValuesBAPI(company);
			attrsToGet.add(company[4]);
			attrsToGet.add(company[5]);
		} else if (DATEFORMAT.equals(objClass)) {
			allObjects = getLookupValuesBAPI(dateFormat);
			attrsToGet.add(dateFormat[4]);
			attrsToGet.add(dateFormat[5]);
		} else if (DECIMALNOTATION.equals(objClass)) {
			allObjects = getLookupValuesBAPI(decimalNotation);
			attrsToGet.add(decimalNotation[4]);
			attrsToGet.add(decimalNotation[5]);
		} else if (TIME_ZONES.equals(objClass)) {
			allObjects = getLookupValuesBAPI(timeZone);
			attrsToGet.add(timeZone[4]);
			attrsToGet.add(timeZone[5]);
		} else if (ObjectClass.GROUP.equals(objClass)) {
			allObjects = getLookupValuesBAPI(userGroups);
			attrsToGet.add(userGroups[4]);
			attrsToGet.add(userGroups[5]);
		} else if (TITLE.equals(objClass)) {
			allObjects = getLookupValuesBAPI(title);
			attrsToGet.add(title[4]);
			attrsToGet.add(title[5]);
		} else if (COMMTYPE.equals(objClass)) {
			allObjects = getLookupValuesBAPI(commType);
			attrsToGet.add(commType[4]);
			attrsToGet.add(commType[5]);
		} else if (CONTRACTUALUSERTYPE.equals(objClass)) {
			allObjects = getLookupValuesBAPI(contUserType);
			attrsToGet.add(contUserType[4]);
			attrsToGet.add(contUserType[5]);
		} else if (PARAMETER.equals(objClass)) {
			allObjects = getLookupValuesBAPI(parameter);
			attrsToGet.add(parameter[4]);
			attrsToGet.add(parameter[5]);
		} else if (CUA_SYSTEMS.equals(objClass)) {
			if(_configuration.getEnableCUA()){
				allObjects = getLookupValuesRFC(system);
				attrsToGet.add(system[2]);
				attrsToGet.add(system[3]);
			} else {
				allObjects = new HashMap<String, String>();
				attrsToGet.add(system[2]);
				attrsToGet.add(system[3]);
				allObjects.put(_configuration.getmasterSystem(), _configuration.getmasterSystem());
			}
		} else if (ACTIVITY_GROUPS.equals(objClass)) {
			if(_configuration.getEnableCUA()){
				allObjects = getRolesProfiles(roles);
				attrsToGet.add(roles[4]);
				attrsToGet.add(roles[5]);
			} else {
				// Start:: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
				log.info("_configuration.getSingle_Roles():: "
						+ _configuration.getSingleRoles());
				log.info("_configuration.getComposite_Roles():: "
						+ _configuration.getCompositeRoles());

				if (_configuration.getSingleRoles().equalsIgnoreCase("YES")
						&& (_configuration.getCompositeRoles()
								.equalsIgnoreCase("NO"))) {
					roleParam[0] = "AGR_SINGLE";
					roleParam[1] = "SH";

				} else if (_configuration.getCompositeRoles().equalsIgnoreCase(
						"YES")
						&& (_configuration.getSingleRoles()
								.equalsIgnoreCase("NO"))) {

					roleParam[0] = "AGR_COLL";
					roleParam[1] = "SH";
				} else {

					roleParam[0] = "";
					roleParam[1] = "";
				}

				log.info("roleparam:: " + roleParam[0] + "  ::SH:: "
						+ roleParam[1]);
				String role[] = { "BAPI_HELPVALUES_GET", "GETDETAIL",
						"ACTIVITYGROUPS", "AGR_NAME", "AGR_NAME", "TEXT",
						roleParam[0], roleParam[1] };
				// END :: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
				allObjects = getLookupValuesBAPI(role);
				attrsToGet.add("SUBSYSTEM");
				attrsToGet.add("USRSYSACT");
			}
		} else if (PROFILES.equals(objClass)) {
			if(_configuration.getEnableCUA()){
				allObjects = getRolesProfiles(profiles);
				attrsToGet.add(profiles[4]);
				attrsToGet.add(profiles[5]);
			} else {
				String profiles[] = { "BAPI_HELPVALUES_GET", "GETDETAIL", "PROFILES", "BAPIPROF",
						"PROFN", "PTEXT" };
				allObjects = getLookupValuesBAPI(profiles);
				attrsToGet.add("SUBSYSTEM");
				attrsToGet.add("USRSYSPRF");
			}
		} else if (LANGUAGECOMM.equals(objClass)
				|| LOGONLANGUAGE.equals(objClass)) {
			allObjects = getLookupValuesBAPI(languageComm);
			attrsToGet.add(languageComm[4]);
			attrsToGet.add(languageComm[5]);
		}
		 else if (USER_TYPE.equals(objClass)) {
				allObjects = getLookupValuesBAPI(usertype);
				attrsToGet.add(usertype[4]);
				attrsToGet.add(usertype[5]);
			}
		 else if (TABLE.equals(objClass)) {
			 allObjectsaks = getTableEntries(options.getOptions());
			 if (allObjectsaks != null) {
		           ConnectorObjectBuilder builder = null;
		           for (String obj : allObjectsaks) {
		               builder = new ConnectorObjectBuilder();
		               builder.setObjectClass(objClass);
		               builder.setUid(obj);
		               builder.setName(obj);
		               handler.handle(builder.build());
		           }
		       }
	       }
		

		if (allObjects != null) {
			createConnectorObject(attrsToGet, objClass, allObjects, handler);
		}
		log.info("END");
	}

	protected TreeSet<String> getDefaultAttributes(
			Map<String, AttributeInfo> infos) {
		TreeSet<String> results = new TreeSet<String>();
		for (Map.Entry<String, AttributeInfo> entry : infos.entrySet()) {
			if (entry.getValue().isReturnedByDefault())
				results.add(entry.getKey());
		}
		return results;
	}

	/**
	 * This method performs the querying and handling of the account object
	 * class.
	 * 
	 * @param query
	 * @param handler
	 * @param options
	 * @return
	 */
	protected void executeQueryAccount(String query, ResultsHandler handler,
			OperationOptions options) {
		log.info("BEGIN");
		ArrayList<String> alFields = new ArrayList<String>();
		ArrayList<String> alCustomFields = new ArrayList<String>();		
		HashMap<String, ArrayList<SAPUMAttributeMapBean>> hmAttrMap = new HashMap<String, ArrayList<SAPUMAttributeMapBean>>();
		if (options != null) {
			// Account attributes names of Target
			String[] attrsToGetArray = options.getAttributesToGet();
			ArrayList<String> attrsToGet = null;
			if (attrsToGetArray != null) {
				attrsToGet = new ArrayList<String>(Arrays.asList(attrsToGetArray));
			
			// During full reconciliation in OW, attrsToGet has only the
			// attribute '__NAME__' but we need to get all user attribute values
			// so, we've to add all account attributes to attrsToGet.
			if (attrsToGet.size() == 1) {
				Set<String> _accountAttributeNames = _accountAttributes
						.keySet();
				attrsToGet.addAll(_accountAttributeNames);
				sBatchSize = _configuration.getBatchSize();
			} else {
				// For OIM
				// Get the parameter values as entered in task scheduler
				//Map<String, Object> optionMap = new HashMap<String, Object>();
				//optionMap = options.getOptions();
				//sBatchSize = (String) optionMap.get("Batch Size");
				//sTimeZone = (String) optionMap.get("SAP System Time Zone");
				sBatchSize = _configuration.getBatchSize();
				sTimeZone = _configuration.getSapSystemTimeZone();
				
			}
			boolean isCUAEnabled = _configuration.getEnableCUA();
			if (!isCUAEnabled) {
				subSystem = _configuration.getmasterSystem();
			}
			//Split the attributes into standard and custom
			int length = attrsToGet.size();
			for (int i = 0; i < length; i++) {
				String sDecode = (String) attrsToGet.get(i);
				String[] keyArr = sDecode.split(";");
				int keyArrlength = keyArr.length;
				if (keyArrlength > 1) {
					if (keyArrlength > 4) {
						alCustomFields.add(sDecode);
					} else {
						alFields.add(sDecode);
					}
				} else {
					if (!sDecode.startsWith("__"))
						alEmbeddedAttributes.add(sDecode);
				}
			}
						
			hmAttrMap = SAPUtil.initializeTargetReconAttrMap(alFields, isCUAEnabled);
			hmCustomAttrMap = SAPUtil.initializeCustomAttrMap(alCustomFields,
					isCUAEnabled);
		}
			// Restructuring query in case of filters - contains/startswith/endswith/containsall with __Name__ attribute
			if(query!= null && query.contains("=[")){
				if(query.contains("|")){
					if (query.contains(">")){
						//retain only time stamp attr of the query
						query = query.substring(query.lastIndexOf("&")+1).trim();
					} else {
						//set for full reconciliation
						query = null;
					}
				} else if (query.contains("&") && query.contains(">")){
					//remove all Name attr with filters - contains/startswith/endswith/containsall
					do {
						query = trimNameAttr(query);
					} while(query.contains("=["));
				}
			}
			
			// OPAM and OW-- Query will be null
			if (StringUtil.isEmpty(query)) {
				getFirstTimeReconEvents(hmAttrMap, handler, attrsToGet, false, null);
			}
			// OIM -- Query will contain atleast >
			else if (query.contains(">")) {
				String sIncrementalReconQuery=null;
				if(query.contains("&")){
					sCustomQuery=query.substring(0, query.lastIndexOf("&")).trim();
					sIncrementalReconQuery=query.substring(query.lastIndexOf("&")+1).trim();
				} else if(query.contains("|")){
					sCustomQuery=query.substring(0, query.lastIndexOf("|")).trim();
					sIncrementalReconQuery=query.substring(query.lastIndexOf("|")+1).trim();	
				} else{
					//Query contains only >
					sIncrementalReconQuery=query;
				}			
				sIncrementalAttrName = sIncrementalReconQuery.substring(0, sIncrementalReconQuery
						.indexOf(">")).trim();
				alEmbeddedAttributes.remove(sIncrementalAttrName);
				String sExecutionTime = sIncrementalReconQuery.substring(sIncrementalReconQuery.indexOf(">") + 1).trim();
				/*
				 * Call getFirstTimeReconEvents() if last execution timestamp is
				 * 0 in task scheduler. Call getIncrementalReconEvents()
				 * for incremental recon events  
				 */
				if (StringUtil.isEmpty(sExecutionTime)
						|| sExecutionTime.equalsIgnoreCase("0")) {
					getFirstTimeReconEvents(hmAttrMap, handler, attrsToGet, false, null);
				} else {
					getIncrementalReconEvents(hmAttrMap, handler,
							sExecutionTime);
				}

			}
			// This block added for OPAM wildcard search - Bug 18998725
			//Commented for Bug 19610570-filter translator
		/*	else if((query.startsWith("%") || query.startsWith("!%")) && query.endsWith("%")){
				String optionStr = buildStringOpFilter(query, true);
				getFirstTimeReconEvents(hmAttrMap, handler, attrsToGet, true, optionStr);
			}  */
			//Bug 19610570-filter translator
			else if((query.contains(Name.NAME) && query.contains("&") && !query.contains("|")) || query.contains("=[")){
				String[] filters = query.split(" & ");
				String nameValue = null;
				StringBuffer filterBuf = new StringBuffer();
				//boolean isNotContanins = false;
				for (String filter : filters){
					if (filter.startsWith(Name.NAME) && filter.contains("=[")){
						nameValue = filter.substring(filter.indexOf("=[")+2);
						//isNotContanins = filter.contains("!=");
					} else {
						if (filterBuf.length() > 0){
							filterBuf.append(" & ");
						}
						filterBuf.append(filter);
					}
				}
				if (filterBuf.length() > 0){
					sCustomQuery = filterBuf.toString();
				}
				//nameFilter = isNotContanins?"!%"+nameFilter+"%":"%"+nameFilter+"%";
				nameValue = "%"+nameValue+"%";
				String optionStr = buildStringOpFilter(nameValue, true);
				getFirstTimeReconEvents(hmAttrMap, handler, attrsToGet, true, optionStr);
			}
			//Bug 19610570-filter translator
			else if (query.contains("&") || query.contains("|")){
				sCustomQuery = query;
				getFirstTimeReconEvents(hmAttrMap, handler, attrsToGet, false, null);
			}
			// OW -- Query contains only User ID
			else {
				// filter enhancement -  Bug 18998725 - start
				if(query.startsWith(Uid.NAME) || query.startsWith(Name.NAME)){
                    query=query.substring(query.indexOf("=")+1);
                }
				// filter enhancement -  Bug 18998725 - end
				ConnectorObject co=createConnectorObjectUser(
						query, hmAttrMap,null);
				if(co!=null){
					handler.handle(co);
				}
			}
		}
		log.info("END");
	}

	/**
	 * Retreives a list of help values from SAP.
	 * 
	 * @param objName
	 * @param method
	 * @param parameter
	 * @param field
	 * @param throwException
	 *            - flag to determine whether or not to throw exceptions.
	 *            currently, all calls eat it but someone else might want to
	 *            throw it.
	 * @return
	 * @throws ConnectorException
	 */
	protected List<String> getHelpValues(String objName, // Bapi object = USER
			String method, // Method = ProfilesAssign
			String parameter, // Parameter = Profiles
			String field, // Field = BAPIBNAME
			boolean throwException) throws ConnectorException {
		log.info("BEGIN");
		List<String> returnList = null;
		try {
			Function function = new Function("BAPI_HELPVALUES_GET",
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			function.setImportValue(null, "FIELD", field, false);
			function.setImportValue(null, "METHOD", method, false);
			function.setImportValue(null, "OBJNAME", objName, false);
			function.setImportValue(null, "PARAMETER", parameter, false);

			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();

			JCoTable values = function.getTable("VALUES_FOR_FIELD");
			if (values != null) {
				for (int i = 0; i < values.getNumRows(); i++) {
					values.setRow(i);
					String val = values.getString("VALUES");
					if (val != null) {
						if (returnList == null)
							returnList = new ArrayList<String>();
						returnList.add(val);
					}
				}
			}
		} catch (JCoException e) {
			if (throwException) {
				String message = _configuration
						.getMessage("SAP_ERRO_HELP_VALUES_GET");
				log.error(e, message);
				throw new ConnectorException(message, e);
			}
		}
		log.info("RETURN");
		return returnList;
	}

	/**
	 * This method retrieves table entries using RFC_GET_TABLE_ENTRIES.
	 * 
	 * @param options
	 * @return
	 */
	public ArrayList<String> getTableEntries(Map<String, Object> options) {
		log.info("BEGIN");
		ArrayList<String> allObjects = null;
		try {

			String[] name = (String[]) options.get("name");
			Function function = new Function(name[0], _connection,
					_configuration.getConnectorMessages(), _configuration
							.getEnableCUA(), _configuration.getRetryWaitTime());

			if (name != null) {
				function.setImportValue(null, "OBJTYPE", name[1], false);
				function.setImportValue(null, "METHOD", name[2], false);
				function.setImportValue(null, "PARAMETER", name[3], false);
				function.setImportValue(null, "FIELD", name[4], false);

				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				// keeping this as a debugging tool
				String dumpFileName = (String) options.get("dumpFile");
				if (dumpFileName != null) {
					FileWriter fw = new FileWriter(dumpFileName);
					fw.write(function.toXML());
					fw.close();
				}

				JCoTable data = function.getTable("ENTRIES");
				allObjects = new ArrayList<String>();

				int offset = (Integer) options.get("offset");
				int length = (Integer) options.get("length");
				length = length == 0 ? -1 : length;

				String sapClient = _configuration.getClient();

				for (int i = 0; i < data.getNumRows(); i++) {
					data.setRow(i);
					String wholeRow = data.getString("WA");
					String field = getFieldFromRow(wholeRow, offset, length,
							sapClient);
					if (field != null) {
						allObjects.add(field);
					}
				}
				if (allObjects != null)
					Collections.sort(allObjects);
			} // if name != null
		} catch (Throwable e) {
			String message = _configuration
					.getMessage("SAP_ERR_LIST_TABLE_ENTRIES");
			log.error(e, message);
			throw new ConnectorException(message, e);
		}
		log.info("RETURN");
		return allObjects;
	}

	/**
	 * This method retrieves table entries with Subsystem using
	 * RFC_GET_TABLE_ENTRIES.
	 * 
	 * @param options
	 * @return
	 */
	protected ArrayList<String> getSubSystemTableEntries(
			Map<String, Object> options) {
		log.info("BEGIN");
		ArrayList<String> allObjects = null;
		try {
			Function function = new Function("RFC_GET_TABLE_ENTRIES",
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			String name = (String) options.get("name");
			if (name != null) {
				function.setImportValue(null, "TABLE_NAME", name, false);
				function.executeWithRetry(_configuration.getMaxBAPIRetries());

				String dumpFileName = (String) options.get("dumpFile");
				if (dumpFileName != null) {
					FileWriter fw = new FileWriter(dumpFileName);
					fw.write(function.toXML());
					fw.close();
				}

				JCoTable data = function.getTable("ENTRIES");
				allObjects = new ArrayList<String>();

				int ssoffset = (Integer) options.get("ssoffset");
				int sslength = (Integer) options.get("sslength");
				int offset = (Integer) options.get("offset");
				int length = (Integer) options.get("length");
				length = length == 0 ? -1 : length;

				String subSystem = (String) options.get("SUBSYSTEM");

				String sapClient = _configuration.getClient();

				for (int i = 0; i < data.getNumRows(); i++) {
					data.setRow(i);
					String wholeRow = data.getString("WA");
					String actGroup = getFieldFromRow(wholeRow, offset, length,
							sapClient);
					if (actGroup != null) {
						String ss = getFieldFromRow(wholeRow, ssoffset,
								sslength, sapClient);
						if (subSystem == null) {
							actGroup = ss + ":" + actGroup;
							allObjects.add(actGroup);
						} else if (subSystem.equalsIgnoreCase(ss)) {
							allObjects.add(actGroup);
						}
					}
				}
				if (allObjects != null)
					Collections.sort(allObjects);
			}
		} catch (Throwable e) {
			String message = _configuration
					.getMessage("SAP_ERR_LIST_SBSTM_ENTRIES");
			log.error(e, message);
			throw new ConnectorException(message, e);
		}
		log.info("RETURN");
		return allObjects;
	}

	/**
	 * Process a rows from an SAP table retrieved with RFC_GET_TABLE_ENTRIES.
	 * The 1st 3 chars are always the SAP Client and should be used to determine
	 * if the data is appropriate. Only data for the client defined for this
	 * resource should be returned.
	 * 
	 * @param row
	 *            - the row data
	 * @param offset
	 *            - offset where the field starts
	 * @param length
	 *            - length of the field
	 * @param sapClient
	 *            - the SAP client. i.e "000" or "001"
	 * @return the value of the field or null if not for this client
	 */
	protected String getFieldFromRow(String row, int offset, int length,
			String sapClient) {
		log.info("BEGIN");
		String subString = null;
		if (row != null) {
			int rowLen = row.length();
			if (rowLen > offset) {
				// ensure that this row contains data for the SAP client
				// that this adapter is configured for
				String rowSapClient = row.substring(0, 3);
				if ((sapClient == null) || rowSapClient != null
						&& rowSapClient.equals(sapClient)) {
					if (rowLen > offset + length)
						subString = row.substring(offset, offset + length);
					else
						subString = row.substring(offset);

					// the WA field is the only field in the row, it is
					// 512 chars with
					if (subString != null && subString.length() > 0) {
						String trimmed = subString.trim();
						if (trimmed.length() > 0) {
							// Some attributes have spaces b/w 2 words.
							// The extra words seem to be junk data. Probably
							// just
							// in our test system, but remove all but first
							// word, just
							// in case.
							int spaceIdx = trimmed.indexOf(' ');
							if (spaceIdx > 0) {
								trimmed = trimmed.substring(0, spaceIdx);
							}
						}
						subString = trimmed;
					}
				}
			}
		}
		log.info("RETURN");
		return subString;
	}

	private final static String LTIME_PATTERN = "^[0-9]{2}+/[0-9]{2}+/[0-9]{2}+ 00:00:00 [A-Z]{3}+.*$";

	/**
	 * Gets the 'expirePassword' attribute value. The password is expired if
	 * LOGONDATA->LTIME is "00:00:00".
	 * 
	 * @param function
	 * @return expiredPassword boolean
	 */
	private boolean getPasswordExpired(Function function) throws JCoException {
		log.info("BEGIN");
		boolean expiredPassword = false;
		JCoParameterList paramList = function.getExportParameterList();
		JCoStructure struct = paramList.getStructure("LOGONDATA");
		Object ltime = struct.getValue("LTIME");
		String dateStr;
		if (ltime instanceof Date) {
			dateStr = SAPUtil.dateToString((Date) ltime, "MM/dd/yy HH:mm:ss z");
		} else {
			dateStr = ltime.toString();
		}
		expiredPassword = dateStr.matches(LTIME_PATTERN);
		log.info(_configuration.getMessage("SAP_INFO_EXPIRED_PSWD", expiredPassword));
		
		
		log.info("RETURN");
		return expiredPassword;
	}

	/**
	 * Return a null value for "" since SAP cannot differentiate between null
	 * and "".
	 * 
	 * @param builder
	 * @param strct
	 * @param attrName
	 * @param fieldName
	 * @param paramList
	 */
	private void addStructAttribute(ConnectorObjectBuilder builder,
			String strct, String attrName, String fieldName,
			JCoParameterList paramList) {
		log.info("BEGIN");
		JCoStructure struct = paramList.getStructure(strct);
		if (struct.getValue(fieldName) == null) {
			builder.addAttribute(attrName);
		} else if (struct.getValue(fieldName).equals("")) {
			builder.addAttribute(attrName);
		} else {
			// since builder does not handle dates
			// convert to String
			Object value = struct.getValue(fieldName);
			if (value instanceof Date)
				value = SAPUtil.dateToString((Date) value, "MM/dd/yyyy");
			builder.addAttribute(attrName, value);
		}
		log.info("END");
	}

	/**
	 * Add a null multivalued attribute to the ConnectorObject if there are no
	 * items in the list.
	 * 
	 * @param builder
	 * @param attrName
	 * @param val
	 */
	private void addMultiValAttribute(ConnectorObjectBuilder builder,
			String attrName, List<String> val) {
		log.info("BEGIN");
		if (val.size() == 0) {
			builder.addAttribute(attrName);
		} else {
			builder.addAttribute(attrName, val.toArray());
		}
		log.info("RETURN");
	}

	/**
	 * This method pulls all information from a result and adds it to the
	 * ConnectorObjectBuilder.
	 * 
	 * @param builder
	 * @param function
	 * @param structOrTable
	 * @param fieldName
	 * @param attrName
	 * @throws ConnectorException
	 * @throws JCoException
	 */
	protected void extractData(ConnectorObjectBuilder builder,
			Function function, String structOrTable, String fieldName,
			String attrName) throws ConnectorException, JCoException {
		log.info("BEGIN");
		JCoParameterList paramList = function.getExportParameterList();
		if (paramList != null
				&& function.isStructure(paramList.getListMetaData(),
						structOrTable)) {
			addStructAttribute(builder, structOrTable, attrName, fieldName,
					paramList);
		} else {
			paramList = function.getTableParameterList();
			if ((paramList != null)
					&& function.isTable(paramList.getListMetaData(),
							structOrTable)) {

				boolean handleTable = fieldName != null
						&& fieldName.equals("TABLE");
				// see what type is declared in the schema map to determine the
				// type to return
				// currently, this should only be used for GROUPS
				if (!handleTable
						&& (_accountAttributes.get(attrName) == null || (attrName != null && _accountAttributes
								.get(attrName).getType().equals(String.class)))) {
					JCoTable table = paramList.getTable(structOrTable);
					ArrayList<String> multiValAttr = new ArrayList<String>();
					for (int i = 0; i < table.getNumRows(); i++) {
						table.setRow(i);
						multiValAttr.add(table.getString(fieldName));
					}
					addMultiValAttribute(builder, attrName, multiValAttr);
				} else {
					List<String> tableVals = SAPUtil.getTableRows(
							structOrTable, function, _configuration
									.getConnectorMessages(), _tableFormats,
							false);
					if (tableVals != null)
						addMultiValAttribute(builder, attrName, tableVals);
				}
			} else {
				String message = _configuration.getMessage(
						"SAP_ERR_ATTR_NOT_FOUND", fieldName);
				log.error(message);
				throw new ConnectorException(message);
			}
		}
		log.info("RETURN");
	}	

	private HashMap getLookupValuesBAPI(String[] sSplitFieldMappings)
			throws ConnectorException {
		log.info("BEGIN");
		HashMap<String, String> returnValuesMap = new HashMap<String, String>();
		try {
			Function function = new Function(sSplitFieldMappings[0],
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			if (sSplitFieldMappings != null) {
				function.setImportValue(null, "OBJTYPE", "USER", false);
				function.setImportValue(null, "METHOD", sSplitFieldMappings[1],
						false);
				function.setImportValue(null, "PARAMETER",
						sSplitFieldMappings[2], false);
				function.setImportValue(null, "FIELD", sSplitFieldMappings[3],
						false);
			}

			if ((sSplitFieldMappings[3].equalsIgnoreCase(("TITLE_P"))
					|| (sSplitFieldMappings[3].equalsIgnoreCase("COMPANY")) || (sSplitFieldMappings[3]
					.equalsIgnoreCase("AGR_NAME")))) {
				JCoStructure jcoStructure = function.getImportParameterList()
						.getStructure("EXPLICIT_SHLP");
				jcoStructure.setValue(("SHLPNAME"), sSplitFieldMappings[6]);
				jcoStructure.setValue(("SHLPTYPE"), sSplitFieldMappings[7]);
				if (sSplitFieldMappings[3].equalsIgnoreCase(("TITLE_P"))) {
					JCoTable jcotable = function.getTableParameterList()
							.getTable(("SELECTION_FOR_HELPVALUES"));
					jcotable.appendRow();
					jcotable.setValue(("SELECT_FLD"), sSplitFieldMappings[8]);
					jcotable.setValue(("SIGN"), sSplitFieldMappings[9]);
					jcotable.setValue("OPTION", sSplitFieldMappings[10]);
					jcotable.setValue(("LOW"), sSplitFieldMappings[11]);
				}
			} else if (sSplitFieldMappings[3].equalsIgnoreCase(("LIC_TYPE"))) {
				JCoTable jcotable = function.getTableParameterList().getTable(
						("SELECTION_FOR_HELPVALUES"));
				jcotable.appendRow();
				jcotable.setValue(("SELECT_FLD"), sSplitFieldMappings[6]);
				jcotable.setValue(("SIGN"), sSplitFieldMappings[7]);
				jcotable.setValue(("OPTION"), sSplitFieldMappings[8]);
				jcotable.setValue(("LOW"), sSplitFieldMappings[9]);
			}

			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			returnValuesMap = getDropValuesBAPI(sSplitFieldMappings, function);
		}
		catch (Exception e) {
			
		}
		log.info("RETURN");
		return returnValuesMap;
	}

	/**
	 * Description : This method is used to get the lookup values executed using
	 * RFC_READ_TABLE
	 * 
	 * @param sSplitFieldMappings
	 *            Connection Object
	 * @param dropDownListFunction
	 *            String Array which contains the parameters RFC_READ_TABLE
	 *            executes
	 * @return HashMap HashMap containing lookup values to be added to the
	 *         lookup table
	 * @throws ConnectorException
	 * 
	 */
	private HashMap<String, String> getDropValuesBAPI(
			String[] sSplitFieldMappings, Function dropDownListFunction)
			throws ConnectorException {
		log.info("BEGIN");
		HashMap<String, String> returnValuesMap = new HashMap<String, String>();
		try {
			/*
			 * Loop through jcoTables and get the corresponding code and decode
			 * values to be put in HashMap
			 */
			JCoTable jcoTable = dropDownListFunction.getTableParameterList()
					.getTable(("DESCRIPTION_FOR_HELPVALUES"));
			JCoTable jcoTable1 = dropDownListFunction.getTableParameterList()
					.getTable("HELPVALUES");
			int iCodeOffSet = 0;
			int iCodeLength = 0;
			int iDescOffSet = 0;
			int iDescLength = 0;
			String sField = "";
			int iDropDownTableRows = jcoTable.getNumRows();

			if (iDropDownTableRows != 0) {
				for (int iIndex = 0; iIndex < iDropDownTableRows; iIndex++) {
					jcoTable.setRow(iIndex);
					sField = jcoTable.getString("FIELDNAME");

					if (sField.equals(sSplitFieldMappings[4].toString())) {
						iCodeOffSet = Integer.parseInt(jcoTable
								.getString("OFFSET"));
						iCodeLength = Integer.parseInt(jcoTable
								.getString("LENG"));
						iCodeLength = iCodeLength + iCodeOffSet;
					}

					if (sField.equals(sSplitFieldMappings[5].toString())) {
						iDescOffSet = Integer.parseInt(jcoTable
								.getString(("OFFSET")));
						iDescLength = Integer.parseInt(jcoTable
								.getString(("LENG")));
						iDescLength = iDescLength + iDescOffSet;
					}
				}
			}

			int idropDownHelpValuesTable = jcoTable1.getNumRows();

			if (idropDownHelpValuesTable != 0) {
				String sCode = null;
				String sDecode = null;
				String sMessage = null;
				int index1 = -1;
				int index2 = -1;
				for (int i = 0; i < idropDownHelpValuesTable; i++) {
					sCode = null;
					sDecode = null;
					jcoTable1.setRow(i);
					sMessage = jcoTable1.getString("HELPVALUES");

					if (sSplitFieldMappings[3].toString().equalsIgnoreCase(
							"DCPFM")) {
						index1 = sMessage.indexOf('N');
						index2 = sMessage.indexOf('1');
					}
					if ((sMessage != null) && (sMessage.length() == 0)) {
						sCode = null;
						sDecode = null;
					} else if (sMessage.length() < iCodeLength) {
						sCode = sMessage.substring(iCodeOffSet);
						sDecode = null;
					} else if ((sMessage.length() > iCodeLength)
							&& (sMessage.length() < iDescLength)) {
						sCode = sMessage.substring(iCodeOffSet, iCodeLength);

						if (index1 != -1) {
							sDecode = sMessage.substring(index1);
						} else if (index2 != -1) {
							sDecode = sMessage.substring(index2);
						} else if ((iDescOffSet > 0)
								&& (sMessage.length() > iDescOffSet)) {
							sDecode = sMessage.substring(iDescOffSet);
						}
					} else if (sMessage.length() > iDescLength) {
						sCode = sMessage.substring(iCodeOffSet, iCodeLength);

						if (index1 != -1) {
							sDecode = sMessage.substring(index1);
						} else if (index2 != -1) {
							sDecode = sMessage.substring(index2);
						} else {
							sDecode = sMessage.substring(iDescOffSet,
									iDescLength);
						}
					} else if (sMessage.length() == iCodeLength) {
						sCode = sMessage.substring(iCodeOffSet, iCodeLength);
						sDecode = null;
					} else if (sMessage.length() == iDescLength) {
						sCode = sMessage.substring(iCodeOffSet, iCodeLength);

						if (index1 != -1) {
							sDecode = sMessage.substring(index1);
						} else if (index2 != -1) {
							sDecode = sMessage.substring(index2);
						} else {
							sDecode = sMessage.substring(iDescOffSet);
						}
					}
					
						if (!StringUtil.isEmpty(sCode)) {
							if (StringUtil.isEmpty(sDecode)) {
								sDecode = sCode;
							}
							if(_configuration.getmasterSystem()!=null) {
								if (!_configuration.getEnableCUA() && ((sSplitFieldMappings[3]
										.equalsIgnoreCase("AGR_NAME") || (sSplitFieldMappings[3]
										.equalsIgnoreCase(("BAPIPROF")))))) {
									sCode = _configuration.getmasterSystem()+ "~" + sCode ;
								} else {
									sCode = sCode;
								}
							sDecode = sDecode;
							returnValuesMap.put(sCode.trim(), sDecode.trim());
						}
						returnValuesMap.put(sCode.trim(), sDecode.trim());
				  }	
					
				}
			}
		} catch (Exception e) {
			throw new ConnectorException(e.getMessage());
		}
		log.info("RETURN");
		return returnValuesMap;
	}

	private HashMap<String, String> getRolesProfiles(String[] roleProfile) {
		log.info("BEGIN");
		HashMap hmValuesforDescription = getLookupValuesRFC(roleProfile);
		HashMap hmValuesForCode = getLookupValuesRolesorProfiles(roleProfile);
		HashMap hmLookupValues = new HashMap<String, String>();
		String sKey = "";
		String sValue = "";
		if (hmValuesForCode.size() > 0) {
			Iterator it = hmValuesForCode.keySet().iterator();
			while (it.hasNext()) {
				sKey = it.next().toString();
				if (hmValuesforDescription.containsKey(sKey)) {
					sValue = (String) hmValuesforDescription.get(sKey);
				} else {
					sValue = (String) hmValuesForCode.get(sKey);
				}
				hmLookupValues.put(sKey, sValue);
			}
		}
		log.info("RETURN");
		return hmLookupValues;
	}

	/**
	 * Description : This method is used to get the lookup values executed using
	 * RFC_READ_TABLE
	 * 
	 * @param jcoConnection
	 *            Connection Object
	 * @param SplitFieldMappings
	 *            String Array which contains the parameters RFC_READ_TABLE
	 *            executes
	 * @return HashMap HashMap containing lookup values to be added to the
	 *         lookup table
	 * @throws ConnectorException
	 * 
	 */
	private HashMap<String, String> getLookupValuesRFC(
			String[] sSplitFieldMappings) throws ConnectorException {
		log.info("BEGIN");
		HashMap<String, String> returnValuesMap = new HashMap<String, String>();
		try {
			/*
			 * Get the jcoFunction by using the BAPI Name passed and set the
			 * fields to be executed for that BAPI
			 */
			Function function = new Function(sSplitFieldMappings[0],
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());

			function.setImportValue(null, "QUERY_TABLE",
					sSplitFieldMappings[1], false);

			if (sSplitFieldMappings.length > 4) {
				JCoTable returnOption = function.getTableParameterList()
						.getTable("OPTIONS");
				returnOption.appendRow();
			//	returnOption.setValue("TEXT", sSplitFieldMappings[6]);
			// START ::BUG 18461406 - HOW TO RECONCILE THE FRENCH LABELS INSTEAD OF ENGLISH LABELS IN OIM 
				returnOption.setValue("TEXT", "LANGU = '"+_configuration.getLanguage()+"'");
		    // END ::BUG 18461406 - HOW TO RECONCILE THE FRENCH LABELS INSTEAD OF ENGLISH LABELS IN OIM 
			}
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			returnValuesMap = getDropValuesRFC(sSplitFieldMappings, function);
		} catch (Exception ex) {
			throw new ConnectorException(ex.getMessage());
		}
		log.info("RETURN");
		return returnValuesMap;
	}

	/**
	 * Description : This method is used to get the lookup values executed using
	 * RFC_READ_TABLE
	 * 
	 * @param sSplitFieldMappings
	 *            Connection Object
	 * @param dropDownListFunction
	 *            String Array which contains the parameters RFC_READ_TABLE
	 *            executes
	 * @return HashMap HashMap containing lookup values to be added to the
	 *         lookup table
	 * @throws ConnectorException
	 * 
	 */
	private HashMap<String, String> getDropValuesRFC(
			String[] sSplitFieldMappings, Function dropDownListFunction)
			throws ConnectorException {
		log.info("BEGIN");
		int icodeOffSet = 0;
		int icodeLength = 0;
		int idescOffSet = 0;
		int idescLength = 0;
		int iSubSystemOffset = 0;
		int iSubSystemLength = 0;
		String sField = "";
		HashMap<String, String> returnValuesMap = new HashMap<String, String>();

		try {
			/*
			 * Loop through jcoTables and get the corresponding code and decode
			 * values to be put in HashMap
			 */
			JCoTable jcoTable = dropDownListFunction.getTableParameterList()
					.getTable("FIELDS");

			JCoTable jcoTable1 = dropDownListFunction.getTableParameterList()
					.getTable("DATA");

			int iReturnValueTable = jcoTable.getNumRows();
			int iReturnValuesDescTable = jcoTable1.getNumRows();
			if (iReturnValueTable != 0) {
				for (int i = 0; i < iReturnValueTable; i++) {
					jcoTable.setRow(i);
					sField = jcoTable.getString("FIELDNAME");

					if (sField.equals(sSplitFieldMappings[2].toString())) {
						icodeOffSet = Integer.parseInt(jcoTable
								.getString("OFFSET"));
						icodeLength = Integer.parseInt(jcoTable
								.getString("LENGTH"));
						icodeLength = icodeLength + icodeOffSet;
					}

					if (sField.equals(sSplitFieldMappings[3].toString())) {
						idescOffSet = Integer.parseInt(jcoTable
								.getString("OFFSET"));
						idescLength = Integer.parseInt(jcoTable
								.getString("LENGTH"));
						idescLength = idescLength + idescOffSet;
					}

					if (!sSplitFieldMappings[1].equalsIgnoreCase("USZBVLNDRC")) {
						if (sField.equals(sSplitFieldMappings[4].toString())) {
							iSubSystemOffset = Integer.parseInt(jcoTable
									.getString("OFFSET"));
							iSubSystemLength = Integer.parseInt(jcoTable
									.getString("LENGTH"));
							iSubSystemLength = iSubSystemLength
									+ iSubSystemOffset;
						}
					}
				}
			}

			if (iReturnValuesDescTable != 0) {
				String sCode = null;
				String sDecode = null;
				String sMessage = null;
				for (int i = 0; i < iReturnValuesDescTable; i++) {
					sCode = null;
					sDecode = null;
					jcoTable1.setRow(i);
					sMessage = jcoTable1.getString("WA");
					if ((sMessage != null) && (sMessage.length() == 0)) {
						sCode = null;
						sDecode = null;
					} else if (sMessage.length() < icodeLength) {
						sCode = sMessage.substring(icodeOffSet);
						sDecode = null;
					} else if ((sMessage.length() > icodeLength)
							&& (sMessage.length() < idescLength)) {
						sCode = sMessage.substring(icodeOffSet, icodeLength);

						if ((idescOffSet > 0)
								&& (sMessage.length() > idescOffSet)) {
							sDecode = sMessage.substring(idescOffSet);
						}
					} else if (sMessage.length() > idescLength) {
						sCode = sMessage.substring(icodeOffSet, icodeLength);
						sDecode = sMessage.substring(idescOffSet, idescLength);
					} else if (sMessage.length() == icodeLength) {
						sCode = sMessage.substring(icodeOffSet, icodeLength);
						sDecode = null;
					} else if (sMessage.length() == idescLength) {
						sCode = sMessage.substring(icodeOffSet, icodeLength);
						sDecode = sMessage.substring(idescOffSet);
					}
					if (StringUtil.isEmpty(sDecode)) {
						sDecode = sCode;
					}
					String sSubSystem = null;
					if(_configuration.getmasterSystem()!= null) {
						if (!StringUtil.isEmpty(sCode)) {
							if (!sSplitFieldMappings[1]
									.equalsIgnoreCase("USZBVLNDRC")) {
								 sSubSystem = sMessage.substring(
										iSubSystemOffset, iSubSystemLength).trim();
								sCode = sSubSystem + "~" + sCode;
							}
							sCode =  sCode;
							if (!sSplitFieldMappings[1]
														.equalsIgnoreCase("USZBVLNDRC")) {
								sDecode = sSubSystem + "~" + sDecode;
							} else {
								sDecode = sDecode;
							}	
							//sDecode = sITResourceName + "~" + sSubSystem + "~" + sDecode;
							returnValuesMap.put(sCode.trim(), sDecode.trim());
						}
					}
					returnValuesMap.put(sCode.trim(), sDecode.trim());
				}	
			}
		} catch (Exception e) {
			throw new ConnectorException(e.getMessage());
		}
		log.info("RETURN");
		return returnValuesMap;
	}

	/**
	 * Description : This method is used to get the lookup values executed using
	 * RFC_READ_TABLE
	 * 
	 * @param jcoConnection
	 *            Connection Object
	 * @param SplitFieldMappings
	 *            String Array which contains the parameters RFC_READ_TABLE
	 *            executes
	 * @return HashMap HashMap containing lookup values to be added to the
	 *         lookup table
	 * @throws ConnectorException
	 * 
	 */
	private HashMap<String, String> getLookupValuesRolesorProfiles(
			String[] sSplitFieldMappings) throws ConnectorException {
		log.info("BEGIN");
		HashMap<String, String> returnValuesMap = new HashMap<String, String>();
		try {
			/*
			 * Get the jcoFunction by using the BAPI Name passed and set the
			 * fields to be executed for that BAPI
			 */
			Function function = new Function(sSplitFieldMappings[0],
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());

			function.setImportValue(null, "QUERY_TABLE",
					sSplitFieldMappings[5], false);

			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			returnValuesMap = getDropValuesRFC(sSplitFieldMappings, function);
		} catch (Exception ex) {
			throw new ConnectorException(ex.getMessage());
		}
		log.info("RETURN");
		return returnValuesMap;
	}

	public void createConnectorObject(ArrayList<String> attrsToGet,
			ObjectClass objClass, HashMap allObjects, ResultsHandler handler) {
		log.info("BEGIN");
		ConnectorObjectBuilder objectBuilder = new ConnectorObjectBuilder();
		String sIdentifier = null;
		Object[] sattrToGet = null;

		Iterator itr = attrsToGet.iterator();
		String codekey = itr.next().toString();
		String decodekey = itr.next().toString();
		Set keys = allObjects.keySet();

		Iterator<String> itrKey = keys.iterator();

		while (itrKey.hasNext()) {
			String key = itrKey.next();

			objectBuilder.addAttribute(AttributeBuilder.build(codekey, key));
			if (allObjects.get(key) == null) {
				objectBuilder.addAttribute(AttributeBuilder.build(decodekey,
						key));
			} /*else if(allObjects.get(key).toString().equals("")){
				objectBuilder.addAttribute(AttributeBuilder.build(decodekey,
						key));
			}*/ else {
				objectBuilder.addAttribute(AttributeBuilder.build(decodekey,
						allObjects.get(key).toString()));
			}
			if (DECIMALNOTATION.equals(objClass)) {
				objectBuilder.setUid(allObjects.get(key).toString());
				objectBuilder.setName(allObjects.get(key).toString());
			} 
			
			/*else if (ACTIVITY_GROUPS.equals(objClass)||PROFILES.equals(objClass))
					 {
				if (allObjects.get(key) == null) {
					objectBuilder.addAttribute(AttributeBuilder.build(decodekey,
							key));
				} else {
					objectBuilder.addAttribute(AttributeBuilder.build(decodekey,
							allObjects.get(key).toString()));
				}*/
			else
			{
				objectBuilder.setUid(key.toString());
				objectBuilder.setName(key.toString());
			}
		
			objectBuilder.setObjectClass(objClass);
			handler.handle(objectBuilder.build());
		}
		log.info("END");
	}
	
	/**
	 * Description : This method is used to reconcile the first time recon
	 * events
	 * 
	 * @param hmAttrMap
	 *            HashMap containing list of parent attributes that needs to be
	 *            reconciled
	 * @param handler
	 * 
	 */
	private void getFirstTimeReconEvents(HashMap<String,ArrayList<SAPUMAttributeMapBean>> hmAttrMap,
			ResultsHandler handler, ArrayList<String> attrsToGet, 
			boolean isWildcard, String customOption) {//parameters added for Bug 18998725
		log.info("BEGIN");
		try {
			/*
			 * Get all the accounts based on the batch size value in HashMap by
			 * querying the USR02 table. Loop through the HashMap and get each
			 * Account ID and call getDetails to get each account and
			 * entitlement information
			 */
			Date str_date=new Date();
			boolean isMoreRecordsFound = true;
			int iStartRecord = 0;			
			if(sBatchSize==null)
				{
				sBatchSize="100";
				}			
			int iBatchSize = Integer.parseInt(sBatchSize);
			boolean isContinueRecon = true;
			while (isMoreRecordsFound) {
				//Added to fix Bug 18998725 - start
				Map<String, String>	accountsMap = getAccounts(
						"USR02", true, iStartRecord, iBatchSize, null,true, 
						isWildcard, customOption);
				Set<String> alAccountID = accountsMap.keySet();
				//Added to fix Bug 18998725 - end
				if (alAccountID.size() > 0) {
					log.info(_configuration.getMessage("SAP_INFO_ACCID_BE_RECON",alAccountID));
					
					for(String sUserID:alAccountID){											
						try {
							if(!isContinueRecon){
								log.warn("Resulthandler returned :: {0}.Exiting getFirstTimeReconEvents", 
										isContinueRecon);
								throw new InterruptedException(
										_configuration.getMessage("SAP_ERR_JOB_INTERRUPTED"));
								}
								
							log.info(_configuration.getMessage("SAP_INFO_USRID_BE_RECON",sUserID));
							ConnectorObject co=null;
							//Assuming following condition for OPAM wildcard search
							//Bug 18998725 - start
							if(isWildcard && attrsToGet==null){
								co=createConnectorObject4Wildcard(sUserID);
							} else {
								co=createConnectorObjectUser(sUserID, hmAttrMap, str_date);
							}
							//Bug 18998725 - end
							
							if(co!=null){
								isContinueRecon = handler.handle(co);
							}
						} catch (Exception ex) {
							log.error(_configuration.getMessage("SAP_ERR_GET_USR_DETAIL",ex.getMessage()));
							
							throw ConnectorException.wrap(ex);
						}					
					}					
					iStartRecord += iBatchSize;
					if (iBatchSize == 0) {
						isMoreRecordsFound = false;
					}
				}else {
					isMoreRecordsFound = false;
				}
			}
		}  catch (Exception e) {
			log.error(_configuration.getMessage("SAP_ERR_FULL_RECON",e.getMessage()));
			throw ConnectorException.wrap(e);
		}
		log.info("END");
	}

	/**
	 * Description : This method is used to reconcile the modified or newly
	 * created UM/CUA target accounts after the running the full recon for creating
	 * recon events
	 * 
	 * @param hmAttrMap
	 *            HashMap containing list of parent and child attributes that needs to be
	 *            reconciled
	 * @param handler	          
	 * @param sExecutionTime           
	 * 
	 */
	private void getIncrementalReconEvents(HashMap<String,ArrayList<SAPUMAttributeMapBean>> hmAttrMap,
			ResultsHandler handler,String sExecutionTime) {
		log.info("BEGIN");
		try {			
			 /* Get all the accounts based on batch size value specified in
			 * HashMap by querying the USZBVSYS/USR04 table. Loop through the
			 * HashMap and get each Account ID and call getDetails to get each
			 * account and entitlement information
			 */
			Date date=null;
			boolean isMoreRecordsFound = true;
			int iStartRecord = 0;			
			int iBatchSize = Integer.parseInt(sBatchSize);
			Set<String> alAccounts = null;
			boolean isContinueRecon = true;
			while (isMoreRecordsFound) {
				/*if (!_configuration.getEnableCUA()) {
					alAccounts = getAccounts("USR04",false,
							iStartRecord, iBatchSize, sExecutionTime,
							true);
				} else {
					alAccounts = getAccounts("USZBVSYS", false,
							iStartRecord, iBatchSize, sExecutionTime,
							 false);
				}*/
				
					//Start::  Bug 18303027 - SAP UM NW7.31 CERTIFICATION
					// updated for  Bug 18998725 - start
							Map<String, String>	accountsMap = getAccounts("USRSTAMP", false,
							iStartRecord, iBatchSize, sExecutionTime,
							 false, false, null);
							 alAccounts = accountsMap.keySet();
					// updated for  Bug 18998725 - end
					//END :: Bug 18303027 - SAP UM NW7.31 CERTIFICATION
				if (alAccounts.size() > 0) {
					log.info(_configuration.getMessage("SAP_INFO_ACCID_BE_RECON",alAccounts));
					for(String sUserID:alAccounts){							
					try {
						if(!isContinueRecon){
							log.warn("Resulthandler returned :: {0}.Exiting getIncrementalReconEvents", 
									isContinueRecon);
							throw new InterruptedException(
									_configuration.getMessage("SAP_ERR_JOB_INTERRUPTED"));
						}
						log.info(_configuration.getMessage("SAP_INFO_USRID_BE_RECON",sUserID));
						date = SAPUtil.stringToDate(accountsMap.get(sUserID), "yyyyMMddHHmmss"); 
						ConnectorObject co=createConnectorObjectUser(
									sUserID, hmAttrMap,date);
							if(co!=null){
								isContinueRecon = handler.handle(co);
							}							
						} catch (Exception ex) {
							log.error(_configuration.getMessage("SAP_ERR_GET_USR_DETAIL",ex.getMessage()));
							throw ConnectorException.wrap(ex);
						}					
					}				
					iStartRecord += iBatchSize;
					if (iBatchSize == 0) {
						isMoreRecordsFound = false;
					}
				} else {
					isMoreRecordsFound = false;
				}
			}					
		} catch (Exception e) {		
			log.error(_configuration.getMessage("SAP_ERR_INC_RECON",e.getMessage()));
			throw new ConnectorException();
		}
		log.info("END");
	}

	/**
	 * Description : This method is used to create connector object for the given user ID
	 * 
	 * @param userID
	 *            User ID for which we need the account details
	 * 
	 * @param hmAttrMap
	 *            HashMap containing list of parent and child attributes that needs to be
	 *            reconciled
	 *            	 
	 * @return str_date 
	 * 
	 */
	public ConnectorObject createConnectorObjectUser(String userID,
			HashMap<String, ArrayList<SAPUMAttributeMapBean>> hmAttrMap, Date str_date) {
		log.info("BEGIN");
		ConnectorObject co = null;
		DateUtil dateutil = new DateUtil();
		boolean isUserExists = false;
		boolean isValid = true;
		try {
		//added for contract test	
			if(hmAttrMap.size()<1){
                ArrayList<String> defAttrs = new ArrayList<String>();
                ArrayList<String> defFields = new ArrayList<String>();
                ArrayList<String> defCustomFields = new ArrayList<String>();
                defAttrs.addAll(getDefaultAttributes(_accountAttributes));
                /*for(String key:_accountAttributes.keySet()){
                    defAttrs.add(_accountAttributes.get(key).getName());
                }*/
                int length = defAttrs.size();
                for (int i = 0; i < length; i++) {
                    String sDecode = (String) defAttrs.get(i);
                    String[] keyArr = sDecode.split(";");
                    int keyArrlength = keyArr.length;
                    if (keyArrlength > 1) {
                        if (keyArrlength > 4) {
                            defCustomFields.add(sDecode);
                        } else {
                            defFields.add(sDecode);
                        }
                    } else {
                        if (!sDecode.startsWith("__"))
                            alEmbeddedAttributes.add(sDecode);
                    }
                }
                hmAttrMap=SAPUtil.initializeTargetReconAttrMap(defFields, false);
                hmCustomAttrMap = SAPUtil.initializeCustomAttrMap(defCustomFields,
                        false);
            }
			//userID = userID.toUpperCase();
			HashMap<String, String> hmQueryMap = new HashMap<String, String>();
			Date dtValidThro = null;
			Date dtToday = new Date();
			//Start:: Bug 17575026 - Commented below and added after calling user existence check			
			/* Function function = new Function("BAPI_USER_GET_DETAIL", true,
					_connection, _configuration.getConnectorMessages(), false,
					_configuration.getRetryWaitTime());
			function.setImportField("USERNAME", userID);
			function.execute();
			JCoTable table = function.getTableParameterList()
					.getTable("RETURN");
			String sMessage = table.getString("MESSAGE");
			if (!sMessage.contains("does not exist")) {
				isUserExists = true;
			}*/
			isUserExists = sSAPCreateUpdateBase.checkIfUserExists(userID);
			log.info(_configuration.getMessage("SAP_INFO_USR_EXISTS",userID,isUserExists));

			if (isUserExists) {
				Function function = new Function("BAPI_USER_GET_DETAIL", true,
						_connection, _configuration.getConnectorMessages(), false,
						_configuration.getRetryWaitTime());
				function.setImportField("USERNAME", userID);
				function.execute();
				//End:: Bug 17575026
				ConnectorObjectBuilder objectBuilder = new ConnectorObjectBuilder();
				Set<String> keySet = hmAttrMap.keySet();
				if (keySet != null) {
					Iterator<String> keySetIterator = keySet.iterator();
					JCoStructure sapStructure = null;
					while (keySetIterator.hasNext()) {
						String sStructure = (String) keySetIterator.next();
						if(!(sStructure.equals("NO_USER_PW")||sStructure.equals("WRNG_LOGON"))){
							if (!sStructure.equalsIgnoreCase("UCLASSSYS")
									&& !sStructure.equalsIgnoreCase("GROUPS")
									&& !sStructure.equalsIgnoreCase("SYSTEMS")) {
								sapStructure = function.getExportParameterList()
										.getStructure(sStructure);
							}
						ArrayList<SAPUMAttributeMapBean> attrMapList = (ArrayList<SAPUMAttributeMapBean>) hmAttrMap
								.get(sStructure);
						for (int index = 0; index < attrMapList.size(); index++) {
							Object fieldValue = null;
							SAPUMAttributeMapBean oAttrMapBean = (SAPUMAttributeMapBean) attrMapList
									.get(index);
							if (sStructure.equalsIgnoreCase("GROUPS")
									||sStructure.equalsIgnoreCase("SYSTEMS")) {
								AttributeBuilder abuilder = new AttributeBuilder();
								abuilder
										.setName(oAttrMapBean.getOIMfieldName());
								JCoTable multiValuesTable = function
										.getTableParameterList().getTable(
												sStructure);
								int iNoOfRows = multiValuesTable.getNumRows();
								if (iNoOfRows > 0) {
									for (int iRow = 0; iRow < iNoOfRows; iRow++) {
										multiValuesTable.setRow(iRow);
										fieldValue = multiValuesTable
												.getValue(oAttrMapBean
														.getBapiFieldName());
										fieldValue=((String)fieldValue).trim();
										abuilder.addValue(fieldValue);
									}
								} else {
									ArrayList<String> alMultiValues = new ArrayList<String>();
									abuilder.addValue(alMultiValues);
								}
								Attribute multiAttrs = abuilder.build();
								objectBuilder.addAttribute(multiAttrs);
							} else {
								if (sStructure.equalsIgnoreCase("UCLASSSYS")) {
									JCoTable multiValuesTable = function
											.getTableParameterList().getTable(
													sStructure);
									if (multiValuesTable.getNumRows() > 0) {
										multiValuesTable.setRow(0);
										fieldValue = multiValuesTable
												.getValue(oAttrMapBean
														.getBapiFieldName());
									}
								} else {
									fieldValue = (sapStructure
											.getValue(oAttrMapBean
													.getBapiFieldName()));
								}
								if (fieldValue != null) {
									if (fieldValue instanceof Date) {
										if (oAttrMapBean.getBapiFieldName()
												.equals("GLTGB")) {
											dtValidThro = (Date) fieldValue;
										}
										fieldValue = ((Date) fieldValue)
												.getTime();
									} else {
										fieldValue = ((String) fieldValue).trim();
									}
								}
								
								if (sCustomQuery != null
										&& (fieldValue instanceof String)) {									
									hmQueryMap.put((String) oAttrMapBean
											.getOIMfieldName(),(String) fieldValue);
								}
								objectBuilder.addAttribute(oAttrMapBean
										.getOIMfieldName(), fieldValue);
							}
						  }	
						}
					}
					// Lock/Unlock
					LockStatus lockStatus=new LockStatus(function);
					int lock=lockStatus.getLockStatus();
					if (lock==0) {
						objectBuilder.addAttribute(
								OperationalAttributes.LOCK_OUT_NAME, "0");
					} else {
						objectBuilder.addAttribute(
								OperationalAttributes.LOCK_OUT_NAME, "1");
					}				

					/*
					 * If valid Through is < current date, then user should be
					 * disabled
					 */
					if ((dtValidThro != null) && dtValidThro.before(dtToday)) {
						objectBuilder.addAttribute(AttributeBuilder
								.buildEnabled(false));
					} else {
						objectBuilder.addAttribute(AttributeBuilder
								.buildEnabled(true));
					}
				}

				// Check if the query is valid
				if (hmQueryMap.size() > 0) {
					isValid = SAPUtil.executeCustomQuery(hmQueryMap,
							sCustomQuery);
					log.info(_configuration.getMessage("SAP_INFO_CUSTOM_QUERY_VALID", isValid));
					
				}

				if (isValid) {
					// Custom Attributes
					if (hmCustomAttrMap != null && hmCustomAttrMap.size() > 0) {
						Set<String> keyCustomAttrSet = hmCustomAttrMap.keySet();
						if (keyCustomAttrSet != null) {
							Iterator<String> keyCustomAttrSetIterator = keyCustomAttrSet
									.iterator();
							while (keyCustomAttrSetIterator.hasNext()) {
								String sBAPIName = (String) keyCustomAttrSetIterator
										.next();
								ArrayList<SAPUMAttributeMapBean> attrMapList = (ArrayList<SAPUMAttributeMapBean>) hmCustomAttrMap
										.get(sBAPIName);
								if (sBAPIName
										.equalsIgnoreCase("RFC_READ_TABLE")) {
									Attribute customAttr = getCustomAttributesRFC(
											userID, attrMapList);
									objectBuilder.addAttribute(customAttr);
								} else {
									Attribute customAttr = getCustomAttributesBAPI(
											sBAPIName, userID, attrMapList);
									objectBuilder.addAttribute(customAttr);
								}
							}
						}
					}
					
					// Multi-Value Embedded Attributes
					for (String values : alEmbeddedAttributes) {
						String sEmbeddedAttrName = values;
						String sEmbeddedConfig = null;
						JCoTable multiValuesTable;
						if (sEmbeddedAttrName.equalsIgnoreCase("roles")) {
							sEmbeddedConfig = _configuration.getRoles();
						} else if (sEmbeddedAttrName
								.equalsIgnoreCase("profiles")) {
							sEmbeddedConfig = _configuration.getProfiles();
						} else if (sEmbeddedAttrName
								.equalsIgnoreCase("parameters")) {
							sEmbeddedConfig = _configuration.getParameters();
						} else if (sEmbeddedAttrName
								.equalsIgnoreCase("groups")) {
							sEmbeddedConfig = _configuration.getGroups();
						}
						if (!StringUtil.isEmpty(sEmbeddedConfig)) {
							String sTableName = sEmbeddedConfig.substring(0,
									sEmbeddedConfig.indexOf("~"));
							String sTargetAttr = sEmbeddedConfig
									.substring(sEmbeddedConfig.indexOf("~") + 1);
							if (sEmbeddedAttrName.equalsIgnoreCase("parameters") ||
									sEmbeddedAttrName.equalsIgnoreCase("groups")	) {
								multiValuesTable = function
										.getTableParameterList().getTable(
												sTableName);
							} else {
								multiValuesTable = getRoleorProfile(userID.toUpperCase(),
										sTableName);
							}

							Attribute role = buildEmbeddedAttr(
									multiValuesTable, sTargetAttr,
									sEmbeddedAttrName, sTableName);
							objectBuilder.addAttribute(role);
						}
					}
					objectBuilder.setUid(userID);
					objectBuilder.setName(userID);
					objectBuilder.setObjectClass(ObjectClass.ACCOUNT);
					if (sIncrementalAttrName != null)
						objectBuilder.addAttribute(sIncrementalAttrName, Long
								.valueOf(dateutil.parseTime(str_date,
										"yyyyMMddHHmmss", sTimeZone)));
					co = objectBuilder.build();
				}
			}
		} catch (JCoException e) {
			log.error(_configuration.getMessage("SAP_ERR_CONN_OBJ",e.getMessage()));
			
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			log.error(_configuration.getMessage("SAP_ERR_CONN_OBJ",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		}
		log.info("END");
		return co;
	}

	/**
	 * Description : This method is used to get the attributes in SAP executed
	 * through custom RFC table
	 * 
	 * @param sUserID
	 *            User ID being reconciled for which we need the attributes
	 * @param attrMapList
	 *            ArrayList containing the set of attributes being reconciled.It
	 *            contains bean having OIM Field Name,Field Type,Table name,User
	 *            ID Field
	 * 
	 * 
	 */
	   private Attribute getCustomAttributesRFC(
			String sUserID, ArrayList<SAPUMAttributeMapBean> attrMapList) {	
		 log.info("BEGIN");
		AttributeBuilder abuilder = new AttributeBuilder();
		try {		
				for (int index = 0; index < attrMapList.size(); index++) {
				SAPUMAttributeMapBean oAttrMapBean = (SAPUMAttributeMapBean) attrMapList
				.get(index);	
				Function function = new Function("RFC_READ_TABLE", _connection, _configuration
						.getConnectorMessages(), _configuration.getEnableCUA(),
						_configuration.getRetryWaitTime());
				function.setImportValue(null, "QUERY_TABLE", oAttrMapBean.getBapiStructure(), false);
				function.setImportValue(null, "ROWSKIPS", 0, false);
				function.setImportValue(null, "ROWCOUNT", 0, false);
				JCoTable returnOption1 = function
				.getTableParameterList().getTable("OPTIONS");
				returnOption1.appendRow();
				returnOption1.setValue("TEXT",oAttrMapBean.getUserIDKeyField()
						+ " EQ '" + sUserID + "'");
				JCoTable returnOption = function.getTableParameterList()
				.getTable("FIELDS");
				returnOption.appendRow();
				returnOption.setValue("FIELDNAME",oAttrMapBean.getBapiFieldName());						
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				JCoTable jcoTable = function.getTableParameterList()
				.getTable("FIELDS");
				JCoTable jcoTable1 = function.getTableParameterList()
				.getTable("DATA");
				int icodeOffSet = 0;
				int icodeLength = 0;				
				abuilder.setName(oAttrMapBean.getOIMfieldName());
				int iReturnValueTable1 = jcoTable1.getNumRows();				
				if (iReturnValueTable1 > 0) {
					for (int iRow = 0; iRow < iReturnValueTable1; iRow++) {
						jcoTable.setRow(0);
						icodeOffSet = Integer.parseInt(jcoTable
								.getString("OFFSET"));
						icodeLength = Integer.parseInt(jcoTable
								.getString("LENGTH"));
						icodeLength = icodeLength + icodeOffSet;
						jcoTable1.setRow(iRow);
						String sMessage = jcoTable1.getString("WA");
						Object sValue = sMessage.substring(icodeOffSet, icodeLength).trim();											
						if (sValue instanceof Date) {
							// sValue= ((Date) sValue).getTime();
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy/MM/dd HH:mm:ss z");
							sValue = sdf.format(sValue);
						}
						abuilder.addValue(sValue);
					}
				} else {
					ArrayList<String> alMultiValues = new ArrayList<String>();
					abuilder.addValue(alMultiValues);
				}
			}			
		} catch (JCoException e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_CUSTOM_ATTR",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_CUSTOM_ATTR",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		}	
		log.info("END");
        return abuilder.build();

	}

	/**
	 * Description : This method is used to get the attributes in SAP executed
	 * through custom BAPI's
	 * 
	 * @param sBAPIName
	 *            Name of the BAPI being used
	 * @param sUserID
	 *            User ID being reconciled for which we need the attributes
	 * @param attrMapList
	 *            ArrayList containing the set of attributes being reconciled.It
	 *            contains bean having OIM Field Name,Field Type,Table name,User
	 *            ID Field
	 * 
	 * 
	 */
	 private Attribute getCustomAttributesBAPI(String sBAPIName,
			 String sUserID,ArrayList<SAPUMAttributeMapBean> attrMapList) {
		 log.info("BEGIN");
		AttributeBuilder abuilder = new AttributeBuilder();
		try {
			HashMap<String,JCoTable> hmTable = new HashMap<String,JCoTable>();
			JCoTable table;
			Object sValue = null;
			String sTable;
			for (int index = 0; index < attrMapList.size(); index++) {
				SAPUMAttributeMapBean oAttrMapBean = (SAPUMAttributeMapBean) attrMapList
						.get(index);
				sTable = oAttrMapBean.getBapiStructure();
				if (!hmTable.containsKey(sTable)) {
					Function function = new Function(sBAPIName, true,
							_connection, _configuration.getConnectorMessages(),
							false, _configuration.getRetryWaitTime());
					function.setImportField(oAttrMapBean.getUserIDKeyField(),
							sUserID);
					function.execute();
					table = function.getTableParameterList().getTable(sTable);
					hmTable.put(sTable, table);
				} else {
					table = (JCoTable) hmTable.get(sTable);
				}
				abuilder.setName(oAttrMapBean.getOIMfieldName());
				int iNoOfRows = table.getNumRows();
				if (iNoOfRows > 0) {
					for (int iRow = 0; iRow < iNoOfRows; iRow++) {
						table.setRow(iRow);
						sValue = table
								.getValue(oAttrMapBean.getBapiFieldName());
						if (sValue instanceof Date) {
							// sValue= ((Date) sValue).getTime();
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy/MM/dd HH:mm:ss z");
							sValue = sdf.format(sValue);
						}
						abuilder.addValue(sValue);
					}
				} else {
					ArrayList<String> alMultiValues = new ArrayList<String>();
					abuilder.addValue(alMultiValues);
				}
			}
		} catch (JCoException e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_CUSTOM_ATTR",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_CUSTOM_ATTR",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		}
		log.info("END");
		return abuilder.build();
	}
	 
	 /**
		 * Description : This method is used query the table name and return all the account ID's
		 * 
		 * @param sTableName
		 *         Name of the table to be queried using RFC_READ_TABLE
		 * @param  isFirstTimeRecon
		 *         Boolean value to indicate if it is first time or incremental recon
		 * @param  iStartRecord
		 * 		   Start Record	
		 * @param  iBatchSize    
		 *         Batch Size    
		 * @param  sExecutionTime
		 *         Execution time for incremental recon
		 * @param  isMODDA
		 * 		   Boolean value to indicate if MODDA is used or not		
		 *          
		 * 
		 */	 
	
	private Map<String, String> getAccounts(String sTableName,
			boolean isFirstTimeRecon, int iStartRecord, int iBatchSize, String sExecutionTime,
			boolean isMODDA, boolean isWildcard, String stringOpFilter) {//parameters added for Bug 18998725
		log.info("BEGIN");
		//ArrayList<String> alAccounts = new ArrayList<String>();
		Map<String, String> accountsMap = new HashMap<String, String>();
		try {
			Function function = new Function("RFC_READ_TABLE", _connection, _configuration
					.getConnectorMessages(), _configuration.getEnableCUA(),
					_configuration.getRetryWaitTime());

			function.setImportValue(null, "QUERY_TABLE", sTableName, false);
			function.setImportValue(null, "ROWSKIPS", iStartRecord, false);
			function.setImportValue(null, "ROWCOUNT", iBatchSize, false);
			// Added for  Bug 18998725 - start
			JCoTable returnOption1 = null;
			// Added for  Bug 18998725 - end
				if (!isFirstTimeRecon) {
					String sDate = sExecutionTime.substring(0, 8);
					String sTime = sExecutionTime.substring(8);
					// updated for  Bug 18998725 - start
					returnOption1 = function
							.getTableParameterList().getTable("OPTIONS");
					// updated for  Bug 18998725 - end
					returnOption1.appendRow();
					//Bug 17642440 :: Start
					/*if (isMODDA) {
						returnOption1.setValue("TEXT",
								"MODDA GE '" + sDate + "'AND MODTI GT '"
										+ sTime + "'");
					} else {
						returnOption1.setValue("TEXT",
								"MODDATE GE '" + sDate + "'AND MODTIME GT '"
										+ sTime + "'");
					}*/
					// Modified code changes made for Bug 17642440 in bug 19551686 
					// Start :: Bug 19551686- SAP UM RECONCILIATION FINDS MODIFIED USERS ONCE IN THREE TIMES
					// New Sample Query Format: MODDA GT 'YYYYMMDD' OR MODDA EQ 'YYYYMMDD' AND MODTI GT 'HHMMSS'
					// Sample Query: MODDA GT '20140117' OR MODDA EQ '20140117' AND MODTI GT '130000'
					if (isMODDA) {
						returnOption1.setValue("TEXT","MODDA GT '" + sDate + "' OR MODDA EQ '"+ sDate + "' AND MODTI GT '" + sTime+ "'");
						log.info("QUERY_TABLE-"+ sTableName + "  TEXT-" + "MODDA GT '" + sDate+ "' OR MODDA EQ '" + sDate+ "' AND MODTI GT '" + sTime + "'");

					} else {
						returnOption1.setValue("TEXT","MODDATE GT '" + sDate + "' OR MODDATE EQ '"+ sDate + "'AND MODTIME GT '" + sTime+ "'");
						log.info("QUERY_TABLE-"+ sTableName + "  TEXT-" + "MODDATE GT '"+ sDate + "' OR MODDATE EQ '" + sDate+ "'AND MODTIME GT '" + sTime + "'");
					}
					//End::Bug-19551686
				}
				
				// Added for  Bug 18998725 - start
				if(isWildcard){
					if(returnOption1==null){
						returnOption1 = function.getTableParameterList().getTable("OPTIONS");
						returnOption1.appendRow();
						returnOption1.setValue("TEXT",stringOpFilter);
					} 
				}
				// Added for  Bug 18998725 - end
				JCoTable returnOption = function.getTableParameterList()
						.getTable("FIELDS");
				returnOption.appendRow();
				//Start:: Bug 18303027 - SAP UM NW7.31 CERTIFICATION

				if(sTableName.equalsIgnoreCase("USRSTAMP")){
					returnOption.setValue("FIELDNAME","USERNAME");
					returnOption.appendRow();
					returnOption.setValue("FIELDNAME","MODDATE");
					returnOption.appendRow();
					returnOption.setValue("FIELDNAME","MODTIME");
				} else if(sTableName.equalsIgnoreCase("USH04")){
					returnOption.setValue("FIELDNAME","BNAME");
					returnOption.appendRow();
					returnOption.setValue("FIELDNAME","MODDA");
					returnOption.appendRow();
					returnOption.setValue("FIELDNAME","MODTI");
				} else {
					returnOption.setValue("FIELDNAME","BNAME");	
				}
				//END ::Bug 18303027 - SAP UM NW7.31 CERTIFICATION
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				JCoTable jcoTable = function.getTableParameterList()
						.getTable("FIELDS");
				JCoTable jcoTable1 = function.getTableParameterList()
						.getTable("DATA");
				int icodeOffSet = 0;
				int ilength=0;
				int iReturnValueTable = jcoTable.getNumRows();
				int iReturnValuesDescTable = jcoTable1.getNumRows();

				if (iReturnValueTable != 0) {
					for (int i = 0; i < iReturnValueTable; i++) {
						jcoTable.setRow(i);
						String sField = jcoTable.getString("FIELDNAME");
						//Start:: Bug 18303027 - SAP UM NW7.31 CERTIFICATION
						if ( sTableName.equalsIgnoreCase("USR02") && sField.equals("BNAME")){
							icodeOffSet = Integer.parseInt(jcoTable.getString("OFFSET"));
						} else if((sTableName.equalsIgnoreCase("USRSTAMP") && sField.equals("USERNAME")) 
								|| (sTableName.equalsIgnoreCase("USH04") && sField.equals("BNAME"))) {
							icodeOffSet = Integer.parseInt(jcoTable.getString("OFFSET"));
							ilength = Integer.parseInt(jcoTable.getString("LENGTH"));
						}
					}
					//END ::Bug 18303027 - SAP UM NW7.31 CERTIFICATION
				}				
				String sUserID = null;
				String sDateTime = null;
				if (iReturnValuesDescTable != 0) {
					for (int i = 0; i < iReturnValuesDescTable; i++) {
						jcoTable1.setRow(i);
						String sMessage = jcoTable1.getString("WA");
						if(sTableName.equalsIgnoreCase("USRSTAMP") || sTableName.equalsIgnoreCase("USH04")){
							sUserID = sMessage.substring(icodeOffSet, ilength).trim();
							sDateTime = sMessage.substring(ilength).trim();
						} else {
							sUserID = sMessage.substring(icodeOffSet).trim();
						}
						
						if(!accountsMap.containsKey(sUserID)){
							accountsMap.put(sUserID, sDateTime);
						}
						else if(Long.parseLong(sDateTime) > Long.parseLong(accountsMap.get(sUserID))){
							accountsMap.put(sUserID, sDateTime);
					     }
				}
				}
		} catch (JCoException e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_USR_ACCNT",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_USR_ACCNT",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		}
		log.info("END");
		return accountsMap;
	}
	
	
	/**
	 * Description : This method is used query the table name and get the NW Version
	 * Added for the Bug: 23211442
	 * @param sTableName
	 *         Name of the table to be queried using DELIVERY_GET_INSTALLED_COMPS
	 * 
	 */	 

	private String getNWVersion() {
		log.info("BEGIN");
		String version = null;
		try {
			Function function = new Function(SAPConstants.NW_VERSION_RFC, _connection, _configuration
					.getConnectorMessages(), _configuration.getEnableCUA(),
					_configuration.getRetryWaitTime());

			function.executeWithRetry(_configuration.getMaxBAPIRetries());

			JCoTable jcoTable = function.getTableParameterList()
					.getTable(SAPConstants.NW_VERSION_OUTPUT_TABLE);
			int noOfRows = jcoTable.getNumRows();

			for (int i = 0; i < noOfRows; i++) {
				jcoTable.setRow(i);
				String key = jcoTable.getString("COMPONENT");
				if("SAP_BASIS".equalsIgnoreCase(key)){
					version = jcoTable.getString("RELEASE");
					break;
				}
			}
		} catch (JCoException e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_USR_ACCNT",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_USR_ACCNT",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		}
		log.info("END");
		return version;
	}
	
	/**
	 * Description : This method is returns a true if the NW version passed is greater than 750 else false
	 * Added for the Bug: 23211442
	 * @param currentNWVersion
	 *         Current version of NW
	 */	 
	private boolean isHigherThanNWv750(String currentNWVersion) {
		boolean isHigerVersion = false;
		if(currentNWVersion != null){
			//Get the substring of the first character to get the major version
			int majorVersion = Integer.parseInt(currentNWVersion.substring(0, 1));

			if(majorVersion > 7){
				isHigerVersion = true;
			}else if(majorVersion == 7){
				//Get the substring of the first character to get the minor version
				int minorVersion = Integer.parseInt(currentNWVersion.substring(1, 2));
				//Checking if the minor version is greater than or equal to 5
				if(minorVersion >= 5){
					isHigerVersion = true;
				}
			}
		}
		return isHigerVersion;
	}
	
	
	 /**
	 * Description : This method is used for getting all the deleted accounts from SAP 
	 * Added for Bug : 23211442
	 * @param sTableName
	 *         Name of the table to be queried using RFC_READ_TABLE
	 * @param  isFirstTimeRecon
	 *         Boolean value to indicate if it is first time or incremental recon
	 * @param  iStartRecord
	 * 		   Start Record	
	 * @param  iBatchSize    
	 *         Batch Size    
	 * @param  sExecutionTime
	 *         Execution time for incremental recon
	 * @param  isMODDA
	 * 		   Boolean value to indicate if MODDA is used or not		
	 *          
	 * @return accountsMap
	 */	 

	private Map<String, String> getDeletedAccounts(boolean isFirstTimeRecon, String sExecutionTime) {
		log.info("BEGIN");
		//Return value
		Map<String, String> accountsMap = new HashMap<String, String>();
		try {
			Function function = new Function(SAPConstants.DELETE_USER_RFC, _connection, _configuration
					.getConnectorMessages(), _configuration.getEnableCUA(),
					_configuration.getRetryWaitTime());
			function.getImportParameterList().setValue(SAPConstants.DELETE_USER_CHANGE_PARAM, 'X');
			function.getImportParameterList().setValue(SAPConstants.DELETE_USER_DEL_PARAM, 'X');
			if(!isFirstTimeRecon){
				if(sExecutionTime != null && sExecutionTime.length() == 14){
					String sDate = sExecutionTime.substring(0, 8);
					String sTime = sExecutionTime.substring(8);
					function.getImportParameterList().setValue(SAPConstants.DELETE_USER_FDATE_PARAM, sDate);
					function.getImportParameterList().setValue(SAPConstants.DELETE_USER_FTIME_PARAM, sTime);
				}else{
					throw new Exception("Sync Token: " + sExecutionTime + " given in the scheduler job is not correct, Please give in right format");
				}
			}

			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			JCoTable jcoTable = function.getExportParameterList()
					.getTable(SAPConstants.DELETE_USER_OUTPUT_TABLE);

			for(int index = 0; index < jcoTable.getNumRows(); index++){
				jcoTable.setRow(index);
				String sUserID = jcoTable.getString("BNAME");
				String sDate = jcoTable.getString("MODDA");
				sDate = sDate.replaceAll("-", "");
				String sTime = jcoTable.getString("MODTI");
				sTime = sTime.replaceAll(":", "");
				String sDateTime = sDate + sTime;
				if(!accountsMap.containsKey(sUserID)){
					accountsMap.put(sUserID, sDateTime);
				}
				else if(Long.parseLong(sDateTime) > Long.parseLong(accountsMap.get(sUserID))){
					accountsMap.put(sUserID, sDateTime);
				}
			}
		} catch (JCoException e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_USR_ACCNT",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			log.error(_configuration.getMessage("SAP_ERR_GET_USR_ACCNT",e.getMessage()));
			throw new ConnectorException(e.getMessage());
		}
		log.info("END");
		return accountsMap;
	}

	/**
	 * Description: Gets the role and profile information for the SAP CUA target
	 * system
	 * 
	 * @param sUserID
	 *            Account ID for which Role and Profile information is required.
	 *            For example: John.Doe
	 * @param sStructure
	 *            Structure name of role or profile. For example:ACTIVITYGROUPS
	 *            	
	 */
	private JCoTable getRoleorProfile(String sUserID, String sStructure) {	
		log.info("BEGIN");
		JCoTable multiValuesTable = null;
		try {			
				String sBAPIName;
				if(_configuration.getEnableCUA()){
					if (sStructure.equalsIgnoreCase("ACTIVITYGROUPS")) {
						sBAPIName = "BAPI_USER_LOCACTGROUPS_READ";
					} else {
						sBAPIName = "BAPI_USER_LOCPROFILES_READ";
					}
				} else {
					sBAPIName = "BAPI_USER_GET_DETAIL";
				}				
				Function function = new Function(sBAPIName, true, 
						_connection, _configuration
						.getConnectorMessages(), false, _configuration.getRetryWaitTime());
				function.setImportField("USERNAME",	sUserID);                
				function.execute();				
				multiValuesTable = function.getTableParameterList()
						.getTable(sStructure);
			
		} catch (Exception e) {
	    	log.error(_configuration.getMessage("SAP_ERR_GET_ROLE_OR_PROF",e.getMessage()));
			
			throw new ConnectorException(e.getMessage());
		}		
		log.info("END");
		return multiValuesTable;
	}
	
	
	
	/**
	 * This method performs the querying and handling of the account object
	 * class for deleted users.
	 * 
	 * @param objClass
	 * @param token
	 * @param handler
	 * @param options
	 * @return
	 */
	public void executeSyncQuery(ObjectClass objClass, SyncToken token,
			SyncResultsHandler handler, OperationOptions options) { 
		log.info("BEGIN");
		if (ObjectClass.ACCOUNT.equals(objClass)) {
			try {
				if (!options.getOptions().containsKey("Disable User")){
					executeIncrementalSyncRecon(handler,token, options);
				}
				executeDeletedAccount(handler,token, options);
			} catch (JCoException e) {
				log.error(_configuration.getMessage("SAP_ERR_IN_SYNC_RECON",e.getMessage()));
				throw new ConnectorException(e.getMessage());
			} catch (InterruptedException e) {
				log.error(_configuration.getMessage("SAP_ERR_IN_SYNC_RECON",e.getMessage()));
				throw new ConnectorException(e.getMessage());
			}
		}
		log.info("END");
	}	
	
	
	
	/**
	 * Description: This method is used to build connector object for deleted objects
	 * 
	 * @param handler
	 * @param token          
	 * @param options
	 * @throws InterruptedException 
	 *          
	 *            	
	 */
	private void executeDeletedAccount(SyncResultsHandler handler,
			SyncToken token,OperationOptions options) throws JCoException, InterruptedException {
		log.info("BEGIN");
		boolean isMoreRecordsFound = true;
		boolean isFirstTime = false;
		int iStartRecord = 0;			
		Map<String,Object> optionMap=new HashMap<String,Object>();
		optionMap=options.getOptions();
		//sBatchSize = (String) optionMap.get("Batch Size");
		//sTimeZone = (String) optionMap.get("SAP System Time Zone");
		sBatchSize = _configuration.getBatchSize();
		sTimeZone = _configuration.getSapSystemTimeZone();
		int iBatchSize = Integer.parseInt(sBatchSize);
		String sDisableUser = (String) optionMap.get("Disable User");
		//START Bug 27576243 - SYNC TOKEN IS NOT UPDATED.
		//To Handle the NullPointerException Added the below Condition.
		String sExecutionTime = "";
		if(token != null) {
			sExecutionTime = (String) token.getValue();
		}
		//END Bug 27576243 - SYNC TOKEN IS NOT UPDATED.
		if (sExecutionTime.equalsIgnoreCase("")||sExecutionTime.equalsIgnoreCase("NONE")
				||sExecutionTime.equalsIgnoreCase("0")) {
			isFirstTime = true;
		}
		//DateUtil dateutil = new DateUtil();
		//Date str_date=new Date();
		//String attrValue=dateutil.parseTime(str_date, "yyyyMMddHHmmss",sTimeZone);
		boolean isContinueRecon = true;
		SyncToken oSyncToken = null;
		String modDate = null;
		int accountSize = 0;
		while (isMoreRecordsFound) {
			/*
			 * Get all accounts modified by querying the USH04 table
			 */
			// updated for  Bug 18998725 - start
			// updated for Bug 23211442 - start
			Map<String, String>	accountsMap = new HashMap<String, String>();;
			//Getting the version of NW and if greater than or equal to 7.5 executing the new BAPI SUSR_SUIM_API_RSUSR100N 
			//otherwise getting from table RFC_READ_TABLE
			boolean isHigherVersion = isHigherThanNWv750(getNWVersion());
			if(isHigherVersion){
				accountsMap = getDeletedAccounts(isFirstTime, sExecutionTime);
			}else{
				accountsMap = getAccounts(
						"USH04", isFirstTime, iStartRecord, iBatchSize, sExecutionTime,
						true, false, null);
			}
			// updated for Bug 23211442 - end
			Set<String> hmAccounts = accountsMap.keySet();
			accountSize = hmAccounts.size();
			// updated for  Bug 18998725 - end
			if (accountSize > 0) {				
				for(String sUserID:hmAccounts){
					if(!isContinueRecon){
						log.warn("Resulthandler returned :: {0}.Exiting executeDeletedAccount", 
								isContinueRecon);
						throw new InterruptedException(
								_configuration.getMessage("SAP_ERR_JOB_INTERRUPTED"));
					}
					/*
					 * Check if account is deleted in SAP.If so then return
					 * true to delete the account in OIM
					 */
					boolean isUserExists=false;
					//Start:: Bug 17575026 - Commented below and added after calling user existence check
  			 	       /*Function function = new Function("BAPI_USER_GET_DETAIL", true, 
							_connection, _configuration
							.getConnectorMessages(), false, _configuration.getRetryWaitTime());
					function.setImportField("USERNAME", sUserID);                
					function.execute();
					JCoTable table = function.getTableParameterList().getTable(
							"RETURN");
					String sMessage = table.getString("MESSAGE");
					if (!sMessage.contains("does not exist")) {
						isUserExists = true;
					}*/
					isUserExists = sSAPCreateUpdateBase.checkIfUserExists(sUserID);
					log.info(_configuration.getMessage("SAP_INFO_IS_USR_EXISTS",sUserID, isUserExists));
					
					//End:: Bug 17575026
					if(!isUserExists){
						log.info(_configuration.getMessage("SAP_INFO_USR_BE_DELETED", sUserID));
						
						ConnectorObject co = null;
						SyncDeltaBuilder sdb = new SyncDeltaBuilder();
						ConnectorObjectBuilder objectBuilder = new ConnectorObjectBuilder();
						objectBuilder.setUid(sUserID);
						objectBuilder.setName(sUserID);
						objectBuilder.setObjectClass(ObjectClass.ACCOUNT);
						if (sDisableUser.equalsIgnoreCase("Yes")) {
							objectBuilder.addAttribute(AttributeBuilder
									.buildEnabled(false));
						}
						co=objectBuilder.build();							
						if (co != null) {								
							sdb.setObject(co);
							sdb.setUid(new Uid(sUserID));
							if (sDisableUser.equalsIgnoreCase("Yes")) {
								sdb.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
							}else{
								sdb.setDeltaType(SyncDeltaType.DELETE);
							}						
							//START Bug 27576243 - SYNC TOKEN IS NOT UPDATED.
							modDate = Collections.max(accountsMap.values());
							//END Bug 27576243 - SYNC TOKEN IS NOT UPDATED.
							oSyncToken = new SyncToken(modDate);
							sdb.setToken(oSyncToken);								
							isContinueRecon = handler.handle(sdb.build());
						}
					}
				}
				iStartRecord += iBatchSize;
				// updated for Bug 23211442 based on the version of NW
				if(isHigherVersion || (!isHigherVersion && iBatchSize == 0)) {
					isMoreRecordsFound = false;
				}
			} else {
				isMoreRecordsFound = false;
			}
		}
		log.info("END");
	}
	
	
	
	/**
	 * Description: This method is used to build the multi value data for embedded objects
	 * like roles/profiles etc
	 * 
	 * @param multiValuesTable           
	 * @param sTargetAttr          
	 * @param sEmbeddedAttr
	 * @param sTableName  
	 *            	
	 */
	private Attribute buildEmbeddedAttr(JCoTable multiValuesTable,
			String sTargetAttr, String sEmbeddedAttr, String sTableName) {
		log.info("BEGIN");
		Date today=new Date();
		AttributeBuilder roleBuilder = new AttributeBuilder();
		roleBuilder.setName(sEmbeddedAttr);
		boolean isValid=true;
		boolean reconcileFutureDatedRoles=_configuration.isReconcilefuturedatedroles();
		boolean reconcilePastDatedRoles=_configuration.isReconcilepastdatedroles();
		int noOfRows = multiValuesTable.getNumRows();
		if (noOfRows > 0) {
			String[] sAttributes = sTargetAttr.split(";");
			int noOfAttr = sAttributes.length;
			for (int index = 0; index < noOfRows; index++) {
				isValid=true;
				EmbeddedObjectBuilder activityGrpBuilder = new EmbeddedObjectBuilder();
				activityGrpBuilder.setObjectClass(new ObjectClass(sTableName));
				multiValuesTable.setRow(index);
				Object fieldValue = null;
				for (int i = 0; i < noOfAttr; i++) {
					if (sAttributes[i].equalsIgnoreCase("SUBSYSTEM")) {
						if (_configuration.getEnableCUA()) {
							fieldValue = multiValuesTable
									.getValue(sAttributes[i]);
							subSystem = (String) fieldValue;
						} else //if (sAttributes[i].equalsIgnoreCase("SUBSYSTEM")) 
							{
							if(_configuration.getmasterSystem() != null)
								{
								subSystem = _configuration.getmasterSystem();
								//continue;
								fieldValue = subSystem;
								} 
							else{
								continue;
								}

							}
						//continue;	
						
					} else if (sAttributes[i].equalsIgnoreCase("AGR_NAME")
							|| sAttributes[i].equalsIgnoreCase("PROFILE")) {
						if (!_configuration.getEnableCUA()
								&& sAttributes[i].equalsIgnoreCase("PROFILE")) {
							fieldValue = multiValuesTable.getValue("BAPIPROF");
						} else {
							fieldValue = multiValuesTable
									.getValue(sAttributes[i]);
						}
						if (_configuration.getmasterSystem() != null)
							fieldValue = subSystem + "~" + fieldValue;
					} else {
						fieldValue = multiValuesTable.getValue(sAttributes[i]);
					}
					if (fieldValue instanceof Date) {
						if (sTableName.equalsIgnoreCase("ACTIVITYGROUPS")) {
							if (!reconcileFutureDatedRoles
									&& sAttributes[i]
											.equalsIgnoreCase("FROM_DAT")) {
								if (fieldValue != null
										&& ((Date) fieldValue).after(today))
									isValid = false;
							}
							if (!reconcilePastDatedRoles
									&& sAttributes[i]
											.equalsIgnoreCase("TO_DAT")) {
								if (fieldValue != null
										&& ((Date) fieldValue).before(today))
									isValid = false;
							}
						}
						fieldValue = ((Date) fieldValue).getTime();								
						// fromDate = sFromDate.getTime();
						if(_configuration.getmasterSystem()!=null) {						
							/*SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy/MM/dd HH:mm:ss z");
							fieldValue = sdf.format(fieldValue);*/
						} else {
							SimpleDateFormat sdf = new SimpleDateFormat(
							"MM/dd/yyyy");
							fieldValue = sdf.format(fieldValue);
						}
					} else {
						fieldValue = ((String) fieldValue).trim();
					}
					/*if (sAttributes[i].equalsIgnoreCase("SUBSYSTEM")) {
						continue;
					} else {*/
						activityGrpBuilder.addAttribute(sAttributes[i], fieldValue);
					//}	
				}				
				// These are multiple rows getting added to roles attribute
				if(isValid)
				roleBuilder.addValue(activityGrpBuilder.build());
			}
		} else {
			ArrayList<String> alMultiValues = new ArrayList<String>();
			roleBuilder.addValue(alMultiValues);
		}
		log.info("END");
		// Add this attribute to the connector object builder
		return roleBuilder.build();
	}
	
	/**
	 * It creates connector object with user id only 
	 * Added for Bug 18998725
	 * @param userID
	 * @return
	 */
	public ConnectorObject createConnectorObject4Wildcard(String userID) {
		log.info("BEGIN");
		ConnectorObject co = null;
		try {
			ConnectorObjectBuilder objectBuilder = new ConnectorObjectBuilder();
			objectBuilder.setUid(userID);
			objectBuilder.setName(userID);
			objectBuilder.setObjectClass(ObjectClass.ACCOUNT);
			co = objectBuilder.build();
		} catch (Exception e) {
			log.error("Error occured while creating connector object for wildcard {0}",
					e.getMessage());
			throw new ConnectorException(e);
		}
		log.info("END");
		return co;
	}
	
	/**
	 * Constructing query for string operations like contains, starts-with and ends-with
	 * Added for Bug 18998725
	 * @param query
	 * @param isFirstTimeRecon
	 * @return
	 */
	private String buildStringOpFilter(String query, boolean isFirstTimeRecon){
		StringBuffer optionBuffer = new StringBuffer();
		if (isFirstTimeRecon){
			optionBuffer.append("BNAME ");
		} else {
			optionBuffer.append("USERNAME ");
		}
		
		if(query.startsWith("%") && query.endsWith("%") ){
			optionBuffer.append("LIKE '");
		} else if(query.startsWith("!%") && query.endsWith("%")){
			optionBuffer.append("NOT LIKE '");
			query=query.substring(1);
		} 
		optionBuffer.append(query);
		optionBuffer.append("'");
		
		return optionBuffer.toString();
	}

	/**
	 * Method removes __NAME__ attribute from given query
	 * Added for Bug 19610570-filter translator
	 * @param query
	 */
	private String trimNameAttr(String query){
		StringBuffer qry = new StringBuffer();
		StringBuffer nameAttr = new StringBuffer(Name.NAME);
		nameAttr.append("=[");
		int nameIndex = query.indexOf(nameAttr.toString());
		if (nameIndex>2){
			qry.append(query.substring(0, nameIndex-2));	
		}
		String part2 = query.substring(nameIndex+10);
		int andIndex = part2.indexOf("&");
		if(andIndex>0){
			if (qry.length()>0){
				qry.append(part2.substring(andIndex));
			} else {
				qry.append(part2.substring(andIndex+2));
			}
		}
		return qry.toString();
	}
 	/**
	 * 
	 * @param handler
	 * @param token
	 * @param options
	 * @throws JCoException
	 */
	private void executeIncrementalSyncRecon(SyncResultsHandler handler,
			SyncToken token,OperationOptions options) throws JCoException {
		ArrayList<String> alFields = new ArrayList<String>();
		ArrayList<String> alCustomFields = new ArrayList<String>();		
		HashMap<String, ArrayList<SAPUMAttributeMapBean>> hmAttrMap = new HashMap<String, 
				ArrayList<SAPUMAttributeMapBean>>();
		if (options != null) {
			// Account attributes names of Target
			String[] attrsToGetArray = options.getAttributesToGet();
			ArrayList<String> attrsToGet = null;
			if (attrsToGetArray != null) {
				attrsToGet = new ArrayList<String>(Arrays.asList(attrsToGetArray));
				// During full reconciliation in OW, attrsToGet has only the
				// attribute '__NAME__' but we need to get all user attribute values
				// so, we've to add all account attributes to attrsToGet.
				if (attrsToGet.size() == 1) {
					Set<String> _accountAttributeNames = _accountAttributes.keySet();
					attrsToGet.addAll(_accountAttributeNames);
					sBatchSize = _configuration.getBatchSize();
				} else {
					// For OIM
					// Get the parameter values as entered in task scheduler
					//Map<String, Object> optionMap = new HashMap<String, Object>();
					//optionMap = options.getOptions();
					//sBatchSize = (String) optionMap.get("Batch Size");
					//sTimeZone = (String) optionMap.get("SAP System Time Zone");
					sBatchSize = _configuration.getBatchSize();
					sTimeZone = _configuration.getSapSystemTimeZone();
				}
				boolean isCUAEnabled = _configuration.getEnableCUA();
				if (!isCUAEnabled) {
					subSystem = _configuration.getmasterSystem();
				}
				//Split the attributes into standard and custom
				int length = attrsToGet.size();
				for (int i = 0; i < length; i++) {
					String sDecode = (String) attrsToGet.get(i);
					String[] keyArr = sDecode.split(";");
					int keyArrlength = keyArr.length;
					if (keyArrlength > 1) {
						if (keyArrlength > 4) {
							alCustomFields.add(sDecode);
						} else {
							alFields.add(sDecode);
						}
					} else {
						if (!sDecode.startsWith("__")){
							alEmbeddedAttributes.add(sDecode);
						}
					}
				}
									
				hmAttrMap = SAPUtil.initializeTargetReconAttrMap(alFields, isCUAEnabled);
				hmCustomAttrMap = SAPUtil.initializeCustomAttrMap(alCustomFields,isCUAEnabled);
			} else {
				sBatchSize = _configuration.getBatchSize();
			}
			String sExecutionTime = "";
			if(token != null){
				sExecutionTime = (String) token.getValue();
			}
			getSyncReconEvents(hmAttrMap, handler, sExecutionTime);
		}
	}
	/**
	 * This method is used to reconcile the modified or newly created UM/CUA target accounts.
	 * 
	 * @param hmAttrMap
	 *            HashMap containing list of parent and child attributes that needs to be
	 *            reconciled
	 * @param handler	          
	 * @param sExecutionTime           
	 * 
	 */
	private void getSyncReconEvents(HashMap<String, 
			ArrayList<SAPUMAttributeMapBean>> hmAttrMap,
			SyncResultsHandler handler,String sExecutionTime) {
		log.info("BEGIN");
		try {			
			 /* Get all the accounts based on batch size value specified in
			 * HashMap by querying the USZBVSYS/USR04 table. Loop through the
			 * HashMap and get each Account ID and call getDetails to get each
			 * account and entitlement information
			 */
			Date date = null;
			boolean isMoreRecordsFound = true;
			int iStartRecord = 0;			
			int iBatchSize = Integer.parseInt(sBatchSize);
			Set<String> alAccounts = null;
			int accountSize=0;
			SyncToken oSyncToken = null;
			String modDate = null;
			ConnectorObject co=null;
			boolean isFirstTime = false;
			if (sExecutionTime.equalsIgnoreCase("")||sExecutionTime.equalsIgnoreCase("NONE")
					||sExecutionTime.equalsIgnoreCase("0")) {
				isFirstTime = true;
			}
			boolean isContinueRecon = true;
			while (isMoreRecordsFound) {
				Map<String, String>	accountsMap = getAccounts("USRSTAMP", isFirstTime,
						iStartRecord, iBatchSize, sExecutionTime,
						 false, false, null);
				alAccounts = accountsMap.keySet();
				accountSize = alAccounts.size();
				if (accountSize > 0) {
					log.info(_configuration.getMessage("SAP_INFO_ACCID_BE_RECON",alAccounts));
					try {
						for(String sUserID:alAccounts){
							if(!isContinueRecon){
								log.warn("Resulthandler returned :: {0}.Exiting getSyncReconEvents", 
										isContinueRecon);
								throw new InterruptedException(
										_configuration.getMessage("SAP_ERR_JOB_INTERRUPTED"));
							}
							log.info(_configuration.getMessage("SAP_INFO_USRID_BE_RECON",sUserID));
							date = SAPUtil.stringToDate(accountsMap.get(sUserID), "yyyyMMddHHmmss");
							co=createConnectorObjectUser(sUserID, hmAttrMap,date);
							SyncDeltaBuilder sdb = new SyncDeltaBuilder();							
							if (co != null) {
								sdb.setObject(co);
								sdb.setUid(AttributeUtil.getUidAttribute(co.getAttributes()));
								sdb.setDeltaType(SyncDeltaType.CREATE_OR_UPDATE);
								modDate = accountsMap.get(sUserID);
		                    	oSyncToken = new SyncToken(modDate);
								sdb.setToken(oSyncToken);
								isContinueRecon = handler.handle(sdb.build());
							}
						}
					} catch (Exception ex) {
						log.error(_configuration.getMessage(
								"SAP_ERR_GET_USR_DETAIL",ex.getMessage()));
						throw ConnectorException.wrap(ex);
					}
					iStartRecord += iBatchSize;
					if (iBatchSize == 0) {
						isMoreRecordsFound = false;
					}
				} else {
					isMoreRecordsFound = false;
				}
			}					
		} catch (Exception e) {		
			log.error(_configuration.getMessage("SAP_ERR_INC_RECON",e.getMessage()));
			throw ConnectorException.wrap(e);
		}
		log.info("END");
	}
}
