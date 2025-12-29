package com.imageinfo.cde.controller;

import java.sql.SQLException;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.imageinfo.cde.dao.MandateVerificationDao;
import com.imageinfo.cde.model.MandateVerification;

public class MandateController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final String ON_SELECT = "onSelect";
	private static final String SELECTEDBATCH = "selectedBatch";

	@Wire
	private Borderlayout processSelection;

	@Wire
	private Vlayout batchList;

	@Wire
	private Borderlayout mandate;

	@Wire
	private Button fetchBtn;

	@Wire
	private Grid batchGrid;

	@Wire
	private Combobox utilityCode;
	@Wire
	private Combobox scanDate;
	@Wire
	private Combobox mandateType;
	@Wire
	private Combobox actiontype;

	@Wire
	Textbox mandateId;
	@Wire
	Datebox scanDatem;

	@Wire
	private Combobox utilityCodem;

	@Wire
	private Textbox utilityDescription;

	@Wire
	private Textbox categoryCode;
	@Wire
	private Textbox debtorIfscMicr;
	@Wire
	private Textbox debtorBankName;
	@Wire
	private Textbox debtorBankCode;

	@Wire
	private Textbox creditorName;

	@Wire
	private Datebox mandateDate;

	@Wire
	private Textbox debtorAccountNumber;
	@Wire
	private Combobox debtorAccountType;

	@Wire
	private Radiogroup debitType;
	@Wire
	private Doublebox amount;
	@Wire
	private Textbox reference1;
	@Wire
	private Textbox reference2;
	@Wire
	private Combobox frequency;
	@Wire
	private Combobox remarksDropdown;
	@Wire
	private Datebox startDate;
	@Wire
	private Datebox endDate;
	@Wire
	private Textbox debtorName;
	@Wire
	private Textbox phoneNumber;
	@Wire
	private Textbox email;
	@Wire
	private Textbox remarks;
	@Wire
	private Textbox dupStatus;
	@Wire
	private Textbox amendCode;
	private transient MandateVerificationDao dao = new MandateVerificationDao();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		batchList.setVisible(false);
		mandate.setVisible(false);

		utilityCode.getItems().clear();
		dao.getUtilityCodes().forEach(utilityCode::appendItem);

		utilityCode.addEventListener(ON_SELECT, e -> {
			scanDate.getItems().clear();
			mandateType.getItems().clear();
			actiontype.getItems().clear();

			if (utilityCode.getSelectedItem() != null) {
				String uCode = utilityCode.getSelectedItem().getLabel();
				dao.getScanDates(uCode).forEach(scanDate::appendItem);
			}
		});

		scanDate.addEventListener(ON_SELECT, e -> {
			mandateType.getItems().clear();
			actiontype.getItems().clear();

			if (utilityCode.getSelectedItem() != null && scanDate.getSelectedItem() != null) {
				String uCode = utilityCode.getSelectedItem().getLabel();
				String sDate = scanDate.getSelectedItem().getLabel();
				dao.getMandateTypes(uCode, sDate).forEach(mandateType::appendItem);
			}
		});

		mandateType.addEventListener(ON_SELECT, e -> {
			actiontype.getItems().clear();

			if (utilityCode.getSelectedItem() != null && scanDate.getSelectedItem() != null
					&& mandateType.getSelectedItem() != null) {
				String uCode = utilityCode.getSelectedItem().getLabel();
				String sDate = scanDate.getSelectedItem().getLabel();
				String mType = mandateType.getSelectedItem().getLabel();
				dao.getActionTypes(uCode, sDate, mType).forEach(actiontype::appendItem);
			}
		});

	}

	@Listen("onClick = #fetchBtn")
	public void onFetchClick() {
		processSelection.setVisible(false);
		batchList.setVisible(true);
		populateBatchGrid();
	}

	private void populateBatchGrid() {
		Combobox utilityCodeCb = (Combobox) processSelection.getFellow("utilityCode");
		Combobox scanDateCb = (Combobox) processSelection.getFellow("scanDate");
		Combobox mandateTypeCb = (Combobox) processSelection.getFellow("mandateType");
		Combobox actionTypeCb = (Combobox) processSelection.getFellow("actiontype");

		String utilityCode1 = utilityCodeCb.getSelectedItem() != null ? utilityCodeCb.getSelectedItem().getLabel() : "";
		String scanDate1 = scanDateCb.getSelectedItem() != null ? scanDateCb.getSelectedItem().getLabel() : "";
		String mandateType1 = mandateTypeCb.getSelectedItem() != null ? mandateTypeCb.getSelectedItem().getLabel() : "";
		String actionType = actionTypeCb.getSelectedItem() != null ? actionTypeCb.getSelectedItem().getLabel() : "";

		List<MandateVerification> batches = dao.getBatches(utilityCode1, scanDate1, mandateType1, actionType);

		Rows rows = (Rows) batchList.getFellow("batchRows");
		rows.getChildren().clear();

		for (MandateVerification batch : batches) {
			Row row = new Row();
			row.appendChild(new Label(batch.getScanDate().toString()));
			row.appendChild(new Label(batch.getBatchId()));
			row.appendChild(new Label(String.valueOf(batch.getBatchCount())));
			row.appendChild(new Label(String.valueOf(batch.getAcceptCount())));
			row.appendChild(new Label(String.valueOf(batch.getRejectedCount())));
			row.appendChild(new Label(batch.getStatus()));
			row.appendChild(new Label(batch.getProcessedBy()));

			row.addEventListener("onClick", event -> {
				batchList.setVisible(false);
				mandate.setVisible(true);

				Sessions.getCurrent().setAttribute(SELECTEDBATCH, batch);
				populateFormFromMandate();
			});

			rows.appendChild(row);
		}
	}

	@Listen("onSelect = #utilityCodem")
	public void onUtilityCodeSelect() {
		Comboitem selectedItem = utilityCodem.getSelectedItem();
		if (selectedItem != null) {
			MandateVerification selected = selectedItem.getValue();
			populateUtilityDetails(selected);
		}
	}

	private void populateUtilityDetails(MandateVerification mv) {
		if (mv != null) {
			utilityDescription.setValue(mv.getUtilityDescription() != null ? mv.getUtilityDescription() : "");
			categoryCode.setValue(mv.getCategoryCode() != null ? mv.getCategoryCode() : "");
		}
	}

	@Listen("onChange=#debtorIfscMicr; onOK=#debtorIfscMicr")
	public void onDebtorIfscMicrEntered() throws SQLException {
		MandateVerification mav = (MandateVerification) Sessions.getCurrent().getAttribute(SELECTEDBATCH);
		String ifscMicr = debtorIfscMicr.getValue();
		if (ifscMicr != null && !ifscMicr.trim().isEmpty()) {

			MandateVerification mv = dao.getBankByIfscMicr(mav.getBatchId(), ifscMicr);
			if (mv != null) {

				debtorBankCode.setValue(mv.getDebitorBankCode());
				debtorBankName.setValue(mv.getDebitorBankName());
			} else {
				Messagebox.show("Invalid IFSC/MICR Code");
				debtorBankCode.setValue("");
				debtorBankName.setValue("");
			}
		}
	}

	@Listen("onClick = #acceptBtn")
	public void onSave() {
		try {

			MandateVerification mav = (MandateVerification) Sessions.getCurrent().getAttribute(SELECTEDBATCH);
			dao.updateMandateStatusAndCounts(mav.getBatchId());
			Messagebox.show("Mandate accept successfully!");

		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error saving mandate: ");
		}
	}

	private void populateFormFromMandate() throws SQLException {
		MandateVerification mav = (MandateVerification) Sessions.getCurrent().getAttribute(SELECTEDBATCH);

		MandateVerification entry = dao.getMandateByBatch(mav.getBatchId());
		if (entry == null) {
			Messagebox.show("No data found for the selected batch");
			return;
		}

		mandateId.setValue(entry.getMandateId());

		if (entry.getScanDate() != null) {
			scanDatem.setValue(java.sql.Date.valueOf(entry.getScanDate()));
		} else {
			scanDatem.setValue(null);
		}

		utilityCodem.setValue(entry.getUtilityCode());
		utilityDescription.setValue(entry.getUtilityDescription());

		creditorName.setValue(entry.getCreditorName());
		categoryCode.setValue(entry.getCategoryCode());

		if (entry.getMandateDate() != null) {
			mandateDate.setValue(java.sql.Date.valueOf(entry.getMandateDate()));
		} else {
			mandateDate.setValue(null);
		}

		debtorAccountNumber.setValue(entry.getDebitorAcNo());
		debtorAccountType.setValue(entry.getDebitorAcType());
		debtorIfscMicr.setValue(entry.getDebitorIfsc());
		debtorBankName.setValue(entry.getDebitorBankName());
		debtorBankCode.setValue(entry.getDebitorBankCode());

		if (entry.getDebitType() != null) {
			for (Component comp : debitType.getChildren()) {

				Radio radio = (Radio) comp;
				radio.setChecked(entry.getDebitType().equals(radio.getValue()));

			}
		} else {
			for (Component comp : debitType.getChildren()) {

				((Radio) comp).setChecked(false);

			}
		}

		amount.setValue(entry.getAmount() != null ? entry.getAmount().doubleValue() : null);

		reference1.setValue(entry.getReference1());
		reference2.setValue(entry.getReference2());
		frequency.setValue(entry.getFrequency());

		startDate.setValue(entry.getStartDate() != null ? java.sql.Date.valueOf(entry.getStartDate()) : null);
		endDate.setValue(entry.getEndDate() != null ? java.sql.Date.valueOf(entry.getEndDate()) : null);

		debtorName.setValue(entry.getDebitorName());
		phoneNumber.setValue(entry.getPhoneNo());
		email.setValue(entry.getDebitorEmail());

		dupStatus.setValue(entry.getDuplicateFlag());
		amendCode.setValue(entry.getAmendCode());
	}

	@Listen("onClick=#sendBtn")
	public void sendBack() throws SQLException {

		if (remarksDropdown.getSelectedItem() == null) {
			Messagebox.show("Select the  remarks first!!");
			return;
		}

		MandateVerification entry = new MandateVerification();
		MandateVerification mav = (MandateVerification) Sessions.getCurrent().getAttribute(SELECTEDBATCH);

		entry.setSentBackRemarks(
				remarksDropdown.getSelectedItem() != null ? remarksDropdown.getSelectedItem().getValue() : null);
		dao.updateMandateStatus(mav.getBatchId(), remarksDropdown.getSelectedItem().getValue());
		Messagebox.show("Send back successfull!!");
	}

	@Listen("onClick=#backBtn")
	public void onBackClick() {
		mandate.setVisible(false);
		batchList.setVisible(true);
	}

	@Listen("onClick = #backBtn1")
	public void onBackClick1() {
		batchList.setVisible(false);
		processSelection.setVisible(true);
	}
}
