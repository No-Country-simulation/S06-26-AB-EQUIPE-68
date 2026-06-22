package com.bitsystem.bitapp.repository;

import com.bitsystem.bitapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================================
 * REPOSITORY: UsuarioRepository
 * ============================================================================
 * 
 * Data Access Layer para entidade Usuario.
 * Fornece operações CRUD + queries customizadas.
 * 
 * RESPONSABILIDADES:
 * - CRUD automático: save(), findById(), findAll(), delete()
 * - Query custom: findByEmail() para buscar por email único
 * - Abstração do SQL → Spring Data JPA gera queries em tempo de exec
 * 
 * HERANÇA:
 * - JpaRepository<Usuario, Long>:
 *   ├─ Entity: Usuario
 *   └─ PK type: Long (id)
 * 
 * MÉTODOS HERDADOS (automáticos):
 * - save(usuario): INSERT/UPDATE na tb_usuarios
 * - findById(id): SELECT by PK
 * - findAll(): SELECT * from tb_usuarios
 * - delete(usuario): DELETE by PK
 * - count(): SELECT COUNT(*) from tb_usuarios
 * 
 * @author BiT System
 * @version 1.0.0
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Query customizada: busca usuário por email único
     * 
     * Spring Data JPA interpreta o nome do método:
     * - findBy + Email → SELECT * FROM tb_usuarios WHERE email = ?
     * 
     * RETORNA:
     * - Optional<Usuario>: present se encontrou, empty se não
     * 
     * CASOS DE USO:
     * - Login (username/email lookup)
     * - Validação de email único (evitar duplicatas)
     * - Recovery de conta (forgot password)
     * 
     * EXEMPLO:
     * Optional<Usuario> usuario = usuarioRepository.findByEmail("alice@example.com");
     * usuario.ifPresent(u -> System.out.println("Email já existe!"));
     * 
     * @param email Email do usuário
     * @return Optional contendo Usuario se encontrado
     */
    Optional<Usuario> findByEmail(String email);
}
