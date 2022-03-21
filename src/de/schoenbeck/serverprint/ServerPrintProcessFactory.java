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

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

public class ServerPrintProcessFactory implements IProcessFactory {

	@Override
	public ProcessCall newProcessInstance(String className) {
		// 
		if ("de.schoenbeck.serverprint.ServerPrintProcess".equalsIgnoreCase(className)) {
			return new ServerPrintProcess();
		} else if ("de.schoenbeck.serverprint.ServerPrintLookupProcess".equalsIgnoreCase(className)) {
			return new ServerPrintLookupProcess();
		}
		return null;
	}

}
