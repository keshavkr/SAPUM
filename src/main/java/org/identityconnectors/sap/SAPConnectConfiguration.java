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

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.common.objects.ObjectClass;

/**
 * Implements the {@link Configuration} interface to provide all the necessary
 * parameters to initialize the SAP Connector.
 *
 * @author bfarrell
 * @version 1.0
 * @since 1.0
 * 
 */
public class SAPConnectConfiguration extends AbstractConfiguration {
    
    /*
     * Set up base configuration elements
     */
    private String        _destination;
    private boolean       _loadBalance = false;
    private String        _host;
    private String        _systemNumber = "00";
    private String        _r3Name;
    private String        _msHost;
    private String        _msServ;
    private String        _jcoGroup;
    private String        _jcoSAPRouter;
    private String        _client = "000";
    private String        _language = "EN";
    private boolean       _useSNC = false;

    private boolean       _configureConnectionTuning = false;
    private int           _connectionPoolCapacity;
    private int           _connectionPoolActiveLimit;
    private int           _connectionPoolExpirationTime;
    private int           _connectionPoolExpirationPeriod;
    private int           _connectionMaxGetTime;
    
    private int           _maxBAPIRetries = 5;
    private int           _retryWaitTime = 500;
    
    private int           _jcoTrace = 0;
    private String        _jcoTraceDir;
    
    // normal authentication
    private String        _user;
    private GuardedString _password;
    
    // snc authentication
    private String        _sncPartnerName;
    private String        _sncName;
    private String        _sncLib;
    private String        _sncProtectionLevel;
    private String        _sncX509Cert;
    
    /*private String        _masterSystem;
    private String        _changePasswordAtNextLogon;
    private GuardedString        _dummyPassword;
   
    private String        _passwordPropagateToChildSystem;*/
    
    private String        _aliasUser;
    private String        _gatewayHost;
    private String        _gatewayService;
    private String        _tpName;
    private String        _tpHost;
    private String        _type;
    //private String        _cpicTrace;
    private String        _codePage;
    private String        _getSSO2;
    private String        _mySAPSSO2;
    private String        _lCheck;
    private String        _dsr;
    private String        _repositoryDestination;
    private String        _repositoryUser;
    private String        _repositoryPassword;
    private String        _repositorySNCMode;
    // Start:: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
    private String singleRoles;
    private String compositeRoles;
    //END:: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
    
    
	/**
     * Constructor
     */
    public SAPConnectConfiguration() {
        
    }
    
    public SAPConnectConfiguration(SAPConnectConfiguration conf) {
        _destination = conf.getDestination();
        _loadBalance = conf.getLoadBalance();
        _host = conf.getHost();
        _systemNumber = conf.getSystemNumber();
        _r3Name = conf.getR3Name();
        _msHost = conf.getMsHost();
        _msServ = conf.getMsServ();
        _jcoGroup = conf.getJcoGroup();
        _jcoSAPRouter = conf.getJcoSAPRouter();
        _client = conf.getClient();
        _language = conf.getLanguage();
        _useSNC = conf.getUseSNC();
        _configureConnectionTuning = conf.getConfigureConnectionTuning();
        _connectionPoolCapacity = conf.getConnectionPoolCapacity();
        _connectionPoolActiveLimit = conf.getConnectionPoolActiveLimit();
        _connectionPoolExpirationTime = conf.getConnectionPoolExpirationTime();
        _connectionPoolExpirationPeriod = conf.getConnectionPoolExpirationPeriod();
        _connectionMaxGetTime = conf.getConnectionMaxGetTime();
        _maxBAPIRetries = conf.getMaxBAPIRetries();
        _retryWaitTime = conf.getRetryWaitTime();
        _jcoTrace = conf.getJcoTrace();
        _jcoTraceDir = conf.getJcoTraceDir();
        _user = conf.getUser();
        _password = conf.getPassword();
        _sncPartnerName = conf.getSncPartnerName();
        _sncName = conf.getSncName();
        _sncLib = conf.getSncLib();
        _sncProtectionLevel = conf.getSncProtectionLevel();
        _sncX509Cert = conf.getSncX509Cert();
        
        _aliasUser=conf.getAliasUser();
        _gatewayHost=conf.getGatewayHost();
        _gatewayService=conf.getGatewayService();
        _tpHost=conf.getTpHost();
        _tpName=conf.getTpName();
        _type=conf.getType();
        _codePage=conf.getCodePage();
        _getSSO2=conf.getGetSSO2();
        _mySAPSSO2=conf.getMySAPSSO2();
        _lCheck=conf.getLCheck();
        _dsr=conf.getDsr();
        _repositoryDestination=conf.getRepositoryDestination();
        _repositoryUser=conf.getRepositoryUser();
        _repositoryPassword=conf.getRepositoryPassword();
        _repositorySNCMode=conf.getRepositorySNCMode();
     // Start:: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
        singleRoles = conf.getSingleRoles();
        compositeRoles = conf.getCompositeRoles();
     // END :: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES 
     
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate() {
        Assertions.blankCheck(_destination, "destination");
        if (_loadBalance) {
            Assertions.blankCheck(_r3Name, "R3 Name");
            Assertions.blankCheck(_msHost, "MSHost");
            Assertions.blankCheck(_jcoGroup, "JCoGroup");
        } else {
            Assertions.blankCheck(_host, "host");
            Assertions.blankCheck(_systemNumber, "systemNumber");
        }
        Assertions.blankCheck(_client, "client");
        Assertions.blankCheck(_language, "language");

        Assertions.nullCheck(_useSNC, "useSNC");
        if (!_useSNC) {
            Assertions.blankCheck(_user, "user");
            Assertions.nullCheck(_password, "password");
        } else {
            Assertions.blankCheck(_sncPartnerName, "sncPartnerName");
            Assertions.blankCheck(_sncName, "sncName");
            Assertions.blankCheck(_sncLib, "sncLib");
            Assertions.blankCheck(_sncProtectionLevel, "sncProtectionLevel");
            Assertions.blankCheck(_sncX509Cert, "sncX509Cert");
        }
        Assertions.nullCheck(_jcoTrace, "jcoTrace");
    }
    
    /**
     * This method gets the connector message with the given key.
     * @param key - String to retrieve
     * @return - formatted message
     */
    public String getMessage(String key) {
        return getConnectorMessages().format(key, key);
    }
    
    /**
     * This method gets the connector message with the given key and formats it 
     * with the given objects.
     * @param key - String to retrieve
     * @param objects - Objects to be inserted with the formated message key.
     * @return - formatted message
     */
    public String getMessage(String key, Object... objects) {
        return getConnectorMessages().format(key, key, objects);
    }
    
    /**
     * @return
     */
    @ConfigurationProperty(order=1, displayMessageKey="SAP_DEST_NAME_DISPLAY", helpMessageKey="SAP_DEST_NAME_HELP", required=true)
    public String getDestination() {
        return _destination;
    }
    
    /**
     * @param dest
     */
    public void setDestination(String dest) {
        _destination = dest;
    }
    
    /**
     * @return the loadBalance
     */
    @ConfigurationProperty(order=2, displayMessageKey="SAP_USE_LOAD_BAL_DISPLAY", helpMessageKey="SAP_USE_LOAD_BAL_HELP")
    public boolean getLoadBalance() {
        return _loadBalance;
    }

    /**
     * @param loadBalance the loadBalance to set
     */
    public void setLoadBalance(boolean loadBalance) {
        this._loadBalance = loadBalance;
    }

    /**
     * @return the _host
     */
    @ConfigurationProperty(order=3, displayMessageKey="SAP_HOST_DISPLAY", helpMessageKey="SAP_HOST_HELP", required=true)
    public String getHost() {
        return _host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this._host = host;
    }

    /**
     * @return the _systemNumber
     */
    @ConfigurationProperty(order=4, displayMessageKey="SAP_SYSTEM_NUMBER_DISPLAY", helpMessageKey="SAP_SYSTEM_NUMBER_HELP", required=true)
    public String getSystemNumber() {
        return _systemNumber;
    }

    /**
     * @param systemNumber the systemNumber to set
     */
    public void setSystemNumber(String systemNumber) {
        _systemNumber = systemNumber;
    }
    
    /**
     * @return the r3Name
     */
    @ConfigurationProperty(order=5, displayMessageKey="SAP_R3NAME_DISPLAY", helpMessageKey="System ID of the SAP system, mandatory for a logon balancing connection")
    public String getR3Name() {
        return _r3Name;
    }

    /**
     * @param name the r3Name to set
     */
    public void setR3Name(String name) {
        _r3Name = name;
    }

    /**
     * @return the msHost
     */
    @ConfigurationProperty(order=6, displayMessageKey="SAP_MSG_SRV_HOST_DISPLAY", helpMessageKey="SAP_MSG_SRV_HOST_HELP")
    public String getMsHost() {
        return _msHost;
    }

    /**
     * @param msHost the msHost to set
     */
    public void setMsHost(String msHost) {
        this._msHost = msHost;
    }

    /**
     * @return the msServ
     */
    @ConfigurationProperty(order=7, displayMessageKey="SAP_MSG_SRV_PORT_DISPLAY", helpMessageKey="SAP_MSG_SRV_PORT_HELP")
    public String getMsServ() {
        return _msServ;
    }

    /**
     * @param msServ the msServ to set
     */
    public void setMsServ(String msServ) {
        this._msServ = msServ;
    }

    /**
     * @return the jcoGroup
     */
    @ConfigurationProperty(order=8, displayMessageKey="SAP_AS_GROUP_DISPLAY", helpMessageKey="SAP_AS_GROUP_HELP")
    public String getJcoGroup() {
        return _jcoGroup;
    }

    /**
     * @param jcoGroup the jcoGroup to set
     */
    public void setJcoGroup(String jcoGroup) {
        this._jcoGroup = jcoGroup;
    }

    /**
     * @return the jcoSAPRouter
     */
    @ConfigurationProperty(order=9, displayMessageKey="SAP_SAP_ROUTER_DISPLAY", helpMessageKey="SAP_SAP_ROUTER_HELP")
    public String getJcoSAPRouter() {
        return _jcoSAPRouter;
    }

    /**
     * @param jcoSAPRouter the jcoSAPRouter to set
     */
    public void setJcoSAPRouter(String jcoSAPRouter) {
        this._jcoSAPRouter = jcoSAPRouter;
    }

    /**
     * @return the _client
     */
    @ConfigurationProperty(order=10, displayMessageKey="SAP_CLIENT_DISPLAY", helpMessageKey="SAP_CLIENT_HELP", required = true)
    public String getClient() {
        return _client;
    }

    /**
     * @param client the _client to set
     */
    public void setClient(String client) {
        this._client = client;
    }

    /**
     * @return the _language
     */
    @ConfigurationProperty(order=11, displayMessageKey="SAP_LANGUAGE_DISPLAY", helpMessageKey="SAP_LANGUAGE_HELP", required=true)
    public String getLanguage() {
        return _language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this._language = language;
    }
    
    /**
     * @return the _useSNC
     */
    @ConfigurationProperty(order=12, displayMessageKey="SAP_USE_SNC_DISPLAY", helpMessageKey="SAP_USE_SNC_HELP")
    public boolean getUseSNC() {
        return _useSNC;
    }

    /**
     * @param useSnc the _useSNC to set
     */
    public void setUseSNC(boolean useSnc) {
        _useSNC = useSnc;
    }

    /**
     * @return the _user
     */
    @ConfigurationProperty(order=13, displayMessageKey="SAP_USER_DISPLAY", helpMessageKey="SAP_USER_HELP", required=true)
    public String getUser() {
        return _user;
    }

    /**
     * @param user the _user to set
     */
    public void setUser(String user) {
        this._user = user;
    }

    /**
     * @return the _password
     */
    @ConfigurationProperty(confidential=true, order=14, displayMessageKey="SAP_PASSWORD_DISPLAY", helpMessageKey="SAP_PASSWORD_HELP", required=true)
    public GuardedString getPassword() {
        return _password;
    }

    /**
     * @param password the _password to set
     */
    public void setPassword(GuardedString password) {
        this._password = password;
    }

    /**
     * @return the _sncProtectionLevel
     */
    @ConfigurationProperty(order=15, displayMessageKey="SAP_SNC_PROT_LEVEL_DISPLAY", helpMessageKey="SAP_SNC_PROT_LEVEL_HELP")
    public String getSncProtectionLevel() {
        return _sncProtectionLevel;
    }

    /**
     * @param protectionLevel the _sncProtectionLevel to set
     */
    public void setSncProtectionLevel(String protectionLevel) {
        _sncProtectionLevel = protectionLevel;
    }
    
    /**
     * @return the _sncName
     */
    @ConfigurationProperty(order=16, displayMessageKey="SAP_SNC_NAME_DISPLAY", helpMessageKey="SAP_SNC_NAME_HELP")
    public String getSncName() {
        return _sncName;
    }

    /**
     * @param sncName the _sncName to set
     */
    public void setSncName(String sncName) {
        _sncName = sncName;
    }

    /**
     * @return the _sncPartnerName
     */
    @ConfigurationProperty(order=17, displayMessageKey="SAP_SNC_PARTNER_NAME_DISPLAY", helpMessageKey="SAP_SNC_PARTNER_NAME_HELP")
    public String getSncPartnerName() {
        return _sncPartnerName;
    }

    /**
     * @param partnerName the _sncPartnerName to set
     */
    public void setSncPartnerName(String partnerName) {
        _sncPartnerName = partnerName;
    }
    
    /**
     * @return the _sncX509Cert
     */
    @ConfigurationProperty(order=18, displayMessageKey="SAP_SNC_X509CERT_DISPLAY", helpMessageKey="SAP_SNC_X509CERT_HELP")
    public String getSncX509Cert() {
        return _sncX509Cert;
    }

    /**
     * @param cert the _sncX509Cert to set
     */
    public void setSncX509Cert(String cert) {
        _sncX509Cert = cert;
    }

    /**
     * @return the _sncLib
     */
    @ConfigurationProperty(order=19, displayMessageKey="SAP_SNC_LIB_DISPLAY", helpMessageKey="SAP_SNC_LIB_HELP")
    public String getSncLib() {
        return _sncLib;
    }

    /**
     * @param lib the _sncLib to set
     */
    public void setSncLib(String lib) {
        _sncLib = lib;
    }
    
    /**
     * @return the configureConnectionTuning
     */
    @ConfigurationProperty(order=20, displayMessageKey="SAP_CONN_ATTR_ENABLE_DISPLAY", helpMessageKey="SAP_CONN_ATTR_ENABLE_HELP")
    public boolean getConfigureConnectionTuning() {
        return _configureConnectionTuning;
    }
    
    /**
     * @param connectionTuning
     */
    public void setConfigureConnectionTuning(boolean connectionTuning) {
        _configureConnectionTuning = connectionTuning;
    }
    
    /**
     * @return the connectionPoolCapacity
     */
    @ConfigurationProperty(order=21, displayMessageKey="SAP_CONN_POOL_CAPACITY_DISPLAY", helpMessageKey="SAP_CONN_POOL_CAPACITY_HELP")
    public int getConnectionPoolCapacity() {
        return _connectionPoolCapacity;
    }

    /**
     * @param connectionPoolCapacity the connectionPoolCapacity to set
     */
    public void setConnectionPoolCapacity(int connectionPoolCapacity) {
        this._connectionPoolCapacity = connectionPoolCapacity;
    }

    /**
     * @return the connectionPoolActiveLimit
     */
    @ConfigurationProperty(order=22, displayMessageKey="SAP_CONN_POOL_ACTIVE_DISPLAY", helpMessageKey="SAP_CONN_POOL_ACTIVE_HELP")
    public int getConnectionPoolActiveLimit() {
        return _connectionPoolActiveLimit;
    }

    /**
     * @param connectionPoolActiveLimit the connectionPoolActiveLimit to set
     */
    public void setConnectionPoolActiveLimit(int connectionPoolActiveLimit) {
        this._connectionPoolActiveLimit = connectionPoolActiveLimit;
    }

    /**
     * @return the connectionPoolExpirationTimeBack2wk!
     */
    @ConfigurationProperty(order=23, displayMessageKey="SAP_CONN_POOL_EXPIRE_TIME_DISPLAY", helpMessageKey="SAP_CONN_POOL_EXPIRE_TIME_HELP")
    public int getConnectionPoolExpirationTime() {
        return _connectionPoolExpirationTime;
    }

    /**
     * @param connectionPoolExpirationTime the connectionPoolExpirationTime to set
     */
    public void setConnectionPoolExpirationTime(int connectionPoolExpirationTime) {
        this._connectionPoolExpirationTime = connectionPoolExpirationTime;
    }

    /**
     * @return the connectionPoolExpirationPeriod
     */
    @ConfigurationProperty(order=24, displayMessageKey="SAP_CONN_POOL_EXPIRE_PERIOD_DISPLAY", helpMessageKey="SAP_CONN_POOL_EXPIRE_PERIOD_HELP")
    public int getConnectionPoolExpirationPeriod() {
        return _connectionPoolExpirationPeriod;
    }

    /**
     * @param connectionPoolExpirationPeriod the connectionPoolExpirationPeriod to set
     */
    public void setConnectionPoolExpirationPeriod(int connectionPoolExpirationPeriod) {
        this._connectionPoolExpirationPeriod = connectionPoolExpirationPeriod;
    }

    /**
     * @return the connectionMaxGetTime
     */
    @ConfigurationProperty(order=25, displayMessageKey="SAP_CONN_MAX_GET_TIME_DISPLAY", helpMessageKey="SAP_CONN_MAX_GET_TIME_HELP")
    public int getConnectionMaxGetTime() {
        return _connectionMaxGetTime;
    }

    /**
     * @param connectionMaxGetTime the connectionMaxGetTime to set
     */
    public void setConnectionMaxGetTime(int connectionMaxGetTime) {
        this._connectionMaxGetTime = connectionMaxGetTime;
    }
    
    /**
     * @return the _jcoTrace
     */
    @ConfigurationProperty(order=26, displayMessageKey="SAP_JCO_TRACE_LEVEL_DISPLAY", helpMessageKey="SAP_JCO_TRACE_LEVEL_HELP")
    public int getJcoTrace() {
        return _jcoTrace;
    }

    /**
     * @param trace the jcoTrace to set
     */
    public void setJcoTrace(int trace) {
        _jcoTrace = trace;
    }

    /**
     * @return the _jcoTraceDir
     */
    @ConfigurationProperty(order=27, displayMessageKey="SAP_JCO_TRACE_PATH_DISPLAY", helpMessageKey="SAP_JCO_TRACE_PATH_HELP")
    public String getJcoTraceDir() {
        return _jcoTraceDir;
    }

    /**
     * @param traceDir the jcoTraceDir to set
     */
    public void setJcoTraceDir(String traceDir) {
        _jcoTraceDir = traceDir;
    }
    
    /**
     * @return the _maxBAPIRetries
     */
    @ConfigurationProperty(order=28, displayMessageKey="SAP_MAX_BAPI_RETRIES_DISPLAY", helpMessageKey="SAP_MAX_BAPI_RETRIES_HELP")
    public int getMaxBAPIRetries() {
        return _maxBAPIRetries;
    }

    /**
     * @param maxBAPIRetries the _maxBAPIRetries to set
     */
    public void setMaxBAPIRetries(int maxBAPIRetries) {
        _maxBAPIRetries = maxBAPIRetries;
    }
    
    /**
     * @return _retryWaitTime
     */
    @ConfigurationProperty(order=29, displayMessageKey="SAP_RETRY_WAIT_TIME_DISPLAY", helpMessageKey="SAP_RETRY_WAIT_TIME_HELP")
    public int getRetryWaitTime() {
        return _retryWaitTime;
    }
    
    /**
     * @param retryWaitTime
     */
    public void setRetryWaitTime(int retryWaitTime) {
        _retryWaitTime = retryWaitTime;
    }

    @ConfigurationProperty(order = 30, displayMessageKey="SAP_ALIAS_USER_DISPLAY", helpMessageKey="SAP_ALIAS_USER_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
    public String getAliasUser() {
		return _aliasUser;
	}

	public void setAliasUser(String aliasUser) {
		_aliasUser = aliasUser;
	}

	@ConfigurationProperty(order = 31, displayMessageKey="SAP_GATEWAY_HOST_DISPLAY", helpMessageKey="SAP_GATEWAY_HOST_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getGatewayHost() {
		return _gatewayHost;
	}

	public void setGatewayHost(String gatewayHost) {
		_gatewayHost = gatewayHost;
	}

	@ConfigurationProperty(order = 32, displayMessageKey="SAP_GATEWAY_SERVICE_DISPLAY", helpMessageKey="SAP_GATEWAY_SERVICE_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getGatewayService() {
		return _gatewayService;
	}

	public void setGatewayService(String gatewayService) {
		_gatewayService = gatewayService;
	}

	@ConfigurationProperty(order = 33, displayMessageKey="SAP_TP_NAME_DISPLAY", helpMessageKey="SAP_TP_NAME_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getTpName() {
		return _tpName;
	}

	public void setTpName(String tpName) {
		_tpName = tpName;
	}

	@ConfigurationProperty(order = 34, displayMessageKey="SAP_TP_HOST_DISPLAY", helpMessageKey="SAP_TP_HOST_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getTpHost() {
		return _tpHost;
	}

	public void setTpHost(String tpHost) {
		_tpHost = tpHost;
	}

	@ConfigurationProperty(order = 35, displayMessageKey="SAP_TYPE_DISPLAY", helpMessageKey="SAP_TYPE_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	@ConfigurationProperty(order = 36, displayMessageKey="SAP_CODE_PAGE_DISPLAY", helpMessageKey="SAP_CODE_PAGE_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getCodePage() {
		return _codePage;
	}

	public void setCodePage(String codePage) {
		_codePage = codePage;
	}

	@ConfigurationProperty(order = 37, displayMessageKey="SAP_GET_SSO2_DISPLAY", helpMessageKey="SAP_GET_SSO2_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getGetSSO2() {
		return _getSSO2;
	}

	public void setGetSSO2(String getSSO2) {
		_getSSO2 = getSSO2;
	}

	@ConfigurationProperty(order = 38, displayMessageKey="SAP_GET_MYSAPSSO2_DISPLAY", helpMessageKey="SAP_GET_MYSAPSSO2_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getMySAPSSO2() {
		return _mySAPSSO2;
	}

	public void setMySAPSSO2(String mySAPSSO2) {
		_mySAPSSO2 = mySAPSSO2;
	}

	@ConfigurationProperty(order = 39, displayMessageKey="SAP_L_CHECK_DISPLAY", helpMessageKey="SAP_L_CHECK_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getLCheck() {
		return _lCheck;
	}

	public void setLCheck(String lCheck) {
		_lCheck = lCheck;
	}

	@ConfigurationProperty(order = 40, displayMessageKey="SAP_DSR_DISPLAY", helpMessageKey="SAP_DSR_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getDsr() {
		return _dsr;
	}

	public void setDsr(String dsr) {
		_dsr = dsr;
	}

	@ConfigurationProperty(order = 41, displayMessageKey="SAP_REPOSITORY_DEST_DISPLAY", helpMessageKey="SAP_REPOSITORY_DEST_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getRepositoryDestination() {
		return _repositoryDestination;
	}

	public void setRepositoryDestination(String repositoryDestination) {
		_repositoryDestination = repositoryDestination;
	}

	@ConfigurationProperty(order = 42, displayMessageKey="SAP_REPOSITORY_USER_DISPLAY", helpMessageKey="SAP_REPOSITORY_USER_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getRepositoryUser() {
		return _repositoryUser;
	}

	public void setRepositoryUser(String repositoryUser) {
		_repositoryUser = repositoryUser;
	}

	@ConfigurationProperty(order = 43, displayMessageKey="SAP_REPOSITORY_PASSWORD_DISPLAY", helpMessageKey="SAP_REPOSITORY_PASSWORD_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getRepositoryPassword() {
		return _repositoryPassword;
	}

	public void setRepositoryPassword(String repositoryPassword) {
		_repositoryPassword = repositoryPassword;
	}

	@ConfigurationProperty(order = 44, displayMessageKey="SAP_REPOSITORY_SNC_MODE_DISPLAY", helpMessageKey="SAP_REPOSITORY_SNC_MODE_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getRepositorySNCMode() {
		return _repositorySNCMode;
	}

	public void setRepositorySNCMode(String repositorySNCMode) {
		_repositorySNCMode = repositorySNCMode;
	}

	// Start:: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES
	/**
	 * @return the singleRoles
	 */
	@ConfigurationProperty(order = 45, displayMessageKey="SAP_GET_SINGLE_ROLES_DISPLAY", helpMessageKey="SAP_GET_SINGLE_ROLES_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getSingleRoles() {
		return singleRoles;
	}

	/**
	 * @param singleRoles the singleRoles to set
	 */
	public void setSingleRoles(String singleRoles) {
		this.singleRoles = singleRoles;
	}

	/**
	 * @return the compositeRoles
	 */
	@ConfigurationProperty(order = 46, displayMessageKey="SAP_GET_COMPOSITE_ROLES_DISPLAY", helpMessageKey="SAP_GET_COMPOSITE_ROLES_HELP", objectClasses="ObjectClass.ACCOUNT_NAME")
	public String getCompositeRoles() {
		return compositeRoles;
	}

	/**
	 * @param compositeRoles the compositeRoles to set
	 */
	public void setCompositeRoles(String compositeRoles) {
		this.compositeRoles = compositeRoles;
	}

	// END :: Bug 17911657-SAP ROLE RECON IS OMITTING COMPOSITE ROLES

	// executed only from OIM during create user method and newly created user
	// will be assigned with the master system
	/*@ConfigurationProperty(order=37)
	public String getmasterSystem() {
		return _masterSystem;
	}

	public void setmasterSystem(String _masterSystem) {
		this._masterSystem = _masterSystem;
	}
	@ConfigurationProperty(order=38)
	public String getchangePasswordAtNextLogon() {
		return _changePasswordAtNextLogon;
	}

	public void setchangePasswordAtNextLogon(String _changePasswordAtNextLogon) {
		this._changePasswordAtNextLogon = _changePasswordAtNextLogon;

	}
	 @ConfigurationProperty(order=39)
	    public GuardedString getdummyPassword() {
	        return _dummyPassword;
	    }

	    *//**
	     * @param password the _password to set
	     *//*
	    public void setdummyPassword(GuardedString _dummyPassword) {
	        this._dummyPassword = _dummyPassword;
	    }
	
	@ConfigurationProperty(order=40)
	public String getpasswordPropagateToChildSystem() {
		return _passwordPropagateToChildSystem;
	}

	public void setpasswordPropagateToChildSystem(String _passwordPropagateToChildSystem) {
		this._passwordPropagateToChildSystem = _passwordPropagateToChildSystem;
	}*/
	
	
}
