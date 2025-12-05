package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.SecretariaDtoIn;
import com.inatel.prototipo_ia.dto.out.SecretariaDtoOut;
import com.inatel.prototipo_ia.entity.SecretariaEntity;
import com.inatel.prototipo_ia.repository.SecretariaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // Importante
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SecretariaService {

    private final SecretariaRepository secretariaRepository;
    private final com.inatel.prototipo_ia.repository.UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Campo declarado

    // --- CONSTRUTOR CORRIGIDO (INJETANDO O PASSWORD ENCODER) ---
    public SecretariaService(SecretariaRepository secretariaRepository, com.inatel.prototipo_ia.repository.UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.secretariaRepository = secretariaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SecretariaDtoOut criar(SecretariaDtoIn secretariaDto) {
        validarSecretariaDto(secretariaDto);

        if (secretariaRepository.existsByEmail(secretariaDto.getEmail())) {
            throw new IllegalStateException("Já existe uma secretária cadastrada com o email: " + secretariaDto.getEmail());
        }

        if (secretariaDto.getLogin() != null && usuarioRepository.findByLogin(secretariaDto.getLogin()) != null) {
            throw new IllegalStateException("Login já cadastrado");
        }

        SecretariaEntity entity = new SecretariaEntity();
        aplicarDtoNoEntity(entity, secretariaDto);

        if (secretariaDto.getLogin() != null) {
            entity.setLogin(secretariaDto.getLogin());
        }
        if (secretariaDto.getSenha() != null && !secretariaDto.getSenha().isBlank()) {
            entity.setSenha(passwordEncoder.encode(secretariaDto.getSenha()));
        }

        SecretariaEntity salvo = secretariaRepository.save(entity);
        return toDto(salvo);
    }

    // ... (Mantenha o restante dos seus métodos buscarTodos, buscarPorId, etc.)
    
    // --- MÉTODOS AUXILIARES (Essenciais para o código acima funcionar) ---
    private SecretariaDtoOut toDto(SecretariaEntity entity) {
        SecretariaDtoOut dto = new SecretariaDtoOut();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setIdade(entity.getIdade());
        dto.setEndereco(entity.getEndereco());
        dto.setEmail(entity.getEmail());
        return dto;
    }

    private void aplicarDtoNoEntity(SecretariaEntity destino, SecretariaDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setIdade(fonte.getIdade());
        destino.setEndereco(fonte.getEndereco());
        destino.setEmail(fonte.getEmail());
    }
    
    private void validarSecretariaDto(SecretariaDtoIn dto) {
        if (dto == null) throw new IllegalArgumentException("DTO nulo");
        if (dto.getNome() == null || dto.getNome().isBlank()) throw new IllegalArgumentException("nome obrigatório");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw new IllegalArgumentException("email obrigatório");
        if (!dto.getEmail().contains("@")) throw new IllegalArgumentException("email deve ser válido");
        if (dto.getIdade() != null && dto.getIdade() < 0) throw new IllegalArgumentException("idade inválida");
    }
    
    // Métodos de busca para evitar erros de compilação no Controller
    public List<SecretariaDtoOut> buscarTodos() {
        return secretariaRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }
    public Optional<SecretariaDtoOut> buscarPorId(Long id) {
        return secretariaRepository.findById(id).map(this::toDto);
    }
    public Optional<SecretariaDtoOut> buscarPorEmail(String email) {
        return secretariaRepository.findByEmail(email).map(this::toDto);
    }
    public List<SecretariaDtoOut> buscarPorNome(String nome) {
        return secretariaRepository.findByNomeContainingIgnoreCase(nome).stream().map(this::toDto).collect(Collectors.toList());
    }
    public List<SecretariaDtoOut> buscarMaioresDeIdade() {
        return secretariaRepository.findSecretariasMaioresDeIdade().stream().map(this::toDto).collect(Collectors.toList());
    }
    public SecretariaDtoOut atualizar(Long id, SecretariaDtoIn dto) {
        Optional<SecretariaEntity> op = secretariaRepository.findById(id);
        if (op.isEmpty()) throw new EntityNotFoundException("ID não encontrado: " + id);
        SecretariaEntity entity = op.get();

        String novoEmail = dto.getEmail();
        if (novoEmail != null && !novoEmail.equalsIgnoreCase(entity.getEmail())) {
            if (secretariaRepository.existsByEmail(novoEmail)) {
                throw new IllegalStateException("Já existe uma secretária cadastrada com o email: " + novoEmail);
            }
        }

        aplicarDtoNoEntity(entity, dto);
        return toDto(secretariaRepository.save(entity));
    }
    public void deletar(Long id) {
        if (!secretariaRepository.existsById(id)) throw new EntityNotFoundException("ID não encontrado: " + id);
        secretariaRepository.deleteById(id);
    }
}
