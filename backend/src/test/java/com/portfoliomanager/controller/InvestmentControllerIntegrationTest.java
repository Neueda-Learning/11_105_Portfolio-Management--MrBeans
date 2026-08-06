package com.portfoliomanager.controller;

import com.portfoliomanager.BaseIntegrationTest;
import com.portfoliomanager.dto.investment.CreateInvestmentRequest;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.dto.investment.InvestmentResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InvestmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void canCreateAndRetrieveInvestment() {
        CreateInvestmentRequest req = new CreateInvestmentRequest();
        req.setSymbol("GOOGL");
        req.setName("Alphabet Inc.");
        req.setType(InvestmentType.STOCK);
        req.setCurrency("USD");
        req.setMetadata(Map.of("sector", "Technology"));

        ResponseEntity<InvestmentResponse> postResponse = restTemplate.postForEntity("/api/investments", req, InvestmentResponse.class);
        
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getBody().getId()).isNotNull();
        
        // Retrieve it back
        ResponseEntity<InvestmentResponse> getResponse = restTemplate.getForEntity(
            "/api/investments/" + postResponse.getBody().getId(), 
            InvestmentResponse.class
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getSymbol()).isEqualTo("GOOGL");
    }

    @Test
    void rejectsInvalidInvestmentData() {
        CreateInvestmentRequest req = new CreateInvestmentRequest();
        req.setSymbol(""); // Invalid, must not be blank
        req.setType(InvestmentType.STOCK);
        req.setCurrency("US"); // Invalid, usually 3 chars

        ResponseEntity<String> postResponse = restTemplate.postForEntity("/api/investments", req, String.class);
        
        // Validation should trigger 400 Bad Request
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
