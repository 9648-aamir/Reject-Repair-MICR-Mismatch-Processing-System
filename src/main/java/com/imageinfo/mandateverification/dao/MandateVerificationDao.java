package com.imageinfo.mandateverification.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.imageinfo.connection.DataBaseConnection;
import com.imageinfo.mandateverification.model.MandateVerification;
import com.imageinfo.util.LoggerUtil;

public class MandateVerificationDao {

	private static final Logger logger = LoggerUtil.getLogger(MandateVerificationDao.class);
	
	private static final String UTILITY_CODE = "utility_code";
	private static final String SCAN_DATE = "scan_date";

	public List<String> getUtilityCodes() {
		List<String> utilityCodes = new ArrayList<>();
		String sql = """
				SELECT DISTINCT utility_code
				  FROM mandate_verification
				  WHERE status = 'Pending'
				 ORDER BY utility_code
				  """;
		try (Connection conn = DataBaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				utilityCodes.add(rs.getString(UTILITY_CODE));
			}
		} catch (Exception e) {
			logger.error("Error while selecting utility_code",e);
		}
		return utilityCodes;
	}

	public List<String> getScanDates(String utilityCode) {
		List<String> scanDates = new ArrayList<>();
		String sql = "SELECT DISTINCT scan_date FROM mandate_verification WHERE utility_code = ? AND status = 'Pending' ORDER BY scan_date DESC";
		try (Connection conn = DataBaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, utilityCode);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					scanDates.add(rs.getDate(SCAN_DATE).toString());
				}
			}
		} catch (Exception e) {
			logger.error("Error while selecting scan_date using utility_code",e);
		}
		return scanDates;
	}

	public List<String> getMandateTypes(String utilityCode, String scanDate) {
		List<String> mandateTypes = new ArrayList<>();
		String sql = "SELECT DISTINCT mandate_type FROM mandate_verification WHERE utility_code = ? AND scan_date = ? ORDER BY mandate_type";
		try (Connection conn = DataBaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, utilityCode);
			ps.setDate(2, Date.valueOf(scanDate));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					mandateTypes.add(rs.getString("mandate_type"));
				}
			}
		} catch (Exception e) {
			logger.error("Error while selecting mandate_type using utility_code and scan_date",e);
		}
		return mandateTypes;
	}

	public List<String> getActionTypes(String utilityCode, String scanDate, String mandateType) {
		List<String> actionTypes = new ArrayList<>();
		String sql = "SELECT DISTINCT action_type FROM mandate_verification WHERE utility_code = ? AND scan_date = ? AND mandate_type = ? ORDER BY action_type";
		try (Connection conn = DataBaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, utilityCode);
			ps.setDate(2, Date.valueOf(scanDate));
			ps.setString(3, mandateType);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					actionTypes.add(rs.getString("action_type"));
				}
			}
		} catch (Exception e) {
			logger.error("Error while selecting action_type using utility_code, scan_date and mandate_type",e);
		}
		return actionTypes;
	}

	public List<MandateVerification> getBatches(String utilityCode, String scanDate, String mandateType,
			String actionType) {

		List<MandateVerification> batchList = new ArrayList<>();

		String sql = """
				SELECT batch_id,
				MIN(scan_date) AS scan_date,
				COUNT(*) AS batch_count,
				SUM(pending) AS pending_count,
				SUM(processed) AS processed_count,
				status,
				processed_by
				FROM mandate_verification
				WHERE utility_code = ?
				AND scan_date = ?
				AND mandate_type = ?
				AND action_type = ?
				GROUP BY batch_id, status, processed_by
				ORDER BY scan_date DESC
				""";

		try (Connection conn = DataBaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, utilityCode);
			ps.setDate(2, Date.valueOf(scanDate));
			ps.setString(3, mandateType);
			ps.setString(4, actionType);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					MandateVerification mv = new MandateVerification();
					mv.setBatchId(rs.getString("batch_id"));
					mv.setScanDate(rs.getDate(SCAN_DATE).toLocalDate());
					mv.setBatchCount(rs.getInt("batch_count"));
					mv.setPendingCount(rs.getInt("pending_count"));
					mv.setProcessedCount(rs.getInt("processed_count"));
					mv.setStatus(rs.getString("status"));
					mv.setProcessedBy(rs.getString("processed_by"));
					batchList.add(mv);
				}
			}

		} catch (Exception e) {
			logger.error("Error while selecting batch using utility_code, scan_date, mandate_type and action_type",e);
		}

		return batchList;
	}

	public List<MandateVerification> getUtilityByScanDate(LocalDate scanDate) {
		List<MandateVerification> utilities = new ArrayList<>();

		String sql = "SELECT DISTINCT utility_code, utility_description, category_code "
				+ "FROM mandate_verification WHERE scan_date = ?";

		try (Connection conn = DataBaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDate(1, Date.valueOf(scanDate));
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				MandateVerification u = new MandateVerification();
				u.setUtilityCode(rs.getString(UTILITY_CODE));
				u.setUtilityDescription(rs.getString("utility_description"));
				u.setCategoryCode(rs.getString("category_code"));
				utilities.add(u);
			}
		}  catch (SQLException e) {
			logger.error("Error while selecting utility_code, utility_description and category_code using scan_date",e);
			}

		return utilities;
	}

	public MandateVerification getBankByIfscMicr(String batchNumber, String ifc) throws SQLException {

		String sql = "SELECT debitor_bank_code, debitor_bank_name FROM mandate_verification WHERE batch_id=? AND debitor_ifsc = ?";
		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, batchNumber);
			ps.setString(2, ifc);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					MandateVerification b = new MandateVerification();
					b.setDebitorBankCode(rs.getString("debitor_bank_code"));
					b.setDebitorBankName(rs.getString("debitor_bank_name"));
					return b;
				}
			}catch (Exception e) {
				logger.error("Error while selecting debitor_bank_code and debitor_bank_name  using batch_id AND debitor_ifsc",e);
			}
		}
		return null;
	}

	public MandateVerification getMandateByBatch(String batchId) throws SQLException {
		String sql = """
				    SELECT
				        mandate_id,
				        scan_date,
				        utility_code,
				        utility_description,
				        creditor_name,
				        category_code,
				        mandate_date,
				        iqa_status,
				        debitor_ac_no,
				        debitor_ac_type,
				        debitor_ifsc,
				        debitor_bank_code,
				        debitor_bank_name,
				        debit_type,
				        amount,
				        reference1,
				        reference2,
				        frequency,
				        start_date,
				        end_date,
				        debitor_name,
				        phone_no,
				        debitor_email,
				        amend_code,
				        duplicate_status,
				        created_at,
				        updated_at
				    FROM mandate_verification
				    WHERE batch_id = ?
				""";

		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, batchId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					MandateVerification entry = new MandateVerification();
					entry.setMandateId(rs.getString("mandate_id"));
					entry.setScanDate(rs.getDate(SCAN_DATE) != null ? rs.getDate(SCAN_DATE).toLocalDate() : null);
					entry.setUtilityCode(rs.getString(UTILITY_CODE));
					entry.setUtilityDescription(rs.getString("utility_description"));
					entry.setCreditorName(rs.getString("creditor_name"));
					entry.setCategoryCode(rs.getString("category_code"));
					entry.setMandateDate(
							rs.getDate("mandate_date") != null ? rs.getDate("mandate_date").toLocalDate() : null);
					entry.setIqaStatus(rs.getString("iqa_status"));
					entry.setDebitorAcNo(rs.getString("debitor_ac_no"));
					entry.setDebitorAcType(rs.getString("debitor_ac_type"));
					entry.setDebitorIfsc(rs.getString("debitor_ifsc"));
					entry.setDebitorBankCode(rs.getString("debitor_bank_code"));
					entry.setDebitorBankName(rs.getString("debitor_bank_name"));
					entry.setDebitType(rs.getString("debit_type"));
					entry.setAmount(rs.getBigDecimal("amount"));
					entry.setReference1(rs.getString("reference1"));
					entry.setReference2(rs.getString("reference2"));
					entry.setFrequency(rs.getString("frequency"));
					entry.setStartDate(
							rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null);
					entry.setEndDate(rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null);
					entry.setDebitorName(rs.getString("debitor_name"));
					entry.setPhoneNo(rs.getString("phone_no"));
					entry.setDebitorEmail(rs.getString("debitor_email"));
					entry.setAmendCode(rs.getString("amend_code"));
					entry.setDuplicateFlag(rs.getString("duplicate_status"));
					
					return entry;
				} else {
					return null;
				}
			}catch (Exception e) {
				logger.error("Error while selecting entry  using batch_id",e);
				return null;
			}
		}
	}

	public void updateMandateStatusAndCounts(String batchId) throws SQLException {
		String sql = """
				    UPDATE mandate_verification
				    SET
				        status = 'processed',
				        pending = COALESCE(pending, 0) - 1,
				        processed = COALESCE(processed, 0) + 1,
				        updated_at = NOW()
				    WHERE batch_id = ?
				""";

		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, batchId);
			ps.executeUpdate();
		}catch (Exception e) {
			logger.error("Error while updating status, pending and processed using batch_id",e);
		}
	}

	public void updateMandateStatus(String batchId, String remark) throws SQLException {
		String sql = """
				UPDATE mandate_verification
				    SET
				        status = 'send back',
				        remark = ?,
				        updated_at = NOW()
				    WHERE batch_id = ?
				""";

		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, remark);
			ps.setString(2, batchId);

			ps.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while updating status and remark using batch_id",e);
		}
	}

}
