/**
 * 
 */
package org.identityconnectors.sap.test;

import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.sap.UserSchema;
import org.identityconnectors.sap.test.util.DummyResultHandler;
import org.identityconnectors.sap.test.util.ICFTestHelper;
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
public class SAPUMFilterTranslatorTest {
	
	private static ConnectorFacade facade=null;	
	private static final Log LOGGER = Log.getLog(SAPUMCreateUserTest.class);
	List<String> attrToGet = new ArrayList<String>();
	
	@Before
	public void setUp() throws Exception {
		
		//getting the facade object		
		 facade =ICFTestHelper.getInstance().getFacade();
		 
		//calling schema
		 facade.schema();
		 
		 attrToGet.addAll(UserSchema.getAccountAttributeNames());
		 attrToGet.remove("LOGONDATA;LTIME");
		 attrToGet.remove("User Lock;NONE;NONE;NONE");
		 //to support query with "Last Updated"
		 attrToGet.add("Last Updated");
	}
	
	@After
	public void tearDown() {
		
		facade=null;
	}
	
	
	@Test
	public void testFilter_Contains_NAME() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST";
		Filter nameFilter = FilterBuilder.contains(AttributeBuilder.build(Name.NAME, name));
		System.out.println(nameFilter);
		DummyResultHandler resultHandler = new DummyResultHandler();
		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, nameFilter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_Contains_NAME failed ", usr.contains(name));
		}
		LOGGER.info("END");
	}
	
	@Test
	public void testFilter_Startswith_NAME() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST";
		Filter nameFilter = FilterBuilder.startsWith(AttributeBuilder.build(Name.NAME, name));
		System.out.println(nameFilter);
		DummyResultHandler resultHandler = new DummyResultHandler();
		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, nameFilter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_Startswith_NAME failed ", usr.contains(name));
		}
		LOGGER.info("END");
	}
	
	@Test
	public void testFilter_Endswith_NAME() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST";
		Filter nameFilter = FilterBuilder.endsWith(AttributeBuilder.build(Name.NAME, name));
		System.out.println(nameFilter);
		DummyResultHandler resultHandler = new DummyResultHandler();
		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, nameFilter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_Endswith_NAME failed ", usr.contains(name));
		}
		LOGGER.info("END");
	}
	
	@Test
	public void testFilter_containsAll_NAME() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST";
		Filter nameFilter = FilterBuilder.containsAllValues(AttributeBuilder.build(Name.NAME, name));
		System.out.println(nameFilter);
		DummyResultHandler resultHandler = new DummyResultHandler();
		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, nameFilter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_containsAll_NAME failed ", usr.contains(name));
		}
		LOGGER.info("END");
	}

	
	@Test
	public void testFilter_EqualTo_NAME() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST";
		Filter nameFilter = FilterBuilder.equalTo(AttributeBuilder.build(Name.NAME, name));
		System.out.println(nameFilter);
		DummyResultHandler resultHandler = new DummyResultHandler();
		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, nameFilter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_EqualTo_NAME failed ", usr.equals(name));
		}
		LOGGER.info("END");
	}
	
	
	@Test
	public void testFilter_Contains_NAME_AndOper() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST1";
		Filter filter = FilterBuilder.and(
				FilterBuilder.contains(AttributeBuilder.build(Name.NAME, name)), 
				FilterBuilder.equalTo(AttributeBuilder.build("LASTNAME;ADDRESS;LASTNAME;ADDRESSX", name)));
		
		DummyResultHandler resultHandler = new DummyResultHandler();

		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, filter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_Contains_NAME_AndOper failed ", usr.contains(name));
		}
		LOGGER.info("END");
	}

	@Test
	public void testFilter_EqualTo_MultipleAttr() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST1";
		
		Filter filter = 
				FilterBuilder.and(
						FilterBuilder.and(
								FilterBuilder.equalTo(AttributeBuilder.build("FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX", name)), 
								FilterBuilder.equalTo(AttributeBuilder.build("LASTNAME;ADDRESS;LASTNAME;ADDRESSX", name))), 
				FilterBuilder.equalTo(AttributeBuilder.build("E_MAIL;ADDRESS;E_MAIL;ADDRESSX", "test@test.com")));
		
		DummyResultHandler resultHandler = new DummyResultHandler();

		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, filter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		Assert.assertTrue("testFilter_EqualTo_MultipleAttr failed ", userList.size()==1);
		LOGGER.info("END");
	}
	
	
	@Test
	public void testFilter_containsIgnoreCase_NAME() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TEST";
		Filter nameFilter = FilterBuilder.containsIgnoreCase(AttributeBuilder.build(Name.NAME, name));
		System.out.println(nameFilter);
		DummyResultHandler resultHandler = new DummyResultHandler();
		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, nameFilter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_containsIgnoreCase_NAME failed ", usr.contains(name));
		}
		LOGGER.info("END");
	}
	
	
	@Test
	public void testFilter_containsIgnoreCase_NAME_AndOper() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TESTxyz";
		Filter filter = FilterBuilder.and(
				FilterBuilder.containsIgnoreCase(AttributeBuilder.build(Name.NAME, name)), 
				FilterBuilder.equalTo(AttributeBuilder.build("LASTNAME;ADDRESS;LASTNAME;ADDRESSX", name.toLowerCase())));
		
		DummyResultHandler resultHandler = new DummyResultHandler();

		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, filter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_containsIgnoreCase_NAME_AndOper failed ", usr.contains(name.toUpperCase()));
		}
		LOGGER.info("END");
	}
	
	@Test
	public void testFilter_containsIgnoreCase_NAME_AndOper_LastUpdated() throws Exception {
		LOGGER.info("BEGIN");
		String name = "TESTXYZ";
		Filter filter = FilterBuilder.and(FilterBuilder.and(
				FilterBuilder.containsIgnoreCase(AttributeBuilder.build(Name.NAME, name)), 
				FilterBuilder.equalTo(AttributeBuilder.build("LASTNAME;ADDRESS;LASTNAME;ADDRESSX", name.toLowerCase()))),
				FilterBuilder.greaterThan(AttributeBuilder.build("Last Updated", 20150125101010L)));
		
		DummyResultHandler resultHandler = new DummyResultHandler();

		OperationOptionsBuilder oopBuilder = new OperationOptionsBuilder();
		oopBuilder.setAttributesToGet(attrToGet);
		OperationOptions oops = oopBuilder.build();
		
		facade.search(ObjectClass.ACCOUNT, filter, resultHandler, oops);
		
		LOGGER.info("User Count:{0}",resultHandler.getUserCount());
		List<String> userList = resultHandler.getUidList();
		LOGGER.info("User List:{0}",userList);
		for (String usr : userList){
			Assert.assertTrue("testFilter_containsIgnoreCase_NAME_AndOper_LastUpdated failed ", usr.contains(name));
		}
		LOGGER.info("END");
	}
}
