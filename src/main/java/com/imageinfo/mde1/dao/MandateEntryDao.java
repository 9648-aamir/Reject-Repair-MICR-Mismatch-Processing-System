package com.imageinfo.mde1.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.imageinfo.connection.DataBaseConnection;
import com.imageinfo.mde1.model.MandateVerification;
import com.imageinfo.util.LoggerUtil;

public class MandateEntryDao {
	
	private static final Logger logger = LoggerUtil.getLogger(MandateEntryDao.class);
	

	public List<String> getMandateTypes() throws SQLException {
		List<String> list = new ArrayList<>();
		String sql = "SELECT DISTINCT mandate_type FROM mandate_verification ORDER BY mandate_type";

		try (Connection con = DataBaseConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(rs.getString("mandate_type"));
			}
		}catch (Exception e) {
			logger.error("Error while selecting mandate_type",e);
		}
		return list;
	}

	public List<String> getUtilityCodes(String mandateType) throws SQLException {
		List<String> list = new ArrayList<>();
		String sql = """
				    SELECT DISTINCT utility_code
				    FROM mandate_verification
				    WHERE mandate_type = ? 
				    ORDER BY utility_code
				""";

		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, mandateType);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(rs.getString("utility_code"));
				}
			}
		}catch (Exception e) {
			logger.error("Error while selecting utility_code using mandate_type",e);
		}
		return list;
	}

	public List<Date> getScanDates(String mandateType, String utilityCode) throws SQLException {
		List<Date> list = new ArrayList<>();
		String sql = """
				    SELECT DISTINCT scan_date
				    FROM mandate_verification
				    WHERE mandate_type = ? AND utility_code = ? And pending!=0
				    ORDER BY scan_date DESC
				""";

		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, mandateType);
			ps.setString(2, utilityCode);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(rs.getDate("scan_date"));
				}
			}
		}catch (Exception e) {
			logger.error("Error while selecting scan_date using mandate_type AND utility_code",e);
		}
		return list;
	}

	public List<MandateVerification> fetchBatches(String mandateType, String utilityCode, String scanDate)
			throws SQLException {

		List<MandateVerification> list = new ArrayList<>();
		String sql = """
				    SELECT batch_id, scan_date,
				           COUNT(*) as batch_count,
				           SUM(CASE WHEN status='Pending' THEN 1 ELSE 0 END) as pending_count,
				           SUM(CASE WHEN status='Processed' THEN 1 ELSE 0 END) as processed_count,
				           SUM(amount) as amount,
				           CASE
				             WHEN SUM(CASE WHEN status='Pending' THEN 1 ELSE 0 END) > 0 THEN 'Pending'
				             ELSE 'Processed'
				           END as batch_status
				    FROM mandate_verification
				    WHERE mandate_type = ? AND utility_code = ? AND scan_date = ?
				    GROUP BY batch_id, scan_date
				    ORDER BY scan_date DESC
				""";

		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, mandateType);
			ps.setString(2, utilityCode);

			java.sql.Date sqlDate = null;
			if (scanDate != null && !scanDate.isEmpty()) {
			
					java.util.Date utilDate = new SimpleDateFormat("dd-MM-yyyy").parse(scanDate);
					sqlDate = new java.sql.Date(utilDate.getTime());
			}
			ps.setDate(3, sqlDate);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					MandateVerification m = new MandateVerification();
					m.setBatchId(rs.getString("batch_id"));

					Date dbDate = rs.getDate("scan_date");
					if (dbDate != null) {
						m.setScanDate(dbDate.toLocalDate());
					} else {
						m.setScanDate(null);
					}

					m.setBatchCount(rs.getInt("batch_count"));
					m.setPendingCount(rs.getInt("pending_count"));
					m.setProcessedCount(rs.getInt("processed_count"));
					m.setAmount(rs.getBigDecimal("amount"));
					m.setStatus(rs.getString("batch_status"));
					list.add(m);
				}
			}
		}catch (Exception e) {
			logger.error("Error while selecting batch using mandate_type, utility_code and scan_date",e);
		}
		return list;
	}

	public void updateMandateByBatch(MandateVerification entry, String batchid) throws SQLException {
		String sql = """
				    UPDATE mandate_verification
				    SET
				        mandate_id = ?, 
				        scan_date = ?,
				        utility_code = ?,
				        utility_description = ?,
				        creditor_name = ?,
				        category_code = ?,
				        mandate_date = ?,
				        iqa_status = ?,
				        debitor_ac_no = ?,
				        debitor_ac_type = ?,
				        debitor_ifsc = ?,
				        debitor_bank_code = ?,
				        debitor_bank_name = ?,
				        debit_type = ?,
				        amount = ?,
				        reference1 = ?,
				        reference2 = ?,
				        frequency = ?,
				        start_date = ?,
				        end_date = ?,
				        debitor_name = ?,
				        phone_no = ?,
				        debitor_email = ?,
				        remark = ?,
				        updated_at = NOW()
				    WHERE batch_id = ?
				""";

		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, entry.getMandateId());
			ps.setDate(2, entry.getScanDate() != null ? Date.valueOf(entry.getScanDate()) : null);
			ps.setString(3, entry.getUtilityCode());
			ps.setString(4, entry.getUtilityDescription());
			ps.setString(5, entry.getCreditorName());
			ps.setString(6, entry.getCategoryCode());
			ps.setDate(7, entry.getMandateDate() != null ? Date.valueOf(entry.getMandateDate()) : null);
			ps.setString(8, entry.getIqaStatus());
			ps.setString(9, entry.getDebitorAcNo());
			ps.setString(10, entry.getDebitorAcType());
			ps.setString(11, entry.getDebitorIfsc());
			ps.setString(12, entry.getDebitorBankCode());
			ps.setString(13, entry.getDebitorBankName());
			ps.setString(14, entry.getDebitType());
			if (entry.getAmount() != null) {
				ps.setBigDecimal(15, entry.getAmount());
			} else {
				ps.setNull(15, java.sql.Types.DECIMAL);
			}
			ps.setString(16, entry.getReference1());
			ps.setString(17, entry.getReference2());
			ps.setString(18, entry.getFrequency());
			ps.setDate(19, entry.getStartDate() != null ? Date.valueOf(entry.getStartDate()) : null);
			ps.setDate(20, entry.getEndDate() != null ? Date.valueOf(entry.getEndDate()) : null);
			ps.setString(21, entry.getDebitorName());
			ps.setString(22, entry.getPhoneNo());
			ps.setString(23, entry.getDebitorEmail());
			ps.setString(24, entry.getRemark());
			ps.setString(25, batchid);

			ps.executeUpdate();

		}catch (Exception e) {
			logger.error("Error while updating entry using batch_id",e);
		}
	}

	public List<MandateVerification> getUtilityCodesByScanDate(Date scanDate) throws SQLException {
		List<MandateVerification> list = new ArrayList<>();

		String sql = "SELECT utility_code, utility_description, category_code "
				+ "FROM mandate_verification WHERE scan_date = ?";
		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setDate(1, new java.sql.Date(scanDate.getTime()));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					MandateVerification u = new MandateVerification();
					u.setUtilityCode(rs.getString("utility_code"));
					u.setUtilityDescription(rs.getString("utility_description"));
					u.setCategoryCode(rs.getString("category_code"));
					list.add(u);
				}
			}
		}catch (Exception e) {
			logger.error("Error while selecting utility_code, utility_description and category_code using scan_date",e);
		}
		return list;
	}

	public MandateVerification getBankByIfscMicr(String batchId, String ifc) throws SQLException {
		String sql = "SELECT debitor_bank_code, debitor_bank_name FROM mandate_verification WHERE batch_id=? AND debitor_ifsc = ?";
		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, batchId);
			ps.setString(2, ifc);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					MandateVerification b = new MandateVerification();
					b.setDebitorBankCode(rs.getString("debitor_bank_code"));
					b.setDebitorBankName(rs.getString("debitor_bank_name"));
					return b;
				}
			}
		}catch (Exception e) {
			logger.error("Error while selecting debitor_bank_code and debitor_bank_name using batch_id and debitor_ifsc",e);
		}
		return null;
	}
	
	public byte[] getMandateImage(String batchId) throws SQLException  {
        String sql = "SELECT mandate_image FROM mandate_verification WHERE batch_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("mandate_image");
                }
            }
        }catch (Exception e) {
			logger.error("Error while selecting mandate_image using batch_id",e);
		}
        return new byte[0];
    }
}
