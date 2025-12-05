-- Migração V8: alinhar Chat para relacionar com Especialista
-- 1) Adiciona coluna especialista_id e FK para Especialista
ALTER TABLE Chat
    ADD COLUMN IF NOT EXISTS especialista_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_chat_especialista'
    ) THEN
        ALTER TABLE Chat
            ADD CONSTRAINT fk_chat_especialista
            FOREIGN KEY (especialista_id) REFERENCES Especialista(usuario_id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_chat_especialista ON Chat (especialista_id);

-- 2) Recria o chat semente (Cliente Demo x Especialista Demo) se necessário
WITH cli AS (
    SELECT c.usuario_id AS cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Demo'
), esp AS (
    SELECT e.usuario_id AS especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista Demo'
), novo AS (
    INSERT INTO Chat (cliente_id, especialista_id, duracao, conversa)
    SELECT cli.cliente_id, esp.especialista_id, 45, 'Conversa inicial de avaliação com especialista'
    FROM cli, esp
    WHERE NOT EXISTS (
        SELECT 1 FROM Chat ch WHERE ch.cliente_id = cli.cliente_id AND ch.especialista_id = esp.especialista_id
    )
    RETURNING id AS new_chat_id
)
SELECT 1;

DO $$
DECLARE
    v_cliente_id BIGINT;
    v_especialista_id BIGINT;
    col_prof_exists BOOLEAN;
BEGIN
    SELECT c.usuario_id INTO v_cliente_id
    FROM Cliente c JOIN Usuario u ON u.id = c.usuario_id
    WHERE u.nome = 'Cliente Demo';

    SELECT e.usuario_id INTO v_especialista_id
    FROM Especialista e JOIN Usuario u ON u.id = e.usuario_id
    WHERE u.nome = 'Especialista Demo';

    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'chat' AND column_name = 'profissional_id'
    ) INTO col_prof_exists;

    IF col_prof_exists THEN
        UPDATE Relatorio r
        SET chat_id = COALESCE(
                (SELECT ch.id FROM Chat ch WHERE ch.cliente_id = v_cliente_id AND ch.especialista_id = v_especialista_id),
                r.chat_id
            ),
            especialista_id = v_especialista_id
        WHERE r.chat_id IN (
            SELECT ch.id FROM Chat ch
            WHERE ch.cliente_id = v_cliente_id
              AND ch.profissional_id = (
                  SELECT p.usuario_id FROM Profissional p JOIN Usuario u ON u.id = p.usuario_id WHERE u.nome = 'Profissional Demo'
              )
        );

        DELETE FROM Chat
        WHERE id IN (
            SELECT ch.id FROM Chat ch
            WHERE ch.cliente_id = v_cliente_id
              AND ch.profissional_id = (
                  SELECT p.usuario_id FROM Profissional p JOIN Usuario u ON u.id = p.usuario_id WHERE u.nome = 'Profissional Demo'
              )
        );
    END IF;
END
$$;

-- 3) Remove o chat antigo baseado em Profissional, se existir
-- Nota: coluna profissional_id é mantida para compatibilidade histórica. O código agora usa especialista_id.
