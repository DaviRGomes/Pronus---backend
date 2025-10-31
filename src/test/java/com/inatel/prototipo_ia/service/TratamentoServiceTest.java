package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.TratamentoDtoIn;
import com.inatel.prototipo_ia.dto.out.TratamentoDtoOut;
import com.inatel.prototipo_ia.entity.ConteudoTesteEntity;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import com.inatel.prototipo_ia.entity.TratamentoEntity;
import com.inatel.prototipo_ia.repository.ConteudoTesteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.repository.TratamentoRepository;
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
class TratamentoServiceTest {

    @Mock
    private TratamentoRepository tratamentoRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ConteudoTesteRepository conteudoTesteRepository;

    @InjectMocks
    private TratamentoService tratamentoService;

    @Nested
    @DisplayName("Testes de Criação de Tratamento")
    class CriacaoTratamentoTests {

        @Test
        @DisplayName("Deve criar tratamento com sucesso quando dados válidos")
        void deveCriarTratamentoComSucesso() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(1L);
            tratamentoDto.setConteudoTesteId(2L);
            tratamentoDto.setTipoTratamento("Fonético");
            tratamentoDto.setQuantidadeDia(3);
            tratamentoDto.setPersonalizado(true);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            ConteudoTesteEntity conteudoTeste = new ConteudoTesteEntity();
            conteudoTeste.setId(2L);

            TratamentoEntity tratamentoSalvo = new TratamentoEntity();
            tratamentoSalvo.setId(10L);
            tratamentoSalvo.setProfissional(profissional);
            tratamentoSalvo.setConteudoTeste(conteudoTeste);
            tratamentoSalvo.setTipoTratamento("Fonético");
            tratamentoSalvo.setQuantidadeDia(3);
            tratamentoSalvo.setPersonalizado(true);

            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(conteudoTesteRepository.findById(2L)).thenReturn(Optional.of(conteudoTeste));
            when(tratamentoRepository.save(any(TratamentoEntity.class))).thenReturn(tratamentoSalvo);

            // Act
            TratamentoDtoOut resultado = tratamentoService.criar(tratamentoDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getProfissionalId()).isEqualTo(1L);
            assertThat(resultado.getConteudoTesteId()).isEqualTo(2L);
            assertThat(resultado.getTipoTratamento()).isEqualTo("Fonético");
            assertThat(resultado.getQuantidadeDia()).isEqualTo(3);

            verify(tratamentoRepository, times(1)).save(any(TratamentoEntity.class));
        }

        @Test
        @DisplayName("Deve criar tratamento sem conteúdo de teste")
        void deveCriarTratamentoSemConteudoTeste() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(1L);
            tratamentoDto.setConteudoTesteId(null);
            tratamentoDto.setTipoTratamento("Geral");
            tratamentoDto.setQuantidadeDia(5);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            TratamentoEntity tratamentoSalvo = new TratamentoEntity();
            tratamentoSalvo.setId(10L);
            tratamentoSalvo.setProfissional(profissional);
            tratamentoSalvo.setConteudoTeste(null);
            tratamentoSalvo.setTipoTratamento("Geral");
            tratamentoSalvo.setQuantidadeDia(5);

            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(tratamentoRepository.save(any(TratamentoEntity.class))).thenReturn(tratamentoSalvo);

            // Act
            TratamentoDtoOut resultado = tratamentoService.criar(tratamentoDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getConteudoTesteId()).isNull();
            verify(tratamentoRepository, times(1)).save(any(TratamentoEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(tratamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando profissional não existe")
        void deveLancarExcecao_QuandoProfissionalNaoExiste() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(999L);
            tratamentoDto.setTipoTratamento("Fonético");
            tratamentoDto.setQuantidadeDia(3);

            when(profissionalRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.criar(tratamentoDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("profissional")
                .hasMessageContaining("999");

            verify(tratamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando conteúdo de teste não existe")
        void deveLancarExcecao_QuandoConteudoTesteNaoExiste() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(1L);
            tratamentoDto.setConteudoTesteId(999L);
            tratamentoDto.setTipoTratamento("Fonético");
            tratamentoDto.setQuantidadeDia(3);

            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
            when(conteudoTesteRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.criar(tratamentoDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Conteúdo de teste")
                .hasMessageContaining("999");

            verify(tratamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando profissionalId é nulo")
        void deveLancarExcecao_QuandoProfissionalIdNulo() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(null);
            tratamentoDto.setTipoTratamento("Fonético");
            tratamentoDto.setQuantidadeDia(3);

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.criar(tratamentoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profissional");

            verify(tratamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tipo de tratamento está em branco")
        void deveLancarExcecao_QuandoTipoTratamentoEstaBranco() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(1L);
            tratamentoDto.setTipoTratamento("   ");
            tratamentoDto.setQuantidadeDia(3);

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.criar(tratamentoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo de tratamento");

            verify(tratamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade de dias é zero")
        void deveLancarExcecao_QuandoQuantidadeDiaZero() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(1L);
            tratamentoDto.setTipoTratamento("Fonético");
            tratamentoDto.setQuantidadeDia(0);

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.criar(tratamentoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantidade de dias");

            verify(tratamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade de dias é negativa")
        void deveLancarExcecao_QuandoQuantidadeDiaNegativa() {
            // Arrange
            TratamentoDtoIn tratamentoDto = new TratamentoDtoIn();
            tratamentoDto.setProfissionalId(1L);
            tratamentoDto.setTipoTratamento("Fonético");
            tratamentoDto.setQuantidadeDia(-5);

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.criar(tratamentoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantidade de dias");

            verify(tratamentoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Tratamentos")
    class BuscaTratamentoTests {

        @Test
        @DisplayName("Deve buscar todos os tratamentos com sucesso")
        void deveBuscarTodosOsTratamentos() {
            // Arrange
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            TratamentoEntity tratamento1 = new TratamentoEntity();
            tratamento1.setId(1L);
            tratamento1.setProfissional(profissional);
            tratamento1.setTipoTratamento("Fonético");

            TratamentoEntity tratamento2 = new TratamentoEntity();
            tratamento2.setId(2L);
            tratamento2.setProfissional(profissional);
            tratamento2.setTipoTratamento("Respiração");

            when(tratamentoRepository.findAll()).thenReturn(Arrays.asList(tratamento1, tratamento2));

            // Act
            List<TratamentoDtoOut> resultados = tratamentoService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getTipoTratamento()).isEqualTo("Fonético");
            assertThat(resultados.get(1).getTipoTratamento()).isEqualTo("Respiração");
            verify(tratamentoRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar tratamento por ID com sucesso")
        void deveBuscarTratamentoPorId() {
            // Arrange
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            TratamentoEntity tratamento = new TratamentoEntity();
            tratamento.setId(10L);
            tratamento.setProfissional(profissional);
            tratamento.setTipoTratamento("Fonético");
            tratamento.setQuantidadeDia(5);

            when(tratamentoRepository.findById(10L)).thenReturn(Optional.of(tratamento));

            // Act
            Optional<TratamentoDtoOut> resultado = tratamentoService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getTipoTratamento()).isEqualTo("Fonético");
            verify(tratamentoRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando tratamento não existe")
        void deveRetornarVazio_QuandoTratamentoNaoExiste() {
            // Arrange
            when(tratamentoRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<TratamentoDtoOut> resultado = tratamentoService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(tratamentoRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar tratamentos por profissional ID")
        void deveBuscarTratamentosPorProfissionalId() {
            // Arrange
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            TratamentoEntity tratamento1 = new TratamentoEntity();
            tratamento1.setId(1L);
            tratamento1.setProfissional(profissional);

            TratamentoEntity tratamento2 = new TratamentoEntity();
            tratamento2.setId(2L);
            tratamento2.setProfissional(profissional);

            when(tratamentoRepository.findByProfissionalId(1L)).thenReturn(Arrays.asList(tratamento1, tratamento2));

            // Act
            List<TratamentoDtoOut> resultados = tratamentoService.buscarPorProfissionalId(1L);

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(t -> t.getProfissionalId().equals(1L));
            verify(tratamentoRepository, times(1)).findByProfissionalId(1L);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Tratamento")
    class AtualizacaoTratamentoTests {

        @Test
        @DisplayName("Deve atualizar tratamento com sucesso")
        void deveAtualizarTratamentoComSucesso() {
            // Arrange
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            TratamentoEntity tratamentoExistente = new TratamentoEntity();
            tratamentoExistente.setId(10L);
            tratamentoExistente.setProfissional(profissional);
            tratamentoExistente.setTipoTratamento("Antigo");
            tratamentoExistente.setQuantidadeDia(3);

            TratamentoDtoIn dadosAtualizados = new TratamentoDtoIn();
            dadosAtualizados.setProfissionalId(1L);
            dadosAtualizados.setTipoTratamento("Atualizado");
            dadosAtualizados.setQuantidadeDia(7);

            TratamentoEntity tratamentoAtualizado = new TratamentoEntity();
            tratamentoAtualizado.setId(10L);
            tratamentoAtualizado.setProfissional(profissional);
            tratamentoAtualizado.setTipoTratamento("Atualizado");
            tratamentoAtualizado.setQuantidadeDia(7);

            when(tratamentoRepository.findById(10L)).thenReturn(Optional.of(tratamentoExistente));
            when(tratamentoRepository.save(any(TratamentoEntity.class))).thenReturn(tratamentoAtualizado);

            // Act
            TratamentoDtoOut resultado = tratamentoService.atualizar(10L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getTipoTratamento()).isEqualTo("Atualizado");
            assertThat(resultado.getQuantidadeDia()).isEqualTo(7);
            verify(tratamentoRepository, times(1)).save(any(TratamentoEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar tratamento inexistente")
        void deveLancarExcecao_QuandoAtualizarTratamentoInexistente() {
            // Arrange
            TratamentoDtoIn dadosAtualizados = new TratamentoDtoIn();
            dadosAtualizados.setProfissionalId(1L);
            dadosAtualizados.setTipoTratamento("Novo");
            dadosAtualizados.setQuantidadeDia(5);

            when(tratamentoRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(tratamentoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Tratamento")
    class DelecaoTratamentoTests {

        @Test
        @DisplayName("Deve deletar tratamento com sucesso")
        void deveDeletarTratamentoComSucesso() {
            // Arrange
            when(tratamentoRepository.existsById(10L)).thenReturn(true);

            // Act
            tratamentoService.deletar(10L);

            // Assert
            verify(tratamentoRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar tratamento inexistente")
        void deveLancarExcecao_QuandoDeletarTratamentoInexistente() {
            // Arrange
            when(tratamentoRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(tratamentoRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Testes de Queries Customizadas")
    class QueriesCustomizadasTests {

        @Test
        @DisplayName("Deve buscar tratamentos por tipo")
        void deveBuscarTratamentosPorTipo() {
            // Arrange
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            TratamentoEntity tratamento1 = new TratamentoEntity();
            tratamento1.setId(1L);
            tratamento1.setProfissional(profissional);
            tratamento1.setTipoTratamento("Fonético");

            TratamentoEntity tratamento2 = new TratamentoEntity();
            tratamento2.setId(2L);
            tratamento2.setProfissional(profissional);
            tratamento2.setTipoTratamento("Fonético");

            when(tratamentoRepository.findByTipoTratamentoIgnoreCase("Fonético"))
                .thenReturn(Arrays.asList(tratamento1, tratamento2));

            // Act
            List<TratamentoDtoOut> resultados = tratamentoService.buscarPorTipo("Fonético");

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(t -> t.getTipoTratamento().equals("Fonético"));
            verify(tratamentoRepository, times(1)).findByTipoTratamentoIgnoreCase("Fonético");
        }

        @Test
        @DisplayName("Deve lançar exceção quando tipo está em branco")
        void deveLancarExcecao_QuandoTipoEstaBranco() {
            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.buscarPorTipo("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo de tratamento");

            verify(tratamentoRepository, never()).findByTipoTratamentoIgnoreCase(any());
        }

        @Test
        @DisplayName("Deve buscar tratamentos por tipo e quantidade mínima")
        void deveBuscarTratamentosPorTipoEQuantidadeMinima() {
            // Arrange
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(1L);

            TratamentoEntity tratamento1 = new TratamentoEntity();
            tratamento1.setId(1L);
            tratamento1.setProfissional(profissional);
            tratamento1.setTipoTratamento("Fonético");
            tratamento1.setQuantidadeDia(10);

            when(tratamentoRepository.findByTipoTratamentoAndQuantidadeDiaGreaterThanEqual("Fonético", 5))
                .thenReturn(Arrays.asList(tratamento1));

            // Act
            List<TratamentoDtoOut> resultados = tratamentoService.buscarPorTipoEQuantidadeMinima("Fonético", 5);

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getQuantidadeDia()).isGreaterThanOrEqualTo(5);
            verify(tratamentoRepository, times(1))
                .findByTipoTratamentoAndQuantidadeDiaGreaterThanEqual("Fonético", 5);
        }

        @Test
        @DisplayName("Deve lançar exceção quando quantidade mínima é negativa")
        void deveLancarExcecao_QuandoQuantidadeMinimaInvalida() {
            // Act & Assert
            assertThatThrownBy(() -> tratamentoService.buscarPorTipoEQuantidadeMinima("Fonético", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantidade mínima");

            verify(tratamentoRepository, never())
                .findByTipoTratamentoAndQuantidadeDiaGreaterThanEqual(any(), any());
        }
    }
}
