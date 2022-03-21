/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package de.schoenbeck.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for sbsp_printerconfigattr
 *  @author iDempiere (generated) 
 *  @version Release 7.1
 */
@SuppressWarnings("all")
public interface I_sbsp_printerconfigattr 
{

    /** TableName=sbsp_printerconfigattr */
    public static final String Table_Name = "sbsp_printerconfigattr";

    /** AD_Table_ID=1000017 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name PrinterAttributeValue */
    public static final String COLUMNNAME_PrinterAttributeValue = "PrinterAttributeValue";

	/** Set Printer Attribute Value.
	  * Allowed Value for Printer Attribute
	  */
	public void setPrinterAttributeValue (String PrinterAttributeValue);

	/** Get Printer Attribute Value.
	  * Allowed Value for Printer Attribute
	  */
	public String getPrinterAttributeValue();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();

    /** Column name sbsp_attributename_ID */
    public static final String COLUMNNAME_sbsp_attributename_ID = "sbsp_attributename_ID";

	/** Set Printer attributename	  */
	public void setsbsp_attributename_ID (int sbsp_attributename_ID);

	/** Get Printer attributename	  */
	public int getsbsp_attributename_ID();

	public I_sbsp_attributename getsbsp_attributename() throws RuntimeException;

    /** Column name sbsp_attributevalue_ID */
    public static final String COLUMNNAME_sbsp_attributevalue_ID = "sbsp_attributevalue_ID";

	/** Set Attributevalue	  */
	public void setsbsp_attributevalue_ID (int sbsp_attributevalue_ID);

	/** Get Attributevalue	  */
	public int getsbsp_attributevalue_ID();

	public I_sbsp_attributevalue getsbsp_attributevalue() throws RuntimeException;

    /** Column name sbsp_printerconfig_ID */
    public static final String COLUMNNAME_sbsp_printerconfig_ID = "sbsp_printerconfig_ID";

	/** Set Printer Config	  */
	public void setsbsp_printerconfig_ID (int sbsp_printerconfig_ID);

	/** Get Printer Config	  */
	public int getsbsp_printerconfig_ID();

	public I_sbsp_printerconfig getsbsp_printerconfig() throws RuntimeException;

    /** Column name sbsp_printerconfigattr_ID */
    public static final String COLUMNNAME_sbsp_printerconfigattr_ID = "sbsp_printerconfigattr_ID";

	/** Set Printer Configuration Attributes	  */
	public void setsbsp_printerconfigattr_ID (int sbsp_printerconfigattr_ID);

	/** Get Printer Configuration Attributes	  */
	public int getsbsp_printerconfigattr_ID();

    /** Column name sbsp_printerconfigattr_UU */
    public static final String COLUMNNAME_sbsp_printerconfigattr_UU = "sbsp_printerconfigattr_UU";

	/** Set sbsp_printerconfigattr_UU	  */
	public void setsbsp_printerconfigattr_UU (String sbsp_printerconfigattr_UU);

	/** Get sbsp_printerconfigattr_UU	  */
	public String getsbsp_printerconfigattr_UU();
}
