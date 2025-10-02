package com.inatel.prototipo_ia.service;

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

    // --- Métodos de Criação (CREATE) ---

    /**
     * Cria um novo chat após validar os participantes.
     * @param chat O objeto de chat a ser criado.
     * @return O chat salvo com seu ID.
     */
    public ChatEntity criar(ChatEntity chat) {
        // 1. Validação da ESTRUTURA do objeto (o "Porteiro")
        validarDadosDoChat(chat);

        // 2. Validação da INTEGRIDADE dos dados (o "Verificador de Sistema")
        ClienteEntity cliente = clienteRepository.findById(chat.getCliente().getId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com o ID: " + chat.getCliente().getId()));

        ProfissionalEntity profissional = profissionalRepository.findById(chat.getProfissional().getId())
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado com o ID: " + chat.getProfissional().getId()));
        
        // 3. Garante que a entidade a ser salva está ligada às entidades do banco
        chat.setCliente(cliente);
        chat.setProfissional(profissional);

        // 4. Salva no banco de dados
        return chatRepository.save(chat);
    }


    // --- Métodos Privados de Validação ---

    /**
     * Valida os dados básicos de um objeto ChatEntity.
     * @param chat O chat a ser validado.
     */
    private void validarDadosDoChat(ChatEntity chat) {
        if (chat == null) {
            throw new IllegalArgumentException("O objeto de chat não pode ser nulo.");
        }
        if (chat.getCliente() == null || chat.getCliente().getId() == null) {
            throw new IllegalArgumentException("O cliente associado ao chat é obrigatório.");
        }
        if (chat.getProfissional() == null || chat.getProfissional().getId() == null) {
            throw new IllegalArgumentException("O profissional associado ao chat é obrigatório.");
        }
    }
}