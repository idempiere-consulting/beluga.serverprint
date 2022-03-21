package de.schoenbeck.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MPrinterAttributeValue extends X_sbsp_attributevalue {

	public MPrinterAttributeValue(Properties ctx, int sbsp_attributevalue_ID, String trxName) {
		super(ctx, sbsp_attributevalue_ID, trxName);
		// 
	}

	public MPrinterAttributeValue(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// 
	}

}
