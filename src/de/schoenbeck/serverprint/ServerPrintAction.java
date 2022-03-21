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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

import org.adempiere.webui.action.IAction;
import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.adwindow.ADWindowContent;
import org.adempiere.webui.apps.AEnv;
import org.compiere.model.GridTab;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

public class ServerPrintAction implements IAction {

	@Override
	public void execute(Object target) {
		ADWindow window = (ADWindow)target;
		ADWindowContent wcontent = window.getADWindowContent();
		GridTab tab = wcontent.getActiveGridTab();
		int ad_client_id = Env.getAD_Client_ID(Env.getCtx()); 
		int ad_user_id = Env.getAD_User_ID(Env.getCtx());
		Integer c_bpartner_id = (Integer)(tab.getValue("c_bpartner_id"));
		Integer ad_user_id_bpartner = (Integer)(tab.getValue("ad_user_id"));
		Integer ad_org_id = (Integer)(tab.getValue("ad_org_id"));
		Integer c_doctype_id = (Integer)(tab.getValue("c_doctype_id"));
		int ad_tab_id = tab.getAD_Tab_ID();
		int ad_table_id = tab.getAD_Table_ID();
		CLogger log = CLogger.getCLogger(getClass());
		
		
		
		// Create instance parameters. I e the parameters you want to send to the process.
		ProcessInfoParameter pi1 = new ProcessInfoParameter("AD_Client_ID", ad_client_id,"","","");
		ProcessInfoParameter pi2 = new ProcessInfoParameter("AD_Org_ID", ad_org_id, "","","");
		ProcessInfoParameter pi3 = new ProcessInfoParameter("ad_user_id", ad_user_id, "","","");
		ProcessInfoParameter pi4 = new ProcessInfoParameter("c_bpartner_id", c_bpartner_id, "","","");
		ProcessInfoParameter pi5 = new ProcessInfoParameter("c_doctype_id", c_doctype_id, "","","");
		ProcessInfoParameter pi6 = new ProcessInfoParameter("ad_tab_id", ad_tab_id, "","","");
		ProcessInfoParameter pi7 = new ProcessInfoParameter("ad_table_id", ad_table_id, "","","");
		ProcessInfoParameter pi8 = new ProcessInfoParameter("ad_user_id_bpartner", ad_user_id_bpartner, "","","");
		
		//Create e-mail dialog windows for each copy to be produced 
		int amount = 0;
		try {
			amount = copyAmount(ad_client_id, ad_org_id, c_bpartner_id, ad_user_id, c_doctype_id, ad_tab_id);
		} catch (Exception e) {}
		ProcessInfoParameter pi9 = new ProcessInfoParameter("email_dialog", makeMailDialogs(amount), "", "", "");
		
		
		
		// Create a process info instance. This is a composite class containing the parameters.
		ProcessInfo pi = new ProcessInfo("",1000005,0,0); //TODO: ID not needed?
		pi.setParameter(new ProcessInfoParameter[] {pi1, pi2, pi3, pi4, pi5, pi6, pi7, pi8, pi9});
		pi.setRecord_ID(tab.getRecord_ID());

		// Lookup process in the AD, in this case by value
		MProcess pr = new Query(Env.getCtx(), MProcess.Table_Name, "value=?", null)
		                        .setParameters(new Object[]{"BelugaServerprint"})
		                        .first();
		if (pr==null) {
		      log.warning("Process does not exist. ");
		      return;
		}

		// Create an instance of the actual process class.
		ServerPrintProcess process = new ServerPrintProcess();

		// Create process instance (mainly for logging/sync purpose)
		MPInstance mpi = new MPInstance(Env.getCtx(), 0, null);
		mpi.setAD_Process_ID(pr.get_ID()); 
		mpi.setRecord_ID(tab.getRecord_ID());
		mpi.save();

		// Connect the process to the process instance.
		pi.setAD_PInstance_ID(mpi.get_ID());
		
		log.info("Starting process " + pr.getName());
		boolean success = process.startProcess(Env.getCtx(), pi, null);
		wcontent.getStatusBar().setStatusLine(pi.getSummary(), !success);
		//MProcess pr = new MProcess(Env.getCtx(), 1000005, null);

		// Create an instance of the actual process class.
		//MyProcess process = new MyProcess();

		//log.info("Starting process " + pr.getName());
		//boolean result = pr.processIt(pi, null);
	}

	/**
	 * Create Dialog from outside the process and hand it over as a ProcessInfoParameter
	 * @return
	 */
	private Stack<SBSP_EMailDialog> makeMailDialogs (int amount) {
		
		Stack<SBSP_EMailDialog> rtn = new Stack<SBSP_EMailDialog>();
		
		for (int i = 0; i < amount; i++) {
			rtn.push(new SBSP_EMailDialog(Msg.getMsg(Env.getCtx(), "SendMail"),
		    				null, null, null, null, null));
			AEnv.showWindow(rtn.peek());
		}
		
		return rtn;
	}
	
	/**
	 * Preemptively determines amount of copies to be produced later
	 * @param ad_client_id 
	 * @param ad_org_id 
	 * @param c_bpartner_id 
	 * @param ad_user_id 
	 * @param c_doctype_id 
	 * @param ad_tab_id 
	 * @return
	 */
	private int copyAmount (int ad_client_id, int ad_org_id, int c_bpartner_id, int ad_user_id, int c_doctype_id, int ad_tab_id) {
		
		int rtn = 0;
		
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
			 + "			                        and ppl.ad_org_id = 0 or ppl.ad_org_id = params.ad_org_id "
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
				boolean mailtoaddress = rs.getString("mailtoaddress").equals("Y");
				boolean mailtouser = rs.getString("mailtouser").equals("Y");
				boolean senddirectly = rs.getString("senddirectly").contentEquals("Y");
				
				if (!senddirectly && (mailtoaddress || mailtouser)) //Add one if we need it later
					rtn++;
			}
		} catch (SQLException e) {}
		
		return rtn;
	}
}
