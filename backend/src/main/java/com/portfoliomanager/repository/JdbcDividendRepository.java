package com.portfoliomanager.repository;

import com.portfoliomanager.model.Dividend;
import com.portfoliomanager.model.DividendMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcDividendRepository implements DividendRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Dividend> rowMapper = (rs, rowNum) -> {
        Dividend dividend = new Dividend();
        dividend.setId(UUID.fromString(rs.getString("id")));
        dividend.setInvestmentId(UUID.fromString(rs.getString("investment_id")));
        dividend.setAmount(rs.getBigDecimal("amount"));
        dividend.setCurrency(rs.getString("currency"));
        dividend.setMode(DividendMode.valueOf(rs.getString("mode")));

        Date exDate = rs.getDate("ex_date");
        if (exDate != null) {
            dividend.setExDate(exDate.toLocalDate());
        }

        Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) {
            dividend.setPaymentDate(paymentDate.toLocalDate());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            dividend.setCreatedAt(createdAt.toInstant());
        }
        return dividend;
    };

    public JdbcDividendRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Dividend save(Dividend dividend) {
        UUID id = dividend.getId() != null ? dividend.getId() : UUID.randomUUID();
        dividend.setId(id);

        if (findById(id).isPresent()) {
            updateDividend(dividend);
        } else {
            insertDividend(dividend);
        }

        return findById(id).orElseThrow(() -> new IllegalStateException("Dividend save failed for id " + id));
    }

    @Override
    public List<Dividend> findByInvestmentId(UUID investmentId) {
        String sql = "SELECT id, investment_id, amount, currency, mode, ex_date, payment_date, created_at "
                + "FROM dividends WHERE investment_id = ?";
        return jdbcTemplate.query(sql, rowMapper, investmentId.toString());
    }

    @Override
    public List<Dividend> findByInvestmentIdOrderByPaymentDateDesc(UUID investmentId) {
        String sql = "SELECT id, investment_id, amount, currency, mode, ex_date, payment_date, created_at "
                + "FROM dividends WHERE investment_id = ? ORDER BY payment_date DESC";
        return jdbcTemplate.query(sql, rowMapper, investmentId.toString());
    }

    @Override
    public Optional<Dividend> findById(UUID id) {
        String sql = "SELECT id, investment_id, amount, currency, mode, ex_date, payment_date, created_at FROM dividends WHERE id = ?";
        List<Dividend> rows = jdbcTemplate.query(sql, rowMapper, id.toString());
        return rows.stream().findFirst();
    }

    @Override
    public void delete(Dividend dividend) {
        if (dividend.getId() == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM dividends WHERE id = ?", dividend.getId().toString());
    }

    @Override
    public void deleteByInvestmentId(UUID investmentId) {
        jdbcTemplate.update("DELETE FROM dividends WHERE investment_id = ?", investmentId.toString());
    }

    @Override
    public void deleteAllInBatch() {
        jdbcTemplate.update("DELETE FROM dividends");
    }

    @Override
    public Optional<BigDecimal> sumNetAmountByPaymentDateBetween(LocalDate start, LocalDate end) {
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount - withholding_tax), 0) FROM dividends WHERE payment_date BETWEEN ? AND ?",
                BigDecimal.class,
                Date.valueOf(start),
                Date.valueOf(end)
        );
        return Optional.ofNullable(total);
    }

    private Optional<Dividend> findById(UUID id) {
        String sql = "SELECT id, investment_id, amount, currency, mode, ex_date, payment_date, created_at FROM dividends WHERE id = ?";
        List<Dividend> rows = jdbcTemplate.query(sql, rowMapper, id.toString());
        return rows.stream().findFirst();
    }

    private void insertDividend(Dividend dividend) {
        String sql = "INSERT INTO dividends (id, investment_id, amount, currency, mode, ex_date, payment_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, dividend.getId().toString());
            ps.setString(2, dividend.getInvestmentId().toString());
            ps.setBigDecimal(3, dividend.getAmount());
            ps.setString(4, dividend.getCurrency());
            ps.setString(5, dividend.getMode().name());
            if (dividend.getExDate() == null) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(dividend.getExDate()));
            }
            ps.setDate(7, Date.valueOf(dividend.getPaymentDate()));
            return ps;
        });
    }

    private void updateDividend(Dividend dividend) {
        String sql = "UPDATE dividends SET investment_id = ?, amount = ?, currency = ?, mode = ?, ex_date = ?, payment_date = ? WHERE id = ?";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, dividend.getInvestmentId().toString());
            ps.setBigDecimal(2, dividend.getAmount());
            ps.setString(3, dividend.getCurrency());
            ps.setString(4, dividend.getMode().name());
            if (dividend.getExDate() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(dividend.getExDate()));
            }
            ps.setDate(6, Date.valueOf(dividend.getPaymentDate()));
            ps.setString(7, dividend.getId().toString());
            return ps;
        });
    }
}