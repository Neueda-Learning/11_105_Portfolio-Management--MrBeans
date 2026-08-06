package com.portfoliomanager.repository;

import com.portfoliomanager.model.PriceSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcPriceSnapshotRepository implements PriceSnapshotRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PriceSnapshot> rowMapper = (rs, rowNum) -> {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setId(UUID.fromString(rs.getString("id")));
        snapshot.setInvestmentId(UUID.fromString(rs.getString("investment_id")));
        snapshot.setPrice(rs.getBigDecimal("price"));
        snapshot.setCurrency(rs.getString("currency"));

        Timestamp fetchedAt = rs.getTimestamp("fetched_at");
        if (fetchedAt != null) {
            snapshot.setFetchedAt(fetchedAt.toInstant());
        }
        return snapshot;
    };

    public JdbcPriceSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PriceSnapshot save(PriceSnapshot snapshot) {
        UUID id = snapshot.getId() != null ? snapshot.getId() : UUID.randomUUID();
        snapshot.setId(id);

        if (findById(id).isPresent()) {
            updateSnapshot(snapshot);
        } else {
            insertSnapshot(snapshot);
        }

        return findById(id).orElseThrow(() -> new IllegalStateException("PriceSnapshot save failed for id " + id));
    }

    @Override
    public List<PriceSnapshot> findByInvestmentIdOrderByFetchedAtDesc(UUID investmentId) {
        String sql = "SELECT id, investment_id, price, currency, fetched_at FROM price_snapshots "
                + "WHERE investment_id = ? ORDER BY fetched_at DESC";
        return jdbcTemplate.query(sql, rowMapper, investmentId.toString());
    }

    @Override
    public void deleteAllInBatch() {
        jdbcTemplate.update("DELETE FROM price_snapshots");
    }

    @Override
    public void deleteByInvestmentId(UUID investmentId) {
        jdbcTemplate.update("DELETE FROM price_snapshots WHERE investment_id = ?", investmentId.toString());
    }

    private java.util.Optional<PriceSnapshot> findById(UUID id) {
        String sql = "SELECT id, investment_id, price, currency, fetched_at FROM price_snapshots WHERE id = ?";
        List<PriceSnapshot> rows = jdbcTemplate.query(sql, rowMapper, id.toString());
        return rows.stream().findFirst();
    }

    private void insertSnapshot(PriceSnapshot snapshot) {
        String sql = "INSERT INTO price_snapshots (id, investment_id, price, currency, fetched_at) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, snapshot.getId().toString());
            ps.setString(2, snapshot.getInvestmentId().toString());
            ps.setBigDecimal(3, snapshot.getPrice());
            ps.setString(4, snapshot.getCurrency());
            ps.setTimestamp(5, Timestamp.from(snapshot.getFetchedAt()));
            return ps;
        });
    }

    private void updateSnapshot(PriceSnapshot snapshot) {
        String sql = "UPDATE price_snapshots SET investment_id = ?, price = ?, currency = ?, fetched_at = ? WHERE id = ?";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, snapshot.getInvestmentId().toString());
            ps.setBigDecimal(2, snapshot.getPrice());
            ps.setString(3, snapshot.getCurrency());
            ps.setTimestamp(4, Timestamp.from(snapshot.getFetchedAt()));
            ps.setString(5, snapshot.getId().toString());
            return ps;
        });
    }
}