package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.DisponibilidadeDtoIn;
import com.inatel.prototipo_ia.dto.out.DisponibilidadeDtoOut;
import com.inatel.prototipo_ia.entity.DisponibilidadeEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.DisponibilidadeRepository;
import com.inatel.prototipo_ia.repository.EspecialistaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {

    @Mock
    private DisponibilidadeRepository disponibilidadeRepository;

    @Mock
    private EspecialistaRepository especialistaRepository;

    @InjectMocks
    private DisponibilidadeService disponibilidadeService;

    @Nested
    @DisplayName("Testes de Criação de Disponibilidade")
    class CriacaoDisponibilidadeTests {

        @Test
        @DisplayName("Deve criar disponibilidade com sucesso quando dados válidos")
        void deveCriarDisponibilidadeComSucesso() {
            // Arrange
            DisponibilidadeDtoIn disponibilidadeDto = new DisponibilidadeDtoIn();
            disponibilidadeDto.setEspecialistaId(1L);
            disponibilidadeDto.setData(LocalDate.of(2025, 12, 15));
            disponibilidadeDto.setHoraInicio(LocalTime.of(9, 0));
            disponibilidadeDto.setHoraFim(LocalTime.of(17, 0));
            disponibilidadeDto.setStatus("Disponível");

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disponibilidadeSalva = new DisponibilidadeEntity();
            disponibilidadeSalva.setId(10L);
            disponibilidadeSalva.setEspecialista(especialista);
            disponibilidadeSalva.setData(LocalDate.of(2025, 12, 15));
            disponibilidadeSalva.setHoraInicio(LocalTime.of(9, 0));
            disponibilidadeSalva.setHoraFim(LocalTime.of(17, 0));
            disponibilidadeSalva.setStatus("Disponível");

            when(especialistaRepository.findById(1L)).thenReturn(Optional.of(especialista));
            when(disponibilidadeRepository.save(any(DisponibilidadeEntity.class))).thenReturn(disponibilidadeSalva);

            // Act
            DisponibilidadeDtoOut resultado = disponibilidadeService.criar(disponibilidadeDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getEspecialistaId()).isEqualTo(1L);
            assertThat(resultado.getData()).isEqualTo(LocalDate.of(2025, 12, 15));
            assertThat(resultado.getHoraInicio()).isEqualTo(LocalTime.of(9, 0));
            assertThat(resultado.getHoraFim()).isEqualTo(LocalTime.of(17, 0));
            assertThat(resultado.getStatus()).isEqualTo("Disponível");

            verify(disponibilidadeRepository, times(1)).save(any(DisponibilidadeEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(disponibilidadeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando especialista não existe")
        void deveLancarExcecao_QuandoEspecialistaNaoExiste() {
            // Arrange
            DisponibilidadeDtoIn disponibilidadeDto = new DisponibilidadeDtoIn();
            disponibilidadeDto.setEspecialistaId(999L);
            disponibilidadeDto.setData(LocalDate.now());
            disponibilidadeDto.setHoraInicio(LocalTime.of(9, 0));
            disponibilidadeDto.setHoraFim(LocalTime.of(17, 0));
            disponibilidadeDto.setStatus("Disponível");

            when(especialistaRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.criar(disponibilidadeDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("especialista")
                .hasMessageContaining("999");

            verify(disponibilidadeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando especialistaId é nulo")
        void deveLancarExcecao_QuandoEspecialistaIdNulo() {
            // Arrange
            DisponibilidadeDtoIn disponibilidadeDto = new DisponibilidadeDtoIn();
            disponibilidadeDto.setEspecialistaId(null);
            disponibilidadeDto.setData(LocalDate.now());
            disponibilidadeDto.setHoraInicio(LocalTime.of(9, 0));
            disponibilidadeDto.setHoraFim(LocalTime.of(17, 0));
            disponibilidadeDto.setStatus("Disponível");

            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.criar(disponibilidadeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("especialista");

            verify(disponibilidadeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando data é nula")
        void deveLancarExcecao_QuandoDataNula() {
            // Arrange
            DisponibilidadeDtoIn disponibilidadeDto = new DisponibilidadeDtoIn();
            disponibilidadeDto.setEspecialistaId(1L);
            disponibilidadeDto.setData(null);
            disponibilidadeDto.setHoraInicio(LocalTime.of(9, 0));
            disponibilidadeDto.setHoraFim(LocalTime.of(17, 0));
            disponibilidadeDto.setStatus("Disponível");

            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.criar(disponibilidadeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");

            verify(disponibilidadeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando hora de início é posterior à hora de fim")
        void deveLancarExcecao_QuandoHoraInicioDepoisHoraFim() {
            // Arrange
            DisponibilidadeDtoIn disponibilidadeDto = new DisponibilidadeDtoIn();
            disponibilidadeDto.setEspecialistaId(1L);
            disponibilidadeDto.setData(LocalDate.now());
            disponibilidadeDto.setHoraInicio(LocalTime.of(17, 0));
            disponibilidadeDto.setHoraFim(LocalTime.of(9, 0));
            disponibilidadeDto.setStatus("Disponível");

            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.criar(disponibilidadeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hora de início")
                .hasMessageContaining("posterior");

            verify(disponibilidadeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando status está em branco")
        void deveLancarExcecao_QuandoStatusEstaBranco() {
            // Arrange
            DisponibilidadeDtoIn disponibilidadeDto = new DisponibilidadeDtoIn();
            disponibilidadeDto.setEspecialistaId(1L);
            disponibilidadeDto.setData(LocalDate.now());
            disponibilidadeDto.setHoraInicio(LocalTime.of(9, 0));
            disponibilidadeDto.setHoraFim(LocalTime.of(17, 0));
            disponibilidadeDto.setStatus("   ");

            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.criar(disponibilidadeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status");

            verify(disponibilidadeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Disponibilidades")
    class BuscaDisponibilidadeTests {

        @Test
        @DisplayName("Deve buscar todas as disponibilidades com sucesso")
        void deveBuscarTodasAsDisponibilidades() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disp1 = new DisponibilidadeEntity();
            disp1.setId(1L);
            disp1.setEspecialista(especialista);

            DisponibilidadeEntity disp2 = new DisponibilidadeEntity();
            disp2.setId(2L);
            disp2.setEspecialista(especialista);

            when(disponibilidadeRepository.findAll()).thenReturn(Arrays.asList(disp1, disp2));

            // Act
            List<DisponibilidadeDtoOut> resultados = disponibilidadeService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getId()).isEqualTo(1L);
            assertThat(resultados.get(1).getId()).isEqualTo(2L);
            verify(disponibilidadeRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar disponibilidade por ID com sucesso")
        void deveBuscarDisponibilidadePorId() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disponibilidade = new DisponibilidadeEntity();
            disponibilidade.setId(10L);
            disponibilidade.setEspecialista(especialista);
            disponibilidade.setStatus("Disponível");

            when(disponibilidadeRepository.findById(10L)).thenReturn(Optional.of(disponibilidade));

            // Act
            Optional<DisponibilidadeDtoOut> resultado = disponibilidadeService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getStatus()).isEqualTo("Disponível");
            verify(disponibilidadeRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando disponibilidade não existe")
        void deveRetornarVazio_QuandoDisponibilidadeNaoExiste() {
            // Arrange
            when(disponibilidadeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<DisponibilidadeDtoOut> resultado = disponibilidadeService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(disponibilidadeRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar disponibilidades por especialista ID")
        void deveBuscarDisponibilidadesPorEspecialistaId() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disp1 = new DisponibilidadeEntity();
            disp1.setId(1L);
            disp1.setEspecialista(especialista);

            when(disponibilidadeRepository.findByEspecialistaId(1L)).thenReturn(Arrays.asList(disp1));

            // Act
            List<DisponibilidadeDtoOut> resultados = disponibilidadeService.buscarPorEspecialistaId(1L);

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getEspecialistaId()).isEqualTo(1L);
            verify(disponibilidadeRepository, times(1)).findByEspecialistaId(1L);
        }

        @Test
        @DisplayName("Deve buscar disponibilidades por data")
        void deveBuscarDisponibilidadesPorData() {
            // Arrange
            LocalDate data = LocalDate.of(2025, 12, 15);
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disp1 = new DisponibilidadeEntity();
            disp1.setId(1L);
            disp1.setEspecialista(especialista);
            disp1.setData(data);

            when(disponibilidadeRepository.findByData(data)).thenReturn(Arrays.asList(disp1));

            // Act
            List<DisponibilidadeDtoOut> resultados = disponibilidadeService.buscarPorData(data);

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getData()).isEqualTo(data);
            verify(disponibilidadeRepository, times(1)).findByData(data);
        }

        @Test
        @DisplayName("Deve lançar exceção quando data é nula na busca")
        void deveLancarExcecao_QuandoDataNulaNaBusca() {
            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.buscarPorData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");

            verify(disponibilidadeRepository, never()).findByData(any());
        }

        @Test
        @DisplayName("Deve buscar disponibilidades por status")
        void deveBuscarDisponibilidadesPorStatus() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disp1 = new DisponibilidadeEntity();
            disp1.setId(1L);
            disp1.setEspecialista(especialista);
            disp1.setStatus("Disponível");

            when(disponibilidadeRepository.findByStatus("Disponível")).thenReturn(Arrays.asList(disp1));

            // Act
            List<DisponibilidadeDtoOut> resultados = disponibilidadeService.buscarPorStatus("Disponível");

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getStatus()).isEqualTo("Disponível");
            verify(disponibilidadeRepository, times(1)).findByStatus("Disponível");
        }

        @Test
        @DisplayName("Deve buscar disponibilidades disponíveis")
        void deveBuscarDisponibilidadesDisponiveis() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disp1 = new DisponibilidadeEntity();
            disp1.setId(1L);
            disp1.setEspecialista(especialista);
            disp1.setStatus("disponível");

            when(disponibilidadeRepository.findDisponibilidadesDisponiveis()).thenReturn(Arrays.asList(disp1));

            // Act
            List<DisponibilidadeDtoOut> resultados = disponibilidadeService.buscarDisponiveis();

            // Assert
            assertThat(resultados).hasSize(1);
            verify(disponibilidadeRepository, times(1)).findDisponibilidadesDisponiveis();
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Disponibilidade")
    class AtualizacaoDisponibilidadeTests {

        @Test
        @DisplayName("Deve atualizar disponibilidade com sucesso")
        void deveAtualizarDisponibilidadeComSucesso() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);

            DisponibilidadeEntity disponibilidadeExistente = new DisponibilidadeEntity();
            disponibilidadeExistente.setId(10L);
            disponibilidadeExistente.setEspecialista(especialista);
            disponibilidadeExistente.setStatus("Disponível");

            DisponibilidadeDtoIn dadosAtualizados = new DisponibilidadeDtoIn();
            dadosAtualizados.setEspecialistaId(1L);
            dadosAtualizados.setData(LocalDate.now());
            dadosAtualizados.setHoraInicio(LocalTime.of(8, 0));
            dadosAtualizados.setHoraFim(LocalTime.of(18, 0));
            dadosAtualizados.setStatus("Ocupado");

            DisponibilidadeEntity disponibilidadeAtualizada = new DisponibilidadeEntity();
            disponibilidadeAtualizada.setId(10L);
            disponibilidadeAtualizada.setEspecialista(especialista);
            disponibilidadeAtualizada.setStatus("Ocupado");

            when(disponibilidadeRepository.findById(10L)).thenReturn(Optional.of(disponibilidadeExistente));
            when(disponibilidadeRepository.save(any(DisponibilidadeEntity.class))).thenReturn(disponibilidadeAtualizada);

            // Act
            DisponibilidadeDtoOut resultado = disponibilidadeService.atualizar(10L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getStatus()).isEqualTo("Ocupado");
            verify(disponibilidadeRepository, times(1)).save(any(DisponibilidadeEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar disponibilidade inexistente")
        void deveLancarExcecao_QuandoAtualizarDisponibilidadeInexistente() {
            // Arrange
            DisponibilidadeDtoIn dadosAtualizados = new DisponibilidadeDtoIn();
            dadosAtualizados.setEspecialistaId(1L);
            dadosAtualizados.setData(LocalDate.now());
            dadosAtualizados.setHoraInicio(LocalTime.of(9, 0));
            dadosAtualizados.setHoraFim(LocalTime.of(17, 0));
            dadosAtualizados.setStatus("Disponível");

            when(disponibilidadeRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(disponibilidadeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Disponibilidade")
    class DelecaoDisponibilidadeTests {

        @Test
        @DisplayName("Deve deletar disponibilidade com sucesso")
        void deveDeletarDisponibilidadeComSucesso() {
            // Arrange
            when(disponibilidadeRepository.existsById(10L)).thenReturn(true);

            // Act
            disponibilidadeService.deletar(10L);

            // Assert
            verify(disponibilidadeRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar disponibilidade inexistente")
        void deveLancarExcecao_QuandoDeletarDisponibilidadeInexistente() {
            // Arrange
            when(disponibilidadeRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> disponibilidadeService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(disponibilidadeRepository, never()).deleteById(any());
        }
    }
}
