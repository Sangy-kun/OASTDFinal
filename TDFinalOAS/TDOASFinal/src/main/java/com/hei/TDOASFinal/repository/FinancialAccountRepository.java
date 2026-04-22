package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
public class FinancialAccountRepository {

    public Optional<FinancialAccount> findById(String id) throws SQLException {
        String sql = "SELECT * FROM financial_accounts WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String type = rs.getString("type");
                if ("CASH".equals(type)) {
                    CashAccount ca = new CashAccount();
                    ca.setId(rs.getString("id"));
                    ca.setAmount(rs.getInt("balance"));
                    return Optional.of(ca);
                } else if ("BANK".equals(type)) {
                    BankAccount ba = new BankAccount();
                    ba.setId(rs.getString("id"));
                    ba.setAmount(rs.getDouble("balance"));
                    ba.setHolderName(rs.getString("holder_name"));
                    String bankNameStr = rs.getString("bank_name");
                    if (bankNameStr != null) ba.setBankName(Bank.valueOf(bankNameStr));
                    ba.setBankCode(rs.getInt("bank_code"));
                    ba.setBankBranchCode(rs.getInt("bank_branch_code"));
                    ba.setBankAccountNumber(rs.getInt("bank_account_number"));
                    ba.setBankAccountKey(rs.getInt("bank_account_key"));
                    return Optional.of(ba);
                } else if ("MOBILE_BANKING".equals(type)) {
                    MobileBankingAccount ma = new MobileBankingAccount();
                    ma.setId(rs.getString("id"));
                    ma.setAmount(rs.getDouble("balance"));
                    ma.setHolderName(rs.getString("holder_name"));
                    String mbs = rs.getString("mobile_banking_service");
                    if (mbs != null) ma.setMobileBankingService(MobileBankingService.valueOf(mbs));
                    ma.setMobileNumber(rs.getInt("mobile_number"));
                    return Optional.of(ma);
                }
            }
        }
        return Optional.empty();
    }

    public void creditAccount(String accountId, int amount) throws SQLException {
        String sql = "UPDATE financial_accounts SET balance = balance + ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, accountId);
            ps.executeUpdate();
        }
    }
}
