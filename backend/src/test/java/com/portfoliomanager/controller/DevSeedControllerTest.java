package com.portfoliomanager.controller;

import com.portfoliomanager.service.DevDataSeederService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevSeedControllerTest {

    @Mock private DevDataSeederService devDataSeederService;

    private DevSeedController controller;

    @BeforeEach
    void setUp() {
        controller = new DevSeedController(devDataSeederService);
    }

    @Test
    void seed_ValidParams_DelegatesToService() {
        DevDataSeederService.SeedSummary summary =
                new DevDataSeederService.SeedSummary(10, 50, 200);
        when(devDataSeederService.seed(10, 5, 60, true)).thenReturn(summary);

        Map<String, Object> result = controller.seed(10, 5, 60, true);

        assertEquals("Seed data generated successfully", result.get("message"));
        assertEquals(10, result.get("investments"));
        assertEquals(50, result.get("transactions"));
        assertEquals(200, result.get("priceSnapshots"));
    }

    @Test
    void seed_InvalidInvestments_Low_Throws() {
        assertThrows(ResponseStatusException.class,
                () -> controller.seed(0, 10, 60, true));
    }

    @Test
    void seed_InvalidInvestments_High_Throws() {
        assertThrows(ResponseStatusException.class,
                () -> controller.seed(201, 10, 60, true));
    }

    @Test
    void seed_InvalidTransactions_Low_Throws() {
        assertThrows(ResponseStatusException.class,
                () -> controller.seed(10, 1, 60, true));
    }

    @Test
    void seed_InvalidTransactions_High_Throws() {
        assertThrows(ResponseStatusException.class,
                () -> controller.seed(10, 501, 60, true));
    }

    @Test
    void seed_InvalidSnapshotDays_Low_Throws() {
        assertThrows(ResponseStatusException.class,
                () -> controller.seed(10, 10, 29, true));
    }

    @Test
    void seed_InvalidSnapshotDays_High_Throws() {
        assertThrows(ResponseStatusException.class,
                () -> controller.seed(10, 10, 731, true));
    }
}
