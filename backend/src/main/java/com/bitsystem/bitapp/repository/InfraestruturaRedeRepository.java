package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.model.InfraestruturaRede;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ============================================================================
 * REPOSITÓRIO JPA: InfraestruturaRedeRepository
 * ============================================================================
 *
 * Repositório para operações de persistência e consulta da entidade InfraestruturaRede.
 * Inclui uma query nativa para buscar torres de rede próximas a um ponto geográfico.
 *
 * @author BiT System
 * @version 1.0.0
 */
public interface InfraestruturaRedeRepository extends JpaRepository<InfraestruturaRede, Integer> {

    /**
     * Busca torres de rede próximas a um ponto geográfico (pontoAluno) dentro de um raio especificado (raioMetros).
     * Utiliza a função nativa do MySQL ST_Distance_Sphere para cálculo de distância em uma esfera (Terra).
     *
     * @param pontoAluno O ponto geográfico do aluno para referência da busca.
     * @param raioMetros O raio em metros para buscar torres de rede.
     * @return Uma lista de objetos InfraestruturaRede encontrados dentro do raio.
     */
    @Query(value = "SELECT * FROM infraestrutura_rede WHERE ST_Distance_Sphere(posicao, :pontoAluno) <= :raioMetros", nativeQuery = true)
    List<InfraestruturaRede> buscarTorresProximas(@Param("pontoAluno") Point pontoAluno, @Param("raioMetros") double raioMetros);
}
