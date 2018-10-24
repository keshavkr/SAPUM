/**
 * 
 */
package org.identityconnectors.sap.test.util;

import java.util.List;

import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ResultsHandler;

/**
 * @author Ranjith.Kumar
 *
 */
public class DummyResultHandler implements ResultsHandler {

    private int userCount = 0;
    private List UidList;
    
    
    public boolean handle(ConnectorObject userObj) {
        setUserCount(getUserCount() + 1);
        return true;
    }

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

	public List getUidList() {
		return UidList;
	}

	public void setUidList(List uidList) {
		UidList = uidList;
	}

}