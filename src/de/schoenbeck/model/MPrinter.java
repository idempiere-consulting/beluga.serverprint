package de.schoenbeck.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MPrinter extends X_sbsp_printer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -83440759853185697L;

	public MPrinter(Properties ctx, int sbsp_printer_ID, String trxName) {
		super(ctx, sbsp_printer_ID, trxName);
		
	}

	public MPrinter(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		
	}

}
