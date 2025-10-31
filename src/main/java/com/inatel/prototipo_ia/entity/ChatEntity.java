package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Chat")
@Getter
@Setter
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer duracao;

    @Column(name = "conversa", columnDefinition = "TEXT")
    private String conversa;

    // Muitos chats para um cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    // Muitos chats para um profissional
    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalEntity profissional;

    // Relatório 1:1
    @OneToOne(mappedBy = "chat", cascade = CascadeType.ALL)
    private RelatorioEntity relatorio;
}