package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.CertificadoDtoIn;
import com.inatel.prototipo_ia.dto.out.CertificadoDtoOut;
import com.inatel.prototipo_ia.service.CertificadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/certificados")
public class CertificadoController {

    @Autowired
    private CertificadoService service;

    @PostMapping
    public ResponseEntity<CertificadoDtoOut> criar(@RequestBody CertificadoDtoIn certificado) {
        CertificadoDtoOut criado = service.criar(certificado);
        return ResponseEntity.ok(criado);
    }

    @GetMapping
    public ResponseEntity<List<CertificadoDtoOut>> buscarTodos() {
        List<CertificadoDtoOut> lista = service.buscarTodos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificadoDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<CertificadoDtoOut> certificado = service.buscarPorId(id);
        return certificado.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CertificadoDtoOut>> buscarPorClienteId(@PathVariable Long clienteId) {
        List<CertificadoDtoOut> certificados = service.buscarPorClienteId(clienteId);
        return ResponseEntity.ok(certificados);
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<CertificadoDtoOut>> buscarPorNivel(@PathVariable String nivel) {
        List<CertificadoDtoOut> certificados = service.buscarPorNivel(nivel);
        return ResponseEntity.ok(certificados);
    }

    @GetMapping("/data-emissao/apos/{data}")
    public ResponseEntity<List<CertificadoDtoOut>> buscarPorDataEmissaoApos(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<CertificadoDtoOut> certificados = service.buscarPorDataEmissaoApos(data);
        return ResponseEntity.ok(certificados);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificadoDtoOut> atualizar(@PathVariable Long id, @RequestBody CertificadoDtoIn certificado) {
        CertificadoDtoOut atualizado = service.atualizar(id, certificado);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}