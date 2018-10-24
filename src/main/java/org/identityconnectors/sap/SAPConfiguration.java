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

import java.util.Arrays;
import java.util.Set;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

/**
 * Implements the {@link Configuration} interface to provide all the necessary
 * parameters to initialize the SAP Connector.
 * 
 * @author bfarrell
 * @version 1.0
 * @since 1.0
 * 
 */
public class SAPConfiguration extends SAPConnectConfiguration {
	/*
	 * Set up base configuration elements
	 */
	private String[] _filteredAccounts = { "DDIC", "SAP*", "SAPCPIC" };
	private boolean _enableCUA = false;
	private boolean _upperCasePassword = false;
	private boolean _useSAPTempPassword = false;
	private boolean _returnSAPTemporaryPasswordsOnFailure = false;
	private GuardedString _tempPassword = new GuardedString("Nr&v8tP(%s"
			.toCharArray());

	// this field stores the delimited string formats for profiles, activity
	// groups
	// and any other tables that users want to access from SAP
	// the following should be the minimum added to this:
	// "PROFILES:=N|:BAPIPROF",
	// "ACTIVITYGROUPS:=N|:AGR_NAME|:FROM_DAT|:TO_DAT|:ORG_FLAG"
	private String[] _tableFormats = {}; // OW Specific

	// SAP does not throw an error when you try to update certain non updateable
	// attributes
	// this flag turns on or off the connector eating the exception as well
	private boolean _eatNonUpdateCreate = false; // OW Specific

	private String _cuaChildInitialPasswordChangeFuncModule;
	private String _cuaChildPasswordCheckFuncModule;
	private int _cuaChildPasswordCheckDelay;
	private String _cuaChildPasswordChangeFuncModule;

	// private boolean changePassword = false; // OIM Specific
	private boolean overwriteLink = true; // OIM Specific
	//private boolean supportHRMS0105InfotypeLinking = false; // OIM Specific
	private boolean validatePERNR = false;// OIM Specific
	// private boolean isPasswordPropagateToChildSystem = false; // OIM Specific
	private boolean reconcilefuturedatedroles = true;
	private boolean reconcilepastdatedroles = true;
	private String batchSize = "10";// OW Specific
	private String sapSystemTimeZone;
	private String roles;
	private String profiles;
	private String parameters;
	private String groups;
	
	private String        _masterSystem;
    private String        _changePasswordAtNextLogon;
    private GuardedString        _dummyPassword;
   
    private String        _passwordPropagateToChildSystem;
    // Start:: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED
    private String        _disableLockStatus;
	// End:: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED

	/**
	 * Constructor
	 */
	public SAPConfiguration() {
		super();
	}

	public SAPConfiguration(SAPConnectConfiguration conf) {
		super(conf);
	}

	/**
	 * {@inheritDoc}
	 */
	public void validate() {
		super.validate();

		Set<String> filteredAcctSet = CollectionUtil.newSet(_filteredAccounts);
		filteredAcctSet.addAll(Arrays.asList(_filteredAccounts));
		if (filteredAcctSet.size() != _filteredAccounts.length) {
			throw new ConfigurationException(getMessage("SAP_ERR_LIST_DUPS",
					"filtered accounts"));
		}
		if (filteredAcctSet.contains(null)) {
			throw new NullPointerException(getMessage(
					"SAP_ERR_LIST_NULL_ENTRIES", "filtered accounts"));
		}

		Assertions.nullCheck(_enableCUA, "enableCUA");
		Assertions.nullCheck(_tempPassword, "tempPassword");
		Set<String> tableFormats = CollectionUtil.newSet(_tableFormats);
		filteredAcctSet.addAll(Arrays.asList(_tableFormats));
		if (tableFormats.size() != _tableFormats.length) {
			throw new ConfigurationException(getMessage("SAP_ERR_LIST_DUPS",
					"table formats"));
		}
		if (tableFormats.contains(null)) {
			throw new NullPointerException(getMessage(
					"SAP_ERR_LIST_NULL_ENTRIES", "table formats"));
		}

		for (String tableFormat : tableFormats) {
			String[] tableDef = tableFormat.split(":=");
			if (tableDef == null || (tableDef != null && tableDef.length != 2)) {
				String message = getMessage("SAP_ERR_TABLE_FORMAT", tableFormat);
				throw new ConnectorException(message);
			}
		}
	}

	/**
	 * @return the filteredAccounts
	 */
	@ConfigurationProperty(order = 47, displayMessageKey="SAP_FILTERED_ACCOUNTS_DISPLAY", helpMessageKey="SAP_FILTERED_ACCOUNTS_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String[] getFilteredAccounts() {
		return _filteredAccounts;
	}

	/**
	 * @param filteredAccounts
	 *            the filteredAccounts to set
	 */
	public void setFilteredAccounts(String[] filteredAccounts) {
		this._filteredAccounts = filteredAccounts;
	}

	/**
	 * @return the _enableCUA
	 */
	@ConfigurationProperty(order = 48, displayMessageKey="SAP_CUA_MODE_DISPLAY", helpMessageKey="SAP_CUA_MODE_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean getEnableCUA() {
		return _enableCUA;
	}

	/**
	 * @param enableCua
	 *            the _enableCUA to set
	 */
	public void setEnableCUA(boolean enableCua) {
		_enableCUA = enableCua;
	}

	/**
	 * Determines whether the connector should upper case passwords before using
	 * them.
	 * 
	 * @return the _upperCasePassword
	 */
	@ConfigurationProperty(order = 49, displayMessageKey="SAP_UPPER_CASE_PASSWORDS_DISPLAY", helpMessageKey="SAP_UPPER_CASE_PASSWORDS_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean getUpperCasePwd() {
		return _upperCasePassword;
	}

	/**
	 * Sets wheather the connector should upper case passwords before using them.
	 * 
	 * @param upperCasePassword
	 *            the upperCasePassword to set
	 */
	public void setUpperCasePwd(boolean upperCasePassword) {
		_upperCasePassword = upperCasePassword;
	}

	/**
	 * @return the _useSAPTempPassword
	 */
	@ConfigurationProperty(order = 50, displayMessageKey="SAP_USE_SAP_TEMP_PWD_DISPLAY", helpMessageKey="SAP_USE_SAP_TEMP_PWD_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean getUseSAPTempPwd() {
		return _useSAPTempPassword;
	}

	/**
	 * @param useTmpPassword
	 *            the useSAPTempPassword to set
	 */
	public void setUseSAPTempPwd(boolean useTmpPassword) {
		_useSAPTempPassword = useTmpPassword;
	}

	/**
	 * @return the _returnSAPTemporaryPasswordsOnFailure
	 */
	@ConfigurationProperty(order = 51, displayMessageKey="SAP_RETURN_SAP_TEMP_PWD_DISPLAY", helpMessageKey="SAP_RETURN_SAP_TEMP_PWD_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean getReturnSAPTemporaryPwdsOnFailure() {
		return _returnSAPTemporaryPasswordsOnFailure;
	}

	/**
	 * @param temporaryPasswordsOnFailure
	 *            the returnSAPTemporaryPasswordsOnFailure to set
	 */
	public void setReturnSAPTemporaryPwdsOnFailure(
			boolean temporaryPasswordsOnFailure) {
		_returnSAPTemporaryPasswordsOnFailure = temporaryPasswordsOnFailure;
	}

	/**
	 * @return the _tempPassword
	 */
	@ConfigurationProperty(confidential = true, order = 52, displayMessageKey="SAP_TEMP_PASSWORD_DISPLAY", helpMessageKey="SAP_TEMP_PASSWORD_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public GuardedString getTempPassword() {
		return _tempPassword;
	}

	/**
	 * @param tempPassword
	 *            the _tempPassword to set
	 */
	public void setTempPassword(GuardedString _tempPassword) {
		this._tempPassword = _tempPassword;

	}

	/**
	 * @return the eatNonUpdateCreate flag
	 */
	@ConfigurationProperty(order = 53, displayMessageKey="SAP_EAT_EXCEPTION_FOR_NON_UPDATEABLE_ATTRS_DISPLAY", helpMessageKey="SAP_EAT_EXCEPTION_FOR_NON_UPDATEABLE_ATTRS_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean isEatNonUpdateCreate() {
		return _eatNonUpdateCreate;
	}

	/**
	 * @param nonUpdateCreate
	 *            the eatNonUpdateCreate to set
	 */
	public void setEatNonUpdateCreate(boolean nonUpdateCreate) {
		_eatNonUpdateCreate = nonUpdateCreate;
	}

	/**
	 * @return the _cuaChildInitialPasswordChangeFuncModule
	 */
	@ConfigurationProperty(order = 54, displayMessageKey="SAP_CUA_INIT_PASSWORD_FM_DISPLAY", helpMessageKey="SAP_CUA_INIT_PASSWORD_FM_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getCuaChildInitialPasswordChangeFuncModule() {
		return _cuaChildInitialPasswordChangeFuncModule;
	}

	/**
	 * @param childInitialPasswordChangeFuncModule
	 *            the _cuaChildInitialPasswordChangeFuncModule to set
	 */
	public void setCuaChildInitialPasswordChangeFuncModule(
			String childInitialPasswordChangeFuncModule) {
		_cuaChildInitialPasswordChangeFuncModule = childInitialPasswordChangeFuncModule;
	}

	/**
	 * @return the _cuaChildPasswordCheckFuncModule
	 */
	@ConfigurationProperty(order = 55, displayMessageKey="SAP_CUA_CHECK_PASSWORD_FM_DISPLAY", helpMessageKey="SAP_CUA_CHECK_PASSWORD_FM_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getCuaChildPasswordCheckFuncModule() {
		return _cuaChildPasswordCheckFuncModule;
	}

	/**
	 * @param childPasswordCheckFuncModule
	 *            the _cuaChildPasswordCheckFuncModule to set
	 */
	public void setCuaChildPasswordCheckFuncModule(
			String childPasswordCheckFuncModule) {
		_cuaChildPasswordCheckFuncModule = childPasswordCheckFuncModule;
	}

	/**
	 * @return the _cuaChildPasswordCheckDelay
	 */
	@ConfigurationProperty(order = 56, displayMessageKey="SAP_CUA_CHECK_PASSWORD_DELAY_DISPLAY", helpMessageKey="SAP_CUA_CHECK_PASSWORD_DELAY_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public int getCuaChildPasswordCheckDelay() {
		return _cuaChildPasswordCheckDelay;
	}

	/**
	 * @param childPasswordCheckDelay
	 *            the _cuaChildPasswordCheckDelay to set
	 */
	public void setCuaChildPasswordCheckDelay(int childPasswordCheckDelay) {
		_cuaChildPasswordCheckDelay = childPasswordCheckDelay;
	}

	/**
	 * @return the _cuaChildPasswordChangeFuncModule
	 */
	@ConfigurationProperty(order = 57, displayMessageKey="SAP_CUA_PASSWORD_FM_DISPLAY", helpMessageKey="SAP_CUA_PASSWORD_FM_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getCuaChildPasswordChangeFuncModule() {
		return _cuaChildPasswordChangeFuncModule;
	}

	/**
	 * @param childPasswordChangeFuncModule
	 *            the _cuaChildPasswordChangeFuncModule to set
	 */
	public void setCuaChildPasswordChangeFuncModule(
			String childPasswordChangeFuncModule) {
		_cuaChildPasswordChangeFuncModule = childPasswordChangeFuncModule;
	}

	/**
	 * @return the tableFormats
	 */
	@ConfigurationProperty(order = 58, displayMessageKey="SAP_TABLE_FORMATS_DISPLAY", helpMessageKey="SAP_TABLE_FORMATS_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String[] getTableFormats() {
		return _tableFormats;
	}

	/**
	 * @param tableFormats
	 *            the tableFormats to set
	 */
	public void setTableFormats(String[] tableFormats) {
		this._tableFormats = tableFormats;
	}

	@ConfigurationProperty(order = 59, displayMessageKey="SAP_OVER_WRITE_LINK_DISPLAY", helpMessageKey="SAP_OVER_WRITE_LINK_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean isOverwriteLink() {
		return overwriteLink;
	}

	public void setOverwriteLink(boolean overwriteLink) {
		this.overwriteLink = overwriteLink;
	}

	/*public boolean isSupportHRMS0105InfotypeLinking() {
		return supportHRMS0105InfotypeLinking;
	}

	public void setSupportHRMS0105InfotypeLinking(
			boolean supportHRMS0105InfotypeLinking) {
		this.supportHRMS0105InfotypeLinking = supportHRMS0105InfotypeLinking;
	}*/

	/*
	 * public boolean isPasswordPropagateToChildSystem() { return
	 * isPasswordPropagateToChildSystem; }
	 * 
	 * public void setPasswordPropagateToChildSystem( boolean
	 * isPasswordPropagateToChildSystem) { this.isPasswordPropagateToChildSystem
	 * = isPasswordPropagateToChildSystem; }
	 */
	@ConfigurationProperty(order = 60, displayMessageKey="SAP_RECON_FUTURE_DATED_ROLES_DISPLAY", helpMessageKey="SAP_RECON_FUTURE_DATED_ROLES_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean isReconcilefuturedatedroles() {
		return reconcilefuturedatedroles;
	}

	public void setReconcilefuturedatedroles(boolean reconcilefuturedatedroles) {
		this.reconcilefuturedatedroles = reconcilefuturedatedroles;
	}
	
	@ConfigurationProperty(order = 61, displayMessageKey="SAP_RECON_PAST_DATED_ROLES_DISPLAY", helpMessageKey="SAP_RECON_PAST_DATED_ROLES_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean isReconcilepastdatedroles() {
		return reconcilepastdatedroles;
	}

	public void setReconcilepastdatedroles(boolean reconcilepastdatedroles) {
		this.reconcilepastdatedroles = reconcilepastdatedroles;
	}

	/*
	 * public boolean isChangePassword() { return changePassword; }
	 * 
	 * public void setChangePassword(boolean changePassword) {
	 * this.changePassword = changePassword; }
	 */

	@ConfigurationProperty(order = 62, displayMessageKey="SAP_VALIDATE_PERNR_DISPLAY", helpMessageKey="SAP_VALIDATE_PERNR_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public boolean isValidatePERNR() {
		return validatePERNR;
	}

	public void setValidatePERNR(boolean validatePERNR) {
		this.validatePERNR = validatePERNR;
	}

	@ConfigurationProperty(order = 63, displayMessageKey="SAP_BATCH_SIZE_DISPLAY", helpMessageKey="SAP_BATCH_SIZE_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}
	

	@ConfigurationProperty(order = 64, displayMessageKey="SAP_ROLES_DISPLAY", helpMessageKey="SAP_ROLES_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	@ConfigurationProperty(order = 65, displayMessageKey="SAP_PROFILES_DISPLAY", helpMessageKey="SAP_PROFILES_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getProfiles() {
		return profiles;
	}

	public void setProfiles(String profiles) {
		this.profiles = profiles;
	}

	@ConfigurationProperty(order = 66, displayMessageKey="SAP_PARAMETERS_DISPLAY", helpMessageKey="SAP_PARAMETERS_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the groups
	 */
	@ConfigurationProperty(order = 67, displayMessageKey="SAP_GROUPS_DISPLAY", helpMessageKey="SAP_GROUPS_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(String groups) {
		this.groups = groups;
	}
	
	@ConfigurationProperty(order = 68, displayMessageKey="SAP_MASTERSYSTEM_DISPLAY", helpMessageKey="SAP_MASTERSYSTEM_HELP")
	public String getmasterSystem() {
		return _masterSystem;
	}

	public void setmasterSystem(String _masterSystem) {
		this._masterSystem = _masterSystem;
	}
	@ConfigurationProperty(order=69, displayMessageKey="SAP_CHANGE_PWD_AT_NEXT_LOGON_DISPLAY", helpMessageKey="SAP_CHANGE_PWD_AT_NEXT_LOGON_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getchangePasswordAtNextLogon() {
		return _changePasswordAtNextLogon;
	}

	public void setchangePasswordAtNextLogon(String _changePasswordAtNextLogon) {
		this._changePasswordAtNextLogon = _changePasswordAtNextLogon;

	}
	 @ConfigurationProperty(order=70, displayMessageKey="SAP_DUMMY_PASSWORD_DISPLAY", helpMessageKey="SAP_DUMMY_PASSWORD_HELP",confidential=true)
	    public GuardedString getdummyPassword() {
	        return _dummyPassword;
	    }

	    /**
	     *@param password the _password to set
	     **/
	    public void setdummyPassword(GuardedString _dummyPassword) {
	        this._dummyPassword = _dummyPassword;
	    }
	
	@ConfigurationProperty(order=71, displayMessageKey="SAP_PWD_PROPAGATE_TO_CHILD_SYS_DISPLAY", helpMessageKey="SAP_PWD_PROPAGATE_TO_CHILD_SYS_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getpasswordPropagateToChildSystem() {
		return _passwordPropagateToChildSystem;
	}

	public void setpasswordPropagateToChildSystem(String _passwordPropagateToChildSystem) {
		this._passwordPropagateToChildSystem = _passwordPropagateToChildSystem;
	}

	@ConfigurationProperty(order = 72, displayMessageKey="SAP_SAP_SYS_TIMEZONE_DISPLAY", helpMessageKey="SAP_SAP_SYS_TIMEZONE_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getSapSystemTimeZone() {
		return sapSystemTimeZone;
	}

	public void setSapSystemTimeZone(String sapSystemTimeZone) {
		this.sapSystemTimeZone = sapSystemTimeZone;
	}
	
	// Start:: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED
	@ConfigurationProperty(order = 73, displayMessageKey="SAP_DISABLE_LOCK_STATUS_DISPLAY", helpMessageKey="SAP_DISABLE_LOCK_STATUS_HELP")
	public String getdisableLockStatus() {
		return _disableLockStatus;
	}

	public void setdisableLockStatus(String _disableLockStatus) {
		this._disableLockStatus = _disableLockStatus;
	}
	
    // End:: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED
}
