package com.imageinfo.lbnrrbnl.controller;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.imageinfo.lbnrrbnl.dao.BatchDao;
import com.imageinfo.lbnrrbnl.dao.InstrumentDao;
import com.imageinfo.lbnrrbnl.model.Batch;
import com.imageinfo.lbnrrbnl.model.Instrument;

public class InstrumentController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;
	
	private static final String CLEARING_TYPE="clearingType";
	private static final String SELECTED_BATCH="selectedBatch";
	
	@Wire
	private Combobox clearingTypeCmb;
	@Wire
	private Rows batchRows;
	@Wire
	private Borderlayout processSelection;

	@Wire
	private Vlayout batchList;

	@Wire
	private Borderlayout mandate;
	@Wire
	private Intbox chequeNo;
	@Wire
	private Intbox cityCode;
	@Wire
	private Intbox bankCode;
	@Wire
	private Intbox branchCode;
	@Wire
	private Intbox baseNumber;
	@Wire
	private Intbox transactionCode;
	@Wire
	Label lblProcessedPending;
	@Wire
	Label lblDeleted;
	@Wire
	Listbox instrumentList;
	@Wire
	private Label lblMatched;
	@Wire
	Combobox deletionRemark;

	@Wire
	private Textbox remarks;

	private transient Batch batch;
	private transient Instrument instrument;
	private transient InstrumentDao instrumentDao;
	String clearingType;
	private transient BatchDao db = new BatchDao();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		processSelection.setVisible(true);
		batchList.setVisible(false);
		mandate.setVisible(false);

	}

	@Listen("onClick=#processBtn")
	public void selectCombo() {
		String clearingType1 = clearingTypeCmb.getSelectedItem().getLabel();

		Sessions.getCurrent().setAttribute(CLEARING_TYPE, clearingType1);

		processSelection.setVisible(false);
		batchList.setVisible(true);
		mandate.setVisible(false);
		refresh();
	}

	@Listen("onClick=#backBtn1")
	public void back() {
		processSelection.setVisible(true);
		batchList.setVisible(false);
		mandate.setVisible(false);
	}

	private void refresh() {
		try {
			Session sess = Sessions.getCurrent();
			String clearingType2 = (String) sess.getAttribute(CLEARING_TYPE);

			List<Batch> batches = db.fetchBatches(clearingType2);

			batchRows.getChildren().clear();
			for (Batch b : batches) {

				DecimalFormat df = new DecimalFormat("##,##,##,##,##0.00");

				Row r = new Row();
				r.appendChild(new Label(b.getBatchNumber()));
				r.appendChild(new Label(String.valueOf(b.getChequeCount())));
				r.appendChild(new Label(String.valueOf(df.format(b.getAmount()))));
				r.appendChild(new Label(String.valueOf(b.getPendingCount())));
				r.appendChild(new Label(String.valueOf(b.getProcessedCount())));
				r.appendChild(new Label(b.getStatus()));
				r.appendChild(new Label(b.getUserId()));

				r.addEventListener("onClick", evt -> {
					Sessions.getCurrent().setAttribute(SELECTED_BATCH, b);
					chequeEntryPopulate();
					processSelection.setVisible(false);
					batchList.setVisible(false);
					mandate.setVisible(true);
				});

				batchRows.appendChild(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void chequeEntryPopulate() throws SQLException {
		batch = (Batch) Sessions.getCurrent().getAttribute(SELECTED_BATCH);
		if (batch != null) {
			lblProcessedPending.setValue(String.valueOf(batch.getPendingCount()));
		} else {
			Session session = Sessions.getCurrent();
			session.removeAttribute(SELECTED_BATCH);
			session.invalidate();
		}

		instrumentDao = new InstrumentDao();

		clearingType = (String) Sessions.getCurrent().getAttribute(CLEARING_TYPE);
		instrument = instrumentDao.deleteCountDao(batch.getBatchNumber(), clearingType);
		lblDeleted.setValue(instrument.getDeleteCount());
		instrument = instrumentDao.matchedDao(batch.getBatchNumber(), clearingType);
		lblMatched.setValue(String.valueOf(instrument.getMatchedCount()));
		load();
	}

	public void load() throws SQLException {
		if (batch != null) {
			lblProcessedPending.setValue(String.valueOf(batch.getPendingCount()));

			List<Instrument> instruments = instrumentDao.fetchInstrumentsForBatch(batch.getBatchNumber(), clearingType);

			instrumentList.getItems().clear();
			for (Instrument i : instruments) {
				Listitem item = new Listitem();
				item.appendChild(new Listcell(i.getChequeNumber()));
				item.appendChild(new Listcell(i.getCityCode()));
				item.appendChild(new Listcell(i.getBankCode()));
				item.appendChild(new Listcell(i.getBranchCode()));
				item.appendChild(new Listcell(i.getBaseNumber()));
				item.appendChild(new Listcell(i.getTransactionCode()));
				item.appendChild(new Listcell(String.valueOf(i.getAmount())));
				item.appendChild(new Listcell(i.getBenefName()));
				instrumentList.appendChild(item);
			}
		}
	}

	@Listen("onClick=#matchBtn")
	public void onMatchClick() throws SQLException {
		Listitem selectedItem = instrumentList.getSelectedItem();

		if (!validation()) {
			return;
		}

		if (selectedItem == null) {
			Clients.showNotification("Select an instrument from the list!", "warning", null, null, 2000);
			return;
		}

		instrument = new Instrument();
		instrument.setChequeNumber(((Listcell) selectedItem.getChildren().get(0)).getLabel());
		instrument.setCityCode(((Listcell) selectedItem.getChildren().get(1)).getLabel());
		instrument.setBankCode(((Listcell) selectedItem.getChildren().get(2)).getLabel());
		instrument.setBranchCode(((Listcell) selectedItem.getChildren().get(3)).getLabel());
		instrument.setBaseNumber(((Listcell) selectedItem.getChildren().get(4)).getLabel());
		instrument.setTransactionCode(((Listcell) selectedItem.getChildren().get(5)).getLabel());

		boolean match = chequeNo.getText().equals(instrument.getChequeNumber())
				&& cityCode.getText().equals(instrument.getCityCode())
				&& bankCode.getText().equals(instrument.getBankCode())
				&& branchCode.getText().equals(instrument.getBranchCode())
				&& baseNumber.getText().equals(instrument.getBaseNumber())
				&& transactionCode.getText().equals(instrument.getTransactionCode());

		if (match) {
			Messagebox.show("Matched successfully!");

			instrumentDao.markAsMatched(instrument.getChequeNumber());

			instrumentList.removeChild(selectedItem);

			int currentMatched = Integer.parseInt(lblMatched.getValue());
			lblMatched.setValue(String.valueOf(currentMatched + 1));
			int currentPending = Integer.parseInt(lblProcessedPending.getValue());
			lblProcessedPending.setValue(String.valueOf(currentPending - 1));
		} else {
			Messagebox.show("Data does not match!");
		}
	}

	@Listen("onClick=#backBtn")
	public void back1() {
		processSelection.setVisible(false);
		batchList.setVisible(true);
		mandate.setVisible(false);
	}

	@Listen("onSelect=#deletionRemark")
	public void onRemarkSelect() {
		String selected = deletionRemark.getSelectedItem().getValue();

		if ("Others".equals(selected)) {
			remarks.setDisabled(false);
			remarks.setValue("");
		} else {
			remarks.setDisabled(true);
			remarks.setValue(selected);
		}
	}

	@Listen("onClick=#delete")
	public void delete() throws SQLException {
		Listitem selectedItem = instrumentList.getSelectedItem();

		String remarkValue;
		if ("Others".equals(deletionRemark.getSelectedItem().getValue())) {
			remarkValue = remarks.getValue();
		} else {
			remarkValue = deletionRemark.getSelectedItem().getValue();
		}

		String chequeNumber = ((Listcell) selectedItem.getChildren().get(0)).getLabel();
		instrumentDao.deleteInstrument(chequeNumber, remarkValue);

		instrumentList.removeChild(selectedItem);

		Messagebox.show("Cheque is deleted!!");
		int currentPending = Integer.parseInt(lblProcessedPending.getValue());
		lblProcessedPending.setValue(String.valueOf(currentPending - 1));
	}

	public boolean validation() {
		Map<Intbox, String> fields = Map.of(chequeNo, "Cheque Number", cityCode, "City Code", bankCode, "Bank Code",
				branchCode, "Branch Code", baseNumber, "Base Number", transactionCode, "Transaction Code");

		List<String> missingFields = fields.entrySet().stream()
			    .filter(entry -> isEmpty(entry.getKey()))
			    .map(Map.Entry::getValue)
			    .toList();


		if (!missingFields.isEmpty()) {
			String message = "Enter the following fields first:\n" + String.join(", ", missingFields);
			Messagebox.show(message);
			return false;
		}

		return true;
	}

	private boolean isEmpty(Intbox input) {
		return input.getValue() == null;
	}

	@Listen("onBlur=#chequeNo, #cityCode, #bankCode, #branchCode, #baseNumber, #transactionCode")
	public void validateIntbox(Event event) {
		Intbox field = (Intbox) event.getTarget();

		Clients.clearWrongValue(field);

		Map<Intbox, Integer> rules = Map.of(chequeNo, 6, cityCode, 3, bankCode, 3, branchCode, 3, baseNumber, 6,
				transactionCode, 2);

		Integer requiredLength = rules.get(field);
		if (requiredLength == null)
			return;

		String text = field.getText();

		if (text == null || text.trim().isEmpty()) {
			return;
		}

		if (text.length() < requiredLength) {
			throw new WrongValueException(field, "Must be at least " + requiredLength + " digits");
		}

		Clients.clearWrongValue(field);
	}

}
