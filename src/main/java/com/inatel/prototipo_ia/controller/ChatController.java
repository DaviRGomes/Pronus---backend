package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.ChatDtoIn;
import com.inatel.prototipo_ia.dto.out.ChatDtoOut;
import com.inatel.prototipo_ia.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chats")
public class ChatController {

    @Autowired
    private ChatService service;

    // Criar chat
    @PostMapping
    public ResponseEntity<ChatDtoOut> criar(@RequestBody ChatDtoIn chat) {
        ChatDtoOut chatCriado = service.criar(chat);
        return ResponseEntity.ok(chatCriado);
    }

    // Buscar todos os chats
    @GetMapping
    public ResponseEntity<List<ChatDtoOut>> buscarTodos() {
        List<ChatDtoOut> chats = service.buscarTodos();
        return ResponseEntity.ok(chats);
    }

    // Buscar chat por ID
    @GetMapping("/{id}")
    public ResponseEntity<ChatDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<ChatDtoOut> chat = service.buscarPorId(id);
        return chat.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // Buscar chats por ID do cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ChatDtoOut>> buscarPorClienteId(@PathVariable Long clienteId) {
        List<ChatDtoOut> chats = service.buscarPorClienteId(clienteId);
        return ResponseEntity.ok(chats);
    }

    // Buscar chats por ID do profissional
    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<ChatDtoOut>> buscarPorProfissionalId(@PathVariable Long profissionalId) {
        List<ChatDtoOut> chats = service.buscarPorProfissionalId(profissionalId);
        return ResponseEntity.ok(chats);
    }

    // Buscar chats longos (mais de 30 minutos)
    @GetMapping("/longos")
    public ResponseEntity<List<ChatDtoOut>> buscarChatsLongos() {
        List<ChatDtoOut> chats = service.buscarChatsLongos();
        return ResponseEntity.ok(chats);
    }

    // Buscar chats com duração maior que X minutos
    @GetMapping("/duracao-maior/{minutos}")
    public ResponseEntity<List<ChatDtoOut>> buscarComDuracaoMaiorQue(@PathVariable Integer minutos) {
        List<ChatDtoOut> chats = service.buscarComDuracaoMaiorQue(minutos);
        return ResponseEntity.ok(chats);
    }

    // Atualizar chat
    @PutMapping("/{id}")
    public ResponseEntity<ChatDtoOut> atualizar(@PathVariable Long id, @RequestBody ChatDtoIn chat) {
        ChatDtoOut chatAtualizado = service.atualizar(id, chat);
        return ResponseEntity.ok(chatAtualizado);
    }

    // Deletar chat
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}