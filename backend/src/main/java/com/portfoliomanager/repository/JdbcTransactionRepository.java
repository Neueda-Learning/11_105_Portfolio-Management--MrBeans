package com.portfoliomanager.repository;

import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcTransactionRepository implements TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> rowMapper = (rs, rowNum) -> {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.fromString(rs.getString("id")));
        transaction.setInvestmentId(UUID.fromString(rs.getString("investment_id")));
        transaction.setType(TransactionType.valueOf(rs.getString("type")));
        transaction.setQuantity(rs.getBigDecimal("quantity"));
        transaction.setPrice(rs.getBigDecimal("price"));
        transaction.setCurrency(rs.getString("currency"));
        transaction.setFxRateToHome(rs.getBigDecimal("fx_rate_to_home"));

        Date txnDate = rs.getDate("txn_date");
        if (txnDate != null) {
            transaction.setTxnDate(txnDate.toLocalDate());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toInstant());
        }
        return transaction;
    };

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        String sql = "SELECT id, investment_id, type, quantity, price, currency, fx_rate_to_home, txn_date, created_at FROM transactions WHERE id = ?";
        List<Transaction> rows = jdbcTemplate.query(sql, rowMapper, id.toString());
        return rows.stream().findFirst();
    }

    @Override
    public Transaction save(Transaction transaction) {
        UUID id = transaction.getId() != null ? transaction.getId() : UUID.randomUUID();
        transaction.setId(id);

        if (findById(id).isPresent()) {
            updateTransaction(transaction);
        } else {
            insertTransaction(transaction);
        }

        return findById(id).orElseThrow(() -> new IllegalStateException("Transaction save failed for id " + id));
    }

    @Override
    public List<Transaction> saveAll(List<Transaction> transactions) {
        List<Transaction> saved = new ArrayList<>(transactions.size());
        for (Transaction transaction : transactions) {
            saved.add(save(transaction));
        }
        return saved;
    }

    @Override
    public void delete(Transaction transaction) {
        if (transaction.getId() == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM transactions WHERE id = ?", transaction.getId().toString());
    }

    @Override
    public List<Transaction> findByInvestmentIdOrderByTxnDateAsc(UUID investmentId) {
        String sql = "SELECT id, investment_id, type, quantity, price, currency, fx_rate_to_home, txn_date, created_at "
                + "FROM transactions WHERE investment_id = ? ORDER BY txn_date ASC";
        return jdbcTemplate.query(sql, rowMapper, investmentId.toString());
    }

    private void insertTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (id, investment_id, type, quantity, price, currency, fx_rate_to_home, txn_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, transaction.getId().toString());
            ps.setString(2, transaction.getInvestmentId().toString());
            ps.setString(3, transaction.getType().name());
            ps.setBigDecimal(4, transaction.getQuantity());
            ps.setBigDecimal(5, transaction.getPrice());
            ps.setString(6, transaction.getCurrency());
            if (transaction.getFxRateToHome() == null) {
                ps.setNull(7, Types.DECIMAL);
            } else {
                ps.setBigDecimal(7, transaction.getFxRateToHome());
            }
            ps.setDate(8, Date.valueOf(transaction.getTxnDate()));
            return ps;
        });
    }

    private void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET investment_id = ?, type = ?, quantity = ?, price = ?, currency = ?, "
                + "fx_rate_to_home = ?, txn_date = ? WHERE id = ?";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, transaction.getInvestmentId().toString());
            ps.setString(2, transaction.getType().name());
            ps.setBigDecimal(3, transaction.getQuantity());
            ps.setBigDecimal(4, transaction.getPrice());
            ps.setString(5, transaction.getCurrency());
            if (transaction.getFxRateToHome() == null) {
                ps.setNull(6, Types.DECIMAL);
            } else {
                ps.setBigDecimal(6, transaction.getFxRateToHome());
            }
            ps.setDate(7, Date.valueOf(transaction.getTxnDate()));
            ps.setString(8, transaction.getId().toString());
            return ps;
        });
    }
}