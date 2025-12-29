package com.imageinfo.maincontroller;

import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.util.Initiator;

import com.imageinfo.model.User;

public class LogOut extends SelectorComposer<Component> implements Initiator{

	private static final long serialVersionUID = 1L;

	@Override
	public void doInit(Page page, Map<String, Object> args) throws Exception {
		User u = (User) Sessions.getCurrent().getAttribute("loggedUser");
        if (u == null) {
            Executions.sendRedirect("/index.zul");
        }
	}
}
