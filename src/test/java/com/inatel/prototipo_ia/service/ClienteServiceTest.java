package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    // Captura o objeto salvo para verificar se foi atualizado corretamente
    private ArgumentCaptor<ClienteEntity> clienteCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clienteCaptor = ArgumentCaptor.forClass(ClienteEntity.class);
    }

    @Test
    @DisplayName("Deve atualizar os dados de um cliente com sucesso (caminho feliz)")
    void deveAtualizarClienteComSucesso() {
        Long clienteId = 1L;

        // Cliente existente no banco antes da atualização
        ClienteEntity clienteAntigo = new ClienteEntity();
        clienteAntigo.setId(clienteId);
        clienteAntigo.setNome("Nome Antigo");
        clienteAntigo.setNivel("Iniciante");

        // Novos dados enviados para atualização
        ClienteEntity clienteAtualizado = new ClienteEntity();
        clienteAtualizado.setNome("Nome Novo e Atualizado");
        clienteAtualizado.setNivel("Intermediário");

        // Simula retorno do cliente antigo ao buscar pelo ID
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteAntigo));

        // Executa a atualização
        clienteService.atualizar(clienteId, clienteAtualizado);

        // Verifica se o método save foi chamado e captura o cliente salvo
        verify(clienteRepository, times(1)).save(clienteCaptor.capture());

        // Confere se os dados foram realmente atualizados
        ClienteEntity clienteSalvo = clienteCaptor.getValue();
        assertEquals("Nome Novo e Atualizado", clienteSalvo.getNome());
        assertEquals("Intermediário", clienteSalvo.getNivel());
        assertEquals(clienteId, clienteSalvo.getId()); // O ID deve permanecer o mesmo
    }
}
