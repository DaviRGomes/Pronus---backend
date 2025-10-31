package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.RelatorioDtoIn;
import com.inatel.prototipo_ia.dto.out.RelatorioDtoOut;
import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.RelatorioEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.RelatorioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;
    private final ChatRepository chatRepository;

    public RelatorioService(RelatorioRepository relatorioRepository, ChatRepository chatRepository) {
        this.relatorioRepository = relatorioRepository;
        this.chatRepository = chatRepository;
    }

    /**
     * Cria um novo relatório a partir de DTO In e retorna DTO Out.
     */
    public RelatorioDtoOut criar(RelatorioDtoIn relatorioDto) {
        validarRelatorioDto(relatorioDto);

        Long chatId = relatorioDto.getChatId();

        // Verifica se o chat existe
        Optional<ChatEntity> optionalChat = chatRepository.findById(chatId);
        if (optionalChat.isEmpty()) {
            throw new EntityNotFoundException("Não é possível criar o relatório pois o chat com ID " + chatId + " não foi encontrado.");
        }

        // Verifica se o chat já possui relatório
        if (relatorioRepository.existsByChatId(chatId)) {
            throw new IllegalStateException("O chat com ID " + chatId + " já possui um relatório associado.");
        }

        ChatEntity chat = optionalChat.get();

        RelatorioEntity entity = new RelatorioEntity();
        aplicarDtoNoEntity(entity, relatorioDto);
        entity.setChat(chat);

        RelatorioEntity salvo = relatorioRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todos os relatórios e retorna lista de DTOs de saída.
     */
    public List<RelatorioDtoOut> buscarTodos() {
        return relatorioRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um relatório pelo seu ID e retorna DTO de saída.
     */
    public Optional<RelatorioDtoOut> buscarPorId(Long id) {
        return relatorioRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca o relatório de um chat específico e retorna DTO de saída.
     */
    public Optional<RelatorioDtoOut> buscarPorChatId(Long chatId) {
        return relatorioRepository.findByChatId(chatId).map(this::toDto);
    }

    /**
     * Atualiza os dados de um relatório existente via DTO In e retorna DTO Out.
     */
    public RelatorioDtoOut atualizar(Long id, RelatorioDtoIn relatorioDto) {
        Optional<RelatorioEntity> optionalRelatorio = relatorioRepository.findById(id);
        if (optionalRelatorio.isEmpty()) {
            throw new EntityNotFoundException("Relatório não encontrado com o ID: " + id);
        }

        validarRelatorioDto(relatorioDto);

        RelatorioEntity existente = optionalRelatorio.get();
        aplicarDtoNoEntity(existente, relatorioDto);
        // Não permitimos a troca do chat associado no update

        RelatorioEntity atualizado = relatorioRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um relatório.
     */
    public void deletar(Long id) {
        if (!relatorioRepository.existsById(id)) {
            throw new EntityNotFoundException("Relatório não encontrado com o ID: " + id);
        }
        relatorioRepository.deleteById(id);
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private RelatorioDtoOut toDto(RelatorioEntity entity) {
        RelatorioDtoOut dto = new RelatorioDtoOut();
        dto.setId(entity.getId());
        dto.setAcuracia(entity.getAcuracia());
        dto.setAnaliseFono(entity.getAnaliseFono());
        dto.setChatId(entity.getChat().getId());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     * Não altera o chat no update.
     */
    private void aplicarDtoNoEntity(RelatorioEntity destino, RelatorioDtoIn fonte) {
        destino.setAcuracia(fonte.getAcuracia());
        destino.setAnaliseFono(fonte.getAnaliseFono());
        // Nota: chatId não é atualizado após criação
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarRelatorioDto(RelatorioDtoIn relatorio) {
        if (relatorio == null) {
            throw new IllegalArgumentException("O objeto de relatório não pode ser nulo.");
        }
        if (relatorio.getChatId() == null) {
            throw new IllegalArgumentException("O relatório deve estar associado a um chat.");
        }
        if (relatorio.getAnaliseFono() == null || relatorio.getAnaliseFono().isBlank()) {
            throw new IllegalArgumentException("A análise do fonoaudiólogo é obrigatória.");
        }
        if (relatorio.getAcuracia() == null || relatorio.getAcuracia() < 0.0 || relatorio.getAcuracia() > 1.0) {
            throw new IllegalArgumentException("A acurácia deve ser um valor entre 0.0 e 1.0.");
        }
    }
}