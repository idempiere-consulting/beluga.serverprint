package de.schoenbeck.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MPrinterConfig extends X_sbsp_printerconfig {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 703767386721369044L;

	
	public MPrinterConfig(Properties ctx, int sbsp_printerconfig_ID, String trxName) {
		super(ctx, sbsp_printerconfig_ID, trxName);
		
	}

	
	public MPrinterConfig(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		
	}
}
