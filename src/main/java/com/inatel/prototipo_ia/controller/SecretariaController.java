package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.SecretariaDtoIn;
import com.inatel.prototipo_ia.dto.out.SecretariaDtoOut;
import com.inatel.prototipo_ia.service.SecretariaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/secretarias")
public class SecretariaController {

    @Autowired
    private SecretariaService service;

    @PostMapping
    public ResponseEntity<SecretariaDtoOut> criar(@RequestBody SecretariaDtoIn secretaria) {
        try {
            SecretariaDtoOut criado = service.criar(secretaria);
            return ResponseEntity.ok(criado);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<SecretariaDtoOut>> buscarTodos() {
        List<SecretariaDtoOut> secretarias = service.buscarTodos();
        return ResponseEntity.ok(secretarias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecretariaDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<SecretariaDtoOut> secretaria = service.buscarPorId(id);
        return secretaria.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<SecretariaDtoOut> buscarPorEmail(@PathVariable String email) {
        Optional<SecretariaDtoOut> secretaria = service.buscarPorEmail(email);
        return secretaria.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<SecretariaDtoOut>> buscarPorNome(@PathVariable String nome) {
        List<SecretariaDtoOut> secretarias = service.buscarPorNome(nome);
        return ResponseEntity.ok(secretarias);
    }

    @GetMapping("/maiores-idade")
    public ResponseEntity<List<SecretariaDtoOut>> buscarMaioresDeIdade() {
        List<SecretariaDtoOut> secretarias = service.buscarMaioresDeIdade();
        return ResponseEntity.ok(secretarias);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SecretariaDtoOut> atualizar(@PathVariable Long id, @RequestBody SecretariaDtoIn secretaria) {
        SecretariaDtoOut atualizado = service.atualizar(id, secretaria);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
