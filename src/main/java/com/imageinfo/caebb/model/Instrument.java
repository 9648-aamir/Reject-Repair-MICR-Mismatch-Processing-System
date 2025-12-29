package com.imageinfo.caebb.model;

import java.util.Date;

public class Instrument {
	private Long chequeId;
	private String chequeNumber;
	private String cityCode;
	private String bankCode;
	private String branchCode;
	private String baseNumber;
	private String transactionCode;
	private Date chequeDate;
	private float amount;
	private String imagePath;
	private String deletionRemark;
	private String remarks;
	
   
	public Long getChequeId() {
		return chequeId;
	}

	public void setChequeId(Long chequeId) {
		this.chequeId = chequeId;
	}

	public String getChequeNumber() {
		return chequeNumber;
	}

	public void setChequeNumber(String chequeNumber) {
		this.chequeNumber = chequeNumber;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public String getBaseNumber() {
		return baseNumber;
	}

	public void setBaseNumber(String baseNumber) {
		this.baseNumber = baseNumber;
	}

	public String getTransactionCode() {
		return transactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public Date getChequeDate() {
		return chequeDate;
	}

	public void setChequeDate(Date chequeDate) {
		this.chequeDate = chequeDate;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getDeletionRemark() {
		return deletionRemark;
	}

	public void setDeletionRemark(String deletionRemark) {
		this.deletionRemark = deletionRemark;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@Override
	public String toString() {
		return "Instrument [chequeId=" + chequeId + ", chequeNumber=" + chequeNumber + ", cityCode=" + cityCode
				+ ", bankCode=" + bankCode + ", branchCode=" + branchCode + ", baseNumber=" + baseNumber
				+ ", transactionCode=" + transactionCode + ", chequeDate=" + chequeDate + ", amount=" + amount
				+ ", imagePath=" + imagePath + ", deletionRemark=" + deletionRemark + ", remarks=" + remarks + "]";
	}
	
	
}