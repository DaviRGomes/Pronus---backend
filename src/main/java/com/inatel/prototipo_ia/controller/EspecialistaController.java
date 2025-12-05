package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.EspecialistaDtoIn;
import com.inatel.prototipo_ia.dto.out.EspecialistaDtoOut;
import com.inatel.prototipo_ia.dto.out.ClienteDtoOut;
import com.inatel.prototipo_ia.dto.out.RelatorioDtoOut;
import com.inatel.prototipo_ia.dto.out.ConsultaDtoOut;
import com.inatel.prototipo_ia.entity.UsuarioEntity;
import com.inatel.prototipo_ia.repository.EspecialistaRepository;
import com.inatel.prototipo_ia.service.ClienteService;
import com.inatel.prototipo_ia.service.ConsultaService;
import com.inatel.prototipo_ia.service.EspecialistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/especialistas")
public class EspecialistaController {

    @Autowired
    private EspecialistaService service;

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private com.inatel.prototipo_ia.service.RelatorioService relatorioService;

    @PostMapping
    public ResponseEntity<EspecialistaDtoOut> criar(@RequestBody EspecialistaDtoIn especialista) {
        try {
            EspecialistaDtoOut criado = service.criar(especialista);
            return ResponseEntity.ok(criado);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<EspecialistaDtoOut>> buscarTodos() {
        List<EspecialistaDtoOut> especialistas = service.buscarTodos();
        return ResponseEntity.ok(especialistas);
    }

    @GetMapping("/me")
    public ResponseEntity<EspecialistaDtoOut> me(@AuthenticationPrincipal UsuarioEntity principal) {
        if (!especialistaRepository.existsById(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        Optional<EspecialistaDtoOut> especialista = service.buscarPorId(principal.getId());
        return especialista.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialistaDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<EspecialistaDtoOut> especialista = service.buscarPorId(id);
        return especialista.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/consultas")
    public ResponseEntity<List<ConsultaDtoOut>> minhasConsultas(@AuthenticationPrincipal UsuarioEntity principal) {
        if (!especialistaRepository.existsById(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        List<ConsultaDtoOut> consultas = consultaService.buscarPorEspecialistaId(principal.getId());
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/me/pacientes")
    public ResponseEntity<List<ClienteDtoOut>> meusPacientes(@AuthenticationPrincipal UsuarioEntity principal) {
        if (!especialistaRepository.existsById(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        List<ConsultaDtoOut> consultas = consultaService.buscarPorEspecialistaId(principal.getId());
        List<Long> ids = consultas.stream().map(ConsultaDtoOut::getClienteId).distinct().toList();
        List<ClienteDtoOut> pacientes = ids.stream()
                .map(clienteService::buscarPorId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/me/relatorios/cliente/{clienteId}")
    public ResponseEntity<List<RelatorioDtoOut>> relatoriosDoCliente(@AuthenticationPrincipal UsuarioEntity principal,
                                                                     @PathVariable Long clienteId) {
        if (!especialistaRepository.existsById(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        boolean temVinculo = consultaService.buscarPorEspecialistaId(principal.getId())
                .stream().anyMatch(c -> c.getClienteId().equals(clienteId));
        if (!temVinculo) {
            return ResponseEntity.status(403).build();
        }
        List<RelatorioDtoOut> relatorios = relatorioService.buscarPorClienteIdEEspecialistaId(clienteId, principal.getId());
        return ResponseEntity.ok(relatorios);
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
