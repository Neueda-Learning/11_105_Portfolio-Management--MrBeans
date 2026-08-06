package com.portfoliomanager.service;

import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.dto.investment.CreateInvestmentRequest;
import com.portfoliomanager.dto.investment.InvestmentResponse;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.dto.investment.UpdateInvestmentRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentService investmentService;

    private Investment testInvestment;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testInvestment = new Investment();
        testInvestment.setId(testId);
        testInvestment.setSymbol("AAPL");
        testInvestment.setName("Apple Inc.");
        testInvestment.setType(InvestmentType.STOCK);
        testInvestment.setCurrency("USD");
        testInvestment.setMetadata(Map.of("sector", "Technology"));
    }

    @Test
    void createInvestment_Success() {
        CreateInvestmentRequest request = new CreateInvestmentRequest();
        request.setSymbol("MSFT");
        request.setName("Microsoft");
        request.setType(InvestmentType.STOCK);
        request.setCurrency("USD");
        request.setMetadata(Map.of("sector", "Tech"));

        when(investmentRepository.save(any(Investment.class))).thenAnswer(invocation -> {
            Investment saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        InvestmentResponse response = investmentService.createInvestment(request);

        assertNotNull(response.getId());
        assertEquals("MSFT", response.getSymbol());
        assertEquals("Microsoft", response.getName());
        assertEquals(InvestmentType.STOCK, response.getType());
        assertEquals("USD", response.getCurrency());
        assertEquals("Tech", response.getMetadata().get("sector"));
    }

    @Test
    void getInvestmentById_Success() {
        when(investmentRepository.findById(testId)).thenReturn(Optional.of(testInvestment));

        InvestmentResponse response = investmentService.getInvestmentById(testId);

        assertEquals(testId, response.getId());
        assertEquals("AAPL", response.getSymbol());
    }

    @Test
    void getInvestmentById_NotFound() {
        when(investmentRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> investmentService.getInvestmentById(testId));
    }

    @Test
    void updateInvestment_Success() {
        when(investmentRepository.findById(testId)).thenReturn(Optional.of(testInvestment));
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        UpdateInvestmentRequest request = new UpdateInvestmentRequest();
        request.setName("Apple Inc. Updated");
        request.setMetadata(Map.of("sector", "Hardware"));

        InvestmentResponse response = investmentService.updateInvestment(testId, request);

        assertEquals("AAPL", response.getSymbol()); // Unchanged
        assertEquals("Apple Inc. Updated", response.getName()); // Changed
        assertEquals("Hardware", response.getMetadata().get("sector")); // Changed
    }

    @Test
    void deleteInvestment_Success() {
        when(investmentRepository.findById(testId)).thenReturn(Optional.of(testInvestment));
        doNothing().when(investmentRepository).delete(testInvestment);

        investmentService.deleteInvestment(testId);

        verify(investmentRepository, times(1)).delete(testInvestment);
    }
    
    @Test
    void getAllInvestments_Success() {
        when(investmentRepository.findAll()).thenReturn(List.of(testInvestment));
        
        List<InvestmentResponse> responses = investmentService.getAllInvestments();
        
        assertEquals(1, responses.size());
        assertEquals(testId, responses.get(0).getId());
    }
}
