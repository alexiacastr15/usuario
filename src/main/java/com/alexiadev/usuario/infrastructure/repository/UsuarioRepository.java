package com.alexiadev.usuario.infrastructure.repository;

import com.alexiadev.usuario.infrastructure.entity.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    /*Optional é uma classe do Java Utill
    * Evitar retorno de informações nulas*/
    Optional<Usuario> findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}
