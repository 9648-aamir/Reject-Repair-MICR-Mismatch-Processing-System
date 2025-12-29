package com.imageinfo.mde1.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.imageinfo.mde1.dao.MandateEntryDao;
import com.imageinfo.mde1.model.MandateVerification;

public class MandateEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final int MANDATE_DIFF_DAYS = 120;
	private static final int DIFF_YEARS = 40;
	private static final String SELECTED_BATCH = "selectedBatch";
	private static final String DATE_PATTERN = "dd-MM-yyyy";

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
	private Textbox mandateId;
	@Wire
	private Datebox scanDatem;
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
	private Image myImage1;

	private transient MandateEntryDao dao = new MandateEntryDao();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		batchList.setVisible(false);
		mandate.setVisible(false);

		List<String> mandateTypes = dao.getMandateTypes();
		mandateType.getItems().clear();
		for (String mt : mandateTypes) {
			mandateType.appendItem(mt);
		}
	}

	@Listen("onSelect = #mandateType")
	public void onMandateTypeSelect() throws SQLException {
		String mt = mandateType.getValue();
		List<String> utilities = dao.getUtilityCodes(mt);
		utilityCode.getItems().clear();
		for (String u : utilities) {
			utilityCode.appendItem(u);
		}
	}

	@Listen("onSelect = #utilityCode")
	public void onUtilityCodeSelect() throws SQLException {
		String mt = mandateType.getValue();
		String uc = utilityCode.getValue();
		List<Date> dates = dao.getScanDates(mt, uc);
		scanDate.getItems().clear();
		for (Date d : dates) {
			scanDate.appendItem(new SimpleDateFormat(DATE_PATTERN).format(d));
		}
	}

	@Listen("onClick = #fetchBtn")
	public void onFetchClick() throws SQLException {
		processSelection.setVisible(false);
		batchList.setVisible(true);
		populateBatchGrid();
	}

	private void populateBatchGrid() throws SQLException {
		String mt = mandateType.getValue();
		String uc = utilityCode.getValue();
		String sd = scanDate.getValue();

		List<MandateVerification> batches = dao.fetchBatches(mt, uc, sd);

		Rows rows = batchGrid.getRows();
		rows.getChildren().clear();

		for (MandateVerification b : batches) {
			Row row = new Row();

			if (b.getScanDate() != null) {
				row.appendChild(
						new Label(new SimpleDateFormat(DATE_PATTERN).format(java.sql.Date.valueOf(b.getScanDate()))));
			} else {
				row.appendChild(new Label(""));
			}

			row.appendChild(new Label(b.getBatchId()));
			row.appendChild(new Label(String.valueOf(b.getBatchCount())));
			row.appendChild(new Label(String.valueOf(b.getAmount())));
			row.appendChild(new Label(String.valueOf(b.getPendingCount())));
			row.appendChild(new Label(String.valueOf(b.getProcessedCount())));
			row.appendChild(new Label(b.getStatus()));
			row.appendChild(new Label(b.getMakerId()));

			row.addEventListener("onClick", event -> {
				batchList.setVisible(false);
				mandate.setVisible(true);
				Sessions.getCurrent().setAttribute(SELECTED_BATCH, b);
				displayMandateImage();
			});

			rows.appendChild(row);
		}
	}

	@Listen("onClick = #saveBtn")
	public void onSave() throws SQLException {

		MandateVerification entry = buildMandateFromForm();

		if (!validateDates(entry)) {
			return;
		}

		MandateVerification mav = (MandateVerification) Sessions.getCurrent().getAttribute(SELECTED_BATCH);

		dao.updateMandateByBatch(entry, mav.getBatchId());
		Messagebox.show("Mandate saved successfully!");
		
		processSelection.setVisible(true);
		batchList.setVisible(false);
		mandate.setVisible(false);
	}

	private MandateVerification buildMandateFromForm() {
		MandateVerification entry = new MandateVerification();

		entry.setMandateId(mandateId.getValue());

		String selectedDateStr = scanDate.getValue();
		if (selectedDateStr != null && !selectedDateStr.isEmpty()) {
			LocalDate scanLocalDate = LocalDate.parse(selectedDateStr, DateTimeFormatter.ofPattern(DATE_PATTERN));
			entry.setScanDate(scanLocalDate);
		} else {
			entry.setScanDate(null);
		}

		entry.setUtilityCode(utilityCode.getValue());
		entry.setUtilityDescription(utilityDescription.getValue());
		entry.setCreditorName(creditorName.getValue());

		entry.setCategoryCode(categoryCode.getValue());

		if (mandateDate.getValue() != null) {
			java.util.Date utilDate1 = mandateDate.getValue();
			LocalDate localMandateDate = utilDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			entry.setMandateDate(localMandateDate);
		}

		entry.setDebitorAcNo(debtorAccountNumber.getValue());
		entry.setDebitorAcType(debtorAccountType.getValue());
		entry.setDebitorIfsc(debtorIfscMicr.getValue());
		entry.setDebitorBankName(debtorBankName.getValue());
		entry.setDebitorBankCode(debtorBankCode.getValue());
		entry.setDebitType(debitType.getSelectedItem() != null ? debitType.getSelectedItem().getValue() : null);
		entry.setAmount(amount.getValue() != null ? BigDecimal.valueOf(amount.getValue()) : null);
		entry.setReference1(reference1.getValue());
		entry.setReference2(reference2.getValue());
		entry.setFrequency(frequency.getValue());

		if (startDate.getValue() != null) {
			java.util.Date utilDate1 = startDate.getValue();
			LocalDate localStartDate1 = utilDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			entry.setStartDate(localStartDate1);
		}

		if (endDate.getValue() != null) {
			java.util.Date utilDate2 = endDate.getValue();
			LocalDate localEndDate2 = utilDate2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			entry.setEndDate(localEndDate2);
		}

		entry.setDebitorName(debtorName.getValue());
		entry.setPhoneNo(phoneNumber.getValue());
		entry.setDebitorEmail(email.getValue());
		entry.setRemark(
				remarksDropdown.getSelectedItem() != null ? remarksDropdown.getSelectedItem().getValue() : null);

		return entry;
	}

	@Listen("onClick=#utilityCodem")
	public void onScanDateEntered() throws SQLException {
		utilityCodem.getItems().clear();

		String enteredDate = scanDate.getValue();
		if (enteredDate != null && !enteredDate.trim().isEmpty()) {
			try {

				SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
				sdf.setLenient(false);
				java.util.Date selectedDate = sdf.parse(enteredDate);

				java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());

				List<MandateVerification> utilities = dao.getUtilityCodesByScanDate(sqlDate);

				for (MandateVerification u : utilities) {
					Comboitem item = new Comboitem(u.getUtilityCode());
					item.setValue(u);
					utilityCodem.appendChild(item);
				}
			} catch (java.text.ParseException e) {
				Clients.showNotification("Invalid date format! Use dd-MM-yyyy", "error", scanDate, "end_center", 3000);
			}
		}
	}

	@Listen("onSelect=#utilityCodem")
	public void onUtilityCodeSelected() {
		Comboitem selected = utilityCodem.getSelectedItem();
		if (selected != null) {
			MandateVerification u = selected.getValue();

			utilityDescription.setValue(u.getUtilityDescription());
			categoryCode.setValue(u.getCategoryCode());
		}
	}

	@Listen("onChange=#debtorIfscMicr; onOK=#debtorIfscMicr")
	public void onDebtorIfscMicrEntered() throws SQLException {
		String ifscMicr = debtorIfscMicr.getValue();
		if (ifscMicr != null && !ifscMicr.trim().isEmpty()) {
			MandateVerification mav = (MandateVerification) Sessions.getCurrent().getAttribute(SELECTED_BATCH);

			MandateVerification bank = dao.getBankByIfscMicr(mav.getBatchId(), ifscMicr);
			if (bank != null) {

				debtorBankCode.setValue(bank.getDebitorBankCode());
				debtorBankName.setValue(bank.getDebitorBankName());
			} else {
				Messagebox.show("Invalid IFSC/MICR Code");
				debtorBankCode.setValue("");
				debtorBankName.setValue("");
			}
		}
	}

	private boolean validateDates(MandateVerification entry) {
		LocalDate scan = entry.getScanDate();
		LocalDate mandate1 = entry.getMandateDate();
		if (scan != null && mandate1 != null) {
			long diffDays = ChronoUnit.DAYS.between(scan, mandate1);
			if (Math.abs(diffDays) > MANDATE_DIFF_DAYS) {
				Messagebox.show("Mandate Date and Scan Date difference cannot exceed " + MANDATE_DIFF_DAYS + " days.",
						"Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
				return false;
			}
		}

		LocalDate start = entry.getStartDate();
		LocalDate end = entry.getEndDate();
		if (start != null && end != null) {
			long years = ChronoUnit.YEARS.between(start, end);
			if (years > DIFF_YEARS) {
				Messagebox.show("Start Date and End Date difference cannot exceed " + DIFF_YEARS + " years.",
						"Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
				return false;
			}
		}

		return true;
	}

	@Listen("onClick = #backBtn")
	public void onBackClick() {
		processSelection.setVisible(true);
		mandate.setVisible(false);
		batchList.setVisible(false);
	}

	@Listen("onClick = #backBtn1")
	public void onBackClick1() {
		processSelection.setVisible(false);
		mandate.setVisible(false);
		batchList.setVisible(true);
	}

	public void displayMandateImage() {
		try {
			MandateVerification mav = (MandateVerification) Sessions.getCurrent().getAttribute(SELECTED_BATCH);
			if (mav == null) {
				Messagebox.show("No batch selected!");
				return;
			}

			byte[] imgBytes = dao.getMandateImage(mav.getBatchId());
			if (imgBytes == null) {
				Messagebox.show("No image found for this Mandate ID!");
				return;
			}

			AImage aimage = new AImage("mandate-photo", new java.io.ByteArrayInputStream(imgBytes));
			myImage1.setContent(aimage);

		} catch (IOException e) {
			Messagebox.show("Image fetching error!");
			e.printStackTrace();
		} catch (Exception e) {
			Messagebox.show("Database error: " + e.getMessage());
			e.printStackTrace();
		}
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
		myImage1.setStyle("object-fit:contain; display:block; margin:auto; transform:rotate(" + rotation + "deg);");
	}

	private double currentScale = 1.0;
	private static final double ZOOM_FACTOR = 0.1;

	@Listen("onClick = #zoomInBtn")
	public void zoomIn() {
		currentScale += ZOOM_FACTOR;
		updateZoom();
	}

	@Listen("onClick = #zoomOutBtn")
	public void zoomOut() {
		if (currentScale > 0.2) {
			currentScale -= ZOOM_FACTOR;
			updateZoom();
		}
	}

	@Listen("onClick = #resetBtn")
	public void resetZoom() {
		currentScale = 1.0;
		updateZoom();
	}

	private void updateZoom() {
		myImage1.setStyle("transform: scale(" + currentScale + "); transition: transform 0.2s ease;");
	}
}
