package com.bitsystem.bitapp.service;

import com.bitsystem.bitapp.domain.Vaga;
import com.bitsystem.bitapp.dto.VagaDto;
import com.bitsystem.bitapp.repository.VagaRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VagaService {

    private final VagaRepository vagaRepository;

    public VagaService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    public List<VagaDto> listarTodas() {
        return vagaRepository.findByAtivaTrue().stream()
                .map(VagaDto::from)
                .collect(Collectors.toList());
    }

    public VagaDto buscarPorId(Long id) {
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada: " + id));
        return VagaDto.from(vaga);
    }

    public List<VagaDto> buscarComFiltros(String q, String regiao, String nivel, String area, String contrato) {
        String qNorm = (q == null || q.isBlank()) ? null : q.trim();
        String rNorm = (regiao == null || regiao.isBlank()) ? null : regiao.trim();
        String nNorm = (nivel == null || nivel.isBlank()) ? null : nivel.trim();
        String aNorm = (area == null || area.isBlank()) ? null : area.trim();
        String cNorm = (contrato == null || contrato.isBlank()) ? null : contrato.trim();

        return vagaRepository.buscarComFiltros(qNorm, rNorm, nNorm, aNorm, cNorm).stream()
                .map(VagaDto::from)
                .collect(Collectors.toList());
    }

    public List<String> listarRegioes() {
        return vagaRepository.findByAtivaTrue().stream()
                .map(Vaga::getRegiao)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public long contarVagas() {
        return vagaRepository.findByAtivaTrue().size();
    }

    public Vaga salvar(Vaga vaga) {
        return vagaRepository.save(vaga);
    }

    public void salvarTodas(List<Vaga> vagas) {
        vagaRepository.saveAll(vagas);
    }
}
