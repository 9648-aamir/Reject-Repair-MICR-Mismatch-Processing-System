package com.imageinfo.lbnrrbnl.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.imageinfo.connection.DataBaseConnection;
import com.imageinfo.lbnrrbnl.model.Batch;
import com.imageinfo.util.LoggerUtil;

public class BatchDao {
	private static final Logger logger = LoggerUtil.getLogger(BatchDao.class);

	public List<Batch> fetchBatches(String clearingType) throws SQLException {
		List<Batch> list = new ArrayList<>();

		String sql = """
				SELECT
				    c.batch_number,
				    COUNT(c.cheque_number) AS cheque_count,
				    MAX(c.batch_amount) AS batch_amount,

				    SUM(
				        CASE
				            WHEN (c.matched='0')
				            THEN 1 ELSE 0
				        END
				    ) AS pending_count,

				    SUM(
				        CASE
				            WHEN (c.matched!='0')
				            THEN 1 ELSE 0
				        END
				    ) AS processed_count,

				    MIN(c.status) AS status,
				    MIN(c.user_id) AS user_id
				FROM cheque_batch_instrument c
				WHERE c.is_deleted = 0
				  AND c.batch_type = ?
				GROUP BY c.batch_number;
				""";

		try (Connection c = DataBaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

			ps.setString(1, clearingType);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Batch b = new Batch();
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
		} catch (SQLException e) {
			logger.error("Error while selecting batch from batch_list", e);
		}
		return list;
	}
}
