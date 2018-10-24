import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.contract.data.groovy.Lazy;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.EmbeddedObject;

connector.client="300"
connector.host="172.26.144.153"
//connector.host="10.20.30.40"
connector.destination="dest87423"
connector.dummyPassword=new GuardedString("Password12".toCharArray())
connector.language="EN"
connector.masterSystem="EH6CLNT300"
connector.user="sapuser"
connector.password=new GuardedString("Mphasis123".toCharArray())

connector.systemNumber="00"
connector.configureConnectionTuning=false
connector.connectionMaxGetTime=0
connector.connectionPoolActiveLimit=0
connector.connectionPoolCapacity=0
connector.connectionPoolExpirationPeriod=0
connector.connectionPoolExpirationTime=0
connector.jcoGroup=""
connector.jcoSAPRouter=""
connector.jcoTrace=0
connector.jcoTraceDir=""
//connector.UseLoadBalance=false
connector.useLoadBalance=false
connector.maxBAPIRetries=5
connector.msHost=""
connector.msServ=""
connector.r3Name=""
connector.retryWaitTime=500
connector.sncLib=""
connector.sncName=""
connector.sncPartnerName=""
connector.sncProtectionLevel=""
connector.sncX509Cert=""
connector.useSNC=false
connector.changePasswordAtNextLogon="yes"
connector.batchSize="0"
connector.roles="ACTIVITYGROUPS~SUBSYSTEM;AGR_NAME;TO_DAT;FROM_DAT"
connector.groups="GROUPS~USERGROUP"


//No setter method for this parameter
//connector.'SOD Configuration lookup'="Lookup.SAPABAP.AC10.Configuration"
//connector.'User Configuration Lookup'="Lookup.SAPABAP.UM.Configuration"

connector.compositeRoles="no"
connector.enableCUA=false
connector.overwriteLink=false
connector.passwordPropagateToChildSystem="no"
connector.validatePERNR=false
connector.aliasUser="none"
connector.codePage="none"
connector.gatewayHost="none"
connector.gatewayService="none"
connector.getSSO2="none"
connector.lCheck="none"
connector.mySAPSSO2="none"
connector.repositoryDestination="none"
connector.repositoryPassword="none"
connector.repositorySNCMode="none"
connector.repositoryUser="none"
connector.tpHost="none"
connector.tpName="none"
connector.type="none"
connector.parameters="PARAMETER1~PARID;PARVA"
connector.profiles="PROFILES~SUBSYSTEM;PROFILE"
connector.sapSystemTimeZone="PST"
connector.reconcilefuturedatedroles=true
connector.reconcilepastdatedroles=true
connector.singleRoles="yes"
connector.cuaChildPasswordChangeFuncModule="ZXLCBAPI_ZXLCUSR_PASSWORDCHNGE"
connector.cuaChildInitialPasswordChangeFuncModule="ZXLCBAPI_ZXLCUSR_PW_CHANGE"
//connector.cuachildpasswordcheckfuncmodule=
//connector.cuachildpasswordcheckdelay="


connector.tableFormats=(String[])["PROFILES:=N|:BAPIPROF","ACTIVITYGROUPS:=N|:AGR_NAME|:FROM_DAT|:TO_DAT|:ORG_FLAG"]

//connector.roleDatasource=(String[])["UME_ROLE_PERSISTENCE","PCD_ROLE_PERSISTENCE"]


// TODO fill in the following test configurations

// Connector WRONG configuration for ValidateApiOpTests
testsuite.Validate.invalidConfig = [
  [ host : "" ]//,
//  [ login : "" ],
//  [ password : "" ]
  ]

// Connector WRONG configuration for TestApiOpTests
testsuite.Test.invalidConfig = [
  [ password: "NonExistingPassword_foo_bar_boo" ]
]
testsuite.Search.disable.caseinsensitive = true

testsuite.Multi.skip.lockout=true

testsuite {
    bundleJar = System.getProperty("bundleJar")
    bundleName = System.getProperty("bundleName")
    bundleVersion=System.getProperty("bundleVersion")
    connectorName="org.identityconnectors.sap.SAPConnector"

    Schema {
        oclasses = ['__ACCOUNT__','PROFILES','PARAMETER' ,'ACTIVITYGROUPS' ]
        attributes {
            __ACCOUNT__.oclasses = [
                           '__NAME__','__PASSWORD__',  '__CURRENT_PASSWORD__', '__LOCK_OUT__', '__ENABLE__','__PASSWORD_EXPIRED__', 'PERNR',
				//'User Lock;NONE;NONE;NONE',
				'ACCNT;LOGONDATA;ACCNT;LOGONDATAX','CLASS;LOGONDATA;CLASS;LOGONDATAX','GLTGV;LOGONDATA;GLTGV;LOGONDATAX', 'GLTGB;LOGONDATA;GLTGB;LOGONDATAX', 
				'TZONE;LOGONDATA;TZONE;LOGONDATAX', 'USTYP;LOGONDATA;USTYP;LOGONDATAX',
				 'LTIME;LOGONDATA;LTIME;LOGONDATAX' ,
 
				'KOSTL;DEFAULTS;KOSTL;DEFAULTSX',  'DATFM;DEFAULTS;DATFM;DEFAULTSX','DCPFM;DEFAULTS;DCPFM;DEFAULTSX',   'LANGU;DEFAULTS;LANGU;DEFAULTSX',  'START_MENU;DEFAULTS;START_MENU;DEFAULTSX', 
 
				'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX', 'LASTNAME;ADDRESS;LASTNAME;ADDRESSX',  'E_MAIL;ADDRESS;E_MAIL;ADDRESSX', 'TITLE_P;ADDRESS;TITLE_P;ADDRESSX', 
				'LANGU_P;ADDRESS;LANGU_P;ADDRESSX', 'FUNCTION;ADDRESS;FUNCTION;ADDRESSX', 'DEPARTMENT;ADDRESS;DEPARTMENT;ADDRESSX', 'COMM_TYPE;ADDRESS;COMM_TYPE;ADDRESSX', 
				'FAX_NUMBER;ADDRESS;FAX_NUMBER;ADDRESSX', 'FAX_EXTENS;ADDRESS;FAX_EXTENS;ADDRESSX','TEL1_EXT;ADDRESS;TEL1_EXT;ADDRESSX', 'TEL1_NUMBR;ADDRESS;TEL1_NUMBR;ADDRESSX',   
				'ROOM_NO_P;ADDRESS;ROOM_NO_P;ADDRESSX', 'BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX', 'FLOOR_P;ADDRESS;FLOOR_P;ADDRESSX',

				'USERALIAS;ALIAS;BAPIALIAS;ALIASX',  'COMPANY;COMPANY;COMPANY;COMPANYX', 'LIC_TYPE;UCLASS;UCLASS;UCLASSX', 'SUBSYSTEM;SYSTEMS',
				'USERGROUP;GROUPS;USERGROUP;GROUPS', 'roles', 'profiles', 'parameters'] // __ACCOUNT__.oclasses

	PROFILES.oclasses=['SUBSYSTEM','PROFILE','__NAME__','BAPIAKTPS','BAPIPTEXT','BAPITYPE']
	PARAMETER.oclasses=['PARTXT','__NAME__','PARID','PARVA' ]
	ACTIVITYGROUPS.oclasses=['AGR_NAME','FROM_DAT','TO_DAT','__NAME__' ,'ORG_FLAG','AGR_TEXT','SUBSYSTEM']

        } // attributes

        attrTemplate = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]// attrTemplate

        attrTemplateLong = [
            type: long.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]// attrTemplateLong

 attreqTemplate = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ]

 attrTemplateMvEmbd = [
            type: EmbeddedObject.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: true,	    
            returnedByDefault: true
        ]

attrTemplateMv = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: true,	    
            returnedByDefault: true
        ]

 attreqTemplatePwd = [
            type: GuardedString.class,
            readable: false,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: false
        ]

 attreqTemplateBool = [
            type: boolean.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: false
        ]

       
        operations = [
          //  UpdateAttributeValuesOp: ['__ACCOUNT__'],  
            SearchApiOp: ['__ACCOUNT__'],
           // ScriptOnConnectorApiOp: ['__ACCOUNT__'],
            ValidateApiOp: [ ],
         //   AuthenticationApiOp: ['__ACCOUNT__'],
            GetApiOp: ['__ACCOUNT__'],
            SchemaApiOp: ['__ACCOUNT__'],
		UpdateApiOp: ['__ACCOUNT__'],
		SyncApiOp: ['__ACCOUNT__'],

            TestApiOp: ['__ACCOUNT__','PROFILES','PARAMETER' ,'ACTIVITYGROUPS' ],
          //  ScriptOnResourceApiOp: ['__ACCOUNT__'],
            CreateApiOp: ['__ACCOUNT__'],
            DeleteApiOp: ['__ACCOUNT__'],
        //    ResolveUsernameApiOp: ['__ACCOUNT__']
        ]//operations
    } // Schema

	 Schema."PERNR".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
		
	 Schema."__PASSWORD__".attribute.__ACCOUNT__.oclasses = [
            type: GuardedString.class,
            readable: false,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: false
        ]
	 Schema."__CURRENT_PASSWORD__".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attreqTemplatePwd")
	 Schema."User Lock;NONE;NONE;NONE".attribute.__ACCOUNT__.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ] 
	 Schema.__LOCK_OUT__.attribute.__ACCOUNT__.oclasses =[
            type: boolean.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]
	 Schema.__ENABLE__.attribute.__ACCOUNT__.oclasses = 
[
            type: boolean.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]
	 Schema.__PASSWORD_EXPIRED__.attribute.__ACCOUNT__.oclasses = [
            type: boolean.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: false
        ]
	
	
	 Schema."ACCNT;LOGONDATA;ACCNT;LOGONDATAX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."CLASS;LOGONDATA;CLASS;LOGONDATAX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."GLTGV;LOGONDATA;GLTGV;LOGONDATAX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."GLTGB;LOGONDATA;GLTGB;LOGONDATAX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."TZONE;LOGONDATA;TZONE;LOGONDATAX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."USTYP;LOGONDATA;USTYP;LOGONDATAX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate") 
	 //Schema."LTIME;LOGONDATA;LTIME;LOGONDATAX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 //Schema."LOGONDATA;LTIME".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")

	 Schema."__NAME__".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
			 
	 Schema."KOSTL;DEFAULTS;KOSTL;DEFAULTSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."DATFM;DEFAULTS;DATFM;DEFAULTSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."DCPFM;DEFAULTS;DCPFM;DEFAULTSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."LANGU;DEFAULTS;LANGU;DEFAULTSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."START_MENU;DEFAULTS;START_MENU;DEFAULTSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 
	 Schema."USERNAME;BAPIBNAME".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate") 
	 Schema."FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."LASTNAME;ADDRESS;LASTNAME;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
	 Schema."E_MAIL;ADDRESS;E_MAIL;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."TITLE_P;ADDRESS;TITLE_P;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."LANGU_P;ADDRESS;LANGU_P;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."FUNCTION;ADDRESS;FUNCTION;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate") 
	 Schema."COMM_TYPE;ADDRESS;COMM_TYPE;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."FAX_NUMBER;ADDRESS;FAX_NUMBER;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."FAX_EXTENS;ADDRESS;FAX_EXTENS;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."TEL1_EXT;ADDRESS;TEL1_EXT;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."TEL1_NUMBR;ADDRESS;TEL1_NUMBR;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."ROOM_NO_P;ADDRESS;ROOM_NO_P;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
				 
	 Schema."BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."DEPARTMENT;ADDRESS;DEPARTMENT;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."FLOOR_P;ADDRESS;FLOOR_P;ADDRESSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."USERALIAS;ALIAS;BAPIALIAS;ALIASX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."COMPANY;COMPANY;COMPANY;COMPANYX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."LIC_TYPE;UCLASS;UCLASS;UCLASSX".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	 Schema."SUBSYSTEM;SYSTEMS".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplateMv")
	 Schema."USERGROUP;GROUPS;USERGROUP;GROUPS".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplateMv")
	 Schema."roles".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplateMvEmbd")
	 Schema."profiles".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplateMvEmbd")
	 Schema."parameters".attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplateMvEmbd")


	Schema."SUBSYSTEM".attribute.PROFILES.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."PROFILE".attribute.PROFILES.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
	Schema."__NAME__".attribute.PROFILES.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
	Schema."BAPIAKTPS".attribute.PROFILES.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."BAPIPTEXT".attribute.PROFILES.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."BAPITYPE".attribute.PROFILES.oclasses = Lazy.get("testsuite.Schema.attrTemplate")


	Schema."PARTXT".attribute.PARAMETER.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."__NAME__".attribute.PARAMETER.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
	Schema."PARID".attribute.PARAMETER.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
	Schema."PARVA".attribute.PARAMETER.oclasses = Lazy.get("testsuite.Schema.attrTemplate")

	Schema."AGR_NAME".attribute.ACTIVITYGROUPS.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
	Schema."FROM_DAT".attribute.ACTIVITYGROUPS.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."TO_DAT".attribute.ACTIVITYGROUPS.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."__NAME__".attribute.ACTIVITYGROUPS.oclasses = Lazy.get("testsuite.Schema.attreqTemplate")
	Schema."ORG_FLAG".attribute.ACTIVITYGROUPS.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."AGR_TEXT".attribute.ACTIVITYGROUPS.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
	Schema."SUBSYSTEM".attribute.ACTIVITYGROUPS.oclasses = Lazy.get("testsuite.Schema.attrTemplate")


}
 //__ACCOUNT__."LTIME;LOGONDATA;LTIME;LOGONDATAX"=new ObjectNotFoundException()
 // __ACCOUNT__."LOGONDATA;LTIME"=new ObjectNotFoundException()
 __ACCOUNT__."SUBSYSTEM;SYSTEMS"=new ObjectNotFoundException()
 __ACCOUNT__.__LOCK_OUT__="0"
__ACCOUNT__.__PASSWORD_EXPIRED__=new ObjectNotFoundException()
__ACCOUNT__.__ENABLE__=true
__ACCOUNT__."USTYP;LOGONDATA;USTYP;LOGONDATAX"="C" 
__ACCOUNT__."CLASS;LOGONDATA;CLASS;LOGONDATAX"=new ObjectNotFoundException()
__ACCOUNT__."START_MENU;DEFAULTS;START_MENU;DEFAULTSX"="start" 
__ACCOUNT__."FLOOR_P;ADDRESS;FLOOR_P;ADDRESSX"="12" 
__ACCOUNT__."COMPANY;COMPANY;COMPANY;COMPANYX"="MPHASIS" 
__ACCOUNT__."ACCNT;LOGONDATA;ACCNT;LOGONDATAX"="A123" 
//__ACCOUNT__."__NAME__"="JU30nor4" 
__ACCOUNT__."FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX"="JU30nov11" 
__ACCOUNT__."LASTNAME;ADDRESS;LASTNAME;ADDRESSX"="JU30OCT0" 
__ACCOUNT__."COMM_TYPE;ADDRESS;COMM_TYPE;ADDRESSX"="FAX" 
__ACCOUNT__."KOSTL;DEFAULTS;KOSTL;DEFAULTSX"="COSTCEN" 
__ACCOUNT__."TZONE;LOGONDATA;TZONE;LOGONDATAX"="INDIA" 
__ACCOUNT__."GLTGB;LOGONDATA;GLTGB;LOGONDATAX"=new ObjectNotFoundException()
__ACCOUNT__."USERALIAS;ALIAS;BAPIALIAS;ALIASX"=new ObjectNotFoundException()
__ACCOUNT__."FAX_NUMBER;ADDRESS;FAX_NUMBER;ADDRESSX"="123456789" 
__ACCOUNT__."PERNR"=new ObjectNotFoundException() 
__ACCOUNT__."E_MAIL;ADDRESS;E_MAIL;ADDRESSX"="E@EMAIL.COM" 
__ACCOUNT__."USERGROUP;GROUPS;USERGROUP;GROUPS"=new ObjectNotFoundException()
__ACCOUNT__."LANGU_P;ADDRESS;LANGU_P;ADDRESSX"="E" 
__ACCOUNT__."__PASSWORD__"=new GuardedString("Mphasis12".toCharArray())
__ACCOUNT__."FUNCTION;ADDRESS;FUNCTION;ADDRESSX"="FUNC" 
__ACCOUNT__."User Lock;NONE;NONE;NONE"=new ObjectNotFoundException()
__ACCOUNT__."TEL1_EXT;ADDRESS;TEL1_EXT;ADDRESSX"="125" 
__ACCOUNT__."DEPARTMENT;ADDRESS;DEPARTMENT;ADDRESSX"="DeptA" 
__ACCOUNT__."BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX"="BuildA" 
__ACCOUNT__."DCPFM;DEFAULTS;DCPFM;DEFAULTSX"="Y" 
__ACCOUNT__."LANGU;DEFAULTS;LANGU;DEFAULTSX"="E" 
__ACCOUNT__."ROOM_NO_P;ADDRESS;ROOM_NO_P;ADDRESSX"="123" 
__ACCOUNT__."LIC_TYPE;UCLASS;UCLASS;UCLASSX"=new ObjectNotFoundException()
__ACCOUNT__."TEL1_NUMBR;ADDRESS;TEL1_NUMBR;ADDRESSX"="987654321" 
__ACCOUNT__."GLTGV;LOGONDATA;GLTGV;LOGONDATAX"=new ObjectNotFoundException()
__ACCOUNT__."FAX_EXTENS;ADDRESS;FAX_EXTENS;ADDRESSX"="123" 
__ACCOUNT__."DATFM;DEFAULTS;DATFM;DEFAULTSX"="1" 
__ACCOUNT__."TITLE_P;ADDRESS;TITLE_P;ADDRESSX"="Mr."
	
	
	__ACCOUNT__.modified."__CURRENT_PASSWORD__"=new ObjectNotFoundException()
	 //__ACCOUNT__.modified."LTIME;LOGONDATA;LTIME;LOGONDATAX"=new ObjectNotFoundException()
	 // __ACCOUNT__.modified."LTIME;LOGONDATA"=new ObjectNotFoundException()
 __ACCOUNT__.modified.__LOCK_OUT__=new ObjectNotFoundException()
__ACCOUNT__.modified.__PASSWORD_EXPIRED__=new ObjectNotFoundException()
__ACCOUNT__.modified.__ENABLE__=new ObjectNotFoundException()
__ACCOUNT__.modified."USTYP;LOGONDATA;USTYP;LOGONDATAX"=new ObjectNotFoundException()
__ACCOUNT__.modified."CLASS;LOGONDATA;CLASS;LOGONDATAX"=new ObjectNotFoundException()
__ACCOUNT__.modified."START_MENU;DEFAULTS;START_MENU;DEFAULTSX"=new ObjectNotFoundException()
__ACCOUNT__.modified."FLOOR_P;ADDRESS;FLOOR_P;ADDRESSX"="14" 
__ACCOUNT__.modified."COMPANY;COMPANY;COMPANY;COMPANYX"=new ObjectNotFoundException() 
__ACCOUNT__.modified."ACCNT;LOGONDATA;ACCNT;LOGONDATAX"=new ObjectNotFoundException() 
__ACCOUNT__.modified."__NAME__"=new ObjectNotFoundException()
__ACCOUNT__.modified."FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX"="fname" 
__ACCOUNT__.modified."LASTNAME;ADDRESS;LASTNAME;ADDRESSX"="lname" 
__ACCOUNT__.modified."COMM_TYPE;ADDRESS;COMM_TYPE;ADDRESSX"=new ObjectNotFoundException()
__ACCOUNT__.modified."KOSTL;DEFAULTS;KOSTL;DEFAULTSX"=new ObjectNotFoundException() 
__ACCOUNT__.modified."TZONE;LOGONDATA;TZONE;LOGONDATAX"="IRAN" 
__ACCOUNT__.modified."GLTGB;LOGONDATA;GLTGB;LOGONDATAX"=new ObjectNotFoundException()
__ACCOUNT__.modified."USERALIAS;ALIAS;BAPIALIAS;ALIASX"=new ObjectNotFoundException()
__ACCOUNT__.modified."FAX_NUMBER;ADDRESS;FAX_NUMBER;ADDRESSX"="22222" 
__ACCOUNT__.modified."PERNR"=new ObjectNotFoundException() 
__ACCOUNT__.modified."E_MAIL;ADDRESS;E_MAIL;ADDRESSX"="E@GMAIL.COM" 
__ACCOUNT__.modified."USERGROUP;GROUPS;USERGROUP;GROUPS"=new ObjectNotFoundException()
__ACCOUNT__.modified."LANGU_P;ADDRESS;LANGU_P;ADDRESSX"="E" 
__ACCOUNT__.modified."__PASSWORD__"=new GuardedString("Summer123".toCharArray())
__ACCOUNT__.modified."FUNCTION;ADDRESS;FUNCTION;ADDRESSX"="FUNC" 
__ACCOUNT__.modified."User Lock;NONE;NONE;NONE"=new ObjectNotFoundException()
__ACCOUNT__.modified."TEL1_EXT;ADDRESS;TEL1_EXT;ADDRESSX"="1245" 
__ACCOUNT__.modified."DEPARTMENT;ADDRESS;DEPARTMENT;ADDRESSX"=new ObjectNotFoundException()
__ACCOUNT__.modified."BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX"="BuildA"
__ACCOUNT__.modified."DCPFM;DEFAULTS;DCPFM;DEFAULTSX"="X" 
__ACCOUNT__.modified."LANGU;DEFAULTS;LANGU;DEFAULTSX"=new ObjectNotFoundException() 
__ACCOUNT__.modified."ROOM_NO_P;ADDRESS;ROOM_NO_P;ADDRESSX"="12443" 
__ACCOUNT__.modified."LIC_TYPE;UCLASS;UCLASS;UCLASSX"=new ObjectNotFoundException()
__ACCOUNT__.modified."TEL1_NUMBR;ADDRESS;TEL1_NUMBR;ADDRESSX"="9666666666" 
__ACCOUNT__.modified."GLTGV;LOGONDATA;GLTGV;LOGONDATAX"=new ObjectNotFoundException()
__ACCOUNT__.modified."FAX_EXTENS;ADDRESS;FAX_EXTENS;ADDRESSX"=new ObjectNotFoundException() 
__ACCOUNT__.modified."DATFM;DEFAULTS;DATFM;DEFAULTSX"="2" 
__ACCOUNT__.modified."TITLE_P;ADDRESS;TITLE_P;ADDRESSX"="Ms."

__ACCOUNT__.modified."SUBSYSTEM;SYSTEMS" = new ObjectNotFoundException()

added."SUBSYSTEM;SYSTEMS" = new ObjectNotFoundException()
added."USERGROUP;GROUPS;USERGROUP;GROUPS" = new ObjectNotFoundException()
	
 testsuite.Update.updateToNullValue.skippedAttributes = [ 'LIC_TYPE;UCLASS;UCLASS;UCLASSX' ,'BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX','FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX', 'LASTNAME;ADDRESS;LASTNAME;ADDRESSX',  
									'E_MAIL;ADDRESS;E_MAIL;ADDRESSX', 'TITLE_P;ADDRESS;TITLE_P;ADDRESSX', 
				'LANGU_P;ADDRESS;LANGU_P;ADDRESSX', 'FUNCTION;ADDRESS;FUNCTION;ADDRESSX', 'DEPARTMENT;ADDRESS;DEPARTMENT;ADDRESSX', 'COMM_TYPE;ADDRESS;COMM_TYPE;ADDRESSX', 
				'FAX_NUMBER;ADDRESS;FAX_NUMBER;ADDRESSX', 'FAX_EXTENS;ADDRESS;FAX_EXTENS;ADDRESSX','TEL1_EXT;ADDRESS;TEL1_EXT;ADDRESSX', 'TEL1_NUMBR;ADDRESS;TEL1_NUMBR;ADDRESSX',   
				'ROOM_NO_P;ADDRESS;ROOM_NO_P;ADDRESSX', 'BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX', 'FLOOR_P;ADDRESS;FLOOR_P;ADDRESSX','ACCNT;LOGONDATA;ACCNT;LOGONDATAX','CLASS;LOGONDATA;CLASS;LOGONDATAX','GLTGV;LOGONDATA;GLTGV;LOGONDATAX', 'GLTGB;LOGONDATA;GLTGB;LOGONDATAX', 
				'TZONE;LOGONDATA;TZONE;LOGONDATAX', 'USTYP;LOGONDATA;USTYP;LOGONDATAX', 'LTIME;LOGONDATA;LTIME;LOGONDATAX',

				'USERALIAS;ALIAS;BAPIALIAS;ALIASX',  'COMPANY;COMPANY;COMPANY;COMPANYX', 'LIC_TYPE;UCLASS;UCLASS;UCLASSX','KOSTL;DEFAULTS;KOSTL;DEFAULTSX',  'DATFM;DEFAULTS;DATFM;DEFAULTSX','DCPFM;DEFAULTS;DCPFM;DEFAULTSX',   'LANGU;DEFAULTS;LANGU;DEFAULTSX',  'START_MENU;DEFAULTS;START_MENU;DEFAULTSX', ]

	strictCheck=false


//----------------------------------------------------
//Input test data - case 1 - with mandatory data
//----------------------------------------------------
account1.__NAME__="JUOCTU014"
account1.__PASSWORD__="Mphasis11"
account1.PERNR="0"
account1.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JUOCTU01"
account1.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JUOCTU01"



//----------------------------------------------------
//Input test data - case 2 - filtered user
//----------------------------------------------------
account2.__NAME__="jan30user1"
account2.__PASSWORD__="Mphasis123"
account2.PERNR="0"
account2.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="jan30user1"
account2.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="jan30user1"

//----------------------------------------------------
//Input test data - case 3 - invalid password
//----------------------------------------------------
account3.__NAME__="JU3OCTU9"
account3.__PASSWORD__="12"
account3.PERNR="0"
account3.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU3OCTU9"
account3.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU3OCTU9"

//----------------------------------------------------
//Input test data - case 4 - all field data
//----------------------------------------------------
//User ID
account4.__NAME__="JU30OCT30"

//Password
account4.__PASSWORD__="Mphasis123"

//Personnel Number
account4.PERNR="10001"

//First Name
account4.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU30OCT30"

//Last Name
account4.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU30OCT30"

//Accounting Number
account4.'ACCNT;LOGONDATA;ACCNT;LOGONDATAX'="A123"

//Building
account4.'BUILDING_P;ADDRESS;BUILDING_P;ADDRESSX'="BuildA"

//Group Name[Lookup]
account4.'CLASS;LOGONDATA;CLASS;LOGONDATAX'="OIM GROUP"

//Communication Type[Lookup]
account4.'COMM_TYPE;ADDRESS;COMM_TYPE;ADDRESSX'="FAX"

//Company[Lookup]
account4.'COMPANY;COMPANY;COMPANY;COMPANYX'="MPHASIS"

//Date Format[Lookup]
account4.'DATFM;DEFAULTS;DATFM;DEFAULTSX'="1"

//Decimal Notation[Lookup]
account4.'DCPFM;DEFAULTS;DCPFM;DEFAULTSX'="Y"

//Department
account4.'DEPARTMENT;ADDRESS;DEPARTMENT;ADDRESSX'="DeptA"

//E Mail
account4.'E_MAIL;ADDRESS;E_MAIL;ADDRESSX'="E@EMAIL.COM"

//Fax Extension
account4.'FAX_EXTENS;ADDRESS;FAX_EXTENS;ADDRESSX'="123"

//Fax Number
account4.'FAX_NUMBER;ADDRESS;FAX_NUMBER;ADDRESSX'="123456789"

//Floor
account4.'FLOOR_P;ADDRESS;FLOOR_P;ADDRESSX'="12"

//Function
account4.'FUNCTION;ADDRESS;FUNCTION;ADDRESSX'="FUNC"

//Valid Through[Date]
account4.'GLTGB;LOGONDATA;GLTGB;LOGONDATAX'="1477643857000"

//Valid From[Date]
account4.'GLTGV;LOGONDATA;GLTGV;LOGONDATAX'="1414485465000"

//Cost Center
account4.'KOSTL;DEFAULTS;KOSTL;DEFAULTSX'="COSTCEN"

//Language Communication[Lookup]
account4.'LANGU_P;ADDRESS;LANGU_P;ADDRESSX'="EN"

//Logon Language[Lookup]
account4.'LANGU;DEFAULTS;LANGU;DEFAULTSX'="EN"

//Contractual User Type[Lookup]
account4.'LIC_TYPE;UCLASS;UCLASS;UCLASSX'="55"

//Room Number
account4.'ROOM_NO_P;ADDRESS;ROOM_NO_P;ADDRESSX'="123"

//Start Menu
account4.'START_MENU;DEFAULTS;START_MENU;DEFAULTSX'="start"

//Telephone Extension
account4.'TEL1_EXT;ADDRESS;TEL1_EXT;ADDRESSX'="125"

//Telephone Number
account4.'TEL1_NUMBR;ADDRESS;TEL1_NUMBR;ADDRESSX'="987654321"

//Title[Lookup]
account4.'TITLE_P;ADDRESS;TITLE_P;ADDRESSX'="Mr."

//Time Zone[Lookup]
account4.'TZONE;LOGONDATA;TZONE;LOGONDATAX'="INDIA"

//Alias
account4.'USERALIAS;ALIAS;BAPIALIAS;ALIASX'="JU30OCT30"

//User Lock
account4.'User Lock;NONE;NONE;NONE'="0"

//User Type[Lookup]
account4.'USTYP;LOGONDATA;USTYP;LOGONDATAX'="C"

//----------------------------------------------------
//Input test data - case 5 - 
//----------------------------------------------------
account5.__NAME__="JU30APRU7"
account5.__PASSWORD__="Mphasis123"
account5.PERNR="0"
account5.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU30APRU7"
account5.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU30APRU7"




//----------------------------------------------------
//Input test data - case 6 - SAPUMUpdateUserTest
//----------------------------------------------------
//account6.__UID__="JU14TEST17SE"
//account6.__NAME__="JU14TEST17SE"
//account6.__PASSWORD__="Mphasis123"

//account6.PERNR="0"
account6.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU30APRU7FS"
account6.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU30APRU7LS"
account6.'E_MAIL;ADDRESS;E_MAIL;ADDRESSX'="JU30APRU7@sap.com"

//----------------------------------------------------
//Input test data - case 7 - SAPUMDeleteUserTest
//----------------------------------------------------
account7.__UID__="JU30APRU7";



//----------------------------------------------------
//Input test data - case 8 - testRecon_User_withFilter
//----------------------------------------------------
//"equalTo('FirstName;ADDRESS','JU30APRU6')"
//account8.query="FirstName;ADDRESS=JU30APRU6 & Last Updated>20140425010010"
account8.query="FIRSTNAME;ADDRESS=ABCTEST & Last Updated>20140826010010"

//----------------------------------------------------
//Input test data - case 9 - testRecon_User_wildcard
//----------------------------------------------------

account9.query="%JAN%"

//----------------------------------------------------
//Input test data - case 10 - testAdd_ChildData_OneRole
//----------------------------------------------------
account10{
	role1{
		TO_DAT="0"
		FROM_DAT="0"
		SUBSYSTEM="G10CLNT200"
		AGR_NAME="G10CLNT200~SAP_WF_EVERYONE"
	}
}

account10.__NAME__="JU8e6y6"
account10.__PASSWORD__="Mphasis123"
account10.PERNR="0"
account10.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8OCT15"
account10.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8OCT15"


//----------------------------------------------------
//Input test data - case 11 - testAdd_ChildData_MultipleRoles
//----------------------------------------------------
account11{
	role1{
		TO_DAT="0"
		FROM_DAT="0"
		SUBSYSTEM="G10CLNT200"
		AGR_NAME="G10CLNT200~SAP_WF_EVERYONE"
		ORG_FLAG=""
		AGR_TEXT=""
	}
	
	role2{
		TO_DAT="0"
		FROM_DAT="0"
		SUBSYSTEM="G10CLNT200"
		AGR_NAME="G10CLNT200~SAP_WF_ADMINISTRATION"
		ORG_FLAG=""
		AGR_TEXT=""
	}
}
account11.__NAME__="JU3eefd3"
account11.__PASSWORD__="Mphasis123"
account11.PERNR="0"
account11.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8MAYU3"
account11.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8MAYU3"



//----------------------------------------------------
//Input test data - case 12 - testAdd_ChildData_OneProfile
//----------------------------------------------------
account12{
	profile1{
		PROFILE="EH6CLNT300~S_BCDEV"
	}
}

account12.__NAME__="JU8MAYU4"
account12.__PASSWORD__="Mphasis123"
account12.PERNR="0"
account12.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8MAYU4"
account12.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8MAYU4"


//----------------------------------------------------
//Input test data - case 13 - testAdd_ChildData_MultipleProfiles
//----------------------------------------------------
account13{
	profile1{
		PROFILE="EH6CLNT300~S_BCDEV"
	}
	profile2{
		PROFILE="EH6CLNT300~B_ALE_ALL"
	}
}

account13.__NAME__="JU8MAYU5"
account13.__PASSWORD__="Mphasis123"
account13.PERNR="0"
account13.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8MAYU5"
account13.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8MAYU5"


//----------------------------------------------------
//Input test data - case 14 - testAdd_ChildData_OneParameter
//----------------------------------------------------
account14{
	parameter1{
		PARID="PISCOMPL"
		PARVA="test"
	}
	
}

account14.__NAME__="JU8OCT24"
account14.__PASSWORD__="Mphasis123"
account14.PERNR="0"
account14.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8OCT24"
account14.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8OCT24"



//----------------------------------------------------
//Input test data - case 15 - testAdd_ChildData_MultipleParameters
//----------------------------------------------------
account15{
	parameter1{
		PARID="/BA1/F4_EXTNO"
		PARVA="102"
	}
	parameter2{
		PARID="/BA1/F4_CCY"
		PARVA="INR"
	}
	
}

account15.__NAME__="JU8MAYU7"
account15.__PASSWORD__="Mphasis123"
account15.PERNR="0"
account15.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8MAYU7"
account15.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8MAYU7"


//----------------------------------------------------
//Input test data - case 16 - testAdd_ChildData_OneGroup
//----------------------------------------------------
account16{
	group1{
		USERGROUP="OIM GROUP"
	}
}
account16.__NAME__="JU8MAYU8"
account16.__PASSWORD__="Mphasis123"
account16.PERNR="0"
account16.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8MAYU8"
account16.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8MAYU8"

//----------------------------------------------------
//Input test data - case 17 - testAdd_ChildData_MultipleGroups
//----------------------------------------------------
account17{
	group1{
		USERGROUP="OIM_USERS"
	}
	group2{
		USERGROUP="TEST_USERS"
	}
}
account17.__NAME__="JU8MAYU9"
account17.__PASSWORD__="Mphasis123"
account17.PERNR="0"
account17.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8MAYU9"
account17.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8MAYU9"

//----------------------------------------------------
//Input test data - case 18 - testAdd_ChildData_AllChildData
//----------------------------------------------------
account18{
	group1{
		USERGROUP="OIM_USERS"
	}
	group2{
		USERGROUP="TEST_USERS"
	}
	parameter1{
		PARID="/BA1/F4_EXTNO"
		PARVA="102"
	}
	parameter2{
		PARID="/BA1/F4_CCY"
		PARVA="INR"
	}
	profile1{
		PROFILE="EH6CLNT300~S_BCDEV"
	}
	profile2{
		PROFILE="EH6CLNT300~B_ALE_ALL"
	}
	role1{
		AGR_NAME="EH6CLNT300~Z_TEST_POWL_CHIP"
		FROM_DAT="0"
		TO_DAT="0"
	}
	
	role2{
		AGR_NAME="EH6CLNT300~Z_SAP_BC_USER_ADMIN"
		FROM_DAT="0"
		TO_DAT="0"
	}
}
account18.__NAME__="JU8MAYU10"
account18.__PASSWORD__="Mphasis123"
account18.PERNR="0"
account18.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU8MAYU10"
account18.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU8MAYU10"


//------------------------------------------------------------
//Input test data - case 19 - testCreateUser_withAllChildData
//------------------------------------------------------------
account19{

	role1{
		AGR_NAME="EC1CLNT900~Z_CO_AUDIT_STAFF"
		FROM_DAT="0"
		TO_DAT="0"
	}
	role2{
		AGR_NAME="EC1CLNT900~Z_CLIENT_ADMINISTRATION"
		FROM_DAT="0"
		TO_DAT="0"
	}
	role3{
		AGR_NAME="EC1CLNT900~SAP_AUDITOR_A"
		FROM_DAT="0"
		TO_DAT="0"
	}
	profile1{
		PROFILE="EC1CLNT900~K_JOB_CUST"
	}
	profile2{
		PROFILE="EC1CLNT900~Q_ALL"
	}

	parameter1{
		PARID="/ISDFPS/MIDAT"
		PARVA="server"
	}
	parameter2{
		PARID="/ISDFPS/MODID"
		PARVA="102"
	}

	group1{
		USERGROUP="GROUP1"
	}
	group2{
		USERGROUP="ADMIN GROUP"
	}

}

account19.__NAME__="JUTEST60"
account19.__PASSWORD__="Mphasis123"
account19.PERNR="0"
account19.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JUTEST60"
account19.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JUTEST60"

//----------------------------------------------------
//Input test data - case 23 - testAdd_ChildData_MultipleGroups
//----------------------------------------------------
account23{
	group1{
		USERGROUP="OIM GROUP"
	}
	group2{
		USERGROUP="ADMIN GROUP"
	}
}
account23.__NAME__="JU27OCT38"
account23.__PASSWORD__="Mphasis123"
account23.PERNR="0"
account23.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU27OCT38"
account23.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU27OCT38"
//account23.'USERGROUP;GROUPS;USERGROUP;GROUPS'="OIM GROUP,ADMIN GROUP"

//----------------------------------------------------
//Input test data - case 24 - with mandatory data
//----------------------------------------------------
account24.__NAME__="JUOCTU81"
account24.__PASSWORD__="Mphasis123"
account24.PERNR="0"
account24.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JUOCTU81"
account24.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JUOCTU81"


//----------------------------------------------------
//Input test data - case 25 - For testContract
//----------------------------------------------------
account25.__NAME__="Sandeep14"
account25.__PASSWORD__="Mphasis11"
account25.PERNR="0"
account25.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="Sandeep14"
account25.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="Sandeep14"

//----------------------------------------------------
//Input test data - case 26 - For testContract
//----------------------------------------------------
account26.__NAME__="march04user1"
account26.__PASSWORD__="Mphasis11"
account26.PERNR="0"
account26.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="Sandeep14"
account26.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="Sandeep14"


//----------------------------------------------------
//Input test data - case 27 - For testContract
//----------------------------------------------------
account27.__NAME__="march04user2"
account27.__PASSWORD__="Mphasis11"
account27.PERNR="0"
account27.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="Sandeep14"
account27.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="Sandeep14"


//----------------------------------------------------
//Input test data - case 28 - For testContract
//----------------------------------------------------
account28.__NAME__="march04user3"
account28.__PASSWORD__="Mphasis11"
account28.PERNR="0"
account28.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="Sandeep14"
account28.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="Sandeep14"



//----------------------------------------------------
//Input test data - case 29 - For testLatestSyncToken
//----------------------------------------------------
account29.__NAME__="march04user4"
account29.__PASSWORD__="Mphasis11"
account29.PERNR="0"
account29.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="march04user4"
account29.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="march04user4"



//----------------------------------------------------
//Input test data - case 30 - For testLatestSyncToken
//----------------------------------------------------
account30.__NAME__="march04user5"
account30.__PASSWORD__="Mphasis11"
account30.PERNR="0"
account30.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="Sandeep14"
account30.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="Sandeep14"




//----------------------------------------------------
//Input test data - case 31 - For testSyncWithoutAttrsToGet
//----------------------------------------------------
account31.__NAME__="march04user6"
account31.__PASSWORD__="Mphasis11"
account31.PERNR="0"
account31.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="Sandeep14"
account31.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="Sandeep14"


//----------------------------------------------------
//Input test data - case 32 - For testSync
//----------------------------------------------------
account32.__NAME__="march04user7"
account32.__PASSWORD__="Mphasis11"
account32.PERNR="0"
account32.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="Sandeep14"
account32.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="Sandeep14"



//----------------------------------------------------
//Input test data - case 33 - For testSync
//----------------------------------------------------
account33.'FIRSTNAME;ADDRESS;FIRSTNAME;ADDRESSX'="JU30AU7FS"
account33.'LASTNAME;ADDRESS;LASTNAME;ADDRESSX'="JU30RU7LS"
account33.'E_MAIL;ADDRESS;E_MAIL;ADDRESSX'="JU30APRU7@sap.com"



