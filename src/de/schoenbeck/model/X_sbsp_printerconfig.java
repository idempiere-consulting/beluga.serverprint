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

/** Generated Model for sbsp_printerconfig
 *  @author iDempiere (generated) 
 *  @version Release 7.1 - $Id$ */
public class X_sbsp_printerconfig extends PO implements I_sbsp_printerconfig, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200826L;

    /** Standard Constructor */
    public X_sbsp_printerconfig (Properties ctx, int sbsp_printerconfig_ID, String trxName)
    {
      super (ctx, sbsp_printerconfig_ID, trxName);
      /** if (sbsp_printerconfig_ID == 0)
        {
			setName (null);
			setValue (null);
			setsbsp_printer_ID (0);
			setsbsp_printerconfig_ID (0);
        } */
    }

    /** Load Constructor */
    public X_sbsp_printerconfig (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_sbsp_printerconfig[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
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

	public I_sbsp_printer getsbsp_printer() throws RuntimeException
    {
		return (I_sbsp_printer)MTable.get(getCtx(), I_sbsp_printer.Table_Name)
			.getPO(getsbsp_printer_ID(), get_TrxName());	}

	/** Set Printer.
		@param sbsp_printer_ID Printer	  */
	public void setsbsp_printer_ID (int sbsp_printer_ID)
	{
		if (sbsp_printer_ID < 1) 
			set_Value (COLUMNNAME_sbsp_printer_ID, null);
		else 
			set_Value (COLUMNNAME_sbsp_printer_ID, Integer.valueOf(sbsp_printer_ID));
	}

	/** Get Printer.
		@return Printer	  */
	public int getsbsp_printer_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_sbsp_printer_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

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

	/** Set sbsp_printerconfig_UU.
		@param sbsp_printerconfig_UU sbsp_printerconfig_UU	  */
	public void setsbsp_printerconfig_UU (String sbsp_printerconfig_UU)
	{
		set_ValueNoCheck (COLUMNNAME_sbsp_printerconfig_UU, sbsp_printerconfig_UU);
	}

	/** Get sbsp_printerconfig_UU.
		@return sbsp_printerconfig_UU	  */
	public String getsbsp_printerconfig_UU () 
	{
		return (String)get_Value(COLUMNNAME_sbsp_printerconfig_UU);
	}
}