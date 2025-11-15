package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ClienteDtoIn;
import com.inatel.prototipo_ia.dto.out.ClienteDtoOut;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Nested
    @DisplayName("Testes de Criação de Cliente")
    class CriacaoClienteTests {

        @Test
        @DisplayName("Deve criar cliente com sucesso quando dados válidos")
        void deveCriarClienteComSucesso() {
            ClienteDtoIn clienteDto = new ClienteDtoIn();
            clienteDto.setNome("João Silva");
            clienteDto.setIdade(25);
            clienteDto.setEndereco("Rua das Flores, 123");
            clienteDto.setNivel("Intermediário");

            ClienteEntity clienteSalvo = new ClienteEntity();
            clienteSalvo.setId(1L);
            clienteSalvo.setNome("João Silva");
            clienteSalvo.setIdade(25);
            clienteSalvo.setEndereco("Rua das Flores, 123");
            clienteSalvo.setNivel("Intermediário");

            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteSalvo);

            ClienteDtoOut resultado = clienteService.salvar(clienteDto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("João Silva");
            assertThat(resultado.getIdade()).isEqualTo(25);
            assertThat(resultado.getEndereco()).isEqualTo("Rua das Flores, 123");
            assertThat(resultado.getNivel()).isEqualTo("Intermediário");

            verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome está em branco")
        void deveLancarExcecao_QuandoNomeEstaBranco() {
            ClienteDtoIn clienteDto = new ClienteDtoIn();
            clienteDto.setNome("   ");
            clienteDto.setIdade(25);

            // Act & Assert
            assertThatThrownBy(() -> clienteService.salvar(clienteDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");

            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> clienteService.salvar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(clienteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Clientes")
    class BuscaClienteTests {

        @Test
        @DisplayName("Deve buscar todos os clientes com sucesso")
        void deveBuscarTodosOsClientes() {
            ClienteEntity cliente1 = new ClienteEntity();
            cliente1.setId(1L);
            cliente1.setNome("Cliente 1");
            cliente1.setIdade(20);
            cliente1.setNivel("Básico");

            ClienteEntity cliente2 = new ClienteEntity();
            cliente2.setId(2L);
            cliente2.setNome("Cliente 2");
            cliente2.setIdade(30);
            cliente2.setNivel("Avançado");

            when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente1, cliente2));

            List<ClienteDtoOut> resultados = clienteService.buscarTodos();

            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getNome()).isEqualTo("Cliente 1");
            assertThat(resultados.get(1).getNome()).isEqualTo("Cliente 2");
            verify(clienteRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar cliente por ID com sucesso")
        void deveBuscarClientePorId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(10L);
            cliente.setNome("Maria Santos");
            cliente.setIdade(28);
            cliente.setNivel("Avançado");

            when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));

            Optional<ClienteDtoOut> resultado = clienteService.buscarPorId(10L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getNome()).isEqualTo("Maria Santos");
            verify(clienteRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando cliente não existe")
        void deveRetornarVazio_QuandoClienteNaoExiste() {
            when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<ClienteDtoOut> resultado = clienteService.buscarPorId(999L);

            assertThat(resultado).isEmpty();
            verify(clienteRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Cliente")
    class AtualizacaoClienteTests {

        @Test
        @DisplayName("Deve atualizar os dados de um cliente com sucesso")
        void deveAtualizarClienteComSucesso() {
            Long clienteId = 1L;

            ClienteEntity clienteExistente = new ClienteEntity();
            clienteExistente.setId(clienteId);
            clienteExistente.setNome("Nome Antigo");
            clienteExistente.setIdade(20);
            clienteExistente.setNivel("Iniciante");

            ClienteDtoIn dadosAtualizados = new ClienteDtoIn();
            dadosAtualizados.setNome("Nome Novo e Atualizado");
            dadosAtualizados.setIdade(21);
            dadosAtualizados.setNivel("Intermediário");

            ClienteEntity clienteAtualizado = new ClienteEntity();
            clienteAtualizado.setId(clienteId);
            clienteAtualizado.setNome("Nome Novo e Atualizado");
            clienteAtualizado.setIdade(21);
            clienteAtualizado.setNivel("Intermediário");

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteExistente));
            when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteAtualizado);

            ClienteDtoOut resultado = clienteService.atualizar(clienteId, dadosAtualizados);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(clienteId);
            assertThat(resultado.getNome()).isEqualTo("Nome Novo e Atualizado");
            assertThat(resultado.getNivel()).isEqualTo("Intermediário");
            assertThat(resultado.getIdade()).isEqualTo(21);

            verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar cliente inexistente")
        void deveLancarExcecao_QuandoAtualizarClienteInexistente() {
            ClienteDtoIn dadosAtualizados = new ClienteDtoIn();
            dadosAtualizados.setNome("Novo Nome");

            when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> clienteService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(clienteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Cliente")
    class DelecaoClienteTests {

        @Test
        @DisplayName("Deve deletar cliente com sucesso quando não vinculado a chat")
        void deveDeletarClienteComSucesso() {
            when(clienteRepository.existsById(10L)).thenReturn(true);
            when(chatRepository.existsByClienteId(10L)).thenReturn(false);

            clienteService.deletar(10L);

            verify(clienteRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar cliente inexistente")
        void deveLancarExcecao_QuandoDeletarClienteInexistente() {
            when(clienteRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> clienteService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(clienteRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar cliente vinculado a chat")
        void deveLancarExcecao_QuandoDeletarClienteVinculadoAChat() {
            when(clienteRepository.existsById(5L)).thenReturn(true);
            when(chatRepository.existsByClienteId(5L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> clienteService.deletar(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("chat");

            verify(clienteRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Testes de Queries Customizadas")
    class QueriesCustomizadasTests {

        @Test
        @DisplayName("Deve buscar clientes maiores de idade")
        void deveBuscarClientesMaioresDeIdade() {
            ClienteEntity cliente1 = new ClienteEntity();
            cliente1.setId(1L);
            cliente1.setNome("João Adulto");
            cliente1.setIdade(25);

            ClienteEntity cliente2 = new ClienteEntity();
            cliente2.setId(2L);
            cliente2.setNome("Maria Adulta");
            cliente2.setIdade(30);

            when(clienteRepository.findClientesMaioresDeIdade()).thenReturn(Arrays.asList(cliente1, cliente2));

            List<ClienteDtoOut> resultados = clienteService.buscarMaioresDeIdade();

            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getIdade()).isGreaterThanOrEqualTo(18);
            assertThat(resultados.get(1).getIdade()).isGreaterThanOrEqualTo(18);
            verify(clienteRepository, times(1)).findClientesMaioresDeIdade();
        }

        @Test
        @DisplayName("Deve buscar clientes por nível com sucesso")
        void deveBuscarClientesPorNivel() {
            ClienteEntity cliente1 = new ClienteEntity();
            cliente1.setId(1L);
            cliente1.setNome("Cliente Avançado 1");
            cliente1.setNivel("Avançado");

            ClienteEntity cliente2 = new ClienteEntity();
            cliente2.setId(2L);
            cliente2.setNome("Cliente Avançado 2");
            cliente2.setNivel("Avançado");

            when(clienteRepository.findByNivel("Avançado")).thenReturn(Arrays.asList(cliente1, cliente2));

            List<ClienteDtoOut> resultados = clienteService.buscarPorNivel("Avançado");

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getNivel().equals("Avançado"));
            verify(clienteRepository, times(1)).findByNivel("Avançado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando nível está em branco")
        void deveLancarExcecao_QuandoNivelEstaBranco() {
            // Act & Assert
            assertThatThrownBy(() -> clienteService.buscarPorNivel("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nível");

            verify(clienteRepository, never()).findByNivel(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nível é nulo")
        void deveLancarExcecao_QuandoNivelNulo() {
            // Act & Assert
            assertThatThrownBy(() -> clienteService.buscarPorNivel(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nível");

            verify(clienteRepository, never()).findByNivel(any());
        }
    }
}
