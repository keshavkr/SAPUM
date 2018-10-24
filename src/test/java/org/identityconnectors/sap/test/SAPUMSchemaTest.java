package org.identityconnectors.sap.test;


import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;
import org.identityconnectors.framework.common.objects.AttributeInfoUtil;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.sap.SAPConstants;
import org.identityconnectors.sap.test.util.ICFTestHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SAPUMSchemaTest {

	
	private static Schema schema;
	private static final Log LOGGER = Log.getLog(SAPUMSchemaTest.class);
	private static ObjectClassInfo uoci,proci,paoci,agoci;//,goci;	
	private static Set<AttributeInfo> accattrInfos = new HashSet<AttributeInfo>();
	private static Set<AttributeInfo> parmattrInfos = new HashSet<AttributeInfo>();
	private static Set<AttributeInfo> roleattrInfos = new HashSet<AttributeInfo>();
	//private static Set<AttributeInfo> grpattrInfos = new HashSet<AttributeInfo>();
	private static Set<AttributeInfo> profattrInfos = new HashSet<AttributeInfo>();
	
	
	@Before
	public void setUp() throws Exception {
		
		
		// Fetching the Schema from loaded Bundle
		
		schema=ICFTestHelper.getInstance().getFacade().schema();
		
		// Fetching ObjectClassInfo from the schema
				
		uoci = schema.findObjectClassInfo("__ACCOUNT__");
		proci = schema.findObjectClassInfo("PROFILES");
		//goci = schema.findObjectClassInfo("GROUPS");
		agoci = schema.findObjectClassInfo("ACTIVITYGROUPS");
		paoci = schema.findObjectClassInfo("PARAMETER");	
	
		
		// Fetching AttributeInfo from the ObjectClassInfo
		
		accattrInfos=uoci.getAttributeInfo();
		//grpattrInfos=goci.getAttributeInfo();
		profattrInfos=proci.getAttributeInfo();
		parmattrInfos=paoci.getAttributeInfo();
		roleattrInfos=agoci.getAttributeInfo();
		
		}
	
	@After
	public void tearDown() {
		
		uoci=null;		
		//goci=null;
		agoci=null;
		paoci=null;
		proci=null;
		
		accattrInfos=null;
		//grpattrInfos=null;
		profattrInfos=null;
		parmattrInfos=null;
		roleattrInfos=null;
		
		schema=null;
	}
	
	
	@Test
	public void testSchema_Attributes() throws Exception {
		
		LOGGER.info("BEGIN");
		
		Set<ObjectClassInfo> objClassInfo = new HashSet<ObjectClassInfo>();		
		objClassInfo.addAll(schema.getObjectClassInfo());		
		Assert.assertNotNull(objClassInfo);	
		
		Assert.assertTrue(objClassInfo.size() == 4);
		LOGGER.info("OBJECTCLASS COUNT:"+objClassInfo.size());		
								
		Assert.assertTrue(proci.isEmbedded());		
		displayLogs(proci.getType(), "Embedded", proci.isEmbedded());		
		
		Assert.assertTrue(agoci.isEmbedded());
		displayLogs(agoci.getType(), "Embedded", agoci.isEmbedded());
		
		Assert.assertTrue(paoci.isEmbedded());
		displayLogs(paoci.getType(), "Embedded", paoci.isEmbedded());
		
		/*Assert.assertTrue(goci.isEmbedded());	
		displayLogs(goci.getType(), "Embedded", goci.isEmbedded());*/
		
		LOGGER.info("END");
			
	}
	
	

	@Test
	public void testRequiredAttrs(){		
		
		Set<AttributeInfo> requiredAttrInfo = new HashSet<AttributeInfo>();	
		
		
		// required attributes test for ACCOUNT ObjectClass
		requiredAttrInfo.add(AttributeInfoUtil.find(SAPConstants.ATTR_LASTNAME,accattrInfos));
		requiredAttrInfo.add(AttributeInfoUtil.find("__NAME__",accattrInfos));		
		assertRequiredAttrs(requiredAttrInfo, uoci);		
		requiredAttrInfo.clear();	
		
		// required attributes test for USERGROUPS ObjectClass	
		//requiredAttrInfo.add(AttributeInfoUtil.find(SAPConstants.GROUPS,grpattrInfos));
		//requiredAttrInfo.add(AttributeInfoUtil.find("__NAME__",grpattrInfos));
		//assertRequiredAttrs(requiredAttrInfo, goci);
		//requiredAttrInfo.clear();
		
		// required attributes test for PROFILES ObjectClass	
	    requiredAttrInfo.add(AttributeInfoUtil.find(SAPConstants.ATTR_PROFILES,profattrInfos));
	    requiredAttrInfo.add(AttributeInfoUtil.find("__NAME__",profattrInfos));
	    assertRequiredAttrs(requiredAttrInfo, proci);
		requiredAttrInfo.clear();
			
		// required attributes test for PARAMETERS ObjectClass		
		requiredAttrInfo.add(AttributeInfoUtil.find(SAPConstants.PARID,parmattrInfos));
		requiredAttrInfo.add(AttributeInfoUtil.find("__NAME__",parmattrInfos));
		assertRequiredAttrs(requiredAttrInfo, paoci);			
		requiredAttrInfo.clear();			
			
		// required attributes test for ROLES ObjectClass		
		requiredAttrInfo.add(AttributeInfoUtil.find(SAPConstants.ATTR_ACTIVITY_GROUPS,roleattrInfos));
		requiredAttrInfo.add(AttributeInfoUtil.find("__NAME__",roleattrInfos));
		assertRequiredAttrs(requiredAttrInfo, agoci);
		requiredAttrInfo.clear();
		LOGGER.info("END");
	}
	
public void assertRequiredAttrs(Set<AttributeInfo> requiredAttrInfo, ObjectClassInfo oci){
		
		for(AttributeInfo attr : requiredAttrInfo) {
			try{
				Assert.assertTrue(attr.isRequired());
			}catch(AssertionError ae){
				LOGGER.info("Error while asserting {0}", attr.getName());
				throw new AssertionError(ae + "while asserting "+ attr.getName());
			}
			displayLogs(oci.getType(), attr.getName(), Flags.REQUIRED);
		}
	
	}
	
	@Test
	public void testMultiValuedAttrs(){
		
		LOGGER.info("BEGIN");
		AttributeInfo multiValuedAttrinfo;
		
		/*AttributeInfo multiValuedAttrinfo = AttributeInfoUtil.find(SAPConstants.ATTR_GROUPS_EMBEDED, accattrInfos);
		assertMultivaluedAttrs(multiValuedAttrinfo,SAPConstants.ATTR_GROUPS_EMBEDED);*/
				
		multiValuedAttrinfo = AttributeInfoUtil.find(SAPConstants.ATTR_PARAMETERS_EMBEDED, accattrInfos);
		assertMultivaluedAttrs(multiValuedAttrinfo,SAPConstants.ATTR_PARAMETERS_EMBEDED);
		
		multiValuedAttrinfo = AttributeInfoUtil.find(SAPConstants.ATTR_ROLES_EMBEDED, accattrInfos);
		assertMultivaluedAttrs(multiValuedAttrinfo,SAPConstants.ATTR_ROLES_EMBEDED);
		
		multiValuedAttrinfo = AttributeInfoUtil.find(SAPConstants.ATTR_PROFILES_EMBEDED, accattrInfos);
		assertMultivaluedAttrs(multiValuedAttrinfo,SAPConstants.ATTR_PROFILES_EMBEDED);
		
		multiValuedAttrinfo = AttributeInfoUtil.find(SAPConstants.ATTR_CUA_SYSTEMS, accattrInfos);
		assertMultivaluedAttrs(multiValuedAttrinfo,SAPConstants.ATTR_CUA_SYSTEMS);
		
				
		LOGGER.info("END");	
	} 

	public void assertMultivaluedAttrs(AttributeInfo multiValuedAttrinfo,  String attrName){
		try{
		Assert.assertTrue(multiValuedAttrinfo.isMultiValued());
		}catch(AssertionError ae){
			LOGGER.info("Error while asserting {0}", multiValuedAttrinfo.getName());
			throw new AssertionError(ae + "while asserting "+ multiValuedAttrinfo.getName());
		}
		displayLogs(uoci.getType(), attrName, Flags.MULTIVALUED);
	}
	
	
	@Test
	public void testNotReturnedByDefaultAttrAttrs(){
		
		LOGGER.info("BEGIN");
		
		Set<AttributeInfo> nonDefaultReturnAttrInfoSet = new HashSet<AttributeInfo>();
		
		nonDefaultReturnAttrInfoSet.add(AttributeInfoUtil.find(OperationalAttributeInfos.PASSWORD.getName(), accattrInfos));
		nonDefaultReturnAttrInfoSet.add(AttributeInfoUtil.find(OperationalAttributeInfos.CURRENT_PASSWORD.getName(), accattrInfos));
		
		for(AttributeInfo attr : nonDefaultReturnAttrInfoSet) {
			try{
				Assert.assertFalse(attr.isReturnedByDefault());
			}catch(AssertionError ae){
				LOGGER.info("Error while asserting {0}", attr.getName());
				throw new AssertionError(ae + "while asserting "+ attr.getName());
			}
			displayLogs(uoci.getType(),	attr.getName(),	Flags.NOT_RETURNED_BY_DEFAULT);
		}
		LOGGER.info("END");	
	}
	
	
	@Test
	public void testNonReadableAttrs(){
		LOGGER.info("BEGIN");
		Set<AttributeInfo> nonreadableAttr = new HashSet<AttributeInfo>();
		
		nonreadableAttr.add(AttributeInfoUtil.find(OperationalAttributeInfos.PASSWORD.getName(), accattrInfos));
		nonreadableAttr.add(AttributeInfoUtil.find(OperationalAttributeInfos.CURRENT_PASSWORD.getName(), accattrInfos));
		
		for(AttributeInfo attr : nonreadableAttr) {
			try{
			Assert.assertFalse(attr.isReadable());
			}catch(AssertionError ae){
				LOGGER.info("Error while asserting {0}", attr.getName());
				throw new AssertionError(ae + "while asserting "+ attr.getName());			
			}
			displayLogs(uoci.getType(),	attr.getName(),	Flags.NOT_READABLE);
		}
		LOGGER.info("END");	
   }

	private void displayLogs( String ObjClassName, String ObjClassType, boolean isType ){
		LOGGER.info("OBJECTCLASS: {0} SUCCESSFULLY CHECKED FOR TYPE: {1} - Result: {2} ", ObjClassName, ObjClassType, isType);
	}
	
	private void displayLogs(String objclassName , String attrName,Flags flag){
		LOGGER.info("OBJECTCLASS: {0} SUCCESSFULLY CHECKED {1} AS {2} ATTRIBUTE", objclassName, attrName, flag);
	}
	
}
