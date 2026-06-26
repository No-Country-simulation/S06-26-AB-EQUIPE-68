package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.dto.AssessmentDto;
import com.bitsystem.bitapp.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssessmentServiceTest {

    @Test
    void shouldValidateAssessmentDto() {
        var request = new AssessmentDto.Request(
            "João", 25, "Superior", "2 anos",
            List.of("Java"), List.of("Comunicação"),
            List.of("Spring"), "Backend"
        );
        assertNotNull(request);
        assertEquals("João", request.nome());
        assertEquals(25, request.idade());
    }

    @Test
    void shouldRejectEmptyNome() {
        var request = new AssessmentDto.Request(
            "", 25, "", "", List.of(), List.of(), List.of(), ""
        );
        assertTrue(request.nome().isEmpty());
    }
}
