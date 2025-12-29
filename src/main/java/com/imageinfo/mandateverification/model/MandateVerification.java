package com.imageinfo.mandateverification.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MandateVerification {
	private Long id;
	private String mandateId;
	private LocalDate scanDate;
	private String utilityCode;
	private String utilityDescription;
	private String creditorName;
	private String categoryCode;
	private LocalDate mandateDate;
	private String iqaStatus;
	private String debitorAcNo;
	private String debitorAcType;
	private String debitorIfsc;
	private String debitorMicr;
	private String debitorBankCode;
	private String debitorBankName;
	private String debitType;
	private BigDecimal amount;
	private String reference1;
	private String reference2;
	private String frequency;
	private LocalDate startDate;
	private LocalDate endDate;
	private String debitorName;
	private String phoneNo;
	private String debitorEmail;
	private String duplicateFlag;
	private String makerId;
	private String cancelCode;
	private String amendCode;
	private String actionType;
	private String status;
	private String sentBackRemarks;
	private String batchId;
	private String processedBy;
	private LocalDateTime processedDate;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<String> utilityCodes;
    private List<String> scanDates;
    private List<String> mandateTypes;
    private List<String> actionTypes;
    private int batchCount;
    private int pendingCount;
    private int processedCount;
    
    

	public String getDuplicateFlag() {
		return duplicateFlag;
	}

	public void setDuplicateFlag(String duplicateFlag) {
		this.duplicateFlag = duplicateFlag;
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

	public int getBatchCount() {
		return batchCount;
	}

	public void setBatchCount(int batchCount) {
		this.batchCount = batchCount;
	}

	public List<String> getUtilityCodes() {
		return utilityCodes;
	}

	public void setUtilityCodes(List<String> utilityCodes) {
		this.utilityCodes = utilityCodes;
	}

	public List<String> getScanDates() {
		return scanDates;
	}

	public void setScanDates(List<String> scanDates) {
		this.scanDates = scanDates;
	}

	public List<String> getMandateTypes() {
		return mandateTypes;
	}

	public void setMandateTypes(List<String> mandateTypes) {
		this.mandateTypes = mandateTypes;
	}

	public List<String> getActionTypes() {
		return actionTypes;
	}

	public void setActionTypes(List<String> actionTypes) {
		this.actionTypes = actionTypes;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMandateId() {
		return mandateId;
	}

	public void setMandateId(String mandateId) {
		this.mandateId = mandateId;
	}

	public LocalDate getScanDate() {
		return scanDate;
	}

	public void setScanDate(LocalDate scanDate) {
		this.scanDate = scanDate;
	}

	public String getUtilityCode() {
		return utilityCode;
	}

	public void setUtilityCode(String utilityCode) {
		this.utilityCode = utilityCode;
	}

	public String getUtilityDescription() {
		return utilityDescription;
	}

	public void setUtilityDescription(String utilityDescription) {
		this.utilityDescription = utilityDescription;
	}

	public String getCreditorName() {
		return creditorName;
	}

	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public LocalDate getMandateDate() {
		return mandateDate;
	}

	public void setMandateDate(LocalDate mandateDate) {
		this.mandateDate = mandateDate;
	}

	public String getIqaStatus() {
		return iqaStatus;
	}

	public void setIqaStatus(String iqaStatus) {
		this.iqaStatus = iqaStatus;
	}

	public String getDebitorAcNo() {
		return debitorAcNo;
	}

	public void setDebitorAcNo(String debitorAcNo) {
		this.debitorAcNo = debitorAcNo;
	}

	public String getDebitorAcType() {
		return debitorAcType;
	}

	public void setDebitorAcType(String debitorAcType) {
		this.debitorAcType = debitorAcType;
	}

	public String getDebitorIfsc() {
		return debitorIfsc;
	}

	public void setDebitorIfsc(String debitorIfsc) {
		this.debitorIfsc = debitorIfsc;
	}

	public String getDebitorMicr() {
		return debitorMicr;
	}

	public void setDebitorMicr(String debitorMicr) {
		this.debitorMicr = debitorMicr;
	}

	public String getDebitorBankCode() {
		return debitorBankCode;
	}

	public void setDebitorBankCode(String debitorBankCode) {
		this.debitorBankCode = debitorBankCode;
	}

	public String getDebitorBankName() {
		return debitorBankName;
	}

	public void setDebitorBankName(String debitorBankName) {
		this.debitorBankName = debitorBankName;
	}

	public String getDebitType() {
		return debitType;
	}

	public void setDebitType(String debitType) {
		this.debitType = debitType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getReference1() {
		return reference1;
	}

	public void setReference1(String reference1) {
		this.reference1 = reference1;
	}

	public String getReference2() {
		return reference2;
	}

	public void setReference2(String reference2) {
		this.reference2 = reference2;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public String getDebitorName() {
		return debitorName;
	}

	public void setDebitorName(String debitorName) {
		this.debitorName = debitorName;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getDebitorEmail() {
		return debitorEmail;
	}

	public void setDebitorEmail(String debitorEmail) {
		this.debitorEmail = debitorEmail;
	}

	public String getMakerId() {
		return makerId;
	}

	public void setMakerId(String makerId) {
		this.makerId = makerId;
	}

	public String getCancelCode() {
		return cancelCode;
	}

	public void setCancelCode(String cancelCode) {
		this.cancelCode = cancelCode;
	}

	public String getAmendCode() {
		return amendCode;
	}

	public void setAmendCode(String amendCode) {
		this.amendCode = amendCode;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSentBackRemarks() {
		return sentBackRemarks;
	}

	public void setSentBackRemarks(String sentBackRemarks) {
		this.sentBackRemarks = sentBackRemarks;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getProcessedBy() {
		return processedBy;
	}

	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}

	public LocalDateTime getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(LocalDateTime processedDate) {
		this.processedDate = processedDate;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "MandateVerification [scanDate=" + scanDate + ", status=" + status + ", batchId=" + batchId
				+ ", scanDates=" + scanDates + ", batchCount=" + batchCount + ", pendingCount=" + pendingCount
				+ ", processedCount=" + processedCount + "]";
	}
	
	
}