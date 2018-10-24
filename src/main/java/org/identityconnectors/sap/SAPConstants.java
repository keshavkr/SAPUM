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

public interface SAPConstants {
	
	// PERSONNEL NUMBER ATTRIBUTE
	
    public static final String PERSONNEL_NUMBER = "PERNR";
    
    // PASSWORD ATTRIBUTES - OPERATIONAL
	
    public static final String ATTR_PASSWORD = "__PASSWORD__"; 
    
    public static final String ATTR_CURR_PASSWORD = "__CURRENT_PASSWORD__";
    
    // ACCOUNT LOCK ATTRIBUTES - OPERATIONAL
    
    public static final String ATTR_ACCOUNT_LOCKED_WRNG_PWD = "ISLOCKED;WRNG_LOGON";  
    
	public static final String ATTR_ACCOUNT_LOCKED_NO_PWD = "ISLOCKED;NO_USER_PW";
	
	public static final String ACCOUNT_LOCKED =  "User Lock;NONE;NONE;NONE";
	
	
	// LOGON DATA ATTRIBUTES    
    
    public static final String ATTR_ACCOUNTING_NUMBER = "ACCNT;LOGONDATA;ACCNT;LOGONDATAX";
    
    public static final String ATTR_GROUPS ="CLASS;LOGONDATA;CLASS;LOGONDATAX";
    
    public static final String ATTR_VALID_FROM ="GLTGV;LOGONDATA;GLTGV;LOGONDATAX";
    
    public static final String ATTR_VALID_THRO ="GLTGB;LOGONDATA;GLTGB;LOGONDATAX";      
    
    public static final String ATTR_TIME_ZONE  = "TZONE;LOGONDATA;TZONE;LOGONDATAX";
    
    public static final String ATTR_USER_TYPE = "USTYP;LOGONDATA;USTYP;LOGONDATAX"; 
    // modified for contract test
    public static final String LOGONDATA_LTIME = "LTIME;LOGONDATA;LTIME;LOGONDATAX";
	
    
    // DEFAULT DATA ATTRIBUTES
    
    public static final String ATTR_COST_CENTER ="KOSTL;DEFAULTS;KOSTL;DEFAULTSX";
        
    public static final String ATTR_DATE_FORMAT = "DATFM;DEFAULTS;DATFM;DEFAULTSX";
    
    public static final String ATTR_DECIMAL_NOTATION ="DCPFM;DEFAULTS;DCPFM;DEFAULTSX";    
    
    public static final String ATTR_LOGON_LANGUAGE = "LANGU;DEFAULTS;LANGU;DEFAULTSX";
    
    public static final String ATTR_START_MENU = "START_MENU;DEFAULTS;START_MENU;DEFAULTSX";   
    
	
	// ADDRESS ATTRIBUTES
	
    public static final String ATTR_ACCOUNT = "USERNAME;BAPIBNAME";	
	
    public static final String ATTR_FIRSTNAME =  "FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX";    
        
    public static final String ATTR_LASTNAME =  "LASTNAME;ADDRESS;LASTNAME;ADDRESSX";
    
    public static final String ATTR_E_MAIL =  "E_MAIL;ADDRESS;E_MAIL;ADDRESSX";

    public static final String ATTR_TITLE_P = "TITLE_P;ADDRESS;TITLE_P;ADDRESSX";

    public static final String ATTR_LANGUAGE_KEY_P  = "LANGU_P;ADDRESS;LANGU_P;ADDRESSX";

    public static final String ATTR_FUNCTION  = "FUNCTION;ADDRESS;FUNCTION;ADDRESSX";

    public static final String ATTR_COMM_TYPE  = "COMM_TYPE;ADDRESS;COMM_TYPE;ADDRESSX";
    
    public static final String ATTR_FAX_NUMBER  = "FAX_NUMBER;ADDRESS;FAX_NUMBER;ADDRESSX";
    
    public static final String ATTR_FAX_EXTENSION  = "FAX_EXTENS;ADDRESS;FAX_EXTENS;ADDRESSX";   
     
    public static final String ATTR_TELEPHONE_EXT = "TEL1_EXT;ADDRESS;TEL1_EXT;ADDRESSX";
    
    public static final String ATTR_TELEPHONE_NUMBER = "TEL1_NUMBR;ADDRESS;TEL1_NUMBR;ADDRESSX";
    
    public static final String ATTR_ROOM_NUMBER	= "ROOM_NO_P;ADDRESS;ROOM_NO_P;ADDRESSX";  
    
    public static final String ATTR_BUILDING = "BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX";
    
    public static final String ATTR_DEPARTMENT = "DEPARTMENT;ADDRESS;DEPARTMENT;ADDRESSX";
    
    public static final String ATTR_FLOOR = "FLOOR_P;ADDRESS;FLOOR_P;ADDRESSX";
    
    // ALIAS ATTRIBUTE
    
    public static final String ATTR_ALIAS = "USERALIAS;ALIAS;BAPIALIAS;ALIASX"; 
    
    // COMPANY ATTRIBUTE
   
    public static final String ATTR_COMPANY = "COMPANY;COMPANY;COMPANY;COMPANYX";
    
    // UCLASS ATTRIBUTE
    
    public static final String ATTR_CONTRACTUAL_USER_TYPE = "LIC_TYPE;UCLASS;UCLASS;UCLASSX";
    
    // SUBSYSTEM ATTRIBUTES
    
    public static final String ATTR_CUA_SYSTEMS = "SUBSYSTEM;SYSTEMS";
    
    public static final String SUBSYSTEM = "SUBSYSTEM";
    
    //GROUP ATTRIBUTE
    
    //public static final String USERGROUP = "USERGROUP;GROUPS;USERGROUP;GROUPSX";
    //public static final String USERGROUP = "USERGROUP";
    public static final String USERGROUP = "USERGROUP;GROUPS;USERGROUP;GROUPS";
	
        
    // Profile Attribute Map
    
    public static final String ATTR_PROFILES = "PROFILE";    
     
    public static final String PROFILE = "PROFILE";
    
    public static final String PROF_TEXT = "BAPIPTEXT";
    
    public static final String PROF_TYPE = "BAPITYPE";
    
    public static final String PROF_AKTPS = "BAPIAKTPS";  

        
    //Roles- ActivityGroups Attribute map
    
    public static final String ATTR_ACTIVITY_GROUPS = "AGR_NAME";    
    
    public static final String AGR_FROM_DAT = "FROM_DAT";
    
    public static final String AGR_TO_DAT = "TO_DAT";
    
    public static final String AGR_TEXT = "AGR_TEXT";
    
    public static final String AGR_ORG_FLAG = "ORG_FLAG";
    
    
    //parameter Attribute Map
    
    public static final String PARID = "PARID";
    
    public static final String PARVA = "PARVA";
    
    public static final String PARTXT = "PARTXT"; 
   
    
    
    //Embedded object 
	
	public static final String ATTR_ROLES_EMBEDED = "roles";
	
	public static final String ATTR_PROFILES_EMBEDED = "profiles";
	
	public static final String ATTR_PARAMETERS_EMBEDED = "parameters";
	
	public static final String ATTR_GROUPS_EMBEDED = "groups";
	
	//Bug: 23211442 NW version RFC and output table
	public static final String NW_VERSION_RFC = "DELIVERY_GET_INSTALLED_COMPS";
	public static final String NW_VERSION_OUTPUT_TABLE = "TT_COMPTAB";
	
	//Bug: 23211442 NW Deleted users RFC, Parameters and output table
	public static final String DELETE_USER_RFC = "SUSR_SUIM_API_RSUSR100N";
	public static final String DELETE_USER_CHANGE_PARAM = "IV_CHANGE";
	public static final String DELETE_USER_DEL_PARAM = "IV_USER_DEL";
	public static final String DELETE_USER_FDATE_PARAM = "IV_FDATE";
	public static final String DELETE_USER_FTIME_PARAM = "IV_FTIME";
	public static final String DELETE_USER_OUTPUT_TABLE = "ET_OUTPUT_CHANGE";
	
}

