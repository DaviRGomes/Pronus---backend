-- V9: Criar tabela sessaotreino para fluxo conversacional de treino
CREATE TABLE IF NOT EXISTS sessaotreino (
                                            id BIGSERIAL PRIMARY KEY,

    -- Relacionamentos
                                            cliente_id BIGINT NOT NULL,
                                            especialista_id BIGINT NOT NULL,
                                            chat_id BIGINT,

    -- Configurações da sessão
                                            dificuldade VARCHAR(50) NOT NULL,
    idadecliente INT NOT NULL,
    totalciclos INT NOT NULL DEFAULT 3,
    palavrasporciclo INT NOT NULL DEFAULT 3,

    -- Estado atual
    cicloatual INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'INICIADA',

    -- Palavras de cada ciclo (JSON)
    palavrasciclo1 TEXT,
    palavrasciclo2 TEXT,
    palavrasciclo3 TEXT,

    -- Resultados de cada ciclo (JSON)
    resultadociclo1 TEXT,
    resultadociclo2 TEXT,
    resultadociclo3 TEXT,

    -- Métricas
    totalpalavras INT DEFAULT 0,
    totalacertos INT DEFAULT 0,
    pontuacaogeral FLOAT DEFAULT 0,

    -- Timestamps
    datainicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    datafim TIMESTAMP,

    -- Histórico da conversa
    historicoconversa TEXT,

    -- Foreign Keys
    CONSTRAINT fk_sessao_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(usuario_id),
    CONSTRAINT fk_sessao_especialista FOREIGN KEY (especialista_id) REFERENCES especialista(usuario_id),
    CONSTRAINT fk_sessao_chat FOREIGN KEY (chat_id) REFERENCES chat(id)
    );

-- Índices
CREATE INDEX IF NOT EXISTS idx_sessao_cliente ON sessaotreino(cliente_id);
CREATE INDEX IF NOT EXISTS idx_sessao_especialista ON sessaotreino(especialista_id);
CREATE INDEX IF NOT EXISTS idx_sessao_status ON sessaotreino(status);
CREATE INDEX IF NOT EXISTS idx_sessao_datainicio ON sessaotreino(datainicio);

ALTER TABLE Chat ALTER COLUMN profissional_id DROP NOT NULL;