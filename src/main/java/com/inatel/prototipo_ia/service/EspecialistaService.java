package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.EspecialistaDtoIn;
import com.inatel.prototipo_ia.dto.out.EspecialistaDtoOut;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.repository.ConsultaRepository;
import com.inatel.prototipo_ia.repository.DisponibilidadeRepository;
import com.inatel.prototipo_ia.repository.EspecialistaRepository;
import com.inatel.prototipo_ia.repository.ChatRepository;
import com.inatel.prototipo_ia.repository.RelatorioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EspecialistaService {

    private final EspecialistaRepository especialistaRepository;
    private final ConsultaRepository consultaRepository;
    private final DisponibilidadeRepository disponibilidadeRepository;
    private final ChatRepository chatRepository;
    private final RelatorioRepository relatorioRepository;
    private final com.inatel.prototipo_ia.repository.UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public EspecialistaService(EspecialistaRepository especialistaRepository,
                               ConsultaRepository consultaRepository,
                               DisponibilidadeRepository disponibilidadeRepository,
                               ChatRepository chatRepository,
                               RelatorioRepository relatorioRepository,
                               com.inatel.prototipo_ia.repository.UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.especialistaRepository = especialistaRepository;
        this.consultaRepository = consultaRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.chatRepository = chatRepository;
        this.relatorioRepository = relatorioRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public EspecialistaDtoOut criar(EspecialistaDtoIn especialistaDto) {
        validarEspecialistaDto(especialistaDto);

        if (especialistaDto.getLogin() != null && usuarioRepository.findByLogin(especialistaDto.getLogin()) != null) {
            throw new IllegalStateException("Login já cadastrado");
        }

        EspecialistaEntity entity = new EspecialistaEntity();
        aplicarDtoNoEntity(entity, especialistaDto);

        if (especialistaDto.getLogin() != null && !especialistaDto.getLogin().isBlank()) {
            entity.setLogin(especialistaDto.getLogin());
        }
        
        if (especialistaDto.getSenha() != null && !especialistaDto.getSenha().isBlank()) {
            entity.setSenha(passwordEncoder.encode(especialistaDto.getSenha()));
        }

        EspecialistaEntity salvo = especialistaRepository.save(entity);
        return toDto(salvo);
    }

    public List<EspecialistaDtoOut> buscarTodos() {
        return especialistaRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<EspecialistaDtoOut> buscarPorId(Long id) {
        return especialistaRepository.findById(id).map(this::toDto);
    }

    public List<EspecialistaDtoOut> buscarPorEspecialidade(String especialidade) {
        if (especialidade == null || especialidade.isBlank()) {
            throw new IllegalArgumentException("A especialidade não pode ser vazia.");
        }
        return especialistaRepository.findByEspecialidade(especialidade)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<EspecialistaDtoOut> buscarPorCrmFono(String crmFono) {
        if (crmFono == null || crmFono.isBlank()) {
            throw new IllegalArgumentException("O CRM/CRFA não pode ser vazio.");
        }
        return especialistaRepository.findByCrmFono(crmFono).map(this::toDto);
    }

    public List<EspecialistaDtoOut> buscarMaioresDeIdade() {
        return especialistaRepository.findEspecialistasMaioresDeIdade()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public EspecialistaDtoOut atualizar(Long id, EspecialistaDtoIn especialistaDto) {
        Optional<EspecialistaEntity> optionalEspecialista = especialistaRepository.findById(id);
        if (optionalEspecialista.isEmpty()) {
            throw new EntityNotFoundException("Especialista não encontrado com o ID: " + id);
        }

        validarEspecialistaDto(especialistaDto);

        EspecialistaEntity existente = optionalEspecialista.get();
        aplicarDtoNoEntity(existente, especialistaDto);

        EspecialistaEntity atualizado = especialistaRepository.save(existente);
        return toDto(atualizado);
    }

    public void deletar(Long id) {
        if (!especialistaRepository.existsById(id)) {
            throw new EntityNotFoundException("Especialista não encontrado com o ID: " + id);
        }

        boolean emUsoEmConsulta = consultaRepository.existsByEspecialistaId(id);
        boolean emUsoEmDisponibilidade = disponibilidadeRepository.existsByEspecialistaId(id);

        if (emUsoEmConsulta || emUsoEmDisponibilidade) {
            throw new IllegalStateException("Não é possível deletar o especialista pois ele está associado a consultas ou disponibilidades existentes.");
        }

        especialistaRepository.deleteById(id);
    }

    private EspecialistaDtoOut toDto(EspecialistaEntity entity) {
        EspecialistaDtoOut dto = new EspecialistaDtoOut();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setIdade(entity.getIdade());
        dto.setEndereco(entity.getEndereco());
        dto.setCrmFono(entity.getCrmFono());
        dto.setEspecialidade(entity.getEspecialidade());
        dto.setConsultaIds(consultaRepository.findByEspecialistaId(entity.getId()).stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()));
        dto.setDisponibilidadeIds(disponibilidadeRepository.findByEspecialistaId(entity.getId()).stream().map(d -> d.getId()).collect(java.util.stream.Collectors.toList()));
        dto.setChatIds(chatRepository.findByEspecialistaId(entity.getId()).stream().map(ch -> ch.getId()).collect(java.util.stream.Collectors.toList()));
        dto.setRelatorioIds(relatorioRepository.findByEspecialistaId(entity.getId()).stream().map(r -> r.getId()).collect(java.util.stream.Collectors.toList()));
        return dto;
    }

    private void aplicarDtoNoEntity(EspecialistaEntity destino, EspecialistaDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setIdade(fonte.getIdade());
        destino.setEndereco(fonte.getEndereco());
        destino.setCrmFono(fonte.getCrmFono());
        destino.setEspecialidade(fonte.getEspecialidade());
    }

    private void validarEspecialistaDto(EspecialistaDtoIn especialista) {
        if (especialista == null) {
            throw new IllegalArgumentException("O objeto de especialista não pode ser nulo.");
        }
        if (especialista.getNome() == null || especialista.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do especialista é obrigatório.");
        }
        if (especialista.getCrmFono() == null || especialista.getCrmFono().isBlank()) {
            throw new IllegalArgumentException("O CRM/CRFA é obrigatório.");
        }
        if (especialista.getEspecialidade() == null || especialista.getEspecialidade().isBlank()) {
            throw new IllegalArgumentException("A especialidade é obrigatória.");
        }
        if (especialista.getIdade() != null && especialista.getIdade() < 0) {
            throw new IllegalArgumentException("A idade não pode ser negativa.");
        }
    }
}
