package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.EspecialistaDtoIn;
import com.inatel.prototipo_ia.dto.out.EspecialistaDtoOut;
import com.inatel.prototipo_ia.service.EspecialistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/especialistas")
public class EspecialistaController {

    @Autowired
    private EspecialistaService service;

    @PostMapping
    public ResponseEntity<EspecialistaDtoOut> criar(@RequestBody EspecialistaDtoIn especialista) {
        EspecialistaDtoOut criado = service.criar(especialista);
        return ResponseEntity.ok(criado);
    }

    @GetMapping
    public ResponseEntity<List<EspecialistaDtoOut>> buscarTodos() {
        List<EspecialistaDtoOut> especialistas = service.buscarTodos();
        return ResponseEntity.ok(especialistas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialistaDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<EspecialistaDtoOut> especialista = service.buscarPorId(id);
        return especialista.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/especialidade/{especialidade}")
    public ResponseEntity<List<EspecialistaDtoOut>> buscarPorEspecialidade(@PathVariable String especialidade) {
        List<EspecialistaDtoOut> especialistas = service.buscarPorEspecialidade(especialidade);
        return ResponseEntity.ok(especialistas);
    }

    @GetMapping("/crm/{crmFono}")
    public ResponseEntity<EspecialistaDtoOut> buscarPorCrmFono(@PathVariable String crmFono) {
        Optional<EspecialistaDtoOut> especialista = service.buscarPorCrmFono(crmFono);
        return especialista.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/maiores-idade")
    public ResponseEntity<List<EspecialistaDtoOut>> buscarMaioresDeIdade() {
        List<EspecialistaDtoOut> especialistas = service.buscarMaioresDeIdade();
        return ResponseEntity.ok(especialistas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialistaDtoOut> atualizar(@PathVariable Long id, @RequestBody EspecialistaDtoIn especialista) {
        EspecialistaDtoOut atualizado = service.atualizar(id, especialista);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}