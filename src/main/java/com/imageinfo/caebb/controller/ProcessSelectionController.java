package com.imageinfo.caebb.controller;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Window;

public class ProcessSelectionController extends SelectorComposer<Window> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Wire
    private Combobox processTypeCmb;
    @Wire
    private Combobox clearingTypeCmb;

    @Listen("onClick=#processBtn")
    public void process() {
    	
        String processType = processTypeCmb.getSelectedItem().getLabel();
        String clearingType = clearingTypeCmb.getSelectedItem().getLabel();

        Session sess = Sessions.getCurrent();
        sess.setAttribute("processType", processType);
        sess.setAttribute("clearingType", clearingType);

        Executions.sendRedirect("batch_list.zul");
    }
}
