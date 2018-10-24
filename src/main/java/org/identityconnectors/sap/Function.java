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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectionBrokenException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.ConnectorMessages;

import com.sap.conn.jco.ConversionException;
import com.sap.conn.jco.JCo;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoRuntimeException;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

public class Function {
    private static final Log log = Log.getLog(Function.class);

    private SAPConnection _connection;
    private ConnectorMessages _conMsgs;
    
    /* 
     * Wraps JCoFunction to facilitate rebuilding of the JCoFunction when the 
     * connection is reset.
     */
    private String _functionName;
    private boolean _update = true;
    private boolean _enableCUA = false; // needed to determine update token for some fields
    private String _accountId;  // a lot of the BAPIs require a user name
    private List<Object> _cache; // cache for the assigned values in case we have
    private int _retryWait = 500;
    // to recreate the function.
    /*
     * Each element of cache is a String array object
     *      String[] values = {
     *          structOrTable,
     *          attrName,
     *          attrValue,
     *          (update ? UPDATE_TOKEN : null)
     *      };
     */
    
    static final String UPDATE_TOKEN = "X";
    static final String REPLACE_TOKEN = "R";

    private JCoFunction _function;

    /**
     * Create a JCoFunction to execute on an SAP system
     * @param bapiName
     * @param identity
     * @param connection
     * @param conMsg
     * @param enableCUA
     * @param retryWait
     * @throws JCoException
     */
    public Function(String bapiName, String identity, SAPConnection connection, ConnectorMessages conMsg, 
                       boolean enableCUA, int retryWait) throws JCoException {
        this(bapiName, false, connection, conMsg, enableCUA, retryWait);
        setUserField(identity);
    }

    /**
     * Create a JCoFunction to execute on an SAP system
     * @param bapiName
     * @param connection
     * @param conMsg
     * @param enableCUA
     * @param retryWait
     * @throws JCoException
     */
    public Function(String bapiName, SAPConnection connection, ConnectorMessages conMsg, 
                    boolean enableCUA, int retryWait) throws JCoException {
        this(bapiName, false, connection, conMsg, enableCUA, retryWait);
    }

    /**
     * Create a JCoFunction to execute on an SAP system
     * @param bapiName
     * @param identity
     * @param retryOnFailedConnection
     * @param connection
     * @param conMsg
     * @param enableCUA
     * @param retryWait
     * @throws JCoException
     */
    public Function(String bapiName, String identity, boolean retryOnFailedConnection, SAPConnection connection, 
                       ConnectorMessages conMsg, boolean enableCUA, int retryWait) throws JCoException {
        this(bapiName, retryOnFailedConnection, connection, conMsg, enableCUA, retryWait);
        setUserField(identity);
    }

    /**
     * Create a JCoFunction to execute on an SAP system
     * @param bapiName
     * @param retryOnFailedConnection
     * @param connection
     * @param conMsg
     * @param enableCUA
     * @param retryWait
     * @throws JCoException
     */
    public Function(String bapiName, boolean retryOnFailedConnection, SAPConnection connection, 
                    ConnectorMessages conMsg, boolean enableCUA, int retryWait) throws JCoException {
                
    	this._functionName = bapiName;
        this._connection = connection;
        this._conMsgs = conMsg;
        this._enableCUA = enableCUA;
        this._retryWait = retryWait;
        
        
        String message=null;
        message=getMessage("SAP_INFO_CREATING_BAPI", bapiName, (retryOnFailedConnection ? "true" : "false"));
        log.info(message);
        
        retryOnFail:
            try {
                createFunction();
            } catch (JCoException e) {
                int group = e.getGroup();
                if (retryOnFailedConnection &&
                        ((group == JCoException.JCO_ERROR_COMMUNICATION) ||
                                (group == JCoException.JCO_ERROR_PROTOCOL) ||
                                (group == JCoException.JCO_ERROR_SYSTEM_FAILURE))) {
                    _connection.dispose();
                    try {
                        try {
                            Thread.sleep(_retryWait);
                        } catch (InterruptedException ee) {
                            log.error(ee.getMessage());
                        }
                        // TODO : remove this if possible
                        // _connection = new SAPConnection(_configuration);
                        _connection.connect();
                    } catch (Exception jcoe) {
                        Throwable e2 = jcoe;
                        if (e2 != null && e2 instanceof JCoException) {
                            // trace the exception
                            log.error(jcoe, "");
                            throw (JCoException) e2;
                        } else {
                                                       
                        	message=getMessage("SAP_INFO_NON_JCO_EXCEPTION", jcoe.getMessage());
                            log.ok(message);
                                                       
                        }
                    }
                    retryOnFailedConnection = false; // only retry once
                    break retryOnFail;
                } else {
                    log.error(e, "");
                    throw e;
                }
            }
    }

    /**
     * Send xml version of the function to a file.
     * @param beforeAfter
     */
    private void traceFunction(String beforeAfter) {
        File traceDirectory = null;
        boolean jcoTrace = JCo.getTraceLevel() > 0;
        String message=null;
        if (jcoTrace) {
            traceDirectory = new File(JCo.getTracePath());
            try {
                File f = File.createTempFile(beforeAfter + "-", ".xml", traceDirectory);
                FileWriter writer = new FileWriter(f);
                writer.write(_function.toXML());
                writer.close();
            } catch (IOException ex) {
            	
            	message=getMessage("SAP_ERR_JCO_TRCE_FILE"); 			
                log.error(ex, message);
            }
        }
    }
    
    /**
     * This method executes the stored JCO function by calling the
     * connection execute.
     */
    public void execute() throws JCoException {
        String message=null;
        if (_accountId != null && !_accountId.equals("")) {
        	message=getMessage("SAP_INFO_EXE_FUN_ACCID", _function.getName(), _accountId); 
        } else {
           	message=getMessage("SAP_INFO_EXE_FUN_NO_ACCID", _function.getName()); 
        }
        log.ok(message);
        traceFunction("before");
        _connection.execute(_function);
        traceFunction("after");
    }

    /**
     * Makes the actual call to the SAP server.
     * @throws JCoException if one occurs during when calling the JCO methods
     */
    public void executeWithRetry(int maxExecRetries) throws JCoException {
    	String message=null;
        if (_function != null &&  _connection._destination != null) {

            traceFunction("before");

            int numExecRetries = 0;
            do {
                try {
                    if (numExecRetries > 0) {
                        _connection.dispose();
                        // TODO: remove this if possible
//                        _connection = new SAPConnection(_configuration);
                        _connection.connect();
                        // recreate the Function object again.
                        this.recreate();
                    }
                    _connection.execute(_function);
                    numExecRetries = maxExecRetries;
                } catch (JCoException e) {
                    int group = e.getGroup();
                    log.ok(getMessage("SAP_INFO_JCO_EXP_GRP", group));
                                     
                                        
                    if ((group == JCoException.JCO_ERROR_COMMUNICATION) ||
                            (group == JCoException.JCO_ERROR_PROTOCOL) ||
                            (group == JCoException.JCO_ERROR_SYSTEM_FAILURE)) {
                         	 log.ok(getMessage("SAP_INFO_JCO_COMMN_ERR"));
                    	
                        try {
                            Thread.sleep(_retryWait);
                        } catch (InterruptedException ee) {
                            log.error(ee.getMessage());
                        }
                    } else {
                       
                    	log.error(e, getMessage("SAP_ERR_JCO"));
                    	
                        throw e;
                    }
                } catch (Throwable t) {
                
                	log.ok(getMessage("SAP_INFO_NON_JCO_EXP"));
                }
            } while (numExecRetries++ < maxExecRetries);

            traceFunction("after");
        } else {
            message = _conMsgs.format("SAP_ERR_JCO_NULL", "SAP_ERR_JCO_NULL");
            NullPointerException npe = new NullPointerException(message);
            log.error(npe, message);
            throw npe;
        }
    }
    /**
     * Returns the name of the JCoFunction. For example "BAPI_USER_CHANGE"
     * @return the name of the JCoFunction that was created.
     */
    public String getName() {
        return _functionName;
    }

    /**
     * Return an XML version of the function.
     * @return
     */
    public String toXML() {
        return _function.toXML();
    }

    /**
     * Retrieves the table with the specified name.
     * WARNING: The returned table should only be used to read data from the
     * table. If writing data to the table, use 'appendTableRow' in order to
     * have the function call recreated correctly if the connection to the
     * SAP system fails.
     * @param tableName - table name to retrieve.
     * @return the JCoTable
     * @throws JCoException
     */
    public JCoTable getTable(String tableName) throws JCoException {
        JCoParameterList paramList = _function.getTableParameterList();
        JCoTable table = null;
        if (paramList != null)
            table = paramList.getTable(tableName);
        return table;
    }

    /**
     * Retrieves the export structure with the specified name.
     * @param structureName
     * @return the specified JCoStructure
     * @throws JCoException
     */
    public JCoStructure getStructure(String structureName) throws JCoException {
        JCoParameterList paramList = _function.getExportParameterList();
        JCoStructure structure = null;
        if (paramList != null)
            structure = paramList.getStructure(structureName);
        return structure;
    }

    /**
     * Retrieve export parameter list.
     * @return JCoParameterList
     * @throws JCoException
     */
    public JCoParameterList getExportParameterList() throws JCoException {
        return _function.getExportParameterList();
    }

    /**
     * Retrieve Import parameter list.
     * @return JCoParameterList
     * @throws JCoException
     */
    public JCoParameterList getImportParameterList() throws JCoException {
        return _function.getImportParameterList();
    }

    /**
     * Retrieve Table Parameter List.
     * @return JCoParameterList
     * @throws JCoException
     */
    public JCoParameterList getTableParameterList() throws JCoException {
        return _function.getTableParameterList();
    }

    /**
     * Recreates the JCoFunction. This typically occurs when the connection to
     * the SAP gateway (or system) is lost and a new connection is restored.
     * @throws JCoException
     */
    public synchronized void recreate() throws JCoException {
       
    	_update = false; // don't update the cached values.
    	
        log.info(getMessage("SAP_INFO_RECREATE_FUNC" , _functionName));
        
        this.createFunction();
        if (_accountId != null) {
            this.setUserField(_accountId);
        }
        if (_cache != null) {
            Iterator<Object> i = _cache.iterator();
            while (i.hasNext()) {
                Object[] element = (Object[]) i.next();
                this.setImportValue((String)element[0],
                                    (String)element[1],
                                    element[2],
                                    (element[3] == null ? false : true));
            }
        }
        _update = true;
    }

    /**
     * Creates a JCoFunction object using the '_functionName' variable.
     * @throws JCoException
     */
    private void createFunction() throws JCoException {
        JCoRepository sapRepository = _connection.getRepository();

        // try to reconnect to see if we can get the IRepository 
        if (sapRepository == null) {
            _connection.dispose();
            _connection.connect();
            sapRepository = _connection.getRepository();
        }

        // if we can, then build
        if (sapRepository != null) {
        	//log.error("Perf: getFunctionTemplate started");
            JCoFunctionTemplate template = sapRepository.getFunctionTemplate(_functionName.toUpperCase());
        	//log.error("Perf: getFunctionTemplate completed");
            // The only way to know if the function is actually available
            // on the SAP system is to check to see if the template is non-null. 
            if (template == null) {
                JCoException e =  new JCoException(JCoException.JCO_ERROR_FUNCTION_NOT_FOUND,
                                                   "JCO_ERROR_FUNCTION_NOT_FOUND",
                "JCO_ERROR_FUNCTION_NOT_FOUND");
                log.error(e,  getMessage("SAP_ERR_CREATE_FUNC", _functionName) );
                throw e;
            } else {
            	//log.error("Perf: getFunction started");
                _function = template.getFunction();
            	//log.error("Perf: getFunction completed");
            }
        } else {
            // A JCO IRepository object can only be obtained with a valid connection.
            // So, by implication, if the sapRepository is null, the connection is
            // invalid.

            ConnectionBrokenException cbe = new ConnectionBrokenException(_conMsgs.format("SAP_ERR_JCO_FUNC_CREATE", "SAP_ERR_JCO_FUNC_CREATE"));
            log.error(cbe, "");
            throw cbe;
        }
    }

    /**
     * Handles values to add to user table. Used to suppress compiler warnings.
     * @param userTable
     * @param attributeName
     * @param value
     */
    @SuppressWarnings("unchecked")
    private void handleValue(JCoTable userTable, String attributeName, Object value) {
        if (value instanceof List) {
            for (String valStr : (List<String>)value) {
                userTable.appendRow();
                userTable.setValue(attributeName, valStr);
            }
        } else {
            userTable.appendRow();
            userTable.setValue(attributeName, value);
        }
    }

    /**
     * This method sets values in the ImportParameterList.
     * @param structOrTable
     * @param attributeName
     * @param value
     * @param update
     * @throws JCoException
     */
    public void setImportValue(String structOrTable, String attributeName,
                                  Object value, boolean update) throws JCoException {
        setImportValue(structOrTable, attributeName, value, update, null);
    }
    
    /**
     * Sets the an import value for the function.
     * @param structOrTable
     * @param attributeName
     * @param value
     * @param update
     * @param tableFormats
     * @throws JCoException
     */
    @SuppressWarnings("unchecked")
    public void setImportValue(String structOrTable, String attributeName,
                                   Object value, boolean update, Map<String, String> tableFormats) throws JCoException {
        // do not want passwords to show in the trace
        if (attributeName.equals("PASSWORD") || attributeName.equals("NEW_PASSWORD") || attributeName.equals("BAPIPWD") || attributeName.equals("ZXLOLD_PASSWORD") || attributeName.equals("ZXLNEW_PASSWORD") )
        {   
        	final char[] _array = new char[50];
        	((GuardedString)value).access(new GuardedString.Accessor() {
            	public void access(char[] clearChars) {
					try {
				        System.arraycopy(clearChars, 0, _array, 0, clearChars.length); 
						//destProps.put(JCO_PASSWD, new String(clearChars));	
					} catch (Exception sException) {
						log.error(sException.getMessage());
					}
            	}
           });
        	value = new String(_array).trim();
            log.info(getMessage("SAP_INFO_SET_STRUCT", structOrTable, attributeName, "********",update ? "update=true" : "update=false"));
			//log.error("Perf: Setting structure or table {0} attribute name {1} with value {2} for user {3}", structOrTable, attributeName, "********",_accountId);			
        } else {
			//log.error("Perf: Setting structure or table {0} attribute name {1} with value {2} for user {3}", structOrTable, attributeName, value,_accountId);        
            log.info(getMessage("SAP_INFO_SET_STRUCT", structOrTable, attributeName, value,
                     update ? "update=true" : "update=false"));
        } // if attributeName

        boolean structure = false;
        boolean table = false;

        JCoParameterList paramList = null;
        if (structOrTable != null && structOrTable.length() > 0) {

            if (isStructure((paramList = _function.getImportParameterList()).getListMetaData(),
                            structOrTable)) {
                structure = true;
            } else {
                paramList = _function.getTableParameterList();
                if (paramList != null && isTable(paramList.getListMetaData(), structOrTable)) {
                    table = true;
                }
            }

            if (structure) {
                JCoStructure userData = paramList.getStructure(structOrTable);
                log.ok( getMessage("SAP_INFO_STRUCT_MSG", userData));
               
                try {	/*if(attributeName.equalsIgnoreCase("GLTGB")||attributeName.equalsIgnoreCase("GLTGV")){
                		if(!value.toString().equalsIgnoreCase("0")){
                		Long lDate = new Long(value.toString());
                		Date d = new Date(lDate);
    					DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
    					// Converting above date format to "yyyyMMddHHmmss'Z'" which
    					// is a target date format
    					value = dateFormatter.format(d);
    					userData.setValue(attributeName, SAPUtil.stringToDate((String)value, null));
                		}
                	} else{*/
           
                		userData.setValue(attributeName, value);
                	//}
                } catch (ConversionException ce) {
                   /* // assume we wanted a date here
                    try {
                        userData.setValue(attributeName, SAPUtil.stringToDate((String)value, null));
                    } catch (Exception e) {
                        // if there is still an exception, throw the original
                        throw ce;
                    }*/
                }

                // When updating a user you must also set the corresponding
                // update table.
                // For example if you are updating the value FIRSTNAME in the
                // ADDRESS table, you must also set FIRSTNAME to 'X' in the
                // ADDRESSX table. This tells SAP that you really do want to
                // change this user.
                // Note: this is the same for table updates
                if (update) {
                    JCoStructure updateStructure = _function.getImportParameterList().getStructure(structOrTable + UPDATE_TOKEN);
                    // special case USERALIAS otherwise we'll 
                    // throw: JCO_ERROR_FIELD_NOT_FOUND: Field USERALIAS
                    // not a member of BAPIALIASX
                    // Since we get BAPIALIASX back from SAP we have to assume
                    //  that this is a special case.
                    if (attributeName.equalsIgnoreCase("useralias")) {
                        updateStructure.setValue("BAPIALIAS", UPDATE_TOKEN);
                    } else if (updateStructure.getMetaData().getName().equalsIgnoreCase("BAPIUCLASSX")) {
                        if (_enableCUA) {
                            updateStructure.setValue("UCLASSSYS", REPLACE_TOKEN);
                        } else {
                            updateStructure.setValue("UCLASS", UPDATE_TOKEN);
                        }
                    } else {
                        updateStructure.setValue(attributeName, UPDATE_TOKEN);
                    } // if .. else attributeName
                }
            } else if (table) {
                JCoTable userTable = paramList.getTable(structOrTable);
              
                log.info( getMessage("SAP_INFO_TABLE_MSG", userTable));
               
                
                String format = null;
                
                // "->TABLE" is used to denote an attribute is a table
                // this handles groups manually
                if (structOrTable.equals("GROUPS") &&
                    !attributeName.equals("TABLE")) {
                    format = "Y|:" + attributeName;
                } else if (tableFormats != null) {
                    // we still want to be able to set normal tables
                    // tableFormats should only contain values of tables
                    // defined in the schema as <TABLENAME>->TABLE
                    format = tableFormats.get(structOrTable);
                }
                
                if (format != null) {
                    StringTokenizer st = new StringTokenizer(format, "|:");
                    boolean updateTable = false;
                    int stCount = st.countTokens();
                    if (stCount > 0) {
                        // the format will have Y or N as its first value
                        // this determines whether the table has an update table or not
                        updateTable = st.nextToken().equals("Y") ? true : false;
                    }
                    
                    JCoStructure updateStruct = null;
                    if (update &&
                        updateTable) {
                        updateStruct = _function.getImportParameterList().getStructure(structOrTable + UPDATE_TOKEN);
                    }
                    if (value != null &&
                        value instanceof List &&
                        !((List<String>)value).isEmpty()) {
                        userTable.clear();
                        userTable.firstRow();
                        
                        // put columns in list so that we can parse easier
                        List<String> colList = new ArrayList<String>();
                        while(st.hasMoreTokens()) {
                            colList.add(st.nextToken());
                        }
                        
                        StringTokenizer formatT = null;
                        for (String valStr : (List<String>)value) {
                            formatT = new StringTokenizer(valStr, "|:");
                            // make sure format and string have the same number of tokens
                            if (formatT.countTokens() == colList.size()) {
                                userTable.appendRow();
                                for (String col : colList) {
                                    if (userTable.getMetaData().hasField(col)) {
                                        userTable.setValue(col, formatT.nextToken());
                                        if (updateStruct != null) {
                                            updateStruct.setValue(col, UPDATE_TOKEN);
                                        }
                                    } else {
                                        String msg = _conMsgs.format("SAP_ERR_INV_COL", col, structOrTable);
                                        log.error(msg);
                                        throw new ConnectorException(msg);
                                    }
                                }

                            } else {
                                String msg = _conMsgs.format("SAP_ERR_INV_TBL_FRMT", structOrTable);
                                log.error(msg);
                                throw new ConnectorException(msg);
                            }
                        }
                    } else if (!(value instanceof List)) {
                       
                    	String msg= getMessage("SAP_INFO_LIST_EXPT_ATTR", attributeName);
                        log.error(msg);
                        throw new ConnectorException(msg);
                    } else if (updateStruct != null) {
                        // empty value so we need to clear the values in table by setting
                        // the update field
                        for (int i = 0; i < userTable.getFieldCount(); i++) {
                            String tFieldName = userTable.getMetaData().getName(i);
                            updateStruct.setValue(tFieldName, UPDATE_TOKEN);
                        }
                    }
                    
                } else {
                    handleValue(userTable, attributeName, value);
                }
            }
        } else { //try it as a field
            log.info( getMessage("SAP_INFO_FIELD_MSG", structOrTable));
        	
            if (value instanceof Boolean) {
                _function.getImportParameterList().setValue(attributeName, (int)(((Boolean)value) ? 1 : 0));
            } else {
                _function.getImportParameterList().setValue(attributeName, value);
            }
            if (update)
                _function.getImportParameterList().setValue(attributeName + UPDATE_TOKEN, UPDATE_TOKEN);
        }

        updateCache(structOrTable, attributeName, value, update);
    }

    /**
     * A convenience method which sets the USERNAME field for this object.
     * Some JCoFunction objects may not contain a USERNAME field, but
     * most of them that are used in this connector do.
     * 
     * @param identity - the value to set for USERNAME.
     */
    public void setUserField(String accountId) {
      
    	log.info(getMessage("SAP_INFO_SET_USRNAME", accountId));
        
        if (_update) this._accountId = accountId;
        _function.getImportParameterList().setValue(SAPConnector.USERNAME, accountId);

    }

    /**
     * Updates the cache with attribute name and value.
     * @param structOrTable
     * @param attrName
     * @param attrValue
     * @param update
     */
    private synchronized void updateCache(String structOrTable, String attrName,
                                          Object attrValue, boolean update) {
        // Update the cache
        if (_update) {
            if (_cache == null) _cache = new ArrayList<Object>();
            Object[] values = {
                    structOrTable,
                    attrName,
                    attrValue,
                    (update ? UPDATE_TOKEN : null)
            };
            _cache.add(values);
        }
    }

    /**
     * Set field to value.
     * @param fieldName
     * @param value
     * @throws JCoException
     */
    public void setImportField(String fieldName, Object value) throws JCoException {
                
        if (fieldName.equals("PASSWORD")) {
           
        	log.info(getMessage("SAP_INFO_SET_FIELD", fieldName, "********"));
        	
        } else {
            
        	log.info(getMessage("SAP_INFO_SET_FIELD", fieldName, value));
        }
        _function.getImportParameterList().setValue(fieldName, value);
        updateCache(null, fieldName, value, false);
    }

    /**
     * Set field to value.
     * @param fieldName
     * @param value
     * @throws JCoException
     */
    public void setTableField(String fieldName, String value) throws JCoException {
       
        if (fieldName.equals("PASSWORD")) {
           
            log.info(getMessage("SAP_INFO_SET_FIELD", fieldName, "********"));
        } else {
            
        	log.info(getMessage("SAP_INFO_SET_FIELD", fieldName, value));
        }
        _function.getTableParameterList().setValue(value, fieldName);
        updateCache(null, fieldName, value, false);
    }

    /**
     * Append row to a table with the specified value.
     * @param table
     * @param rowName
     * @param value
     */
    public void appendTableRow(JCoTable table, String rowName, Object value) {
      
        log.info(getMessage("SAP_INFO_ADD_ROW", rowName, value));
        
        if (table != null) {
            table.appendRow();
            table.setValue(rowName, value);
        }
    }

    /**
     * Set row in table with a specific value.
     * @param table
     * @param rowName
     * @param value
     */
    public void setTableRowValue(JCoTable table, String rowName, Object value) {
        
        log.info(getMessage("SAP_INFO_SET_ROW", rowName, value));
        if (table != null) {
            table.setValue(rowName, value);
        }
    }

    public boolean isTable(JCoMetaData metaData, String tableName) {
        boolean isTable = false;
        try {
            isTable = (metaData != null) && metaData.isTable(tableName);
        } catch (JCoRuntimeException e) {
            //isTable = e.getGroup() != JCoException.JCO_ERROR_FIELD_NOT_FOUND;
        }
        return isTable;
    }

    public boolean isStructure(JCoMetaData metaData, String structName) {
        boolean isStruct = false;
        try {
            isStruct = (metaData != null) && metaData.isStructure(structName);
        } catch (JCoRuntimeException e) {
            //isStruct = e.getGroup() != JCoException.JCO_ERROR_FIELD_NOT_FOUND;
        }
        return isStruct;
    }

    /**
     * Retrieves the messages from the RETURN table and throws an exception if an error message exists
     * @throws ConnectorException
     */
    public void jcoErrorCheck() throws ConnectorException, JCoException {
        JCoParameterList paramList = this.getExportParameterList();
        if (paramList != null && this.isStructure(paramList.getListMetaData(), "RETURN")) {
            JCoStructure struct = paramList.getStructure("RETURN");
            if (struct != null) {
                String type = struct.getString("TYPE");
                String message = struct.getString("MESSAGE");
                if (type.equals("E")) {
                    log.error(message);
                    ConnectorException ce = new ConnectorException(message);
                    throw ce;
                } else { 
                    // TODO: store results message if its not an error?
                    log.info(message);
                }
            }
        } else {
            paramList = this.getTableParameterList();
            if ((paramList != null) && this.isTable(paramList.getListMetaData(), "RETURN")) {
                JCoTable table = paramList.getTable("RETURN");
                if (table != null) {
                    StringBuffer messages = null;
                    for (int i = 0; table != null && i < table.getNumRows(); i++) {
                        table.setRow(i);
                        int number = table.getInt("NUMBER");
                        String type = table.getString("TYPE");
                        String message = table.getString("MESSAGE");
                        if (type.equals("E") && number != 124) {
                            if (messages == null) {
                                messages = new StringBuffer();
                            }
                            messages.append(message);
                            messages.append("\n");
                            log.error(message);
                        } else {
                            // TODO: store results message if its not an error?
                            log.info(message);
                        }
                    } // for each returned table row

                    if (messages != null) {
                        throw new ConnectorException(messages.toString());
                    }
                }
            }
        }
    }
    
    /**
     * In some cases you want to know whether or not you need to throw an Unknown UID.
     * @throws JCoException
     */
    public void jcoUserNotExistCheck() throws JCoException {
        JCoParameterList paramList = this.getExportParameterList();
        paramList = this.getTableParameterList();
        if ((paramList != null) && this.isTable(paramList.getListMetaData(), "RETURN")) {
            JCoTable table = paramList.getTable("RETURN");
            if (table != null) {
                for (int i = 0; table != null && i < table.getNumRows(); i++) {
                    table.setRow(i);
                    int number = table.getInt("NUMBER");
                    String type = table.getString("TYPE");
                    String message = table.getString("MESSAGE");
                    if (type.equals("E") && number == 124) {
                        log.error(message);
                        throw new UnknownUidException(message);
                    } 
                } // for each returned table row
            }
        }
    }
    /**
     * This method gets the connector message with the given key and formats it 
     * with the given objects.
     * @param key - String to retrieve
     * @param objects - Objects to be inserted with the formated message key.
     * @return - formatted message
     */
    private String getMessage(String key, Object... objects) {
        return _conMsgs.format(key, key, objects);
    }
}