package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.entity.ChatEntity;
import com.inatel.prototipo_ia.entity.RelatorioEntity;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.RelatorioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Ativa o uso do Mockito nos testes
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveLancarExcecao_QuandoCriarRelatorioComAcuraciaInvalida() {
        // Cria um chat fictício para associar ao relatório
        ChatEntity chatDeMentira = new ChatEntity();
        chatDeMentira.setId(1L);

        // Cria um relatório com acurácia inválida (> 1.0)
        RelatorioEntity relatorioComProblema = new RelatorioEntity();
        relatorioComProblema.setChat(chatDeMentira);
        relatorioComProblema.setAnaliseFono("Análise de teste");
        relatorioComProblema.setAcuracia(1.1f);

        // Verifica se o método lança exceção de validação
        assertThrows(IllegalArgumentException.class, () -> {
            relatorioService.criar(relatorioComProblema);
        });

        // Garante que o repositório nunca foi acionado
        verify(relatorioRepository, never()).save(any(RelatorioEntity.class));
    }
}
