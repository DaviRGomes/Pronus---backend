package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.EspecialistaDtoIn;
import com.inatel.prototipo_ia.dto.out.EspecialistaDtoOut;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.ConsultaRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialistaServiceTest {

    @Mock
    private EspecialistaRepository especialistaRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private DisponibilidadeRepository disponibilidadeRepository;

    @InjectMocks
    private EspecialistaService especialistaService;

    @Nested
    @DisplayName("Testes de Criação de Especialista")
    class CriacaoEspecialistaTests {

        @Test
        @DisplayName("Deve criar especialista com sucesso quando dados válidos")
        void deveCriarEspecialistaComSucesso() {
            // Arrange
            EspecialistaDtoIn especialistaDto = new EspecialistaDtoIn();
            especialistaDto.setNome("Dr. Carlos Silva");
            especialistaDto.setIdade(40);
            especialistaDto.setEndereco("Rua Principal, 100");
            especialistaDto.setCrmFono("CRM 12345");
            especialistaDto.setEspecialidade("Fonoaudiologia");

            EspecialistaEntity especialistaSalvo = new EspecialistaEntity();
            especialistaSalvo.setId(1L);
            especialistaSalvo.setNome("Dr. Carlos Silva");
            especialistaSalvo.setIdade(40);
            especialistaSalvo.setEndereco("Rua Principal, 100");
            especialistaSalvo.setCrmFono("CRM 12345");
            especialistaSalvo.setEspecialidade("Fonoaudiologia");

            when(especialistaRepository.save(any(EspecialistaEntity.class))).thenReturn(especialistaSalvo);

            // Act
            EspecialistaDtoOut resultado = especialistaService.criar(especialistaDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Dr. Carlos Silva");
            assertThat(resultado.getCrmFono()).isEqualTo("CRM 12345");
            assertThat(resultado.getEspecialidade()).isEqualTo("Fonoaudiologia");

            verify(especialistaRepository, times(1)).save(any(EspecialistaEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> especialistaService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(especialistaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome está em branco")
        void deveLancarExcecao_QuandoNomeEstaBranco() {
            // Arrange
            EspecialistaDtoIn especialistaDto = new EspecialistaDtoIn();
            especialistaDto.setNome("   ");
            especialistaDto.setCrmFono("CRM 123");
            especialistaDto.setEspecialidade("Fonoaudiologia");

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.criar(especialistaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");

            verify(especialistaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CRM/CRFA está em branco")
        void deveLancarExcecao_QuandoCrmFonoEstaBranco() {
            // Arrange
            EspecialistaDtoIn especialistaDto = new EspecialistaDtoIn();
            especialistaDto.setNome("Dr. Silva");
            especialistaDto.setCrmFono("   ");
            especialistaDto.setEspecialidade("Fonoaudiologia");

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.criar(especialistaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CRM/CRFA");

            verify(especialistaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando especialidade está em branco")
        void deveLancarExcecao_QuandoEspecialidadeEstaBranco() {
            // Arrange
            EspecialistaDtoIn especialistaDto = new EspecialistaDtoIn();
            especialistaDto.setNome("Dr. Silva");
            especialistaDto.setCrmFono("CRM 123");
            especialistaDto.setEspecialidade("   ");

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.criar(especialistaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("especialidade");

            verify(especialistaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade é negativa")
        void deveLancarExcecao_QuandoIdadeNegativa() {
            // Arrange
            EspecialistaDtoIn especialistaDto = new EspecialistaDtoIn();
            especialistaDto.setNome("Dr. Silva");
            especialistaDto.setCrmFono("CRM 123");
            especialistaDto.setEspecialidade("Fonoaudiologia");
            especialistaDto.setIdade(-5);

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.criar(especialistaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idade");

            verify(especialistaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Especialistas")
    class BuscaEspecialistaTests {

        @Test
        @DisplayName("Deve buscar todos os especialistas com sucesso")
        void deveBuscarTodosOsEspecialistas() {
            // Arrange
            EspecialistaEntity esp1 = new EspecialistaEntity();
            esp1.setId(1L);
            esp1.setNome("Dr. Silva");
            esp1.setEspecialidade("Fonoaudiologia");

            EspecialistaEntity esp2 = new EspecialistaEntity();
            esp2.setId(2L);
            esp2.setNome("Dra. Santos");
            esp2.setEspecialidade("Neurologia");

            when(especialistaRepository.findAll()).thenReturn(Arrays.asList(esp1, esp2));

            // Act
            List<EspecialistaDtoOut> resultados = especialistaService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getNome()).isEqualTo("Dr. Silva");
            assertThat(resultados.get(1).getNome()).isEqualTo("Dra. Santos");
            verify(especialistaRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar especialista por ID com sucesso")
        void deveBuscarEspecialistaPorId() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(10L);
            especialista.setNome("Dr. João");
            especialista.setEspecialidade("Fonoaudiologia");

            when(especialistaRepository.findById(10L)).thenReturn(Optional.of(especialista));

            // Act
            Optional<EspecialistaDtoOut> resultado = especialistaService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getNome()).isEqualTo("Dr. João");
            verify(especialistaRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando especialista não existe")
        void deveRetornarVazio_QuandoEspecialistaNaoExiste() {
            // Arrange
            when(especialistaRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<EspecialistaDtoOut> resultado = especialistaService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(especialistaRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar especialistas por especialidade")
        void deveBuscarEspecialistasPorEspecialidade() {
            // Arrange
            EspecialistaEntity esp1 = new EspecialistaEntity();
            esp1.setId(1L);
            esp1.setNome("Dr. Silva");
            esp1.setEspecialidade("Fonoaudiologia");

            when(especialistaRepository.findByEspecialidade("Fonoaudiologia"))
                .thenReturn(Arrays.asList(esp1));

            // Act
            List<EspecialistaDtoOut> resultados = especialistaService.buscarPorEspecialidade("Fonoaudiologia");

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getEspecialidade()).isEqualTo("Fonoaudiologia");
            verify(especialistaRepository, times(1)).findByEspecialidade("Fonoaudiologia");
        }

        @Test
        @DisplayName("Deve lançar exceção quando especialidade está em branco na busca")
        void deveLancarExcecao_QuandoEspecialidadeEstaBrancoNaBusca() {
            // Act & Assert
            assertThatThrownBy(() -> especialistaService.buscarPorEspecialidade("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("especialidade");

            verify(especialistaRepository, never()).findByEspecialidade(any());
        }

        @Test
        @DisplayName("Deve buscar especialista por CRM/CRFA")
        void deveBuscarEspecialistaPorCrmFono() {
            // Arrange
            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(1L);
            especialista.setNome("Dr. Silva");
            especialista.setCrmFono("CRM 12345");

            when(especialistaRepository.findByCrmFono("CRM 12345")).thenReturn(Optional.of(especialista));

            // Act
            Optional<EspecialistaDtoOut> resultado = especialistaService.buscarPorCrmFono("CRM 12345");

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getCrmFono()).isEqualTo("CRM 12345");
            verify(especialistaRepository, times(1)).findByCrmFono("CRM 12345");
        }

        @Test
        @DisplayName("Deve buscar especialistas maiores de idade")
        void deveBuscarEspecialistasMaioresDeIdade() {
            // Arrange
            EspecialistaEntity esp1 = new EspecialistaEntity();
            esp1.setId(1L);
            esp1.setNome("Dr. Silva");
            esp1.setIdade(40);

            when(especialistaRepository.findEspecialistasMaioresDeIdade()).thenReturn(Arrays.asList(esp1));

            // Act
            List<EspecialistaDtoOut> resultados = especialistaService.buscarMaioresDeIdade();

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getIdade()).isGreaterThanOrEqualTo(18);
            verify(especialistaRepository, times(1)).findEspecialistasMaioresDeIdade();
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Especialista")
    class AtualizacaoEspecialistaTests {

        @Test
        @DisplayName("Deve atualizar especialista com sucesso")
        void deveAtualizarEspecialistaComSucesso() {
            // Arrange
            EspecialistaEntity especialistaExistente = new EspecialistaEntity();
            especialistaExistente.setId(1L);
            especialistaExistente.setNome("Dr. Antigo");
            especialistaExistente.setEspecialidade("Antigo");

            EspecialistaDtoIn dadosAtualizados = new EspecialistaDtoIn();
            dadosAtualizados.setNome("Dr. Atualizado");
            dadosAtualizados.setCrmFono("CRM 999");
            dadosAtualizados.setEspecialidade("Fonoaudiologia");

            EspecialistaEntity especialistaAtualizado = new EspecialistaEntity();
            especialistaAtualizado.setId(1L);
            especialistaAtualizado.setNome("Dr. Atualizado");
            especialistaAtualizado.setEspecialidade("Fonoaudiologia");

            when(especialistaRepository.findById(1L)).thenReturn(Optional.of(especialistaExistente));
            when(especialistaRepository.save(any(EspecialistaEntity.class))).thenReturn(especialistaAtualizado);

            // Act
            EspecialistaDtoOut resultado = especialistaService.atualizar(1L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Dr. Atualizado");
            assertThat(resultado.getEspecialidade()).isEqualTo("Fonoaudiologia");
            verify(especialistaRepository, times(1)).save(any(EspecialistaEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar especialista inexistente")
        void deveLancarExcecao_QuandoAtualizarEspecialistaInexistente() {
            // Arrange
            EspecialistaDtoIn dadosAtualizados = new EspecialistaDtoIn();
            dadosAtualizados.setNome("Novo Nome");
            dadosAtualizados.setCrmFono("CRM 123");
            dadosAtualizados.setEspecialidade("Fonoaudiologia");

            when(especialistaRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(especialistaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Especialista")
    class DelecaoEspecialistaTests {

        @Test
        @DisplayName("Deve deletar especialista com sucesso quando não está em uso")
        void deveDeletarEspecialistaComSucesso() {
            // Arrange
            when(especialistaRepository.existsById(10L)).thenReturn(true);
            when(consultaRepository.existsByEspecialistaId(10L)).thenReturn(false);
            when(disponibilidadeRepository.existsByEspecialistaId(10L)).thenReturn(false);

            // Act
            especialistaService.deletar(10L);

            // Assert
            verify(especialistaRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar especialista inexistente")
        void deveLancarExcecao_QuandoDeletarEspecialistaInexistente() {
            // Arrange
            when(especialistaRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(especialistaRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar especialista vinculado a consulta")
        void deveLancarExcecao_QuandoDeletarEspecialistaVinculadoAConsulta() {
            // Arrange
            when(especialistaRepository.existsById(5L)).thenReturn(true);
            when(consultaRepository.existsByEspecialistaId(5L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.deletar(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("consultas");

            verify(especialistaRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar especialista vinculado a disponibilidade")
        void deveLancarExcecao_QuandoDeletarEspecialistaVinculadoADisponibilidade() {
            // Arrange
            when(especialistaRepository.existsById(5L)).thenReturn(true);
            when(consultaRepository.existsByEspecialistaId(5L)).thenReturn(false);
            when(disponibilidadeRepository.existsByEspecialistaId(5L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> especialistaService.deletar(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disponibilidades");

            verify(especialistaRepository, never()).deleteById(any());
        }
    }
}
