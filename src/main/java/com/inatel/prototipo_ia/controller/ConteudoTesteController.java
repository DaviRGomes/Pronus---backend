package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.ConteudoTesteDtoIn;
import com.inatel.prototipo_ia.dto.out.ConteudoTesteDtoOut;
import com.inatel.prototipo_ia.service.ConteudoTesteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/conteudos-teste")
public class ConteudoTesteController {

    @Autowired
    private ConteudoTesteService service;

    @PostMapping
    public ResponseEntity<ConteudoTesteDtoOut> criar(@RequestBody ConteudoTesteDtoIn conteudo) {
        ConteudoTesteDtoOut criado = service.criar(conteudo);
        return ResponseEntity.ok(criado);
    }

    @GetMapping
    public ResponseEntity<List<ConteudoTesteDtoOut>> buscarTodos() {
        List<ConteudoTesteDtoOut> conteudos = service.buscarTodos();
        return ResponseEntity.ok(conteudos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConteudoTesteDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<ConteudoTesteDtoOut> conteudo = service.buscarPorId(id);
        return conteudo.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dificuldade/{dificuldade}")
    public ResponseEntity<List<ConteudoTesteDtoOut>> buscarPorDificuldade(@PathVariable String dificuldade) {
        List<ConteudoTesteDtoOut> conteudos = service.buscarPorDificuldade(dificuldade);
        return ResponseEntity.ok(conteudos);
    }

    @GetMapping("/idioma/{idioma}")
    public ResponseEntity<List<ConteudoTesteDtoOut>> buscarPorIdioma(@PathVariable String idioma) {
        List<ConteudoTesteDtoOut> conteudos = service.buscarPorIdioma(idioma);
        return ResponseEntity.ok(conteudos);
    }

    @GetMapping("/dificuldade/{dificuldade}/idioma/{idioma}")
    public ResponseEntity<List<ConteudoTesteDtoOut>> buscarPorDificuldadeEIdioma(
            @PathVariable String dificuldade, @PathVariable String idioma) {
        List<ConteudoTesteDtoOut> conteudos = service.buscarPorDificuldadeEIdioma(dificuldade, idioma);
        return ResponseEntity.ok(conteudos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConteudoTesteDtoOut> atualizar(@PathVariable Long id, @RequestBody ConteudoTesteDtoIn conteudo) {
        ConteudoTesteDtoOut atualizado = service.atualizar(id, conteudo);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}