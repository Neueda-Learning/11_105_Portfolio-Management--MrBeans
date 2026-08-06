package com.portfoliomanager.repository;

import com.portfoliomanager.model.FxRate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcFxRateRepository implements FxRateRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<FxRate> rowMapper = (rs, rowNum) -> {
        FxRate fxRate = new FxRate();
        fxRate.setId(UUID.fromString(rs.getString("id")));
        fxRate.setFromCurrency(rs.getString("from_currency"));
        fxRate.setToCurrency(rs.getString("to_currency"));
        fxRate.setRate(rs.getBigDecimal("rate"));
        Date rateDate = rs.getDate("rate_date");
        if (rateDate != null) {
            fxRate.setRateDate(rateDate.toLocalDate());
        }
        return fxRate;
    };

    public JdbcFxRateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public FxRate save(FxRate fxRate) {
        UUID id = fxRate.getId() != null ? fxRate.getId() : UUID.randomUUID();
        fxRate.setId(id);

        if (findById(id).isPresent()) {
            updateFxRate(fxRate);
        } else {
            insertFxRate(fxRate);
        }

        return findById(id).orElseThrow(() -> new IllegalStateException("FxRate save failed for id " + id));
    }

    @Override
    public Optional<FxRate> findByFromCurrencyAndToCurrencyAndRateDate(String fromCurrency, String toCurrency, LocalDate rateDate) {
        String sql = "SELECT id, from_currency, to_currency, rate, rate_date FROM fx_rates "
                + "WHERE from_currency = ? AND to_currency = ? AND rate_date = ?";
        List<FxRate> rows = jdbcTemplate.query(sql, rowMapper, fromCurrency, toCurrency, Date.valueOf(rateDate));
        return rows.stream().findFirst();
    }

    private Optional<FxRate> findById(UUID id) {
        String sql = "SELECT id, from_currency, to_currency, rate, rate_date FROM fx_rates WHERE id = ?";
        List<FxRate> rows = jdbcTemplate.query(sql, rowMapper, id.toString());
        return rows.stream().findFirst();
    }

    private void insertFxRate(FxRate fxRate) {
        String sql = "INSERT INTO fx_rates (id, from_currency, to_currency, rate, rate_date) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                fxRate.getId().toString(),
                fxRate.getFromCurrency(),
                fxRate.getToCurrency(),
                fxRate.getRate(),
                Date.valueOf(fxRate.getRateDate()));
    }

    private void updateFxRate(FxRate fxRate) {
        String sql = "UPDATE fx_rates SET from_currency = ?, to_currency = ?, rate = ?, rate_date = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                fxRate.getFromCurrency(),
                fxRate.getToCurrency(),
                fxRate.getRate(),
                Date.valueOf(fxRate.getRateDate()),
                fxRate.getId().toString());
    }
}