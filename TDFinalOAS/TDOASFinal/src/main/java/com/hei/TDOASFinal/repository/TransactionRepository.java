package com.hei.TDOASFinal.repository;

import com.hei.TDOASFinal.config.DatabaseConnection;
import com.hei.TDOASFinal.model.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionRepository {

    private final FinancialAccountRepository accountRepository;
    private final MemberRepository memberRepository;

    public TransactionRepository(FinancialAccountRepository accountRepository, MemberRepository memberRepository) {
        this.accountRepository = accountRepository;
        this.memberRepository = memberRepository;
    }

    public List<CollectivityTransaction> findByCollectivityAndDates(String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        List<CollectivityTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE collectivity_id = ? AND creation_date >= ? AND creation_date <= ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, collectivityId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public CollectivityTransaction save(String collectivityId, CollectivityTransaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (id, collectivity_id, creation_date, amount, payment_mode, account_credited_id, member_debited_id) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, transaction.getId());
            ps.setString(2, collectivityId);
            ps.setDate(3, Date.valueOf(transaction.getCreationDate()));
            ps.setDouble(4, transaction.getAmount());
            ps.setString(5, transaction.getPaymentMode().name());
            ps.setString(6, transaction.getAccountCredited().getId());
            ps.setString(7, transaction.getMemberDebited().getId());
            ps.executeUpdate();
        }
        return transaction;
    }

    private CollectivityTransaction map(ResultSet rs) throws SQLException {
        CollectivityTransaction t = new CollectivityTransaction();
        t.setId(rs.getString("id"));
        t.setCreationDate(rs.getDate("creation_date").toLocalDate());
        t.setAmount(rs.getDouble("amount"));
        t.setPaymentMode(PaymentMode.valueOf(rs.getString("payment_mode")));
        
        String accountId = rs.getString("account_credited_id");
        t.setAccountCredited(accountRepository.findById(accountId).orElse(null));
        
        String memberId = rs.getString("member_debited_id");
        t.setMemberDebited(memberRepository.findById(memberId).orElse(null));
        
        return t;
    }
}
