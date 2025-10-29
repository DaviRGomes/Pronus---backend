package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.ConteudoTesteDtoIn;
import com.inatel.prototipo_ia.dto.out.ConteudoTesteDtoOut;
import com.inatel.prototipo_ia.entity.ConteudoTesteEntity;
import com.inatel.prototipo_ia.repository.ConteudoTesteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConteudoTesteService {

    private final ConteudoTesteRepository conteudoTesteRepository;

    public ConteudoTesteService(ConteudoTesteRepository conteudoTesteRepository) {
        this.conteudoTesteRepository = conteudoTesteRepository;
    }

    /**
     * Cria um novo conteúdo de teste a partir de DTO In e retorna DTO Out.
     */
    public ConteudoTesteDtoOut criar(ConteudoTesteDtoIn conteudoDto) {
        validarConteudoDto(conteudoDto);

        ConteudoTesteEntity entity = new ConteudoTesteEntity();
        aplicarDtoNoEntity(entity, conteudoDto);

        ConteudoTesteEntity salvo = conteudoTesteRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todos os conteúdos de teste e retorna lista de DTOs de saída.
     */
    public List<ConteudoTesteDtoOut> buscarTodos() {
        return conteudoTesteRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um conteúdo de teste pelo seu ID e retorna DTO de saída.
     */
    public Optional<ConteudoTesteDtoOut> buscarPorId(Long id) {
        return conteudoTesteRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca conteúdos de teste por dificuldade.
     */
    public List<ConteudoTesteDtoOut> buscarPorDificuldade(String dificuldade) {
        if (dificuldade == null || dificuldade.isBlank()) {
            throw new IllegalArgumentException("A dificuldade não pode ser vazia.");
        }
        return conteudoTesteRepository.findByDificuldade(dificuldade)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca conteúdos de teste por idioma.
     */
    public List<ConteudoTesteDtoOut> buscarPorIdioma(String idioma) {
        if (idioma == null || idioma.isBlank()) {
            throw new IllegalArgumentException("O idioma não pode ser vazio.");
        }
        return conteudoTesteRepository.findByIdioma(idioma)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca conteúdos de teste por dificuldade e idioma.
     */
    public List<ConteudoTesteDtoOut> buscarPorDificuldadeEIdioma(String dificuldade, String idioma) {
        if (dificuldade == null || dificuldade.isBlank()) {
            throw new IllegalArgumentException("A dificuldade não pode ser vazia.");
        }
        if (idioma == null || idioma.isBlank()) {
            throw new IllegalArgumentException("O idioma não pode ser vazio.");
        }
        return conteudoTesteRepository.findByDificuldadeAndIdioma(dificuldade, idioma)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza os dados de um conteúdo de teste existente via DTO In e retorna DTO Out.
     */
    public ConteudoTesteDtoOut atualizar(Long id, ConteudoTesteDtoIn conteudoDto) {
        Optional<ConteudoTesteEntity> optionalConteudo = conteudoTesteRepository.findById(id);
        if (optionalConteudo.isEmpty()) {
            throw new EntityNotFoundException("Conteúdo de teste não encontrado com o ID: " + id);
        }

        validarConteudoDto(conteudoDto);

        ConteudoTesteEntity existente = optionalConteudo.get();
        aplicarDtoNoEntity(existente, conteudoDto);

        ConteudoTesteEntity atualizado = conteudoTesteRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um conteúdo de teste.
     */
    public void deletar(Long id) {
        if (!conteudoTesteRepository.existsById(id)) {
            throw new EntityNotFoundException("Conteúdo de teste não encontrado com o ID: " + id);
        }
        conteudoTesteRepository.deleteById(id);
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private ConteudoTesteDtoOut toDto(ConteudoTesteEntity entity) {
        ConteudoTesteDtoOut dto = new ConteudoTesteDtoOut();
        dto.setId(entity.getId());
        dto.setTextoFrase(entity.getTextoFrase());
        dto.setFonemasChave(entity.getFonemasChave());
        dto.setDificuldade(entity.getDificuldade());
        dto.setIdioma(entity.getIdioma());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     */
    private void aplicarDtoNoEntity(ConteudoTesteEntity destino, ConteudoTesteDtoIn fonte) {
        destino.setTextoFrase(fonte.getTextoFrase());
        destino.setFonemasChave(fonte.getFonemasChave());
        destino.setDificuldade(fonte.getDificuldade());
        destino.setIdioma(fonte.getIdioma());
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarConteudoDto(ConteudoTesteDtoIn conteudo) {
        if (conteudo == null) {
            throw new IllegalArgumentException("O objeto de conteúdo de teste não pode ser nulo.");
        }
        if (conteudo.getTextoFrase() == null || conteudo.getTextoFrase().isBlank()) {
            throw new IllegalArgumentException("O texto da frase é obrigatório.");
        }
        if (conteudo.getFonemasChave() == null || conteudo.getFonemasChave().isBlank()) {
            throw new IllegalArgumentException("Os fonemas-chave são obrigatórios.");
        }
        if (conteudo.getDificuldade() == null || conteudo.getDificuldade().isBlank()) {
            throw new IllegalArgumentException("A dificuldade é obrigatória.");
        }
        if (conteudo.getIdioma() == null || conteudo.getIdioma().isBlank()) {
            throw new IllegalArgumentException("O idioma é obrigatório.");
        }
    }
}