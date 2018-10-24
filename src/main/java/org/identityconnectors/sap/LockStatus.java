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

import org.identityconnectors.sap.Function;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoStructure;

public class LockStatus {
    /**
     * Isolates the Lock Status of an SAP User.
     */
    // 0 (docs say this can also be 00) enabled
    // 32 disabled by CUA
    // 64 disabled by Administrator
    // 128 disabled by too many failed log in attempts
    // 192 disabled by too many failed log in attempts and by
    // administrator
    // it is possible, though I haven't see supporting docs, for
    // 32 + 128, 32 + 64, 32 + 64 + 128 values. rwagner

    // Lock status constants
    final static int NOT_LOCKED = 0 ;
    final static int LOCAL_LOCK = 32 ;
    final static int GLOBAL_LOCK = 64 ;
    final static int WRNG_LOGON_LOCK = 128 ;
    final static int NO_USER_PW_LOCK = 256 ;

    // used to store the lock status of the user
    private int lockStatus = 0;

    protected LockStatus() {

    }

    // used to set the lock status of the user
    protected LockStatus(int lockMask) {
        lockStatus = lockMask;
    }

    public LockStatus(Function function) throws JCoException {
        JCoStructure lockingData = function.getExportParameterList().getStructure( "ISLOCKED" );
        String localLock = lockingData.getString( "LOCAL_LOCK" );
        String globalLock = lockingData.getString( "GLOB_LOCK" );
        String noUserPwdLock = lockingData.getString( "NO_USER_PW" );
        String wrongLogonLock = lockingData.getString( "WRNG_LOGON" );

        lockStatus = ("L".equals(localLock) ? LockStatus.LOCAL_LOCK : 0) |
        ("L".equals(globalLock) ? LockStatus.GLOBAL_LOCK : 0) |
        ("L".equals(wrongLogonLock) ? LockStatus.WRNG_LOGON_LOCK : 0) |
        ("L".equals(noUserPwdLock) ? LockStatus.NO_USER_PW_LOCK : 0); 
    }

    public boolean isGlobalLock() {
        return (lockStatus & GLOBAL_LOCK) == GLOBAL_LOCK ;
    }

    public boolean isLocalLock() {
        return (lockStatus & LOCAL_LOCK) == LOCAL_LOCK;
    }

    public boolean isWrongLogonLock() {
        return (lockStatus & WRNG_LOGON_LOCK) == WRNG_LOGON_LOCK;
    }

    public boolean isNoPwdLock() {
        return (lockStatus & NO_USER_PW_LOCK) == NO_USER_PW_LOCK;
    }

    public boolean isDisabled() {
        return ((lockStatus & GLOBAL_LOCK ) | (lockStatus & LOCAL_LOCK)) != 0;
    }
	public int getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(int lockStatus) {
		this.lockStatus = lockStatus;
	}
}

