/******************************************************************************
 * Copyright (C) 2019 Martin Schönbeck Beratungen GmbH  					  *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package de.schoenbeck.serverprint;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;

import javax.activation.FileDataSource;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.TextSyntax;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
//import javax.ws.rs.core.MediaType;
import javax.print.attribute.standard.RequestingUserName;

import org.adempiere.report.jasper.ReportStarter;
import org.compiere.model.I_AD_Table;
import org.compiere.model.MArchive;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MClient;
import org.compiere.model.MMailText;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MTable;
import org.compiere.model.MUser;
import org.compiere.model.MUserMail;
import org.compiere.model.PO;
import org.compiere.model.POInfo;
import org.compiere.model.PrintInfo;
import org.compiere.print.PrintUtil;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Language;
import org.compiere.util.Msg;
import org.jfree.io.IOUtils;

import de.lohndirekt.print.IppPrintService;
import de.lohndirekt.print.IppPrintServiceLookup;
import de.lohndirekt.print.attribute.IppAttributeName;
import de.lohndirekt.print.attribute.auth.RequestingUserPassword;
import de.lohndirekt.print.attribute.ipp.jobtempl.LdMediaTray;

public class ServerPrintProcess extends SvrProcess {
	private int ad_client_id;
	private int ad_org_id;
	private Integer c_bpartner_id;
	private int ad_user_id;
	private Integer c_doctype_id;
	private int ad_tab_id;
	private int ad_table_id;
	private int record_id;
	private File printedDoc;
	private int ad_user_id_bpartner;
	private Stack<SBSP_EMailDialog> email_dialog;
	/**
	 * The Deletion Stack serves the purpose to count down
	 * how many windows are open, therefore when to delete
	 * the file, as to not make a window's attachment invalid
	 * or leave any permanent trace on the hard drive
	 */
	private Stack<Object> deletionStack = new Stack<Object>();

	@Override
	protected void prepare() {
		//TODO: Figure out how to handle NULL-parameters
		//get Parameters 
		ProcessInfoParameter[] paras = getParameter();
		for (ProcessInfoParameter p : paras) {
			String name = p.getParameterName();
			if (name.equalsIgnoreCase("AD_Client_ID")) {
				ad_client_id = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("AD_Org_ID")) {
				ad_org_id = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("C_BPartner_ID")) {
				c_bpartner_id = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("AD_User_ID")) {
				ad_user_id = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("C_DocType_ID")) {
				c_doctype_id = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("AD_Tab_ID")) {
				ad_tab_id = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("AD_Table_ID")) {
				ad_table_id = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("AD_User_ID_bpartner")) {
				ad_user_id_bpartner = p.getParameterAsInt();
			}else if (name.equalsIgnoreCase("email_dialog")) {
				email_dialog = (Stack<SBSP_EMailDialog>)p.getParameter();
			}else {
				log.severe("Unknown Parameter: " + name);
			}
		}
		record_id = getRecord_ID();
		
	}
	
	
	
	@Override
	protected String doIt() throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = 
               "with params as"
             + "     (select ? ad_client_id,"
			 + "             ? ad_org_id,"
			 + "             ? c_bpartner_id,"
			 + "             ? ad_user_id,"
			 + "             cast (? as numeric) c_doctype_id,"
			 + "             cast (? as numeric) ad_tab_id),"
			 /* subquery subprofiles looks up a list of applicable subprofiles for each present copytype
			  * this subprofiles are then sorted by priority and those with both a tab and a doctype first,
			  * then those with only a doctype and then those with only a tab. Only the first of them is
			  * delivered, so that for each copytype one or no subprofile is delivered
			  */
             + "     subprofiles as "
             + "	 (select spp.sbsp_subprintprofile_id,"
             + "	             cptype.sbsp_copytype_id "
             + "	        from sbsp_copytype cptype,"
             + "		     	 sbsp_subprintprofile spp,"
             + "				 params "
			 + "		   where cptype.ad_client_id = params.ad_client_id "
			 + "		  	 and cptype.isactive = 'Y' "
			 + "		     and spp.sbsp_subprintprofile_id in " 
			 + "				   (select sp.sbsp_subprintprofile_id " 
			 + "	           	      from sbsp_printprofile pp,"
			 + "	           		       sbsp_copy cp,"
			 + "	           		       sbsp_subprintprofile sp "
			 + "			         where sp.sbsp_printprofile_id = pp.sbsp_printprofile_id "
			 + "		        	   and sp.ad_client_id = params.ad_client_id "
			 + "		               and cp.ad_client_id = params.ad_client_id "
			 + "			           and pp.ad_client_id = params.ad_client_id "
			 + "			           and (pp.isstandardprintprofile = 'Y' "
			 + "			                or pp.sbsp_printprofile_id in "
			 + "			                    (select ppl.sbsp_printprofile_id "
			 + "			                       from sbsp_printprofilelink ppl "
			 + "			                      where ppl.ad_client_id = params.ad_client_id "
			 + "			                        and (ppl.ad_org_id = 0 or ppl.ad_org_id = params.ad_org_id) "
			 + "			                        and (ppl.c_bpartner_id = params.c_bpartner_id "
			 + "			                             or ppl.ad_user_id = params.ad_user_id)"
			 + "			                        and ppl.isactive = 'Y'))"
			 + "			           and (sp.ad_org_id = 0 or sp.ad_org_id = params.ad_org_id)"
			 + "		               and (pp.ad_org_id = 0 or pp.ad_org_id = params.ad_org_id)"
			 + "			           and (cp.ad_org_id = 0 or cp.ad_org_id = params.ad_org_id)"
			 + "			           and (sp.c_doctype_id = params.c_doctype_id or sp.ad_tab_id = params.ad_tab_id)"      
			 + "		               and cp.sbsp_subprintprofile_id = sp.sbsp_subprintprofile_id "
			 + "		               and cp.sbsp_copytype_id = cptype.sbsp_copytype_id "
			 + "		               and pp.isactive = 'Y' "
			 + "		               and cp.isactive = 'Y' "
			 + "		               and sp.isactive = 'Y' "		            
			 + "	     	          order by pp.printpriority desc,"
			 + "	                        pp.isstandardprintprofile,"
			 + "	                        pp.ad_org_id desc,"			 
			 + "	                        sp.c_doctype_id nulls last,"
			 + "	                        sp.ad_tab_id nulls last "
			 + "		              fetch first row only))"
			 /* with the previously selected subprofiles now select all copy entries, but only those
			  * which are defined for that copytype the subprofile was chosen for. Sorted by ad_process_id
			  * so that each jasper report has to be run only once as soon as it is found 
			  */
			 + "	 select cp.* "
			 + "	   from subprofiles sub,"
			 + "	        sbsp_copy cp,"
			 + "	        params "
			 + "	  where cp.sbsp_subprintprofile_id = sub.sbsp_subprintprofile_id "
			 + "	    and cp.sbsp_copytype_id = sub.sbsp_copytype_id "
			 + "	    and cp.ad_client_id = params.ad_client_id "
			 + "	    and cp.isactive = 'Y' "
			 + "	    and (cp.ad_org_id = 0 or cp.ad_org_id = params.ad_org_id)"
			 + "      order by cp.sbsp_copytype_id, cp.reportvariant";
					
			ArrayList<Copy> copies = new ArrayList<Copy>();
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, ad_client_id);
			pstmt.setInt(2, ad_org_id);
			pstmt.setInt(3, c_bpartner_id);
			pstmt.setInt(4, ad_user_id);
			pstmt.setInt(5, c_doctype_id);
			pstmt.setInt(6, ad_tab_id);
			rs = pstmt.executeQuery();
			//create Copy objects
			while (rs.next()) {
				Copy c = new Copy();
				c.ad_client_id = ad_client_id;
				c.ad_org_id = ad_org_id;
				c.ad_user_id =  ad_user_id;
				c.ad_table_id = ad_table_id;
				c.c_bpartner_id = c_bpartner_id;
				c.ad_user_id_bpartner = ad_user_id_bpartner;
				c.EMail_to = rs.getString("EMail_to");
				c.makeCcList(rs.getString("EMail_CC"));
				c.DepositPath = rs.getString("DepositPath");				
				c.ReportVariant = rs.getString("ReportVariant");
				c.ExportFileExtension = rs.getString("ExportFileExtension");
				c.mailAttPrefix = ( (rs.getString("mailattachment_prefix") != null) ? (rs.getString("mailattachment_prefix").split(",")) : new String[0] );
				c.sbsp_printconfig_id = rs.getInt("sbsp_printconfig_id");
				c.ad_process_id = rs.getInt("ad_process_id");
				c.r_mailtext_id = rs.getInt("r_mailtext_id");
				c.from_ad_user_id = rs.getInt("From_AD_User_ID");
				c.copies = (rs.getObject("copies") == null) ? (1) : rs.getInt("copies"); // if column is null take one
				c.toArchive = rs.getString("toarchive").equals("Y");
				c.mailtoaddress = rs.getString("mailtoaddress").equals("Y");
				c.mailtouser = rs.getString("mailtouser").equals("Y");
				c.senddirectly = rs.getString("senddirectly").contentEquals("Y");
				c.actualuserasfrom = rs.getString("actualuserasfrom").contentEquals("Y");
				c.email_dialogs = email_dialog;
				
				
				c.record_id = record_id;

				copies.add(c);
			}
			if (!(copies.size() > 0)) {
				if (c_doctype_id == 0) {
					throw new Exception(Msg.getMsg(Env.getCtx(), "Documentisnew"));					
				}
				else {
					throw new Exception(Msg.getMsg(Env.getCtx(), "Nothingtoprint"));
				}
			}
			//print first
			int last_ad_process_id = 0;
			String last_ReportVariant = ""; 
			String last_ExportFileExtension = "";
			for (Copy copyToPrint : copies) {
				if (copyToPrint.ad_process_id != last_ad_process_id
						|| copyToPrint.ReportVariant != last_ReportVariant
						|| copyToPrint.ExportFileExtension != last_ExportFileExtension) {
					/* remove file if present of last loop, ignore result */
					if (printedDoc != null) {
						printedDoc.delete();
					}
					last_ad_process_id = copyToPrint.ad_process_id;
					last_ReportVariant = copyToPrint.ReportVariant;
					last_ExportFileExtension = copyToPrint.ExportFileExtension;
					/* first run the jasper report and remember the result */
					printedDoc = copyToPrint.prepareReport();
					
					deletionStack.push(printedDoc);
				}
				/* now do what the copy entry requests */
				copyToPrint.execute();
			}
		} finally {
			DB.close(rs, pstmt);
            rs = null; pstmt = null;

            if (printedDoc != null)
	            try {
	            	((File)deletionStack.pop()).delete();	//delete the top entry if it's the file
	            } catch (ClassCastException e) {}			//just throw it out if it's not
		}
		return Msg.getMsg(Env.getCtx(), "Printedsuccessfully");
	}
	
	
	class Copy{
		String EMail_to;
		List<String> EMail_cc;
		String DepositPath;
		String ReportVariant;
		String ExportFileExtension;
		String[] mailAttPrefix; //prefixes of attachments to be added to mail
		int ad_client_id;
		int ad_org_id;
		int ad_user_id;
		int sbsp_printconfig_id;
		int ad_process_id;
		int record_id;
		int ad_table_id;
		int c_bpartner_id;
		int ad_user_id_bpartner;
		int from_ad_user_id;
		int r_mailtext_id;
		int copies;
		boolean toArchive;
		boolean mailtoaddress;
		boolean mailtouser;
		boolean senddirectly;
		boolean actualuserasfrom;
		Stack<SBSP_EMailDialog> email_dialogs;
		
		
		/**
		 * Start report generation
		 */
	    File prepareReport() throws Exception{

	    	ProcessInfoParameter pi1 = new ProcessInfoParameter("C_Doctype_ID", c_doctype_id, null,"","");			
	    	ProcessInfoParameter pi2 = new ProcessInfoParameter("ReportVariant", ReportVariant, null,"","");			
			
			// Create a process info instance. This is a composite class containing the parameters.
			ProcessInfo pi = new ProcessInfo("",0,0,0);
			pi.setParameter(new ProcessInfoParameter[] {pi1, pi2});
			pi.setRecord_ID(record_id);
			
			// Lookup process in the AD, in this case by value
			MProcess pr = new MProcess(Env.getCtx(), ad_process_id, null);

			// Create an instance of the actual process class.
			ReportStarter process = new ReportStarter();

			// Create process instance (mainly for logging/sync purpose)
			MPInstance mpi = new MPInstance(Env.getCtx(), 0, null);
			mpi.setAD_Process_ID(pr.get_ID()); 
			mpi.setRecord_ID(record_id);
			mpi.save();

			// Connect the process to the process instance.
			pi.setAD_PInstance_ID(mpi.get_ID());
			pi.setAD_Process_ID(ad_process_id);
			pi.setExport(true);
			if (ExportFileExtension != null)
				pi.setExportFileExtension(ExportFileExtension);	
			pi.setTable_ID(ad_table_id);

			log.info("Starting process " + pr.getName());
			/*boolean result =*/ process.startProcess(Env.getCtx(), pi, null); //Variable never used, might as well not allocate the memory
			return pi.getExportFile();			
}
	    

	    void execute() throws Exception {
	    	if (toArchive) {
	    		archive();
	    	}
	    	if (sbsp_printconfig_id > 0 && copies > 0) {
	    		print();
	    	}
	    	if (mailtouser || mailtoaddress) {
	    		sendmail();
	    	}
	    	if (DepositPath != null) {
	    		copyfile();
	    	}
	    		
	    }
	    
	    /*
	     * save printedDoc to path
	     */
	    void copyfile() throws Exception {
			
	    	Path target = Paths.get(DepositPath, printedDoc.getName());
	    	try {
	    		Files.copy(printedDoc.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
			} catch (FileNotFoundException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				throw e;
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				throw e;
	    	}
			    	
	    }
	    
	    /*
	     * save printedDoc to archive
	     */
	    void archive() throws Exception {
			
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(printedDoc);
				BufferedInputStream bis = new BufferedInputStream(fis);
				IOUtils.getInstance().copyStreams(bis, bas);
			} catch (FileNotFoundException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				throw e;
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				throw e;
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {}
				}
			}
			
			byte[] data = bas.toByteArray();  
					
			PrintInfo pinfo = new PrintInfo (printedDoc.getName(), ad_table_id, record_id, c_bpartner_id);
			MArchive archive = new MArchive (Env.getCtx(), pinfo, get_TrxName());
			archive.setBinaryData(data);
			archive.saveEx();
			    	
	    }
	    
		/**
		 * Sets Attributes, starts report generation and the print
		 * @throws Exception if process fails
		 */
	    void print() throws Exception{ 
	    	/* first retrieve the information for this printconfiguration */
	    	PrintService service = null;
			PreparedStatement pstmt = null;
			int printerconfig_id = 0; 
			ResultSet rs = null;
			try {
				String sql = 
				 "with params as" + 
				 "     (select ? ad_client_id," + 
				 "             ? ad_org_id," + 
				 "             ? sbsp_printconfig_id," + 
				 "             cast (? as numeric) ad_user_id)" + 
				 "select printer.printernameipp printername, printer.printer_uri, printer.printer_username, printer.printer_password," + 
				 "       pce.sbsp_printerconfig_id " + 
				 " from sbsp_printconfig pc," + 
				 "      sbsp_printconfig_entry pce," + 
				 "      sbsp_printer printer," + 
				 "      params " + 
				 "where pc.sbsp_printconfig_id = params.sbsp_printconfig_id " + 
				 "  and pc.isactive = 'Y' " + 
				 "  and pce.ad_client_id = params.ad_client_id " + 
				 "  and (pce.ad_org_id = 0 or pce.ad_org_id = params.ad_org_id)" + 
				 "  and pce.sbsp_printconfig_id = params.sbsp_printconfig_id " + 
				 "  and (pce.ad_user_id = params.ad_user_id or pce.isstandardprintconfig = 'Y')" + 
				 "  and pce.isactive = 'Y' " + 
				 "  and printer.sbsp_printer_id = pce.sbsp_printer_id " + 
				 "order by pce.isstandardprintconfig asc " + 
				 "fetch first row only"; 
				
				pstmt = DB.prepareStatement(sql, null);
				pstmt.setInt(1, ad_client_id);
				pstmt.setInt(2, ad_org_id);
				pstmt.setInt(3, sbsp_printconfig_id);
				pstmt.setInt(4, ad_user_id);
				rs = pstmt.executeQuery();
				
				if (rs.next()) { 
					String printername = rs.getString("printername");
					printerconfig_id = rs.getInt("sbsp_printerconfig_id");
					if (printername != null && !printername.equals("")) {
						System.getProperties().setProperty(IppPrintServiceLookup.URI_KEY, rs.getString("printer_uri")); //$NON-NLS-1$
						System.getProperties().setProperty(IppPrintServiceLookup.USERNAME_KEY, nvl(rs.getString("printer_username"))); //$NON-NLS-1$
						System.getProperties().setProperty(IppPrintServiceLookup.PASSWORD_KEY, nvl(rs.getString("printer_password"))); //$NON-NLS-1$

						PrintService[] services = new IppPrintServiceLookup().getPrintServices();
						if (services != null && services.length > 0)
							for (PrintService s : services) {
								if (s.getName().equals(printername)) {
									service = s;
									break;
								}
							}
						
					}
					//If the url is not a server or no name is given, try direct connection
					if (service == null) {
						log.info("Trying direct connection");
						URI uri = new URI(rs.getString("printer_uri"));
						service = new IppPrintService(uri);

						((IppPrintService) service).setRequestingUserName(new RequestingUserName(nvl(rs.getString("printer_username")), null));
						((IppPrintService) service).setRequestingUserPassword(new RequestingUserPassword(nvl(rs.getString("printer_password")), null));
					}
					try {
						testIppPrinter((IppPrintService) service);
					} catch (ClassCastException e) {log.fine("Direct connection...");}
					
			    	DocPrintJob printerJob = service.createPrintJob();
			    	PrintRequestAttributeSet prats = getAttributes(service, printerconfig_id);	
			    		    
					prats.add (new Copies(copies));
					Locale locale = Language.getLoginLanguage().getLocale();
					
					prats.add(new JobName(Integer.toString(record_id), locale));
					prats.add(PrintUtil.getJobPriority(1, copies, false));
					
					//prats.add(getMedia(printer, paper));
					
					//PrintService s = printerJob.getPrintService();
					//s.getSupportedDocFlavors();
					
					InputStream stream = new FileInputStream(printedDoc);
					Doc doc = new SimpleDoc(stream, DocFlavor.INPUT_STREAM.PDF,
		                new HashDocAttributeSet());
					try {
						// we are setting the doc and the job attributes
						printerJob.print(doc, prats); 
						System.out.println("printing successfull...");               
					} catch (Exception e) {//PrintException e) {	//generalized to exception for easier debug mode
						e.printStackTrace();
						throw (e);
					} 
				} 
				else {
					PreparedStatement pstmt2 = null;
					ResultSet rs2 = null;
					try {
						sql = 
						 "select name, isactive " + 
						 "from sbsp_printconfig " + 
						 "where sbsp_printconfig_id = ?"; 
						
						pstmt2 = DB.prepareStatement(sql, null);
						pstmt2.setInt(1, sbsp_printconfig_id);
						rs2 = pstmt2.executeQuery();						
						if (rs2.next()) { 
							if (rs2.getString("isactive").equals("Y")) {						
								throw new Exception(Msg.getMsg(Env.getCtx(), "NoPrintConfigEntryFound", new Object[] {rs2.getString("name")}));
							}
							else {
								throw new Exception(Msg.getMsg(Env.getCtx(), "PrintConfigInactive", new Object[] {rs2.getString("name")}));								
							}
						}
						else {
							throw new Exception(Msg.getMsg(Env.getCtx(), "PrintConfigNotFound"));							
						}
					} finally {
						DB.close(rs2, pstmt2);
						rs2 = null; pstmt2 = null;
					}
				}
			} finally {
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
			    	
		}
			
	    /*
	     * send printedDoc via mail
	     */
	    void sendmail() throws Exception {
			/* first setup mail contents */
	    	MUser m_to = null;
	    	MUser m_from = null;
	    	String to = null;
	    	StringBuilder message = null;
	    	MMailText  m_MailText = null;
	    	
			Properties props = System.getProperties();
			props.put("mail.mime.allowutf8", "true");


	    	/* set mail of ad_user_id_bpartner as To: */
    		m_to = new MUser(getCtx(), ad_user_id_bpartner, null);
	    	if (mailtouser) {
	    	    to = m_to.getEMail();
	    	}
	    	
	    	if (mailtoaddress) {
	    		if (mailtouser) {
	    			/* set fixed mail address as CC: */
		    		EMail_cc.add(EMail_to);
	    		}
	    		else {
	    			/* set fixed mail address as To: */
	    			to = EMail_to;
	    		}
	    	}
	    	if (to == null) {
				throw new Exception(Msg.getMsg(Env.getCtx(), "Noemailto"));	    		
	    	}
	    	
	    	if (r_mailtext_id > 0) {
	    		m_MailText = new MMailText (getCtx(), r_mailtext_id, get_TrxName());
	    		if (m_MailText.getR_MailText_ID() == 0)
	    			throw new Exception (Msg.getMsg(Env.getCtx(), "EMailtemplatenotfound", new Object[] {r_mailtext_id}));
	    		m_MailText.setUser(m_to);		
	    		m_MailText.setBPartner(m_to.getC_BPartner_ID());
	    		message = new StringBuilder(m_MailText.getMailText(true));	
	    	}
	    	else if (senddirectly) {
				throw new Exception(Msg.getMsg(Env.getCtx(), "Notemplatedefined"));
	    	}
	    	/* set from if configured */	    	
	    	if (actualuserasfrom || from_ad_user_id > 0) {
	    		m_from = new MUser(null, actualuserasfrom ? ad_user_id : from_ad_user_id, null);
	    	}
    		
	    	if (!senddirectly) {

	    		// Take a window from the Stack
	    		SBSP_EMailDialog email_dialog = email_dialogs.pop();
	    		
	    		email_dialog.setDeletionStack(deletionStack); //As this is vital for the frame to be removable afterwards, it is placed up here
	    		deletionStack.push(new Object()); //Push a placeholder into the deletion stack
	    		
	    		/* present prepared mail to user */
	    		// Edit existing window as prepared by the action
	    		try {
		    		String subject = m_MailText.getMailHeader();
		    		email_dialog.setFrom(m_from);
		    		email_dialog.setTo(to);
		    		for (String cc : EMail_cc) {
		    			if (!isValidEmailAddress(cc)) 
		    				throw new Exception (Msg.getMsg(Env.getCtx(), "InvalidEmailAddress", new Object[] {cc}));
		    			email_dialog.addCC(cc, true);
		    		}
		    		email_dialog.setSubject(subject);
		    		email_dialog.setMessage(message.toString());
	    		} catch (NullPointerException e) {
					log.warning("Cannot initialize mail window; some information might have been incorrect\n" + e.toString());
				}

	    		for (File a : collectAttachments()) {
	    			email_dialog.addAttachment(new FileDataSource(a), false);
	    		}

	    		
	    		email_dialog.focus();
	    		

	    	}
	    	else {
	    		//	Client Info
	    		MClient m_client = MClient.get (getCtx());
	    		if (m_client.getAD_Client_ID() == 0)
	    			throw new Exception ("Not found @AD_Client_ID@");
	    		if (m_client.getSMTPHost() == null || m_client.getSMTPHost().length() == 0)
	    			throw new Exception ("No SMTP Host found");
	    		EMail email = m_client.createEMail(m_from, to, m_MailText.getMailHeader(), message.toString());
	    		if (m_MailText.isHtml())
	    			email.setMessageHTML(m_MailText.getMailHeader(), message.toString());
	    		else
	    		{
	    			email.setSubject (m_MailText.getMailHeader());
	    			email.setMessageText (message.toString());
	    		}
	    		for (String cc : EMail_cc) {
	    			if (!email.addCc(cc)) {
	    				throw new Exception (Msg.getMsg(Env.getCtx(), "InvalidEmailAddress", new Object[] {cc}));
	    			}
	    		}

	    		for (File a : collectAttachments())
	    			email.addAttachment(a);

	    		
	    		if (!email.isValid() && !email.isValid(true))
	    		{
	    			log.warning("NOT VALID - " + email);
	    			m_to.setIsActive(false);
	    			m_to.addDescription("Invalid EMail");
	    			m_to.saveEx();
	    			return; //Boolean.FALSE;
	    		}
	    		boolean OK = EMail.SENT_OK.equals(email.send());
	    		new MUserMail(m_MailText, ad_user_id_bpartner, email).saveEx();
	    		//
	    		if (OK) {
	    			if (log.isLoggable(Level.FINE)) log.fine(m_to.getEMail());
	    		} else {
	    			log.warning("FAILURE - " + m_to.getEMail());
	    			throw new Exception(Msg.getMsg(Env.getCtx(), "EMailnotsent", new Object[] {m_to.getEMail()}));
	    		}
	    		StringBuilder msglog = new StringBuilder((OK ? "@OK@" : "@ERROR@")).append(" - ").append(m_to.getEMail());
	    		addLog(0, null, null, msglog.toString());
	    		return; 

	    	}
			    
	    	
	    }
	    
	    /**
	     * Collects all attachments to be added to the mail
	     * @return A list of files to be added 
	     */
	    private File[] collectAttachments() {
    		
    		List<File> list = new ArrayList<File>();
    		File[] rtn;
    		
    		list.add(printedDoc);
    		
    		try {
	    		PO record = getPO(ad_table_id, record_id); //Look up the PO to pick up the attachments from there
	    		MAttachmentEntry[] attachments = record.getAttachment().getEntries();
	    		if (!attachments.equals(null) && attachments.length > 0)
	    			for (MAttachmentEntry ae : attachments) //Loop over all attachments
	    				for (String prefix : mailAttPrefix) //Loop over all prefixes per attachment
	    					if (ae.getName().startsWith(prefix.trim()))
	    						list.add(ae.getFile());
	    		
    		} catch (NullPointerException e) { //As thrown by "getEntries()", when "record.getAttachment()" is null
    			log.fine("Couldn't add attachments; there might be none\n" + e);
    		}
    		
    		//Manual conversion to avoid ClassCastException
    		rtn = new File[list.size()];
    		for (int i = 0; i < rtn.length; i++)
    			rtn[i] = list.get(i);
    		
    		return rtn;
    	}
	    
	    /**
	     * Send a simple request to an Ipp Printer to check if it answers
	     * @param service - The printservice to be checked
	     */
	    private void testIppPrinter(IppPrintService service) {
			try {
				service.getSupportedAttributeCategories();
			} catch (NullPointerException e) {
				log.warning("Printer not reachable: " + service);
				throw e;
			}
		}
	    
	    private boolean isValidEmailAddress(String email) {
	           String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
	           java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
	           java.util.regex.Matcher m = p.matcher(email);
	           return m.matches();
	    }
	    
	    protected void makeCcList (String dbEntry) {
	    	this.EMail_cc = new ArrayList<>();
	    	if (dbEntry != null && !dbEntry.equals("")) //if necessary, return empty list to continue workflow
	    		for (String s : dbEntry.split(","))
	    			this.EMail_cc.add(s.trim());
	    }

} 

		private PrintRequestAttributeSet getAttributes(PrintService service, int printerconfig_id) throws Exception {		
			PrintRequestAttributeSet as = null;
				String sql = 
						"select attrname.printerattributename attributename, " +
						"       coalesce (attrvalue.printerattributevalue, attr.printerattributevalue) attributevalue " + 
						"  from sbsp_printerconfigattr attr " + 
						"  left join sbsp_attributename attrname on  attr.sbsp_attributename_id = attrname.sbsp_attributename_id " + 
						"  left outer join sbsp_attributevalue attrvalue on attr.sbsp_attributevalue_id = attrvalue.sbsp_attributevalue_id " + 
						"  where attr.sbsp_printerconfig_id = ?";
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				
				pstmt = DB.prepareStatement(sql, null);
				pstmt.setInt(1, printerconfig_id);
				
				rs = pstmt.executeQuery();
				
				as = new HashPrintRequestAttributeSet();
				
				while(rs.next()) { 
					
					
					String attValue = rs.getString("attributevalue"); 
					String attName = rs.getString("attributename");
					
					if  (rs.getString("attributename").equals("media-source")) {
						Attribute[] suppAttr = (Attribute[]) service.getSupportedAttributeValues(IppAttributeName.MEDIA_SOURCE_SUPPORTED.getCategory(), null, null);
						for (Attribute at : suppAttr) {
							if (at.toString().equals(attValue)) {
								as.add(new LdMediaTray(attValue));
								break;
							}
						}						
					} else {

						Object a = null;
						
						IppAttributeName ippattribute = IppAttributeName.get(attName);
						
						Class attrClass = ippattribute.getAttributeClass(); 

						if (TextSyntax.class.isAssignableFrom(attrClass))
							a = attrClass.getDeclaredConstructor(String.class, Locale.class).newInstance(attValue, new Locale("de_DE"));
						else if (IntegerSyntax.class.isAssignableFrom(attrClass))
							a = attrClass.getDeclaredConstructor(int.class).newInstance(Integer.parseInt(attValue));
						else if (EnumSyntax.class.isAssignableFrom(attrClass))
							a = new EnumSubtitute(rs.getString("attributename"), attValue);
						else
							log.warning("Empty attribute");
						
						
						as.add((Attribute) a);
					}
				}
				DB.close(rs, pstmt);
	            rs = null; pstmt = null;
			return as;
		}

		/**
		 * Returns the current record as PO
		 * @param table_id - The table's ID in the database
		 * @param record_id - The record's ID int he table
		 * @return The record as PO
		 */
		@SuppressWarnings("serial")
		private PO getPO (int table_id, int record_id) {
			
			PO rtn = null;
			String tablename = null;
			
			//Get the table name
			String sql = "SELECT " + I_AD_Table.COLUMNNAME_AD_Table_ID + ", " + I_AD_Table.COLUMNNAME_TableName
					+ " FROM " + I_AD_Table.Table_Name + " WHERE ad_table_id = " + table_id;
			
			PreparedStatement ps = DB.prepareStatement(sql, null);
			ResultSet rs = null;
			try {
				rs = ps.executeQuery();
				
				while (rs.next()) {
					tablename = rs.getString(2);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DB.close(rs, ps);
				rs = null;
				ps = null;
			}
			
			//Get the record
			String sql1 = "SELECT * FROM " + tablename + " WHERE " + tablename + "_id = " + record_id;
			ps = DB.prepareStatement(sql1, null); 
			final String tblname = tablename; //for use in PO
			
			try {
				rs = ps.executeQuery();
				
				while (rs.next()) {
					
					if (rs.getInt(1) == record_id) {
						
						//Make the PO
						rtn = new PO(Env.getCtx(), rs, null) {
							
							@Override
							protected POInfo initPO(Properties ctx) {
								POInfo poi = POInfo.getPOInfo (ctx, MTable.getTable_ID(tblname), get_TrxName());
								return poi;
							}
							
							@Override
							protected int get_AccessLevel() {
								return 0;
							}
						};
						
						break; //return rtn;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				log.severe(e.toString());
			} finally {
				DB.close(rs, ps);
				rs = null;
				ps = null;
			}
			
			return rtn;
		}
		
		private String nvl(String string) {
			if (string == null)
				return "";
			else
				return string;
		}

		
	    /**
		 * Searches for matching printer and tray/paper and returns it
		 * @param printername
		 * @param trayname
		 * @return Media - The corresponding Media object
		 */
//		private Media getMedia(String printer, String paper) throws Exception{
//			PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
//			PrintService service = null;
//			for (PrintService s:services) {
//				if (s.toString().equalsIgnoreCase(printer)) {
//					service = s;
//					break;
//				}
//			}
//			if (service==null) throw new Exception("Printer " + printer + " not found.");
//			
//			Media [] media = (Media [])service.getSupportedAttributeValues(Media.class, null, null);
//			for (Media m : media){
//				if (m.toString().equalsIgnoreCase(paper)){
//					return m;
//				}
//			}
//			throw new Exception("Media " + paper + " not found.");
//		}
		
	}
