package com.imageinfo.maincontroller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;

import com.imageinfo.dao.PageLinkDao;
import com.imageinfo.model.User;

public class NewController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    @Wire
    private Borderlayout mainPage;

    @Wire
    private Tabbox mainTab;

    @Wire
    private Navitem mandateEntry;
    @Wire
    private Navitem mandateVerification;
    @Wire
    private Navitem chequeAmountEntry;
    @Wire
    private Navitem lbnr;
    @Wire
    private Navitem corporateDataEntry;

    private transient PageLinkDao dao = new PageLinkDao();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        User u = (User) Sessions.getCurrent().getAttribute("loggedUser");
        if (u == null) {
            Executions.sendRedirect("/index.zul");
        }
    }

    
    @Listen("onClick = #mandateEntry")
    public void openMandateEntry() {
        openTab("Mandate Entry", "mandateEntry");
    }

    @Listen("onClick = #mandateVerification")
    public void openMandateVerification() {
        openTab("Mandate Verification", "mandateVerification");
    }

    @Listen("onClick = #chequeAmountEntry")
    public void openChequeAmountEntry() {
        openTab("Cheque Amount Entry & Batch Balancing", "chequeAmountEntry");
    }

    @Listen("onClick = #lbnr")
    public void openLbnrPage() {
        openTab("LBNR / RBNL Processing", "lbnr");
    }

    @Listen("onClick = #corporateDataEntry")
    public void openCorporateDataEntry() {
        openTab("Corporate Data Entry", "corporateDataEntry");
    }

    @Listen("onClick = #logout")
    public void logout() {
        Sessions.getCurrent().invalidate();
        Executions.sendRedirect("/index.zul");
    }

    private void openTab(String title, String key) {
        
        for (Component c : mainTab.getTabs().getChildren()) {
            Tab existingTab = (Tab) c;
            if (existingTab.getLabel().equals(title)) {
                existingTab.setSelected(true);
                return;
            }
        }

       
        String path = dao.getZulPath(key);
        if (path != null) {
            Tab tab = new Tab(title);
            tab.setClosable(true);
            Tabpanel panel = new Tabpanel();
            panel.setVflex("1");
            panel.setHflex("1");
            panel.setStyle("width:100%; height:595px;");

            org.zkoss.zul.Include include = new org.zkoss.zul.Include();
            include.setSrc(path);
            include.setParent(panel);
            include.setStyle("width:100%; height:100%; ");
            mainTab.getTabs().appendChild(tab);
            mainTab.getTabpanels().appendChild(panel);

            tab.setSelected(true);
        }
    }
}
