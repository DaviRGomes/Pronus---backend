package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.ClienteDtoIn;
import com.inatel.prototipo_ia.dto.out.ClienteDtoOut;
import com.inatel.prototipo_ia.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    // Criar cliente (DTO In -> DTO Out)
    @PostMapping
    public ResponseEntity<ClienteDtoOut> criar(@RequestBody ClienteDtoIn cliente) {
        ClienteDtoOut clienteCriado = service.salvar(cliente);
        return ResponseEntity.ok(clienteCriado);
    }

    // Buscar todos os clientes (DTO Out)
    @GetMapping
    public ResponseEntity<List<ClienteDtoOut>> buscarTodos() {
        List<ClienteDtoOut> clientes = service.buscarTodos();
        return ResponseEntity.ok(clientes);
    }

    // Buscar cliente por ID (DTO Out)
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDtoOut> buscarPorId(@PathVariable Long id) {
        Optional<ClienteDtoOut> cliente = service.buscarPorId(id);
        return cliente.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // Buscar clientes maiores de 18 anos (DTO Out)
    @GetMapping("/maiores-idade")
    public ResponseEntity<List<ClienteDtoOut>> buscarMaioresDeIdade() {
        List<ClienteDtoOut> clientes = service.buscarMaioresDeIdade();
        return ResponseEntity.ok(clientes);
    }

    // Buscar clientes por nível (DTO Out)
    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<ClienteDtoOut>> buscarPorNivel(@PathVariable String nivel) {
        List<ClienteDtoOut> clientes = service.buscarPorNivel(nivel);
        return ResponseEntity.ok(clientes);
    }

    // Buscar clientes por nível e idade mínima (DTO Out)
    @GetMapping("/nivel/{nivel}/idade-minima/{idade}")
    public ResponseEntity<List<ClienteDtoOut>> buscarPorNivelEIdadeMinima(
            @PathVariable String nivel,
            @PathVariable Integer idade) {
        // Serviço não expõe método composto; aplica-se filtro no controller.
        List<ClienteDtoOut> clientes = service.buscarPorNivel(nivel).stream()
                .filter(c -> c.getIdade() != null && c.getIdade() >= idade)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // Atualizar cliente (DTO In -> DTO Out)
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDtoOut> atualizar(@PathVariable Long id, @RequestBody ClienteDtoIn cliente) {
        ClienteDtoOut clienteAtualizado = service.atualizar(id, cliente);
        return ResponseEntity.ok(clienteAtualizado);
    }

    // Deletar cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}