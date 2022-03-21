package de.schoenbeck.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MPrinterConfigAttr extends X_sbsp_printerconfigattr {

	public MPrinterConfigAttr(Properties ctx, int sbsp_printerconfigattr_ID, String trxName) {
		super(ctx, sbsp_printerconfigattr_ID, trxName);
		// 
	}

	public MPrinterConfigAttr(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// 
	}

}
