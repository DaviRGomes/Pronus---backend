-- V10: Ajustar colunas da tabela sessaotreino para corresponder à entidade Java
-- Renomeia colunas para snake_case e adiciona campos faltantes

-- 1. Renomear colunas para padrão snake_case esperado pelo JPA
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='sessaotreino' AND column_name='idadecliente') THEN
        ALTER TABLE sessaotreino RENAME COLUMN idadecliente TO idade_cliente;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='sessaotreino' AND column_name='datainicio') THEN
        ALTER TABLE sessaotreino RENAME COLUMN datainicio TO data_inicio;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='sessaotreino' AND column_name='datafim') THEN
        ALTER TABLE sessaotreino RENAME COLUMN datafim TO data_fim;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='sessaotreino' AND column_name='historicoconversa') THEN
        ALTER TABLE sessaotreino RENAME COLUMN historicoconversa TO historico_conversa;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='sessaotreino' AND column_name='totalpalavras') THEN
        ALTER TABLE sessaotreino RENAME COLUMN totalpalavras TO total_palavras;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='sessaotreino' AND column_name='totalacertos') THEN
        ALTER TABLE sessaotreino RENAME COLUMN totalacertos TO total_acertos;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='sessaotreino' AND column_name='pontuacaogeral') THEN
        ALTER TABLE sessaotreino RENAME COLUMN pontuacaogeral TO pontuacao_geral;
    END IF;
END
$$;

-- 2. Adicionar colunas que existem na entidade Java mas não no banco
ALTER TABLE sessaotreino ADD COLUMN IF NOT EXISTS trava_lingua TEXT;
ALTER TABLE sessaotreino ADD COLUMN IF NOT EXISTS resultado TEXT;
