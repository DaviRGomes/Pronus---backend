-- Tabela base Usuario
CREATE TABLE IF NOT EXISTS Usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    idade INT,
    endereco VARCHAR(255)
);

-- Cliente herda de Usuario (1:1)
CREATE TABLE IF NOT EXISTS Cliente (
    usuario_id BIGINT PRIMARY KEY,
    nivel VARCHAR(50), -- Enum pode ser armazenado como VARCHAR
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id)
);

-- Profissional herda de Usuario (1:1)
CREATE TABLE IF NOT EXISTS Profissional (
    usuario_id BIGINT PRIMARY KEY,
    certificados VARCHAR(255), -- pode ser JSON ou tabela separada
    experiencia INT,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id)
);

-- Chat relacionado Cliente e Profissional (N:1 de cada lado)
CREATE TABLE IF NOT EXISTS Chat (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    profissional_id BIGINT NOT NULL,
    duracao INT,
    conversa TEXT,
    FOREIGN KEY (cliente_id) REFERENCES Cliente(usuario_id),
    FOREIGN KEY (profissional_id) REFERENCES Profissional(usuario_id)
);

-- Relatorio associado a um Chat (1:1)
CREATE TABLE IF NOT EXISTS Relatorio (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT UNIQUE NOT NULL,
    acuracia FLOAT,
    analiseFono VARCHAR(255),
    FOREIGN KEY (chat_id) REFERENCES Chat(id)
);

-- Tratamento associado a um Profissional (N:1)
CREATE TABLE IF NOT EXISTS Tratamento (
    id BIGSERIAL PRIMARY KEY,
    profissional_id BIGINT NOT NULL,
    quantidadeDia INT,
    tipoTratamento VARCHAR(50), -- Enum armazenado como string
    FOREIGN KEY (profissional_id) REFERENCES Profissional(usuario_id)
);
