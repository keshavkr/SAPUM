package org.identityconnectors.sap.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.sap.UserSchema;
import org.identityconnectors.sap.test.util.ICFTestHelper;
import org.identityconnectors.sap.test.util.SAPUMTestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SAPUMSyncUserTest extends TestCase{	
	
	private static final Log LOGGER = Log.getLog(SAPUMSchemaTest.class);
	List<String> attrToGet = new ArrayList<String>();
	List<String> roleAttrs = new ArrayList<String>();
	List<String> profileAttrs = new ArrayList<String>();
	List<String> parameterAttrs = new ArrayList<String>();
	List<String> groupAttrs = new ArrayList<String>();
	boolean delete = false;
	
	private static ConnectorFacade facade=null;	
	private static SAPUMTestUtil configUtil =  new SAPUMTestUtil();
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
    public void testLatestSyncToken() {
		
		LOGGER.info("BEGIN");
        // run the test only if sync is supported by the tested object class

            Uid uid1 = null;
            Uid uid2 = null;
            try {
                // create one new object
    			Set<Attribute> attrSet1 = configUtil.toAttributeSet(29, attrToGet);
    			assertNotNull(attrSet1);
    			OperationOptionsBuilder Builder = new OperationOptionsBuilder();
    			
    			uid1 = facade.create(ObjectClass.ACCOUNT, attrSet1, Builder.build());
                assertNotNull(uid1);                
                
                // get latest sync token
        		SyncToken latestToken = getLatestSyncToken();
                
                // sync with latest sync token, should return nothing
                final LinkedList<SyncDelta> deltas = new LinkedList<SyncDelta>();
                facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                    public boolean handle(SyncDelta delta) {
                        deltas.add(delta);
                        return true;
                    }
                }, null);
               
                
                final String MSG1 = "Sync with previously retrieved latest sync token should not return any deltas, but returned: %d.";
                assertTrue(String.format(MSG1, deltas.size()), deltas.size() == 0);
                
                // create another object
    			Set<Attribute> attrSet2 = configUtil.toAttributeSet(30, attrToGet);
    			assertNotNull(attrSet2);
    			
    			uid2 = facade.create(ObjectClass.ACCOUNT, attrSet2, Builder.build());
                assertNotNull(uid2);
                                
                // sync with latest sync token, should return nothing
                facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                    public boolean handle(SyncDelta delta) {
                        deltas.add(delta);
                        return true;
                    }
                }, null);
                
                ConnectorObject obj = deltas.get(0).getObject();
                //Test return by default attributes in CO
                checkAttributes(obj);
                
                final String MSG2 = "Sync with latest sync token retrieved before one create should return one sync delta, but returned: %d";
                assertTrue(String.format(MSG2, deltas.size()), deltas.size() == 1);
                
            } finally {
                // cleanup
            	
                facade.delete(ObjectClass.ACCOUNT, uid1, null);
                facade.delete(ObjectClass.ACCOUNT, uid2, null);
            }
    		LOGGER.info("END");
    }
	@Test
    public void testSyncWithoutAttrsToGet() {
		
		LOGGER.info("BEGIN");
        // run the test only if sync is supported and also object class is
        // supported and connector can sync CREATEs

            Uid uid = null;
            try {
            	
        		SyncToken latestToken = getLatestSyncToken();

                // create record
    			Set<Attribute> attrSet = configUtil.toAttributeSet(31, attrToGet);
    			assertNotNull(attrSet);
                uid = facade.create(ObjectClass.ACCOUNT, attrSet, null);
                assertNotNull("Create returned null uid.", uid);

                 final LinkedList<SyncDelta> deltasCreate = new LinkedList<SyncDelta>();
                facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                    public boolean handle(SyncDelta delta) {
                    	deltasCreate.add(delta);
                        return true;
                    }
                }, null);

                // check that returned one delta
                final String MSG = "Sync should have returned one sync delta after creation of one object, but returned: %d";
                assertTrue(String.format(MSG, deltasCreate.size()), deltasCreate.size() == 1);
                
                ConnectorObject obj = deltasCreate.get(0).getObject();
                //Test return by default attributes in CO
                checkAttributes(obj);

            } finally {
                // cleanup
                facade.delete(ObjectClass.ACCOUNT, uid, null);
            }
    		LOGGER.info("END");
    } 
	
	@Test
	public void testSync() {
		
		LOGGER.info("BEGIN");
        Uid uid = null;
        SyncToken token = null;
        String msg = null;
        

        try {
        	
    		SyncToken latestToken = getLatestSyncToken();

            /* CREATE: */
           // create record
			Set<Attribute> attrSet = configUtil.toAttributeSet(32, attrToGet);
            uid = facade.create(ObjectClass.ACCOUNT, attrSet, null);
            assertNotNull("Create returned null uid.", uid);
            
           // sync after create
            final LinkedList<SyncDelta> deltasCreate = new LinkedList<SyncDelta>();
            	facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                public boolean handle(SyncDelta delta) {
                	deltasCreate.add(delta);
                    return true;
                }
            }, null);

                // check that returned one delta
                msg = "Sync should have returned one sync delta after creation of one object, but returned: %d";
                assertTrue(String.format(msg, deltasCreate.size()), deltasCreate.size() == 1);
                token = deltasCreate.get(0).getToken();
            
                /* UPDATE: */

                Set<Attribute> replaceAttributes = configUtil.toAttributeSet(33, attrToGet);

                // update only in case there is something to update
                if (replaceAttributes.size() > 0) {
                 //   replaceAttributes.add(uid);

                    assertTrue("no update attributes were found", (replaceAttributes.size() > 0));
                    
        		OperationOptionsBuilder builder = new OperationOptionsBuilder();
        	    Uid newUid = facade.update(ObjectClass.ACCOUNT, uid, replaceAttributes,builder.build());
                    // Update change of Uid must be propagated to
                    // replaceAttributes
                  if (!newUid.equals(uid)) {
                       replaceAttributes.remove(uid);
                       replaceAttributes.add(newUid);
                       uid = newUid;
                    }

                    // sync after update
                    final LinkedList<SyncDelta> deltasUpdate = new LinkedList<SyncDelta>();
                        facade.sync(ObjectClass.ACCOUNT, token, new SyncResultsHandler() {
                        public boolean handle(SyncDelta delta) {
                        	deltasUpdate.add(delta);
                        return true;
                        }
                    }, null);

                    // check that returned one delta
                    msg = "Sync should have returned one sync delta after update of one object, but returned: %d";
                    assertTrue(String.format(msg, deltasUpdate.size()), deltasUpdate.size() == 1);

                    token = deltasUpdate.get(0).getToken();
                }

            /* DELETE: */

                // delete object
                facade.delete(ObjectClass.ACCOUNT, uid, null);
                boolean isDelete= true;

                // sync after delete
                final LinkedList<SyncDelta> deltasDelete = new LinkedList<SyncDelta>();
                facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                    public boolean handle(SyncDelta delta) {
                    	deltasDelete.add(delta);
                        return true;
                    }
                }, null);

                // check that returned one delta
                msg = "Sync should have returned one sync delta after delete of one object, but returned: %d";
                assertTrue(String.format(msg, deltasDelete.size()), deltasDelete.size() == 1);
                
                ConnectorObject obj = deltasDelete.get(0).getObject();
                checkAttributes(obj);

                delete=isDelete;
	}      
          finally {
                	if (!delete)
					// cleanup test data
                    facade.delete(ObjectClass.ACCOUNT, uid, null);

        
    }    		
        LOGGER.info("END");   
    		}
	
	@Test
	public void testContract() {
		
		LOGGER.info("BEGIN");
        // initial number of objects to be created
        final int recordCount = 3;
        
        List<Uid> uids = new ArrayList<Uid>();
        List<Set<Attribute>> attrs = new ArrayList<Set<Attribute>>();

        // variable for assert messages
        String msg = null;
        
        try {                   
            /* SyncApiOp - start synchronizing from now */
            // start synchronizing from now
    		SyncToken latestToken = getLatestSyncToken();
            

            /* CreateApiOp - create initial objects */
            for (int i = 0; i < recordCount; i++) {
    			OperationOptionsBuilder Builder = new OperationOptionsBuilder();
    			Set<Attribute> attrSet1 = configUtil.toAttributeSet(i+25, attrToGet);
    			assertNotNull(attrSet1);
    			Uid uid = facade.create(ObjectClass.ACCOUNT, attrSet1, Builder.build());
                assertNotNull(uid);
                attrs.add(attrSet1);
                uids.add(uid);
            }            

            /* SyncApiOp - check sync of created objects */
            // sync after create
            final LinkedList<SyncDelta> deltasCreate = new LinkedList<SyncDelta>();
            facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                public boolean handle(SyncDelta delta) {
                	deltasCreate.add(delta);
                    return true;
                }
            }, null);

                    msg = "Sync after %d creates returned %d deltas.";
                    assertTrue(String.format(msg, recordCount, deltasCreate.size()),
                    		deltasCreate.size() == recordCount); 

                    /* DeleteApiOp - delete one object */
            Uid deleteUid = uids.remove(0);
            attrs.remove(0);
            
            // delete it and check that it was really deleted
            facade.delete(ObjectClass.ACCOUNT, deleteUid, null);
            
            latestToken = deltasCreate.get(recordCount - 1).getToken();

            /* UpdateApiOp - update one object */
            Uid updateUid = null;
                updateUid = uids.remove(0);
                attrs.remove(0);
                Set<Attribute> replaceAttributes = configUtil.toAttributeSet(6, attrToGet);
                
                // update only in case there is something to update
                if (replaceAttributes.size() > 0) {                    
                    // Uid must be present in attributes
            		OperationOptionsBuilder builder = new OperationOptionsBuilder();
            	    Uid newUid = facade.update(ObjectClass.ACCOUNT, updateUid, replaceAttributes,builder.build());
                    replaceAttributes.remove(updateUid);
                    
                    if (!updateUid.equals(newUid)) {
                        updateUid = newUid;
                    }
                    
                    attrs.add(replaceAttributes);
                    uids.add(updateUid);
                }               
                        
            /* SyncApiOp - sync after one delete and one possible update */
                final LinkedList<SyncDelta> deltasUpdate = new LinkedList<SyncDelta>();
                facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                    public boolean handle(SyncDelta delta) {
                    	deltasUpdate.add(delta);
                        return true;
                    }
                }, null);
                    // one deleted, one updated (if existed attributes to
                    // update)
                    assertTrue("Sync returned unexpected number of deltas. Exptected: max 2, but returned: "
                                    + deltasUpdate.size(), ((deltasUpdate.size() <= 2) && (deltasUpdate.size() > 0)));                        
                        
                        /* CreateApiOp - create one last object */
            			OperationOptionsBuilder Builder = new OperationOptionsBuilder();
            			Set<Attribute> attrSet12 = configUtil.toAttributeSet(1, attrToGet);
            			Uid uid1 = facade.create(ObjectClass.ACCOUNT, attrSet12, Builder.build());
                        assertNotNull(uid1);
            attrs.add(attrSet12);
            assertNotNull("Create returned null Uid.", uid1);
            
            
            latestToken = deltasUpdate.get(0).getToken();

            /* DeleteApiOp - delete one object */
            boolean isDelete= true;
            deleteUid = uids.remove(0);
            attrs.remove(0);
            // delete it and check that it was really deleted
            facade.delete(ObjectClass.ACCOUNT, deleteUid, null);

            /* SyncApiOp - after create, delete */
            final LinkedList<SyncDelta> deltasDelete = new LinkedList<SyncDelta>();
            facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                public boolean handle(SyncDelta delta) {
                	deltasDelete.add(delta);
                    return true;
                }
            }, null);
                    // one deleted, one created
                    assertTrue("Sync returned unexpected number of deltas. Exptected: max 2, but returned: "
                                    + deltasDelete.size(), deltasDelete.size() <= 2);
                    
            latestToken = deltasDelete.get(1).getToken();   

                        /* DeleteApiOp - delete all objects */
            for (int i = 0; i < uids.size(); i++) {
                facade.delete(ObjectClass.ACCOUNT, uids.get(i), null);
            }

            /* SyncApiOp - all objects were deleted */
            final LinkedList<SyncDelta> deltasDeleteAll = new LinkedList<SyncDelta>();
            facade.sync(ObjectClass.ACCOUNT, latestToken, new SyncResultsHandler() {
                public boolean handle(SyncDelta delta) {
                	deltasDeleteAll.add(delta);
                    return true;
                }
            }, null);
                msg = "Sync returned unexpected number of deltas. Exptected: %d, but returned: %d";
                assertTrue(String.format(msg, uids.size(), deltasDeleteAll.size()), deltasDeleteAll.size() == uids
                        .size());
                
				delete=isDelete;
        } finally {           
            // cleanup
        	if (!delete){      		
            for (Uid deluid : uids) {
                try {
                    facade.delete(ObjectClass.ACCOUNT, deluid, null);
                } catch (Exception e) {
                    // ok
                }
            }
        }
		LOGGER.info("END");
        }
    }

	/*
	 * Check the existence of attributes with flag as "NON returned by default" in the Connector object during Sync
	 * 
	 * 
	 */
	
	public void checkAttributes(ConnectorObject obj) {
		
		Schema schema = ICFTestHelper.getInstance().getFacade().schema();
		ObjectClassInfo uoci = schema.findObjectClassInfo("__ACCOUNT__");
		Set<AttributeInfo> accattrInfos = uoci.getAttributeInfo();	

		for(Attribute attr :  obj.getAttributes()) {
			try{
			// skips check for UID, as it is automatically returned by default
			if (!attr.getName().equals(Uid.NAME)){
				Assert.assertTrue(AttributeInfoUtil.find(attr.getName(), accattrInfos).isReturnedByDefault());
			}
		}
			catch(AssertionError ae){
			     throw new AssertionError(ae + "while asserting "+ attr.getName());
			}	
		}
	
	}
	
	/*
	 * To get the latest token to do sync operation
	 * 
	 * 
	 */
	
	public SyncToken getLatestSyncToken(){
	String format = "yyyyMMddHHmmss";
	SimpleDateFormat formatter = new SimpleDateFormat(format);
	String formattedDate = formatter.format(new Date());
	SyncToken latestToken = new SyncToken(formattedDate);
	return latestToken;
	}
}
			
	
		
	




