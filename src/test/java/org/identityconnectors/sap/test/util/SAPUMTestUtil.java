/**
 * 
 */
package org.identityconnectors.sap.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Method;


import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.EmbeddedObject;
import org.identityconnectors.framework.common.objects.EmbeddedObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.sap.SAPConfiguration;
import org.identityconnectors.sap.SAPConnector;
import org.identityconnectors.test.common.PropertyBag;
import org.identityconnectors.test.common.TestHelpers;

/**
 * @author Ranjith.Kumar
 *
 */
public class SAPUMTestUtil {	
	
	private static final Log LOGGER = Log.getLog(SAPUMTestUtil.class);
	private PropertyBag prop = null;
	public static final String ROLE="role";
	public static final String PROFILE="profile";
	public static final String PARAMETER="parameter";
	public static final String GROUP="group";
	public static final String callParseBoolean="enablecua;overwritelink;validatepernr;reconcilefuturedatedroles;reconcilepastdatedroles";
	public static final String skipMethods="connectormessages;filteredaccounts;tableformats;dsr;temppassword;cuachildpasswordcheckfuncmodule;cuachildpasswordcheckdelay;eatnonupdatecreate;usesaptemppwd;returnsaptemporarypwdsonfailure;uppercasepwd";
	
	
	public SAPConfiguration newConfiguration() {
		
		LOGGER.info("BEGIN");
		String methodName=null;
		String firstchar=null;
		String property=null;
		String propertyName=null;
		Class[] paramType=null;
		
		SAPConfiguration config = new SAPConfiguration();
		PropertyBag properties = TestHelpers.getProperties(SAPConnector.class);	
		Method[] methods = config.getClass().getMethods();

		try {
			for(Method method : methods){
				if(method.getName().startsWith("set")){					
					  methodName=new String(method.getName().substring(3));		
				}else{
					continue;
				}
				firstchar= methodName.toLowerCase().substring(0, 1);
				property = firstchar.concat(methodName.substring(1));				
				propertyName="connector."+property;				
				paramType=method.getParameterTypes();
				
			   if(paramType[0].equals(int.class)){				   
				   if(!(skipMethods.contains(property.toLowerCase()))){
					   method.invoke(config,properties.getProperty(propertyName, Integer.class));
				   }				   
			   } else if(paramType[0].equals(boolean.class)){				   
				   if(callParseBoolean.contains(property.toLowerCase())){
					   method.invoke(config,Boolean.parseBoolean(properties.getStringProperty(propertyName)));
				   }else if(!(skipMethods.contains(property.toLowerCase()))){					 
					   method.invoke(config,properties.getProperty(propertyName, Boolean.class));
				   }
				  
			   }else{
				   if (property.equalsIgnoreCase("password")||property.equalsIgnoreCase("dummypassword")){					   
					   method.invoke(config,new GuardedString(properties.getStringProperty(propertyName).toCharArray()));
				   }else if(!(skipMethods.contains(property.toLowerCase()))){
					   method.invoke(config,properties.getStringProperty(propertyName));
				   } 
			   }
				
			}		
			
		} catch (Exception e) {
			System.out.println("newConfiguration : " + e.getMessage());
			e.printStackTrace();
			LOGGER.error("failed @ newConfiguration: ", e.getMessage());
		} finally{
			LOGGER.info("END");
		}
		return config;
	}
	
	public Set<Attribute> toAttributeSet(int dataCase, List<String> attrToGet) {
		LOGGER.info("BEGIN");
		Set<Attribute> ret = new HashSet<Attribute>();
		Map<String, Object> map = toMap(dataCase, attrToGet);
		try{
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				if (entry.getValue() instanceof Collection) {
					ret.add(AttributeBuilder.build(entry.getKey(),
							(Collection) entry.getValue()));
				} else if (entry.getValue() instanceof GuardedString) {
					GuardedString newPassword = (GuardedString) entry.getValue();
					//String sPassword = decode(newPassword);
	
					ret.add(AttributeBuilder.build(entry.getKey(), newPassword));
				} else {
					ret.add(AttributeBuilder.build(entry.getKey(), (String) entry
							.getValue()));
				}
			}
		}finally{
			LOGGER.info("END");
		}
		return ret;
	}

	/**
	 * 
	 * @param dataCase
	 * @param attrToGet
	 * @return
	 */
	private Map<String, Object> toMap(int dataCase, List<String> attrToGet) {
		LOGGER.info("BEGIN");
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (prop == null){
				prop = TestHelpers.getProperties(SAPConnector.class);
			}
			
			for (String attrName : attrToGet) {
				String sAttr = "account"+dataCase+"."+attrName;
				try {
					if (prop.getStringProperty(sAttr) != null) {
						if (sAttr.contains("__PASSWORD__")) {
							GuardedString gspwd = new GuardedString(prop
									.getStringProperty(sAttr).toCharArray());
							map.put(attrName, gspwd);
						} else {
							map.put(attrName, prop.getStringProperty(sAttr));
						}
						System.out.println("Name : " + attrName + " - Value : "
								+ prop.getStringProperty(sAttr));
					}
				} catch (Exception e) {	}
			}
		} catch (Exception e) {
			System.out.println("toMap : " + e.getMessage());
		} finally{
			LOGGER.info("END");
		}
		return map;
	}
	
	/**
     * 
      * @param dataCase
     * @param attrs
     * @return
     */
     public Attribute getChildTableAttr(int dataCase, List<String> attrs, String childTableName) {
            LOGGER.info("BEGIN");
            Attribute childTableAttr = null;
            Collection<EmbeddedObject> childDataColl = new ArrayList<EmbeddedObject>();
            EmbeddedObjectBuilder childDataObjBldr = null;                    
            Boolean isRecordExist = false;
            String childDataKeyName = null;
            String childDataObjClassName = null;
            try {
                   if (prop == null){
                         prop = TestHelpers.getProperties(SAPConnector.class);
                   }
                   
                   if(SAPUMTestUtil.ROLE.equals(childTableName)){
                         childDataObjClassName="ACTIVITYGROUPS";
                         childDataKeyName ="roles" ;
                   } else if(SAPUMTestUtil.PROFILE.equals(childTableName)){
                         childDataObjClassName="PROFILES";
                         childDataKeyName = "profiles";
                   } else if(SAPUMTestUtil.PARAMETER.equals(childTableName)){
                         childDataObjClassName="PARAMETER1";
                         childDataKeyName = "parameters";
                   } else if(SAPUMTestUtil.GROUP.equals(childTableName)){
                         childDataObjClassName="GROUPS";
                         childDataKeyName ="groups" ;
                   }
                   
                   for(int i=1;i<=2;i++){
                         childDataObjBldr = new EmbeddedObjectBuilder();                       
                         isRecordExist = false;
                         for (String attrName : attrs) {
                               
                                childDataObjBldr.setObjectClass(new ObjectClass(childDataObjClassName));                              
                                String sAttr = "account"+dataCase+"."+childTableName+i+"."+attrName;
                                try {
                                       if (prop.getStringProperty(sAttr) != null) {
                                              childDataObjBldr.addAttribute(AttributeBuilder.build(attrName, 
                                                            prop.getStringProperty(sAttr)));
                                              isRecordExist = true;
                                       } else{
                                              break;
                                       }
                                } catch (Exception e) {    
                                       LOGGER.warn("getChildTableAttr():"+e);
                                }
                         }
                         if(isRecordExist){
                                childDataColl.add(childDataObjBldr.build());
                         }
                   }
                   if(childDataColl.isEmpty()) {
                         LOGGER.error("No child data found in data file");
                   } else{
                         childTableAttr = AttributeBuilder.build(childDataKeyName, childDataColl);
                   }                    
            } catch (Exception e) {
                   LOGGER.error("getChildTableAttr() : " + e.getMessage());
            } finally{
                   LOGGER.info("END");
            }
     return childTableAttr;
     }

		
	/**
	 * 
	 * @param string
	 * @return
	 */
	public String decode(final GuardedString string) {
		LOGGER.info("BEGIN");
		if (string == null) {
			return null;
		}
		GuardedStringAccessor accessor = new GuardedStringAccessor();
		string.access(accessor);
		String decoded = new String(accessor.getArray());
		accessor.clear();
		LOGGER.info("END");
		return decoded;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getReconQuery(int dataCase){
		LOGGER.info("BEGIN");
		String queryString = null;
		try{
			PropertyBag prop = TestHelpers.getProperties(SAPConnector.class);
			String sAttr = "account"+dataCase+".query";
			queryString = prop.getStringProperty(sAttr);
			if (queryString != null) {
				return queryString;
			}
			return queryString;
		} finally {
			LOGGER.info("END");
		}
	}
}
