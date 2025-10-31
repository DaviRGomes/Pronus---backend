package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.UsuarioDtoIn;
import com.inatel.prototipo_ia.dto.out.UsuarioDtoOut;
import com.inatel.prototipo_ia.entity.UsuarioEntity;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.repository.UsuarioRepository;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Nested
    @DisplayName("Testes de Criação de Usuário")
    class CriacaoUsuarioTests {

        @Test
        @DisplayName("Deve criar usuário com sucesso quando dados válidos")
        void deveCriarUsuarioComSucesso() {
            UsuarioDtoIn usuarioDto = new UsuarioDtoIn();
            usuarioDto.setNome("João Silva");
            usuarioDto.setIdade(30);
            usuarioDto.setEndereco("Rua A, 123");

            UsuarioEntity usuarioSalvo = new UsuarioEntity();
            usuarioSalvo.setId(1L);
            usuarioSalvo.setNome("João Silva");
            usuarioSalvo.setIdade(30);
            usuarioSalvo.setEndereco("Rua A, 123");

            when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioSalvo);

            UsuarioDtoOut resultado = usuarioService.criar(usuarioDto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("João Silva");
            assertThat(resultado.getIdade()).isEqualTo(30);

            verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {

            assertThatThrownBy(() -> usuarioService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome está em branco")
        void deveLancarExcecao_QuandoNomeEstaBranco() {
            UsuarioDtoIn usuarioDto = new UsuarioDtoIn();
            usuarioDto.setNome("   ");

            assertThatThrownBy(() -> usuarioService.criar(usuarioDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade é negativa")
        void deveLancarExcecao_QuandoIdadeNegativa() {
            UsuarioDtoIn usuarioDto = new UsuarioDtoIn();
            usuarioDto.setNome("João Silva");
            usuarioDto.setIdade(-5);


            assertThatThrownBy(() -> usuarioService.criar(usuarioDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idade");

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Usuários")
    class BuscaUsuarioTests {

        @Test
        @DisplayName("Deve buscar todos os usuários com sucesso")
        void deveBuscarTodosOsUsuarios() {
            UsuarioEntity user1 = new UsuarioEntity();
            user1.setId(1L);
            user1.setNome("João");

            UsuarioEntity user2 = new UsuarioEntity();
            user2.setId(2L);
            user2.setNome("Maria");

            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(user1, user2));


            List<UsuarioDtoOut> resultados = usuarioService.buscarTodos();


            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getNome()).isEqualTo("João");
            assertThat(resultados.get(1).getNome()).isEqualTo("Maria");
            verify(usuarioRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar usuário por ID com sucesso")
        void deveBuscarUsuarioPorId() {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setId(10L);
            usuario.setNome("João");

            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));


            Optional<UsuarioDtoOut> resultado = usuarioService.buscarPorId(10L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getNome()).isEqualTo("João");
            verify(usuarioRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando usuário não existe")
        void deveRetornarVazio_QuandoUsuarioNaoExiste() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<UsuarioDtoOut> resultado = usuarioService.buscarPorId(999L);

            assertThat(resultado).isEmpty();
            verify(usuarioRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar usuários por nome")
        void deveBuscarUsuariosPorNome() {
            // Arrange
            UsuarioEntity user1 = new UsuarioEntity();
            user1.setId(1L);
            user1.setNome("João Silva");

            when(usuarioRepository.findByNomeContainingIgnoreCase("João"))
                .thenReturn(Arrays.asList(user1));

            List<UsuarioDtoOut> resultados = usuarioService.buscarPorNome("João");

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getNome()).contains("João");
            verify(usuarioRepository, times(1)).findByNomeContainingIgnoreCase("João");
        }

        @Test
        @DisplayName("Deve buscar usuários por idade")
        void deveBuscarUsuariosPorIdade() {
            // Arrange
            UsuarioEntity user1 = new UsuarioEntity();
            user1.setId(1L);
            user1.setNome("João");
            user1.setIdade(30);

            when(usuarioRepository.findByIdade(30)).thenReturn(Arrays.asList(user1));

            List<UsuarioDtoOut> resultados = usuarioService.buscarPorIdade(30);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getIdade()).isEqualTo(30);
            verify(usuarioRepository, times(1)).findByIdade(30);
        }

        @Test
        @DisplayName("Deve lançar exceção quando idade é negativa na busca")
        void deveLancarExcecao_QuandoIdadeNegativaNaBusca() {
            assertThatThrownBy(() -> usuarioService.buscarPorIdade(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idade");

            verify(usuarioRepository, never()).findByIdade(any());
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Usuário")
    class AtualizacaoUsuarioTests {

        @Test
        @DisplayName("Deve atualizar usuário com sucesso")
        void deveAtualizarUsuarioComSucesso() {
            // Arrange
            UsuarioEntity usuarioExistente = new UsuarioEntity();
            usuarioExistente.setId(1L);
            usuarioExistente.setNome("Nome Antigo");
            usuarioExistente.setIdade(25);

            UsuarioDtoIn dadosAtualizados = new UsuarioDtoIn();
            dadosAtualizados.setNome("Nome Novo");
            dadosAtualizados.setIdade(30);

            UsuarioEntity usuarioAtualizado = new UsuarioEntity();
            usuarioAtualizado.setId(1L);
            usuarioAtualizado.setNome("Nome Novo");
            usuarioAtualizado.setIdade(30);

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepository.save(any(UsuarioEntity.class))).thenReturn(usuarioAtualizado);

            UsuarioDtoOut resultado = usuarioService.atualizar(1L, dadosAtualizados);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNome()).isEqualTo("Nome Novo");
            assertThat(resultado.getIdade()).isEqualTo(30);
            verify(usuarioRepository, times(1)).save(any(UsuarioEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar usuário inexistente")
        void deveLancarExcecao_QuandoAtualizarUsuarioInexistente() {
            // Arrange
            UsuarioDtoIn dadosAtualizados = new UsuarioDtoIn();
            dadosAtualizados.setNome("Novo Nome");

            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Usuário")
    class DelecaoUsuarioTests {

        @Test
        @DisplayName("Deve deletar usuário com sucesso quando não está associado")
        void deveDeletarUsuarioComSucesso() {
            // Arrange
            when(usuarioRepository.existsById(10L)).thenReturn(true);
            when(clienteRepository.existsById(10L)).thenReturn(false);
            when(profissionalRepository.existsById(10L)).thenReturn(false);

            usuarioService.deletar(10L);

            verify(usuarioRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar usuário inexistente")
        void deveLancarExcecao_QuandoDeletarUsuarioInexistente() {
            // Arrange
            when(usuarioRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(usuarioRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar usuário associado a cliente")
        void deveLancarExcecao_QuandoDeletarUsuarioAssociadoACliente() {
            when(usuarioRepository.existsById(5L)).thenReturn(true);
            when(clienteRepository.existsById(5L)).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.deletar(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cliente");

            verify(usuarioRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar usuário associado a profissional")
        void deveLancarExcecao_QuandoDeletarUsuarioAssociadoAProfissional() {
            when(usuarioRepository.existsById(5L)).thenReturn(true);
            when(clienteRepository.existsById(5L)).thenReturn(false);
            when(profissionalRepository.existsById(5L)).thenReturn(true);

x            assertThatThrownBy(() -> usuarioService.deletar(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("profissional");

            verify(usuarioRepository, never()).deleteById(any());
        }
    }
}
