package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.ChatDtoIn;
import com.inatel.prototipo_ia.dto.out.ChatDtoOut;
import com.inatel.prototipo_ia.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService service;

    @PostMapping
    public ResponseEntity<ChatDtoOut> criar(@RequestBody ChatDtoIn chat) {
        return ResponseEntity.ok(service.criar(chat));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatDtoOut> atualizar(@PathVariable Long id, @RequestBody ChatDtoIn chat) {
        return ResponseEntity.ok(service.atualizar(id, chat));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ChatDtoOut>> buscarPorClienteId(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarPorClienteId(clienteId));
    }

    // --- MUDANÇA: Endpoint agora é /especialista/{id} ---
    @GetMapping("/especialista/{especialistaId}")
    public ResponseEntity<List<ChatDtoOut>> buscarPorEspecialistaId(@PathVariable Long especialistaId) {
        return ResponseEntity.ok(service.buscarPorEspecialistaId(especialistaId));
    }
    
    // (Outros métodos como deletar, buscarTodos mantidos...)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
