package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.ConsultaDtoIn;
import com.inatel.prototipo_ia.dto.out.ConsultaDtoOut;
import com.inatel.prototipo_ia.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService service;

    @PostMapping
    public ResponseEntity<ConsultaDtoOut> criar(@RequestBody ConsultaDtoIn consulta) {
        ConsultaDtoOut criado = service.criar(consulta);
        return ResponseEntity.ok(criado);
    }

    @GetMapping
    public ResponseEntity<List<ConsultaDtoOut>> buscarTodos() {
        List<ConsultaDtoOut> consultas = service.buscarTodos();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<ConsultaDtoOut> consulta = service.buscarPorId(id);
        return consulta.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ConsultaDtoOut>> buscarPorClienteId(@PathVariable Long clienteId) {
        List<ConsultaDtoOut> consultas = service.buscarPorClienteId(clienteId);
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/especialista/{especialistaId}")
    public ResponseEntity<List<ConsultaDtoOut>> buscarPorEspecialistaId(@PathVariable Long especialistaId) {
        List<ConsultaDtoOut> consultas = service.buscarPorEspecialistaId(especialistaId);
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/data/{data}")
    public ResponseEntity<List<ConsultaDtoOut>> buscarPorData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<ConsultaDtoOut> consultas = service.buscarPorData(data);
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ConsultaDtoOut>> buscarPorStatus(@PathVariable String status) {
        List<ConsultaDtoOut> consultas = service.buscarPorStatus(status);
        return ResponseEntity.ok(consultas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaDtoOut> atualizar(@PathVariable Long id, @RequestBody ConsultaDtoIn consulta) {
        ConsultaDtoOut atualizado = service.atualizar(id, consulta);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}