package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private ChatService chatService;

//    @Test
//    void criar_comClienteEProfissionalExistentes_deveRetornarChatSalvo() {
//
//        // criando objetos
//        ClienteEntity cliente = new ClienteEntity();
//        cliente.setId(1L);
//
//        ProfissionalEntity profissional = new ProfissionalEntity();
//        profissional.setId(1L);
//
//        ChatEntity chatParaSalvar = new ChatEntity();
//        chatParaSalvar.setCliente(cliente);
//        chatParaSalvar.setProfissional(profissional);
//
//        // criando objeto resultado
//        ChatEntity chatSalvo = new ChatEntity();
//        chatSalvo.setId(100L); // banco de dados deu id 100
//
//        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
//        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
//        when(chatRepository.save(any(ChatEntity.class))).thenReturn(chatSalvo);
//
//        // executando o que queremos testar
//        ChatEntity resultado = chatService.criar(chatParaSalvar);
//
//        // verifica o resultado
//        assertThat(resultado).isNotNull();
//        assertThat(resultado.getId()).isEqualTo(100L);
//
//        // verfica o save
//        verify(chatRepository).save(any(ChatEntity.class));
//    }
}