package de.schoenbeck.serverprint;

import java.io.File;
import java.util.Stack;

import javax.activation.DataSource;

import org.adempiere.webui.window.WEMailDialog;
import org.compiere.model.MUser;

public class SBSP_EMailDialog extends WEMailDialog {

	File printeddoc;
	Stack<Object> deletionStack = null;
	
	public SBSP_EMailDialog(String title, MUser from, String to, String subject, String message,
			DataSource attachment) {
		super(title, from, to, subject, message, attachment);
	}

	/**
	 * Leaves a note about the document to be attached
	 * To be used in later procedures if need be
	 * @param printeddoc - The invoice file
	 */
	@Deprecated
	public void setPrintedDoc (File printeddoc) {
		this.printeddoc = printeddoc;
	}
	
	/**
	 * Set the deletion stack to synchronize deletion
	 * @param s
	 */
	public void setDeletionStack (Stack<Object> s) {
		deletionStack = s;
	}
	
	/**
	 * Overidden instead of onClose due to access limitations
	 */
	@Override
	public void detach() {
		
		try {
			if(deletionStack != null && !deletionStack.isEmpty()) {
				//delete the top entry if it's the file
				File f = (File)deletionStack.pop();
				f.delete();
			}
		} catch (ClassCastException e) {}
		
		super.detach();
	}
}
