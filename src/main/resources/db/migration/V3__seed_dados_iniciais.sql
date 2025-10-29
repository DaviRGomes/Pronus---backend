-- V3: Seed idempotente de dados iniciais (Usuario, Cliente, Profissional, Chat, Relatorio, Tratamento)
-- + tabelas novas da V2 (Especialista, Secretaria, Disponibilidade, Consulta, ConteudoTeste, Certificado, DetalheErro)

-- 1) Usuarios base
INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Cliente Demo', 30, 'Rua das Flores, 123'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Cliente Demo');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Profissional Demo', 40, 'Av. Central, 456'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Profissional Demo');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Especialista Demo', 38, 'Rua Alfa, 789'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Especialista Demo');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Secretaria Demo', 28, 'Rua Beta, 101'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Secretaria Demo');

-- 2) Cliente (1:1 Usuario) - PK/UNIQUE: usuario_id
INSERT INTO Cliente (usuario_id, nivel)
SELECT u.id, 'INICIANTE'
FROM Usuario u
WHERE u.nome = 'Cliente Demo'
ON CONFLICT (usuario_id) DO NOTHING;

-- 3) Profissional (1:1 Usuario) - PK/UNIQUE: usuario_id
INSERT INTO Profissional (usuario_id, certificados, experiencia)
SELECT u.id, 'Cert 1, Cert 2', 10
FROM Usuario u
WHERE u.nome = 'Profissional Demo'
ON CONFLICT (usuario_id) DO NOTHING;

-- 4) Chat (N:1 Cliente, N:1 Profissional)
WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c
    JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Demo'
),
pro AS (
    SELECT p.usuario_id AS profissional_id
    FROM Profissional p
    JOIN Usuario u ON u.id = p.usuario_id
    WHERE u.nome = 'Profissional Demo'
)
INSERT INTO Chat (cliente_id, profissional_id, duracao, conversa)
SELECT cli.cliente_id, pro.profissional_id, 45, 'Conversa inicial de avaliação'
FROM cli, pro
WHERE NOT EXISTS (
    SELECT 1 FROM Chat ch
    WHERE ch.cliente_id = cli.cliente_id AND ch.profissional_id = pro.profissional_id
);

-- 5) Relatorio (1:1 Chat) - UNIQUE (chat_id)
WITH ch AS (
    SELECT ch.id AS chat_id
    FROM Chat ch
    WHERE ch.cliente_id = (
        SELECT c.usuario_id
        FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
        WHERE u.nome = 'Cliente Demo'
    )
    AND ch.profissional_id = (
        SELECT p.usuario_id
        FROM Profissional p JOIN Usuario u ON u.id = p.usuario_id
        WHERE u.nome = 'Profissional Demo'
    )
)
INSERT INTO Relatorio (chat_id, acuracia, analiseFono)
SELECT ch.chat_id, 0.82, 'Paciente apresenta dificuldades com fonema R'
FROM ch
ON CONFLICT (chat_id) DO NOTHING;

-- 6) Tratamento (N:1 Profissional)
WITH pro AS (
    SELECT p.usuario_id AS profissional_id
    FROM Profissional p JOIN Usuario u ON u.id = p.usuario_id
    WHERE u.nome = 'Profissional Demo'
)
INSERT INTO Tratamento (profissional_id, quantidadeDia, tipoTratamento)
SELECT pro.profissional_id, 3, 'ARTICULACAO'
FROM pro
WHERE NOT EXISTS (
    SELECT 1 FROM Tratamento t WHERE t.profissional_id = pro.profissional_id
);

-- 7) ConteudoTeste (tabela V2)
INSERT INTO ConteudoTeste (textoFrase, fonemasChave, dificuldade, idioma)
SELECT 'O rato roeu a roupa do rei de Roma.', '["R"]'::jsonb, 'INTERMEDIARIO', 'PT-BR'
WHERE NOT EXISTS (
    SELECT 1 FROM ConteudoTeste WHERE textoFrase = 'O rato roeu a roupa do rei de Roma.'
);

INSERT INTO ConteudoTeste (textoFrase, fonemasChave, dificuldade, idioma)
SELECT 'Lili lambeu limões no lago.', '["L"]'::jsonb, 'FACIL', 'PT-BR'
WHERE NOT EXISTS (
    SELECT 1 FROM ConteudoTeste WHERE textoFrase = 'Lili lambeu limões no lago.'
);

-- 8) Vincula Tratamento -> ConteudoTeste (se V2 adicionou coluna conteudoTeste_id)
-- Marca personalizado=true para indicar conteúdo específico
WITH pro AS (
    SELECT p.usuario_id AS profissional_id
    FROM Profissional p JOIN Usuario u ON u.id = p.usuario_id
    WHERE u.nome = 'Profissional Demo'
),
ct AS (
    SELECT id AS conteudo_id
    FROM ConteudoTeste
    WHERE textoFrase = 'O rato roeu a roupa do rei de Roma.'
)
UPDATE Tratamento t
SET conteudoTeste_id = (SELECT conteudo_id FROM ct),
    personalizado = TRUE
WHERE t.profissional_id = (SELECT profissional_id FROM pro)
  AND t.conteudoTeste_id IS NULL;

-- 9) Especialista (1:1 Usuario) - V2
INSERT INTO Especialista (usuario_id, crmFono, especialidade)
SELECT u.id, 'CRFA-12345', 'Fonoaudiologia'
FROM Usuario u
WHERE u.nome = 'Especialista Demo'
ON CONFLICT (usuario_id) DO NOTHING;

-- 10) Secretaria (1:1 Usuario) - V2
INSERT INTO Secretaria (usuario_id, email)
SELECT u.id, 'secretaria.demo@example.com'
FROM Usuario u
WHERE u.nome = 'Secretaria Demo'
ON CONFLICT (usuario_id) DO NOTHING;

-- 11) Disponibilidade (N:1 Especialista) - V2
WITH esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista Demo'
)
INSERT INTO Disponibilidade (especialista_id, data, horaInicio, horaFim, status)
SELECT esp.especialista_id, CURRENT_DATE + 1, '09:00', '12:00', 'DISPONIVEL'
FROM esp
WHERE NOT EXISTS (
    SELECT 1 FROM Disponibilidade d
    WHERE d.especialista_id = esp.especialista_id
      AND d.data = CURRENT_DATE + 1
      AND d.horaInicio = '09:00'
      AND d.horaFim = '12:00'
);

-- 12) Consulta (N:1 Cliente, N:1 Especialista) - V2
WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Demo'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista Demo'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 2, '10:00', 'AVALIACAO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 2
      AND co.hora = '10:00'
);

-- 13) Certificado (1:N Cliente) - V2
WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Demo'
)
INSERT INTO Certificado (cliente_id, nome, dataEmissao, nivelAlcancado)
SELECT cli.cliente_id, 'Certificado de Progresso', CURRENT_DATE, 'BASICO'
FROM cli
WHERE NOT EXISTS (
    SELECT 1 FROM Certificado cert
    WHERE cert.cliente_id = cli.cliente_id
      AND cert.nome = 'Certificado de Progresso'
);

-- 14) DetalheErro (1:N Relatorio) - V2
WITH ch AS (
    SELECT ch.id AS chat_id
    FROM Chat ch
    WHERE ch.cliente_id = (
        SELECT c.usuario_id FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id WHERE u.nome = 'Cliente Demo'
    )
    AND ch.profissional_id = (
        SELECT p.usuario_id FROM Profissional p JOIN Usuario u ON u.id = p.usuario_id WHERE u.nome = 'Profissional Demo'
    )
),
rel AS (
    SELECT r.id AS relatorio_id
    FROM Relatorio r JOIN ch ON r.chat_id = ch.chat_id
)
INSERT INTO DetalheErro (relatorio_id, fonemaEsperado, fonemaProduzido, scoreDesvio)
SELECT rel.relatorio_id, 'R', 'L', 0.40
FROM rel
WHERE NOT EXISTS (
    SELECT 1 FROM DetalheErro de WHERE de.relatorio_id = rel.relatorio_id
);