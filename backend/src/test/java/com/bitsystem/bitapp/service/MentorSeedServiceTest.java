package com.bitsystem.bitapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bitsystem.bitapp.domain.Mentor;
import com.bitsystem.bitapp.repository.MentorRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MentorSeedServiceTest {

    @Mock private MentorRepository mentorRepository;

    @Test
    @SuppressWarnings("unchecked")
    void run_bancoVazio_semeiaOsQuatroMentores() {
        when(mentorRepository.count()).thenReturn(0L);
        MentorSeedService service = new MentorSeedService(mentorRepository);

        service.run();

        ArgumentCaptor<List<Mentor>> captor = ArgumentCaptor.forClass(List.class);
        verify(mentorRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(4);
        assertThat(captor.getValue()).extracting(Mentor::getNome)
                .containsExactlyInAnyOrder("André Teixeira", "Carlos", "Daniela", "Tiago Farias");
    }

    @Test
    void run_bancoJaPossuiMentores_naoSemeiaNovamente() {
        when(mentorRepository.count()).thenReturn(4L);
        MentorSeedService service = new MentorSeedService(mentorRepository);

        service.run();
        service.run();

        verify(mentorRepository, never()).saveAll(any());
    }
}
