package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ClienteDtoIn;
import com.inatel.prototipo_ia.dto.out.ClienteDtoOut;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORTANTE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ChatRepository chatRepository;
    private final com.inatel.prototipo_ia.repository.ConsultaRepository consultaRepository;
    private final com.inatel.prototipo_ia.repository.CertificadoRepository certificadoRepository;
    private final com.inatel.prototipo_ia.repository.UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // IMPORTANTE

    // Construtor atualizado para injetar o PasswordEncoder
    public ClienteService(ClienteRepository clienteRepository, 
                          ChatRepository chatRepository,
                          com.inatel.prototipo_ia.repository.ConsultaRepository consultaRepository,
                          com.inatel.prototipo_ia.repository.CertificadoRepository certificadoRepository,
                          com.inatel.prototipo_ia.repository.UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.chatRepository = chatRepository;
        this.consultaRepository = consultaRepository;
        this.certificadoRepository = certificadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria um novo cliente a partir de DTO In e retorna DTO Out.
     */
    public ClienteDtoOut salvar(ClienteDtoIn clienteCreate) {
        validarClienteDto(clienteCreate);

        if (clienteCreate.getLogin() != null && usuarioRepository.findByLogin(clienteCreate.getLogin()) != null) {
            throw new IllegalStateException("Login já cadastrado");
        }

        ClienteEntity novo = new ClienteEntity();
        aplicarDtoNoEntity(novo, clienteCreate);

        // --- CORREÇÃO DO ERRO 500 AQUI ---
        // Agora pegamos o login e senha do DTO e salvamos na Entidade
        novo.setLogin(clienteCreate.getLogin());
        
        // Criptografamos a senha antes de salvar
        if (clienteCreate.getSenha() != null) {
            novo.setSenha(passwordEncoder.encode(clienteCreate.getSenha()));
        }
        // --------------------------------

        ClienteEntity salvo = clienteRepository.save(novo);
        return toDto(salvo);
    }

    public List<ClienteDtoOut> buscarTodos() {
        return clienteRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<ClienteDtoOut> buscarPorId(Long id) {
        return clienteRepository.findById(id).map(this::toDto);
    }

    public ClienteDtoOut atualizar(Long id, ClienteDtoIn clienteAtualizado) {
        Optional<ClienteEntity> optionalCliente = clienteRepository.findById(id);
        if (optionalCliente.isEmpty()) {
            throw new EntityNotFoundException("Cliente não encontrado com o ID: " + id);
        }
        validarClienteDto(clienteAtualizado);
        ClienteEntity existente = optionalCliente.get();
        aplicarDtoNoEntity(existente, clienteAtualizado);
        ClienteEntity atualizado = clienteRepository.save(existente);
        return toDto(atualizado);
    }

    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente não encontrado com o ID: " + id);
        }
        if (chatRepository.existsByClienteId(id)) {
            throw new IllegalStateException("Não é possível deletar o cliente pois ele está associado a um ou mais chats.");
        }
        clienteRepository.deleteById(id);
    }

    public List<ClienteDtoOut> buscarMaioresDeIdade() {
        return clienteRepository.findClientesMaioresDeIdade().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ClienteDtoOut> buscarPorNivel(String nivel) {
        if (nivel == null || nivel.isBlank()) {
            throw new IllegalArgumentException("O nível não pode ser vazio.");
        }
        return clienteRepository.findByNivel(nivel).stream().map(this::toDto).collect(Collectors.toList());
    }

    private ClienteDtoOut toDto(ClienteEntity cliente) {
        List<Long> chatIds = chatRepository.findByClienteId(cliente.getId()).stream().map(c -> c.getId()).collect(Collectors.toList());
        List<Long> consultaIds = consultaRepository.findByClienteId(cliente.getId()).stream().map(c -> c.getId()).collect(Collectors.toList());
        List<Long> certificadoIds = certificadoRepository.findByClienteId(cliente.getId()).stream().map(c -> c.getId()).collect(Collectors.toList());
        ClienteDtoOut dto = new ClienteDtoOut();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setIdade(cliente.getIdade());
        dto.setEndereco(cliente.getEndereco());
        dto.setNivel(cliente.getNivel());
        dto.setChatIds(chatIds);
        dto.setConsultaIds(consultaIds);
        dto.setCertificadoIds(certificadoIds);
        return dto;
    }

    private void aplicarDtoNoEntity(ClienteEntity destino, ClienteDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setIdade(fonte.getIdade());
        destino.setEndereco(fonte.getEndereco());
        destino.setNivel(fonte.getNivel());
    }

    private void validarClienteDto(ClienteDtoIn cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O objeto de cliente não pode ser nulo.");
        }
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do cliente/usuário é obrigatório.");
        }
    }
}
