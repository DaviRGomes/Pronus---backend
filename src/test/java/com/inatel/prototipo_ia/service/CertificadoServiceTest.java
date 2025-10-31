package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.CertificadoDtoIn;
import com.inatel.prototipo_ia.dto.out.CertificadoDtoOut;
import com.inatel.prototipo_ia.entity.CertificadoEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.repository.CertificadoRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificadoServiceTest {

    @Mock
    private CertificadoRepository certificadoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CertificadoService certificadoService;

    @Nested
    @DisplayName("Testes de Criação de Certificado")
    class CriacaoCertificadoTests {

        @Test
        @DisplayName("Deve criar certificado com sucesso quando dados válidos")
        void deveCriarCertificadoComSucesso() {
            CertificadoDtoIn certificadoDto = new CertificadoDtoIn();
            certificadoDto.setClienteId(1L);
            certificadoDto.setNome("Certificado de Conclusão");
            certificadoDto.setDataEmissao(LocalDate.of(2025, 11, 1));
            certificadoDto.setNivelAlcancado("Avançado");

            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            CertificadoEntity certificadoSalvo = new CertificadoEntity();
            certificadoSalvo.setId(10L);
            certificadoSalvo.setCliente(cliente);
            certificadoSalvo.setNome("Certificado de Conclusão");
            certificadoSalvo.setDataEmissao(LocalDate.of(2025, 11, 1));
            certificadoSalvo.setNivelAlcancado("Avançado");

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(certificadoRepository.save(any(CertificadoEntity.class))).thenReturn(certificadoSalvo);

            CertificadoDtoOut resultado = certificadoService.criar(certificadoDto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getClienteId()).isEqualTo(1L);
            assertThat(resultado.getNome()).isEqualTo("Certificado de Conclusão");
            assertThat(resultado.getDataEmissao()).isEqualTo(LocalDate.of(2025, 11, 1));
            assertThat(resultado.getNivelAlcancado()).isEqualTo("Avançado");

            verify(certificadoRepository, times(1)).save(any(CertificadoEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> certificadoService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(certificadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void deveLancarExcecao_QuandoClienteNaoExiste() {
            CertificadoDtoIn certificadoDto = new CertificadoDtoIn();
            certificadoDto.setClienteId(999L);
            certificadoDto.setNome("Certificado");
            certificadoDto.setDataEmissao(LocalDate.now());
            certificadoDto.setNivelAlcancado("Básico");

            when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> certificadoService.criar(certificadoDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("cliente")
                .hasMessageContaining("999");

            verify(certificadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando clienteId é nulo")
        void deveLancarExcecao_QuandoClienteIdNulo() {
            CertificadoDtoIn certificadoDto = new CertificadoDtoIn();
            certificadoDto.setClienteId(null);
            certificadoDto.setNome("Certificado");
            certificadoDto.setDataEmissao(LocalDate.now());
            certificadoDto.setNivelAlcancado("Básico");

            // Act & Assert
            assertThatThrownBy(() -> certificadoService.criar(certificadoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cliente");

            verify(certificadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome está em branco")
        void deveLancarExcecao_QuandoNomeEstaBranco() {
            CertificadoDtoIn certificadoDto = new CertificadoDtoIn();
            certificadoDto.setClienteId(1L);
            certificadoDto.setNome("   ");
            certificadoDto.setDataEmissao(LocalDate.now());
            certificadoDto.setNivelAlcancado("Básico");

            // Act & Assert
            assertThatThrownBy(() -> certificadoService.criar(certificadoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nome");

            verify(certificadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando data de emissão é nula")
        void deveLancarExcecao_QuandoDataEmissaoNula() {
            CertificadoDtoIn certificadoDto = new CertificadoDtoIn();
            certificadoDto.setClienteId(1L);
            certificadoDto.setNome("Certificado");
            certificadoDto.setDataEmissao(null);
            certificadoDto.setNivelAlcancado("Básico");

            // Act & Assert
            assertThatThrownBy(() -> certificadoService.criar(certificadoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data de emissão");

            verify(certificadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando nível alcançado está em branco")
        void deveLancarExcecao_QuandoNivelAlcancadoEstaBranco() {
            CertificadoDtoIn certificadoDto = new CertificadoDtoIn();
            certificadoDto.setClienteId(1L);
            certificadoDto.setNome("Certificado");
            certificadoDto.setDataEmissao(LocalDate.now());
            certificadoDto.setNivelAlcancado("   ");

            // Act & Assert
            assertThatThrownBy(() -> certificadoService.criar(certificadoDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nível alcançado");

            verify(certificadoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Certificados")
    class BuscaCertificadoTests {

        @Test
        @DisplayName("Deve buscar todos os certificados com sucesso")
        void deveBuscarTodosOsCertificados() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            CertificadoEntity cert1 = new CertificadoEntity();
            cert1.setId(1L);
            cert1.setCliente(cliente);
            cert1.setNome("Certificado 1");

            CertificadoEntity cert2 = new CertificadoEntity();
            cert2.setId(2L);
            cert2.setCliente(cliente);
            cert2.setNome("Certificado 2");

            when(certificadoRepository.findAll()).thenReturn(Arrays.asList(cert1, cert2));

            List<CertificadoDtoOut> resultados = certificadoService.buscarTodos();

            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getNome()).isEqualTo("Certificado 1");
            assertThat(resultados.get(1).getNome()).isEqualTo("Certificado 2");
            verify(certificadoRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar certificado por ID com sucesso")
        void deveBuscarCertificadoPorId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            CertificadoEntity certificado = new CertificadoEntity();
            certificado.setId(10L);
            certificado.setCliente(cliente);
            certificado.setNome("Certificado de Conclusão");

            when(certificadoRepository.findById(10L)).thenReturn(Optional.of(certificado));

            Optional<CertificadoDtoOut> resultado = certificadoService.buscarPorId(10L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getNome()).isEqualTo("Certificado de Conclusão");
            verify(certificadoRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando certificado não existe")
        void deveRetornarVazio_QuandoCertificadoNaoExiste() {
            when(certificadoRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<CertificadoDtoOut> resultado = certificadoService.buscarPorId(999L);

            assertThat(resultado).isEmpty();
            verify(certificadoRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar certificados por cliente ID")
        void deveBuscarCertificadosPorClienteId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            CertificadoEntity cert1 = new CertificadoEntity();
            cert1.setId(1L);
            cert1.setCliente(cliente);

            when(certificadoRepository.findByClienteId(1L)).thenReturn(Arrays.asList(cert1));

            List<CertificadoDtoOut> resultados = certificadoService.buscarPorClienteId(1L);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getClienteId()).isEqualTo(1L);
            verify(certificadoRepository, times(1)).findByClienteId(1L);
        }

        @Test
        @DisplayName("Deve buscar certificados por nível")
        void deveBuscarCertificadosPorNivel() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            CertificadoEntity cert1 = new CertificadoEntity();
            cert1.setId(1L);
            cert1.setCliente(cliente);
            cert1.setNivelAlcancado("Avançado");

            when(certificadoRepository.findByNivelAlcancado("Avançado")).thenReturn(Arrays.asList(cert1));

            List<CertificadoDtoOut> resultados = certificadoService.buscarPorNivel("Avançado");

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getNivelAlcancado()).isEqualTo("Avançado");
            verify(certificadoRepository, times(1)).findByNivelAlcancado("Avançado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando nível está em branco na busca")
        void deveLancarExcecao_QuandoNivelEstaBrancoNaBusca() {
            // Act & Assert
            assertThatThrownBy(() -> certificadoService.buscarPorNivel("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nível alcançado");

            verify(certificadoRepository, never()).findByNivelAlcancado(any());
        }

        @Test
        @DisplayName("Deve buscar certificados emitidos após uma data")
        void deveBuscarCertificadosEmitidosAposData() {
            LocalDate data = LocalDate.of(2025, 1, 1);
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            CertificadoEntity cert1 = new CertificadoEntity();
            cert1.setId(1L);
            cert1.setCliente(cliente);
            cert1.setDataEmissao(LocalDate.of(2025, 6, 1));

            when(certificadoRepository.findByDataEmissaoAfter(data)).thenReturn(Arrays.asList(cert1));

            List<CertificadoDtoOut> resultados = certificadoService.buscarPorDataEmissaoApos(data);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getDataEmissao()).isAfter(data);
            verify(certificadoRepository, times(1)).findByDataEmissaoAfter(data);
        }

        @Test
        @DisplayName("Deve lançar exceção quando data é nula na busca")
        void deveLancarExcecao_QuandoDataNulaNaBusca() {
            // Act & Assert
            assertThatThrownBy(() -> certificadoService.buscarPorDataEmissaoApos(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");

            verify(certificadoRepository, never()).findByDataEmissaoAfter(any());
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Certificado")
    class AtualizacaoCertificadoTests {

        @Test
        @DisplayName("Deve atualizar certificado com sucesso")
        void deveAtualizarCertificadoComSucesso() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            CertificadoEntity certificadoExistente = new CertificadoEntity();
            certificadoExistente.setId(10L);
            certificadoExistente.setCliente(cliente);
            certificadoExistente.setNome("Nome Antigo");

            CertificadoDtoIn dadosAtualizados = new CertificadoDtoIn();
            dadosAtualizados.setClienteId(1L);
            dadosAtualizados.setNome("Nome Atualizado");
            dadosAtualizados.setDataEmissao(LocalDate.now());
            dadosAtualizados.setNivelAlcancado("Expert");

            CertificadoEntity certificadoAtualizado = new CertificadoEntity();
            certificadoAtualizado.setId(10L);
            certificadoAtualizado.setCliente(cliente);
            certificadoAtualizado.setNome("Nome Atualizado");
            certificadoAtualizado.setNivelAlcancado("Expert");

            when(certificadoRepository.findById(10L)).thenReturn(Optional.of(certificadoExistente));
            when(certificadoRepository.save(any(CertificadoEntity.class))).thenReturn(certificadoAtualizado);

            CertificadoDtoOut resultado = certificadoService.atualizar(10L, dadosAtualizados);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getNome()).isEqualTo("Nome Atualizado");
            assertThat(resultado.getNivelAlcancado()).isEqualTo("Expert");
            verify(certificadoRepository, times(1)).save(any(CertificadoEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar certificado inexistente")
        void deveLancarExcecao_QuandoAtualizarCertificadoInexistente() {
            CertificadoDtoIn dadosAtualizados = new CertificadoDtoIn();
            dadosAtualizados.setClienteId(1L);
            dadosAtualizados.setNome("Novo Nome");
            dadosAtualizados.setDataEmissao(LocalDate.now());
            dadosAtualizados.setNivelAlcancado("Avançado");

            when(certificadoRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> certificadoService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(certificadoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Certificado")
    class DelecaoCertificadoTests {

        @Test
        @DisplayName("Deve deletar certificado com sucesso")
        void deveDeletarCertificadoComSucesso() {
            when(certificadoRepository.existsById(10L)).thenReturn(true);

            certificadoService.deletar(10L);

            verify(certificadoRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar certificado inexistente")
        void deveLancarExcecao_QuandoDeletarCertificadoInexistente() {
            when(certificadoRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> certificadoService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(certificadoRepository, never()).deleteById(any());
        }
    }
}
