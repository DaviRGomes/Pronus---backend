package com.inatel.prototipo_ia.integration;

import com.inatel.prototipo_ia.dto.in.ChatDtoIn;
import com.inatel.prototipo_ia.dto.out.ChatDtoOut;
import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.EspecialistaRepository;
import com.inatel.prototipo_ia.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de Integração - ChatService
 *
 * Tipo: Integração (Service + Repository + Banco H2)
 * Valida: CRUD completo, relacionamentos Cliente-Profissional, queries customizadas de duração
 * Foco: Persistência real, foreign keys, e queries específicas (findChatsLongos, etc)
 */
@DisplayName("Testes de Integração - ChatService")
class ChatServiceIntegrationTest extends BaseIntegrationTest {

    @TestConfiguration
    static class ChatServiceTestConfiguration {
        @Bean
        public ChatService chatService(ChatRepository chatRepository,
                                        ClienteRepository clienteRepository,
                                        EspecialistaRepository especialistaRepository) {
            return new ChatService(chatRepository, clienteRepository, especialistaRepository);
        }
    }

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private ChatService chatService;

    private ClienteEntity clientePadrao;
    private EspecialistaEntity especialistaPadrao;

    @BeforeEach
    void setUp() {
        chatRepository.deleteAll();
        especialistaRepository.deleteAll();
        clienteRepository.deleteAll();

        // Setup de cliente e profissional padrões pra usar nos testes
        clientePadrao = criarEPersistirCliente("Cliente Chat", "clientechat@teste.com");
        especialistaPadrao = criarEPersistirEspecialista("Especialista Chat", "espchat@teste.com");
    }

    @Nested
    @DisplayName("Testes de Criação com Relacionamentos")
    class CriacaoIntegracaoTests {

        @Test
        @DisplayName("Deve criar chat com relacionamentos Cliente e Especialista")
        void deveCriarChatComRelacionamentos() {
            ChatDtoIn chatDto = criarChatDto(
                    clientePadrao.getId(),
                    especialistaPadrao.getId(),
                    45,
                    "Conversa sobre pronúncia"
            );

            ChatDtoOut resultado = chatService.criar(chatDto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isNotNull();
            assertThat(resultado.getClienteId()).isEqualTo(clientePadrao.getId());
            assertThat(resultado.getProfissionalId()).isEqualTo(especialistaPadrao.getId());
            assertThat(resultado.getDuracao()).isEqualTo(45);

            // Validando se realmente persistiu no banco H2
            ChatEntity chatNoBanco = chatRepository.findById(resultado.getId()).get();
            assertThat(chatNoBanco.getCliente().getId()).isEqualTo(clientePadrao.getId());
            assertThat(chatNoBanco.getEspecialista().getId()).isEqualTo(especialistaPadrao.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar chat com cliente inexistente")
        void deveLancarExcecaoComClienteInexistente() {
            ChatDtoIn chatDto = criarChatDto(999L, especialistaPadrao.getId(), 30, "Conversa");

            assertThatThrownBy(() -> chatService.criar(chatDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar chat com especialista inexistente")
        void deveLancarExcecaoComEspecialistaInexistente() {
            ChatDtoIn chatDto = criarChatDto(clientePadrao.getId(), 999L, 30, "Conversa");

            assertThatThrownBy(() -> chatService.criar(chatDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Especialista");
        }

        @Test
        @DisplayName("Deve validar duração negativa")
        void deveValidarDuracaoNegativa() {
            ChatDtoIn chatDto = criarChatDto(
                    clientePadrao.getId(),
                    especialistaPadrao.getId(),
                    -10,
                    "Conversa"
            );

            assertThatThrownBy(() -> chatService.criar(chatDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duração");
        }
    }

    @Nested
    @DisplayName("Testes de Busca com Filtros")
    class BuscaIntegracaoTests {

        @Test
        @DisplayName("Deve buscar todos os chats do banco")
        void deveBuscarTodosOsChats() {
            criarEPersistirChat(30, "Conversa 1");
            criarEPersistirChat(45, "Conversa 2");
            criarEPersistirChat(60, "Conversa 3");

            List<ChatDtoOut> resultados = chatService.buscarTodos();

            assertThat(resultados).hasSize(3);
        }

        @Test
        @DisplayName("Deve buscar chats por cliente ID")
        void deveBuscarChatsPorClienteId() {
            ClienteEntity outroCliente = criarEPersistirCliente("Outro Cliente", "outro@teste.com");

            criarEPersistirChat(clientePadrao, especialistaPadrao, 30);
            criarEPersistirChat(clientePadrao, especialistaPadrao, 45);
            criarEPersistirChat(outroCliente, especialistaPadrao, 60);

            List<ChatDtoOut> resultados = chatService.buscarPorClienteId(clientePadrao.getId());

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getClienteId().equals(clientePadrao.getId()));
        }

        @Test
        @DisplayName("Deve buscar chats por especialista ID")
        void deveBuscarChatsPorEspecialistaId() {
            EspecialistaEntity outroEspecialista = criarEPersistirEspecialista(
                    "Outro Especialista", "outro@teste.com");

            criarEPersistirChat(clientePadrao, especialistaPadrao, 30);
            criarEPersistirChat(clientePadrao, outroEspecialista, 45);
            criarEPersistirChat(clientePadrao, especialistaPadrao, 60);

            List<ChatDtoOut> resultados = chatService.buscarPorEspecialistaId(especialistaPadrao.getId());

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getProfissionalId().equals(especialistaPadrao.getId()));
        }
    }

    @Nested
    @DisplayName("Testes de Queries Customizadas")
    class QueriesCustomizadasIntegracaoTests {

        @Test
        @DisplayName("Deve buscar chats com duração maior que valor especificado")
        void deveBuscarChatsComDuracaoMaiorQue() {
            criarEPersistirChat(20, "Chat curto 1");
            criarEPersistirChat(35, "Chat médio");
            criarEPersistirChat(60, "Chat longo 1");
            criarEPersistirChat(90, "Chat longo 2");

            // funcionalidade descontinuada: usamos buscarChatsLongos()
            List<ChatDtoOut> resultados = chatService.buscarChatsLongos();

            assertThat(resultados).hasSize(3);
            assertThat(resultados).allMatch(c -> c.getDuracao() > 30);
            assertThat(resultados).extracting(ChatDtoOut::getDuracao)
                    .containsExactlyInAnyOrder(35, 60, 90);
        }

        @Test
        @DisplayName("Deve buscar chats longos (duração > 30 minutos)")
        void deveBuscarChatsLongos() {
            criarEPersistirChat(20, "Chat curto 1");
            criarEPersistirChat(30, "Chat curto 2");
            criarEPersistirChat(70, "Chat longo 1");
            criarEPersistirChat(120, "Chat longo 2");

            List<ChatDtoOut> resultados = chatService.buscarChatsLongos();

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getDuracao() > 30);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há chats com duração maior")
        void deveRetornarVazioQuandoNaoHaChatsComDuracaoMaior() {
            criarEPersistirChat(10, "Chat 1");
            criarEPersistirChat(20, "Chat 2");

            List<ChatDtoOut> resultados = chatService.buscarChatsLongos();

            assertThat(resultados).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Atualização")
    class AtualizacaoIntegracaoTests {

        @Test
        @DisplayName("Deve atualizar dados do chat mantendo relacionamentos")
        void deveAtualizarChatMantentoRelacionamentos() {
            ChatEntity chatExistente = criarEPersistirChat(30, "Conversa inicial");

            ChatDtoIn dadosAtualizados = new ChatDtoIn();
            dadosAtualizados.setClienteId(clientePadrao.getId());
            dadosAtualizados.setEspecialistaId(especialistaPadrao.getId());
            dadosAtualizados.setDuracao(60);
            dadosAtualizados.setConversa("Conversa atualizada e estendida");

            ChatDtoOut resultado = chatService.atualizar(chatExistente.getId(), dadosAtualizados);

            assertThat(resultado.getDuracao()).isEqualTo(60);
            assertThat(resultado.getConversa()).isEqualTo("Conversa atualizada e estendida");
            assertThat(resultado.getClienteId()).isEqualTo(clientePadrao.getId());

            // Consultando banco pra confirmar persistência
            ChatEntity atualizado = chatRepository.findById(chatExistente.getId()).get();
            assertThat(atualizado.getDuracao()).isEqualTo(60);
            assertThat(atualizado.getConversa()).isEqualTo("Conversa atualizada e estendida");
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar chat inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            ChatDtoIn dadosAtualizados = new ChatDtoIn();
            dadosAtualizados.setClienteId(clientePadrao.getId());
            dadosAtualizados.setEspecialistaId(especialistaPadrao.getId());
            dadosAtualizados.setDuracao(30);

            assertThatThrownBy(() -> chatService.atualizar(999L, dadosAtualizados))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Testes de Deleção")
    class DelecaoIntegracaoTests {

        @Test
        @DisplayName("Deve deletar chat do banco")
        void deveDeletarChat() {
            ChatEntity chat = criarEPersistirChat(30, "Chat para deletar");
            Long chatId = chat.getId();

            chatService.deletar(chatId);

            assertThat(chatRepository.findById(chatId)).isEmpty();
            assertThat(chatRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar chat inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            assertThatThrownBy(() -> chatService.deletar(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Testes de Integridade de Relacionamentos")
    class IntegridadeRelacionamentosTests {

        @Test
        @DisplayName("Deve manter múltiplos chats para o mesmo cliente")
        void deveManterMultiplosChatsMesmoCliente() {
            criarEPersistirChat(clientePadrao, especialistaPadrao, 30);
            criarEPersistirChat(clientePadrao, especialistaPadrao, 45);
            criarEPersistirChat(clientePadrao, especialistaPadrao, 60);

            List<ChatDtoOut> chatsDoCliente = chatService.buscarPorClienteId(clientePadrao.getId());

            assertThat(chatsDoCliente).hasSize(3);
            assertThat(chatsDoCliente).extracting(ChatDtoOut::getDuracao)
                    .containsExactlyInAnyOrder(30, 45, 60);
        }

        @Test
        @DisplayName("Deve manter múltiplos chats para o mesmo especialista")
        void deveManterMultiplosChatsMesmoEspecialista() {
            ClienteEntity cliente1 = criarEPersistirCliente("Cliente 1", "c1@teste.com");
            ClienteEntity cliente2 = criarEPersistirCliente("Cliente 2", "c2@teste.com");

            criarEPersistirChat(cliente1, especialistaPadrao, 30);
            criarEPersistirChat(cliente2, especialistaPadrao, 45);

            List<ChatDtoOut> chatsDoEspecialista = chatService.buscarPorEspecialistaId(especialistaPadrao.getId());

            assertThat(chatsDoEspecialista).hasSize(2);
        }
    }

    // ===== Métodos auxiliares para setup de dados de teste =====
    private ClienteEntity criarEPersistirCliente(String nome, String login) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome(nome);
        cliente.setLogin(login);
        cliente.setSenha("senha123");
        cliente.setIdade(25);
        return clienteRepository.save(cliente);
    }

    private EspecialistaEntity criarEPersistirEspecialista(String nome, String login) {
        EspecialistaEntity esp = new EspecialistaEntity();
        esp.setNome(nome);
        esp.setLogin(login);
        esp.setSenha("senha123");
        esp.setCrmFono("CRFa 12345");
        esp.setEspecialidade("Fono");
        return especialistaRepository.save(esp);
    }

    private ChatEntity criarEPersistirChat(Integer duracao, String conversa) {
        return criarEPersistirChat(clientePadrao, especialistaPadrao, duracao, conversa);
    }

    private ChatEntity criarEPersistirChat(ClienteEntity cliente, EspecialistaEntity especialista, Integer duracao) {
        return criarEPersistirChat(cliente, especialista, duracao, "Conversa padrão");
    }

    private ChatEntity criarEPersistirChat(ClienteEntity cliente, EspecialistaEntity especialista,
                                           Integer duracao, String conversa) {
        ChatEntity chat = new ChatEntity();
        chat.setCliente(cliente);
        chat.setEspecialista(especialista);
        chat.setDuracao(duracao);
        chat.setConversa(conversa);
        return chatRepository.save(chat);
    }

    private ChatDtoIn criarChatDto(Long clienteId, Long especialistaId, Integer duracao, String conversa) {
        ChatDtoIn dto = new ChatDtoIn();
        dto.setClienteId(clienteId);
        dto.setEspecialistaId(especialistaId);
        dto.setDuracao(duracao);
        dto.setConversa(conversa);
        return dto;
    }
}
