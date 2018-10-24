/**
 * 
 */
package org.identityconnectors.sap.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import org.identityconnectors.sap.UserSchema;
import org.identityconnectors.sap.test.util.SAPUMTestUtil;
import org.identityconnectors.sap.test.util.ICFTestHelper;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Ranjith.Kumar
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SAPUMCreateUserTest {

	List<String> attrToGet = new ArrayList<String>();
	List<String> roleAttrs = new ArrayList<String>();
	List<String> profileAttrs = new ArrayList<String>();
	List<String> parameterAttrs = new ArrayList<String>();
	List<String> groupAttrs = new ArrayList<String>();
	
	
	private static ConnectorFacade facade=null;	
	private static SAPUMTestUtil configUtil =  new SAPUMTestUtil();
	private static final Log LOGGER = Log.getLog(SAPUMCreateUserTest.class);
	
	
	@Before
	public void setUp() throws Exception {
		
		//getting the facade object		
		 facade =ICFTestHelper.getInstance().getFacade();
		 
		//calling schema
		 facade.schema();
		
		// Get all account attributes from schema
		attrToGet.addAll(UserSchema.getAccountAttributeNames());				
		roleAttrs.addAll(UserSchema.getRoleAttributeNames());
		profileAttrs.addAll(UserSchema.getProfileAttributeNames());
		parameterAttrs.addAll(UserSchema.getParamAttributeNames());
		//groupAttrs.addAll(UserSchema.getGroupAttributeNames());			
		
	}

	@After
	public void tearDown() {
		
		facade=null;
		configUtil=null;
		attrToGet=null;
		roleAttrs=null;
		profileAttrs=null;
		parameterAttrs=null;
		groupAttrs=null;
		
	}
	
	@Test
	public void testCreate_User() throws Exception {// with mandatory data
		LOGGER.info("BEGIN");
		try{
			Uid uid = null;
			Set<Attribute> attrSet = configUtil.toAttributeSet(1, attrToGet);
			TestCase.assertNotNull(attrSet);
			OperationOptionsBuilder builder = new OperationOptionsBuilder();
			uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
			TestCase.assertNotNull(uid);
			LOGGER.info("testCreate_User: User '" + uid.getUidValue()	
					+ "' Created Successfully ");
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User", "Successful");
		} catch(Exception e){
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User", "Failed");
		} finally{
			LOGGER.info("END");
		}
	}
	
	
	@Test(expected=AlreadyExistsException.class)
	public void testCreate_User_AlreadyExist() throws Exception {
		LOGGER.info("BEGIN");
		try{
			Uid uid = null;
			Set<Attribute> attrSet = configUtil.toAttributeSet(1, attrToGet);
			TestCase.assertNotNull(attrSet);
			OperationOptionsBuilder builder = new OperationOptionsBuilder();
			uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
			TestCase.assertNotNull(uid);
			LOGGER.info("testCreate_Account: User '" + uid.getUidValue()	
					+ "' Created Successfully");
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_AlreadyExist", "Failed");
		}catch (Exception e){
			Assert.assertTrue(e.getMessage().contains("account with name"));
			LOGGER.error("testCreate_User_AlreadyExist"+e.getMessage());
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_AlreadyExist", "Successful");
			throw e;
		} finally{
			LOGGER.info("END");
		}
	}
	
	@Test(expected=ConnectorException.class)
	public void testCreate_User_MissingData() throws Exception {
		LOGGER.info("BEGIN");
		try{
			Uid uid = null;
			Set<Attribute> attrSet = configUtil.toAttributeSet(24, attrToGet);
			TestCase.assertNotNull(attrSet);
			OperationOptionsBuilder builder = new OperationOptionsBuilder();
			uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
			TestCase.assertNotNull(uid);
			LOGGER.info("testCreate_Account: User '" + uid.getUidValue()	
					+ "' Created Successfully ");
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_MissingData", "Failed");
		} catch (Exception e){
			Assert.assertTrue(e.getMessage().contains("Error creating user"));
			LOGGER.error("testCreate_User_MissingData"+ e.getMessage());
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_MissingData", "Successful");
			throw e;
		} finally{
			LOGGER.info("END");
		}
	}
	
	@Test
	public void testCreate_User_FilteredUser() throws Exception {
		LOGGER.info("BEGIN");
		try{
			Uid uid = null;
			Set<Attribute> attrSet = configUtil.toAttributeSet(2, attrToGet);
			TestCase.assertNotNull(attrSet);
			OperationOptionsBuilder builder = new OperationOptionsBuilder();
			uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
			TestCase.assertNotNull(uid);
			LOGGER.info("testCreate_Account: User '" + uid.getUidValue()	
					+ "' Created Successfully ");
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_FilteredUser", "Failed");
		} catch (Exception e){
			LOGGER.error("testCreate_User_FilteredUser"+e.getMessage());
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_FilteredUser", "Successful");
		} finally{
			LOGGER.info("END");
		}
	}
	
	@Test(expected=ConnectorException.class)
	public void testCreate_User_InvalidPassword() throws Exception {
		LOGGER.info("BEGIN");
		try{
			Uid uid = null;
			Set<Attribute> attrSet = configUtil.toAttributeSet(3, attrToGet);
			TestCase.assertNotNull(attrSet);
			OperationOptionsBuilder builder = new OperationOptionsBuilder();
			uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
			TestCase.assertNotNull(uid);
			LOGGER.info("testCreate_Account: User '" + uid.getUidValue()	
					+ "' Created Successfully ");
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_InvalidPassword", "Failed");
		} catch (Exception e){
			Assert.assertTrue(e.getMessage().contains("Error creating user"));
			Assert.assertTrue(e.getMessage().contains("Password is not long enough"));
			LOGGER.error("testCreate_User_InvalidPassword"+ e.getMessage());
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_InvalidPassword", "Successful");
			throw e;
		} finally{
			LOGGER.info("END");
		}
	}
	
	@Test
	public void testCreate_User_AllFieldData() throws Exception {
		LOGGER.info("BEGIN");
		try{
			Uid uid = null;
			Set<Attribute> attrSet = configUtil.toAttributeSet(4, attrToGet);
			List<String> groupList = new ArrayList<String>();
			groupList.add("OIM GROUP");
			groupList.add("ADMIN GROUP");
			attrSet.add(AttributeBuilder.build("USERGROUP;GROUPS;USERGROUP;GROUPS", groupList));
			TestCase.assertNotNull(attrSet);
			OperationOptionsBuilder builder = new OperationOptionsBuilder();		
			uid=facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
			TestCase.assertNotNull(uid);
			LOGGER.info("testCreate_User_AllFieldData: User '" + uid.getUidValue()	
					+ "' Created Successfully ");
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_AllFieldData", "Successful");
		} catch(Exception e){
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreate_User_AllFieldData", "Failed");
			throw e;
		} finally{
			LOGGER.info("END");
		}
	}

	/**
	 * Create user with 2 roles, profiles, parameters, and groups respectively.
	 * @throws Exception
	 */
	@Test
	public void testCreateUser_withAllChildData() throws Exception{
		Uid uid = null;
		Set<Attribute> attrSet = configUtil.toAttributeSet(19, attrToGet);
		List<String> groupList = new ArrayList<String>();
		groupList.add("OIM GROUP");
		groupList.add("ADMIN GROUP");
		attrSet.add(AttributeBuilder.build("USERGROUP;GROUPS;USERGROUP;GROUPS", groupList));
		TestCase.assertNotNull(attrSet);
		attrSet.add(configUtil.getChildTableAttr(19,roleAttrs, SAPUMTestUtil.ROLE));
		attrSet.add(configUtil.getChildTableAttr(19,profileAttrs, SAPUMTestUtil.PROFILE));
		attrSet.add(configUtil.getChildTableAttr(19,parameterAttrs, SAPUMTestUtil.PARAMETER));
		//attrSet.add(configUtil.getChildTableAttr(19,groupAttrs, SAPUMTestUtil.GROUP));
		
		OperationOptionsBuilder builder = new OperationOptionsBuilder();
		uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
		TestCase.assertNotNull(uid);
		LOGGER.info("testCreateUser_withAllChildData: User '" + uid.getUidValue()	
				+ "' Created Successfully ");
		LOGGER.info(">>>>>Result::{0}::{1}", "testCreateUser_withAllChildData", "Successful");
	}
	
	/**
	 * Create user with a role
	 * @throws Exception
	 */
	@Test
	public void testCreateUser_withOneRole() throws Exception{
		Uid uid = null;
		Set<Attribute> attrSet = configUtil.toAttributeSet(20, attrToGet);
		TestCase.assertNotNull(attrSet);
		attrSet.add(configUtil.getChildTableAttr(20,roleAttrs, SAPUMTestUtil.ROLE));
		OperationOptionsBuilder builder = new OperationOptionsBuilder();
		uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
		TestCase.assertNotNull(uid);
		LOGGER.info("testCreateUser_withOneRole: User '" + uid.getUidValue()	
				+ "' Created Successfully ");
		LOGGER.info(">>>>>Result::{0}::{1}", "testCreateUser_withOneRole", "Successful");
	}
	
	/**
	 * Create user with a profile
	 * @throws Exception
	 */
	@Test
	public void testCreateUser_withOneProfile() throws Exception{
		Uid uid = null;
		Set<Attribute> attrSet = configUtil.toAttributeSet(21, attrToGet);
		TestCase.assertNotNull(attrSet);
		attrSet.add(configUtil.getChildTableAttr(21,profileAttrs, SAPUMTestUtil.PROFILE));
		OperationOptionsBuilder builder = new OperationOptionsBuilder();
		uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
		TestCase.assertNotNull(uid);
		LOGGER.info("testCreateUser_withOneProfile: User '" + uid.getUidValue()	
				+ "' Created Successfully ");
		LOGGER.info(">>>>>Result::{0}::{1}", "testCreateUser_withOneProfile", "Successful");
	}
	
	/**
	 * Create user with a parameter
	 * @throws Exception
	 */
	@Test
	public void testCreateUser_withOneParameter() throws Exception{
		Uid uid = null;
		Set<Attribute> attrSet = configUtil.toAttributeSet(14, attrToGet);
		TestCase.assertNotNull(attrSet);
		attrSet.add(configUtil.getChildTableAttr(14,parameterAttrs, SAPUMTestUtil.PARAMETER));
		OperationOptionsBuilder builder = new OperationOptionsBuilder();
		uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());		
		TestCase.assertNotNull(uid);
		LOGGER.info("testCreateUser_withOneParameter: User '" + uid.getUidValue()	
				+ "' Created Successfully ");
		LOGGER.info(">>>>>Result::{0}::{1}", "testCreateUser_withOneParameter", "Successful");
	}	

	/**
	 * Create user with a group
	 * @throws Exception
	 */
	@Test
	public void testCreateUser_withOneGroup() throws Exception{
		Uid uid = null;
		Set<Attribute> attrSet = configUtil.toAttributeSet(23, attrToGet);
		List<String> groupList = new ArrayList<String>();
		groupList.add("OIM GROUP");
		groupList.add("ADMIN GROUP");
		attrSet.add(AttributeBuilder.build("USERGROUP;GROUPS;USERGROUP;GROUPS", groupList));		
		TestCase.assertNotNull(attrSet);
		//attrSet.add(configUtil.getChildTableAttr(23,groupAttrs, SAPUMTestUtil.GROUP));
		OperationOptionsBuilder builder = new OperationOptionsBuilder();
		uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
		TestCase.assertNotNull(uid);
		LOGGER.info("testCreateUser_withOneGroup: User '" + uid.getUidValue()	
				+ "' Created Successfully ");
		LOGGER.info(">>>>>Result::{0}::{1}", "testCreateUser_withOneGroup", "Successful");
	}
	
	
		/**
	 * Create user with a parameter
	 * @throws Exception
	 */
	@Test
	public void testCreateUser_withInvalidParameter() throws Exception{
		Uid uid = null;
		try{
		Set<Attribute> attrSet = configUtil.toAttributeSet(24, attrToGet);
		TestCase.assertNotNull(attrSet);
		attrSet.add(configUtil.getChildTableAttr(24,parameterAttrs, SAPUMTestUtil.PARAMETER));
		OperationOptionsBuilder builder = new OperationOptionsBuilder();
		uid = facade.create(ObjectClass.ACCOUNT, attrSet, builder.build());
		TestCase.assertNotNull(uid);
		LOGGER.info("testCreateUser_withOneParameter: User '" + uid.getUidValue()	
				+ "' Created Successfully ");
		}catch(Exception e){
			LOGGER.info(">>>>>Result::{0}::{1}", "testCreateUser_withInvalidParameter", "Successful");	
		}
		
	}
}
