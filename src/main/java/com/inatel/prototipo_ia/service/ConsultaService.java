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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final ClienteRepository clienteRepository;
    private final EspecialistaRepository especialistaRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           ClienteRepository clienteRepository,
                           EspecialistaRepository especialistaRepository) {
        this.consultaRepository = consultaRepository;
        this.clienteRepository = clienteRepository;
        this.especialistaRepository = especialistaRepository;
    }

    /**
     * Cria uma nova consulta a partir de DTO In e retorna DTO Out.
     */
    public ConsultaDtoOut criar(ConsultaDtoIn consultaDto) {
        validarConsultaDto(consultaDto);

        // Validação do cliente
        Long clienteId = consultaDto.getClienteId();
        Optional<ClienteEntity> optionalCliente = clienteRepository.findById(clienteId);
        if (optionalCliente.isEmpty()) {
            throw new EntityNotFoundException("Cliente não encontrado com o ID: " + clienteId);
        }
        ClienteEntity cliente = optionalCliente.get();

        // Validação do especialista
        Long especialistaId = consultaDto.getEspecialistaId();
        Optional<EspecialistaEntity> optionalEspecialista = especialistaRepository.findById(especialistaId);
        if (optionalEspecialista.isEmpty()) {
            throw new EntityNotFoundException("Especialista não encontrado com o ID: " + especialistaId);
        }
        EspecialistaEntity especialista = optionalEspecialista.get();

        ConsultaEntity entity = new ConsultaEntity();
        aplicarDtoNoEntity(entity, consultaDto);
        entity.setCliente(cliente);
        entity.setEspecialista(especialista);

        ConsultaEntity salvo = consultaRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todas as consultas e retorna lista de DTOs de saída.
     */
    public List<ConsultaDtoOut> buscarTodos() {
        return consultaRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma consulta pelo seu ID e retorna DTO de saída.
     */
    public Optional<ConsultaDtoOut> buscarPorId(Long id) {
        return consultaRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca consultas de um cliente específico.
     */
    public List<ConsultaDtoOut> buscarPorClienteId(Long clienteId) {
        return consultaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca consultas de um especialista específico.
     */
    public List<ConsultaDtoOut> buscarPorEspecialistaId(Long especialistaId) {
        return consultaRepository.findByEspecialistaId(especialistaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca consultas por data.
     */
    public List<ConsultaDtoOut> buscarPorData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data não pode ser nula.");
        }
        return consultaRepository.findByData(data)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca consultas por status.
     */
    public List<ConsultaDtoOut> buscarPorStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("O status não pode ser vazio.");
        }
        return consultaRepository.findByStatus(status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza os dados de uma consulta existente via DTO In e retorna DTO Out.
     */
    public ConsultaDtoOut atualizar(Long id, ConsultaDtoIn consultaDto) {
        Optional<ConsultaEntity> optionalConsulta = consultaRepository.findById(id);
        if (optionalConsulta.isEmpty()) {
            throw new EntityNotFoundException("Consulta não encontrada com o ID: " + id);
        }

        validarConsultaDto(consultaDto);

        ConsultaEntity existente = optionalConsulta.get();
        aplicarDtoNoEntity(existente, consultaDto);
        // Não permitimos alterar cliente e especialista no update

        ConsultaEntity atualizado = consultaRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta uma consulta.
     */
    public void deletar(Long id) {
        if (!consultaRepository.existsById(id)) {
            throw new EntityNotFoundException("Consulta não encontrada com o ID: " + id);
        }
        consultaRepository.deleteById(id);
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private ConsultaDtoOut toDto(ConsultaEntity entity) {
        ConsultaDtoOut dto = new ConsultaDtoOut();
        dto.setId(entity.getId());
        dto.setData(entity.getData());
        dto.setHora(entity.getHora());
        dto.setTipo(entity.getTipo());
        dto.setStatus(entity.getStatus());
        dto.setClienteId(entity.getCliente().getId());
        dto.setEspecialistaId(entity.getEspecialista().getId());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     * Não altera cliente e especialista no update.
     */
    private void aplicarDtoNoEntity(ConsultaEntity destino, ConsultaDtoIn fonte) {
        destino.setData(fonte.getData());
        destino.setHora(fonte.getHora());
        destino.setTipo(fonte.getTipo());
        destino.setStatus(fonte.getStatus());
        // Nota: clienteId e especialistaId não são atualizados após criação
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarConsultaDto(ConsultaDtoIn consulta) {
        if (consulta == null) {
            throw new IllegalArgumentException("O objeto de consulta não pode ser nulo.");
        }
        if (consulta.getClienteId() == null) {
            throw new IllegalArgumentException("A consulta deve estar associada a um cliente.");
        }
        if (consulta.getEspecialistaId() == null) {
            throw new IllegalArgumentException("A consulta deve estar associada a um especialista.");
        }
        if (consulta.getData() == null) {
            throw new IllegalArgumentException("A data da consulta é obrigatória.");
        }
        if (consulta.getHora() == null) {
            throw new IllegalArgumentException("A hora da consulta é obrigatória.");
        }
        if (consulta.getTipo() == null || consulta.getTipo().isBlank()) {
            throw new IllegalArgumentException("O tipo da consulta é obrigatório.");
        }
        if (consulta.getStatus() == null || consulta.getStatus().isBlank()) {
            throw new IllegalArgumentException("O status da consulta é obrigatório.");
        }
    }
}