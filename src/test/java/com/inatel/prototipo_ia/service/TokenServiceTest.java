package com.inatel.prototipo_ia.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.inatel.prototipo_ia.entity.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes do TokenService - geração e validação de JWT
 *
 * Teste crítico de segurança - qualquer falha aqui compromete autenticação
 * Precisa validar: geração correta, rejeição de tokens inválidos, e ciclo completo
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private TokenService tokenService;
    private final String SECRET = "test-secret-key-12345";
    private final Long EXPIRATION = 3600000L; // 1 hora

    @BeforeEach
    void setUp() {
        // Usando ReflectionTestUtils pra injetar valores do @Value
        // Não dá pra usar @InjectMocks porque são fields com @Value do Spring
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        ReflectionTestUtils.setField(tokenService, "expiration", EXPIRATION);
    }

    @Nested
    @DisplayName("Testes de Geração de Token")
    class GeracaoTokenTests {

        @Test
        @DisplayName("Deve gerar token JWT válido com sucesso")
        void deveGerarTokenComSucesso() {
            // Setup do usuário teste
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(1L);
            usuario.setLogin("usuario@teste.com");
            usuario.setNome("Usuário Teste");

            String token = tokenService.gerarToken(usuario);

            // Validações básicas
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT = header.payload.signature

            // Decodificando e validando conteúdo
            Algorithm algoritmo = Algorithm.HMAC256(SECRET);
            DecodedJWT decodedJWT = JWT.require(algoritmo)
                    .withIssuer("API Prototipo IA")
                    .build()
                    .verify(token);

            // Verificando dados do payload
            assertThat(decodedJWT.getSubject()).isEqualTo("usuario@teste.com");
            assertThat(decodedJWT.getClaim("id").asLong()).isEqualTo(1L);
            assertThat(decodedJWT.getIssuer()).isEqualTo("API Prototipo IA");
            assertThat(decodedJWT.getExpiresAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve gerar tokens diferentes para usuários diferentes")
        void deveGerarTokensDiferentes() {
            UsuarioEntity usuario1 = new UsuarioEntity();
            usuario1.setId(1L);
            usuario1.setLogin("usuario1@teste.com");

            UsuarioEntity usuario2 = new UsuarioEntity();
            usuario2.setId(2L);
            usuario2.setLogin("usuario2@teste.com");

            String token1 = tokenService.gerarToken(usuario1);
            String token2 = tokenService.gerarToken(usuario2);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Deve incluir login do usuário no subject do token")
        void deveIncluirLoginNoSubject() {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(5L);
            usuario.setLogin("maria@teste.com");

            String token = tokenService.gerarToken(usuario);

            Algorithm algoritmo = Algorithm.HMAC256(SECRET);
            DecodedJWT decodedJWT = JWT.require(algoritmo)
                    .withIssuer("API Prototipo IA")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getSubject()).isEqualTo("maria@teste.com");
        }

        @Test
        @DisplayName("Deve incluir ID do usuário no claim do token")
        void deveIncluirIdNoClaim() {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(42L);
            usuario.setLogin("joao@teste.com");

            String token = tokenService.gerarToken(usuario);

            Algorithm algoritmo = Algorithm.HMAC256(SECRET);
            DecodedJWT decodedJWT = JWT.require(algoritmo)
                    .withIssuer("API Prototipo IA")
                    .build()
                    .verify(token);

            assertThat(decodedJWT.getClaim("id").asLong()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Token")
    class ValidacaoTokenTests {

        @Test
        @DisplayName("Deve extrair subject de token válido com sucesso")
        void deveExtrairSubjectDeTokenValido() {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(1L);
            usuario.setLogin("teste@exemplo.com");

            String token = tokenService.gerarToken(usuario);
            String subject = tokenService.getSubject(token);

            assertThat(subject).isEqualTo("teste@exemplo.com");
        }

        @Test
        @DisplayName("Deve lançar exceção para token inválido")
        void deveLancarExcecaoParaTokenInvalido() {
            String tokenInvalido = "token.invalido.aqui";

            assertThatThrownBy(() -> tokenService.getSubject(tokenInvalido))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar exceção para token vazio")
        void deveLancarExcecaoParaTokenVazio() {
            assertThatThrownBy(() -> tokenService.getSubject(""))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar exceção para token nulo")
        void deveLancarExcecaoParaTokenNulo() {
            assertThatThrownBy(() -> tokenService.getSubject(null))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para token com assinatura incorreta")
        void deveLancarExcecaoParaTokenComAssinaturaIncorreta() {
            // Simulando ataque: token com secret diferente (token forjado)
            String secretDiferente = "outro-secret-key";
            Algorithm algoritmoDiferente = Algorithm.HMAC256(secretDiferente);
            String tokenComAssinaturaIncorreta = JWT.create()
                    .withIssuer("API Prototipo IA")
                    .withSubject("teste@exemplo.com")
                    .sign(algoritmoDiferente);

            // Sistema deve rejeitar
            assertThatThrownBy(() -> tokenService.getSubject(tokenComAssinaturaIncorreta))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar exceção para token com issuer incorreto")
        void deveLancarExcecaoParaTokenComIssuerIncorreto() {
            Algorithm algoritmo = Algorithm.HMAC256(SECRET);
            String tokenComIssuerIncorreto = JWT.create()
                    .withIssuer("Issuer Diferente")
                    .withSubject("teste@exemplo.com")
                    .sign(algoritmo);

            assertThatThrownBy(() -> tokenService.getSubject(tokenComIssuerIncorreto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido ou expirado");
        }
    }

    @Nested
    @DisplayName("Testes de Ciclo Completo")
    class CicloCompletoTests {

        @Test
        @DisplayName("Deve gerar e validar token com sucesso")
        void deveGerarEValidarTokenComSucesso() {
            // Fluxo completo: gerar → validar → extrair dados
            // Como funciona na prática: geração no login, validação em cada request
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(10L);
            usuario.setLogin("ciclo@teste.com");
            usuario.setNome("Teste Ciclo Completo");

            String token = tokenService.gerarToken(usuario);
            assertThat(token).isNotNull();

            // Recuperando login do token
            String subject = tokenService.getSubject(token);
            assertThat(subject).isEqualTo("ciclo@teste.com");
        }

        @Test
        @DisplayName("Deve manter consistência entre múltiplas validações do mesmo token")
        void deveManterConsistenciaEntreValidacoes() {
            // Token pode ser validado N vezes durante vida útil
            // Garantindo que sempre retorna o mesmo resultado
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(7L);
            usuario.setLogin("consistencia@teste.com");

            String token = tokenService.gerarToken(usuario);

            // Validando 3x o mesmo token (como em requests diferentes)
            String subject1 = tokenService.getSubject(token);
            String subject2 = tokenService.getSubject(token);
            String subject3 = tokenService.getSubject(token);

            // Todos devem ser idênticos
            assertThat(subject1)
                    .isEqualTo(subject2)
                    .isEqualTo(subject3)
                    .isEqualTo("consistencia@teste.com");
        }
    }
}
