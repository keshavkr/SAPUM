package org.identityconnectors.sap.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.sap.SAPConfiguration;
import org.identityconnectors.sap.test.util.SAPUMTestUtil;
import org.junit.Assert;
import org.junit.Test;

public class SAPUMConfigurationTests{
	
	@Test
	public void defaults() {		
		
		SAPConfiguration config = new SAPConfiguration();
			
		Assert.assertNull(config.getUser());
		Assert.assertNull(config.getHost());	
		Assert.assertNull(config.getPassword());
		Assert.assertNull(config.getdummyPassword());			
		Assert.assertNull(config.getDestination());
		Assert.assertNull(config.getmasterSystem());		
		Assert.assertNull(config.getchangePasswordAtNextLogon());		
		
		Assert.assertNull(config.getJcoGroup());
		Assert.assertNull(config.getJcoSAPRouter());
		Assert.assertNull(config.getJcoTraceDir());
		Assert.assertNotNull(config.getJcoTrace());
		
		Assert.assertNull(config.getGatewayHost());
		Assert.assertNull(config.getGatewayService());
		
		Assert.assertNull(config.getDsr());
		Assert.assertNull(config.getMsHost());
		Assert.assertNull(config.getMsServ());	
		Assert.assertNull(config.getAliasUser());
		Assert.assertNull(config.getType());		
		
		Assert.assertNull(config.getSncLib());
		Assert.assertNull(config.getSncName());
		Assert.assertNull(config.getSncPartnerName());
		Assert.assertNull(config.getSncProtectionLevel());
		Assert.assertNull(config.getSncX509Cert());			
		
		Assert.assertNotNull(config.getSystemNumber());		
		Assert.assertNotNull( config.getBatchSize());
		Assert.assertNotNull(config.getClient());
		Assert.assertNotNull(config.getLanguage());		
		Assert.assertNotNull(config.getFilteredAccounts());
		Assert.assertNotNull(config.getMaxBAPIRetries());
		Assert.assertNotNull(config.getRetryWaitTime());
		
		Assert.assertEquals(new String("10"), config.getBatchSize());
		Assert.assertEquals(new String("00"), config.getSystemNumber());
		Assert.assertEquals(new String("5"), Integer.toString(config.getMaxBAPIRetries()));
		Assert.assertEquals(new String("EN"), config.getLanguage());
		Assert.assertEquals(new String("000"), config.getClient());
		Assert.assertEquals(new String("500"), Integer.toString(config.getRetryWaitTime()));
		
		Assert.assertNotNull(config.getConnectionMaxGetTime());
		Assert.assertNotNull(config.getConnectionPoolActiveLimit());
		Assert.assertNotNull(config.getConnectionPoolCapacity());
		Assert.assertNotNull(config.getConnectionPoolExpirationPeriod());
		Assert.assertNotNull(config.getConnectionPoolExpirationTime());		
		
		Assert.assertFalse(config.getUseSNC());
		Assert.assertFalse(config.getEnableCUA());
		Assert.assertFalse(config.getUseLoadBalance());		
		Assert.assertFalse(config.getUpperCasePwd());
		Assert.assertFalse(config.getUseSAPTempPwd());		
				
	}
	
	@Test
	public void setters() {

		// Init config parameters
		
		SAPUMTestUtil configUtil = new SAPUMTestUtil();
		SAPConfiguration config = configUtil.newConfiguration();	
				
		Assert.assertNotNull(config.getUser());
		Assert.assertNotNull(config.getHost());	
		Assert.assertNotNull(config.getPassword());
		Assert.assertNotNull(config.getdummyPassword());			
		Assert.assertNotNull(config.getDestination());
		Assert.assertNotNull(config.getmasterSystem());		
		Assert.assertNotNull(config.getchangePasswordAtNextLogon());		
		
		Assert.assertNotNull(config.getJcoGroup());
		Assert.assertNotNull(config.getJcoSAPRouter());
		Assert.assertNotNull(config.getJcoTraceDir());
		Assert.assertNotNull(config.getJcoTrace());		
		Assert.assertNotNull(config.getGatewayHost());
		Assert.assertNotNull(config.getGatewayService());		
		Assert.assertNotNull(config.getMsHost());
		Assert.assertNotNull(config.getMsServ());	
		Assert.assertNotNull(config.getAliasUser());
		Assert.assertNotNull(config.getType());		
		
		Assert.assertNotNull(config.getSncLib());
		Assert.assertNotNull(config.getSncName());
		Assert.assertNotNull(config.getSncPartnerName());
		Assert.assertNotNull(config.getSncProtectionLevel());
		Assert.assertNotNull(config.getSncX509Cert());	
		
		config.setBatchSize("20");
		config.setSystemNumber("20");
		config.setMaxBAPIRetries(20);
		config.setLanguage("DE");
		config.setClient("600");
		config.setRetryWaitTime(100);		
		
		Assert.assertEquals(new String("20"), config.getBatchSize());
		Assert.assertEquals(new String("20"), config.getSystemNumber());
		Assert.assertEquals(new String("20"), Integer.toString(config.getMaxBAPIRetries()));
		Assert.assertEquals(new String("DE"), config.getLanguage());
		Assert.assertEquals(new String("600"), config.getClient());
		Assert.assertEquals(new String("100"),Integer.toString(config.getRetryWaitTime()));
		
		config.setConnectionMaxGetTime(20);
		config.setConnectionPoolActiveLimit(20);
		config.setConnectionPoolCapacity(20);
		config.setConnectionPoolExpirationPeriod(20);
		config.setConnectionPoolExpirationTime(20);
		config.setConnectionPoolExpirationTime(20);
		
		Assert.assertEquals(new String("20"),Integer.toString(config.getConnectionMaxGetTime()));
		Assert.assertEquals(new String("20"),Integer.toString(config.getConnectionPoolActiveLimit()));
		Assert.assertEquals(new String("20"),Integer.toString(config.getConnectionPoolCapacity()));
		Assert.assertEquals(new String("20"),Integer.toString(config.getConnectionPoolExpirationPeriod()));
		Assert.assertEquals(new String("20"),Integer.toString(config.getConnectionPoolExpirationTime()));		
	}
	
	
	@Test
	public void testConfigurationalPropsAnnotations() {	
	
		SAPConfiguration cfg = new SAPConfiguration();	
		Method[] md = cfg.getClass().getMethods();
		int gettersCounter = 0;
		
		Collection<String> confidentialmethodSet = Arrays.asList("getPassword","getdummyPassword","getTempPassword");
		Collection<String> requiredMethodSet =  Arrays.asList("getDestination","getClient");
		Collection<String> objclassesmethodSet = Arrays.asList("getGroups","getRoles","getProfiles","getParameters","getFilteredAccounts","getSingleRoles","getCompositeRoles");
		
		for (Method method : md) {
			try {
				
				 Annotation[] annotations = method.getDeclaredAnnotations();
				
				
				for (Annotation annotation : annotations) {
					
					if (annotation instanceof ConfigurationProperty) {
						ConfigurationProperty myAnnotation = (ConfigurationProperty) annotation;
						gettersCounter++;
						// check for Object Classes
						if (objclassesmethodSet.contains(method.getName()))
							Assert.assertEquals(myAnnotation.objectClasses().length, 1);
						else
							Assert.assertEquals(myAnnotation.objectClasses().length, 0);
						// check for Confidential Tags
						if (confidentialmethodSet.contains(method.getName()))
							Assert.assertTrue(myAnnotation.confidential());
						else
							Assert.assertFalse(myAnnotation.confidential());

						// Check for Required Tags
						if (requiredMethodSet.contains(method.getName()))
							Assert.assertTrue(myAnnotation.required());
						else
							Assert.assertFalse(myAnnotation.required());
					}
				}
				
				
			} catch (AssertionError ae) {
				throw new AssertionError(ae + " : Error while asserting  "+ method.getName());
			}
			
		}
		//check for getter methods counts
		Assert.assertTrue(gettersCounter == 72);
	}	
}