/*
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description :SAPUME Connector Schema placeholder
 * Source code : SAPUMESchema.java
 * Author :Chellappan Sampath
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sap;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfoUtil;
import org.identityconnectors.framework.common.objects.EmbeddedObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;


public final class UserSchema {
	
	private static final Log LOG = Log.getLog(UserSchema.class);
	private static Schema _schema;
	private static Map<String, AttributeInfo> _accountAttributeMap;	
	private static Map<String, AttributeInfo> _roleAttributeMap;
	private static Map<String, AttributeInfo> _paramAttributeMap;
	private static Map<String, AttributeInfo> _profAttributeMap;
	//private static Map<String, AttributeInfo> _groupAttributeMap;
	//private static Set<String> _groupAttributeNames;
	private static Set<String> _accountAttributeNames;
	private static Set<String> _roleAttributeNames;
	private static Set<String> _paramAttributeNames;	
	private static Set<String> _profAttributeNames;
	

	/**
	 * private constructor, not used, everything is static
	 */
	public UserSchema(){
	}

	public Map<String, AttributeInfo> getAccountAttributeMap() {
		return _accountAttributeMap;
	}

	public Map<String, AttributeInfo> getRoleAttributeMap() {
		return _roleAttributeMap;
	}
	
	public Map<String, AttributeInfo> getParamAttributeMap() {
		return _paramAttributeMap;
	}
	
	public Map<String, AttributeInfo> getProfileAttributeMap() {
		return _profAttributeMap;
	}

	public Set<String> getAccountAttributeNames() {
		return _accountAttributeNames;
	}

	public Set<String> getRoleAttributeNames() {
		return _roleAttributeNames;
	}
	
	public Set<String> getProfileAttributeNames() {
		return _profAttributeNames;
	}
	
	public Set<String> getParamAttributeNames() {
		return _paramAttributeNames;
	}

	public Schema getSchema() {
		initSchema();
		return _schema;
	}

	private void initSchema() {


		  final SchemaBuilder schemaBuilder = new SchemaBuilder(SAPConnector.class);
		  
	        Set<AttributeInfo> acctAttributes = new HashSet<AttributeInfo>();	        
	        
	        // ACCOUNT ATTRIBUTES  	          
	        
	        		//OPERATIONAL ATTRIBUTES
	        acctAttributes.add(AttributeInfoBuilder.build(Name.NAME, String.class, EnumSet.of(Flags.REQUIRED)));  //required  
	        acctAttributes.add(AttributeInfoBuilder.build(OperationalAttributes.PASSWORD_NAME,GuardedString.class, EnumSet.of(Flags.NOT_READABLE,Flags.NOT_RETURNED_BY_DEFAULT, Flags.REQUIRED)));
	        acctAttributes.add(AttributeInfoBuilder.build(OperationalAttributes.CURRENT_PASSWORD_NAME,GuardedString.class, EnumSet.of(Flags.NOT_READABLE,Flags.NOT_RETURNED_BY_DEFAULT )));
	        acctAttributes.add(AttributeInfoBuilder.build(OperationalAttributes.PASSWORD_EXPIRED_NAME,boolean.class, EnumSet.of(Flags.NOT_RETURNED_BY_DEFAULT)));
	        acctAttributes.add(OperationalAttributeInfos.ENABLE);	        	        
	        acctAttributes.add(OperationalAttributeInfos.LOCK_OUT);
	        
	      //  acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ACCOUNT_LOCKED, String.class));	        
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.PERSONNEL_NUMBER, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_CUA_SYSTEMS, String.class, EnumSet.of(Flags.MULTIVALUED)));
	        
	        		// ADDRESS ATTRIBUTES
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_LASTNAME, String.class, EnumSet.of(Flags.REQUIRED)));  //required
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_FIRSTNAME, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_E_MAIL, String.class));	       
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_TITLE_P, String.class));	       
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_FLOOR, String.class));	        
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_ROOM_NUMBER, String.class));      
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_LANGUAGE_KEY_P, String.class)); // TODO: iso639 seems to set language key P	         
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_DEPARTMENT, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_FUNCTION, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_BUILDING, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_COMM_TYPE, String.class));	        
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_TELEPHONE_NUMBER, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_TELEPHONE_EXT, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_FAX_NUMBER, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_FAX_EXTENSION, String.class));	     
	        
	        		//LOGONDATA ATTRIBUTES
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_ACCOUNTING_NUMBER, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_GROUPS, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_VALID_FROM, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_VALID_THRO, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_TIME_ZONE, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_USER_TYPE, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.LOGONDATA_LTIME, String.class, EnumSet.of(Flags.NOT_CREATABLE, Flags.NOT_UPDATEABLE)));
	        
	        		//DEFAULT DATA ATTRIBUTES
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_COST_CENTER, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_DECIMAL_NOTATION, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_DATE_FORMAT, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_LOGON_LANGUAGE, String.class));
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_START_MENU, String.class));
	        
	        		// ALIAS ATTRIBUTE
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_ALIAS, String.class));
	        
	        		// COMPANY ATTRIBUTE
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_COMPANY, String.class));
	        
	        		// UCLASS ATTRIBUTE
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.ATTR_CONTRACTUAL_USER_TYPE, String.class)); 
	        
	        		// GROUP ATTRIBUTE
	        acctAttributes.add(AttributeInfoBuilder.build(SAPConstants.USERGROUP, String.class, EnumSet.of(Flags.MULTIVALUED)));
	            
	        
	        
	        //ROLE ATTRIBUTES
	        HashSet<AttributeInfo> roleAttr = new HashSet<AttributeInfo>();
	    	
	        roleAttr.add(AttributeInfoBuilder.build(SAPConstants.ATTR_ACTIVITY_GROUPS, String.class, EnumSet.of(Flags.REQUIRED)));
	        roleAttr.add(AttributeInfoBuilder.build(SAPConstants.SUBSYSTEM, String.class));
	        roleAttr.add(AttributeInfoBuilder.build(SAPConstants.AGR_FROM_DAT, String.class));
	        roleAttr.add(AttributeInfoBuilder.build(SAPConstants.AGR_TO_DAT, String.class));
	        roleAttr.add(AttributeInfoBuilder.build(SAPConstants.AGR_TEXT, String.class));
			roleAttr.add(AttributeInfoBuilder.build(SAPConstants.AGR_ORG_FLAG, String.class));
	        
	        _roleAttributeMap = AttributeInfoUtil.toMap(roleAttr);
	        _roleAttributeNames=_roleAttributeMap.keySet();
 
	        //Role Object and its Info Builder		
			ObjectClassInfoBuilder ocRoleBuilder = new ObjectClassInfoBuilder();
			ocRoleBuilder.addAllAttributeInfo(roleAttr);
			ocRoleBuilder.setType("ACTIVITYGROUPS");	
			ocRoleBuilder.setEmbedded(true);
			ObjectClassInfo roleInfo = ocRoleBuilder.build();
			schemaBuilder.defineObjectClass(roleInfo);
			
		    //Adding Role object as an attribute in Account Object		
			AttributeInfoBuilder roleInfoBuilder = new AttributeInfoBuilder();
        	roleInfoBuilder.setName(SAPConstants.ATTR_ROLES_EMBEDED);
        	roleInfoBuilder.setObjectClassName("ACTIVITYGROUPS");
        	roleInfoBuilder.setType(EmbeddedObject.class);
        	roleInfoBuilder.setFlags(EnumSet.of(Flags.MULTIVALUED));
        	AttributeInfo roleAttrInfo = roleInfoBuilder.build();        	
        	acctAttributes.add(roleAttrInfo);
        	
        	//PROFILE ATTRIBUTES
        	HashSet<AttributeInfo> profileAttr = new HashSet<AttributeInfo>(); 	    	
        	profileAttr.add(AttributeInfoBuilder.build(SAPConstants.ATTR_PROFILES, String.class, EnumSet.of(Flags.REQUIRED)));
        	profileAttr.add(AttributeInfoBuilder.build(SAPConstants.SUBSYSTEM, String.class));
        	profileAttr.add(AttributeInfoBuilder.build(SAPConstants.PROF_TEXT, String.class));
        	profileAttr.add(AttributeInfoBuilder.build(SAPConstants.PROF_TYPE, String.class));
        	profileAttr.add(AttributeInfoBuilder.build(SAPConstants.PROF_AKTPS,String.class));
        	
        	_profAttributeMap = AttributeInfoUtil.toMap(profileAttr);
        	_profAttributeNames=_profAttributeMap.keySet();
        	
        	//Profile Object and its Info Builder
 		
 			ObjectClassInfoBuilder profBuilder = new ObjectClassInfoBuilder();
 			profBuilder.addAllAttributeInfo(profileAttr);
 			profBuilder.setType("PROFILES");	
 			profBuilder.setEmbedded(true);
 			ObjectClassInfo profInfo = profBuilder.build();
 			schemaBuilder.defineObjectClass(profInfo);
 			
 			//Adding profile object as an attribute in Account Object	 
 			
 			AttributeInfoBuilder profAttrBuilder = new AttributeInfoBuilder();
         	profAttrBuilder.setName(SAPConstants.ATTR_PROFILES_EMBEDED);
         	profAttrBuilder.setObjectClassName("PROFILES");
         	profAttrBuilder.setType(EmbeddedObject.class);
         	profAttrBuilder.setFlags(EnumSet.of(Flags.MULTIVALUED));
         	AttributeInfo profAttrInfo = profAttrBuilder.build();        	
         	acctAttributes.add(profAttrInfo);
         	
        	//PARAMETER ATTRIBUTES
         	
         	HashSet<AttributeInfo> paramAttr = new HashSet<AttributeInfo>(); 	    	
         	paramAttr.add(AttributeInfoBuilder.build(SAPConstants.PARID, String.class,  EnumSet.of(Flags.REQUIRED)));
         	paramAttr.add(AttributeInfoBuilder.build(SAPConstants.PARVA, String.class));
         	paramAttr.add(AttributeInfoBuilder.build(SAPConstants.PARTXT, String.class));
         	
         	 _paramAttributeMap = AttributeInfoUtil.toMap(paramAttr);
         	 _paramAttributeNames=_paramAttributeMap.keySet();
        	
         	//Parameter Object and its Info Builder
         	 
 			ObjectClassInfoBuilder paramBuilder = new ObjectClassInfoBuilder();
 			paramBuilder.addAllAttributeInfo(paramAttr);
 			paramBuilder.setType("PARAMETER");	
 			paramBuilder.setEmbedded(true);
 			ObjectClassInfo paramInfo = paramBuilder.build();
 			schemaBuilder.defineObjectClass(paramInfo);
 			
 			//Adding parameter object as an attribute in Account Object	
 			
 			AttributeInfoBuilder paramAttrBuilder = new AttributeInfoBuilder(); 		
 			paramAttrBuilder.setName(SAPConstants.ATTR_PARAMETERS_EMBEDED);
 			paramAttrBuilder.setObjectClassName("PARAMETER");
 			paramAttrBuilder.setType(EmbeddedObject.class);
 			paramAttrBuilder.setFlags(EnumSet.of(Flags.MULTIVALUED));
         	AttributeInfo paramAttrInfo = paramAttrBuilder.build();        	
         	acctAttributes.add(paramAttrInfo);           	
         	
         	//USERGROUP ATTRIBUTES
         	
         	
         	/*HashSet<AttributeInfo> groupAttr = new HashSet<AttributeInfo>(); 	    	
         	groupAttr.add(AttributeInfoBuilder.build(SAPConstants.USERGROUP, String.class, EnumSet.of(Flags.REQUIRED)));
         	
         	 _groupAttributeMap = AttributeInfoUtil.toMap(groupAttr);
         	 _groupAttributeNames= _groupAttributeMap.keySet();
         	       	
 		     //userGroup Object and its Info Builder
         	 
 			ObjectClassInfoBuilder groupBuilder = new ObjectClassInfoBuilder();
 			groupBuilder.addAllAttributeInfo(groupAttr);
 			groupBuilder.setType("GROUPS");	
 			groupBuilder.setEmbedded(true);
 			ObjectClassInfo groupInfo = groupBuilder.build();
 			schemaBuilder.defineObjectClass(groupInfo);
 			
 			//Adding userGroup object as an attribute in Account Object	
 			
 			AttributeInfoBuilder groupAttrBuilder = new AttributeInfoBuilder(); 			
 			groupAttrBuilder.setName(SAPConstants.ATTR_GROUPS_EMBEDED);
 			groupAttrBuilder.setObjectClassName("GROUPS");
 			groupAttrBuilder.setType(EmbeddedObject.class);
 			groupAttrBuilder.setFlags(EnumSet.of(Flags.MULTIVALUED));
         	AttributeInfo groupAttrInfo = groupAttrBuilder.build();        	
         	acctAttributes.add(groupAttrInfo);  */
         	
	        _accountAttributeMap = AttributeInfoUtil.toMap(acctAttributes);
	        _accountAttributeNames = _accountAttributeMap.keySet();
	        
	        //Account Object and its Info Builder
	        
	        ObjectClassInfoBuilder objcBuilder = new ObjectClassInfoBuilder();
	        objcBuilder.setType(ObjectClass.ACCOUNT_NAME);	       
	        objcBuilder.addAllAttributeInfo(acctAttributes);
	        ObjectClassInfo oci = objcBuilder.build();
	        schemaBuilder.defineObjectClass(oci);
	
	        schemaBuilder.clearSupportedObjectClassesByOperation();
	        schemaBuilder.addSupportedObjectClass(SchemaOp.class, oci);
	        schemaBuilder.addSupportedObjectClass(CreateOp.class, oci);
	        schemaBuilder.addSupportedObjectClass(SearchOp.class, oci);
	        schemaBuilder.addSupportedObjectClass(SyncOp.class, oci);
	        schemaBuilder.addSupportedObjectClass(TestOp.class, oci);
	        schemaBuilder.addSupportedObjectClass(UpdateAttributeValuesOp.class, oci);
	        schemaBuilder.addSupportedObjectClass(DeleteOp.class, oci);
	        schemaBuilder.addSupportedObjectClass(TestOp.class, roleInfo);
	        schemaBuilder.addSupportedObjectClass(TestOp.class, profInfo);
	        schemaBuilder.addSupportedObjectClass(TestOp.class, paramInfo);
    
	        _schema = schemaBuilder.build();
	        
	        LOG.info("RETURN");
	}
}

