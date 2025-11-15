package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.DetalheErroDtoIn;
import com.inatel.prototipo_ia.dto.out.DetalheErroDtoOut;
import com.inatel.prototipo_ia.entity.DetalheErroEntity;
import com.inatel.prototipo_ia.entity.RelatorioEntity;
import com.inatel.prototipo_ia.repository.DetalheErroRepository;
import com.inatel.prototipo_ia.repository.RelatorioRepository;
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
class DetalheErroServiceTest {

    @Mock
    private DetalheErroRepository detalheErroRepository;

    @Mock
    private RelatorioRepository relatorioRepository;

    @InjectMocks
    private DetalheErroService detalheErroService;

    @Nested
    @DisplayName("Testes de Criação de Detalhe de Erro")
    class CriacaoDetalheErroTests {

        @Test
        @DisplayName("Deve criar detalhe de erro com sucesso quando dados válidos")
        void deveCriarDetalheErroComSucesso() {
            // Arrange
            DetalheErroDtoIn detalheDto = new DetalheErroDtoIn();
            detalheDto.setRelatorioId(1L);
            detalheDto.setFonemaEsperado("R");
            detalheDto.setFonemaProduzido("L");
            detalheDto.setScoreDesvio(0.75f);

            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(1L);

            DetalheErroEntity detalheSalvo = new DetalheErroEntity();
            detalheSalvo.setId(10L);
            detalheSalvo.setRelatorio(relatorio);
            detalheSalvo.setFonemaEsperado("R");
            detalheSalvo.setFonemaProduzido("L");
            detalheSalvo.setScoreDesvio(0.75f);

            when(relatorioRepository.findById(1L)).thenReturn(Optional.of(relatorio));
            when(detalheErroRepository.save(any(DetalheErroEntity.class))).thenReturn(detalheSalvo);

            // Act
            DetalheErroDtoOut resultado = detalheErroService.criar(detalheDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getRelatorioId()).isEqualTo(1L);
            assertThat(resultado.getFonemaEsperado()).isEqualTo("R");
            assertThat(resultado.getFonemaProduzido()).isEqualTo("L");
            assertThat(resultado.getScoreDesvio()).isEqualTo(0.75f);

            verify(detalheErroRepository, times(1)).save(any(DetalheErroEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(detalheErroRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando relatório não existe")
        void deveLancarExcecao_QuandoRelatorioNaoExiste() {
            // Arrange
            DetalheErroDtoIn detalheDto = new DetalheErroDtoIn();
            detalheDto.setRelatorioId(999L);
            detalheDto.setFonemaEsperado("R");
            detalheDto.setFonemaProduzido("L");
            detalheDto.setScoreDesvio(0.75f);

            when(relatorioRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.criar(detalheDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("relatório")
                .hasMessageContaining("999");

            verify(detalheErroRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando relatórioId é nulo")
        void deveLancarExcecao_QuandoRelatorioIdNulo() {
            // Arrange
            DetalheErroDtoIn detalheDto = new DetalheErroDtoIn();
            detalheDto.setRelatorioId(null);
            detalheDto.setFonemaEsperado("R");
            detalheDto.setFonemaProduzido("L");
            detalheDto.setScoreDesvio(0.75f);

            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.criar(detalheDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relatório");

            verify(detalheErroRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando fonema esperado está em branco")
        void deveLancarExcecao_QuandoFonemaEsperadoEstaBranco() {
            // Arrange
            DetalheErroDtoIn detalheDto = new DetalheErroDtoIn();
            detalheDto.setRelatorioId(1L);
            detalheDto.setFonemaEsperado("   ");
            detalheDto.setFonemaProduzido("L");
            detalheDto.setScoreDesvio(0.75f);

            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.criar(detalheDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fonema esperado");

            verify(detalheErroRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando fonema produzido está em branco")
        void deveLancarExcecao_QuandoFonemaProduzidoEstaBranco() {
            // Arrange
            DetalheErroDtoIn detalheDto = new DetalheErroDtoIn();
            detalheDto.setRelatorioId(1L);
            detalheDto.setFonemaEsperado("R");
            detalheDto.setFonemaProduzido("   ");
            detalheDto.setScoreDesvio(0.75f);

            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.criar(detalheDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fonema produzido");

            verify(detalheErroRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando score de desvio é negativo")
        void deveLancarExcecao_QuandoScoreDesvioNegativo() {
            // Arrange
            DetalheErroDtoIn detalheDto = new DetalheErroDtoIn();
            detalheDto.setRelatorioId(1L);
            detalheDto.setFonemaEsperado("R");
            detalheDto.setFonemaProduzido("L");
            detalheDto.setScoreDesvio(-0.5f);

            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.criar(detalheDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("score de desvio");

            verify(detalheErroRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Detalhes de Erro")
    class BuscaDetalheErroTests {

        @Test
        @DisplayName("Deve buscar todos os detalhes de erro com sucesso")
        void deveBuscarTodosOsDetalhesDeErro() {
            // Arrange
            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(1L);

            DetalheErroEntity detalhe1 = new DetalheErroEntity();
            detalhe1.setId(1L);
            detalhe1.setRelatorio(relatorio);
            detalhe1.setFonemaEsperado("R");

            DetalheErroEntity detalhe2 = new DetalheErroEntity();
            detalhe2.setId(2L);
            detalhe2.setRelatorio(relatorio);
            detalhe2.setFonemaEsperado("S");

            when(detalheErroRepository.findAll()).thenReturn(Arrays.asList(detalhe1, detalhe2));

            // Act
            List<DetalheErroDtoOut> resultados = detalheErroService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getFonemaEsperado()).isEqualTo("R");
            assertThat(resultados.get(1).getFonemaEsperado()).isEqualTo("S");
            verify(detalheErroRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar detalhe de erro por ID com sucesso")
        void deveBuscarDetalheErroPorId() {
            // Arrange
            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(1L);

            DetalheErroEntity detalhe = new DetalheErroEntity();
            detalhe.setId(10L);
            detalhe.setRelatorio(relatorio);
            detalhe.setFonemaEsperado("R");
            detalhe.setFonemaProduzido("L");

            when(detalheErroRepository.findById(10L)).thenReturn(Optional.of(detalhe));

            // Act
            Optional<DetalheErroDtoOut> resultado = detalheErroService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getFonemaEsperado()).isEqualTo("R");
            verify(detalheErroRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando detalhe de erro não existe")
        void deveRetornarVazio_QuandoDetalheErroNaoExiste() {
            // Arrange
            when(detalheErroRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<DetalheErroDtoOut> resultado = detalheErroService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(detalheErroRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar detalhes de erro por relatório ID")
        void deveBuscarDetalhesErroPorRelatorioId() {
            // Arrange
            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(1L);

            DetalheErroEntity detalhe1 = new DetalheErroEntity();
            detalhe1.setId(1L);
            detalhe1.setRelatorio(relatorio);

            when(detalheErroRepository.findByRelatorioId(1L)).thenReturn(Arrays.asList(detalhe1));

            // Act
            List<DetalheErroDtoOut> resultados = detalheErroService.buscarPorRelatorioId(1L);

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getRelatorioId()).isEqualTo(1L);
            verify(detalheErroRepository, times(1)).findByRelatorioId(1L);
        }

        @Test
        @DisplayName("Deve buscar detalhes de erro por fonema esperado")
        void deveBuscarDetalhesErroPorFonemaEsperado() {
            // Arrange
            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(1L);

            DetalheErroEntity detalhe1 = new DetalheErroEntity();
            detalhe1.setId(1L);
            detalhe1.setRelatorio(relatorio);
            detalhe1.setFonemaEsperado("R");

            when(detalheErroRepository.findByFonemaEsperado("R")).thenReturn(Arrays.asList(detalhe1));

            // Act
            List<DetalheErroDtoOut> resultados = detalheErroService.buscarPorFonemaEsperado("R");

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getFonemaEsperado()).isEqualTo("R");
            verify(detalheErroRepository, times(1)).findByFonemaEsperado("R");
        }

        @Test
        @DisplayName("Deve lançar exceção quando fonema esperado está em branco na busca")
        void deveLancarExcecao_QuandoFonemaEsperadoEstaBrancoNaBusca() {
            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.buscarPorFonemaEsperado("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fonema esperado");

            verify(detalheErroRepository, never()).findByFonemaEsperado(any());
        }

        @Test
        @DisplayName("Deve buscar detalhes de erro com score maior que valor especificado")
        void deveBuscarDetalhesErroComScoreMaiorQue() {
            // Arrange
            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(1L);

            DetalheErroEntity detalhe1 = new DetalheErroEntity();
            detalhe1.setId(1L);
            detalhe1.setRelatorio(relatorio);
            detalhe1.setScoreDesvio(0.8f);

            when(detalheErroRepository.findByScoreDesvioGreaterThan(0.5f)).thenReturn(Arrays.asList(detalhe1));

            // Act
            List<DetalheErroDtoOut> resultados = detalheErroService.buscarComScoreMaiorQue(0.5f);

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getScoreDesvio()).isGreaterThan(0.5f);
            verify(detalheErroRepository, times(1)).findByScoreDesvioGreaterThan(0.5f);
        }

        @Test
        @DisplayName("Deve lançar exceção quando score é negativo na busca")
        void deveLancarExcecao_QuandoScoreNegativoNaBusca() {
            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.buscarComScoreMaiorQue(-0.5f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("score");

            verify(detalheErroRepository, never()).findByScoreDesvioGreaterThan(any());
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Detalhe de Erro")
    class AtualizacaoDetalheErroTests {

        @Test
        @DisplayName("Deve atualizar detalhe de erro com sucesso")
        void deveAtualizarDetalheErroComSucesso() {
            // Arrange
            RelatorioEntity relatorio = new RelatorioEntity();
            relatorio.setId(1L);

            DetalheErroEntity detalheExistente = new DetalheErroEntity();
            detalheExistente.setId(10L);
            detalheExistente.setRelatorio(relatorio);
            detalheExistente.setFonemaEsperado("R");
            detalheExistente.setScoreDesvio(0.5f);

            DetalheErroDtoIn dadosAtualizados = new DetalheErroDtoIn();
            dadosAtualizados.setRelatorioId(1L);
            dadosAtualizados.setFonemaEsperado("S");
            dadosAtualizados.setFonemaProduzido("T");
            dadosAtualizados.setScoreDesvio(0.9f);

            DetalheErroEntity detalheAtualizado = new DetalheErroEntity();
            detalheAtualizado.setId(10L);
            detalheAtualizado.setRelatorio(relatorio);
            detalheAtualizado.setFonemaEsperado("S");
            detalheAtualizado.setFonemaProduzido("T");
            detalheAtualizado.setScoreDesvio(0.9f);

            when(detalheErroRepository.findById(10L)).thenReturn(Optional.of(detalheExistente));
            when(detalheErroRepository.save(any(DetalheErroEntity.class))).thenReturn(detalheAtualizado);

            // Act
            DetalheErroDtoOut resultado = detalheErroService.atualizar(10L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getFonemaEsperado()).isEqualTo("S");
            assertThat(resultado.getFonemaProduzido()).isEqualTo("T");
            assertThat(resultado.getScoreDesvio()).isEqualTo(0.9f);
            verify(detalheErroRepository, times(1)).save(any(DetalheErroEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar detalhe de erro inexistente")
        void deveLancarExcecao_QuandoAtualizarDetalheErroInexistente() {
            // Arrange
            DetalheErroDtoIn dadosAtualizados = new DetalheErroDtoIn();
            dadosAtualizados.setRelatorioId(1L);
            dadosAtualizados.setFonemaEsperado("R");
            dadosAtualizados.setFonemaProduzido("L");
            dadosAtualizados.setScoreDesvio(0.5f);

            when(detalheErroRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(detalheErroRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Detalhe de Erro")
    class DelecaoDetalheErroTests {

        @Test
        @DisplayName("Deve deletar detalhe de erro com sucesso")
        void deveDeletarDetalheErroComSucesso() {
            // Arrange
            when(detalheErroRepository.existsById(10L)).thenReturn(true);

            // Act
            detalheErroService.deletar(10L);

            // Assert
            verify(detalheErroRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar detalhe de erro inexistente")
        void deveLancarExcecao_QuandoDeletarDetalheErroInexistente() {
            // Arrange
            when(detalheErroRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> detalheErroService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(detalheErroRepository, never()).deleteById(any());
        }
    }
}
