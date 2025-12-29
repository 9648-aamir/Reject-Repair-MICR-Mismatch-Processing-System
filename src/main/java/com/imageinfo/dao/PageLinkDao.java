package com.imageinfo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.Logger;

import com.imageinfo.connection.DataBaseConnection;
import com.imageinfo.util.LoggerUtil;

public class PageLinkDao {

	private static final Logger logger = LoggerUtil.getLogger(PageLinkDao.class);
	
    public String getZulPath(String pageKey) {
        String path = null;
        String sql = "SELECT zul_path FROM page_links WHERE page_key = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pageKey);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                path = rs.getString("zul_path");
            }

        } catch (Exception e) {
        	logger.error("Error in selecting zul_path using page_key",e);
        }

        return path;
    }
}
