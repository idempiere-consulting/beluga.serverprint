package de.schoenbeck.serverprint;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.PrintService;
import javax.print.attribute.Attribute;

import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import de.lohndirekt.print.IppPrintService;
import de.lohndirekt.print.IppPrintServiceLookup;
import de.lohndirekt.print.attribute.ipp.jobtempl.LdMediaTray;
import de.lohndirekt.print.attribute.ipp.printerdesc.supported.MediaSourceSupported;
import de.schoenbeck.model.I_sbsp_attributename;
import de.schoenbeck.model.I_sbsp_attributevalue;
import de.schoenbeck.model.I_sbsp_printer;
import de.schoenbeck.model.MPrinterAttributeName;
import de.schoenbeck.model.MPrinterAttributeValue;


@SuppressWarnings("rawtypes")
public class ServerPrintLookupProcess extends SvrProcess {

	private String printerNameIPP; //Name of printer
	private String printer_uri; //URI of printer or print server
	private String printerusername, printerpassword; //Credentials if necessary
	
	private int printerID;
	
	
	public ServerPrintLookupProcess() {

	}

	@Override
	protected void prepare() {
		// Grab process parameters
		printerID = getRecord_ID();
		
		String sql = "SELECT * FROM " + I_sbsp_printer.Table_Name + " WHERE " + I_sbsp_printer.COLUMNNAME_sbsp_printer_ID + " = " + printerID;
		PreparedStatement ps = DB.prepareStatement(sql, null);
		ResultSet rs = null;
		try {
			rs = ps.executeQuery();
			
			rs.next();
			printerNameIPP = rs.getString(I_sbsp_printer.COLUMNNAME_PrinterNameIpp);
			printer_uri = rs.getString(I_sbsp_printer.COLUMNNAME_printer_uri);
			printerusername = rs.getString(I_sbsp_printer.COLUMNNAME_printer_username);
			printerpassword = rs.getString(I_sbsp_printer.COLUMNNAME_printer_password);
		} catch (SQLException e) {
			log.severe(e.toString());
		} finally {
			DB.close(rs, ps);
		}
	}

	@Override
	protected String doIt() throws Exception {
		
		//Find printer
		PrintService service = findPrinter();
//		if (service == null)
//			throw new Exception("No such printer in that location");
		//Now unnecessary because findPrinter checks and throws Exception itself	
		
		//Find supported attributes
		Class<?>[] classes = service.getSupportedAttributeCategories();
		if (classes == null)
			throw new Exception("Something went wrong; no supported attribute categories");
		
		//Combine Categories and permitted values
		HashMap<Class, Object> attributeValues = findAttributeValues(service, classes);
		if (attributeValues == null || attributeValues.isEmpty())
			throw new Exception("Something went wrong; no supported attribute values");
		
		//Convert to <String, Object>; being <IppName, Attributevalues>
		HashMap<String, Object> ippAttVal = convMap(attributeValues);
		
		//Write entries
		writeNewEntries(service, ippAttVal); 
		
		
		return "Successfully gathered attributes"; //TODO: Soft code message for translation
	}

	
	
	/**
	 * Finds the desired printer by name
	 * @return The desired printer; NULL if it doesn't exist
	 * @throws Exception 
	 */
	private PrintService findPrinter () throws Exception {
		
		PrintService rtnPS = null; //PrintService to be returned
		PrintService ps; //Used when the URI is not a server
		
		//try server connection if a printer name was given
		if (printerNameIPP != null && !printerNameIPP.equalsIgnoreCase("")) {
			System.getProperties().setProperty(IppPrintServiceLookup.URI_KEY, (printer_uri==null)?"":printer_uri);
			System.getProperties().setProperty(IppPrintServiceLookup.USERNAME_KEY, (printerusername==null)?"":printerusername); //Nullpointerexception here
			System.getProperties().setProperty(IppPrintServiceLookup.PASSWORD_KEY, (printerpassword==null)?"":printerpassword);
			
			PrintService[] serv = new IppPrintServiceLookup().getPrintServices();
			if (serv != null && serv.length > 0)
		        for (PrintService s : serv)
		        	if (s.getName().equalsIgnoreCase(printerNameIPP)) {
		        		rtnPS = s; 
		        		break;
		        	}
	        
		//try direct connection otherwise
		} else {
			try {
	    		ps = new IppPrintService(new URI(printer_uri)); //Split for debugging
	    		rtnPS = ps;
	    	} catch (Exception e) {
	    		ps = null; rtnPS = null;
	    	}
		}

		if (rtnPS == null)
        	throw new Exception("No printers in that location");
        
		return rtnPS;
	}
	
	
	
	/**
	 * Creates a map of all attributes and their respective values for easier lookup
	 * @param service The target printservice
	 * @param classes A list of allowed attribute categories
	 * @return a hashmap containing categories and supported values
	 */
	private HashMap<Class, Object> findAttributeValues (PrintService service, Class[] classes) {
		
		HashMap<Class, Object> attributeValues = new HashMap<Class, Object>();
		for (Class c : classes) {
			if(c.equals(LdMediaTray.class))
				attributeValues.put(c, service.getSupportedAttributeValues(MediaSourceSupported.class, null, null));
			else
				attributeValues.put(c, service.getSupportedAttributeValues(c, null, null));
		}
		
		return attributeValues;
	}
	
	
	private HashMap<String, Object> convMap (HashMap<Class, Object> map) {
		
		HashMap<String, Object> rtn = new HashMap<String, Object>();
		
		for (Map.Entry e : map.entrySet()) {
			
			Attribute[] temp;
			
			if (e.getValue().getClass().isArray())
				temp = (Attribute[])(e.getValue());
			else
				temp = new Attribute[] {(Attribute)e.getValue()};
			
			
			if (temp.length == 0)
				log.severe("Length 0 on: " + e.getKey() + "");
			else {
				String cat = (temp[0].getName().contains("-supported"))?temp[0].getName().substring(0, temp[0].getName().length() - "-supported".length()):temp[0].getName();
				rtn.put(cat, e.getValue());
			}
			
		}
		
		return rtn;
	}
	
	/**
	 * Makes new entries
	 * @param service
	 * @param attributeValues
	 */
	private void writeNewEntries (PrintService service, HashMap<String, Object> attributeValues) throws Exception {
		
		List<String> exCatNames = existingCategories(); //Existing categories
		List<String> exAttNames = existingAttributes(); //Existing attributes
		
		//Write new entries
		for (Map.Entry<String, Object> entry : attributeValues.entrySet()) {
			
			if (entry.getValue() == null) //Unnecessary without applicable values
				continue;
			
			MPrinterAttributeName category = new MPrinterAttributeName(getCtx(), 0, get_TrxName());
			
			String c = findCat(entry.getKey(), exCatNames);
			
			if (c == null) {
				
				
				//set all non-null values without defaults
				category.setName(entry.getKey());
				category.setsbsp_printer_ID(printerID);
				
				category.setPrinterAttributeName(entry.getKey());
				
				//save category after setting
				category.saveEx();
			} else {
				category.setsbsp_attributename_ID(Integer.parseInt(c.split("::")[0]));
			}
			
			
			//do values afterwards
			MPrinterAttributeValue val = null;
			Object[] attr = null;
			
			
			if (entry.getValue().getClass().isArray()) {
				attr = (Object[])entry.getValue();
			} else {
				attr = new Object[] {entry.getValue()};
			}
			
			for (Object o : attr) {
				
				if(exAttNames.contains(o.toString()))
					continue;
				
				val = new MPrinterAttributeValue(getCtx(), 0, get_TrxName());
				val.setName(o.toString());
				val.setPrinterAttributeValue(o.toString());
				val.setsbsp_attributename_ID(category.getsbsp_attributename_ID());
				
				val.saveEx();
				
				val = null;
			}

			category = null;
			
		} 
	}
	
	
	/**
	 * Finds a category by name in a list of categories
	 * @param cat The name of the desired category
	 * @param catList A list of IDs and categories in form 'id:name:
	 * @return ID and Name in form 'id::name'; NULL if not contained
	 */
	private String findCat (String cat, List<String> catList) {
		
		String rtn = null;
		
		for (String s : catList) {
			if (s.split("::")[1].equalsIgnoreCase(cat) || (cat.equalsIgnoreCase(LdMediaTray.class.getName()) && s.split("::")[1].equalsIgnoreCase("media-source")))
				rtn = s;
		}
		
		return rtn;
	}
	
	
	private List<String> existingCategories () {
		
		List<String> rtn = new ArrayList<String>();
		
		String sql = "SELECT " + I_sbsp_attributename.COLUMNNAME_sbsp_attributename_ID + "," + I_sbsp_attributename.COLUMNNAME_PrinterAttributeName + "," + I_sbsp_attributename.COLUMNNAME_sbsp_printer_ID + " FROM " + I_sbsp_attributename.Table_Name;
		PreparedStatement ps = DB.prepareStatement(sql, null);
		ResultSet rs = null;
		
		try {
			rs = ps.executeQuery();
			while(rs.next()) {
				if (rs.getInt(I_sbsp_attributename.COLUMNNAME_sbsp_printer_ID) == printerID)
					rtn.add(rs.getInt(I_sbsp_attributename.COLUMNNAME_sbsp_attributename_ID) + "::" + rs.getString(I_sbsp_attributename.COLUMNNAME_PrinterAttributeName));
			}
		} catch (SQLException e) {
			log.severe(e.toString());
		} finally {
			DB.close(rs, ps);
			rs = null;
			ps = null;
		} 
		
		return rtn;
	}
	
	
	/**
	 * Lists names of existing attributes and values
	 * @return ArrayList of type string, containing existing attributes
	 */
	private List<String> existingAttributes () {
		
		List<String> rtn = new ArrayList<String>();
		
		String sql = "SELECT " + I_sbsp_attributevalue.COLUMNNAME_PrinterAttributeValue + " FROM " + I_sbsp_attributevalue.Table_Name;
		PreparedStatement ps = DB.prepareStatement(sql, null);
		ResultSet rs = null;
		
		try {
			rs = ps.executeQuery();
			while (rs.next()) {
				rtn.add(rs.getString(I_sbsp_attributevalue.COLUMNNAME_PrinterAttributeValue));
			}
		} catch (SQLException e) {
			log.severe(e.toString());
		} finally {
			DB.close(rs, ps);
			ps = null;
			rs = null;
		}
		
		return rtn;
	}
	
}
