package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ChatDtoIn;
import com.inatel.prototipo_ia.dto.out.ChatDtoOut;
import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public ChatService(ChatRepository chatRepository, ClienteRepository clienteRepository, ProfissionalRepository profissionalRepository) {
        this.chatRepository = chatRepository;
        this.clienteRepository = clienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    /**
     * Cria um novo chat a partir de DTO In e retorna DTO Out.
     */
    public ChatDtoOut criar(ChatDtoIn chatDto) {
        validarChatDto(chatDto);

        // Validação da integridade do cliente
        Optional<ClienteEntity> optionalCliente = clienteRepository.findById(chatDto.getClienteId());
        if (optionalCliente.isEmpty()) {
            throw new EntityNotFoundException("Cliente não encontrado com o ID: " + chatDto.getClienteId());
        }
        ClienteEntity cliente = optionalCliente.get();

        // Validação da integridade do profissional
        Optional<ProfissionalEntity> optionalProfissional = profissionalRepository.findById(chatDto.getProfissionalId());
        if (optionalProfissional.isEmpty()) {
            throw new EntityNotFoundException("Profissional não encontrado com o ID: " + chatDto.getProfissionalId());
        }
        ProfissionalEntity profissional = optionalProfissional.get();
        
        ChatEntity entity = new ChatEntity();
        aplicarDtoNoEntity(entity, chatDto);
        entity.setCliente(cliente);
        entity.setProfissional(profissional);

        ChatEntity salvo = chatRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Atualiza um chat existente via DTO In e retorna DTO Out.
     */
    public ChatDtoOut atualizar(Long id, ChatDtoIn chatDto) {
        Optional<ChatEntity> optionalChat = chatRepository.findById(id);

        if (optionalChat.isEmpty()) {
            throw new EntityNotFoundException("Chat não encontrado com o ID: " + id);
        }

        validarChatDto(chatDto);

        ChatEntity existente = optionalChat.get();
        aplicarDtoNoEntity(existente, chatDto);

        ChatEntity atualizado = chatRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um chat pelo seu ID.
     */
    public void deletar(Long id) {
        if (!chatRepository.existsById(id)) {
            throw new EntityNotFoundException("Chat não encontrado com o ID: " + id);
        }
        
        chatRepository.deleteById(id);
    }

    /**
     * Busca todos os chats cadastrados e retorna lista de DTOs de saída.
     */
    public List<ChatDtoOut> buscarTodos() {
        return chatRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um chat específico pelo seu ID e retorna DTO de saída.
     */
    public Optional<ChatDtoOut> buscarPorId(Long id) {
        return chatRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca todos os chats de um cliente específico.
     */
    public List<ChatDtoOut> buscarPorClienteId(Long clienteId) {
        return chatRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos os chats de um profissional específico.
     */
    public List<ChatDtoOut> buscarPorProfissionalId(Long profissionalId) {
        return chatRepository.findByProfissionalId(profissionalId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca chats com duração maior que um valor especificado.
     */
    public List<ChatDtoOut> buscarComDuracaoMaiorQue(Integer minutos) {
        if (minutos == null || minutos < 0) {
            throw new IllegalArgumentException("A duração em minutos deve ser um número positivo.");
        }
        return chatRepository.findByDuracaoGreaterThan(minutos)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca chats longos (usando query customizada do repository).
     */
    public List<ChatDtoOut> buscarChatsLongos() {
        return chatRepository.findChatsLongos()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private ChatDtoOut toDto(ChatEntity entity) {
        Long relatorioId = (entity.getRelatorio() != null) 
                ? entity.getRelatorio().getId() 
                : null;

        ChatDtoOut dto = new ChatDtoOut();
        dto.setId(entity.getId());
        dto.setDuracao(entity.getDuracao());
        dto.setConversa(entity.getConversa());
        dto.setClienteId(entity.getCliente().getId());
        dto.setProfissionalId(entity.getProfissional().getId());
        dto.setRelatorioId(relatorioId);
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     * Não altera cliente e profissional no update.
     */
    private void aplicarDtoNoEntity(ChatEntity destino, ChatDtoIn fonte) {
        destino.setDuracao(fonte.getDuracao());
        destino.setConversa(fonte.getConversa());
        // Nota: clienteId e profissionalId não são atualizados após criação
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarChatDto(ChatDtoIn chat) {
        if (chat == null) {
            throw new IllegalArgumentException("O objeto de chat não pode ser nulo.");
        }
        if (chat.getClienteId() == null) {
            throw new IllegalArgumentException("O cliente associado ao chat é obrigatório.");
        }
        if (chat.getProfissionalId() == null) {
            throw new IllegalArgumentException("O profissional associado ao chat é obrigatório.");
        }
        if (chat.getDuracao() != null && chat.getDuracao() < 0) {
            throw new IllegalArgumentException("A duração não pode ser negativa.");
        }
    }
}