package org.identityconnectors.sap;


/**
 * Description: Represents the SAP User Management attribute mapping configuration and contains the VO attributes
 */
public class SAPUMAttributeMapBean {
    /*********************
	 * BAPI Structure Name
	 * e.g. ADDRESS
	 */	
    private String bapiStructure;
    /*********************
	 * BAPI StructureX Name
	 * e.g. ADDRESSX
	 */
    private String bapiStructureX;
    /*********************
	 * Field Name of the field in the BAPI 
	 * e.g. TEL1_NUMBR
	 */
    private String bapiFieldName;
    /*********************
	 * Field NameX of the field in the BAPI 
	 *  e.g. TEL1_NUMBR
	 */
    private String bapiFieldNameX;
    /*********************
	 * Field Name of the field in OIM 
	 */
    private String OIMfieldName;
    /*********************
	 * Field Value of the field in OIM 
	 *  e.g. 9812523562
	 */
    private String fieldValue;
    /*********************
	 * Field Type of the field in OIM 
	 *  e.g. TEXT
	 */
    private String fieldType;
    /*********************
	 * Child Table Name of the field in OIM 
	 *  e.g. UD_SAPRL
	 */  
  /*  private String childTableName;
    *//*********************
	 * Key Field used for querying the custom table 
	 *  e.g. BNAME
	 *//*  */
    private String UserIDKeyField;
    
    /*********************
	 * BAPI Field Name 
	 * e.g. BAPI_USER_GETDETAILS
	 */ 
   private String sBAPINAME; 
   
   
   /**
	 * Description: Gets the BAPI Name
	 * 
	 * @return BAPI Name
	 */
	public String getSBAPINAME() {
		return sBAPINAME;
	}

	/**
	 * Description: Sets the key User ID Field Name
	 * 
	 * @param userIDKeyField
	 *            User ID Field Name
	 */
	public void setSBAPINAME(String sbapiname) {
		sBAPINAME = sbapiname;
	}

	/**
	 * Description: Gets the key User ID Field Name
	 * 
	 * @return Key User ID Field Name
	 */
	public String getUserIDKeyField() {
		return UserIDKeyField;
	}

	
	/**
	 * Description: Sets the key User ID Field Name
	 * 
	 * @param userIDKeyField
	 *            User ID Field Name
	 */
	public void setUserIDKeyField(String userIDKeyField) {
		UserIDKeyField = userIDKeyField;
	}

	 /**
     * Description: Gets the BAPI Structure Name
     * @return  BAPI Structure Name
     */
	public String getBapiStructure() {
		return bapiStructure;
	}

	/**
     * Description: Sets the BAPI Structure Name
     * @param bapiStructure
     *             BAPI Structure Name
     */
	public void setBapiStructure(String bapiStructure) {
		this.bapiStructure = bapiStructure;
	}

	 /**
     * Description: Gets the BAPI StructureX Name
     * @return BAPI StructureX Name
     */
	public String getBapiStructureX() {
		return bapiStructureX;
	}

	/**
     * Description: Sets the BAPI StructureX Name
     * @param bapiStructureX
     *             BAPI StructureX Name
     */
	public void setBapiStructureX(String bapiStructureX) {
		this.bapiStructureX = bapiStructureX;
	}

	 /**
     * Description: Gets the BAPI Field Name
     * @return BAPI Field Name
     */
	public String getBapiFieldName() {
		return bapiFieldName;
	}

	/**
     * Description: Sets the BAPI Field Name
     * @param bapiFieldName
     *             BAPI Field Name
     */
	public void setBapiFieldName(String bapiFieldName) {
		this.bapiFieldName = bapiFieldName;
	}

	 /**
     * Description: Gets the BAPI FieldX Name
     * @return BAPI FieldX Name
     */
	public String getBapiFieldNameX() {
		return bapiFieldNameX;
	}

	/**
     * Description: Sets the BAPI FieldX Name
     * @param bapiFieldNameX
     *              BAPI FieldX Name
     */
	public void setBapiFieldNameX(String bapiFieldNameX) {
		this.bapiFieldNameX = bapiFieldNameX;
	}

	 /**
     * Description: Gets the OIM Field Name
     * @return OIM Field Name
     */
	public String getOIMfieldName() {
		return OIMfieldName;
	}

	/**
     * Description: Sets the OIM Field Name
     * @param  mfieldName
     *            OIM Field Name
     */
	public void setOIMfieldName(String mfieldName) {
		OIMfieldName = mfieldName;
	}
	 /**
     * Description: Gets the SAP Field Value
     * @return SAP Field Value
     */
	public String getFieldValue() {
		return fieldValue;
	}

	/**
     * Description: Sets the SAP Field Value
     * @param  fieldValue
     *             SAP Field Value
     */
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}
	 /**
     * Description: Gets the Field Type
     * @return Field Type
     */
	public String getFieldType() {
		return fieldType;
	}

	/**
     * Description: Sets the Field Type
     * @param  fieldType
     *              Field Type
     */
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	/* *//**
     * Description: Gets the Child Table Name
     * @return Child Table Name
     *//*
	public String getChildTableName() {
		return childTableName;
	}

	*//**
     * Description: Sets the Child Table Name
     * @param childTableName
     *              Child Table Name
     *//*
	public void setChildTableName(String childTableName) {
		this.childTableName = childTableName;
	}*/

}
