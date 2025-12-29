package com.imageinfo.maincontroller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.imageinfo.dao.UserDao;
import com.imageinfo.model.User;

public class LoginController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;
	@Wire
	private Textbox username;
	@Wire
	private Textbox password;
	@Wire
	private Button login;
	@Wire
	private Borderlayout loginPage;
	@Wire
	private Borderlayout mainPage;
	@Wire
	private Label mandateEntry;
	@Wire
	private Label mandateVerification;
	@Wire
	private Label chequeAmountEntry;
	@Wire
	private Label lbnr;
	@Wire
	private Label corporateDataEntry;

	@Wire
	private Include mainInclude;

	private transient UserDao userDao = new UserDao();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		username.setFocus(true);
	}

	@Listen("onClick= #login")
	public void handleLogin() {
		String user = username.getValue();
		String pass = password.getValue();

		if (user.isEmpty() || pass.isEmpty()) {
			Messagebox.show("Please enter username and password!", "Error", Messagebox.OK, Messagebox.EXCLAMATION);
			return;
		}

		User u = userDao.login(user, pass);
		if (u != null) {
			Session session = Sessions.getCurrent();
			session.setAttribute("loggedUser", u);

			Messagebox.show("Welcome, " + u.getFullname() + "!", "Login Successful", Messagebox.OK,
					Messagebox.INFORMATION);

			Executions.sendRedirect("/main.zul");

		} else {
			Messagebox.show("Invalid credentials!", "Login Failed", Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	@Listen("onOK=#password")
          public void onok() {
		handleLogin();
	}
}
