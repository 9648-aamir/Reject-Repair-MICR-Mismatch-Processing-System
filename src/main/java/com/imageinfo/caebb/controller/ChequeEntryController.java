package com.imageinfo.caebb.controller;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.imageinfo.caebb.dao.BatchDao;
import com.imageinfo.caebb.dao.InstrumentDao;
import com.imageinfo.caebb.model.Batch;
import com.imageinfo.caebb.model.Instrument;

public class ChequeEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final String SELECTEDBATCH = "selectedBatch";
	@Wire
	private Combobox processTypeCmb;
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
	private Intbox chequeId;
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
	private Textbox remarks;
	@Wire
	private Button saveBtn;

	@Wire
	private Label lblBatchNumber;
	@Wire
	private Label lblBatchCount;
	@Wire
	private Label lblProcessedPending;
	@Wire
	private Button nextBtn;
	@Wire
	private Image myImage;

	@Wire
	private Textbox date;

	@Wire
	private Intbox amount;
	@Wire
	private Intbox batchAmount1;

	@Wire
	Combobox deletionRemark;

	@Wire
	private Label batchCount;
	@Wire
	private Label scanCount;
	@Wire
	private Label difference;
	@Wire
	private Label batchAmount;
	@Wire
	private Label chequeAmount;
	@Wire
	private Label difference1;

	private transient List<Instrument> instruments;

	private int currentIndex;

	private transient Batch batch;
	private transient InstrumentDao instrumentDao;
	float allChequeAmt;
	private static final String DIFFERENCE_PREFIX = "Difference: ";
	private static final String OTHERS_PREFIX = "Others";

	DecimalFormat df = new DecimalFormat("##,##,##,##,##0.00");
	private transient BatchDao db = new BatchDao();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		processSelection.setVisible(true);
		batchList.setVisible(false);
		mandate.setVisible(false);
	}

	@Listen("onClick=#processBtn")
	public void process() {

		String processType = processTypeCmb.getSelectedItem().getLabel();
		String clearingType = clearingTypeCmb.getSelectedItem().getLabel();

		Session sess = Sessions.getCurrent();
		sess.setAttribute("processType", processType);
		sess.setAttribute("clearingType", clearingType);
		refresh();
		processSelection.setVisible(false);
		batchList.setVisible(true);
		mandate.setVisible(false);
	}

	@Listen("onClick=#backBtn")
	public void back() {
		processSelection.setVisible(true);
		batchList.setVisible(false);
		mandate.setVisible(false);
	}

	@Listen("onClick=#backBtn1")
	public void back1() {
		processSelection.setVisible(false);
		batchList.setVisible(true);
		mandate.setVisible(false);
	}

	private void refresh() {
		try {
			Session sess = Sessions.getCurrent();
			String processType = (String) sess.getAttribute("processType");
			String clearingType = (String) sess.getAttribute("clearingType");

			List<Batch> batches = db.fetchBatches(processType, clearingType);

			DecimalFormat df1 = new DecimalFormat("##,##,##,##,##0.00");
			batchRows.getChildren().clear();
			for (Batch b : batches) {
				Row r = new Row();
				r.appendChild(new Label(b.getBatchNumber()));
				r.appendChild(new Label(String.valueOf(b.getChequeCount())));
				r.appendChild(new Label(String.valueOf(df1.format(b.getAmount()))));
				r.appendChild(new Label(String.valueOf(b.getPendingCount())));
				r.appendChild(new Label(String.valueOf(b.getProcessedCount())));
				r.appendChild(new Label(b.getStatus()));
				r.appendChild(new Label(b.getUserId()));

				Batch scanStats = db.getBatchScanCounts();
				b.setScanCount(scanStats.getScanCount());
				b.setBatchCount(scanStats.getBatchCount());
				b.setDifference(scanStats.getDifference());

				r.addEventListener("onClick", evt -> {
					Sessions.getCurrent().setAttribute(SELECTEDBATCH, b);
					processSelection.setVisible(false);
					batchList.setVisible(false);
					mandate.setVisible(true);
					chequeEntryPopulate();
				});

				batchRows.appendChild(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void chequeEntryPopulate() throws SQLException {
		batch = (Batch) Sessions.getCurrent().getAttribute(SELECTEDBATCH);

		if (batch != null) {
			lblBatchNumber.setValue(batch.getBatchNumber());
			lblBatchCount.setValue(String.valueOf(batch.getBatchCount()));
			lblProcessedPending.setValue(batch.getProcessedCount() + "/" + batch.getPendingCount());
		} else {
			Session session = Sessions.getCurrent();
			session.removeAttribute(SELECTEDBATCH);
			session.invalidate();
		}

		Instrument cheque = new Instrument();

		instrumentDao = new InstrumentDao();
		instrumentDao.fetchInstrumentsForBatch(batch.getBatchNumber(), cheque);

		chequeId.setValue(Integer.parseInt(String.valueOf(cheque.getChequeId())));
		chequeNo.setValue(Integer.parseInt(cheque.getChequeNumber()));
		cityCode.setValue(Integer.parseInt(cheque.getCityCode()));
		bankCode.setValue(Integer.parseInt(cheque.getBankCode()));
		branchCode.setValue(Integer.parseInt(cheque.getBankCode()));
		baseNumber.setValue(Integer.parseInt(cheque.getBaseNumber()));
		transactionCode.setValue(Integer.parseInt(cheque.getTransactionCode()));

		updateBatchScanCounts();

		float batchAmt = batch.getAmount();

		batchAmount.setValue("Batch Amount: " + batchAmt);

		instruments = instrumentDao.fetchPendingInstrumentsForBatch(batch.getBatchNumber());
		currentIndex = 0;
		if (!instruments.isEmpty()) {
			showInstrument(instruments.get(currentIndex));
		}
		allChequeAmt = instrumentDao.getSumOfInstrumentAmountByBatchNumber(batch.getBatchNumber());

		float diff = batchAmt - allChequeAmt;
		chequeAmount.setValue(String.valueOf(df.format(allChequeAmt)));
		difference1.setValue(DIFFERENCE_PREFIX + df.format(diff));

	}

	@Listen("onOK=#amount")
	public void saveDateAmount() throws ParseException, WrongValueException, NumberFormatException, SQLException {

		float batchAmt = batch.getAmount();

		Integer enteredAmt = amount.getValue();
		if (enteredAmt == null)
			enteredAmt = 0;

		float prevChequeAmt = 0f;
		if (chequeAmount.getValue() != null && !chequeAmount.getValue().isEmpty()) {
			try {
				prevChequeAmt = Float.parseFloat(chequeAmount.getValue());
			} catch (NumberFormatException e) {
				prevChequeAmt = 0f;
			}
		}

		float calAmt = prevChequeAmt + enteredAmt;

		float diff = batchAmt - calAmt;

		chequeAmount.setValue(String.valueOf(df.format(calAmt)));
		difference1.setValue(DIFFERENCE_PREFIX + diff);

		if (diff <= 0) {
			Messagebox.show("Batch Amount is less. Cannot save cheque!");
			return;
		}

		String dateFormatPattern = "dd-MM-yyyy";

		SimpleDateFormat formatter = new SimpleDateFormat(dateFormatPattern);

		java.util.Date enteredDate;

		try {
			enteredDate = formatter.parse(date.getValue());
		} catch (ParseException e) {
			Messagebox.show("Date format is not correct. Please use dd-MM-yyyy.", "Error", Messagebox.OK,
					Messagebox.ERROR);
			return;
		}

		java.util.Date today = new java.util.Date();

		int chqValidationMonths = 3;

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.MONTH, -chqValidationMonths);
		java.util.Date staleDate = cal.getTime();

		if (enteredDate.before(staleDate) || enteredDate.equals(staleDate)) {

			Messagebox.show("The Cheque Date is Stale. Please delete the cheque entry!");
			return;
		}

		if (enteredDate.after(today)) {
			Messagebox.show("The Cheque Date is Post-dated. Please check holiday rules!");
			return;
		}

		if (diff <= 0) {
			instrumentDao.saveInstrumentEntry(chequeId.getValue(), enteredDate, amount.getValue().floatValue());
			batch.setProcessedCount(batch.getProcessedCount() + 1);
			batch.setPendingCount(batch.getPendingCount() - 1);
			Messagebox.show("Cheque saved successfully!");
		} else {
			Messagebox.show("Force balance the batch amount!!!");
		}

	}

	@Listen("onSelect=#deletionRemark")
	public void onRemarkSelect() {
		String selected = deletionRemark.getSelectedItem().getValue();

		if (OTHERS_PREFIX.equals(selected)) {
			remarks.setDisabled(false);
			remarks.setPlaceholder("Enter the remark");
			remarks.setValue("");
		} else {
			remarks.setDisabled(true);
			remarks.setValue(selected);
		}
	}

	@Listen("onClick=#delete")
	public void delete() throws SQLException {
		String remarkValue;
		if (OTHERS_PREFIX.equals(deletionRemark.getSelectedItem().getValue())) {
			remarkValue = remarks.getValue();
		} else {
			remarkValue = deletionRemark.getSelectedItem().getValue();
		}

		instrumentDao.deleteInstrument(chequeId.getValue(), remarkValue);
		Messagebox.show("Cheque is deleted!!");
	}

	@Listen("onClick=#deleteAll")
	public void deleteAll() throws SQLException {
		String remarkValue;
		if (OTHERS_PREFIX.equals(deletionRemark.getSelectedItem().getValue())) {
			remarkValue = remarks.getValue();
		} else {
			remarkValue = deletionRemark.getSelectedItem().getValue();
		}

		instrumentDao.deleteAllInstrumentsByBatch(batch.getBatchNumber(), remarkValue);
		Messagebox.show("Batch is deleted!!");
	}

	public void updateBatchScanCounts() {

		batchCount.setValue("Batch Count: " + batch.getBatchCount());
		scanCount.setValue("Scan Count: " + batch.getScanCount());
		difference.setValue(DIFFERENCE_PREFIX + batch.getDifference());
	}

	@Listen("onClick=#nextBtn")
	public void showNextCheque() {
		if (instruments == null || instruments.isEmpty()) {
			Messagebox.show("No pending cheques found!");
			return;
		}

		if (currentIndex < instruments.size() - 1) {
			currentIndex++;
			showInstrument(instruments.get(currentIndex));
		} else {
			Messagebox.show("You have reached the last pending cheque!");
		}
	}

	private void showInstrument(Instrument ins) {

		chequeId.setValue(ins.getChequeId().intValue());
		chequeNo.setValue(Integer.parseInt(ins.getChequeNumber()));
		cityCode.setValue(Integer.parseInt(ins.getCityCode()));
		bankCode.setValue(Integer.parseInt(ins.getBankCode()));
		branchCode.setValue(Integer.parseInt(ins.getBranchCode()));
		baseNumber.setValue(Integer.parseInt(ins.getBaseNumber()));
		transactionCode.setValue(Integer.parseInt(ins.getTransactionCode()));
	}

	private int rotation = 0;

	@Listen("onClick=#rotateRightBtn")
	public void rotateRight() {
		rotation += 90;
		updateRotation();
	}

	@Listen("onClick=#rotateLeftBtn")
	public void rotateLeft() {
		rotation -= 90;
		updateRotation();
	}

	private void updateRotation() {
		myImage.setStyle("object-fit:contain; display:block; margin:auto; transform:rotate(" + rotation + "deg);");
	}

	@Wire
	Button forceSaveBtn;

	@Listen("onClick=#forceBalance")
	public void forceBalance() {

		forceSaveBtn.setDisabled(false);
		batchAmount1.setDisabled(false);
		batchAmount1.setPlaceholder("Enter the amount");
	}

	@Listen("onClick=#forceSaveBtn")
	public void save() throws WrongValueException, SQLException {
		int chequeVal = (int) Float.parseFloat(chequeAmount.getValue());
		if (batchAmount1.getValue() - chequeVal >= 0) {
			instrumentDao.forceUpdateBatchAmountDao(batchAmount1.getValue().floatValue(), batch.getBatchNumber());
			Messagebox.show("Force balance successfull!!");
			batchAmount.setValue("Batch Amount: " + batchAmount1.getValue());

			batchAmount1.setValue(null);

		} else {
			Messagebox.show("Force balance failed!!");
		}

	}
}
