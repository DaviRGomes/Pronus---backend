package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.entity.RelatorioEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.RelatorioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
     * Cria um novo relatório para um chat, garante que o chat não tenha outro relatório.
     */
    public RelatorioEntity criar(RelatorioEntity relatorio) {
        validarRelatorio(relatorio);

        Long chatId = relatorio.getChat().getId();

        if (!chatRepository.existsById(chatId)) {
            throw new EntityNotFoundException("Não é possível criar o relatório pois o chat com ID " + chatId + " não foi encontrado.");
        }
        if (relatorioRepository.existsByChatId(chatId)) {
            throw new IllegalStateException("O chat com ID " + chatId + " já possui um relatório associado.");
        }

        return relatorioRepository.save(relatorio);
    }

    /**
     * Busca todos os relatórios.
     */
    public List<RelatorioEntity> buscarTodos() {
        return relatorioRepository.findAll();
    }

    /**
     * Busca um relatório pelo seu ID.
     */
    public Optional<RelatorioEntity> buscarPorId(Long id) {
        return relatorioRepository.findById(id);
    }

    /**
     * Busca o relatório de um chat específico.
     */
    public Optional<RelatorioEntity> buscarPorChatId(Long chatId) {
        return relatorioRepository.findByChatId(chatId);
    }

    /**
     * Atualiza os dados de um relatório existente.
     */
    public RelatorioEntity atualizar(Long id, RelatorioEntity relatorioAtualizado) {
        Optional<RelatorioEntity> optionalRelatorio = relatorioRepository.findById(id);
        if (optionalRelatorio.isEmpty()) {
            throw new EntityNotFoundException("Relatório não encontrado com o ID: " + id);
        }

        RelatorioEntity relatorioExistente = optionalRelatorio.get();
        validarRelatorio(relatorioAtualizado);

        // Atualiza os campos permitidos. Não permitimos a troca do chat associado.
        relatorioExistente.setAcuracia(relatorioAtualizado.getAcuracia());
        relatorioExistente.setAnaliseFono(relatorioAtualizado.getAnaliseFono());

        return relatorioRepository.save(relatorioExistente);
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
     * Valida os campos do objeto Relatorio.
     */
    private void validarRelatorio(RelatorioEntity relatorio) {
        if (relatorio == null) {
            throw new IllegalArgumentException("O objeto de relatório não pode ser nulo.");
        }
        if (relatorio.getChat() == null || relatorio.getChat().getId() == null) {
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