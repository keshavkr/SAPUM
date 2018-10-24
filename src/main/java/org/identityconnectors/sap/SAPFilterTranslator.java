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

import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsIgnoreCaseFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;

/**
 * This is an implementation of AbstractFilterTranslator that gives a concrete representation
 * of which filters can be applied at the connector level (natively). If the 
 * SAP doesn't support a certain expression type, that factory
 * method should return null. This level of filtering is present only to allow any
 * native contructs that may be available to help reduce the result set for the framework,
 * which will (strictly) reapply all filters specified after the connector does the initial
 * filtering.<p><p>Note: The generic query type is most commonly a String, but does not have to be.
 * 
 * @author bfarrell
 * @version 1.0
 * @since 1.0
 */
public class SAPFilterTranslator extends AbstractFilterTranslator<String> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createEqualsExpression(EqualsFilter filter, boolean not) {
    	// filter enhancement -  Bug 18998725 - start
    	if (!not) {
        	return filter.getName() + "=" + 
        			AttributeUtil.getAsStringValue(filter.getAttribute());
        } else {
        	return null;
        }
    	// filter enhancement -  Bug 18998725 - end
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String createAndExpression(String leftExpression, String rightExpression) {
        // sap does not support AND but we can use one of them
        if (leftExpression!=null && rightExpression!=null)          
            return leftExpression + " & " + rightExpression;
        else
            return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String createOrExpression(String leftExpression, String rightExpression) {
        // sap does not support AND but we can use one of them
        if (leftExpression!=null && rightExpression!=null ){
            return leftExpression + " | " + rightExpression;
        } else {
            return null;
        }
    }
      
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String createGreaterThanExpression(GreaterThanFilter filter, boolean not) {        
        return filter.getName()+ ">" +AttributeUtil.getAsStringValue(filter.getAttribute());
   
	}
    
    /**
     * Added for OPAM support
     * Bug 18998725
     */
    @Override
    protected String createContainsIgnoreCaseExpression(ContainsIgnoreCaseFilter filter, boolean not) {
    	StringBuffer strbuffer = new StringBuffer();
    	//Updated for Bug 19610570-filter translator
    	if (!not && 
    			(filter.getAttribute().is(Name.NAME) || filter.getAttribute().is(Uid.NAME))){
    		strbuffer.append(Name.NAME);
    		strbuffer.append("=[");
    		strbuffer.append(AttributeUtil.getAsStringValue(filter.getAttribute()));
        } else {
			return null;
        }
    	return strbuffer.toString().toUpperCase();
    }
    
    /**
     * Added for Bug 19610570-filter translator
     * It supports Contains filter
     * This method implemented to support only __NAME__ attribute
     */
    @Override
    protected String createContainsExpression(ContainsFilter filter, boolean not){
    	StringBuffer strbuffer = new StringBuffer();
    	if (!not && filter.getAttribute().is(Name.NAME)){
    		strbuffer.append(filter.getName());
    		strbuffer.append("=[");
    		strbuffer.append(AttributeUtil.getAsStringValue(filter.getAttribute()));
    	} else {
			return null;
        }
    	return strbuffer.toString().toUpperCase();
    }
    
    /**
     * Added for Bug 19610570-filter translator
     * It supports ContainsAllValues filter
     * This method implemented to support only __NAME__ attribute
     */
    @Override
    protected String createContainsAllValuesExpression(ContainsAllValuesFilter filter, boolean not){
    	StringBuffer strbuffer = new StringBuffer();
    	if (!not && filter.getAttribute().is(Name.NAME)){
    		strbuffer.append(filter.getName());
    		strbuffer.append("=[");
    		strbuffer.append(AttributeUtil.getAsStringValue(filter.getAttribute()));
    	} else {
			return null;
        }
    	return strbuffer.toString().toUpperCase();
    }
    
    /**
     * Added for Bug 19610570-filter translator
     * It supports StartsWith filter
     * This method implemented to support only __NAME__ attribute
     */
    @Override
    protected String createStartsWithExpression(StartsWithFilter filter, boolean not){
    	StringBuffer strbuffer = new StringBuffer();
    	if (!not && filter.getAttribute().is(Name.NAME)){
    		strbuffer.append(filter.getName());
    		strbuffer.append("=[");
    		strbuffer.append(AttributeUtil.getAsStringValue(filter.getAttribute()));
    	} else {
			return null;
        }
    	return strbuffer.toString().toUpperCase();
    }
    
    /**
     * Added for Bug 19610570-filter translator
     * It supports EndsWith filter
     * This method implemented to support only __NAME__ attribute
     */
    @Override
    protected String createEndsWithExpression(EndsWithFilter filter, boolean not){
    	StringBuffer strbuffer = new StringBuffer();
    	if (!not && filter.getAttribute().is(Name.NAME)){
    		strbuffer.append(filter.getName());
    		strbuffer.append("=[");
    		strbuffer.append(AttributeUtil.getAsStringValue(filter.getAttribute()));
    	} else {
			return null;
        }
    	return strbuffer.toString().toUpperCase();
    }

}
