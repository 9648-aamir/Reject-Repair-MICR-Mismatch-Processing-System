package com.imageinfo.lbnrrbnl.model;

public class Batch {
	private String batchId;
	private String batchNumber;
	private int batchCount;
	private int chequeCount;
	private float amount;
	private int pendingCount;
	private int processedCount;
	private String status;
	private String userId;
	
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public int getBatchCount() {
		return batchCount;
	}
	public void setBatchCount(int batchCount) {
		this.batchCount = batchCount;
	}
	public int getChequeCount() {
		return chequeCount;
	}
	public void setChequeCount(int chequeCount) {
		this.chequeCount = chequeCount;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public int getPendingCount() {
		return pendingCount;
	}
	public void setPendingCount(int pendingCount) {
		this.pendingCount = pendingCount;
	}
	public int getProcessedCount() {
		return processedCount;
	}
	public void setProcessedCount(int processedCount) {
		this.processedCount = processedCount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Override
	public String toString() {
		return "Batch [batchId=" + batchId + ", batchNumber=" + batchNumber + ", batchCount=" + batchCount
				+ ", chequeCount=" + chequeCount + ", amount=" + amount + ", pendingCount=" + pendingCount
				+ ", processedCount=" + processedCount + ", status=" + status + ", userId=" + userId + "]";
	}
	
	
}
