package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.*;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class MemberPaymentRepository {

    private final FinancialAccountRepository accountRepository;

    public MemberPaymentRepository(FinancialAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public MemberPayment save(MemberPayment payment, String memberId, String feeId, String accountId) throws SQLException {
        String sql = "INSERT INTO member_payments (id, member_id, membership_fee_id, account_credited_id, amount, payment_mode, creation_date) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, payment.getId());
            ps.setString(2, memberId);
            ps.setString(3, feeId);
            ps.setString(4, accountId);
            ps.setDouble(5, payment.getAmount());
            ps.setString(6, payment.getPaymentMode().name());
            ps.setDate(7, Date.valueOf(payment.getCreationDate()));
            ps.executeUpdate();
        }
        payment.setAccountCredited(accountRepository.findById(accountId).orElse(null));
        return payment;
    }
}
