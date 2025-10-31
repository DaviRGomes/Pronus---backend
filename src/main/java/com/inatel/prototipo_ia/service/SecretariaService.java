package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.SecretariaDtoIn;
import com.inatel.prototipo_ia.dto.out.SecretariaDtoOut;
import com.inatel.prototipo_ia.entity.SecretariaEntity;
import com.inatel.prototipo_ia.repository.SecretariaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SecretariaService {

    private final SecretariaRepository secretariaRepository;

    public SecretariaService(SecretariaRepository secretariaRepository) {
        this.secretariaRepository = secretariaRepository;
    }

    /**
     * Cria uma nova secretária a partir de DTO In e retorna DTO Out.
     */
    public SecretariaDtoOut criar(SecretariaDtoIn secretariaDto) {
        validarSecretariaDto(secretariaDto);

        // Validação de email único
        if (secretariaRepository.existsByEmail(secretariaDto.getEmail())) {
            throw new IllegalStateException("Já existe uma secretária cadastrada com o email: " + secretariaDto.getEmail());
        }

        SecretariaEntity entity = new SecretariaEntity();
        aplicarDtoNoEntity(entity, secretariaDto);

        SecretariaEntity salvo = secretariaRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todas as secretárias e retorna lista de DTOs de saída.
     */
    public List<SecretariaDtoOut> buscarTodos() {
        return secretariaRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma secretária pelo seu ID e retorna DTO de saída.
     */
    public Optional<SecretariaDtoOut> buscarPorId(Long id) {
        return secretariaRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca uma secretária por email.
     */
    public Optional<SecretariaDtoOut> buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email não pode ser vazio.");
        }
        return secretariaRepository.findByEmail(email).map(this::toDto);
    }

    /**
     * Busca secretárias por nome (busca parcial).
     */
    public List<SecretariaDtoOut> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
        return secretariaRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca secretárias maiores de idade.
     */
    public List<SecretariaDtoOut> buscarMaioresDeIdade() {
        return secretariaRepository.findSecretariasMaioresDeIdade()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza os dados de uma secretária existente via DTO In e retorna DTO Out.
     */
    public SecretariaDtoOut atualizar(Long id, SecretariaDtoIn secretariaDto) {
        Optional<SecretariaEntity> optionalSecretaria = secretariaRepository.findById(id);
        if (optionalSecretaria.isEmpty()) {
            throw new EntityNotFoundException("Secretária não encontrada com o ID: " + id);
        }

        validarSecretariaDto(secretariaDto);

        SecretariaEntity existente = optionalSecretaria.get();

        // Validação de email único (se estiver sendo alterado)
        if (!existente.getEmail().equals(secretariaDto.getEmail())) {
            if (secretariaRepository.existsByEmail(secretariaDto.getEmail())) {
                throw new IllegalStateException("Já existe uma secretária cadastrada com o email: " + secretariaDto.getEmail());
            }
        }

        aplicarDtoNoEntity(existente, secretariaDto);

        SecretariaEntity atualizado = secretariaRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta uma secretária.
     */
    public void deletar(Long id) {
        if (!secretariaRepository.existsById(id)) {
            throw new EntityNotFoundException("Secretária não encontrada com o ID: " + id);
        }
        secretariaRepository.deleteById(id);
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private SecretariaDtoOut toDto(SecretariaEntity entity) {
        SecretariaDtoOut dto = new SecretariaDtoOut();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setIdade(entity.getIdade());
        dto.setEndereco(entity.getEndereco());
        dto.setEmail(entity.getEmail());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     */
    private void aplicarDtoNoEntity(SecretariaEntity destino, SecretariaDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setIdade(fonte.getIdade());
        destino.setEndereco(fonte.getEndereco());
        destino.setEmail(fonte.getEmail());
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarSecretariaDto(SecretariaDtoIn secretaria) {
        if (secretaria == null) {
            throw new IllegalArgumentException("O objeto de secretária não pode ser nulo.");
        }
        if (secretaria.getNome() == null || secretaria.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da secretária é obrigatório.");
        }
        if (secretaria.getEmail() == null || secretaria.getEmail().isBlank()) {
            throw new IllegalArgumentException("O email é obrigatório.");
        }
        if (!secretaria.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("O email fornecido não é válido.");
        }
        if (secretaria.getIdade() != null && secretaria.getIdade() < 0) {
            throw new IllegalArgumentException("A idade não pode ser negativa.");
        }
    }
}