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

import java.util.Map;
import java.util.Set;

import org.identityconnectors.framework.common.objects.AttributeInfo;

/**
 * Abstract SAP operation class.
 * 
 * @author bfarrell
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractSAPOperation {
    protected SAPConnection _connection;
    protected SAPConfiguration _configuration;
    protected Set<String> _filteredAccounts;
    protected Map<String, AttributeInfo> _accountAttributes;
    protected  Map<String, String> _tableFormats;
   public  AbstractSAPOperation(SAPConnection conn, SAPConfiguration config, 
                         Map<String, AttributeInfo> accountAttributes,Set<String> filteredAccounts,Map<String, String> tableFormats) {
        _connection = conn;
        _configuration = config;
        _accountAttributes = accountAttributes;
        _tableFormats = tableFormats;
        _filteredAccounts = filteredAccounts;
    }
    public AbstractSAPOperation() {
		// TODO Auto-generated constructor stub
	}
    public void setConfiguration(SAPConfiguration config) {
        _configuration = config;
    }
    
}

