-- V6: Adicionar colunas login e senha na tabela Usuario e popular dados existentes

-- Adiciona as colunas 'login' e 'senha' na tabela 'Usuario'
ALTER TABLE Usuario
ADD COLUMN IF NOT EXISTS login VARCHAR(255),
ADD COLUMN IF NOT EXISTS senha VARCHAR(255);

-- Preenche login e senha derivados antes de aplicar NOT NULL e UNIQUE
UPDATE Usuario
SET login = LOWER(REPLACE(nome, ' ', '.')) || '.' || id
WHERE login IS NULL;

UPDATE Usuario
SET senha = '$2a$10$gCL23bi2jTYT2s2yRqR.GeiS9f5t2p.i29saderj.gc7rA81y/96S'
WHERE senha IS NULL;

-- Altera as colunas para serem NOT NULL
ALTER TABLE Usuario
ALTER COLUMN login SET NOT NULL,
ALTER COLUMN senha SET NOT NULL;

-- Adiciona a constraint de unicidade para o login
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_usuario_login'
    ) THEN
        ALTER TABLE Usuario
        ADD CONSTRAINT uk_usuario_login UNIQUE (login);
    END IF;
END
$$;

-- Atualiza os registros existentes com um login padronizado e uma senha criptografada
-- A senha para todos os usuários será '123456'
-- O hash bcrypt para '123456' é: $2a$10$gCL23bi2jTYT2s2yRqR.GeiS9f5t2p.i29saderj.gc7rA81y/96S
-- Ajuste final de consistência (idempotente): mantém formato único login
UPDATE Usuario u
SET login = LOWER(REPLACE(u.nome, ' ', '.')) || '.' || u.id
WHERE u.login IS NOT NULL;
