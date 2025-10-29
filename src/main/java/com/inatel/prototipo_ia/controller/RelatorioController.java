package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.RelatorioDtoIn;
import com.inatel.prototipo_ia.dto.out.RelatorioDtoOut;
import com.inatel.prototipo_ia.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService service;

    // Criar relatório
    @PostMapping
    public ResponseEntity<RelatorioDtoOut> criar(@RequestBody RelatorioDtoIn relatorio) {
        RelatorioDtoOut relatorioCriado = service.criar(relatorio);
        return ResponseEntity.ok(relatorioCriado);
    }

    // Buscar todos os relatórios
    @GetMapping
    public ResponseEntity<List<RelatorioDtoOut>> buscarTodos() {
        List<RelatorioDtoOut> relatorios = service.buscarTodos();
        return ResponseEntity.ok(relatorios);
    }

    // Buscar relatório por ID
    @GetMapping("/{id}")
    public ResponseEntity<RelatorioDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<RelatorioDtoOut> relatorio = service.buscarPorId(id);
        return relatorio.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // Buscar relatório por ID do chat
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<RelatorioDtoOut> buscarPorChatId(@PathVariable Long chatId) {
        Optional<RelatorioDtoOut> relatorio = service.buscarPorChatId(chatId);
        return relatorio.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // Atualizar relatório
    @PutMapping("/{id}")
    public ResponseEntity<RelatorioDtoOut> atualizar(@PathVariable Long id, @RequestBody RelatorioDtoIn relatorio) {
        RelatorioDtoOut relatorioAtualizado = service.atualizar(id, relatorio);
        return ResponseEntity.ok(relatorioAtualizado);
    }

    // Deletar relatório
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}