package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.model.CompanyProfile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CompanyProfileDAO {
    public CompanyProfile load() {
        String sql = """
                SELECT company_id, company_name, company_address, tin, sss_no, phone, fax, email,
                       executive_name, is_read_only, auto_ac, updated_by
                FROM company_profile
                ORDER BY company_id
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                CompanyProfile profile = new CompanyProfile();
                profile.setCompanyId(rs.getLong("company_id"));
                profile.setCompanyName(rs.getString("company_name"));
                profile.setCompanyAddress(rs.getString("company_address"));
                profile.setTin(rs.getString("tin"));
                profile.setSssNo(rs.getString("sss_no"));
                profile.setPhone(rs.getString("phone"));
                profile.setFax(rs.getString("fax"));
                profile.setEmail(rs.getString("email"));
                profile.setExecutiveName(rs.getString("executive_name"));
                profile.setReadOnly(rs.getBoolean("is_read_only"));
                profile.setAutoAc(rs.getBoolean("auto_ac"));
                profile.setUpdatedBy(rs.getString("updated_by"));
                return profile;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load company profile: " + e.getMessage(), e);
        }
        CompanyProfile fallback = new CompanyProfile();
        fallback.setCompanyName("RVS FISHWORLD, INC.");
        return fallback;
    }
}
