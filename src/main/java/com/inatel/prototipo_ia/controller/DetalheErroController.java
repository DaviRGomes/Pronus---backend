package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.DetalheErroDtoIn;
import com.inatel.prototipo_ia.dto.out.DetalheErroDtoOut;
import com.inatel.prototipo_ia.service.DetalheErroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/detalhes-erro")
public class DetalheErroController {

    @Autowired
    private DetalheErroService service;

    @PostMapping
    public ResponseEntity<DetalheErroDtoOut> criar(@RequestBody DetalheErroDtoIn detalhe) {
        DetalheErroDtoOut criado = service.criar(detalhe);
        return ResponseEntity.ok(criado);
    }

    @GetMapping
    public ResponseEntity<List<DetalheErroDtoOut>> buscarTodos() {
        List<DetalheErroDtoOut> detalhes = service.buscarTodos();
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalheErroDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<DetalheErroDtoOut> detalhe = service.buscarPorId(id);
        return detalhe.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/relatorio/{relatorioId}")
    public ResponseEntity<List<DetalheErroDtoOut>> buscarPorRelatorioId(@PathVariable Long relatorioId) {
        List<DetalheErroDtoOut> detalhes = service.buscarPorRelatorioId(relatorioId);
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping("/fonema-esperado/{fonema}")
    public ResponseEntity<List<DetalheErroDtoOut>> buscarPorFonemaEsperado(@PathVariable String fonema) {
        List<DetalheErroDtoOut> detalhes = service.buscarPorFonemaEsperado(fonema);
        return ResponseEntity.ok(detalhes);
    }

    @GetMapping("/score-maior/{score}")
    public ResponseEntity<List<DetalheErroDtoOut>> buscarComScoreMaiorQue(@PathVariable Float score) {
        List<DetalheErroDtoOut> detalhes = service.buscarComScoreMaiorQue(score);
        return ResponseEntity.ok(detalhes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalheErroDtoOut> atualizar(@PathVariable Long id, @RequestBody DetalheErroDtoIn detalhe) {
        DetalheErroDtoOut atualizado = service.atualizar(id, detalhe);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}