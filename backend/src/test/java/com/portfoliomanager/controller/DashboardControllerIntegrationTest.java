package com.portfoliomanager.controller;

import com.portfoliomanager.BaseIntegrationTest;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DashboardControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getTrendFiltered_ReturnsDateRangeSeries() {
        ResponseEntity<TrendResponse[]> response = restTemplate.getForEntity(
                "/api/dashboard/trend/filter?fromDate=2026-01-01&toDate=2026-01-05",
                TrendResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(5);
    }

    @Test
    void getTrendFiltered_RejectsPartialDateRange() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/dashboard/trend/filter?fromDate=2026-01-01",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getTrendFiltered_RejectsInvalidType() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/dashboard/trend/filter?types=INVALID_TYPE",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
