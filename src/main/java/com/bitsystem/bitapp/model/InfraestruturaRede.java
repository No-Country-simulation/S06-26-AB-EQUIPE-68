package com.bitsystem.bitapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

/**
 * ============================================================================
 * ENTIDADE JPA: InfraestruturaRede
 * ============================================================================
 *
 * Representa uma torre de infraestrutura de rede (3G, 4G, 5G) com sua localização
 * geográfica. Persistido em infraestrutura_rede no MySQL.
 *
 * @author BiT System
 * @version 1.0.0
 */
@Entity
@Table(name = "infraestrutura_rede")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class InfraestruturaRede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_estacao")
    private String codigoEstacao;

    private String operadora;

    @Column(name = "tipo_tecnologia")
    private String tipoTecnologia; // 3G, 4G, 5G

    @Column(name = "densidade_populacional")
    private Double densidadePopulacional;

    @Column(columnDefinition = "GEOMETRY") // Define explicitamente o tipo para o Hibernate e MySQL
    private Point posicao;
}