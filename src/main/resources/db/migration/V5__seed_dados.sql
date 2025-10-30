-- V5 FINALMENTE REVISADA E CORRIGIDA: Seed de Agenda Aumentada (Clientes Sem Prefixo)
-- CORREÇÃO: Adicionado o CAST ::TIME nas comparações de hora para evitar o erro 42883.
-- Requer tabelas de V1/V2/V3 já aplicadas.

------------------------------------------
-- 1) Clientes Base (6 originais)
------------------------------------------
-- Inserção dos 6 Usuários-Cliente base
INSERT INTO Usuario (nome, idade, endereco)
SELECT nome, idade, endereco
FROM (
    VALUES
    ('Ana', 27, 'Rua das Palmeiras, 12'),
    ('Bruno', 34, 'Av. Rio Branco, 45'),
    ('Carla', 29, 'Rua das Acácias, 67'),
    ('Diego', 31, 'Rua dos Pinheiros, 89'),
    ('Elisa', 25, 'Rua das Hortênsias, 101'),
    ('Fabio', 36, 'Av. Paulista, 1500')
) AS novos_usuarios (nome, idade, endereco)
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = novos_usuarios.nome);

-- Inserção dos Clientes na tabela Cliente
WITH base_clients AS (
    SELECT id, nome,
           CASE nome
               WHEN 'Ana' THEN 'INICIANTE'
               WHEN 'Bruno' THEN 'INTERMEDIARIO'
               WHEN 'Carla' THEN 'AVANCADO'
               WHEN 'Diego' THEN 'INICIANTE'
               WHEN 'Elisa' THEN 'INTERMEDIARIO'
               WHEN 'Fabio' THEN 'AVANCADO'
               ELSE NULL
           END AS nivel
    FROM Usuario
    WHERE nome IN ('Ana', 'Bruno', 'Carla', 'Diego', 'Elisa', 'Fabio')
)
INSERT INTO Cliente (usuario_id, nivel)
SELECT id, nivel
FROM base_clients
ON CONFLICT (usuario_id) DO NOTHING;

------------------------------------------
-- 2) Clientes Adicionais (10 novos)
------------------------------------------
-- Inserção de 10 Usuários-Cliente de uma só vez
INSERT INTO Usuario (nome, idade, endereco)
SELECT nome, idade, endereco
FROM (
    VALUES
    ('Gustavo', 22, 'Rua Viena, 1'),
    ('Helena', 45, 'Av. Madri, 2'),
    ('Igor', 33, 'Rua Berlim, 3'),
    ('Julia', 28, 'Av. Paris, 4'),
    ('Kevin', 51, 'Rua Londres, 5'),
    ('Laura', 30, 'Av. Roma, 6'),
    ('Marcelo', 42, 'Rua Lisboa, 7'),
    ('Natalia', 26, 'Av. Praga, 8'),
    ('Otavio', 38, 'Rua Atenas, 9'),
    ('Patricia', 24, 'Av. Cairo, 10')
) AS novos_usuarios (nome, idade, endereco)
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = novos_usuarios.nome);

-- Inserção dos novos Clientes na tabela Cliente
WITH new_clients AS (
    SELECT id, nome,
           CASE
               WHEN nome IN ('Gustavo', 'Igor', 'Marcelo') THEN 'INICIANTE'
               WHEN nome IN ('Helena', 'Julia', 'Otavio') THEN 'INTERMEDIARIO'
               ELSE 'AVANCADO'
           END AS nivel
    FROM Usuario
    WHERE nome IN (
        'Gustavo', 'Helena', 'Igor', 'Julia', 'Kevin', 'Laura',
        'Marcelo', 'Natalia', 'Otavio', 'Patricia'
    )
)
INSERT INTO Cliente (usuario_id, nivel)
SELECT id, nivel
FROM new_clients
ON CONFLICT (usuario_id) DO NOTHING;

------------------------------------------
-- 3) Especialistas Base e Adicionais (5 no total)
------------------------------------------
-- Inserção dos Especialistas (Base: 001, 002 | Novos: 003, 004, 005)
INSERT INTO Usuario (nome, idade, endereco)
SELECT nome, idade, endereco
FROM (
    VALUES
    ('Especialista 001', 40, 'Rua Alfa, 10'),
    ('Especialista 002', 44, 'Rua Beta, 20'),
    ('Especialista 003', 48, 'Rua Gamma, 30'),
    ('Especialista 004', 35, 'Rua Delta, 40'),
    ('Especialista 005', 55, 'Rua Epsilon, 50')
) AS novos_especialistas (nome, idade, endereco)
WHERE NOT EXISTS (SELECT 1 FROM Usuario WHERE nome = novos_especialistas.nome);

-- Inserção dos Especialistas na tabela Especialista
WITH specialists AS (
    SELECT u.id,
           CASE u.nome
               WHEN 'Especialista 001' THEN 'CRFA-10001'
               WHEN 'Especialista 002' THEN 'CRFA-10002'
               WHEN 'Especialista 003' THEN 'CRFA-10003'
               WHEN 'Especialista 004' THEN 'CRFA-10004'
               ELSE 'CRFA-10005'
           END AS crm_fono,
           'Fonoaudiologia' AS especialidade
    FROM Usuario u
    WHERE u.nome LIKE 'Especialista %'
)
INSERT INTO Especialista (usuario_id, crmFono, especialidade)
SELECT id, crm_fono, especialidade
FROM specialists
ON CONFLICT (usuario_id) DO NOTHING;

------------------------------------------
-- 4) Disponibilidades para 7 dias para todos os 5 Especialistas
------------------------------------------
-- Define os 5 Especialistas existentes
WITH especialistas AS (
    SELECT e.usuario_id AS especialista_id, u.nome
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
),
-- Define os 7 dias seguintes (offset 1 a 7)
dias AS (
    SELECT generate_series(1, 7) AS dia_offset
),
-- Define os horários padrão para cada especialista (strings, mas convertidas abaixo)
horarios_base AS (
    SELECT e.especialista_id, h.horaInicio, h.horaFim
    FROM especialistas e, (
        VALUES
        -- Especialista 001: 09:00-12:00 e 14:00-18:00
        ('Especialista 001', '09:00', '12:00'),
        ('Especialista 001', '14:00', '18:00'),
        -- Especialista 002: 08:00-11:00 e 13:00-17:00
        ('Especialista 002', '08:00', '11:00'),
        ('Especialista 002', '13:00', '17:00'),
        -- Especialista 003: 10:00-13:00 e 15:00-19:00
        ('Especialista 003', '10:00', '13:00'),
        ('Especialista 003', '15:00', '19:00'),
        -- Especialista 004: 09:30-12:30 e 14:30-17:30
        ('Especialista 004', '09:30', '12:30'),
        ('Especialista 004', '14:30', '17:30'),
        -- Especialista 005: 07:00-11:00 e 13:00-16:00
        ('Especialista 005', '07:00', '11:00'),
        ('Especialista 005', '13:00', '16:00')
    ) AS h (especialista_nome_ref, horaInicio, horaFim)
    WHERE e.nome = h.especialista_nome_ref
)
-- Insere todas as combinações (70 disponibilidades)
INSERT INTO Disponibilidade (especialista_id, data, horaInicio, horaFim, status)
SELECT h.especialista_id, CURRENT_DATE + d.dia_offset, h.horaInicio::TIME, h.horaFim::TIME, 'DISPONIVEL'
FROM horarios_base h, dias d
WHERE NOT EXISTS (
    SELECT 1 FROM Disponibilidade disp
    WHERE disp.especialista_id = h.especialista_id
      AND disp.data = CURRENT_DATE + d.dia_offset
      -- CORREÇÃO DE TIPAGEM APLICADA ABAIXO:
      AND disp.horaInicio = h.horaInicio::TIME
      AND disp.horaFim = h.horaFim::TIME
);

------------------------------------------
-- 5) Consultas Base (Agendamentos dos 6 clientes originais)
------------------------------------------
WITH consultas_base AS (
    SELECT * FROM (
        VALUES
        -- Ana
        ('Ana', 'Especialista 001', 1, '10:00', 'AVALIACAO'),
        ('Ana', 'Especialista 001', 1, '15:00', 'RETORNO'),
        -- Bruno
        ('Bruno', 'Especialista 002', 2, '09:00', 'AVALIACAO'),
        ('Bruno', 'Especialista 002', 2, '14:00', 'RETORNO'),
        -- Carla
        ('Carla', 'Especialista 001', 1, '11:00', 'TRATAMENTO'),
        -- Diego
        ('Diego', 'Especialista 002', 3, '10:00', 'EXAME'),
        -- Elisa
        ('Elisa', 'Especialista 001', 3, '15:00', 'RETORNO'),
        -- Fabio
        ('Fabio', 'Especialista 002', 1, '10:00', 'AVALIACAO')
    ) AS t (cliente_nome, especialista_nome, dia_offset, hora, tipo)
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT c.usuario_id, e.usuario_id, CURRENT_DATE + cb.dia_offset, cb.hora::TIME, cb.tipo, 'AGENDADA'
FROM consultas_base cb
JOIN Usuario uc ON uc.nome = cb.cliente_nome
JOIN Cliente c ON c.usuario_id = uc.id
JOIN Usuario ue ON ue.nome = cb.especialista_nome
JOIN Especialista e ON e.usuario_id = ue.id
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = c.usuario_id
      AND co.especialista_id = e.usuario_id
      AND co.data = CURRENT_DATE + cb.dia_offset
      -- CORREÇÃO DE TIPAGEM JÁ APLICADA ABAIXO:
      AND co.hora = cb.hora::TIME
);

------------------------------------------
-- 6) Consultas Adicionais (Agendamentos dos 10 novos clientes)
------------------------------------------
WITH consultas_adicionais AS (
    SELECT * FROM (
        VALUES
        -- Gustavo (Dia 1, 3)
        ('Gustavo', 'Especialista 003', 1, '11:00', 'AVALIACAO'),
        ('Gustavo', 'Especialista 003', 3, '16:00', 'RETORNO'),
        -- Helena (Dia 2, 4)
        ('Helena', 'Especialista 004', 2, '10:00', 'TRATAMENTO'),
        ('Helena', 'Especialista 004', 4, '15:00', 'EXAME'),
        -- Igor (Dia 3, 5)
        ('Igor', 'Especialista 005', 3, '08:00', 'AVALIACAO'),
        ('Igor', 'Especialista 005', 5, '14:00', 'RETORNO'),
        -- Julia (Dia 4, 6)
        ('Julia', 'Especialista 001', 4, '16:00', 'TRATAMENTO'),
        ('Julia', 'Especialista 001', 6, '10:00', 'AVALIACAO'),
        -- Kevin (Dia 5, 7)
        ('Kevin', 'Especialista 002', 5, '15:00', 'EXAME'),
        ('Kevin', 'Especialista 002', 7, '09:00', 'RETORNO'),
        -- Laura (Dia 6, 1)
        ('Laura', 'Especialista 003', 6, '12:00', 'AVALIACAO'),
        ('Laura', 'Especialista 003', 1, '17:00', 'TRATAMENTO'),
        -- Marcelo (Dia 7, 2)
        ('Marcelo', 'Especialista 004', 7, '11:30', 'RETORNO'),
        ('Marcelo', 'Especialista 004', 2, '15:30', 'AVALIACAO'),
        -- Natalia (Dia 1, 3)
        ('Natalia', 'Especialista 005', 1, '10:30', 'EXAME'),
        ('Natalia', 'Especialista 005', 3, '13:30', 'TRATAMENTO'),
        -- Otavio (Dia 2, 4)
        ('Otavio', 'Especialista 001', 2, '09:30', 'AVALIACAO'),
        ('Otavio', 'Especialista 001', 4, '14:30', 'RETORNO'),
        -- Patricia (Dia 5, 7)
        ('Patricia', 'Especialista 002', 5, '10:30', 'TRATAMENTO'),
        ('Patricia', 'Especialista 002', 7, '14:30', 'AVALIACAO')
    ) AS t (cliente_nome, especialista_nome, dia_offset, hora, tipo)
)
INSERT INTO Consulta (cliente_id, especialista_id, data, hora, tipo, status)
SELECT c.usuario_id, e.usuario_id, CURRENT_DATE + ca.dia_offset, ca.hora::TIME, ca.tipo, 'AGENDADA'
FROM consultas_adicionais ca
JOIN Usuario uc ON uc.nome = ca.cliente_nome
JOIN Cliente c ON c.usuario_id = uc.id
JOIN Usuario ue ON ue.nome = ca.especialista_nome
JOIN Especialista e ON e.usuario_id = ue.id
WHERE NOT EXISTS (
    SELECT 1 FROM Consulta co
    WHERE co.cliente_id = c.usuario_id
      AND co.especialista_id = e.usuario_id
      AND co.data = CURRENT_DATE + ca.dia_offset
      -- CORREÇÃO DE TIPAGEM JÁ APLICADA ABAIXO:
      AND co.hora = ca.hora::TIME
);

-- Você pode adicionar aqui as demais inserções de tabelas secundárias (Chat, Relatorio, Tratamento, etc.)
-- do seu script V3/V5 original que não foram incluídas neste foco em Agendamento.