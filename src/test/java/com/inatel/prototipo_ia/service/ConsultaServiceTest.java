package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ConsultaDtoIn;
import com.inatel.prototipo_ia.dto.out.ConsultaDtoOut;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.ConsultaEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ConsultaRepository;
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
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EspecialistaRepository especialistaRepository;

    @InjectMocks
    private ConsultaService consultaService;

    @Nested
    @DisplayName("Testes de Criação de Consulta")
    class CriacaoConsultaTests {

        @Test
        @DisplayName("Deve criar consulta com sucesso quando dados válidos")
        void deveCriarConsultaComSucesso() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(1L);
            consultaDto.setEspecialistaId(2L);
            consultaDto.setData(LocalDate.of(2025, 12, 15));
            consultaDto.setHora(LocalTime.of(14, 30));
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("Agendada");

            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consultaSalva = new ConsultaEntity();
            consultaSalva.setId(10L);
            consultaSalva.setCliente(cliente);
            consultaSalva.setEspecialista(especialista);
            consultaSalva.setData(LocalDate.of(2025, 12, 15));
            consultaSalva.setHora(LocalTime.of(14, 30));
            consultaSalva.setTipo("Avaliação");
            consultaSalva.setStatus("Agendada");

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(especialistaRepository.findById(2L)).thenReturn(Optional.of(especialista));
            when(consultaRepository.save(any(ConsultaEntity.class))).thenReturn(consultaSalva);

            ConsultaDtoOut resultado = consultaService.criar(consultaDto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getClienteId()).isEqualTo(1L);
            assertThat(resultado.getEspecialistaId()).isEqualTo(2L);
            assertThat(resultado.getData()).isEqualTo(LocalDate.of(2025, 12, 15));
            assertThat(resultado.getHora()).isEqualTo(LocalTime.of(14, 30));
            assertThat(resultado.getTipo()).isEqualTo("Avaliação");
            assertThat(resultado.getStatus()).isEqualTo("Agendada");

            verify(consultaRepository, times(1)).save(any(ConsultaEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando DTO é nulo")
        void deveLancarExcecao_QuandoDtoNulo() {
            assertThatThrownBy(() -> consultaService.criar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void deveLancarExcecao_QuandoClienteNaoExiste() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(999L);
            consultaDto.setEspecialistaId(2L);
            consultaDto.setData(LocalDate.now());
            consultaDto.setHora(LocalTime.now());
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("Agendada");

            when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cliente")
                .hasMessageContaining("999");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando especialista não existe")
        void deveLancarExcecao_QuandoEspecialistaNaoExiste() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(1L);
            consultaDto.setEspecialistaId(999L);
            consultaDto.setData(LocalDate.now());
            consultaDto.setHora(LocalTime.now());
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("Agendada");

            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(especialistaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Especialista")
                .hasMessageContaining("999");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando clienteId é nulo")
        void deveLancarExcecao_QuandoClienteIdNulo() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(null);
            consultaDto.setEspecialistaId(2L);
            consultaDto.setData(LocalDate.now());
            consultaDto.setHora(LocalTime.now());
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("Agendada");

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cliente");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando especialistaId é nulo")
        void deveLancarExcecao_QuandoEspecialistaIdNulo() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(1L);
            consultaDto.setEspecialistaId(null);
            consultaDto.setData(LocalDate.now());
            consultaDto.setHora(LocalTime.now());
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("Agendada");

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("especialista");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando data é nula")
        void deveLancarExcecao_QuandoDataNula() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(1L);
            consultaDto.setEspecialistaId(2L);
            consultaDto.setData(null);
            consultaDto.setHora(LocalTime.now());
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("Agendada");

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando hora é nula")
        void deveLancarExcecao_QuandoHoraNula() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(1L);
            consultaDto.setEspecialistaId(2L);
            consultaDto.setData(LocalDate.now());
            consultaDto.setHora(null);
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("Agendada");

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hora");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tipo está em branco")
        void deveLancarExcecao_QuandoTipoEstaBranco() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(1L);
            consultaDto.setEspecialistaId(2L);
            consultaDto.setData(LocalDate.now());
            consultaDto.setHora(LocalTime.now());
            consultaDto.setTipo("   ");
            consultaDto.setStatus("Agendada");

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo");

            verify(consultaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando status está em branco")
        void deveLancarExcecao_QuandoStatusEstaBranco() {
            ConsultaDtoIn consultaDto = new ConsultaDtoIn();
            consultaDto.setClienteId(1L);
            consultaDto.setEspecialistaId(2L);
            consultaDto.setData(LocalDate.now());
            consultaDto.setHora(LocalTime.now());
            consultaDto.setTipo("Avaliação");
            consultaDto.setStatus("   ");

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status");

            verify(consultaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Consultas")
    class BuscaConsultaTests {

        @Test
        @DisplayName("Deve buscar todas as consultas com sucesso")
        void deveBuscarTodasAsConsultas() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consulta1 = new ConsultaEntity();
            consulta1.setId(1L);
            consulta1.setCliente(cliente);
            consulta1.setEspecialista(especialista);

            ConsultaEntity consulta2 = new ConsultaEntity();
            consulta2.setId(2L);
            consulta2.setCliente(cliente);
            consulta2.setEspecialista(especialista);

            when(consultaRepository.findAll()).thenReturn(Arrays.asList(consulta1, consulta2));

            List<ConsultaDtoOut> resultados = consultaService.buscarTodos();

            assertThat(resultados).hasSize(2);
            assertThat(resultados.get(0).getId()).isEqualTo(1L);
            assertThat(resultados.get(1).getId()).isEqualTo(2L);
            verify(consultaRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve buscar consulta por ID com sucesso")
        void deveBuscarConsultaPorId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consulta = new ConsultaEntity();
            consulta.setId(10L);
            consulta.setCliente(cliente);
            consulta.setEspecialista(especialista);
            consulta.setTipo("Retorno");

            when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));

            Optional<ConsultaDtoOut> resultado = consultaService.buscarPorId(10L);

            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(10L);
            assertThat(resultado.get().getTipo()).isEqualTo("Retorno");
            verify(consultaRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("Deve retornar vazio quando consulta não existe")
        void deveRetornarVazio_QuandoConsultaNaoExiste() {
            when(consultaRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<ConsultaDtoOut> resultado = consultaService.buscarPorId(999L);

            assertThat(resultado).isEmpty();
            verify(consultaRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Deve buscar consultas por cliente ID")
        void deveBuscarConsultasPorClienteId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consulta1 = new ConsultaEntity();
            consulta1.setId(1L);
            consulta1.setCliente(cliente);
            consulta1.setEspecialista(especialista);

            when(consultaRepository.findByClienteId(1L)).thenReturn(Arrays.asList(consulta1));

            List<ConsultaDtoOut> resultados = consultaService.buscarPorClienteId(1L);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getClienteId()).isEqualTo(1L);
            verify(consultaRepository, times(1)).findByClienteId(1L);
        }

        @Test
        @DisplayName("Deve buscar consultas por especialista ID")
        void deveBuscarConsultasPorEspecialistaId() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consulta1 = new ConsultaEntity();
            consulta1.setId(1L);
            consulta1.setCliente(cliente);
            consulta1.setEspecialista(especialista);

            when(consultaRepository.findByEspecialistaId(2L)).thenReturn(Arrays.asList(consulta1));

            List<ConsultaDtoOut> resultados = consultaService.buscarPorEspecialistaId(2L);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getEspecialistaId()).isEqualTo(2L);
            verify(consultaRepository, times(1)).findByEspecialistaId(2L);
        }

        @Test
        @DisplayName("Deve buscar consultas por data")
        void deveBuscarConsultasPorData() {
            LocalDate data = LocalDate.of(2025, 12, 15);
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consulta1 = new ConsultaEntity();
            consulta1.setId(1L);
            consulta1.setCliente(cliente);
            consulta1.setEspecialista(especialista);
            consulta1.setData(data);

            when(consultaRepository.findByData(data)).thenReturn(Arrays.asList(consulta1));

            List<ConsultaDtoOut> resultados = consultaService.buscarPorData(data);

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getData()).isEqualTo(data);
            verify(consultaRepository, times(1)).findByData(data);
        }

        @Test
        @DisplayName("Deve lançar exceção quando data é nula na busca")
        void deveLancarExcecao_QuandoDataNulaNaBusca() {
            assertThatThrownBy(() -> consultaService.buscarPorData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");

            verify(consultaRepository, never()).findByData(any());
        }

        @Test
        @DisplayName("Deve buscar consultas por status")
        void deveBuscarConsultasPorStatus() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consulta1 = new ConsultaEntity();
            consulta1.setId(1L);
            consulta1.setCliente(cliente);
            consulta1.setEspecialista(especialista);
            consulta1.setStatus("Confirmada");

            when(consultaRepository.findByStatus("Confirmada")).thenReturn(Arrays.asList(consulta1));

            List<ConsultaDtoOut> resultados = consultaService.buscarPorStatus("Confirmada");

            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getStatus()).isEqualTo("Confirmada");
            verify(consultaRepository, times(1)).findByStatus("Confirmada");
        }

        @Test
        @DisplayName("Deve lançar exceção quando status está em branco na busca")
        void deveLancarExcecao_QuandoStatusEstaBrancoNaBusca() {
            assertThatThrownBy(() -> consultaService.buscarPorStatus("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status");

            verify(consultaRepository, never()).findByStatus(any());
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Consulta")
    class AtualizacaoConsultaTests {

        @Test
        @DisplayName("Deve atualizar consulta com sucesso")
        void deveAtualizarConsultaComSucesso() {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(1L);

            EspecialistaEntity especialista = new EspecialistaEntity();
            especialista.setId(2L);

            ConsultaEntity consultaExistente = new ConsultaEntity();
            consultaExistente.setId(10L);
            consultaExistente.setCliente(cliente);
            consultaExistente.setEspecialista(especialista);
            consultaExistente.setStatus("Agendada");

            ConsultaDtoIn dadosAtualizados = new ConsultaDtoIn();
            dadosAtualizados.setClienteId(1L);
            dadosAtualizados.setEspecialistaId(2L);
            dadosAtualizados.setData(LocalDate.now());
            dadosAtualizados.setHora(LocalTime.now());
            dadosAtualizados.setTipo("Retorno");
            dadosAtualizados.setStatus("Confirmada");

            ConsultaEntity consultaAtualizada = new ConsultaEntity();
            consultaAtualizada.setId(10L);
            consultaAtualizada.setCliente(cliente);
            consultaAtualizada.setEspecialista(especialista);
            consultaAtualizada.setStatus("Confirmada");

            when(consultaRepository.findById(10L)).thenReturn(Optional.of(consultaExistente));
            when(consultaRepository.save(any(ConsultaEntity.class))).thenReturn(consultaAtualizada);

            ConsultaDtoOut resultado = consultaService.atualizar(10L, dadosAtualizados);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getStatus()).isEqualTo("Confirmada");
            verify(consultaRepository, times(1)).save(any(ConsultaEntity.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar consulta inexistente")
        void deveLancarExcecao_QuandoAtualizarConsultaInexistente() {
            ConsultaDtoIn dadosAtualizados = new ConsultaDtoIn();
            dadosAtualizados.setClienteId(1L);
            dadosAtualizados.setEspecialistaId(2L);
            dadosAtualizados.setData(LocalDate.now());
            dadosAtualizados.setHora(LocalTime.now());
            dadosAtualizados.setTipo("Avaliação");
            dadosAtualizados.setStatus("Agendada");

            when(consultaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> consultaService.atualizar(999L, dadosAtualizados))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(consultaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Deleção de Consulta")
    class DelecaoConsultaTests {

        @Test
        @DisplayName("Deve deletar consulta com sucesso")
        void deveDeletarConsultaComSucesso() {
            when(consultaRepository.existsById(10L)).thenReturn(true);

            consultaService.deletar(10L);

            verify(consultaRepository, times(1)).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar consulta inexistente")
        void deveLancarExcecao_QuandoDeletarConsultaInexistente() {
            when(consultaRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> consultaService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");

            verify(consultaRepository, never()).deleteById(any());
        }
    }
}
