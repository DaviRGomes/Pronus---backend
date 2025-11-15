package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ConteudoTesteDtoIn;
import com.inatel.prototipo_ia.dto.out.ConteudoTesteDtoOut;
import com.inatel.prototipo_ia.entity.ConteudoTesteEntity;
import com.inatel.prototipo_ia.repository.ConteudoTesteRepository;
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
class ConteudoTesteServiceTest {

    @Mock
    private ConteudoTesteRepository conteudoTesteRepository;

    @InjectMocks
    private ConteudoTesteService conteudoTesteService;

    @Nested
    @DisplayName("Testes de Criação de Conteúdo de Teste")
    class CriacaoConteudoTesteTests {

        @Test
        @DisplayName("Deve criar conteúdo de teste com sucesso quando dados válidos")
        void deveCriarConteudoTesteComSucesso() {
            // Arrange
            ConteudoTesteDtoIn conteudoDto = new ConteudoTesteDtoIn();
            conteudoDto.setTextoFrase("O rato roeu a roupa do rei de Roma");
            conteudoDto.setFonemasChave("R, RR");
            conteudoDto.setDificuldade("Intermediário");
            conteudoDto.setIdioma("Português");

            ConteudoTesteEntity conteudoSalvo = new ConteudoTesteEntity();
            conteudoSalvo.setId(1L);
            conteudoSalvo.setTextoFrase("O rato roeu a roupa do rei de Roma");
            conteudoSalvo.setFonemasChave("R, RR");
            conteudoSalvo.setDificuldade("Intermediário");
            conteudoSalvo.setIdioma("Português");

            when(conteudoTesteRepository.save(any(ConteudoTesteEntity.class))).thenReturn(conteudoSalvo);

            // Act
            ConteudoTesteDtoOut resultado = conteudoTesteService.criar(conteudoDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getTextoFrase()).isEqualTo("O rato roeu a roupa do rei de Roma");
            assertThat(resultado.getFonemasChave()).isEqualTo("R, RR");
            assertThat(resultado.getDificuldade()).isEqualTo("Intermediário");
            assertThat(resultado.getIdioma()).isEqualTo("Português");

            verify(conteudoTesteRepository, times(1)).save(any(ConteudoTesteEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(conteudoTesteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando texto da frase está em branco")
        void deveLancarExcecao_QuandoTextoFraseEstaBranco() {
            // Arrange
            ConteudoTesteDtoIn conteudoDto = new ConteudoTesteDtoIn();
            conteudoDto.setTextoFrase("   ");
            conteudoDto.setFonemasChave("R");
            conteudoDto.setDificuldade("Básico");
            conteudoDto.setIdioma("Português");

            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.criar(conteudoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("texto da frase");

            verify(conteudoTesteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando fonemas-chave estão em branco")
        void deveLancarExcecao_QuandoFonemasChaveEstaBranco() {
            // Arrange
            ConteudoTesteDtoIn conteudoDto = new ConteudoTesteDtoIn();
            conteudoDto.setTextoFrase("Teste de frase");
            conteudoDto.setFonemasChave("   ");
            conteudoDto.setDificuldade("Básico");
            conteudoDto.setIdioma("Português");

            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.criar(conteudoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fonemas-chave");

            verify(conteudoTesteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando dificuldade está em branco")
        void deveLancarExcecao_QuandoDificuldadeEstaBranco() {
            // Arrange
            ConteudoTesteDtoIn conteudoDto = new ConteudoTesteDtoIn();
            conteudoDto.setTextoFrase("Teste de frase");
            conteudoDto.setFonemasChave("R");
            conteudoDto.setDificuldade("   ");
            conteudoDto.setIdioma("Português");

            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.criar(conteudoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dificuldade");

            verify(conteudoTesteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idioma está em branco")
        void deveLancarExcecao_QuandoIdiomaEstaBranco() {
            // Arrange
            ConteudoTesteDtoIn conteudoDto = new ConteudoTesteDtoIn();
            conteudoDto.setTextoFrase("Teste de frase");
            conteudoDto.setFonemasChave("R");
            conteudoDto.setDificuldade("Básico");
            conteudoDto.setIdioma("   ");

            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.criar(conteudoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idioma");

            verify(conteudoTesteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Conteúdos de Teste")
    class BuscaConteudoTesteTests {

        @Test
        @DisplayName("Deve buscar todos os conteúdos de teste com sucesso")
        void deveBuscarTodosOsConteudosTeste() {
            // Arrange
            ConteudoTesteEntity conteudo1 = new ConteudoTesteEntity();
            conteudo1.setId(1L);
            conteudo1.setTextoFrase("Frase 1");
            conteudo1.setDificuldade("Básico");

            ConteudoTesteEntity conteudo2 = new ConteudoTesteEntity();
            conteudo2.setId(2L);
            conteudo2.setTextoFrase("Frase 2");
            conteudo2.setDificuldade("Avançado");

            when(conteudoTesteRepository.findAll()).thenReturn(Arrays.asList(conteudo1, conteudo2));

            // Act
            List<ConteudoTesteDtoOut> resultados = conteudoTesteService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getTextoFrase()).isEqualTo("Frase 1");
            assertThat(resultados.get(1).getTextoFrase()).isEqualTo("Frase 2");
            verify(conteudoTesteRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar conteúdo de teste por ID com sucesso")
        void deveBuscarConteudoTestePorId() {
            // Arrange
            ConteudoTesteEntity conteudo = new ConteudoTesteEntity();
            conteudo.setId(10L);
            conteudo.setTextoFrase("Teste de frase");
            conteudo.setDificuldade("Intermediário");

            when(conteudoTesteRepository.findById(10L)).thenReturn(Optional.of(conteudo));

            // Act
            Optional<ConteudoTesteDtoOut> resultado = conteudoTesteService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getTextoFrase()).isEqualTo("Teste de frase");
            verify(conteudoTesteRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando conteúdo de teste não existe")
        void deveRetornarVazio_QuandoConteudoTesteNaoExiste() {
            // Arrange
            when(conteudoTesteRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<ConteudoTesteDtoOut> resultado = conteudoTesteService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(conteudoTesteRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar conteúdos de teste por dificuldade")
        void deveBuscarConteudosTestePorDificuldade() {
            // Arrange
            ConteudoTesteEntity conteudo1 = new ConteudoTesteEntity();
            conteudo1.setId(1L);
            conteudo1.setDificuldade("Básico");
            conteudo1.setTextoFrase("Frase básica");

            when(conteudoTesteRepository.findByDificuldade("Básico")).thenReturn(Arrays.asList(conteudo1));

            // Act
            List<ConteudoTesteDtoOut> resultados = conteudoTesteService.buscarPorDificuldade("Básico");

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getDificuldade()).isEqualTo("Básico");
            verify(conteudoTesteRepository, times(1)).findByDificuldade("Básico");
        }

        @Test
        @DisplayName("Deve lançar exceção quando dificuldade está em branco na busca")
        void deveLancarExcecao_QuandoDificuldadeEstaBrancoNaBusca() {
            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.buscarPorDificuldade("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dificuldade");

            verify(conteudoTesteRepository, never()).findByDificuldade(any());
        }

        @Test
        @DisplayName("Deve buscar conteúdos de teste por idioma")
        void deveBuscarConteudosTestePorIdioma() {
            // Arrange
            ConteudoTesteEntity conteudo1 = new ConteudoTesteEntity();
            conteudo1.setId(1L);
            conteudo1.setIdioma("Português");
            conteudo1.setTextoFrase("Frase em português");

            when(conteudoTesteRepository.findByIdioma("Português")).thenReturn(Arrays.asList(conteudo1));

            // Act
            List<ConteudoTesteDtoOut> resultados = conteudoTesteService.buscarPorIdioma("Português");

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getIdioma()).isEqualTo("Português");
            verify(conteudoTesteRepository, times(1)).findByIdioma("Português");
        }

        @Test
        @DisplayName("Deve buscar conteúdos de teste por dificuldade e idioma")
        void deveBuscarConteudosTestePorDificuldadeEIdioma() {
            // Arrange
            ConteudoTesteEntity conteudo1 = new ConteudoTesteEntity();
            conteudo1.setId(1L);
            conteudo1.setDificuldade("Básico");
            conteudo1.setIdioma("Português");
            conteudo1.setTextoFrase("Frase básica em português");

            when(conteudoTesteRepository.findByDificuldadeAndIdioma("Básico", "Português"))
                .thenReturn(Arrays.asList(conteudo1));

            // Act
            List<ConteudoTesteDtoOut> resultados = conteudoTesteService.buscarPorDificuldadeEIdioma("Básico", "Português");

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getDificuldade()).isEqualTo("Básico");
            assertThat(resultados.get(0).getIdioma()).isEqualTo("Português");
            verify(conteudoTesteRepository, times(1)).findByDificuldadeAndIdioma("Básico", "Português");
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Conteúdo de Teste")
    class AtualizacaoConteudoTesteTests {

        @Test
        @DisplayName("Deve atualizar conteúdo de teste com sucesso")
        void deveAtualizarConteudoTesteComSucesso() {
            // Arrange
            ConteudoTesteEntity conteudoExistente = new ConteudoTesteEntity();
            conteudoExistente.setId(1L);
            conteudoExistente.setTextoFrase("Frase Antiga");
            conteudoExistente.setDificuldade("Básico");

            ConteudoTesteDtoIn dadosAtualizados = new ConteudoTesteDtoIn();
            dadosAtualizados.setTextoFrase("Frase Atualizada");
            dadosAtualizados.setFonemasChave("R, S");
            dadosAtualizados.setDificuldade("Intermediário");
            dadosAtualizados.setIdioma("Português");

            ConteudoTesteEntity conteudoAtualizado = new ConteudoTesteEntity();
            conteudoAtualizado.setId(1L);
            conteudoAtualizado.setTextoFrase("Frase Atualizada");
            conteudoAtualizado.setDificuldade("Intermediário");

            when(conteudoTesteRepository.findById(1L)).thenReturn(Optional.of(conteudoExistente));
            when(conteudoTesteRepository.save(any(ConteudoTesteEntity.class))).thenReturn(conteudoAtualizado);

            // Act
            ConteudoTesteDtoOut resultado = conteudoTesteService.atualizar(1L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTextoFrase()).isEqualTo("Frase Atualizada");
            assertThat(resultado.getDificuldade()).isEqualTo("Intermediário");
            verify(conteudoTesteRepository, times(1)).save(any(ConteudoTesteEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar conteúdo de teste inexistente")
        void deveLancarExcecao_QuandoAtualizarConteudoTesteInexistente() {
            // Arrange
            ConteudoTesteDtoIn dadosAtualizados = new ConteudoTesteDtoIn();
            dadosAtualizados.setTextoFrase("Nova Frase");
            dadosAtualizados.setFonemasChave("R");
            dadosAtualizados.setDificuldade("Básico");
            dadosAtualizados.setIdioma("Português");

            when(conteudoTesteRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(conteudoTesteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Conteúdo de Teste")
    class DelecaoConteudoTesteTests {

        @Test
        @DisplayName("Deve deletar conteúdo de teste com sucesso")
        void deveDeletarConteudoTesteComSucesso() {
            // Arrange
            when(conteudoTesteRepository.existsById(10L)).thenReturn(true);

            // Act
            conteudoTesteService.deletar(10L);

            // Assert
            verify(conteudoTesteRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar conteúdo de teste inexistente")
        void deveLancarExcecao_QuandoDeletarConteudoTesteInexistente() {
            // Arrange
            when(conteudoTesteRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> conteudoTesteService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(conteudoTesteRepository, never()).deleteById(any());
        }
    }
}
