-- V5: Seed de agenda (muitos clientes, especialistas, disponibilidades e consultas)
-- Idempotente: usa NOT EXISTS e ON CONFLICT em chaves.
-- Requer tabelas de V1/V2 já aplicadas.

-- 1) Clientes (Usuarios + Cliente)
INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Cliente Ana', 27, 'Rua das Palmeiras, 12'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Cliente Ana');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Cliente Bruno', 34, 'Av. Rio Branco, 45'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Cliente Bruno');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Cliente Carla', 29, 'Rua das Acácias, 67'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Cliente Carla');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Cliente Diego', 31, 'Rua dos Pinheiros, 89'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Cliente Diego');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Cliente Elisa', 25, 'Rua das Hortênsias, 101'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Cliente Elisa');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Cliente Fabio', 36, 'Av. Paulista, 1500'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Cliente Fabio');

INSERT INTO Cliente (usuario_id, nivel)
SELECT u.id, 'INICIANTE'
FROM Usuario u WHERE u.nome = 'Cliente Ana'
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO Cliente (usuario_id, nivel)
SELECT u.id, 'INTERMEDIARIO'
FROM Usuario u WHERE u.nome = 'Cliente Bruno'
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO Cliente (usuario_id, nivel)
SELECT u.id, 'AVANCADO'
FROM Usuario u WHERE u.nome = 'Cliente Carla'
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO Cliente (usuario_id, nivel)
SELECT u.id, 'INICIANTE'
FROM Usuario u WHERE u.nome = 'Cliente Diego'
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO Cliente (usuario_id, nivel)
SELECT u.id, 'INTERMEDIARIO'
FROM Usuario u WHERE u.nome = 'Cliente Elisa'
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO Cliente (usuario_id, nivel)
SELECT u.id, 'AVANCADO'
FROM Usuario u WHERE u.nome = 'Cliente Fabio'
ON CONFLICT (usuario_id) DO NOTHING;

-- 2) Especialistas (Usuarios + Especialista)
INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Especialista 001', 40, 'Rua Alfa, 10'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Especialista 001');

INSERT INTO Usuario (nome, idade, endereco)
SELECT 'Especialista 002', 44, 'Rua Beta, 20'
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = 'Especialista 002');

INSERT INTO Especialista (usuario_id, crmFono, especialidade)
SELECT u.id, 'CRFA-10001', 'Fonoaudiologia'
FROM Usuario u WHERE u.nome = 'Especialista 001'
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO Especialista (usuario_id, crmFono, especialidade)
SELECT u.id, 'CRFA-10002', 'Fonoaudiologia'
FROM Usuario u WHERE u.nome = 'Especialista 002'
ON CONFLICT (usuario_id) DO NOTHING;

-- 3) Disponibilidades (para próximos 3 dias) manhã e tarde
-- Especialista 001: 09:00-12:00 e 14:00-18:00 nos próximos 3 dias
WITH esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 001'
),
ds AS (
    SELECT generate_series(1,3) AS dia_offset
)
INSERT INTO Disponibilidade (especialista_id, data, horaInicio, horaFim, status)
SELECT esp.especialista_id, CURRENT_DATE + ds.dia_offset, '09:00', '12:00', 'DISPONIVEL'
FROM esp, ds
WHERE NOT EXISTS (
    SELECT 1 FROM Disponibilidade d
    WHERE d.especialista_id = esp.especialista_id
      AND d.data = CURRENT_DATE + ds.dia_offset
      AND d.horaInicio = '09:00'
      AND d.horaFim = '12:00'
);

WITH esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 001'
),
ds AS (
    SELECT generate_series(1,3) AS dia_offset
)
INSERT INTO Disponibilidade (especialista_id, data, horaInicio, horaFim, status)
SELECT esp.especialista_id, CURRENT_DATE + ds.dia_offset, '14:00', '18:00', 'DISPONIVEL'
FROM esp, ds
WHERE NOT EXISTS (
    SELECT 1 FROM Disponibilidade d
    WHERE d.especialista_id = esp.especialista_id
      AND d.data = CURRENT_DATE + ds.dia_offset
      AND d.horaInicio = '14:00'
      AND d.horaFim = '18:00'
);

-- Especialista 002: 08:00-11:00 e 13:00-17:00 nos próximos 3 dias
WITH esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 002'
),
ds AS (
    SELECT generate_series(1,3) AS dia_offset
)
INSERT INTO Disponibilidade (especialista_id, data, horaInicio, horaFim, status)
SELECT esp.especialista_id, CURRENT_DATE + ds.dia_offset, '08:00', '11:00', 'DISPONIVEL'
FROM esp, ds
WHERE NOT EXISTS (
    SELECT 1 FROM Disponibilidade d
    WHERE d.especialista_id = esp.especialista_id
      AND d.data = CURRENT_DATE + ds.dia_offset
      AND d.horaInicio = '08:00'
      AND d.horaFim = '11:00'
);

WITH esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 002'
),
ds AS (
    SELECT generate_series(1,3) AS dia_offset
)
INSERT INTO Disponibilidade (especialista_id, data, horaInicio, horaFim, status)
SELECT esp.especialista_id, CURRENT_DATE + ds.dia_offset, '13:00', '17:00', 'DISPONIVEL'
FROM esp, ds
WHERE NOT EXISTS (
    SELECT 1 FROM Disponibilidade d
    WHERE d.especialista_id = esp.especialista_id
      AND d.data = CURRENT_DATE + ds.dia_offset
      AND d.horaInicio = '13:00'
      AND d.horaFim = '17:00'
);

-- 4) Consultas (cada cliente agenda com especialistas dentro das disponibilidades)
-- Cliente Ana: com Especialista 001 amanhã 10:00 (manhã) e 15:00 (tarde)
WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Ana'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 001'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 1, '10:00', 'AVALIACAO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 1
      AND co.hora = '10:00'
);

WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Ana'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 001'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 1, '15:00', 'RETORNO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 1
      AND co.hora = '15:00'
);

-- Cliente Bruno: com Especialista 002 em 2 dias 09:00 (manhã) e 14:00 (tarde)
WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Bruno'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 002'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 2, '09:00', 'AVALIACAO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 2
      AND co.hora = '09:00'
);

WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Bruno'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 002'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 2, '14:00', 'RETORNO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 2
      AND co.hora = '14:00'
);

-- Clientes Carla, Diego, Elisa, Fabio: um agendamento cada com Especialista 001/002
WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Carla'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 001'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 1, '11:00', 'TRATAMENTO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 1
      AND co.hora = '11:00'
);

WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Diego'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 002'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 3, '10:00', 'EXAME', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 3
      AND co.hora = '10:00'
);

WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Elisa'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 001'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 3, '15:00', 'RETORNO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 3
      AND co.hora = '15:00'
);

WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Fabio'
),
esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista 002'
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT cli.cliente_id, esp.especialista_id, CURRENT_DATE + 1, '10:00', 'AVALIACAO', 'AGENDADA'
FROM cli, esp
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = cli.cliente_id
      AND co.especialista_id = esp.especialista_id
      AND co.data = CURRENT_DATE + 1
      AND co.hora = '10:00'
);