-- V2: alterações de schema para novo dataset (agendamento, análise detalhada)

-- Nova entidade Especialista (herança de Usuario)
CREATE TABLE Especialista (
    usuario_id BIGINT PRIMARY KEY,
    crmFono VARCHAR(50),
    especialidade VARCHAR(100),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id)
);

-- Nova entidade Secretaria (herança de Usuario)
CREATE TABLE Secretaria (
    usuario_id BIGINT PRIMARY KEY,
    email VARCHAR(255),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id)
);

-- Disponibilidade de Especialista (N:1 Especialista)
CREATE TABLE Disponibilidade (
    id BIGSERIAL PRIMARY KEY,
    especialista_id BIGINT NOT NULL,
    data DATE NOT NULL,
    horaInicio TIME NOT NULL,
    horaFim TIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (especialista_id) REFERENCES Especialista(usuario_id)
);
CREATE INDEX idx_disponibilidade_especialista ON Disponibilidade (especialista_id);
CREATE INDEX idx_disponibilidade_data ON Disponibilidade (data);

-- Consultas (N:1 Cliente, N:1 Especialista)
CREATE TABLE Consulta (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    especialista_id BIGINT NOT NULL,
    data DATE NOT NULL,
    hora TIME NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES Cliente(usuario_id),
    FOREIGN KEY (especialista_id) REFERENCES Especialista(usuario_id)
);
CREATE INDEX idx_consulta_cliente ON Consulta (cliente_id);
CREATE INDEX idx_consulta_especialista ON Consulta (especialista_id);
CREATE INDEX idx_consulta_data ON Consulta (data);

-- Conteúdo de Teste (conteúdo parametrizado para tratamentos)
CREATE TABLE ConteudoTeste (
    id BIGSERIAL PRIMARY KEY,
    textoFrase TEXT NOT NULL,
    fonemasChave JSONB,
    dificuldade VARCHAR(50),
    idioma VARCHAR(50)
);

-- Alterações em Tratamento: campo personalizado e FK para ConteudoTeste
ALTER TABLE Tratamento
    ADD COLUMN personalizado BOOLEAN DEFAULT FALSE;
ALTER TABLE Tratamento
    ADD COLUMN conteudoTeste_id BIGINT;
ALTER TABLE Tratamento
    ADD CONSTRAINT fk_tratamento_conteudo_teste
        FOREIGN KEY (conteudoTeste_id) REFERENCES ConteudoTeste(id);

-- Certificados do Cliente (1:N)
CREATE TABLE Certificado (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    dataEmissao DATE,
    nivelAlcancado VARCHAR(50),
    FOREIGN KEY (cliente_id) REFERENCES Cliente(usuario_id)
);
CREATE INDEX idx_certificado_cliente ON Certificado (cliente_id);

-- Detalhes de erro do Relatório (1:N)
CREATE TABLE DetalheErro (
    id BIGSERIAL PRIMARY KEY,
    relatorio_id BIGINT NOT NULL,
    fonemaEsperado VARCHAR(50),
    fonemaProduzido VARCHAR(50),
    scoreDesvio FLOAT,
    FOREIGN KEY (relatorio_id) REFERENCES Relatorio(id)
);
CREATE INDEX idx_detalhe_erro_relatorio ON DetalheErro (relatorio_id);

-- Ajuste em Cliente: experiência acumulada
ALTER TABLE Cliente
    ADD COLUMN experiencia INT;