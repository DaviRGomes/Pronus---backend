package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.RelatorioDtoIn;
import com.inatel.prototipo_ia.dto.out.RelatorioDtoOut;
import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.RelatorioEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.RelatorioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Ativa o uso do Mockito nos testes
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Nested
    @DisplayName("Testes de Criação de Relatório")
    class CriacaoRelatorioTests {

        @Test
        @DisplayName("Deve criar relatório com sucesso quando dados válidos")
        void deveCriarRelatorioComSucesso() {
            // Arrange - Dados válidos
            RelatorioDtoIn relatorioDto = new RelatorioDtoIn();
            relatorioDto.setChatId(1L);
            relatorioDto.setAnaliseFono("Paciente apresenta dificuldade em falar R");
            relatorioDto.setAcuracia(0.85f);

            ChatEntity chatExistente = new ChatEntity();
            chatExistente.setId(1L);

            RelatorioEntity relatorioSalvo = new RelatorioEntity();
            relatorioSalvo.setId(100L);
            relatorioSalvo.setChat(chatExistente);
            relatorioSalvo.setAnaliseFono("Paciente apresenta dificuldade em falar R");
            relatorioSalvo.setAcuracia(0.85f);

            when(chatRepository.findById(1L)).thenReturn(Optional.of(chatExistente));
            when(relatorioRepository.existsByChatId(1L)).thenReturn(false);
            when(relatorioRepository.save(any(RelatorioEntity.class))).thenReturn(relatorioSalvo);

            // Act
            RelatorioDtoOut resultado = relatorioService.criar(relatorioDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(100L);
            assertThat(resultado.getAcuracia()).isEqualTo(0.85f);
            assertThat(resultado.getAnaliseFono()).isEqualTo("Paciente apresenta dificuldade em falar R");
            assertThat(resultado.getChatId()).isEqualTo(1L);

            verify(relatorioRepository, times(1)).save(any(RelatorioEntity.class));
        }

        @ParameterizedTest
        @ValueSource(floats = {-0.1f, 1.1f, 2.0f, -1.0f, 1.5f})
        @DisplayName("Deve lançar exceção ao criar relatório com acurácia inválida")
        void deveLancarExcecao_QuandoAcuraciaForInvalida(float acuraciaInvalida) {
            // Arrange
            RelatorioDtoIn relatorioDto = new RelatorioDtoIn();
            relatorioDto.setChatId(1L);
            relatorioDto.setAnaliseFono("Análise de teste");
            relatorioDto.setAcuracia(acuraciaInvalida);

            // Act & Assert
            assertThatThrownBy(() -> relatorioService.criar(relatorioDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("acurácia");

            verify(relatorioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando chat não existe")
        void deveLancarExcecao_QuandoChatNaoExiste() {
            // Arrange
            RelatorioDtoIn relatorioDto = new RelatorioDtoIn();
            relatorioDto.setChatId(999L);
            relatorioDto.setAnaliseFono("Análise válida");
            relatorioDto.setAcuracia(0.9f);

            when(chatRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> relatorioService.criar(relatorioDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("chat")
                .hasMessageContaining("999");

            verify(relatorioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando chat já possui relatório")
        void deveLancarExcecao_QuandoChatJaPossuiRelatorio() {
            // Arrange
            RelatorioDtoIn relatorioDto = new RelatorioDtoIn();
            relatorioDto.setChatId(1L);
            relatorioDto.setAnaliseFono("Análise válida");
            relatorioDto.setAcuracia(0.9f);

            ChatEntity chatExistente = new ChatEntity();
            chatExistente.setId(1L);

            when(chatRepository.findById(1L)).thenReturn(Optional.of(chatExistente));
            when(relatorioRepository.existsByChatId(1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> relatorioService.criar(relatorioDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já possui um relatório");

            verify(relatorioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando análise fonoaudiológica está em branco")
        void deveLancarExcecao_QuandoAnaliseFonoEstaBranco() {
            // Arrange
            RelatorioDtoIn relatorioDto = new RelatorioDtoIn();
            relatorioDto.setChatId(1L);
            relatorioDto.setAnaliseFono("   ");
            relatorioDto.setAcuracia(0.9f);

            // Act & Assert
            assertThatThrownBy(() -> relatorioService.criar(relatorioDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("análise");

            verify(relatorioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Relatórios")
    class BuscaRelatorioTests {

        @Test
        @DisplayName("Deve buscar todos os relatórios com sucesso")
        void deveBuscarTodosOsRelatorios() {
            // Arrange
            ChatEntity chat1 = new ChatEntity();
            chat1.setId(1L);

            ChatEntity chat2 = new ChatEntity();
            chat2.setId(2L);

            RelatorioEntity relatorio1 = new RelatorioEntity();
            relatorio1.setId(1L);
            relatorio1.setChat(chat1);
            relatorio1.setAcuracia(0.8f);
            relatorio1.setAnaliseFono("Análise 1");

            RelatorioEntity relatorio2 = new RelatorioEntity();
            relatorio2.setId(2L);
            relatorio2.setChat(chat2);
            relatorio2.setAcuracia(0.9f);
            relatorio2.setAnaliseFono("Análise 2");

            when(relatorioRepository.findAll()).thenReturn(Arrays.asList(relatorio1, relatorio2));

            // Act
            List<RelatorioDtoOut> resultados = relatorioService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getId()).isEqualTo(1L);
            assertThat(resultados.get(1).getId()).isEqualTo(2L);
            verify(relatorioRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar relatório por ID com sucesso")
        void deveBuscarRelatorioPorId() {
            // Arrange
            ChatEntity chat = new ChatEntity();
            chat.setId(1L);

            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(10L);
            relatorio.setChat(chat);
            relatorio.setAcuracia(0.85f);
            relatorio.setAnaliseFono("Análise detalhada");

            when(relatorioRepository.findById(10L)).thenReturn(Optional.of(relatorio));

            // Act
            Optional<RelatorioDtoOut> resultado = relatorioService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getAcuracia()).isEqualTo(0.85f);
            verify(relatorioRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando relatório não existe")
        void deveRetornarVazio_QuandoRelatorioNaoExiste() {
            // Arrange
            when(relatorioRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<RelatorioDtoOut> resultado = relatorioService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(relatorioRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar relatório por chat ID com sucesso")
        void deveBuscarRelatorioPorChatId() {
            // Arrange
            ChatEntity chat = new ChatEntity();
            chat.setId(5L);

            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(20L);
            relatorio.setChat(chat);
            relatorio.setAcuracia(0.75f);
            relatorio.setAnaliseFono("Análise do chat 5");

            when(relatorioRepository.findByChatId(5L)).thenReturn(Optional.of(relatorio));

            // Act
            Optional<RelatorioDtoOut> resultado = relatorioService.buscarPorChatId(5L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getChatId()).isEqualTo(5L);
            assertThat(resultado.get().getId()).isEqualTo(20L);
            verify(relatorioRepository, times(1)).findByChatId(5L);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Relatório")
    class AtualizacaoRelatorioTests {

        @Test
        @DisplayName("Deve atualizar relatório com sucesso")
        void deveAtualizarRelatorioComSucesso() {
            // Arrange
            ChatEntity chat = new ChatEntity();
            chat.setId(1L);

            RelatorioEntity relatorioExistente = new RelatorioEntity();
            relatorioExistente.setId(10L);
            relatorioExistente.setChat(chat);
            relatorioExistente.setAcuracia(0.7f);
            relatorioExistente.setAnaliseFono("Análise antiga");

            RelatorioDtoIn dadosAtualizados = new RelatorioDtoIn();
            dadosAtualizados.setChatId(1L);
            dadosAtualizados.setAcuracia(0.95f);
            dadosAtualizados.setAnaliseFono("Análise atualizada com mais detalhes");

            RelatorioEntity relatorioAtualizado = new RelatorioEntity();
            relatorioAtualizado.setId(10L);
            relatorioAtualizado.setChat(chat);
            relatorioAtualizado.setAcuracia(0.95f);
            relatorioAtualizado.setAnaliseFono("Análise atualizada com mais detalhes");

            when(relatorioRepository.findById(10L)).thenReturn(Optional.of(relatorioExistente));
            when(relatorioRepository.save(any(RelatorioEntity.class))).thenReturn(relatorioAtualizado);

            // Act
            RelatorioDtoOut resultado = relatorioService.atualizar(10L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getAcuracia()).isEqualTo(0.95f);
            assertThat(resultado.getAnaliseFono()).isEqualTo("Análise atualizada com mais detalhes");
            verify(relatorioRepository, times(1)).save(any(RelatorioEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar relatório inexistente")
        void deveLancarExcecao_QuandoAtualizarRelatorioInexistente() {
            // Arrange
            RelatorioDtoIn dadosAtualizados = new RelatorioDtoIn();
            dadosAtualizados.setChatId(1L);
            dadosAtualizados.setAcuracia(0.95f);
            dadosAtualizados.setAnaliseFono("Análise");

            when(relatorioRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> relatorioService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(relatorioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Relatório")
    class DelecaoRelatorioTests {

        @Test
        @DisplayName("Deve deletar relatório com sucesso")
        void deveDeletarRelatorioComSucesso() {
            // Arrange
            when(relatorioRepository.existsById(10L)).thenReturn(true);

            // Act
            relatorioService.deletar(10L);

            // Assert
            verify(relatorioRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar relatório inexistente")
        void deveLancarExcecao_QuandoDeletarRelatorioInexistente() {
            // Arrange
            when(relatorioRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> relatorioService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(relatorioRepository, never()).deleteById(any());
        }
    }
}
