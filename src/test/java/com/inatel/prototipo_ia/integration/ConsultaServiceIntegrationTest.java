package com.inatel.prototipo_ia.integration;

import com.inatel.prototipo_ia.dto.in.ConsultaDtoIn;
import com.inatel.prototipo_ia.dto.out.ConsultaDtoOut;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.ConsultaEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ConsultaRepository;
import com.inatel.prototipo_ia.repository.EspecialistaRepository;
import com.inatel.prototipo_ia.service.ConsultaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de Integração - ConsultaService
 *
 * Valida:
 * - Relacionamento @ManyToOne entre Consulta ↔ Cliente e Consulta ↔ Especialista
 * - Foreign keys funcionando corretamente
 * - Queries com filtros por data e status
 * - Constraints do banco impedindo consultas inválidas
 *
 * Importância:
 * - Consultas têm dois relacionamentos simultâneos (Cliente + Especialista)
 * - Queries por data são críticas pra agenda do sistema
 */
@DisplayName("Testes de Integração - ConsultaService")
class ConsultaServiceIntegrationTest extends BaseIntegrationTest {

    @TestConfiguration
    static class ConsultaServiceTestConfiguration {
        @Bean
        public ConsultaService consultaService(ConsultaRepository consultaRepository,
                                                ClienteRepository clienteRepository,
                                                EspecialistaRepository especialistaRepository) {
            return new ConsultaService(consultaRepository, clienteRepository, especialistaRepository);
        }
    }

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private ConsultaService consultaService;

    private ClienteEntity clientePadrao;
    private EspecialistaEntity especialistaPadrao;

    @BeforeEach
    void setUp() {
        // Limpeza e setup de dados base
        consultaRepository.deleteAll();
        especialistaRepository.deleteAll();
        clienteRepository.deleteAll();

        // Criando cliente e especialista padrão pra reutilizar nos testes
        // Evita duplicação de código
        clientePadrao = criarEPersistirCliente("Cliente Teste", "cliente@teste.com");
        especialistaPadrao = criarEPersistirEspecialista("Dr. Especialista", "especialista@teste.com");
    }

    @Nested
    @DisplayName("Testes de Criação com Relacionamentos")
    class CriacaoIntegracaoTests {

        @Test
        @DisplayName("Deve criar consulta com relacionamentos Cliente e Especialista")
        void deveCriarConsultaComRelacionamentos() {
            ConsultaDtoIn consultaDto = criarConsultaDto(
                    clientePadrao.getId(),
                    especialistaPadrao.getId(),
                    LocalDate.now().plusDays(1),
                    "Agendada"
            );

            ConsultaDtoOut resultado = consultaService.criar(consultaDto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isNotNull();
            assertThat(resultado.getClienteId()).isEqualTo(clientePadrao.getId());
            assertThat(resultado.getEspecialistaId()).isEqualTo(especialistaPadrao.getId());

            // Validando foreign keys no banco H2
            ConsultaEntity consultaNoBanco = consultaRepository.findById(resultado.getId()).get();
            assertThat(consultaNoBanco.getCliente().getId()).isEqualTo(clientePadrao.getId());
            assertThat(consultaNoBanco.getEspecialista().getId()).isEqualTo(especialistaPadrao.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar consulta com cliente inexistente")
        void deveLancarExcecaoComClienteInexistente() {
            ConsultaDtoIn consultaDto = criarConsultaDto(
                    999L,
                    especialistaPadrao.getId(),
                    LocalDate.now(),
                    "Agendada"
            );

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar consulta com especialista inexistente")
        void deveLancarExcecaoComEspecialistaInexistente() {
            ConsultaDtoIn consultaDto = criarConsultaDto(
                    clientePadrao.getId(),
                    999L,
                    LocalDate.now(),
                    "Agendada"
            );

            assertThatThrownBy(() -> consultaService.criar(consultaDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Especialista");
        }
    }

    @Nested
    @DisplayName("Testes de Busca com Filtros")
    class BuscaIntegracaoTests {

        @Test
        @DisplayName("Deve buscar todas as consultas do banco")
        void deveBuscarTodasAsConsultas() {
            criarEPersistirConsulta(LocalDate.now(), "Agendada");
            criarEPersistirConsulta(LocalDate.now().plusDays(1), "Confirmada");
            criarEPersistirConsulta(LocalDate.now().plusDays(2), "Realizada");

            List<ConsultaDtoOut> resultados = consultaService.buscarTodos();

            assertThat(resultados).hasSize(3);
        }

        @Test
        @DisplayName("Deve buscar consultas por cliente ID")
        void deveBuscarConsultasPorClienteId() {
            ClienteEntity outroCliente = criarEPersistirCliente("Outro Cliente", "outro@teste.com");

            criarEPersistirConsulta(clientePadrao, especialistaPadrao, LocalDate.now());
            criarEPersistirConsulta(clientePadrao, especialistaPadrao, LocalDate.now().plusDays(1));
            criarEPersistirConsulta(outroCliente, especialistaPadrao, LocalDate.now());

            List<ConsultaDtoOut> resultados = consultaService.buscarPorClienteId(clientePadrao.getId());

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getClienteId().equals(clientePadrao.getId()));
        }

        @Test
        @DisplayName("Deve buscar consultas por especialista ID")
        void deveBuscarConsultasPorEspecialistaId() {
            EspecialistaEntity outroEspecialista = criarEPersistirEspecialista(
                    "Dra. Outra", "outra@teste.com");

            criarEPersistirConsulta(clientePadrao, especialistaPadrao, LocalDate.now());
            criarEPersistirConsulta(clientePadrao, outroEspecialista, LocalDate.now());
            criarEPersistirConsulta(clientePadrao, especialistaPadrao, LocalDate.now().plusDays(1));

            List<ConsultaDtoOut> resultados = consultaService.buscarPorEspecialistaId(especialistaPadrao.getId());

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getEspecialistaId().equals(especialistaPadrao.getId()));
        }

        @Test
        @DisplayName("Deve buscar consultas por data específica")
        void deveBuscarConsultasPorData() {
            LocalDate dataAlvo = LocalDate.of(2024, 12, 25);

            criarEPersistirConsulta(dataAlvo, "Agendada");
            criarEPersistirConsulta(dataAlvo, "Confirmada");
            criarEPersistirConsulta(LocalDate.of(2024, 12, 26), "Agendada");

            List<ConsultaDtoOut> resultados = consultaService.buscarPorData(dataAlvo);

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getData().equals(dataAlvo));
        }

        @Test
        @DisplayName("Deve buscar consultas por status")
        void deveBuscarConsultasPorStatus() {
            criarEPersistirConsulta(LocalDate.now(), "Agendada");
            criarEPersistirConsulta(LocalDate.now().plusDays(1), "Confirmada");
            criarEPersistirConsulta(LocalDate.now().plusDays(2), "Agendada");
            criarEPersistirConsulta(LocalDate.now().plusDays(3), "Realizada");

            List<ConsultaDtoOut> resultados = consultaService.buscarPorStatus("Agendada");

            assertThat(resultados).hasSize(2);
            assertThat(resultados).allMatch(c -> c.getStatus().equals("Agendada"));
        }
    }

    @Nested
    @DisplayName("Testes de Atualização")
    class AtualizacaoIntegracaoTests {

        @Test
        @DisplayName("Deve atualizar dados da consulta mantendo relacionamentos")
        void deveAtualizarConsultaMantentoRelacionamentos() {
            ConsultaEntity consultaExistente = criarEPersistirConsulta(
                    LocalDate.now(), "Agendada");

            ConsultaDtoIn dadosAtualizados = new ConsultaDtoIn();
            dadosAtualizados.setClienteId(clientePadrao.getId());
            dadosAtualizados.setEspecialistaId(especialistaPadrao.getId());
            dadosAtualizados.setData(LocalDate.now().plusDays(5));
            dadosAtualizados.setHora(LocalTime.of(15, 0));
            dadosAtualizados.setTipo("Retorno");
            dadosAtualizados.setStatus("Confirmada");

            ConsultaDtoOut resultado = consultaService.atualizar(consultaExistente.getId(), dadosAtualizados);

            assertThat(resultado.getStatus()).isEqualTo("Confirmada");
            assertThat(resultado.getTipo()).isEqualTo("Retorno");
            assertThat(resultado.getClienteId()).isEqualTo(clientePadrao.getId());
            assertThat(resultado.getEspecialistaId()).isEqualTo(especialistaPadrao.getId());

            // Verifica persistência
            ConsultaEntity atualizada = consultaRepository.findById(consultaExistente.getId()).get();
            assertThat(atualizada.getStatus()).isEqualTo("Confirmada");
        }
    }

    @Nested
    @DisplayName("Testes de Deleção")
    class DelecaoIntegracaoTests {

        @Test
        @DisplayName("Deve deletar consulta do banco")
        void deveDeletarConsulta() {
            ConsultaEntity consulta = criarEPersistirConsulta(LocalDate.now(), "Agendada");
            Long consultaId = consulta.getId();

            consultaService.deletar(consultaId);

            assertThat(consultaRepository.findById(consultaId)).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar consulta inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            assertThatThrownBy(() -> consultaService.deletar(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // Métodos auxiliares
    private ClienteEntity criarEPersistirCliente(String nome, String login) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome(nome);
        cliente.setLogin(login);
        cliente.setSenha("senha123");
        cliente.setIdade(30);
        return clienteRepository.save(cliente);
    }

    private EspecialistaEntity criarEPersistirEspecialista(String nome, String login) {
        EspecialistaEntity especialista = new EspecialistaEntity();
        especialista.setNome(nome);
        especialista.setLogin(login);
        especialista.setSenha("senha123");
        especialista.setEspecialidade("Fonoaudiologia");
        return especialistaRepository.save(especialista);
    }

    private ConsultaEntity criarEPersistirConsulta(LocalDate data, String status) {
        return criarEPersistirConsulta(clientePadrao, especialistaPadrao, data, status);
    }

    private ConsultaEntity criarEPersistirConsulta(ClienteEntity cliente, EspecialistaEntity especialista, LocalDate data) {
        return criarEPersistirConsulta(cliente, especialista, data, "Agendada");
    }

    private ConsultaEntity criarEPersistirConsulta(ClienteEntity cliente, EspecialistaEntity especialista,
                                                    LocalDate data, String status) {
        ConsultaEntity consulta = new ConsultaEntity();
        consulta.setCliente(cliente);
        consulta.setEspecialista(especialista);
        consulta.setData(data);
        consulta.setHora(LocalTime.of(10, 0));
        consulta.setTipo("Primeira Consulta");
        consulta.setStatus(status);
        return consultaRepository.save(consulta);
    }

    private ConsultaDtoIn criarConsultaDto(Long clienteId, Long especialistaId, LocalDate data, String status) {
        ConsultaDtoIn dto = new ConsultaDtoIn();
        dto.setClienteId(clienteId);
        dto.setEspecialistaId(especialistaId);
        dto.setData(data);
        dto.setHora(LocalTime.of(14, 30));
        dto.setTipo("Consulta Regular");
        dto.setStatus(status);
        return dto;
    }
}
