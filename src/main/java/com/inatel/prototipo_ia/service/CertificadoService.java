package com.inatel.prototipo_ia.service;

import com.inatel.prototipo_ia.dto.in.CertificadoDtoIn;
import com.inatel.prototipo_ia.dto.out.CertificadoDtoOut;
import com.inatel.prototipo_ia.entity.CertificadoEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.repository.CertificadoRepository;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CertificadoService {

    private final CertificadoRepository certificadoRepository;
    private final ClienteRepository clienteRepository;

    public CertificadoService(CertificadoRepository certificadoRepository, ClienteRepository clienteRepository) {
        this.certificadoRepository = certificadoRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Cria um novo certificado a partir de DTO In e retorna DTO Out.
     */
    public CertificadoDtoOut criar(CertificadoDtoIn certificadoDto) {
        validarCertificadoDto(certificadoDto);

        // Validação do cliente
        Long clienteId = certificadoDto.getClienteId();
        Optional<ClienteEntity> optionalCliente = clienteRepository.findById(clienteId);
        if (optionalCliente.isEmpty()) {
            throw new EntityNotFoundException("Não é possível criar o certificado pois o cliente com ID " + clienteId + " não foi encontrado.");
        }
        ClienteEntity cliente = optionalCliente.get();

        CertificadoEntity entity = new CertificadoEntity();
        aplicarDtoNoEntity(entity, certificadoDto);
        entity.setCliente(cliente);

        CertificadoEntity salvo = certificadoRepository.save(entity);
        return toDto(salvo);
    }

    /**
     * Busca todos os certificados e retorna lista de DTOs de saída.
     */
    public List<CertificadoDtoOut> buscarTodos() {
        return certificadoRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca um certificado pelo seu ID e retorna DTO de saída.
     */
    public Optional<CertificadoDtoOut> buscarPorId(Long id) {
        return certificadoRepository.findById(id).map(this::toDto);
    }

    /**
     * Busca certificados de um cliente específico.
     */
    public List<CertificadoDtoOut> buscarPorClienteId(Long clienteId) {
        return certificadoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca certificados por nível alcançado.
     */
    public List<CertificadoDtoOut> buscarPorNivel(String nivelAlcancado) {
        if (nivelAlcancado == null || nivelAlcancado.isBlank()) {
            throw new IllegalArgumentException("O nível alcançado não pode ser vazio.");
        }
        return certificadoRepository.findByNivelAlcancado(nivelAlcancado)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca certificados emitidos após uma data específica.
     */
    public List<CertificadoDtoOut> buscarPorDataEmissaoApos(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data não pode ser nula.");
        }
        return certificadoRepository.findByDataEmissaoAfter(data)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza os dados de um certificado existente via DTO In e retorna DTO Out.
     */
    public CertificadoDtoOut atualizar(Long id, CertificadoDtoIn certificadoDto) {
        Optional<CertificadoEntity> optionalCertificado = certificadoRepository.findById(id);
        if (optionalCertificado.isEmpty()) {
            throw new EntityNotFoundException("Certificado não encontrado com o ID: " + id);
        }

        validarCertificadoDto(certificadoDto);

        CertificadoEntity existente = optionalCertificado.get();
        aplicarDtoNoEntity(existente, certificadoDto);
        // Não permitimos alterar o cliente no update

        CertificadoEntity atualizado = certificadoRepository.save(existente);
        return toDto(atualizado);
    }

    /**
     * Deleta um certificado.
     */
    public void deletar(Long id) {
        if (!certificadoRepository.existsById(id)) {
            throw new EntityNotFoundException("Certificado não encontrado com o ID: " + id);
        }
        certificadoRepository.deleteById(id);
    }

    /**
     * Conversor de Entidade -> DTO Out.
     */
    private CertificadoDtoOut toDto(CertificadoEntity entity) {
        CertificadoDtoOut dto = new CertificadoDtoOut();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDataEmissao(entity.getDataEmissao());
        dto.setNivelAlcancado(entity.getNivelAlcancado());
        dto.setClienteId(entity.getCliente().getId());
        return dto;
    }

    /**
     * Aplica os campos do DTO In na entidade (create/update).
     * Não altera o cliente no update.
     */
    private void aplicarDtoNoEntity(CertificadoEntity destino, CertificadoDtoIn fonte) {
        destino.setNome(fonte.getNome());
        destino.setDataEmissao(fonte.getDataEmissao());
        destino.setNivelAlcancado(fonte.getNivelAlcancado());
        // Nota: clienteId não é atualizado após criação
    }

    /**
     * Validação do DTO de entrada.
     */
    private void validarCertificadoDto(CertificadoDtoIn certificado) {
        if (certificado == null) {
            throw new IllegalArgumentException("O objeto de certificado não pode ser nulo.");
        }
        if (certificado.getClienteId() == null) {
            throw new IllegalArgumentException("O certificado deve estar associado a um cliente.");
        }
        if (certificado.getNome() == null || certificado.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do certificado é obrigatório.");
        }
        if (certificado.getDataEmissao() == null) {
            throw new IllegalArgumentException("A data de emissão é obrigatória.");
        }
        if (certificado.getNivelAlcancado() == null || certificado.getNivelAlcancado().isBlank()) {
            throw new IllegalArgumentException("O nível alcançado é obrigatório.");
        }
    }
}