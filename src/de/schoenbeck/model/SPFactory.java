package de.schoenbeck.model;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;

public class SPFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		
		//maybe to lower case?
		switch (tableName) {
		case MPrinter.Table_Name: return MPrinter.class;	
		case MPrinterAttributeName.Table_Name: return MPrinterAttributeName.class;
		case MPrinterAttributeValue.Table_Name: return MPrinterAttributeValue.class;
		case MPrinterConfig.Table_Name: return MPrinterConfig.class;
		case MPrinterConfigAttr.Table_Name: return MPrinterConfigAttr.class;
		}
		return null;
	}

	/**
	 * Beluga Serverprint Model Factory
	 */
	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		
		//maybe to lower case?
		switch (tableName) {
		case MPrinter.Table_Name: return new MPrinter(Env.getCtx(), Record_ID, trxName);	
		case MPrinterAttributeName.Table_Name: return new MPrinterAttributeName(Env.getCtx(), Record_ID, trxName);
		case MPrinterAttributeValue.Table_Name: return new MPrinterAttributeValue(Env.getCtx(), Record_ID, trxName);
		case MPrinterConfig.Table_Name: return new MPrinterConfig(Env.getCtx(), Record_ID, trxName);
		case MPrinterConfigAttr.Table_Name: return new MPrinterConfigAttr(Env.getCtx(), Record_ID, trxName);
		}
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		
		//maybe to lower case?
		switch (tableName) {
		case MPrinter.Table_Name: return new MPrinter(Env.getCtx(), rs, trxName);	
		case MPrinterAttributeName.Table_Name: return new MPrinterAttributeName(Env.getCtx(), rs, trxName);
		case MPrinterAttributeValue.Table_Name: return new MPrinterAttributeValue(Env.getCtx(), rs, trxName);
		case MPrinterConfig.Table_Name: return new MPrinterConfig(Env.getCtx(), rs, trxName);
		case MPrinterConfigAttr.Table_Name: return new MPrinterConfigAttr(Env.getCtx(), rs, trxName);
		}
		return null;
	}

	
}