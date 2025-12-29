package com.imageinfo.caebb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import com.imageinfo.caebb.model.Batch;
import com.imageinfo.connection.DataBaseConnection;


public class BatchDao {
	private static final Logger logger = com.imageinfo.util.LoggerUtil.getLogger(BatchDao.class);
	
	public List<Batch> fetchBatches(String processType, String clearingType) throws SQLException {
		List<Batch> list = new ArrayList<>();

		String sql = """
				SELECT
				    c.batch_id,
				    c.batch_number,
				    COUNT(c.cheque_id) AS cheque_count,
				    SUM(DISTINCT c.batch_amount) AS batch_amount,
				    SUM(
				        CASE
				            WHEN (c.instrument_amount IS NULL)
				              OR (c.city_code IS NULL OR c.city_code = '')
				              OR (c.cheque_date IS NULL)
				            THEN 1 ELSE 0
				        END
				    ) AS pending_count,
				    SUM(
				        CASE
				            WHEN (c.instrument_amount IS NOT NULL)
				              AND (c.city_code IS NOT NULL AND c.city_code <> '')
				              AND (c.cheque_date IS NOT NULL)
				            THEN 1 ELSE 0
				        END
				    ) AS processed_count,
				    MIN(c.status) AS status,
				    MIN(c.user_id) AS user_id
				FROM cheque_batch_instruments c
				WHERE c.is_deleted = FALSE
				  AND c.process_type = COALESCE(?::process_type_enum, c.process_type)
				  AND c.clearing_type = COALESCE(?::clearing_type_enum, c.clearing_type)
				GROUP BY c.batch_id, c.batch_number
				""";

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, processType);
			ps.setString(2, clearingType);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Batch b = new Batch();
					b.setBatchId(rs.getString("batch_id"));
					b.setBatchNumber(rs.getString("batch_number"));
					b.setChequeCount(rs.getInt("cheque_count"));
					b.setAmount(rs.getFloat("batch_amount"));
					b.setPendingCount(rs.getInt("pending_count"));
					b.setProcessedCount(rs.getInt("processed_count"));
					b.setStatus(rs.getString("status"));
					b.setUserId(rs.getString("user_id"));
					list.add(b);
				}
			}
		}catch (SQLException e) {
			logger.error("Error while selecting batch from batch_list", e);
		}
		return list;
	}

	public Batch getBatchScanCounts() throws SQLException {
		String sql = """
				            SELECT
				    COUNT(DISTINCT batch_number) AS batch_count,
				    SUM(
				        CASE
				            WHEN sub.filled_cheques = sub.total_cheques THEN 1
				            ELSE 0
				        END
				    ) AS scan_count
				FROM (
				    SELECT
				        batch_number,
				        COUNT(*) AS total_cheques,
				        SUM(
				            CASE
				                WHEN instrument_amount IS NOT NULL AND cheque_date IS NOT NULL THEN 1
				                ELSE 0
				            END
				        ) AS filled_cheques
				    FROM cheque_batch_instruments
				    WHERE is_deleted = FALSE
				    GROUP BY batch_number
				) sub

				        """;

		Batch batch = new Batch();

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int batchCount = rs.getInt("batch_count");
					int scanCount = rs.getInt("scan_count");
					int difference = batchCount - scanCount;

					batch.setBatchCount(batchCount);
					batch.setScanCount(scanCount);
					batch.setDifference(difference);
				}
			}
		}catch (SQLException e) {
			logger.error("Error while counting batch", e);
		}
		return batch;
	}

}
