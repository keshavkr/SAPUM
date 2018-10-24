package org.identityconnectors.sap.test;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.sap.SAPConfiguration;
import org.identityconnectors.sap.test.util.ICFTestHelper;
import org.identityconnectors.sap.test.util.SAPUMTestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;




public class SAPUMConnectionTests  {
	
	
	private static SAPUMTestUtil configUtil=null;	
	private static SAPConfiguration _config=null;		
	private static final Log LOGGER = Log.getLog(SAPUMSchemaTest.class);
	
	
	@Before
	public void setup()
	{
		configUtil =  new SAPUMTestUtil();
		
	}
	
	@After
	public void tearDown()
	{
		configUtil=null;
		_config=null;			
	}	
	
	/*@Test
	public void testHealthyConnection() {
		
		connector = new SAPConnector();
		connector.init(_config);		
		Assert.assertNotNull(connector.getConfiguration());		
		try {
			connector.checkAlive();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} finally {
			connector.dispose();
		}
	

	}
	
	// Null Pointer Exception
	
	@Test(expected=ConnectorException.class)
	
		public void test_Check_InvalidConnection(){
		
		connector = new SAPConnector();
		connector.init(_config);		
		connector.dispose();		
		connector.checkAlive();	
		
	}	
	
	@Test(expected=ConnectorException.class)
	
	public void test_InvalidTestConnection(){
	
	connector = new SAPConnector();	
	connector.init(_config);		
	connector.dispose();		
	connector.test();	
	
}	*/
	
	
	@Test
	public void testTestMethod(){		
		
		ICFTestHelper.getInstance().newFacade().test();
	}
		
	//ConnectorException- Testing for Invalid/wrong Connection Parameter
	
	@Test(expected=NullPointerException.class)
	public void testNullPassword(){
		
		_config = configUtil.newConfiguration();
		_config.setPassword(null);
		ICFTestHelper.getInstance().newFacade(_config).test();
		
	}
	
	@Test(expected=ConnectorException.class)
	public void testInvalidHost(){
		
		_config = configUtil.newConfiguration();		
		_config.setHost("10.20.30.40");
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	@Test(expected=ConnectorException.class)
	public void testInvalidUser(){
		
		_config = configUtil.newConfiguration();
		_config.setUser("abcd");
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	@Test(expected=ConnectorException.class)
	public void testInvalidClient(){
		_config = configUtil.newConfiguration();
		_config.setClient("43");	
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	@Test(expected=ConnectorException.class)
	public void testInvalidLanguage(){
		_config = configUtil.newConfiguration();
		_config.setLanguage("ss");
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	@Test(expected=ConnectorException.class)
	public void testInvalidSystemNumber(){
		_config = configUtil.newConfiguration();
		_config.setSystemNumber("22");
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
		
	//IllegalArgumentException- Testing for a null Connection parameter
	
	@Test(expected=IllegalArgumentException.class)
	public void testNullhost(){
		_config = configUtil.newConfiguration();
		_config.setHost(null);
		ICFTestHelper.getInstance().newFacade(_config).test();		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testnullDestination(){
		_config = configUtil.newConfiguration();
		_config.setDestination(null);
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
		
	@Test(expected=IllegalArgumentException.class)
	public void testnullClient(){
		_config = configUtil.newConfiguration();
		_config.setClient(null);
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
		
	
	@Test(expected=IllegalArgumentException.class)
	public void testnullLanguage(){
		_config = configUtil.newConfiguration();
		_config.setLanguage(null);
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testnullUser(){
		_config = configUtil.newConfiguration();
		_config.setUser(null);
		ICFTestHelper.getInstance().newFacade(_config).test();
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testnullSystemNumber(){
		_config = configUtil.newConfiguration();
		_config.setSystemNumber(null);
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	
	@Test
	public void testNullDummyPassword(){
		_config = configUtil.newConfiguration();
		_config.setdummyPassword(new GuardedString("".toCharArray()));
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	
	@Test
	public void testnullMasterSystem(){
		_config = configUtil.newConfiguration();
		_config.setmasterSystem(null);
		ICFTestHelper.getInstance().newFacade(_config).test();
	}
	
	@Test
	 public void testnullTopologyname(){
		 _config = configUtil.newConfiguration();
		 _config.setTpName(null);
		 ICFTestHelper.getInstance().newFacade(_config).test();
	 }
	
	
	// Testing Load balance parameters 
	
	@Test(expected=IllegalArgumentException.class)
	 public void testLoadBalance(){
		 _config = configUtil.newConfiguration();
		 _config.setUseLoadBalance(true);
		 Assert.assertNotNull(_config.getR3Name());
		 Assert.assertNotNull(_config.getMsHost());
		 Assert.assertNotNull(_config.getMsServ());
		 Assert.assertNotNull(_config.getJcoGroup());		
		 ICFTestHelper.getInstance().newFacade(_config).test();
	 }
	
	// Testing SNC (Secure Network Connection) parameters 
	
	@Test(expected=IllegalArgumentException.class)
	 public void testSNC(){
		_config = configUtil.newConfiguration();
		_config.setUseSNC(true);
		Assert.assertNotNull(_config.getSncLib());
		Assert.assertNotNull(_config.getSncName());
		Assert.assertNotNull(_config.getSncPartnerName());
		Assert.assertNotNull(_config.getSncProtectionLevel());
		Assert.assertNotNull(_config.getSncX509Cert());
		ICFTestHelper.getInstance().newFacade(_config).test();
	 }
	
	
}
