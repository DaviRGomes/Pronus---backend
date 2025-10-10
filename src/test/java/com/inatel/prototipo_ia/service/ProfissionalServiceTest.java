package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.repository.TratamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private TratamentoRepository tratamentoRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar profissional vinculado a um chat")
    void deveLancarExcecaoAoTentarDeletarProfissionalEmUsoEmChat() {
        Long profissionalId = 1L;

        // Simula que o profissional existe
        when(profissionalRepository.existsById(profissionalId)).thenReturn(true);

        // Simula vínculo com um chat (impede deleção)
        when(chatRepository.existsByProfissionalId(profissionalId)).thenReturn(true);

        // Verifica se a exceção esperada é lançada
        assertThrows(IllegalStateException.class, () -> {
            profissionalService.deletar(profissionalId);
        });

        // Garante que o deleteById nunca foi executado
        verify(profissionalRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar profissional vinculado a um tratamento")
    void deveLancarExcecaoAoTentarDeletarProfissionalEmUsoEmTratamento() {
        Long profissionalId = 2L;

        // Simula que o profissional existe
        when(profissionalRepository.existsById(profissionalId)).thenReturn(true);

        // Não há vínculo com chat
        when(chatRepository.existsByProfissionalId(profissionalId)).thenReturn(false);

        // Mas há vínculo com tratamento
        when(tratamentoRepository.existsByProfissionalId(profissionalId)).thenReturn(true);

        // Espera que a deleção lance exceção
        assertThrows(IllegalStateException.class, () -> {
            profissionalService.deletar(profissionalId);
        });

        // Garante que o deleteById não foi chamado
        verify(profissionalRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve deletar o profissional com sucesso quando ele não estiver em uso")
    void deveDeletarProfissionalComSucesso() {
        Long profissionalId = 3L;

        // Simula que o profissional existe
        when(profissionalRepository.existsById(profissionalId)).thenReturn(true);

        // Não há vínculos com chat nem tratamento
        when(chatRepository.existsByProfissionalId(profissionalId)).thenReturn(false);
        when(tratamentoRepository.existsByProfissionalId(profissionalId)).thenReturn(false);

        // Executa a exclusão
        profissionalService.deletar(profissionalId);

        // Confirma que o método delete foi chamado uma única vez
        verify(profissionalRepository, times(1)).deleteById(profissionalId);
    }
}