package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.entity.TratamentoEntity;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.repository.TratamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TratamentoService {

    private final TratamentoRepository tratamentoRepository;
    private final ProfissionalRepository profissionalRepository;

    public TratamentoService(TratamentoRepository tratamentoRepository, ProfissionalRepository profissionalRepository) {
        this.tratamentoRepository = tratamentoRepository;
        this.profissionalRepository = profissionalRepository;
    }

    /**
     * Cria um novo tratamento, associando um profissional.
     */
    public TratamentoEntity criar(TratamentoEntity tratamento) {
        validarTratamento(tratamento);

        Long profissionalId = tratamento.getProfissional().getId();

        // Verificando o profissional
        if (!profissionalRepository.existsById(profissionalId)) {
            throw new EntityNotFoundException("Não é possível criar o tratamento pois o profissional com ID " + profissionalId + " não foi encontrado.");
        }

        return tratamentoRepository.save(tratamento);
    }

    /**
     * Busca todos os tratamentos.
     */
    public List<TratamentoEntity> buscarTodos() {
        return tratamentoRepository.findAll();
    }

    /**
     * Busca um tratamento pelo seu ID.
     */
    public Optional<TratamentoEntity> buscarPorId(Long id) {
        return tratamentoRepository.findById(id);
    }
    
    /**
     * Busca todos os tratamentos de um profissional específico.
     */
    public List<TratamentoEntity> buscarPorProfissionalId(Long profissionalId) {
        return tratamentoRepository.findByProfissionalId(profissionalId);
    }

    /**
     * Atualiza os dados de um tratamento existente.
     */
    public TratamentoEntity atualizar(Long id, TratamentoEntity tratamentoAtualizado) {
        Optional<TratamentoEntity> optionalTratamento = tratamentoRepository.findById(id);
        if (optionalTratamento.isEmpty()) {
            throw new EntityNotFoundException("Tratamento não encontrado com o ID: " + id);
        }

        TratamentoEntity tratamentoExistente = optionalTratamento.get();
        validarTratamento(tratamentoAtualizado);
        
        tratamentoExistente.setTipoTratamento(tratamentoAtualizado.getTipoTratamento());
        tratamentoExistente.setQuantidadeDia(tratamentoAtualizado.getQuantidadeDia());

        return tratamentoRepository.save(tratamentoExistente);
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
     * Valida os campos do objeto Tratamento.
     */
    private void validarTratamento(TratamentoEntity tratamento) {
        if (tratamento == null) {
            throw new IllegalArgumentException("O objeto de tratamento não pode ser nulo.");
        }
        if (tratamento.getProfissional() == null || tratamento.getProfissional().getId() == null) {
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