package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.SecretariaDtoIn;
import com.inatel.prototipo_ia.dto.out.SecretariaDtoOut;
import com.inatel.prototipo_ia.entity.SecretariaEntity;
import com.inatel.prototipo_ia.repository.SecretariaRepository;
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
class SecretariaServiceTest {

    @Mock
    private SecretariaRepository secretariaRepository;

    @InjectMocks
    private SecretariaService secretariaService;

    @Nested
    @DisplayName("Testes de Criação de Secretária")
    class CriacaoSecretariaTests {

        @Test
        @DisplayName("Deve criar secretária com sucesso quando dados válidos")
        void deveCriarSecretariaComSucesso() {
            // Arrange
            SecretariaDtoIn secretariaDto = new SecretariaDtoIn();
            secretariaDto.setNome("Maria Silva");
            secretariaDto.setIdade(30);
            secretariaDto.setEndereco("Rua das Flores, 50");
            secretariaDto.setEmail("maria.silva@email.com");

            SecretariaEntity secretariaSalva = new SecretariaEntity();
            secretariaSalva.setId(1L);
            secretariaSalva.setNome("Maria Silva");
            secretariaSalva.setIdade(30);
            secretariaSalva.setEndereco("Rua das Flores, 50");
            secretariaSalva.setEmail("maria.silva@email.com");

            when(secretariaRepository.existsByEmail("maria.silva@email.com")).thenReturn(false);
            when(secretariaRepository.save(any(SecretariaEntity.class))).thenReturn(secretariaSalva);

            // Act
            SecretariaDtoOut resultado = secretariaService.criar(secretariaDto);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Maria Silva");
            assertThat(resultado.getEmail()).isEqualTo("maria.silva@email.com");

            verify(secretariaRepository, times(1)).save(any(SecretariaEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> secretariaService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(secretariaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome está em branco")
        void deveLancarExcecao_QuandoNomeEstaBranco() {
            // Arrange
            SecretariaDtoIn secretariaDto = new SecretariaDtoIn();
            secretariaDto.setNome("   ");
            secretariaDto.setEmail("email@test.com");

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.criar(secretariaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");

            verify(secretariaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email está em branco")
        void deveLancarExcecao_QuandoEmailEstaBranco() {
            // Arrange
            SecretariaDtoIn secretariaDto = new SecretariaDtoIn();
            secretariaDto.setNome("Maria Silva");
            secretariaDto.setEmail("   ");

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.criar(secretariaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");

            verify(secretariaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email é inválido")
        void deveLancarExcecao_QuandoEmailInvalido() {
            // Arrange
            SecretariaDtoIn secretariaDto = new SecretariaDtoIn();
            secretariaDto.setNome("Maria Silva");
            secretariaDto.setEmail("emailinvalido");

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.criar(secretariaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("válido");

            verify(secretariaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void deveLancarExcecao_QuandoEmailJaExiste() {
            // Arrange
            SecretariaDtoIn secretariaDto = new SecretariaDtoIn();
            secretariaDto.setNome("Maria Silva");
            secretariaDto.setEmail("existente@email.com");

            when(secretariaRepository.existsByEmail("existente@email.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.criar(secretariaDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("existente@email.com");

            verify(secretariaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade é negativa")
        void deveLancarExcecao_QuandoIdadeNegativa() {
            // Arrange
            SecretariaDtoIn secretariaDto = new SecretariaDtoIn();
            secretariaDto.setNome("Maria Silva");
            secretariaDto.setEmail("maria@email.com");
            secretariaDto.setIdade(-5);

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.criar(secretariaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idade");

            verify(secretariaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Secretárias")
    class BuscaSecretariaTests {

        @Test
        @DisplayName("Deve buscar todas as secretárias com sucesso")
        void deveBuscarTodasAsSecretarias() {
            // Arrange
            SecretariaEntity sec1 = new SecretariaEntity();
            sec1.setId(1L);
            sec1.setNome("Maria");
            sec1.setEmail("maria@email.com");

            SecretariaEntity sec2 = new SecretariaEntity();
            sec2.setId(2L);
            sec2.setNome("Ana");
            sec2.setEmail("ana@email.com");

            when(secretariaRepository.findAll()).thenReturn(Arrays.asList(sec1, sec2));

            // Act
            List<SecretariaDtoOut> resultados = secretariaService.buscarTodos();

            // Assert
            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getNome()).isEqualTo("Maria");
            assertThat(resultados.get(1).getNome()).isEqualTo("Ana");
            verify(secretariaRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar secretária por ID com sucesso")
        void deveBuscarSecretariaPorId() {
            // Arrange
            SecretariaEntity secretaria = new SecretariaEntity();
            secretaria.setId(10L);
            secretaria.setNome("Maria");
            secretaria.setEmail("maria@email.com");

            when(secretariaRepository.findById(10L)).thenReturn(Optional.of(secretaria));

            // Act
            Optional<SecretariaDtoOut> resultado = secretariaService.buscarPorId(10L);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getNome()).isEqualTo("Maria");
            verify(secretariaRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando secretária não existe")
        void deveRetornarVazio_QuandoSecretariaNaoExiste() {
            // Arrange
            when(secretariaRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<SecretariaDtoOut> resultado = secretariaService.buscarPorId(999L);

            // Assert
            assertThat(resultado).isEmpty();
            verify(secretariaRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar secretária por email")
        void deveBuscarSecretariaPorEmail() {
            // Arrange
            SecretariaEntity secretaria = new SecretariaEntity();
            secretaria.setId(1L);
            secretaria.setNome("Maria");
            secretaria.setEmail("maria@email.com");

            when(secretariaRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(secretaria));

            // Act
            Optional<SecretariaDtoOut> resultado = secretariaService.buscarPorEmail("maria@email.com");

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getEmail()).isEqualTo("maria@email.com");
            verify(secretariaRepository, times(1)).findByEmail("maria@email.com");
        }

        @Test
        @DisplayName("Deve buscar secretárias por nome")
        void deveBuscarSecretariasPorNome() {
            // Arrange
            SecretariaEntity sec1 = new SecretariaEntity();
            sec1.setId(1L);
            sec1.setNome("Maria Silva");
            sec1.setEmail("maria@email.com");

            when(secretariaRepository.findByNomeContainingIgnoreCase("Maria"))
                .thenReturn(Arrays.asList(sec1));

            // Act
            List<SecretariaDtoOut> resultados = secretariaService.buscarPorNome("Maria");

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getNome()).contains("Maria");
            verify(secretariaRepository, times(1)).findByNomeContainingIgnoreCase("Maria");
        }

        @Test
        @DisplayName("Deve buscar secretárias maiores de idade")
        void deveBuscarSecretariasMaioresDeIdade() {
            // Arrange
            SecretariaEntity sec1 = new SecretariaEntity();
            sec1.setId(1L);
            sec1.setNome("Maria");
            sec1.setIdade(25);

            when(secretariaRepository.findSecretariasMaioresDeIdade()).thenReturn(Arrays.asList(sec1));

            // Act
            List<SecretariaDtoOut> resultados = secretariaService.buscarMaioresDeIdade();

            // Assert
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getIdade()).isGreaterThanOrEqualTo(18);
            verify(secretariaRepository, times(1)).findSecretariasMaioresDeIdade();
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Secretária")
    class AtualizacaoSecretariaTests {

        @Test
        @DisplayName("Deve atualizar secretária com sucesso")
        void deveAtualizarSecretariaComSucesso() {
            // Arrange
            SecretariaEntity secretariaExistente = new SecretariaEntity();
            secretariaExistente.setId(1L);
            secretariaExistente.setNome("Maria Antiga");
            secretariaExistente.setEmail("antiga@email.com");

            SecretariaDtoIn dadosAtualizados = new SecretariaDtoIn();
            dadosAtualizados.setNome("Maria Nova");
            dadosAtualizados.setEmail("nova@email.com");
            dadosAtualizados.setIdade(32);

            SecretariaEntity secretariaAtualizada = new SecretariaEntity();
            secretariaAtualizada.setId(1L);
            secretariaAtualizada.setNome("Maria Nova");
            secretariaAtualizada.setEmail("nova@email.com");
            secretariaAtualizada.setIdade(32);

            when(secretariaRepository.findById(1L)).thenReturn(Optional.of(secretariaExistente));
            when(secretariaRepository.existsByEmail("nova@email.com")).thenReturn(false);
            when(secretariaRepository.save(any(SecretariaEntity.class))).thenReturn(secretariaAtualizada);

            // Act
            SecretariaDtoOut resultado = secretariaService.atualizar(1L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Maria Nova");
            assertThat(resultado.getEmail()).isEqualTo("nova@email.com");
            verify(secretariaRepository, times(1)).save(any(SecretariaEntity.class));
        }

        @Test
        @DisplayName("Deve atualizar secretária mantendo mesmo email")
        void deveAtualizarSecretariaManendoMesmoEmail() {
            // Arrange
            SecretariaEntity secretariaExistente = new SecretariaEntity();
            secretariaExistente.setId(1L);
            secretariaExistente.setNome("Maria Antiga");
            secretariaExistente.setEmail("mesmo@email.com");

            SecretariaDtoIn dadosAtualizados = new SecretariaDtoIn();
            dadosAtualizados.setNome("Maria Nova");
            dadosAtualizados.setEmail("mesmo@email.com");
            dadosAtualizados.setIdade(32);

            SecretariaEntity secretariaAtualizada = new SecretariaEntity();
            secretariaAtualizada.setId(1L);
            secretariaAtualizada.setNome("Maria Nova");
            secretariaAtualizada.setEmail("mesmo@email.com");

            when(secretariaRepository.findById(1L)).thenReturn(Optional.of(secretariaExistente));
            when(secretariaRepository.save(any(SecretariaEntity.class))).thenReturn(secretariaAtualizada);

            // Act
            SecretariaDtoOut resultado = secretariaService.atualizar(1L, dadosAtualizados);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Maria Nova");
            verify(secretariaRepository, times(1)).save(any(SecretariaEntity.class));
            verify(secretariaRepository, never()).existsByEmail(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar secretária inexistente")
        void deveLancarExcecao_QuandoAtualizarSecretariaInexistente() {
            // Arrange
            SecretariaDtoIn dadosAtualizados = new SecretariaDtoIn();
            dadosAtualizados.setNome("Novo Nome");
            dadosAtualizados.setEmail("novo@email.com");

            when(secretariaRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(secretariaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando novo email já existe")
        void deveLancarExcecao_QuandoNovoEmailJaExiste() {
            // Arrange
            SecretariaEntity secretariaExistente = new SecretariaEntity();
            secretariaExistente.setId(1L);
            secretariaExistente.setNome("Maria");
            secretariaExistente.setEmail("atual@email.com");

            SecretariaDtoIn dadosAtualizados = new SecretariaDtoIn();
            dadosAtualizados.setNome("Maria");
            dadosAtualizados.setEmail("existente@email.com");

            when(secretariaRepository.findById(1L)).thenReturn(Optional.of(secretariaExistente));
            when(secretariaRepository.existsByEmail("existente@email.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.atualizar(1L, dadosAtualizados))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("existente@email.com");

            verify(secretariaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Secretária")
    class DelecaoSecretariaTests {

        @Test
        @DisplayName("Deve deletar secretária com sucesso")
        void deveDeletarSecretariaComSucesso() {
            // Arrange
            when(secretariaRepository.existsById(10L)).thenReturn(true);

            // Act
            secretariaService.deletar(10L);

            // Assert
            verify(secretariaRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar secretária inexistente")
        void deveLancarExcecao_QuandoDeletarSecretariaInexistente() {
            // Arrange
            when(secretariaRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> secretariaService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(secretariaRepository, never()).deleteById(any());
        }
    }
}
