package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ChatDtoIn;
import com.inatel.prototipo_ia.dto.out.ChatDtoOut;
import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.EspecialistaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final ClienteRepository clienteRepository;
    private final EspecialistaRepository especialistaRepository;

    public ChatService(ChatRepository chatRepository, 
                       ClienteRepository clienteRepository, 
                       EspecialistaRepository especialistaRepository) {
        this.chatRepository = chatRepository;
        this.clienteRepository = clienteRepository;
        this.especialistaRepository = especialistaRepository;
    }

    public ChatDtoOut criar(ChatDtoIn chatDto) {
        validarChatDto(chatDto);

        // --- CORREÇÃO: VERIFICAR SE JÁ EXISTE ---
        // Antes de criar, verificamos se esse cliente já tem chat com esse especialista
        List<ChatEntity> chatsExistentes = chatRepository.findByClienteId(chatDto.getClienteId());
        
        // Filtra para ver se é com o mesmo especialista (caso tenha mais de um)
        Optional<ChatEntity> chatJaExistente = chatsExistentes.stream()
            .filter(c -> c.getEspecialista().getId().equals(chatDto.getEspecialistaId()))
            .findFirst();

        if (chatJaExistente.isPresent()) {
            // Se já existe, retorna ele mesmo em vez de criar um novo!
            return toDto(chatJaExistente.get());
        }
        // ----------------------------------------

        ClienteEntity cliente = clienteRepository.findById(chatDto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + chatDto.getClienteId()));

        EspecialistaEntity especialista = especialistaRepository.findById(chatDto.getEspecialistaId())
                .orElseThrow(() -> new EntityNotFoundException("Especialista não encontrado: " + chatDto.getEspecialistaId()));
        
        ChatEntity entity = new ChatEntity();
        entity.setDuracao(chatDto.getDuracao());
        entity.setConversa(chatDto.getConversa());
        entity.setCliente(cliente);
        entity.setEspecialista(especialista);

        ChatEntity salvo = chatRepository.save(entity);
        return toDto(salvo);
    }

    // ... (Mantenha o restante dos métodos IGUAIS: deletar, buscarTodos, etc.) ...
    
    public void deletar(Long id) {
        if (!chatRepository.existsById(id)) throw new EntityNotFoundException("Chat não encontrado: " + id);
        chatRepository.deleteById(id);
    }

    public List<ChatDtoOut> buscarTodos() {
        return chatRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<ChatDtoOut> buscarPorId(Long id) {
        return chatRepository.findById(id).map(this::toDto);
    }

    public List<ChatDtoOut> buscarPorClienteId(Long clienteId) {
        return chatRepository.findByClienteId(clienteId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ChatDtoOut> buscarPorEspecialistaId(Long especialistaId) {
        return chatRepository.findByEspecialistaId(especialistaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<ChatDtoOut> buscarChatsLongos() {
        return chatRepository.findChatsLongos().stream().map(this::toDto).collect(Collectors.toList());
    }
    
    // Método antigo mantido para compatibilidade se houver chamada
    public List<ChatDtoOut> buscarComDuracaoMaiorQue(Integer minutos) {
         // Implementação opcional ou usar query do repository
         return List.of(); 
    }
    
    public ChatDtoOut atualizar(Long id, ChatDtoIn chatDto) {
        ChatEntity entity = chatRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Chat não encontrado: " + id));
        entity.setDuracao(chatDto.getDuracao());
        entity.setConversa(chatDto.getConversa());
        return toDto(chatRepository.save(entity));
    }

    private ChatDtoOut toDto(ChatEntity entity) {
        Long relatorioId = (entity.getRelatorio() != null) ? entity.getRelatorio().getId() : null;

        ChatDtoOut dto = new ChatDtoOut();
        dto.setId(entity.getId());
        dto.setDuracao(entity.getDuracao());
        dto.setConversa(entity.getConversa());
        dto.setClienteId(entity.getCliente().getId());
        dto.setProfissionalId(entity.getEspecialista().getId());
        dto.setRelatorioId(relatorioId);
        return dto;
    }

    private void validarChatDto(ChatDtoIn chat) {
        if (chat == null) throw new IllegalArgumentException("chat nulo");
        if (chat.getClienteId() == null) throw new IllegalArgumentException("cliente obrigatório");
        if (chat.getEspecialistaId() == null) throw new IllegalArgumentException("Especialista obrigatório");
        if (chat.getDuracao() != null && chat.getDuracao() < 0) throw new IllegalArgumentException("duração inválida");
    }
}
