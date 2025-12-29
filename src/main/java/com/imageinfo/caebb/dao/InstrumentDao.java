package com.imageinfo.caebb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.imageinfo.caebb.model.Instrument;
import com.imageinfo.connection.DataBaseConnection;
import com.imageinfo.util.LoggerUtil;

public class InstrumentDao {

	private static final Logger logger = LoggerUtil.getLogger(InstrumentDao.class);

	public List<Instrument> fetchInstrumentsForBatch(String batchNumber, Instrument ins) throws SQLException {
		List<Instrument> list = new ArrayList<>();
		String sql = """
				            SELECT cheque_id,
				       cheque_number,
				       city_code,
				       bank_code,
				       branch_code,
				       base_number,
				       transaction_code
				FROM cheque_batch_instruments
				WHERE batch_number = ?
				  AND is_deleted = FALSE
				  AND cheque_date IS NULL
				  AND (instrument_amount IS NULL OR instrument_amount = 0)
				ORDER BY cheque_id
				        """;

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, batchNumber);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ins.setChequeId(rs.getLong("cheque_id"));
					ins.setChequeNumber(rs.getString("cheque_number"));
					ins.setCityCode(rs.getString("city_code"));
					ins.setBankCode(rs.getString("bank_code"));
					ins.setBranchCode(rs.getString("branch_code"));
					ins.setBaseNumber(rs.getString("base_number"));
					ins.setTransactionCode(rs.getString("transaction_code"));
					list.add(ins);
				}
			}
		} catch (SQLException e) {
			logger.error("Error while fetching data using batch_number", e);
		}
		return list;
	}

	public void saveInstrumentEntry(long chequeId, Date chequeDate, Float amount) throws SQLException {
		String sql = "UPDATE cheque_batch_instruments SET cheque_date=?, instrument_amount=?, status='PROCESSED' WHERE cheque_id=?";
		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setDate(1, new java.sql.Date(chequeDate.getTime()));
			ps.setFloat(2, amount);
			ps.setLong(3, chequeId);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error while fetching data using batch_number", e);
		}
	}

	public void deleteInstrument(long instrumentId, String deletionRemark) throws SQLException {
		String sql = "UPDATE cheque_batch_instruments SET status='DELETED', deletion_remark=?, is_deleted=TRUE WHERE cheque_id=?";
		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, deletionRemark);
			ps.setLong(2, instrumentId);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error while deleting", e);
		}
	}

	public void deleteAllInstrumentsByBatch(String batchNumber, String deletionRemark) throws SQLException {
		String sql = "UPDATE cheque_batch_instruments " + "SET status='DELETED', deletion_remark=?, is_deleted=TRUE "
				+ "WHERE batch_number = ? AND is_deleted = FALSE";

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, deletionRemark);
			ps.setString(2, batchNumber);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error while deleting batch", e);
		}
	}

	public List<Instrument> fetchPendingInstrumentsForBatch(String batchNumber) throws SQLException {
		List<Instrument> list = new ArrayList<>();
		String sql = """
				    SELECT cheque_id, cheque_number, city_code, bank_code, branch_code, base_number, transaction_code
				    FROM cheque_batch_instruments
				    WHERE batch_number=? AND is_deleted=FALSE
				      AND (instrument_amount IS NULL OR cheque_date IS NULL OR city_code IS NULL OR city_code = '')
				    ORDER BY cheque_id
				""";

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, batchNumber);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Instrument ins = new Instrument();
					ins.setChequeId(rs.getLong("cheque_id"));
					ins.setChequeNumber(rs.getString("cheque_number"));
					ins.setCityCode(rs.getString("city_code"));
					ins.setBankCode(rs.getString("bank_code"));
					ins.setBranchCode(rs.getString("branch_code"));
					ins.setBaseNumber(rs.getString("base_number"));
					ins.setTransactionCode(rs.getString("transaction_code"));
					list.add(ins);
				}
			}
		} catch (SQLException e) {
			logger.error("Error while fetching pending data", e);
		}
		return list;
	}

	public float getSumOfInstrumentAmountByBatchNumber(String batchNumber) throws SQLException {

		String sql = "SELECT COALESCE(SUM(instrument_amount), 0) AS total_amount " + "FROM cheque_batch_instruments "
				+ "WHERE batch_number = ? AND is_deleted = FALSE AND instrument_amount IS NOT NULL";

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, batchNumber);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getFloat("total_amount");
				}
			}
		} catch (SQLException e) {
			logger.error("Error while fetching cheques amount", e);
		}
		return 0f;
	}

	public void forceUpdateBatchAmountDao(Float batchAmount, String batchNumber) throws SQLException {
		String sql = "UPDATE cheque_batch_instruments SET batch_amount = ? WHERE batch_number = ?";

		try (Connection conn = DataBaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setFloat(1, batchAmount);
			ps.setString(2, batchNumber);
			ps.executeUpdate();

		} catch (SQLException e) {
			logger.error("Error while force balance", e);
		}
	}
}
