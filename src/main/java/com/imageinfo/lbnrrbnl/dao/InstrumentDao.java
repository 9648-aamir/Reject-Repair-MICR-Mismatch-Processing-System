package com.imageinfo.lbnrrbnl.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.imageinfo.connection.DataBaseConnection;
import com.imageinfo.lbnrrbnl.model.Instrument;
import com.imageinfo.util.LoggerUtil;

public class InstrumentDao {
	
	private static final Logger logger = LoggerUtil.getLogger(InstrumentDao.class);
	
	public List<Instrument> fetchInstrumentsForBatch(String batchNumber, String clearingType) throws SQLException {

		List<Instrument> list = new ArrayList<>();
		String sql = """
				    SELECT
				        id,
				        cheque_number,
				        city_code,
				        bank_code,
				        branch_code,
				        base_number,
				        transaction_code,
				        amount,
				        status,
				        remark,
				        is_deleted,
				        matched,
				        SUM(CASE WHEN matched = 1 THEN 1 ELSE 0 END) OVER () AS matched_count
				    FROM cheque_batch_instrument
				    WHERE batch_number=? AND batch_type = ? AND is_deleted=0  AND matched='0'
				    ORDER BY id
				""";

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, batchNumber);
			ps.setString(2, clearingType);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Instrument instrument = new Instrument();
					instrument.setChequeId(rs.getLong("id"));
					instrument.setChequeNumber(rs.getString("cheque_number"));
					instrument.setCityCode(rs.getString("city_code"));
					instrument.setBankCode(rs.getString("bank_code"));
					instrument.setBranchCode(rs.getString("branch_code"));
					instrument.setBaseNumber(rs.getString("base_number"));
					instrument.setTransactionCode(rs.getString("transaction_code"));
					instrument.setAmount(rs.getFloat("amount"));
					instrument.setStatus(rs.getString("status"));
					instrument.setRemark(rs.getString("remark"));
					instrument.setDeleted(rs.getBoolean("is_deleted"));
					instrument.setMatchedCount(rs.getInt("matched_count"));
					list.add(instrument);
				}
			}
		}catch (SQLException e) {
			logger.error("Error while fetching data using  batch_number and batch_type", e);
		}
		return list;
	}

	public Instrument matchedDao(String batchNumber, String clearingType) {
		Instrument instrument;
		String sql = """
				    SELECT
				        matched,
				        SUM(CASE WHEN matched = 1 THEN 1 ELSE 0 END) OVER () AS matched_count
				    FROM cheque_batch_instrument
				    WHERE batch_number = ?
				      AND batch_type = ?
				      AND is_deleted = 0
				      AND matched = 1
				    ORDER BY id;
				""";

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, batchNumber);
			ps.setString(2, clearingType);

			try (ResultSet rs = ps.executeQuery()) {
				instrument = new Instrument();
				while (rs.next()) {
					instrument.setMatchedCount(rs.getInt("matched_count"));
				}
				return instrument;
			}
		} catch (SQLException e) {
			logger.error("Error while updating match count", e);
		}
		return null;
	}

	public void deleteInstrument(String chequeNumber, String deletionRemark) throws SQLException {
		String sql = "UPDATE cheque_batch_instrument SET status='DELETED', remark=?, is_deleted=1 WHERE cheque_number=?";
		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, deletionRemark);
			ps.setString(2, chequeNumber);
			ps.executeUpdate();
		}catch (SQLException e) {
			logger.error("Error while deleting", e);
		}
	}

	public void markAsMatched(String chequeNumber) throws SQLException {
		String sql = "UPDATE cheque_batch_instrument SET matched = matched + 1 WHERE cheque_number = ?";
		try (Connection con = DataBaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, chequeNumber);
			ps.executeUpdate();
		}catch (SQLException e) {
			logger.error("Error while matching", e);
		}
	}

	public Instrument deleteCountDao(String batchNumber, String clearingType) throws SQLException {

		String sql = """
				SELECT
				    c.batch_number,

				    SUM(
				        CASE
				            WHEN (c.is_deleted=1)
				            THEN 1 ELSE 0
				        END
				    ) AS delete_count

				FROM cheque_batch_instrument c
				WHERE c.batch_number = ?
				AND c.batch_type = ?
				GROUP BY c.batch_number
				 """;

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, batchNumber);
			ps.setString(2, clearingType);

			Instrument instrument;
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					instrument = new Instrument();
					instrument.setDeleteCount(rs.getString("delete_count"));
					return instrument;
				}
			}
		}catch (SQLException e) {
			logger.error("Error while counting delete", e);
		}
		return null;

	}
}
