package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.ProfissionalDtoIn;
import com.inatel.prototipo_ia.dto.out.ChatDtoOut;
import com.inatel.prototipo_ia.dto.out.ProfissionalDtoOut;
import com.inatel.prototipo_ia.dto.out.RelatorioDtoOut;
import com.inatel.prototipo_ia.entity.UsuarioEntity;
import com.inatel.prototipo_ia.repository.ProfissionalRepository;
import com.inatel.prototipo_ia.service.ProfissionalService;
import com.inatel.prototipo_ia.service.ChatService;
import com.inatel.prototipo_ia.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    @Autowired
    private ProfissionalService service;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private RelatorioService relatorioService;

    // Criar profissional
    @PostMapping
    public ResponseEntity<ProfissionalDtoOut> criar(@RequestBody ProfissionalDtoIn profissional) {
        ProfissionalDtoOut profissionalCriado = service.criar(profissional);
        return ResponseEntity.ok(profissionalCriado);
    }

    // Buscar todos os profissionais
    @GetMapping
    public ResponseEntity<List<ProfissionalDtoOut>> buscarTodos() {
        List<ProfissionalDtoOut> profissionais = service.buscarTodos();
        return ResponseEntity.ok(profissionais);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfissionalDtoOut> me(@AuthenticationPrincipal UsuarioEntity principal) {
        if (!profissionalRepository.existsById(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        Optional<ProfissionalDtoOut> profissional = service.buscarPorId(principal.getId());
        return profissional.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Buscar profissional por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<ProfissionalDtoOut> profissional = service.buscarPorId(id);
        return profissional.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint de chats removido: chats agora pertencem ao Especialista

    @GetMapping("/me/relatorios/cliente/{clienteId}")
    public ResponseEntity<List<RelatorioDtoOut>> relatoriosDoCliente(@AuthenticationPrincipal UsuarioEntity principal,
                                                                     @PathVariable Long clienteId) {
        if (!profissionalRepository.existsById(principal.getId())) {
            return ResponseEntity.status(403).build();
        }
        List<RelatorioDtoOut> relatorios = relatorioService.buscarPorClienteIdEProfissionalId(clienteId, principal.getId());
        return ResponseEntity.ok(relatorios);
    }

    // Buscar profissionais experientes (mais de 5 anos)
    @GetMapping("/experientes")
    public ResponseEntity<List<ProfissionalDtoOut>> buscarExperientes() {
        List<ProfissionalDtoOut> profissionais = service.buscarExperientes();
        return ResponseEntity.ok(profissionais);
    }

    // Buscar profissionais com experiência maior que X anos
    @GetMapping("/experiencia-maior/{anos}")
    public ResponseEntity<List<ProfissionalDtoOut>> buscarComExperienciaMaiorQue(@PathVariable Integer anos) {
        List<ProfissionalDtoOut> profissionais = service.buscarComExperienciaMaiorQue(anos);
        return ResponseEntity.ok(profissionais);
    }

    // // Buscar profissionais por certificado
    // @GetMapping("/certificado/{certificado}")
    // public ResponseEntity<List<ProfissionalDtoOut>> buscarPorCertificado(@PathVariable String certificado) {
    //     List<ProfissionalDtoOut> profissionais = service.buscarPorCertificado(certificado);
    //     return ResponseEntity.ok(profissionais);
    // }

    // Buscar profissionais qualificados
    @GetMapping("/qualificados/{experienciaMinima}/{idadeMinima}")
    public ResponseEntity<List<ProfissionalDtoOut>> buscarQualificados(
            @PathVariable Integer experienciaMinima, 
            @PathVariable Integer idadeMinima) {
        List<ProfissionalDtoOut> profissionais = service.buscarQualificados(experienciaMinima, idadeMinima);
        return ResponseEntity.ok(profissionais);
    }

    // Atualizar profissional
    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalDtoOut> atualizar(@PathVariable Long id, @RequestBody ProfissionalDtoIn profissional) {
        ProfissionalDtoOut profissionalAtualizado = service.atualizar(id, profissional);
        return ResponseEntity.ok(profissionalAtualizado);
    }

    // Deletar profissional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
