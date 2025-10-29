package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.UsuarioDtoIn;
import com.inatel.prototipo_ia.dto.out.UsuarioDtoOut;
import com.inatel.prototipo_ia.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    // Criar usuário
    @PostMapping
    public ResponseEntity<UsuarioDtoOut> criar(@RequestBody UsuarioDtoIn usuario) {
        UsuarioDtoOut usuarioCriado = service.criar(usuario);
        return ResponseEntity.ok(usuarioCriado);
    }

    // Buscar todos os usuários
    @GetMapping
    public ResponseEntity<List<UsuarioDtoOut>> buscarTodos() {
        List<UsuarioDtoOut> usuarios = service.buscarTodos();
        return ResponseEntity.ok(usuarios);
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<UsuarioDtoOut> usuario = service.buscarPorId(id);
        return usuario.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // Buscar usuários por idade
    @GetMapping("/idade/{idade}")
    public ResponseEntity<List<UsuarioDtoOut>> buscarPorIdade(@PathVariable Integer idade) {
        List<UsuarioDtoOut> usuarios = service.buscarPorIdade(idade);
        return ResponseEntity.ok(usuarios);
    }

    // Atualizar usuário
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDtoOut> atualizar(@PathVariable Long id, @RequestBody UsuarioDtoIn usuario) {
        UsuarioDtoOut usuarioAtualizado = service.atualizar(id, usuario);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    // Deletar usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Removidos os endpoints:
    // - /maiores-idade
    // - /nome/{nome}
    // - /endereco/{endereco}
    // - /idade-entre/{idadeMin}/{idadeMax}
    // Mantidos os que já existem na Service:
}