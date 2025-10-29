package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.DisponibilidadeDtoIn;
import com.inatel.prototipo_ia.dto.out.DisponibilidadeDtoOut;
import com.inatel.prototipo_ia.service.DisponibilidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/disponibilidades")
public class DisponibilidadeController {

    @Autowired
    private DisponibilidadeService service;

    @PostMapping
    public ResponseEntity<DisponibilidadeDtoOut> criar(@RequestBody DisponibilidadeDtoIn disponibilidade) {
        DisponibilidadeDtoOut criado = service.criar(disponibilidade);
        return ResponseEntity.ok(criado);
    }

    @GetMapping
    public ResponseEntity<List<DisponibilidadeDtoOut>> buscarTodos() {
        List<DisponibilidadeDtoOut> disponibilidades = service.buscarTodos();
        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisponibilidadeDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<DisponibilidadeDtoOut> disponibilidade = service.buscarPorId(id);
        return disponibilidade.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/especialista/{especialistaId}")
    public ResponseEntity<List<DisponibilidadeDtoOut>> buscarPorEspecialistaId(@PathVariable Long especialistaId) {
        List<DisponibilidadeDtoOut> disponibilidades = service.buscarPorEspecialistaId(especialistaId);
        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/data/{data}")
    public ResponseEntity<List<DisponibilidadeDtoOut>> buscarPorData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<DisponibilidadeDtoOut> disponibilidades = service.buscarPorData(data);
        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DisponibilidadeDtoOut>> buscarPorStatus(@PathVariable String status) {
        List<DisponibilidadeDtoOut> disponibilidades = service.buscarPorStatus(status);
        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<DisponibilidadeDtoOut>> buscarDisponiveis() {
        List<DisponibilidadeDtoOut> disponibilidades = service.buscarDisponiveis();
        return ResponseEntity.ok(disponibilidades);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisponibilidadeDtoOut> atualizar(@PathVariable Long id, @RequestBody DisponibilidadeDtoIn disponibilidade) {
        DisponibilidadeDtoOut atualizado = service.atualizar(id, disponibilidade);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}