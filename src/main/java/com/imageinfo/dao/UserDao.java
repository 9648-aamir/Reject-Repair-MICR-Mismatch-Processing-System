package com.imageinfo.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.Logger;

import com.imageinfo.connection.DataBaseConnection;
import com.imageinfo.model.User;
import com.imageinfo.util.LoggerUtil;

public class UserDao {

	private static final Logger logger = LoggerUtil.getLogger(UserDao.class);
	
    public User login(String username, String password) {
        User user = null;
        String sql = "SELECT username,id FROM users WHERE username=? AND password=?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
            }

        } catch (Exception e) {
        	logger.error("Error in selecting username, password",e);
        }

        return user;
    }
}

