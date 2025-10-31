package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.DisponibilidadeDtoIn;
import com.inatel.prototipo_ia.dto.out.DisponibilidadeDtoOut;
import com.inatel.prototipo_ia.entity.DisponibilidadeEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.DisponibilidadeRepository;
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
public class DisponibilidadeService {

    private final DisponibilidadeRepository disponibilidadeRepository;
    private final EspecialistaRepository especialistaRepository;

    public DisponibilidadeService(DisponibilidadeRepository disponibilidadeRepository,
                                  EspecialistaRepository especialistaRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.especialistaRepository = especialistaRepository;
    }

    /**
     * Cria uma nova disponibilidade a partir de DTO In e retorna DTO Out.
     */
    public DisponibilidadeDtoOut criar(DisponibilidadeDtoIn disponibilidadeDto) {
        validarDisponibilidadeDto(disponibilidadeDto);

        // Validação do especialista
        Long especialistaId = disponibilidadeDto.getEspecialistaId();
        Optional<EspecialistaEntity> optionalEspecialista = especialistaRepository.findById(especialistaId);
        if (optionalEspecialista.isEmpty()) {
            throw new EntityNotFoundException("Não é possível criar a disponibilidade pois o especialista com ID " + especialistaId + " não foi encontrado.");
        }
        EspecialistaEntity especialista = optionalEspecialista.get();

        DisponibilidadeEntity entity = new DisponibilidadeEntity();
        aplicarDtoNoEntity(entity, disponibilidadeDto);
        entity.setEspecialista(especialista);

        DisponibilidadeEntity salvo = disponibilidadeRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todas as disponibilidades e retorna lista de DTOs de saída.
     */
    public List<DisponibilidadeDtoOut> buscarTodos() {
        return disponibilidadeRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma disponibilidade pelo seu ID e retorna DTO de saída.
     */
    public Optional<DisponibilidadeDtoOut> buscarPorId(Long id) {
        return disponibilidadeRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca disponibilidades de um especialista específico.
     */
    public List<DisponibilidadeDtoOut> buscarPorEspecialistaId(Long especialistaId) {
        return disponibilidadeRepository.findByEspecialistaId(especialistaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca disponibilidades por data.
     */
    public List<DisponibilidadeDtoOut> buscarPorData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data não pode ser nula.");
        }
        return disponibilidadeRepository.findByData(data)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca disponibilidades por status.
     */
    public List<DisponibilidadeDtoOut> buscarPorStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("O status não pode ser vazio.");
        }
        return disponibilidadeRepository.findByStatus(status)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca disponibilidades disponíveis (status "disponível").
     */
    public List<DisponibilidadeDtoOut> buscarDisponiveis() {
        return disponibilidadeRepository.findDisponibilidadesDisponiveis()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza os dados de uma disponibilidade existente via DTO In e retorna DTO Out.
     */
    public DisponibilidadeDtoOut atualizar(Long id, DisponibilidadeDtoIn disponibilidadeDto) {
        Optional<DisponibilidadeEntity> optionalDisponibilidade = disponibilidadeRepository.findById(id);
        if (optionalDisponibilidade.isEmpty()) {
            throw new EntityNotFoundException("Disponibilidade não encontrada com o ID: " + id);
        }

        validarDisponibilidadeDto(disponibilidadeDto);

        DisponibilidadeEntity existente = optionalDisponibilidade.get();
        aplicarDtoNoEntity(existente, disponibilidadeDto);
        // Não permitimos alterar o especialista no update

        DisponibilidadeEntity atualizado = disponibilidadeRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta uma disponibilidade.
     */
    public void deletar(Long id) {
        if (!disponibilidadeRepository.existsById(id)) {
            throw new EntityNotFoundException("Disponibilidade não encontrada com o ID: " + id);
        }
        disponibilidadeRepository.deleteById(id);
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private DisponibilidadeDtoOut toDto(DisponibilidadeEntity entity) {
        DisponibilidadeDtoOut dto = new DisponibilidadeDtoOut();
        dto.setId(entity.getId());
        dto.setData(entity.getData());
        dto.setHoraInicio(entity.getHoraInicio());
        dto.setHoraFim(entity.getHoraFim());
        dto.setStatus(entity.getStatus());
        dto.setEspecialistaId(entity.getEspecialista().getId());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     * Não altera o especialista no update.
     */
    private void aplicarDtoNoEntity(DisponibilidadeEntity destino, DisponibilidadeDtoIn fonte) {
        destino.setData(fonte.getData());
        destino.setHoraInicio(fonte.getHoraInicio());
        destino.setHoraFim(fonte.getHoraFim());
        destino.setStatus(fonte.getStatus());
        // Nota: especialistaId não é atualizado após criação
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarDisponibilidadeDto(DisponibilidadeDtoIn disponibilidade) {
        if (disponibilidade == null) {
            throw new IllegalArgumentException("O objeto de disponibilidade não pode ser nulo.");
        }
        if (disponibilidade.getEspecialistaId() == null) {
            throw new IllegalArgumentException("A disponibilidade deve estar associada a um especialista.");
        }
        if (disponibilidade.getData() == null) {
            throw new IllegalArgumentException("A data é obrigatória.");
        }
        if (disponibilidade.getHoraInicio() == null) {
            throw new IllegalArgumentException("A hora de início é obrigatória.");
        }
        if (disponibilidade.getHoraFim() == null) {
            throw new IllegalArgumentException("A hora de fim é obrigatória.");
        }
        if (disponibilidade.getHoraInicio().isAfter(disponibilidade.getHoraFim())) {
            throw new IllegalArgumentException("A hora de início não pode ser posterior à hora de fim.");
        }
        if (disponibilidade.getStatus() == null || disponibilidade.getStatus().isBlank()) {
            throw new IllegalArgumentException("O status é obrigatório.");
        }
    }
}