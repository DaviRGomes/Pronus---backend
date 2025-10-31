package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ChatDtoIn;
import com.inatel.prototipo_ia.dto.out.ChatDtoOut;
import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private ChatService chatService;

    @Nested
    @DisplayName("Testes de Criação de Chat")
    class CriacaoChatTests {

        @Test
        @DisplayName("Deve criar chat com sucesso quando dados válidos")
        void deveCriarChatComSucesso() {
            ChatDtoIn chatDto = new ChatDtoIn();
            chatDto.setClienteId(1L);
            chatDto.setProfissionalId(2L);
            chatDto.setDuracao(30);
            chatDto.setConversa("Conversa sobre pronúncia");

            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);
            cliente.setNome("João");

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);
            profissional.setNome("Dr. Silva");

            ChatEntity chatSalvo = new ChatEntity();
            chatSalvo.setId(10L);
            chatSalvo.setCliente(cliente);
            chatSalvo.setProfissional(profissional);
            chatSalvo.setDuracao(30);
            chatSalvo.setConversa("Conversa sobre pronúncia");

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(2L)).thenReturn(Optional.of(profissional));
            when(chatRepository.save(any(ChatEntity.class))).thenReturn(chatSalvo);

            ChatDtoOut resultado = chatService.criar(chatDto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getClienteId()).isEqualTo(1L);
            assertThat(resultado.getProfissionalId()).isEqualTo(2L);
            assertThat(resultado.getDuracao()).isEqualTo(30);
            assertThat(resultado.getConversa()).isEqualTo("Conversa sobre pronúncia");

            verify(chatRepository, times(1)).save(any(ChatEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            assertThatThrownBy(() -> chatService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(chatRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void deveLancarExcecao_QuandoClienteNaoExiste() {
            ChatDtoIn chatDto = new ChatDtoIn();
            chatDto.setClienteId(999L);
            chatDto.setProfissionalId(2L);

            when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.criar(chatDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cliente")
                .hasMessageContaining("999");

            verify(chatRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando profissional não existe")
        void deveLancarExcecao_QuandoProfissionalNaoExiste() {
            ChatDtoIn chatDto = new ChatDtoIn();
            chatDto.setClienteId(1L);
            chatDto.setProfissionalId(999L);

            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(profissionalRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.criar(chatDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Profissional")
                .hasMessageContaining("999");

            verify(chatRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando clienteId é nulo")
        void deveLancarExcecao_QuandoClienteIdNulo() {
            ChatDtoIn chatDto = new ChatDtoIn();
            chatDto.setClienteId(null);
            chatDto.setProfissionalId(2L);

            assertThatThrownBy(() -> chatService.criar(chatDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cliente");

            verify(chatRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando profissionalId é nulo")
        void deveLancarExcecao_QuandoProfissionalIdNulo() {
            ChatDtoIn chatDto = new ChatDtoIn();
            chatDto.setClienteId(1L);
            chatDto.setProfissionalId(null);

            assertThatThrownBy(() -> chatService.criar(chatDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profissional");

            verify(chatRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando duração é negativa")
        void deveLancarExcecao_QuandoDuracaoNegativa() {
            ChatDtoIn chatDto = new ChatDtoIn();
            chatDto.setClienteId(1L);
            chatDto.setProfissionalId(2L);
            chatDto.setDuracao(-10);

            assertThatThrownBy(() -> chatService.criar(chatDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duração");

            verify(chatRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Chats")
    class BuscaChatTests {

        @Test
        @DisplayName("Deve buscar todos os chats com sucesso")
        void deveBuscarTodosOsChats() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);

            ChatEntity chat1 = new ChatEntity();
            chat1.setId(1L);
            chat1.setCliente(cliente);
            chat1.setProfissional(profissional);
            chat1.setDuracao(20);

            ChatEntity chat2 = new ChatEntity();
            chat2.setId(2L);
            chat2.setCliente(cliente);
            chat2.setProfissional(profissional);
            chat2.setDuracao(30);

            when(chatRepository.findAll()).thenReturn(Arrays.asList(chat1, chat2));

            List<ChatDtoOut> resultados = chatService.buscarTodos();

            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getId()).isEqualTo(1L);
            assertThat(resultados.get(1).getId()).isEqualTo(2L);
            verify(chatRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar chat por ID com sucesso")
        void deveBuscarChatPorId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);

            ChatEntity chat = new ChatEntity();
            chat.setId(10L);
            chat.setCliente(cliente);
            chat.setProfissional(profissional);
            chat.setDuracao(25);

            when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));

            Optional<ChatDtoOut> resultado = chatService.buscarPorId(10L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getDuracao()).isEqualTo(25);
            verify(chatRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando chat não existe")
        void deveRetornarVazio_QuandoChatNaoExiste() {
            when(chatRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<ChatDtoOut> resultado = chatService.buscarPorId(999L);

            assertThat(resultado).isEmpty();
            verify(chatRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar chats por cliente ID")
        void deveBuscarChatsPorClienteId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);

            ChatEntity chat1 = new ChatEntity();
            chat1.setId(1L);
            chat1.setCliente(cliente);
            chat1.setProfissional(profissional);

            ChatEntity chat2 = new ChatEntity();
            chat2.setId(2L);
            chat2.setCliente(cliente);
            chat2.setProfissional(profissional);

            when(chatRepository.findByClienteId(1L)).thenReturn(Arrays.asList(chat1, chat2));

            List<ChatDtoOut> resultados = chatService.buscarPorClienteId(1L);

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getClienteId().equals(1L));
            verify(chatRepository, times(1)).findByClienteId(1L);
        }

        @Test
        @DisplayName("Deve buscar chats por profissional ID")
        void deveBuscarChatsPorProfissionalId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);

            ChatEntity chat1 = new ChatEntity();
            chat1.setId(1L);
            chat1.setCliente(cliente);
            chat1.setProfissional(profissional);

            when(chatRepository.findByProfissionalId(2L)).thenReturn(Arrays.asList(chat1));

            List<ChatDtoOut> resultados = chatService.buscarPorProfissionalId(2L);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getProfissionalId()).isEqualTo(2L);
            verify(chatRepository, times(1)).findByProfissionalId(2L);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Chat")
    class AtualizacaoChatTests {

        @Test
        @DisplayName("Deve atualizar chat com sucesso")
        void deveAtualizarChatComSucesso() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);

            ChatEntity chatExistente = new ChatEntity();
            chatExistente.setId(10L);
            chatExistente.setCliente(cliente);
            chatExistente.setProfissional(profissional);
            chatExistente.setDuracao(20);
            chatExistente.setConversa("Conversa antiga");

            ChatDtoIn dadosAtualizados = new ChatDtoIn();
            dadosAtualizados.setClienteId(1L);
            dadosAtualizados.setProfissionalId(2L);
            dadosAtualizados.setDuracao(40);
            dadosAtualizados.setConversa("Conversa atualizada");

            ChatEntity chatAtualizado = new ChatEntity();
            chatAtualizado.setId(10L);
            chatAtualizado.setCliente(cliente);
            chatAtualizado.setProfissional(profissional);
            chatAtualizado.setDuracao(40);
            chatAtualizado.setConversa("Conversa atualizada");

            when(chatRepository.findById(10L)).thenReturn(Optional.of(chatExistente));
            when(chatRepository.save(any(ChatEntity.class))).thenReturn(chatAtualizado);

            ChatDtoOut resultado = chatService.atualizar(10L, dadosAtualizados);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getDuracao()).isEqualTo(40);
            assertThat(resultado.getConversa()).isEqualTo("Conversa atualizada");
            verify(chatRepository, times(1)).save(any(ChatEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar chat inexistente")
        void deveLancarExcecao_QuandoAtualizarChatInexistente() {
            ChatDtoIn dadosAtualizados = new ChatDtoIn();
            dadosAtualizados.setClienteId(1L);
            dadosAtualizados.setProfissionalId(2L);

            when(chatRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(chatRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Chat")
    class DelecaoChatTests {

        @Test
        @DisplayName("Deve deletar chat com sucesso")
        void deveDeletarChatComSucesso() {
            when(chatRepository.existsById(10L)).thenReturn(true);

            chatService.deletar(10L);

            verify(chatRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar chat inexistente")
        void deveLancarExcecao_QuandoDeletarChatInexistente() {
            when(chatRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> chatService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(chatRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Testes de Queries Customizadas")
    class QueriesCustomizadasTests {

        @Test
        @DisplayName("Deve buscar chats com duração maior que valor especificado")
        void deveBuscarChatsComDuracaoMaiorQue() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);

            ChatEntity chat1 = new ChatEntity();
            chat1.setId(1L);
            chat1.setCliente(cliente);
            chat1.setProfissional(profissional);
            chat1.setDuracao(60);

            ChatEntity chat2 = new ChatEntity();
            chat2.setId(2L);
            chat2.setCliente(cliente);
            chat2.setProfissional(profissional);
            chat2.setDuracao(90);

            when(chatRepository.findByDuracaoGreaterThan(30)).thenReturn(Arrays.asList(chat1, chat2));

            List<ChatDtoOut> resultados = chatService.buscarComDuracaoMaiorQue(30);

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getDuracao() > 30);
            verify(chatRepository, times(1)).findByDuracaoGreaterThan(30);
        }

        @Test
        @DisplayName("Deve lançar exceção quando minutos é nulo")
        void deveLancarExcecao_QuandoMinutosNulo() {
            assertThatThrownBy(() -> chatService.buscarComDuracaoMaiorQue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duração");

            verify(chatRepository, never()).findByDuracaoGreaterThan(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando minutos é negativo")
        void deveLancarExcecao_QuandoMinutosNegativo() {
            assertThatThrownBy(() -> chatService.buscarComDuracaoMaiorQue(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duração");

            verify(chatRepository, never()).findByDuracaoGreaterThan(any());
        }

        @Test
        @DisplayName("Deve buscar chats longos")
        void deveBuscarChatsLongos() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(2L);

            ChatEntity chat1 = new ChatEntity();
            chat1.setId(1L);
            chat1.setCliente(cliente);
            chat1.setProfissional(profissional);
            chat1.setDuracao(120);

            when(chatRepository.findChatsLongos()).thenReturn(Arrays.asList(chat1));

            List<ChatDtoOut> resultados = chatService.buscarChatsLongos();

            assertThat(resultados).hasSize(1);
            verify(chatRepository, times(1)).findChatsLongos();
        }
    }
}
