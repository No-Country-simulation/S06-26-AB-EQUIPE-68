package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Mentor;
import com.bitsystem.bitapp.repository.MentorRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Seed dos 4 mentores voluntários da equipe, com áreas atendidas conforme
 * decisão final do André (2026-07-23): André=Java/DevOps/IA, Carlos=Web/Mobile/UIUX,
 * Daniela=Java/Web/Mobile/Dados (QA cruza as áreas "de código"), Tiago Farias=IA.
 * Sala única (Google Meet) compartilhada pelos 4.
 */
@Service
@Order(3) // boot: 3º — entre catálogo de cursos e o seed de demo
public class MentorSeedService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MentorSeedService.class);

    private static final String LINK_SALA = "https://meet.google.com/kin-zmkx-rhs";

    private final MentorRepository mentorRepository;

    public MentorSeedService(MentorRepository mentorRepository) {
        this.mentorRepository = mentorRepository;
    }

    @Override
    public void run(String... args) {
        if (mentorRepository.count() > 0) {
            log.info("MentorSeedService: Banco já possui mentores, pulando seed.");
            return;
        }

        log.info("MentorSeedService: Iniciando geração de mentores...");

        List<Mentor> mentores = List.of(
            new Mentor("André Teixeira", "Backend", "Java,DevOps,IA", LINK_SALA),
            new Mentor("Carlos", "Frontend", "Web,Mobile,UIUX", LINK_SALA),
            new Mentor("Daniela", "Qualidade de Software", "Java,Web,Mobile,Dados", LINK_SALA),
            new Mentor("Tiago Farias", "Agentes de IA", "IA", LINK_SALA)
        );

        mentorRepository.saveAll(mentores);
        log.info("MentorSeedService: {} mentores gerados com sucesso!", mentores.size());
    }
}
