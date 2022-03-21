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
/** Generated Model - DO NOT CHANGE */
package de.schoenbeck.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for sbsp_printerconfigattr
 *  @author iDempiere (generated) 
 *  @version Release 7.1 - $Id$ */
public class X_sbsp_printerconfigattr extends PO implements I_sbsp_printerconfigattr, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200826L;

    /** Standard Constructor */
    public X_sbsp_printerconfigattr (Properties ctx, int sbsp_printerconfigattr_ID, String trxName)
    {
      super (ctx, sbsp_printerconfigattr_ID, trxName);
      /** if (sbsp_printerconfigattr_ID == 0)
        {
			setValue (null);
			setsbsp_attributename_ID (0);
			setsbsp_printerconfig_ID (0);
			setsbsp_printerconfigattr_ID (0);
        } */
    }

    /** Load Constructor */
    public X_sbsp_printerconfigattr (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder ("X_sbsp_printerconfigattr[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Printer Attribute Value.
		@param PrinterAttributeValue 
		Allowed Value for Printer Attribute
	  */
	public void setPrinterAttributeValue (String PrinterAttributeValue)
	{
		set_Value (COLUMNNAME_PrinterAttributeValue, PrinterAttributeValue);
	}

	/** Get Printer Attribute Value.
		@return Allowed Value for Printer Attribute
	  */
	public String getPrinterAttributeValue () 
	{
		return (String)get_Value(COLUMNNAME_PrinterAttributeValue);
	}

	/** Set Search Key.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
	}

	public I_sbsp_attributename getsbsp_attributename() throws RuntimeException
    {
		return (I_sbsp_attributename)MTable.get(getCtx(), I_sbsp_attributename.Table_Name)
			.getPO(getsbsp_attributename_ID(), get_TrxName());	}

	/** Set Printer attributename.
		@param sbsp_attributename_ID Printer attributename	  */
	public void setsbsp_attributename_ID (int sbsp_attributename_ID)
	{
		if (sbsp_attributename_ID < 1) 
			set_Value (COLUMNNAME_sbsp_attributename_ID, null);
		else 
			set_Value (COLUMNNAME_sbsp_attributename_ID, Integer.valueOf(sbsp_attributename_ID));
	}

	/** Get Printer attributename.
		@return Printer attributename	  */
	public int getsbsp_attributename_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_sbsp_attributename_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_sbsp_attributevalue getsbsp_attributevalue() throws RuntimeException
    {
		return (I_sbsp_attributevalue)MTable.get(getCtx(), I_sbsp_attributevalue.Table_Name)
			.getPO(getsbsp_attributevalue_ID(), get_TrxName());	}

	/** Set Attributevalue.
		@param sbsp_attributevalue_ID Attributevalue	  */
	public void setsbsp_attributevalue_ID (int sbsp_attributevalue_ID)
	{
		if (sbsp_attributevalue_ID < 1) 
			set_Value (COLUMNNAME_sbsp_attributevalue_ID, null);
		else 
			set_Value (COLUMNNAME_sbsp_attributevalue_ID, Integer.valueOf(sbsp_attributevalue_ID));
	}

	/** Get Attributevalue.
		@return Attributevalue	  */
	public int getsbsp_attributevalue_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_sbsp_attributevalue_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_sbsp_printerconfig getsbsp_printerconfig() throws RuntimeException
    {
		return (I_sbsp_printerconfig)MTable.get(getCtx(), I_sbsp_printerconfig.Table_Name)
			.getPO(getsbsp_printerconfig_ID(), get_TrxName());	}

	/** Set Printer Config.
		@param sbsp_printerconfig_ID Printer Config	  */
	public void setsbsp_printerconfig_ID (int sbsp_printerconfig_ID)
	{
		if (sbsp_printerconfig_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_sbsp_printerconfig_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_sbsp_printerconfig_ID, Integer.valueOf(sbsp_printerconfig_ID));
	}

	/** Get Printer Config.
		@return Printer Config	  */
	public int getsbsp_printerconfig_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_sbsp_printerconfig_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Printer Configuration Attributes.
		@param sbsp_printerconfigattr_ID Printer Configuration Attributes	  */
	public void setsbsp_printerconfigattr_ID (int sbsp_printerconfigattr_ID)
	{
		if (sbsp_printerconfigattr_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_sbsp_printerconfigattr_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_sbsp_printerconfigattr_ID, Integer.valueOf(sbsp_printerconfigattr_ID));
	}

	/** Get Printer Configuration Attributes.
		@return Printer Configuration Attributes	  */
	public int getsbsp_printerconfigattr_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_sbsp_printerconfigattr_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set sbsp_printerconfigattr_UU.
		@param sbsp_printerconfigattr_UU sbsp_printerconfigattr_UU	  */
	public void setsbsp_printerconfigattr_UU (String sbsp_printerconfigattr_UU)
	{
		set_ValueNoCheck (COLUMNNAME_sbsp_printerconfigattr_UU, sbsp_printerconfigattr_UU);
	}

	/** Get sbsp_printerconfigattr_UU.
		@return sbsp_printerconfigattr_UU	  */
	public String getsbsp_printerconfigattr_UU () 
	{
		return (String)get_Value(COLUMNNAME_sbsp_printerconfigattr_UU);
	}
}