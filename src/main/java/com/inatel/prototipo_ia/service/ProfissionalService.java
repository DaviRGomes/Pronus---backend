package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ProfissionalDtoIn;
import com.inatel.prototipo_ia.dto.out.ProfissionalDtoOut;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.repository.TratamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ChatRepository chatRepository;
    private final TratamentoRepository tratamentoRepository;

    public ProfissionalService(ProfissionalRepository profissionalRepository, ChatRepository chatRepository, TratamentoRepository tratamentoRepository) {
        this.profissionalRepository = profissionalRepository;
        this.chatRepository = chatRepository;
        this.tratamentoRepository = tratamentoRepository;
    }

    /**
     * Cria um novo profissional a partir de DTO In e retorna DTO Out.
     */
    public ProfissionalDtoOut criar(ProfissionalDtoIn profissionalDto) {
        validarProfissionalDto(profissionalDto);

        ProfissionalEntity entity = new ProfissionalEntity();
        aplicarDtoNoEntity(entity, profissionalDto);

        ProfissionalEntity salvo = profissionalRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todos os profissionais e retorna lista de DTOs de saída.
     */
    public List<ProfissionalDtoOut> buscarTodos() {
        return profissionalRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um profissional pelo seu ID e retorna DTO de saída.
     */
    public Optional<ProfissionalDtoOut> buscarPorId(Long id) {
        return profissionalRepository.findById(id).map(this::toDto);
    }

    /**
     * Atualiza os dados de um profissional via DTO In e retorna DTO Out.
     */
    public ProfissionalDtoOut atualizar(Long id, ProfissionalDtoIn profissionalDto) {
        Optional<ProfissionalEntity> optionalProfissional = profissionalRepository.findById(id);
        if (optionalProfissional.isEmpty()) {
            throw new EntityNotFoundException("Profissional não encontrado com o ID: " + id);
        }

        validarProfissionalDto(profissionalDto);

        ProfissionalEntity existente = optionalProfissional.get();
        aplicarDtoNoEntity(existente, profissionalDto);

        ProfissionalEntity atualizado = profissionalRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um profissional, se ele não estiver associado a chats ou tratamentos.
     */
    public void deletar(Long id) {
        if (!profissionalRepository.existsById(id)) {
            throw new EntityNotFoundException("Profissional não encontrado com o ID: " + id);
        }

        boolean emUsoEmChat = chatRepository.existsByProfissionalId(id);
        boolean emUsoEmTratamento = tratamentoRepository.existsByProfissionalId(id);

        if (emUsoEmChat || emUsoEmTratamento) {
            throw new IllegalStateException("Não é possível deletar o profissional pois ele está associado a chats ou tratamentos existentes.");
        }

        profissionalRepository.deleteById(id);
    }

    /**
     * Busca profissionais experientes (>= 5 anos).
     */
    public List<ProfissionalDtoOut> buscarExperientes() {
        return profissionalRepository.findProfissionaisExperientes()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca profissionais com experiência maior que X anos.
     */
    public List<ProfissionalDtoOut> buscarComExperienciaMaiorQue(Integer anos) {
        if (anos == null || anos < 0) {
            throw new IllegalArgumentException("Os anos de experiência devem ser não negativos.");
        }
        return profissionalRepository.findByExperienciaGreaterThan(anos)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca profissionais qualificados por experiência mínima e idade mínima.
     */
    public List<ProfissionalDtoOut> buscarQualificados(Integer experienciaMinima, Integer idadeMinima) {
        if (experienciaMinima == null || experienciaMinima < 0) {
            throw new IllegalArgumentException("A experiência mínima deve ser não negativa.");
        }
        if (idadeMinima == null || idadeMinima < 0) {
            throw new IllegalArgumentException("A idade mínima deve ser não negativa.");
        }
        return profissionalRepository.findByExperienciaAndIdadeMinima(experienciaMinima, idadeMinima)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private ProfissionalDtoOut toDto(ProfissionalEntity entity) {
        List<Long> chatIds = (entity.getChats() != null)
                ? entity.getChats().stream().map(c -> c.getId()).collect(Collectors.toList())
                : null;

        List<Long> tratamentoIds = (entity.getTratamentos() != null)
                ? entity.getTratamentos().stream().map(t -> t.getId()).collect(Collectors.toList())
                : null;

        ProfissionalDtoOut dto = new ProfissionalDtoOut();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setIdade(entity.getIdade());
        dto.setEndereco(entity.getEndereco());
        dto.setCertificados(entity.getCertificados());
        dto.setExperiencia(entity.getExperiencia());
        dto.setChatIds(chatIds);
        dto.setTratamentoIds(tratamentoIds);
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     */
    private void aplicarDtoNoEntity(ProfissionalEntity destino, ProfissionalDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setIdade(fonte.getIdade());
        destino.setEndereco(fonte.getEndereco());
        destino.setCertificados(fonte.getCertificados());
        destino.setExperiencia(fonte.getExperiencia());
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarProfissionalDto(ProfissionalDtoIn profissional) {
        if (profissional == null) {
            throw new IllegalArgumentException("O objeto de profissional não pode ser nulo.");
        }
        if (profissional.getNome() == null || profissional.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do profissional é obrigatório.");
        }
        if (profissional.getCertificados() == null || profissional.getCertificados().isBlank()) {
            throw new IllegalArgumentException("Os certificados são obrigatórios.");
        }
        if (profissional.getIdade() != null && profissional.getIdade() < 0) {
            throw new IllegalArgumentException("A idade não pode ser negativa.");
        }
        if (profissional.getExperiencia() != null && profissional.getExperiencia() < 0) {
            throw new IllegalArgumentException("A experiência não pode ser negativa.");
        }
    }
}