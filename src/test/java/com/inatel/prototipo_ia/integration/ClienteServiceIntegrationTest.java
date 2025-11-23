package com.inatel.prototipo_ia.integration;

import com.inatel.prototipo_ia.dto.in.ClienteDtoIn;
import com.inatel.prototipo_ia.dto.out.ClienteDtoOut;
import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.service.ClienteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de Integração - ClienteService
 *
 * Valida com banco H2 real (não é mock):
 * - Persistência completa no banco
 * - Geração automática de IDs
 * - Criptografia BCrypt de senhas
 * - Relacionamentos com ChatEntity (foreign keys)
 * - Queries customizadas do repository
 *
 * Diferença dos testes unitários:
 * - Unitários: Mock do banco → valida só lógica de negócio
 * - Integração: Banco H2 real → valida funcionamento completo
 */
@DisplayName("Testes de Integração - ClienteService")
class ClienteServiceIntegrationTest extends BaseIntegrationTest {

    // @DataJpaTest não sobe todos os beans - precisa criar services manualmente
    // Criando PasswordEncoder e ClienteService via @TestConfiguration
    @TestConfiguration
    static class ClienteServiceTestConfiguration {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public ClienteService clienteService(ClienteRepository clienteRepository,
                                              ChatRepository chatRepository,
                                              PasswordEncoder passwordEncoder) {
            return new ClienteService(clienteRepository, chatRepository, passwordEncoder);
        }
    }

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        // Limpeza do banco antes de cada teste (garante isolamento)
        // Ordem importante: deleta chats primeiro (foreign keys)
        chatRepository.deleteAll();
        clienteRepository.deleteAll();
        profissionalRepository.deleteAll();
    }

    @Nested
    @DisplayName("Testes de Criação com Banco de Dados")
    class CriacaoIntegracaoTests {

        @Test
        @DisplayName("Deve salvar cliente no banco e gerar ID automaticamente")
        void deveSalvarClienteNoBanco() {
            // Teste principal de persistência real
            ClienteDtoIn clienteDto = new ClienteDtoIn();
            clienteDto.setNome("João da Silva");
            clienteDto.setIdade(30);
            clienteDto.setEndereco("Rua A, 100");
            clienteDto.setNivel("Intermediário");
            clienteDto.setLogin("joao@teste.com");
            clienteDto.setSenha("senha123");

            // Salvando no banco H2
            ClienteDtoOut resultado = clienteService.salvar(clienteDto);

            // Valida retorno com ID gerado automaticamente
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("João da Silva");

            // Consultando banco direto pra confirmar persistência
            // Isso garante que não está só retornando um DTO sem salvar
            Optional<ClienteEntity> clienteNoBanco = clienteRepository.findById(resultado.getId());
            assertThat(clienteNoBanco).isPresent();
            assertThat(clienteNoBanco.get().getNome()).isEqualTo("João da Silva");

            // Validação crítica de segurança: senha criptografada com BCrypt
            assertThat(clienteNoBanco.get().getSenha()).isNotEqualTo("senha123");
            // BCrypt gera hash começando com $2a$
            assertThat(clienteNoBanco.get().getSenha()).startsWith("$2a$");
        }

        @Test
        @DisplayName("Deve salvar múltiplos clientes com IDs diferentes")
        void deveSalvarMultiplosClientes() {
            ClienteDtoIn cliente1 = criarClienteDto("Cliente 1", "cliente1@teste.com");
            ClienteDtoIn cliente2 = criarClienteDto("Cliente 2", "cliente2@teste.com");

            ClienteDtoOut resultado1 = clienteService.salvar(cliente1);
            ClienteDtoOut resultado2 = clienteService.salvar(cliente2);

            assertThat(resultado1.getId()).isNotEqualTo(resultado2.getId());
            assertThat(clienteRepository.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Testes de Busca com Banco de Dados")
    class BuscaIntegracaoTests {

        @Test
        @DisplayName("Deve buscar todos os clientes do banco")
        void deveBuscarTodosOsClientes() {
            clienteRepository.save(criarClienteEntity("Cliente 1", "cliente1@teste.com"));
            clienteRepository.save(criarClienteEntity("Cliente 2", "cliente2@teste.com"));
            clienteRepository.save(criarClienteEntity("Cliente 3", "cliente3@teste.com"));

            List<ClienteDtoOut> resultados = clienteService.buscarTodos();

            assertThat(resultados).hasSize(3);
            assertThat(resultados).extracting(ClienteDtoOut::getNome)
                    .containsExactlyInAnyOrder("Cliente 1", "Cliente 2", "Cliente 3");
        }

        @Test
        @DisplayName("Deve buscar cliente por ID do banco")
        void deveBuscarClientePorId() {
            ClienteEntity clienteSalvo = clienteRepository.save(
                    criarClienteEntity("Maria Santos", "maria@teste.com"));

            Optional<ClienteDtoOut> resultado = clienteService.buscarPorId(clienteSalvo.getId());

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(clienteSalvo.getId());
            assertThat(resultado.get().getNome()).isEqualTo("Maria Santos");
        }

        @Test
        @DisplayName("Deve retornar vazio quando cliente não existe no banco")
        void deveRetornarVazioParaIdInexistente() {
            Optional<ClienteDtoOut> resultado = clienteService.buscarPorId(999L);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Atualização com Banco de Dados")
    class AtualizacaoIntegracaoTests {

        @Test
        @DisplayName("Deve atualizar cliente existente no banco")
        void deveAtualizarClienteNoBanco() {
            ClienteEntity clienteExistente = clienteRepository.save(
                    criarClienteEntity("Nome Antigo", "antigo@teste.com"));

            ClienteDtoIn dadosAtualizados = new ClienteDtoIn();
            dadosAtualizados.setNome("Nome Novo");
            dadosAtualizados.setIdade(35);
            dadosAtualizados.setNivel("Avançado");

            ClienteDtoOut resultado = clienteService.atualizar(clienteExistente.getId(), dadosAtualizados);

            assertThat(resultado.getNome()).isEqualTo("Nome Novo");
            assertThat(resultado.getIdade()).isEqualTo(35);

            // Consultando banco pra validar atualização
            ClienteEntity clienteAtualizado = clienteRepository.findById(clienteExistente.getId()).get();
            assertThat(clienteAtualizado.getNome()).isEqualTo("Nome Novo");
            assertThat(clienteAtualizado.getIdade()).isEqualTo(35);
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar cliente inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            ClienteDtoIn dadosAtualizados = new ClienteDtoIn();
            dadosAtualizados.setNome("Teste");

            assertThatThrownBy(() -> clienteService.atualizar(999L, dadosAtualizados))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Testes de Deleção com Banco de Dados")
    class DelecaoIntegracaoTests {

        @Test
        @DisplayName("Deve deletar cliente do banco quando não tem chats")
        void deveDeletarClienteSemChats() {
            ClienteEntity cliente = clienteRepository.save(
                    criarClienteEntity("Cliente Deletável", "deletavel@teste.com"));
            Long clienteId = cliente.getId();

            clienteService.deletar(clienteId);

            assertThat(clienteRepository.findById(clienteId)).isEmpty();
            assertThat(clienteRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar cliente com chats associados")
        void deveLancarExcecaoAoDeletarClienteComChats() {
            ClienteEntity cliente = clienteRepository.save(
                    criarClienteEntity("Cliente Com Chat", "comchat@teste.com"));

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setNome("Profissional Teste");
            profissional.setLogin("prof@teste.com");
            profissional.setSenha("senha");
            profissional = profissionalRepository.save(profissional);

            ChatEntity chat = new ChatEntity();
            chat.setCliente(cliente);
            chat.setProfissional(profissional);
            chat.setDuracao(30);
            chatRepository.save(chat);

            assertThatThrownBy(() -> clienteService.deletar(cliente.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("chat");

            // Validando que cliente continua no banco (deleção bloqueada)
            assertThat(clienteRepository.findById(cliente.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("Testes de Queries Customizadas")
    class QueriesCustomizadasIntegracaoTests {

        @Test
        @DisplayName("Deve buscar apenas clientes maiores de idade")
        void deveBuscarClientesMaioresDeIdade() {
            clienteRepository.save(criarClienteEntityComIdade("Menor 1", 15, "menor1@teste.com"));
            clienteRepository.save(criarClienteEntityComIdade("Adulto 1", 18, "adulto1@teste.com"));
            clienteRepository.save(criarClienteEntityComIdade("Adulto 2", 25, "adulto2@teste.com"));
            clienteRepository.save(criarClienteEntityComIdade("Menor 2", 10, "menor2@teste.com"));

            List<ClienteDtoOut> resultados = clienteService.buscarMaioresDeIdade();

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getIdade() >= 18);
        }

        @Test
        @DisplayName("Deve buscar clientes por nível específico")
        void deveBuscarClientesPorNivel() {
            clienteRepository.save(criarClienteEntityComNivel("Cliente 1", "Básico", "c1@teste.com"));
            clienteRepository.save(criarClienteEntityComNivel("Cliente 2", "Avançado", "c2@teste.com"));
            clienteRepository.save(criarClienteEntityComNivel("Cliente 3", "Avançado", "c3@teste.com"));
            clienteRepository.save(criarClienteEntityComNivel("Cliente 4", "Intermediário", "c4@teste.com"));

            List<ClienteDtoOut> resultados = clienteService.buscarPorNivel("Avançado");

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getNivel().equals("Avançado"));
        }
    }

    // ===== Métodos auxiliares para criar dados de teste =====
    private ClienteDtoIn criarClienteDto(String nome, String login) {
        ClienteDtoIn dto = new ClienteDtoIn();
        dto.setNome(nome);
        dto.setIdade(25);
        dto.setEndereco("Rua Teste, 100");
        dto.setNivel("Básico");
        dto.setLogin(login);
        dto.setSenha("senha123");
        return dto;
    }

    private ClienteEntity criarClienteEntity(String nome, String login) {
        ClienteEntity entity = new ClienteEntity();
        entity.setNome(nome);
        entity.setIdade(25);
        entity.setEndereco("Rua Teste");
        entity.setNivel("Básico");
        entity.setLogin(login);
        entity.setSenha("senha123");
        return entity;
    }

    private ClienteEntity criarClienteEntityComIdade(String nome, Integer idade, String login) {
        ClienteEntity entity = criarClienteEntity(nome, login);
        entity.setIdade(idade);
        return entity;
    }

    private ClienteEntity criarClienteEntityComNivel(String nome, String nivel, String login) {
        ClienteEntity entity = criarClienteEntity(nome, login);
        entity.setNivel(nivel);
        return entity;
    }
}
