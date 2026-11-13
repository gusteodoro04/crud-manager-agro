-- =====================================================================
--  CRUD Manager  |  MIGRAÇÃO para a atividade 3  (NÃO apaga nada)
--
--  Use este arquivo se o banco crud_manager JÁ EXISTE e tem dados que você
--  quer manter. Ele pode ser executado mais de uma vez sem estragar nada.
--
--  O que ele faz:
--    1) acrescenta a coluna users.password_hash (login)
--    2) define a senha padrão  agro123  para quem ainda não tem senha
--    3) cria as tabelas rural_properties e plantings
--    4) insere dados de exemplo (só se as tabelas novas estiverem vazias;
--       usuários/posts/empresas extras só entram se ainda não existirem)
--
--  Como rodar (MySQL 8):   mysql -u root -p crud_manager < script-migracao-atividade3.sql
-- =====================================================================

-- Lê este arquivo como UTF-8 (acentos) mesmo que o cliente use outra codificação
-- (o prompt do Windows, por exemplo). Sem isso, "Rosânia" vira "RosÃ¢nia".
SET NAMES utf8mb4;

USE crud_manager;

-- 1) coluna password_hash em users (só se ainda não existir)
SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'password_hash');
SET @ddl := IF(@col = 0,
               'ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER email',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) senha padrão (agro123) para quem ainda não tem senha
UPDATE users SET password_hash = 'pbkdf2_sha256$600000$TAiz/HWCz1OFoFhzkuTC/g==$R9/f5AZXa98OzSyI0ZppOz8YTYx2iPjx7TiaPKGfDis=' WHERE password_hash IS NULL;

-- 3) e-mail único (necessário para o login) - só cria se não houver e-mails repetidos
SET @dup := (SELECT COUNT(*) FROM (SELECT email FROM users GROUP BY email HAVING COUNT(*) > 1) d);
SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
              WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND INDEX_NAME = 'uq_users_email');
SET @ddl := IF(@idx = 0 AND @dup = 0,
               'ALTER TABLE users ADD UNIQUE KEY uq_users_email (email)',
               'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) NOVAS TABELAS da atividade 3
CREATE TABLE IF NOT EXISTS rural_properties (
    id            INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120)  NOT NULL,
    property_type ENUM('FAZENDA', 'SITIO', 'CHACARA', 'GRANJA') NOT NULL DEFAULT 'FAZENDA',
    city          VARCHAR(100)  NOT NULL,
    state         CHAR(2)       NOT NULL,
    area          DECIMAL(10,2) NOT NULL,           -- hectares
    acquired_on   DATE          NULL,
    irrigated     BOOLEAN       NOT NULL DEFAULT FALSE,
    map_url       VARCHAR(500)  NULL,
    notes         TEXT          NULL,
    owner_id      INT           NOT NULL,           -- FK: quem é o dono (users)
    CONSTRAINT fk_property_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT chk_property_area CHECK (area > 0)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS plantings (
    id               INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    crop             VARCHAR(80)   NOT NULL,          -- cultura: Café, Soja, Milho...
    variety          VARCHAR(80)   NULL,              -- cultivar / variedade
    planting_date    DATE          NOT NULL,
    expected_harvest DATE          NOT NULL,
    area             DECIMAL(10,2) NOT NULL,          -- hectares plantados
    expected_yield   DECIMAL(8,2)  NOT NULL DEFAULT 0, -- produtividade esperada (sacas por hectare)
    status           ENUM('PLANNED', 'GROWING', 'HARVESTED', 'LOST') NOT NULL DEFAULT 'PLANNED',
    notes            TEXT          NULL,
    property_id      INT           NOT NULL,          -- FK: onde foi plantado (rural_properties)
    manager_id       INT           NOT NULL,          -- FK: quem é o responsável (users)
    CONSTRAINT fk_planting_property FOREIGN KEY (property_id) REFERENCES rural_properties (id),
    CONSTRAINT fk_planting_manager  FOREIGN KEY (manager_id)  REFERENCES users (id),
    CONSTRAINT chk_planting_area    CHECK (area > 0),
    CONSTRAINT chk_planting_dates   CHECK (expected_harvest >= planting_date),
    INDEX idx_planting_status (status),
    INDEX idx_planting_date (planting_date)
) DEFAULT CHARSET = utf8mb4;

-- 5) dados de exemplo ---------------------------------------------------
-- usuários extras (para a paginação aparecer em Usuários)
INSERT INTO users (nome, sexo, email, password_hash)
SELECT 'Ana Beatriz Souza', 'F', 'ana.souza@mail.com', 'pbkdf2_sha256$600000$0kPxGyTDJgAXW51f8MAVTA==$vJRvsawREJkb9b/CDbUBBssKzsebe84I6NCZB+f+URg=' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'ana.souza@mail.com');
INSERT INTO users (nome, sexo, email, password_hash)
SELECT 'Carlos Eduardo Lima', 'M', 'carlos.lima@mail.com', 'pbkdf2_sha256$600000$aHYWq9PmUWcBzf/JqBmadw==$AxLymECrkljIdkuoPSQDGPZoLkCKd1QBGbYdy2rbz5E=' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'carlos.lima@mail.com');
INSERT INTO users (nome, sexo, email, password_hash)
SELECT 'Fernanda Alves', 'F', 'fernanda.alves@mail.com', 'pbkdf2_sha256$600000$kNhW5Y25EhWKPHBDReyayQ==$PjJu09wjmdCBZI6rKU2AyXrAmDNSWHtpdSTSvr60fnA=' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'fernanda.alves@mail.com');
INSERT INTO users (nome, sexo, email, password_hash)
SELECT 'Joaquim Pereira', 'M', 'joaquim.pereira@mail.com', 'pbkdf2_sha256$600000$QMnmX7uL+tC04eyw44TdbA==$JDvUmm7lgRWR9gglWRblaK7ALeCcbzJycWmXZ0iSexQ=' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'joaquim.pereira@mail.com');
INSERT INTO users (nome, sexo, email, password_hash)
SELECT 'Marta Ribeiro', 'F', 'marta.ribeiro@mail.com', 'pbkdf2_sha256$600000$BKK75+v4BAB71txsRk8j6Q==$Klx9IG3DfynKg+zE00BEKSlzX6delnX5BJTvIADhoZU=' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'marta.ribeiro@mail.com');
INSERT INTO users (nome, sexo, email, password_hash)
SELECT 'Paulo Henrique Silva', 'M', 'paulo.silva@mail.com', 'pbkdf2_sha256$600000$s3/NkAIX/VXVO7kzNlmrbg==$n0rmkD//uA258BEjJStchvv6XPv+LlSf8Vgfs9P3vRU=' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'paulo.silva@mail.com');
INSERT INTO users (nome, sexo, email, password_hash)
SELECT 'Teresa Oliveira', 'F', 'teresa.oliveira@mail.com', 'pbkdf2_sha256$600000$DVBeZKSpNX1IjCenW7G38w==$sQyYV5WhAKnGle4pxdBzZNMxVOe8K9Mtxiatgx2YvFk=' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'teresa.oliveira@mail.com');

-- descobre os ids dos usuários pelo e-mail (se não achar, usa o primeiro usuário)
SET @u1 := COALESCE((SELECT id FROM users WHERE email = 'emerson@mail.com'), (SELECT MIN(id) FROM users));
SET @u2 := COALESCE((SELECT id FROM users WHERE email = 'lu@mail.com'), @u1);
SET @u3 := COALESCE((SELECT id FROM users WHERE email = 'le@mail.com'), @u1);
SET @u4 := COALESCE((SELECT id FROM users WHERE email = 'noe@mail.com'), @u1);
SET @u5 := COALESCE((SELECT id FROM users WHERE email = 'ro@mail.com'), @u1);
SET @u6 := COALESCE((SELECT id FROM users WHERE email = 'ana.souza@mail.com'), @u1);
SET @u7 := COALESCE((SELECT id FROM users WHERE email = 'carlos.lima@mail.com'), @u1);
SET @u8 := COALESCE((SELECT id FROM users WHERE email = 'fernanda.alves@mail.com'), @u1);
SET @u9 := COALESCE((SELECT id FROM users WHERE email = 'joaquim.pereira@mail.com'), @u1);
SET @u10 := COALESCE((SELECT id FROM users WHERE email = 'marta.ribeiro@mail.com'), @u1);
SET @u11 := COALESCE((SELECT id FROM users WHERE email = 'paulo.silva@mail.com'), @u1);
SET @u12 := COALESCE((SELECT id FROM users WHERE email = 'teresa.oliveira@mail.com'), @u1);

-- posts e empresas extras (para a paginação aparecer nas outras listas)
INSERT INTO posts (content, post_date, user_id)
SELECT 'Começou a florada do café na Boa Esperança. Se o tempo ajudar, a safra vem forte.', DATE_ADD(CURDATE(), INTERVAL -1 DAY), @u1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'Começou a florada do café na Boa Esperança. Se o tempo ajudar, a safra vem forte.');
INSERT INTO posts (content, post_date, user_id)
SELECT 'Choveu 38 mm ontem à noite. Chuva boa para o plantio do feijão das águas.', DATE_ADD(CURDATE(), INTERVAL -2 DAY), @u2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'Choveu 38 mm ontem à noite. Chuva boa para o plantio do feijão das águas.');
INSERT INTO posts (content, post_date, user_id)
SELECT 'Alguém tem indicação de regulagem de colhedora de café em terreno inclinado?', DATE_ADD(CURDATE(), INTERVAL -3 DAY), @u6 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'Alguém tem indicação de regulagem de colhedora de café em terreno inclinado?');
INSERT INTO posts (content, post_date, user_id)
SELECT 'Terminamos a adubação de cobertura do milho. Agora é torcer pela chuva.', DATE_ADD(CURDATE(), INTERVAL -3 DAY), @u7 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'Terminamos a adubação de cobertura do milho. Agora é torcer pela chuva.');
INSERT INTO posts (content, post_date, user_id)
SELECT 'Primeira colheita da horta da chácara: alface, couve e cebolinha.', DATE_ADD(CURDATE(), INTERVAL -4 DAY), @u8 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'Primeira colheita da horta da chácara: alface, couve e cebolinha.');
INSERT INTO posts (content, post_date, user_id)
SELECT 'A soja do Ouro Verde emergiu uniforme em cinco dias. Stand muito bom.', DATE_ADD(CURDATE(), INTERVAL -5 DAY), @u9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'A soja do Ouro Verde emergiu uniforme em cinco dias. Stand muito bom.');
INSERT INTO posts (content, post_date, user_id)
SELECT 'Dica do dia: faça a análise de solo antes de comprar o calcário.', DATE_ADD(CURDATE(), INTERVAL -6 DAY), @u10 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'Dica do dia: faça a análise de solo antes de comprar o calcário.');
INSERT INTO posts (content, post_date, user_id)
SELECT 'Dia de campo na cooperativa sobre manejo de pragas no cafezal.', DATE_ADD(CURDATE(), INTERVAL -7 DAY), @u11 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE content = 'Dia de campo na cooperativa sobre manejo de pragas no cafezal.');

INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Cooperativa Cafeeira do Sul', 'Técnico agrícola', '2015-02-02', '2019-12-20', @u1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Cooperativa Cafeeira do Sul' AND `role` = 'Técnico agrícola' AND user_id = @u1);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Agropecuária Boa Safra', 'Gerente comercial', '2020-01-15', NULL, @u1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Agropecuária Boa Safra' AND `role` = 'Gerente comercial' AND user_id = @u1);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Armazém São José', 'Conferente de grãos', '2018-06-01', '2021-03-31', @u4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Armazém São José' AND `role` = 'Conferente de grãos' AND user_id = @u4);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Sementes Santa Fé', 'Consultora de vendas', '2019-09-10', NULL, @u2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Sementes Santa Fé' AND `role` = 'Consultora de vendas' AND user_id = @u2);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Laticínios Vale Verde', 'Assistente de qualidade', '2017-04-03', '2022-08-15', @u3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Laticínios Vale Verde' AND `role` = 'Assistente de qualidade' AND user_id = @u3);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Fazenda Escola Monte Alto', 'Estagiária', '2023-02-01', '2023-12-20', @u5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Fazenda Escola Monte Alto' AND `role` = 'Estagiária' AND user_id = @u5);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Cerealista Três Pontas', 'Operador de máquinas', '2016-03-14', '2020-02-28', @u7 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Cerealista Três Pontas' AND `role` = 'Operador de máquinas' AND user_id = @u7);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Cooperativa Cafeeira do Sul', 'Analista de qualidade', '2021-05-03', NULL, @u6 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Cooperativa Cafeeira do Sul' AND `role` = 'Analista de qualidade' AND user_id = @u6);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Insumos Campo Forte', 'Representante técnico', '2019-07-22', '2024-01-31', @u9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Insumos Campo Forte' AND `role` = 'Representante técnico' AND user_id = @u9);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Viveiro Mudas do Sul', 'Viveirista', '2014-10-06', '2018-09-28', @u10 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Viveiro Mudas do Sul' AND `role` = 'Viveirista' AND user_id = @u10);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Transportadora Rota Verde', 'Motorista', '2022-04-11', NULL, @u11 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Transportadora Rota Verde' AND `role` = 'Motorista' AND user_id = @u11);
INSERT INTO companies (`name`, `role`, `start`, `end`, user_id)
SELECT 'Escritório Contábil Rural Alves', 'Contadora', '2018-02-19', NULL, @u8 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE `name` = 'Escritório Contábil Rural Alves' AND `role` = 'Contadora' AND user_id = @u8);

-- propriedades e plantios (só entram se as tabelas estiverem vazias)
INSERT INTO rural_properties
    (name, property_type, city, state, area, acquired_on, irrigated, map_url, notes, owner_id)
SELECT t.* FROM (
    SELECT 'Fazenda Boa Esperança' AS name, 'FAZENDA' AS property_type, 'Poço Fundo' AS city, 'MG' AS state, 86.40 AS area, '2009-03-12' AS acquired_on, FALSE AS irrigated, 'https://www.google.com/maps/search/?api=1&query=Po%C3%A7o+Fundo%2C+MG' AS map_url, 'Sede com terreiro e tulha. Lavoura de café em meia encosta.' AS notes, @u1 AS owner_id
    UNION ALL SELECT 'Sítio Santa Luzia', 'SITIO', 'Machado', 'MG', 12.75, '2016-08-20', TRUE, 'https://www.google.com/maps/search/?api=1&query=Machado%2C+MG', 'Nascente protegida e pequeno açude para irrigação.', @u2
    UNION ALL SELECT 'Chácara Recanto do Ipê', 'CHACARA', 'Alfenas', 'MG', 3.20, '2021-05-02', TRUE, 'https://www.google.com/maps/search/?api=1&query=Alfenas%2C+MG', 'Pomar e horta para consumo da família.', @u3
    UNION ALL SELECT 'Fazenda Cachoeirinha', 'FAZENDA', 'Campestre', 'MG', 142.00, '2004-11-30', FALSE, 'https://www.google.com/maps/search/?api=1&query=Campestre%2C+MG', 'Maior área do grupo. Parte em reserva legal.', @u4
    UNION ALL SELECT 'Granja Vale Verde', 'GRANJA', 'Varginha', 'MG', 18.50, '2018-01-15', TRUE, 'https://www.google.com/maps/search/?api=1&query=Varginha%2C+MG', 'Aviário de postura e área de pastagem.', @u5
    UNION ALL SELECT 'Sítio Três Barras', 'SITIO', 'Três Pontas', 'MG', 27.80, '2012-06-09', FALSE, 'https://www.google.com/maps/search/?api=1&query=Tr%C3%AAs+Pontas%2C+MG', 'Café e milho em rotação com feijão.', @u1
    UNION ALL SELECT 'Fazenda Santa Rita', 'FAZENDA', 'Guaxupé', 'MG', 64.30, '2011-02-14', TRUE, 'https://www.google.com/maps/search/?api=1&query=Guaxup%C3%A9%2C+MG', 'Irrigação por gotejamento nos talhões mais novos.', @u6
    UNION ALL SELECT 'Sítio Água Limpa', 'SITIO', 'Carmo do Rio Claro', 'MG', 9.60, '2019-10-05', FALSE, 'https://www.google.com/maps/search/?api=1&query=Carmo+do+Rio+Claro%2C+MG', 'Pequena propriedade familiar com milho e feijão.', @u7
    UNION ALL SELECT 'Chácara Bela Vista', 'CHACARA', 'Poços de Caldas', 'MG', 2.40, '2022-03-27', TRUE, NULL, 'Uso misto: lazer e horta orgânica.', @u8
    UNION ALL SELECT 'Fazenda Ouro Verde', 'FAZENDA', 'Boa Esperança', 'MG', 210.00, '2001-07-19', TRUE, NULL, 'Colheita mecanizada nos talhões planos.', @u9
    UNION ALL SELECT 'Sítio Pé de Serra', 'SITIO', 'Cabo Verde', 'MG', 15.20, '2015-12-01', FALSE, NULL, 'Café de altitude, perto da serra.', @u10
    UNION ALL SELECT 'Granja Primavera', 'GRANJA', 'Ipuiúna', 'MG', 11.80, '2020-06-18', TRUE, NULL, 'Criação de aves e cultivo de sorgo para ração.', @u11
) AS t
WHERE NOT EXISTS (SELECT 1 FROM rural_properties);

SET @p1 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Fazenda Boa Esperança');
SET @p2 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Sítio Santa Luzia');
SET @p3 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Chácara Recanto do Ipê');
SET @p4 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Fazenda Cachoeirinha');
SET @p5 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Granja Vale Verde');
SET @p6 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Sítio Três Barras');
SET @p7 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Fazenda Santa Rita');
SET @p8 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Sítio Água Limpa');
SET @p9 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Chácara Bela Vista');
SET @p10 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Fazenda Ouro Verde');
SET @p11 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Sítio Pé de Serra');
SET @p12 := (SELECT MIN(id) FROM rural_properties WHERE name = 'Granja Primavera');

INSERT INTO plantings
    (crop, variety, planting_date, expected_harvest, area, expected_yield, status, notes, property_id, manager_id)
SELECT t.* FROM (
    SELECT 'Café' AS crop, 'Catuaí Vermelho IAC 144' AS variety, DATE_ADD(CURDATE(), INTERVAL -2100 DAY) AS planting_date, DATE_ADD(CURDATE(), INTERVAL 205 DAY) AS expected_harvest, 24.00 AS area, 38.00 AS expected_yield, 'GROWING' AS status, 'Talhão 03, meia encosta. Poda programada para depois da colheita.' AS notes, COALESCE(@p1, (SELECT MIN(id) FROM rural_properties)) AS property_id, @u1 AS manager_id
    UNION ALL SELECT 'Café', 'Mundo Novo', DATE_ADD(CURDATE(), INTERVAL -3300 DAY), DATE_ADD(CURDATE(), INTERVAL 215 DAY), 18.50, 32.00, 'GROWING', 'Talhão 01, lavoura antiga. Recepa prevista para 2028.', COALESCE(@p1, (SELECT MIN(id) FROM rural_properties)), @u1
    UNION ALL SELECT 'Café', 'Arara', DATE_ADD(CURDATE(), INTERVAL -900 DAY), DATE_ADD(CURDATE(), INTERVAL 190 DAY), 12.00, 45.00, 'GROWING', 'Talhão 05, variedade resistente à ferrugem.', COALESCE(@p1, (SELECT MIN(id) FROM rural_properties)), @u6
    UNION ALL SELECT 'Café', 'Bourbon Amarelo', DATE_ADD(CURDATE(), INTERVAL -2500 DAY), DATE_ADD(CURDATE(), INTERVAL 225 DAY), 9.50, 28.00, 'GROWING', 'Café especial, colheita seletiva.', COALESCE(@p6, (SELECT MIN(id) FROM rural_properties)), @u1
    UNION ALL SELECT 'Café', 'Catuaí Amarelo IAC 62', DATE_ADD(CURDATE(), INTERVAL -1800 DAY), DATE_ADD(CURDATE(), INTERVAL 210 DAY), 30.00, 36.00, 'GROWING', 'Gotejamento no talhão 02.', COALESCE(@p7, (SELECT MIN(id) FROM rural_properties)), @u7
    UNION ALL SELECT 'Café', 'Topázio MG 1190', DATE_ADD(CURDATE(), INTERVAL -1200 DAY), DATE_ADD(CURDATE(), INTERVAL 215 DAY), 14.00, 40.00, 'GROWING', NULL, COALESCE(@p7, (SELECT MIN(id) FROM rural_properties)), @u7
    UNION ALL SELECT 'Café', 'Catuaí Vermelho IAC 144', DATE_ADD(CURDATE(), INTERVAL -2800 DAY), DATE_ADD(CURDATE(), INTERVAL 200 DAY), 60.00, 34.00, 'GROWING', 'Maior talhão da fazenda, colheita mecanizada.', COALESCE(@p4, (SELECT MIN(id) FROM rural_properties)), @u4
    UNION ALL SELECT 'Café', 'Paraíso MG H 419-1', DATE_ADD(CURDATE(), INTERVAL -1500 DAY), DATE_ADD(CURDATE(), INTERVAL 205 DAY), 75.00, 42.00, 'GROWING', 'Secagem em terreiro suspenso.', COALESCE(@p10, (SELECT MIN(id) FROM rural_properties)), @u9
    UNION ALL SELECT 'Café', 'Acaiá', DATE_ADD(CURDATE(), INTERVAL -3600 DAY), DATE_ADD(CURDATE(), INTERVAL -115 DAY), 8.00, 30.00, 'HARVESTED', 'Safra colhida, secagem concluída.', COALESCE(@p11, (SELECT MIN(id) FROM rural_properties)), @u10
    UNION ALL SELECT 'Soja', 'Ciclo médio', DATE_ADD(CURDATE(), INTERVAL -40 DAY), DATE_ADD(CURDATE(), INTERVAL 105 DAY), 40.00, 62.00, 'GROWING', 'Plantio direto sobre palhada de milho.', COALESCE(@p4, (SELECT MIN(id) FROM rural_properties)), @u4
    UNION ALL SELECT 'Soja', 'Ciclo precoce', DATE_ADD(CURDATE(), INTERVAL -30 DAY), DATE_ADD(CURDATE(), INTERVAL 95 DAY), 55.00, 60.00, 'GROWING', 'Dessecação feita 15 dias antes do plantio.', COALESCE(@p10, (SELECT MIN(id) FROM rural_properties)), @u9
    UNION ALL SELECT 'Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL -35 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 35.00, 150.00, 'GROWING', 'Adubação de cobertura aos 25 dias.', COALESCE(@p4, (SELECT MIN(id) FROM rural_properties)), @u4
    UNION ALL SELECT 'Milho', 'Híbrido duplo', DATE_ADD(CURDATE(), INTERVAL -25 DAY), DATE_ADD(CURDATE(), INTERVAL 125 DAY), 45.00, 140.00, 'GROWING', NULL, COALESCE(@p10, (SELECT MIN(id) FROM rural_properties)), @u9
    UNION ALL SELECT 'Milho', 'Crioulo', DATE_ADD(CURDATE(), INTERVAL -128 DAY), DATE_ADD(CURDATE(), INTERVAL -3 DAY), 3.50, 90.00, 'GROWING', 'Variedade crioula para silagem e consumo próprio.', COALESCE(@p2, (SELECT MIN(id) FROM rural_properties)), @u2
    UNION ALL SELECT 'Feijão', 'Carioca (das águas)', DATE_ADD(CURDATE(), INTERVAL -75 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 6.00, 38.00, 'GROWING', 'Irrigado por aspersão. Colheita chegando.', COALESCE(@p5, (SELECT MIN(id) FROM rural_properties)), @u5
    UNION ALL SELECT 'Feijão', 'Preto', DATE_ADD(CURDATE(), INTERVAL -92 DAY), DATE_ADD(CURDATE(), INTERVAL -6 DAY), 4.00, 32.00, 'GROWING', 'Colheita adiada pelas chuvas. Atualizar a situação quando terminar.', COALESCE(@p8, (SELECT MIN(id) FROM rural_properties)), @u7
    UNION ALL SELECT 'Sorgo', 'Granífero', DATE_ADD(CURDATE(), INTERVAL -100 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), 5.00, 90.00, 'GROWING', 'Para ração das aves.', COALESCE(@p12, (SELECT MIN(id) FROM rural_properties)), @u11
    UNION ALL SELECT 'Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 260 DAY), 8.00, 140.00, 'PLANNED', 'Safrinha depois do feijão das águas.', COALESCE(@p5, (SELECT MIN(id) FROM rural_properties)), @u5
    UNION ALL SELECT 'Soja', 'Ciclo precoce', DATE_ADD(CURDATE(), INTERVAL 35 DAY), DATE_ADD(CURDATE(), INTERVAL 140 DAY), 20.00, 58.00, 'PLANNED', 'Aguardando a regularização das chuvas.', COALESCE(@p7, (SELECT MIN(id) FROM rural_properties)), @u7
    UNION ALL SELECT 'Feijão', 'Carioca (da seca)', DATE_ADD(CURDATE(), INTERVAL 130 DAY), DATE_ADD(CURDATE(), INTERVAL 205 DAY), 3.00, 30.00, 'PLANNED', NULL, COALESCE(@p2, (SELECT MIN(id) FROM rural_properties)), @u2
    UNION ALL SELECT 'Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL 150 DAY), DATE_ADD(CURDATE(), INTERVAL 290 DAY), 30.00, 120.00, 'PLANNED', 'Safrinha depois da soja.', COALESCE(@p4, (SELECT MIN(id) FROM rural_properties)), @u4
    UNION ALL SELECT 'Café', 'Arara', DATE_ADD(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 900 DAY), 1.20, 20.00, 'PLANNED', 'Mudas encomendadas. Primeira colheita em cerca de 2,5 anos.', COALESCE(@p9, (SELECT MIN(id) FROM rural_properties)), @u8
    UNION ALL SELECT 'Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL -260 DAY), DATE_ADD(CURDATE(), INTERVAL -130 DAY), 50.00, 145.00, 'HARVESTED', 'Safra anterior colhida.', COALESCE(@p10, (SELECT MIN(id) FROM rural_properties)), @u9
    UNION ALL SELECT 'Soja', 'Ciclo médio', DATE_ADD(CURDATE(), INTERVAL -300 DAY), DATE_ADD(CURDATE(), INTERVAL -170 DAY), 38.00, 61.00, 'HARVESTED', NULL, COALESCE(@p4, (SELECT MIN(id) FROM rural_properties)), @u4
    UNION ALL SELECT 'Feijão', 'Carioca (da seca)', DATE_ADD(CURDATE(), INTERVAL -240 DAY), DATE_ADD(CURDATE(), INTERVAL -150 DAY), 2.80, 34.00, 'HARVESTED', NULL, COALESCE(@p2, (SELECT MIN(id) FROM rural_properties)), @u2
    UNION ALL SELECT 'Milho', 'Híbrido duplo', DATE_ADD(CURDATE(), INTERVAL -250 DAY), DATE_ADD(CURDATE(), INTERVAL -120 DAY), 14.00, 135.00, 'HARVESTED', 'Colheita mecânica.', COALESCE(@p6, (SELECT MIN(id) FROM rural_properties)), @u1
    UNION ALL SELECT 'Soja', 'Ciclo precoce', DATE_ADD(CURDATE(), INTERVAL -310 DAY), DATE_ADD(CURDATE(), INTERVAL -180 DAY), 5.00, 60.00, 'HARVESTED', NULL, COALESCE(@p8, (SELECT MIN(id) FROM rural_properties)), @u7
    UNION ALL SELECT 'Feijão', 'Preto', DATE_ADD(CURDATE(), INTERVAL -170 DAY), DATE_ADD(CURDATE(), INTERVAL -95 DAY), 3.00, 28.00, 'LOST', 'Perda total por geada em julho.', COALESCE(@p12, (SELECT MIN(id) FROM rural_properties)), @u11
    UNION ALL SELECT 'Milho', 'Safrinha', DATE_ADD(CURDATE(), INTERVAL -230 DAY), DATE_ADD(CURDATE(), INTERVAL -105 DAY), 6.00, 100.00, 'LOST', 'Estiagem prolongada na fase de enchimento de grãos.', COALESCE(@p11, (SELECT MIN(id) FROM rural_properties)), @u10
) AS t
WHERE NOT EXISTS (SELECT 1 FROM plantings);
