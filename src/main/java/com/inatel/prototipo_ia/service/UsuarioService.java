package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.UsuarioDtoIn;
import com.inatel.prototipo_ia.dto.out.UsuarioDtoOut;
import com.inatel.prototipo_ia.entity.UsuarioEntity;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, ProfissionalRepository profissionalRepository) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    /**
     * Cria um novo usuário a partir de DTO In e retorna DTO Out.
     */
    public UsuarioDtoOut criar(UsuarioDtoIn usuarioDto) {
        validarUsuarioDto(usuarioDto);
        
        UsuarioEntity entity = new UsuarioEntity();
        aplicarDtoNoEntity(entity, usuarioDto);
        
        UsuarioEntity salvo = usuarioRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todos os usuários e retorna lista de DTOs de saída.
     */
    public List<UsuarioDtoOut> buscarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um usuário pelo ID e retorna DTO de saída.
     */
    public Optional<UsuarioDtoOut> buscarPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toDto);
    }
    
    /**
     * Busca usuários por nome (busca parcial, ignora maiúsculas/minúsculas).
     */
    public List<UsuarioDtoOut> buscarPorNome(String nome) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza um usuário existente via DTO In e retorna DTO Out.
     */
    public UsuarioDtoOut atualizar(Long id, UsuarioDtoIn usuarioDto) {
        Optional<UsuarioEntity> optionalUsuario = usuarioRepository.findById(id);

        if (optionalUsuario.isEmpty()) {
            throw new EntityNotFoundException("Usuário não encontrado com o ID: " + id);
        }

        validarUsuarioDto(usuarioDto);

        UsuarioEntity existente = optionalUsuario.get();
        aplicarDtoNoEntity(existente, usuarioDto);

        UsuarioEntity atualizado = usuarioRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um usuário, se ele não estiver associado a um cliente ou profissional.
     */
    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado com o ID: " + id);
        }

        if (clienteRepository.existsById(id) || profissionalRepository.existsById(id)) {
            throw new IllegalStateException("Não é possível deletar o usuário pois ele está associado a um cliente ou profissional.");
        }

        usuarioRepository.deleteById(id);
    }

    /**
     * Busca usuários por idade específica.
     */
    public List<UsuarioDtoOut> buscarPorIdade(Integer idade) {
        if (idade == null || idade < 0) {
            throw new IllegalArgumentException("A idade deve ser não negativa.");
        }
        return usuarioRepository.findByIdade(idade)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private UsuarioDtoOut toDto(UsuarioEntity entity) {
        UsuarioDtoOut dto = new UsuarioDtoOut();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setIdade(entity.getIdade());
        dto.setEndereco(entity.getEndereco());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     */
    private void aplicarDtoNoEntity(UsuarioEntity destino, UsuarioDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setIdade(fonte.getIdade());
        destino.setEndereco(fonte.getEndereco());
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarUsuarioDto(UsuarioDtoIn usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O objeto de usuário não pode ser nulo.");
        }
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        if (usuario.getIdade() != null && usuario.getIdade() < 0) {
            throw new IllegalArgumentException("A idade do usuário não pode ser negativa.");
        }
    }
}