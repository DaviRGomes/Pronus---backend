package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ProfissionalDtoIn;
import com.inatel.prototipo_ia.dto.out.ProfissionalDtoOut;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
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

/**
 * Testes Unitários - ProfissionalService
 * Valida CRUD e lógica de negócio dos profissionais fonoaudiólogos
 */
@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;


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

            ProfissionalDtoOut resultado = profissionalService.criar(profissionalDto);

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
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("   ");
            profissionalDto.setCertificados("CRFa 123");

            assertThatThrownBy(() -> profissionalService.criar(profissionalDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");

            verify(profissionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando certificados estão em branco")
        void deveLancarExcecao_QuandoCertificadosEstaBranco() {
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("Dr. Silva");
            profissionalDto.setCertificados("   ");

            assertThatThrownBy(() -> profissionalService.criar(profissionalDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("certificados");

            verify(profissionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando experiência é negativa")
        void deveLancarExcecao_QuandoExperienciaNegativa() {
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("Dr. Silva");
            profissionalDto.setCertificados("CRFa 123");
            profissionalDto.setExperiencia(-5);

            assertThatThrownBy(() -> profissionalService.criar(profissionalDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("experiência");

            verify(profissionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade é negativa")
        void deveLancarExcecao_QuandoIdadeNegativa() {
            ProfissionalDtoIn profissionalDto = new ProfissionalDtoIn();
            profissionalDto.setNome("Dr. Silva");
            profissionalDto.setCertificados("CRFa 123");
            profissionalDto.setIdade(-10);

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
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Silva");
            prof1.setExperiencia(10);

            ProfissionalEntity prof2 = new ProfissionalEntity();
            prof2.setId(2L);
            prof2.setNome("Dra. Santos");
            prof2.setExperiencia(5);

            when(profissionalRepository.findAll()).thenReturn(Arrays.asList(prof1, prof2));

            List<ProfissionalDtoOut> resultados = profissionalService.buscarTodos();

            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getNome()).isEqualTo("Dr. Silva");
            assertThat(resultados.get(1).getNome()).isEqualTo("Dra. Santos");
            verify(profissionalRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar profissional por ID com sucesso")
        void deveBuscarProfissionalPorId() {
            ProfissionalEntity profissional = new ProfissionalEntity();
            profissional.setId(10L);
            profissional.setNome("Dr. João");
            profissional.setExperiencia(8);

            when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));

            Optional<ProfissionalDtoOut> resultado = profissionalService.buscarPorId(10L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getNome()).isEqualTo("Dr. João");
            verify(profissionalRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando profissional não existe")
        void deveRetornarVazio_QuandoProfissionalNaoExiste() {
            when(profissionalRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<ProfissionalDtoOut> resultado = profissionalService.buscarPorId(999L);

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

            ProfissionalDtoOut resultado = profissionalService.atualizar(1L, dadosAtualizados);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Dr. Atualizado");
            assertThat(resultado.getExperiencia()).isEqualTo(10);
            verify(profissionalRepository, times(1)).save(any(ProfissionalEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar profissional inexistente")
        void deveLancarExcecao_QuandoAtualizarProfissionalInexistente() {
            ProfissionalDtoIn dadosAtualizados = new ProfissionalDtoIn();
            dadosAtualizados.setNome("Novo Nome");
            dadosAtualizados.setCertificados("CRFa 123");

            when(profissionalRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profissionalService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(profissionalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Profissional")
    class DelecaoProfissionalTests {

        // Removido teste de vínculo com chat: regra migrou para Especialista

        @Test
        @DisplayName("Deve lançar exceção ao tentar deletar profissional vinculado a um tratamento")
        void deveLancarExcecaoAoTentarDeletarProfissionalEmUsoEmTratamento() {
            Long profissionalId = 2L;
            when(profissionalRepository.existsById(profissionalId)).thenReturn(true);
            when(tratamentoRepository.existsByProfissionalId(profissionalId)).thenReturn(true);

            assertThatThrownBy(() -> profissionalService.deletar(profissionalId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tratamento");

            verify(profissionalRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve deletar o profissional com sucesso quando ele não estiver em uso")
        void deveDeletarProfissionalComSucesso() {
            Long profissionalId = 3L;
            when(profissionalRepository.existsById(profissionalId)).thenReturn(true);
            when(tratamentoRepository.existsByProfissionalId(profissionalId)).thenReturn(false);

            profissionalService.deletar(profissionalId);

            verify(profissionalRepository, times(1)).deleteById(profissionalId);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar profissional inexistente")
        void deveLancarExcecao_QuandoDeletarProfissionalInexistente() {
            when(profissionalRepository.existsById(999L)).thenReturn(false);

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
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Experiente 1");
            prof1.setExperiencia(10);

            ProfissionalEntity prof2 = new ProfissionalEntity();
            prof2.setId(2L);
            prof2.setNome("Dr. Experiente 2");
            prof2.setExperiencia(15);

            when(profissionalRepository.findProfissionaisExperientes()).thenReturn(Arrays.asList(prof1, prof2));

            List<ProfissionalDtoOut> resultados = profissionalService.buscarExperientes();

            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getExperiencia()).isGreaterThanOrEqualTo(5);
            assertThat(resultados.get(1).getExperiencia()).isGreaterThanOrEqualTo(5);
            verify(profissionalRepository, times(1)).findProfissionaisExperientes();
        }

        @Test
        @DisplayName("Deve buscar profissionais com experiência maior que X anos")
        void deveBuscarProfissionaisComExperienciaMaiorQue() {
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Senior");
            prof1.setExperiencia(12);

            when(profissionalRepository.findByExperienciaGreaterThan(8)).thenReturn(Arrays.asList(prof1));

            List<ProfissionalDtoOut> resultados = profissionalService.buscarComExperienciaMaiorQue(8);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getExperiencia()).isGreaterThan(8);
            verify(profissionalRepository, times(1)).findByExperienciaGreaterThan(8);
        }

        @Test
        @DisplayName("Deve lançar exceção quando anos de experiência é negativo")
        void deveLancarExcecao_QuandoAnosExperienciaNegativo() {
            assertThatThrownBy(() -> profissionalService.buscarComExperienciaMaiorQue(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("experiência");

            verify(profissionalRepository, never()).findByExperienciaGreaterThan(any());
        }

        @Test
        @DisplayName("Deve buscar profissionais qualificados")
        void deveBuscarProfissionaisQualificados() {
            ProfissionalEntity prof1 = new ProfissionalEntity();
            prof1.setId(1L);
            prof1.setNome("Dr. Qualificado");
            prof1.setExperiencia(10);
            prof1.setIdade(35);

            when(profissionalRepository.findByExperienciaAndIdadeMinima(5, 25))
                .thenReturn(Arrays.asList(prof1));

            List<ProfissionalDtoOut> resultados = profissionalService.buscarQualificados(5, 25);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getExperiencia()).isGreaterThanOrEqualTo(5);
            assertThat(resultados.get(0).getIdade()).isGreaterThanOrEqualTo(25);
            verify(profissionalRepository, times(1)).findByExperienciaAndIdadeMinima(5, 25);
        }

        @Test
        @DisplayName("Deve lançar exceção quando experiência mínima é negativa")
        void deveLancarExcecao_QuandoExperienciaMinimaInvalida() {
            assertThatThrownBy(() -> profissionalService.buscarQualificados(-1, 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("experiência");

            verify(profissionalRepository, never()).findByExperienciaAndIdadeMinima(any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade mínima é negativa")
        void deveLancarExcecao_QuandoIdadeMinimaInvalida() {
            assertThatThrownBy(() -> profissionalService.buscarQualificados(5, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idade");

            verify(profissionalRepository, never()).findByExperienciaAndIdadeMinima(any(), any());
        }
    }
}
