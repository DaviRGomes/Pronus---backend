package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.TratamentoDtoIn;
import com.inatel.prototipo_ia.dto.out.TratamentoDtoOut;
import com.inatel.prototipo_ia.entity.ConteudoTesteEntity;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import com.inatel.prototipo_ia.entity.TratamentoEntity;
import com.inatel.prototipo_ia.repository.ConteudoTesteRepository;
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
public class TratamentoService {

    private final TratamentoRepository tratamentoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ConteudoTesteRepository conteudoTesteRepository;

    public TratamentoService(TratamentoRepository tratamentoRepository, 
                            ProfissionalRepository profissionalRepository,
                            ConteudoTesteRepository conteudoTesteRepository) {
        this.tratamentoRepository = tratamentoRepository;
        this.profissionalRepository = profissionalRepository;
        this.conteudoTesteRepository = conteudoTesteRepository;
    }

    /**
     * Cria um novo tratamento a partir de DTO In e retorna DTO Out.
     */
    public TratamentoDtoOut criar(TratamentoDtoIn tratamentoDto) {
        validarTratamentoDto(tratamentoDto);

        // Validação do profissional
        Long profissionalId = tratamentoDto.getProfissionalId();
        Optional<ProfissionalEntity> optionalProfissional = profissionalRepository.findById(profissionalId);
        if (optionalProfissional.isEmpty()) {
            throw new EntityNotFoundException("Não é possível criar o tratamento pois o profissional com ID " + profissionalId + " não foi encontrado.");
        }
        ProfissionalEntity profissional = optionalProfissional.get();

        // Validação do conteúdo teste (se fornecido)
        ConteudoTesteEntity conteudoTeste = null;
        if (tratamentoDto.getConteudoTesteId() != null) {
            Optional<ConteudoTesteEntity> optionalConteudo = conteudoTesteRepository.findById(tratamentoDto.getConteudoTesteId());
            if (optionalConteudo.isEmpty()) {
                throw new EntityNotFoundException("Conteúdo de teste não encontrado com o ID: " + tratamentoDto.getConteudoTesteId());
            }
            conteudoTeste = optionalConteudo.get();
        }

        TratamentoEntity entity = new TratamentoEntity();
        aplicarDtoNoEntity(entity, tratamentoDto);
        entity.setProfissional(profissional);
        entity.setConteudoTeste(conteudoTeste);

        TratamentoEntity salvo = tratamentoRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todos os tratamentos e retorna lista de DTOs de saída.
     */
    public List<TratamentoDtoOut> buscarTodos() {
        return tratamentoRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um tratamento pelo seu ID e retorna DTO de saída.
     */
    public Optional<TratamentoDtoOut> buscarPorId(Long id) {
        return tratamentoRepository.findById(id).map(this::toDto);
    }
    
    /**
     * Busca todos os tratamentos de um profissional específico.
     */
    public List<TratamentoDtoOut> buscarPorProfissionalId(Long profissionalId) {
        return tratamentoRepository.findByProfissionalId(profissionalId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza os dados de um tratamento existente via DTO In e retorna DTO Out.
     */
    public TratamentoDtoOut atualizar(Long id, TratamentoDtoIn tratamentoDto) {
        Optional<TratamentoEntity> optionalTratamento = tratamentoRepository.findById(id);
        if (optionalTratamento.isEmpty()) {
            throw new EntityNotFoundException("Tratamento não encontrado com o ID: " + id);
        }

        validarTratamentoDto(tratamentoDto);

        TratamentoEntity existente = optionalTratamento.get();
        aplicarDtoNoEntity(existente, tratamentoDto);
        // Não permitimos alterar profissional e conteudoTeste no update

        TratamentoEntity atualizado = tratamentoRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um tratamento.
     */
    public void deletar(Long id) {
        if (!tratamentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Tratamento não encontrado com o ID: " + id);
        }
        
        tratamentoRepository.deleteById(id);
    }

    /**
     * Busca tratamentos por tipo (ignorando maiúsculas/minúsculas).
     */
    public List<TratamentoDtoOut> buscarPorTipo(String tipoTratamento) {
        if (tipoTratamento == null || tipoTratamento.isBlank()) {
            throw new IllegalArgumentException("O tipo de tratamento não pode ser vazio.");
        }
        return tratamentoRepository.findByTipoTratamentoIgnoreCase(tipoTratamento)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca tratamentos por tipo e quantidade mínima.
     */
    public List<TratamentoDtoOut> buscarPorTipoEQuantidadeMinima(String tipo, Integer quantidade) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("O tipo de tratamento não pode ser vazio.");
        }
        if (quantidade == null || quantidade < 0) {
            throw new IllegalArgumentException("A quantidade mínima deve ser não negativa.");
        }
        return tratamentoRepository.findByTipoTratamentoAndQuantidadeDiaGreaterThanEqual(tipo, quantidade)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private TratamentoDtoOut toDto(TratamentoEntity entity) {
        Long conteudoTesteId = (entity.getConteudoTeste() != null)
                ? entity.getConteudoTeste().getId()
                : null;

        TratamentoDtoOut dto = new TratamentoDtoOut();
        dto.setId(entity.getId());
        dto.setQuantidadeDia(entity.getQuantidadeDia());
        dto.setTipoTratamento(entity.getTipoTratamento());
        dto.setPersonalizado(entity.getPersonalizado());
        dto.setConteudoTesteId(conteudoTesteId);
        dto.setProfissionalId(entity.getProfissional().getId());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     * Não altera profissional e conteudoTeste no update.
     */
    private void aplicarDtoNoEntity(TratamentoEntity destino, TratamentoDtoIn fonte) {
        destino.setQuantidadeDia(fonte.getQuantidadeDia());
        destino.setTipoTratamento(fonte.getTipoTratamento());
        destino.setPersonalizado(fonte.getPersonalizado());
        // Nota: profissionalId e conteudoTesteId não são atualizados após criação
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarTratamentoDto(TratamentoDtoIn tratamento) {
        if (tratamento == null) {
            throw new IllegalArgumentException("O objeto de tratamento não pode ser nulo.");
        }
        if (tratamento.getProfissionalId() == null) {
            throw new IllegalArgumentException("O tratamento deve estar associado a um profissional.");
        }
        if (tratamento.getTipoTratamento() == null || tratamento.getTipoTratamento().isBlank()) {
            throw new IllegalArgumentException("O tipo de tratamento é obrigatório.");
        }
        if (tratamento.getQuantidadeDia() == null || tratamento.getQuantidadeDia() <= 0) {
            throw new IllegalArgumentException("A quantidade de dias deve ser um número positivo.");
        }
    }
}