package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.entity.UsuarioEntity;
import com.inatel.prototipo_ia.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes do AutenticacaoService - integração com Spring Security
 *
 * Service usado pelo Spring Security pra buscar usuários no login
 * Implementa UserDetailsService - interface obrigatória do Spring Security
 */
@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    @Nested
    @DisplayName("Testes de Carregamento de Usuário por Username")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Deve carregar usuário com sucesso quando login existe")
        void deveCarregarUsuarioComSucesso() {
            // Setup de um login válido no banco
            String login = "usuario@teste.com";

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(1L);
            usuario.setLogin(login);
            usuario.setSenha("senha123");
            usuario.setNome("Usuário Teste");

            // Mock do repository retornando o usuário
            when(usuarioRepository.findByLogin(login)).thenReturn(usuario);

            // Chama o método que o Spring Security usa
            UserDetails resultado = autenticacaoService.loadUserByUsername(login);

            // Verifica se os dados vieram corretos
            assertThat(resultado).isNotNull();
            assertThat(resultado.getUsername()).isEqualTo(login);
            assertThat(resultado.getPassword()).isEqualTo("senha123");
            verify(usuarioRepository, times(1)).findByLogin(login);
        }

        @Test
        @DisplayName("Deve retornar null quando usuário não existe")
        void deveRetornarNullQuandoUsuarioNaoExiste() {
            String loginInexistente = "naoexiste@teste.com";

            when(usuarioRepository.findByLogin(loginInexistente)).thenReturn(null);

            UserDetails resultado = autenticacaoService.loadUserByUsername(loginInexistente);

            assertThat(resultado).isNull();
            verify(usuarioRepository, times(1)).findByLogin(loginInexistente);
        }

        @Test
        @DisplayName("Deve carregar diferentes usuários corretamente")
        void deveCarregarDiferentesUsuarios() {
            UsuarioEntity usuario1 = new UsuarioEntity();
            usuario1.setId(1L);
            usuario1.setLogin("usuario1@teste.com");
            usuario1.setSenha("senha1");

            UsuarioEntity usuario2 = new UsuarioEntity();
            usuario2.setId(2L);
            usuario2.setLogin("usuario2@teste.com");
            usuario2.setSenha("senha2");

            when(usuarioRepository.findByLogin("usuario1@teste.com")).thenReturn(usuario1);
            when(usuarioRepository.findByLogin("usuario2@teste.com")).thenReturn(usuario2);

            UserDetails resultado1 = autenticacaoService.loadUserByUsername("usuario1@teste.com");
            UserDetails resultado2 = autenticacaoService.loadUserByUsername("usuario2@teste.com");

            assertThat(resultado1.getUsername()).isEqualTo("usuario1@teste.com");
            assertThat(resultado2.getUsername()).isEqualTo("usuario2@teste.com");
            assertThat(resultado1.getPassword()).isEqualTo("senha1");
            assertThat(resultado2.getPassword()).isEqualTo("senha2");
        }

        @Test
        @DisplayName("Deve chamar repository com o username correto")
        void deveChamarRepositoryComUsernameCorreto() {
            String username = "teste@exemplo.com";

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setLogin(username);

            when(usuarioRepository.findByLogin(username)).thenReturn(usuario);

            autenticacaoService.loadUserByUsername(username);

            verify(usuarioRepository, times(1)).findByLogin(username);
            verify(usuarioRepository, never()).findByLogin(argThat(arg -> !arg.equals(username)));
        }
    }

    @Nested
    @DisplayName("Testes de Validação de UserDetails")
    class ValidacaoUserDetailsTests {

        @Test
        @DisplayName("Deve retornar UserDetails com authorities vazias")
        void deveRetornarUserDetailsComAuthoritiesVazias() {
            String login = "usuario@teste.com";

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(1L);
            usuario.setLogin(login);
            usuario.setSenha("senha");

            when(usuarioRepository.findByLogin(login)).thenReturn(usuario);

            UserDetails resultado = autenticacaoService.loadUserByUsername(login);

            assertThat(resultado.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar UserDetails com conta não expirada")
        void deveRetornarUserDetailsComContaNaoExpirada() {
            String login = "usuario@teste.com";

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setLogin(login);
            usuario.setSenha("senha");

            when(usuarioRepository.findByLogin(login)).thenReturn(usuario);

            UserDetails resultado = autenticacaoService.loadUserByUsername(login);

            assertThat(resultado.isAccountNonExpired()).isTrue();
            assertThat(resultado.isAccountNonLocked()).isTrue();
            assertThat(resultado.isCredentialsNonExpired()).isTrue();
            assertThat(resultado.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Integração com UserDetails")
    class IntegracaoUserDetailsTests {

        @Test
        @DisplayName("Deve retornar UserDetails compatível com Spring Security")
        void deveRetornarUserDetailsCompativelComSpringSecurity() {
            // Validando integração completa com Spring Security
            // Retorno precisa implementar UserDetails com todos os métodos obrigatórios
            String login = "admin@teste.com";
            String senha = "senhaSegura123";

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(100L);
            usuario.setLogin(login);
            usuario.setSenha(senha);
            usuario.setNome("Administrador");

            when(usuarioRepository.findByLogin(login)).thenReturn(usuario);

            UserDetails resultado = autenticacaoService.loadUserByUsername(login);

            // Validando TODOS os métodos que o Spring Security exige
            assertThat(resultado).isInstanceOf(UserDetails.class);
            assertThat(resultado.getUsername()).isEqualTo(login);
            assertThat(resultado.getPassword()).isEqualTo(senha);
            assertThat(resultado.getAuthorities()).isNotNull();
            assertThat(resultado.isAccountNonExpired()).isTrue();
            assertThat(resultado.isAccountNonLocked()).isTrue();
            assertThat(resultado.isCredentialsNonExpired()).isTrue();
            assertThat(resultado.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar UsuarioEntity que implementa UserDetails")
        void deveRetornarUsuarioEntityQueImplementaUserDetails() {
            // UsuarioEntity implementa UserDetails diretamente
            // Permite ter dados extras (ID, nome) além dos campos padrão do Spring
            String login = "teste@exemplo.com";

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(50L);
            usuario.setLogin(login);
            usuario.setSenha("senha");

            when(usuarioRepository.findByLogin(login)).thenReturn(usuario);

            UserDetails resultado = autenticacaoService.loadUserByUsername(login);

            // Cast pra UsuarioEntity permite acessar campos extras
            assertThat(resultado).isInstanceOf(UsuarioEntity.class);
            UsuarioEntity usuarioEntity = (UsuarioEntity) resultado;
            assertThat(usuarioEntity.getId()).isEqualTo(50L);
            assertThat(usuarioEntity.getLogin()).isEqualTo(login);
        }
    }
}
