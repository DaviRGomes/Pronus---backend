package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
// Adicionamos a importação para as classes de segurança do Spring
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Usuario")
@Inheritance(strategy = InheritanceType.JOINED)
// Implementar UserDetails é o padrão do Spring Security
public class UsuarioEntity implements UserDetails { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private Integer idade;

    private String endereco;
    
    // ⬇️ CAMPOS DE AUTENTICAÇÃO ADICIONADOS ⬇️
    @Column(nullable = false, unique = true)
    private String login; // Pode ser email ou username
    
    @Column(nullable = false)
    private String senha;
    // ⬆️ CAMPOS DE AUTENTICAÇÃO ADICIONADOS ⬆️


    // =================================================================
    // MÉTODOS DE UserDetails (Interface do Spring Security)
    // =================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this instanceof EspecialistaEntity) {
            return List.of(new SimpleGrantedAuthority("ROLE_ESPECIALISTA"));
        } else if (this instanceof ClienteEntity) {
            return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        } else if (this instanceof SecretariaEntity) {
            return List.of(new SimpleGrantedAuthority("ROLE_SECRETARIA"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    // Deixamos true por padrão para que o usuário esteja sempre ativo
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}