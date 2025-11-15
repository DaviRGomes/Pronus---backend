package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ProfissionalDtoIn;
import com.inatel.prototipo_ia.dto.out.ProfissionalDtoOut;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
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
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private TratamentoRepository tratamentoRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    @Nested
    @DisplayName("Testes de Criação de Profissional")
    class CriacaoProfissionalTests {

        @Test
        @DisplayName("Deve criar profissional com sucesso quando dados válidos")
        void deveCriarProfissionalComSucesso() {
            // Arrange
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("Dr. Carlos Silva");
            profissionalDto.setIdade(35);
            profissionalDto.setEndereco("Av. Paulista, 1000");
            profissionalDto.setCertificados("CRFa 12345");
            profissionalDto.setExperiencia(10);

            ProfissionalEntity profissionalSalvo = new ProfissionalEntity();
            profissionalSalvo.setId(1L);
            profissionalSalvo.setNome("Dr. Carlos Silva");
            profissionalSalvo.setIdade(35);
            profissionalSalvo.setEndereco("Av. Paulista, 1000");
            profissionalSalvo.setCertificados("CRFa 12345");
            profissionalSalvo.setExperiencia(10);

            when(profissionalRepository.save(any(ProfissionalEntity.class))).thenReturn(profissionalSalvo);

            // Act
            ProfissionalDtoOut resultado = profissionalService.criar(profissionalDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Dr. Carlos Silva");
            assertThat(resultado.getCertificados()).isEqualTo("CRFa 12345");
            assertThat(resultado.getExperiencia()).isEqualTo(10);

            verify(profissionalRepository, times(1)).save(any(ProfissionalEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome está em branco")
        void deveLancarExcecao_QuandoNomeEstaBranco() {
            // Arrange
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("   ");
            profissionalDto.setCertificados("CRFa 123");

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.criar(profissionalDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");

            verify(profissionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando certificados estão em branco")
        void deveLancarExcecao_QuandoCertificadosEstaBranco() {
            // Arrange
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("Dr. Silva");
            profissionalDto.setCertificados("   ");

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.criar(profissionalDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("certificados");

            verify(profissionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando experiência é negativa")
        void deveLancarExcecao_QuandoExperienciaNegativa() {
            // Arrange
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("Dr. Silva");
            profissionalDto.setCertificados("CRFa 123");
            profissionalDto.setExperiencia(-5);

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.criar(profissionalDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("experiência");

            verify(profissionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade é negativa")
        void deveLancarExcecao_QuandoIdadeNegativa() {
            // Arrange
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("Dr. Silva");
            profissionalDto.setCertificados("CRFa 123");
            profissionalDto.setIdade(-10);

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.criar(profissionalDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idade");

            verify(profissionalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Profissionais")
    class BuscaProfissionalTests {

        @Test
        @DisplayName("Deve buscar todos os profissionais com sucesso")
        void deveBuscarTodosOsProfissionais() {
            // Arrange
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Silva");
            prof1.setExperiencia(10);

            ProfissionalEntity prof2 = new ProfissionalEntity();
            prof2.setId(2L);
            prof2.setNome("Dra. Santos");
            prof2.setExperiencia(5);

            when(profissionalRepository.findAll()).thenReturn(Arrays.asList(prof1, prof2));

            // Act
            List<ProfissionalDtoOut> resultados = profissionalService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getNome()).isEqualTo("Dr. Silva");
            assertThat(resultados.get(1).getNome()).isEqualTo("Dra. Santos");
            verify(profissionalRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar profissional por ID com sucesso")
        void deveBuscarProfissionalPorId() {
            // Arrange
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(10L);
            profissional.setNome("Dr. João");
            profissional.setExperiencia(8);

            when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));

            // Act
            Optional<ProfissionalDtoOut> resultado = profissionalService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getNome()).isEqualTo("Dr. João");
            verify(profissionalRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando profissional não existe")
        void deveRetornarVazio_QuandoProfissionalNaoExiste() {
            // Arrange
            when(profissionalRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<ProfissionalDtoOut> resultado = profissionalService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(profissionalRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Profissional")
    class AtualizacaoProfissionalTests {

        @Test
        @DisplayName("Deve atualizar profissional com sucesso")
        void deveAtualizarProfissionalComSucesso() {
            // Arrange
            ProfissionalEntity profissionalExistente = new ProfissionalEntity();
            profissionalExistente.setId(1L);
            profissionalExistente.setNome("Dr. Antigo");
            profissionalExistente.setExperiencia(5);

            ProfissionalDtoIn dadosAtualizados = new ProfissionalDtoIn();
            dadosAtualizados.setNome("Dr. Atualizado");
            dadosAtualizados.setCertificados("CRFa 999");
            dadosAtualizados.setExperiencia(10);

            ProfissionalEntity profissionalAtualizado = new ProfissionalEntity();
            profissionalAtualizado.setId(1L);
            profissionalAtualizado.setNome("Dr. Atualizado");
            profissionalAtualizado.setCertificados("CRFa 999");
            profissionalAtualizado.setExperiencia(10);

            when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissionalExistente));
            when(profissionalRepository.save(any(ProfissionalEntity.class))).thenReturn(profissionalAtualizado);

            // Act
            ProfissionalDtoOut resultado = profissionalService.atualizar(1L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Dr. Atualizado");
            assertThat(resultado.getExperiencia()).isEqualTo(10);
            verify(profissionalRepository, times(1)).save(any(ProfissionalEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar profissional inexistente")
        void deveLancarExcecao_QuandoAtualizarProfissionalInexistente() {
            // Arrange
            ProfissionalDtoIn dadosAtualizados = new ProfissionalDtoIn();
            dadosAtualizados.setNome("Novo Nome");
            dadosAtualizados.setCertificados("CRFa 123");

            when(profissionalRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(profissionalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Profissional")
    class DelecaoProfissionalTests {

        @Test
        @DisplayName("Deve lançar exceção ao tentar deletar profissional vinculado a um chat")
        void deveLancarExcecaoAoTentarDeletarProfissionalEmUsoEmChat() {
            // Arrange
            Long profissionalId = 1L;
            when(profissionalRepository.existsById(profissionalId)).thenReturn(true);
            when(chatRepository.existsByProfissionalId(profissionalId)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.deletar(profissionalId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("chat");

            verify(profissionalRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar deletar profissional vinculado a um tratamento")
        void deveLancarExcecaoAoTentarDeletarProfissionalEmUsoEmTratamento() {
            // Arrange
            Long profissionalId = 2L;
            when(profissionalRepository.existsById(profissionalId)).thenReturn(true);
            when(chatRepository.existsByProfissionalId(profissionalId)).thenReturn(false);
            when(tratamentoRepository.existsByProfissionalId(profissionalId)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.deletar(profissionalId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tratamento");

            verify(profissionalRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve deletar o profissional com sucesso quando ele não estiver em uso")
        void deveDeletarProfissionalComSucesso() {
            // Arrange
            Long profissionalId = 3L;
            when(profissionalRepository.existsById(profissionalId)).thenReturn(true);
            when(chatRepository.existsByProfissionalId(profissionalId)).thenReturn(false);
            when(tratamentoRepository.existsByProfissionalId(profissionalId)).thenReturn(false);

            // Act
            profissionalService.deletar(profissionalId);

            // Assert
            verify(profissionalRepository, times(1)).deleteById(profissionalId);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar profissional inexistente")
        void deveLancarExcecao_QuandoDeletarProfissionalInexistente() {
            // Arrange
            when(profissionalRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> profissionalService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(profissionalRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Testes de Queries Customizadas")
    class QueriesCustomizadasTests {

        @Test
        @DisplayName("Deve buscar profissionais experientes (>= 5 anos)")
        void deveBuscarProfissionaisExperientes() {
            // Arrange
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Experiente 1");
            prof1.setExperiencia(10);

            ProfissionalEntity prof2 = new ProfissionalEntity();
            prof2.setId(2L);
            prof2.setNome("Dr. Experiente 2");
            prof2.setExperiencia(15);

            when(profissionalRepository.findProfissionaisExperientes()).thenReturn(Arrays.asList(prof1, prof2));

            // Act
            List<ProfissionalDtoOut> resultados = profissionalService.buscarExperientes();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getExperiencia()).isGreaterThanOrEqualTo(5);
            assertThat(resultados.get(1).getExperiencia()).isGreaterThanOrEqualTo(5);
            verify(profissionalRepository, times(1)).findProfissionaisExperientes();
        }

        @Test
        @DisplayName("Deve buscar profissionais com experiência maior que X anos")
        void deveBuscarProfissionaisComExperienciaMaiorQue() {
            // Arrange
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Senior");
            prof1.setExperiencia(12);

            when(profissionalRepository.findByExperienciaGreaterThan(8)).thenReturn(Arrays.asList(prof1));

            // Act
            List<ProfissionalDtoOut> resultados = profissionalService.buscarComExperienciaMaiorQue(8);

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getExperiencia()).isGreaterThan(8);
            verify(profissionalRepository, times(1)).findByExperienciaGreaterThan(8);
        }

        @Test
        @DisplayName("Deve lançar exceção quando anos de experiência é negativo")
        void deveLancarExcecao_QuandoAnosExperienciaNegativo() {
            // Act & Assert
            assertThatThrownBy(() -> profissionalService.buscarComExperienciaMaiorQue(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("experiência");

            verify(profissionalRepository, never()).findByExperienciaGreaterThan(any());
        }

        @Test
        @DisplayName("Deve buscar profissionais qualificados")
        void deveBuscarProfissionaisQualificados() {
            // Arrange
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Qualificado");
            prof1.setExperiencia(10);
            prof1.setIdade(35);

            when(profissionalRepository.findByExperienciaAndIdadeMinima(5, 25))
                .thenReturn(Arrays.asList(prof1));

            // Act
            List<ProfissionalDtoOut> resultados = profissionalService.buscarQualificados(5, 25);

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getExperiencia()).isGreaterThanOrEqualTo(5);
            assertThat(resultados.get(0).getIdade()).isGreaterThanOrEqualTo(25);
            verify(profissionalRepository, times(1)).findByExperienciaAndIdadeMinima(5, 25);
        }

        @Test
        @DisplayName("Deve lançar exceção quando experiência mínima é negativa")
        void deveLancarExcecao_QuandoExperienciaMinimaInvalida() {
            // Act & Assert
            assertThatThrownBy(() -> profissionalService.buscarQualificados(-1, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("experiência");

            verify(profissionalRepository, never()).findByExperienciaAndIdadeMinima(any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade mínima é negativa")
        void deveLancarExcecao_QuandoIdadeMinimaInvalida() {
            // Act & Assert
            assertThatThrownBy(() -> profissionalService.buscarQualificados(5, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idade");

            verify(profissionalRepository, never()).findByExperienciaAndIdadeMinima(any(), any());
        }
    }
}