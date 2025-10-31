package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ClienteDtoIn;
import com.inatel.prototipo_ia.dto.out.ClienteDtoOut;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public ClienteService(ClienteRepository clienteRepository, ChatRepository chatRepository) {
        this.clienteRepository = clienteRepository;
        this.chatRepository = chatRepository;
    }

    /**
     * Cria um novo cliente a partir de DTO In e retorna DTO Out.
     */
    public ClienteDtoOut salvar(ClienteDtoIn clienteCreate) {
        validarClienteDto(clienteCreate);

        ClienteEntity novo = new ClienteEntity();
        aplicarDtoNoEntity(novo, clienteCreate);

        ClienteEntity salvo = clienteRepository.save(novo);
        return toDto(salvo);
    }

    /**
     * Busca todos os clientes e retorna lista de DTOs de saída.
     */
    public List<ClienteDtoOut> buscarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um cliente pelo seu ID e retorna DTO de saída.
     */
    public Optional<ClienteDtoOut> buscarPorId(Long id) {
        return clienteRepository.findById(id).map(this::toDto);
    }

    /**
     * Atualiza os dados de um cliente (via DTO In) e retorna DTO Out.
     */
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

    /**
     * Deleta um cliente, se ele não estiver participando de nenhum chat.
     */
    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente não encontrado com o ID: " + id);
        }

        if (chatRepository.existsByClienteId(id)) {
            throw new IllegalStateException("Não é possível deletar o cliente pois ele está associado a um ou mais chats.");
        }

        clienteRepository.deleteById(id);
    }

    /**
     * Busca clientes maiores de idade e retorna DTO Out.
     */
    public List<ClienteDtoOut> buscarMaioresDeIdade() {
        return clienteRepository.findClientesMaioresDeIdade()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca clientes por nível e retorna DTO Out.
     */
    public List<ClienteDtoOut> buscarPorNivel(String nivel) {
        if (nivel == null || nivel.isBlank()) {
            throw new IllegalArgumentException("O nível não pode ser vazio.");
        }
        return clienteRepository.findByNivel(nivel)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private ClienteDtoOut toDto(ClienteEntity cliente) {
        List<Long> chatIds = (cliente.getChats() != null)
                ? cliente.getChats().stream().map(c -> c.getId()).collect(Collectors.toList())
                : null;

        ClienteDtoOut dto = new ClienteDtoOut();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setIdade(cliente.getIdade());
        dto.setEndereco(cliente.getEndereco());
        dto.setNivel(cliente.getNivel());
        dto.setChatIds(chatIds);
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     */
    private void aplicarDtoNoEntity(ClienteEntity destino, ClienteDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setIdade(fonte.getIdade());
        destino.setEndereco(fonte.getEndereco());
        destino.setNivel(fonte.getNivel());
    }

    /**
     * Validação simples do DTO de entrada.
     */
    private void validarClienteDto(ClienteDtoIn cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O objeto de cliente não pode ser nulo.");
        }
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do cliente/usuário é obrigatório.");
        }
    }
}
