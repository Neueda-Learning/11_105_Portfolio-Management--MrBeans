package com.portfoliomanager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcInvestmentRepository implements InvestmentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RowMapper<Investment> rowMapper;

    public JdbcInvestmentRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.rowMapper = (rs, rowNum) -> {
            Investment investment = new Investment();
            investment.setId(UUID.fromString(rs.getString("id")));
            investment.setSymbol(rs.getString("symbol"));
            investment.setName(rs.getString("name"));
            investment.setType(InvestmentType.valueOf(rs.getString("type")));
            investment.setCurrency(rs.getString("currency"));

            String metadataJson = rs.getString("metadata");
            if (metadataJson != null) {
                try {
                    Map<String, Object> metadata = this.objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {
                    });
                    investment.setMetadata(metadata);
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Failed to deserialize investment metadata", e);
                }
            }

            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                investment.setCreatedAt(createdAt.toInstant());
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                investment.setUpdatedAt(updatedAt.toInstant());
            }
            return investment;
        };
    }

    @Override
    public List<Investment> findAll() {
        String sql = "SELECT id, symbol, name, type, currency, metadata, created_at, updated_at FROM investments";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Investment> findById(UUID id) {
        String sql = "SELECT id, symbol, name, type, currency, metadata, created_at, updated_at FROM investments WHERE id = ?";
        List<Investment> rows = jdbcTemplate.query(sql, rowMapper, id.toString());
        return rows.stream().findFirst();
    }

    @Override
    public boolean existsById(UUID id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM investments WHERE id = ?", Integer.class, id.toString());
        return count != null && count > 0;
    }

    @Override
    public Investment save(Investment investment) {
        UUID id = investment.getId() != null ? investment.getId() : UUID.randomUUID();
        investment.setId(id);

        if (findById(id).isPresent()) {
            updateInvestment(investment);
        } else {
            insertInvestment(investment);
        }

        return findById(id).orElseThrow(() -> new IllegalStateException("Investment save failed for id " + id));
    }

    @Override
    public void delete(Investment investment) {
        if (investment.getId() == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM investments WHERE id = ?", investment.getId().toString());
    }

    @Override
    public void deleteAllInBatch() {
        jdbcTemplate.update("DELETE FROM investments");
    }

    private void insertInvestment(Investment investment) {
        String sql = "INSERT INTO investments (id, symbol, name, type, currency, metadata) VALUES (?, ?, ?, ?, ?, ?)";
        String metadataJson = toMetadataJson(investment.getMetadata());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, investment.getId().toString());
            ps.setString(2, investment.getSymbol());
            ps.setString(3, investment.getName());
            ps.setString(4, investment.getType().name());
            ps.setString(5, investment.getCurrency());
            if (metadataJson == null) {
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(6, metadataJson);
            }
            return ps;
        });
    }

    private void updateInvestment(Investment investment) {
        String sql = "UPDATE investments SET symbol = ?, name = ?, type = ?, currency = ?, metadata = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        String metadataJson = toMetadataJson(investment.getMetadata());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, investment.getSymbol());
            ps.setString(2, investment.getName());
            ps.setString(3, investment.getType().name());
            ps.setString(4, investment.getCurrency());
            if (metadataJson == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, metadataJson);
            }
            ps.setString(6, investment.getId().toString());
            return ps;
        });
    }

    private String toMetadataJson(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize investment metadata", e);
        }
    }
}