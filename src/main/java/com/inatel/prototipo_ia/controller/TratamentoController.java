package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.TratamentoDtoIn;
import com.inatel.prototipo_ia.dto.out.TratamentoDtoOut;
import com.inatel.prototipo_ia.service.TratamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tratamentos")
public class TratamentoController {

    @Autowired
    private TratamentoService service;

    // Criar tratamento
    @PostMapping
    public ResponseEntity<TratamentoDtoOut> criar(@RequestBody TratamentoDtoIn tratamento) {
        TratamentoDtoOut tratamentoCriado = service.criar(tratamento);
        return ResponseEntity.ok(tratamentoCriado);
    }

    // Buscar todos os tratamentos
    @GetMapping
    public ResponseEntity<List<TratamentoDtoOut>> buscarTodos() {
        List<TratamentoDtoOut> tratamentos = service.buscarTodos();
        return ResponseEntity.ok(tratamentos);
    }

    // Buscar tratamento por ID
    @GetMapping("/{id}")
    public ResponseEntity<TratamentoDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<TratamentoDtoOut> tratamento = service.buscarPorId(id);
        return tratamento.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    // Buscar tratamentos por ID do profissional
    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<TratamentoDtoOut>> buscarPorProfissionalId(@PathVariable Long profissionalId) {
        List<TratamentoDtoOut> tratamentos = service.buscarPorProfissionalId(profissionalId);
        return ResponseEntity.ok(tratamentos);
    }

    // Buscar tratamentos por tipo
    @GetMapping("/tipo/{tipoTratamento}")
    public ResponseEntity<List<TratamentoDtoOut>> buscarPorTipo(@PathVariable String tipoTratamento) {
        List<TratamentoDtoOut> tratamentos = service.buscarPorTipo(tipoTratamento);
        return ResponseEntity.ok(tratamentos);
    }

    // // Buscar tratamentos intensivos (mais de 3 por dia)
    // @GetMapping("/intensivos")
    // public ResponseEntity<List<TratamentoDtoOut>> buscarIntensivos() {
    //     List<TratamentoDtoOut> tratamentos = service.buscarIntensivos();
    //     return ResponseEntity.ok(tratamentos);
    // }

    // // Buscar tratamentos com quantidade por dia maior que X
    // @GetMapping("/quantidade-maior/{quantidade}")
    // public ResponseEntity<List<TratamentoDtoOut>> buscarComQuantidadeMaiorQue(@PathVariable Integer quantidade) {
    //     List<TratamentoDtoOut> tratamentos = service.buscarComQuantidadeMaiorQue(quantidade);
    //     return ResponseEntity.ok(tratamentos);
    // }

    // Buscar tratamentos por tipo e quantidade mínima
    @GetMapping("/tipo/{tipo}/quantidade-minima/{quantidade}")
    public ResponseEntity<List<TratamentoDtoOut>> buscarPorTipoEQuantidadeMinima(
            @PathVariable String tipo, 
            @PathVariable Integer quantidade) {
        List<TratamentoDtoOut> tratamentos = service.buscarPorTipoEQuantidadeMinima(tipo, quantidade);
        return ResponseEntity.ok(tratamentos);
    }

    // Atualizar tratamento
    @PutMapping("/{id}")
    public ResponseEntity<TratamentoDtoOut> atualizar(@PathVariable Long id, @RequestBody TratamentoDtoIn tratamento) {
        TratamentoDtoOut tratamentoAtualizado = service.atualizar(id, tratamento);
        return ResponseEntity.ok(tratamentoAtualizado);
    }

    // Deletar tratamento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}