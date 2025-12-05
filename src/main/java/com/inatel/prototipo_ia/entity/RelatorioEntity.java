package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import java.util.List;

@Entity
@Table(name = "Relatorio")
@Getter
@Setter
public class RelatorioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float acuracia;

    @Column(name = "analisefono")   
    private String analiseFono; 
    
    // Relatório pertence a um Chat
    @OneToOne
    @JoinColumn(name = "chat_id", nullable = false, unique = true)
    private ChatEntity chat;

    // Especialista responsável (opcional)
    @ManyToOne
    @JoinColumn(name = "especialista_id", nullable = true)
    private EspecialistaEntity especialista;

    @OneToMany(mappedBy = "relatorio")
    private List<DetalheErroEntity> detalhes;
}
