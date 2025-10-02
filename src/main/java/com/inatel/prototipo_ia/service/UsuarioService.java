package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.entity.UsuarioEntity;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
     * Cria um novo usuário.
     */
    public UsuarioEntity criar(UsuarioEntity usuario) {
        validarUsuario(usuario);
        return usuarioRepository.save(usuario);
    }

    /**
     * Busca todos os usuários.
     */
    public List<UsuarioEntity> buscarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca um usuário pelo ID.
     */
    public Optional<UsuarioEntity> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Busca usuários por nome (busca parcial, ignora maiúsculas/minúsculas).
     */
    public List<UsuarioEntity> buscarPorNome(String nome) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Atualiza um usuário existente.
     */
    public UsuarioEntity atualizar(Long id, UsuarioEntity usuarioAtualizado) {

        Optional<UsuarioEntity> optionalUsuario = usuarioRepository.findById(id);

        if (optionalUsuario.isEmpty()) {
            throw new EntityNotFoundException("Usuário não encontrado com o ID: " + id);
        }

        UsuarioEntity usuarioExistente = optionalUsuario.get();
        
        // Valida os dados novos antes de atribuir
        validarUsuario(usuarioAtualizado);

        // Atualiza os campos do objeto existente
        usuarioExistente.setNome(usuarioAtualizado.getNome());
        usuarioExistente.setIdade(usuarioAtualizado.getIdade());
        usuarioExistente.setEndereco(usuarioAtualizado.getEndereco());

        return usuarioRepository.save(usuarioExistente);
    }

    /**
     * Deleta um usuário, se ele não estiver associado a um cliente ou profissional.
     */
    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado com o ID: " + id);
        }

        // Checa se o usuário está em uso.
        if (clienteRepository.existsById(id) || profissionalRepository.existsById(id)) {
            throw new IllegalStateException("Não é possível deletar o usuário pois ele está associado a um cliente ou profissional.");
        }

        usuarioRepository.deleteById(id);
    }

    private void validarUsuario(UsuarioEntity usuario) {
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