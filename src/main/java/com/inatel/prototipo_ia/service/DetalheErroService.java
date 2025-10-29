package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.DetalheErroDtoIn;
import com.inatel.prototipo_ia.dto.out.DetalheErroDtoOut;
import com.inatel.prototipo_ia.entity.DetalheErroEntity;
import com.inatel.prototipo_ia.entity.RelatorioEntity;
import com.inatel.prototipo_ia.repository.DetalheErroRepository;
import com.inatel.prototipo_ia.repository.RelatorioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DetalheErroService {

    private final DetalheErroRepository detalheErroRepository;
    private final RelatorioRepository relatorioRepository;

    public DetalheErroService(DetalheErroRepository detalheErroRepository,
                              RelatorioRepository relatorioRepository) {
        this.detalheErroRepository = detalheErroRepository;
        this.relatorioRepository = relatorioRepository;
    }

    /**
     * Cria um novo detalhe de erro a partir de DTO In e retorna DTO Out.
     */
    public DetalheErroDtoOut criar(DetalheErroDtoIn detalheDto) {
        validarDetalheDto(detalheDto);

        // Validação do relatório
        Long relatorioId = detalheDto.getRelatorioId();
        Optional<RelatorioEntity> optionalRelatorio = relatorioRepository.findById(relatorioId);
        if (optionalRelatorio.isEmpty()) {
            throw new EntityNotFoundException("Não é possível criar o detalhe de erro pois o relatório com ID " + relatorioId + " não foi encontrado.");
        }
        RelatorioEntity relatorio = optionalRelatorio.get();

        DetalheErroEntity entity = new DetalheErroEntity();
        aplicarDtoNoEntity(entity, detalheDto);
        entity.setRelatorio(relatorio);

        DetalheErroEntity salvo = detalheErroRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todos os detalhes de erro e retorna lista de DTOs de saída.
     */
    public List<DetalheErroDtoOut> buscarTodos() {
        return detalheErroRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um detalhe de erro pelo seu ID e retorna DTO de saída.
     */
    public Optional<DetalheErroDtoOut> buscarPorId(Long id) {
        return detalheErroRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca detalhes de erro de um relatório específico.
     */
    public List<DetalheErroDtoOut> buscarPorRelatorioId(Long relatorioId) {
        return detalheErroRepository.findByRelatorioId(relatorioId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca detalhes de erro por fonema esperado.
     */
    public List<DetalheErroDtoOut> buscarPorFonemaEsperado(String fonemaEsperado) {
        if (fonemaEsperado == null || fonemaEsperado.isBlank()) {
            throw new IllegalArgumentException("O fonema esperado não pode ser vazio.");
        }
        return detalheErroRepository.findByFonemaEsperado(fonemaEsperado)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca detalhes de erro com score de desvio maior que um valor.
     */
    public List<DetalheErroDtoOut> buscarComScoreMaiorQue(Float score) {
        if (score == null || score < 0) {
            throw new IllegalArgumentException("O score deve ser não negativo.");
        }
        return detalheErroRepository.findByScoreDesvioGreaterThan(score)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza os dados de um detalhe de erro existente via DTO In e retorna DTO Out.
     */
    public DetalheErroDtoOut atualizar(Long id, DetalheErroDtoIn detalheDto) {
        Optional<DetalheErroEntity> optionalDetalhe = detalheErroRepository.findById(id);
        if (optionalDetalhe.isEmpty()) {
            throw new EntityNotFoundException("Detalhe de erro não encontrado com o ID: " + id);
        }

        validarDetalheDto(detalheDto);

        DetalheErroEntity existente = optionalDetalhe.get();
        aplicarDtoNoEntity(existente, detalheDto);
        // Não permitimos alterar o relatório no update

        DetalheErroEntity atualizado = detalheErroRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um detalhe de erro.
     */
    public void deletar(Long id) {
        if (!detalheErroRepository.existsById(id)) {
            throw new EntityNotFoundException("Detalhe de erro não encontrado com o ID: " + id);
        }
        detalheErroRepository.deleteById(id);
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private DetalheErroDtoOut toDto(DetalheErroEntity entity) {
        DetalheErroDtoOut dto = new DetalheErroDtoOut();
        dto.setId(entity.getId());
        dto.setFonemaEsperado(entity.getFonemaEsperado());
        dto.setFonemaProduzido(entity.getFonemaProduzido());
        dto.setScoreDesvio(entity.getScoreDesvio());
        dto.setRelatorioId(entity.getRelatorio().getId());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     * Não altera o relatório no update.
     */
    private void aplicarDtoNoEntity(DetalheErroEntity destino, DetalheErroDtoIn fonte) {
        destino.setFonemaEsperado(fonte.getFonemaEsperado());
        destino.setFonemaProduzido(fonte.getFonemaProduzido());
        destino.setScoreDesvio(fonte.getScoreDesvio());
        // Nota: relatorioId não é atualizado após criação
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarDetalheDto(DetalheErroDtoIn detalhe) {
        if (detalhe == null) {
            throw new IllegalArgumentException("O objeto de detalhe de erro não pode ser nulo.");
        }
        if (detalhe.getRelatorioId() == null) {
            throw new IllegalArgumentException("O detalhe de erro deve estar associado a um relatório.");
        }
        if (detalhe.getFonemaEsperado() == null || detalhe.getFonemaEsperado().isBlank()) {
            throw new IllegalArgumentException("O fonema esperado é obrigatório.");
        }
        if (detalhe.getFonemaProduzido() == null || detalhe.getFonemaProduzido().isBlank()) {
            throw new IllegalArgumentException("O fonema produzido é obrigatório.");
        }
        if (detalhe.getScoreDesvio() == null || detalhe.getScoreDesvio() < 0) {
            throw new IllegalArgumentException("O score de desvio deve ser não negativo.");
        }
    }
}