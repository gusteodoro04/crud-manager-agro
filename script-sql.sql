-- =====================================================================
--  CRUD Manager  |  script COMPLETO (banco novo)
--  Tema da atividade 3: Propriedade Rural + Plantio
--
--  ATENÇÃO: este script começa com DROP DATABASE e recria o banco do zero.
--  Se você já tem dados no crud_manager e quer MANTÊ-LOS, não rode este
--  arquivo: rode o  script-migracao-atividade3.sql  (ele não apaga nada).
--
--  Como rodar (MySQL 8):   mysql -u root -p < script-sql.sql
--  Login de teste:         emerson@mail.com  /  senha: agro123
--                          (todos os usuários de exemplo usam a mesma senha)
-- =====================================================================

-- Lê este arquivo como UTF-8 (acentos) mesmo que o cliente use outra codificação
-- (o prompt do Windows, por exemplo). Sem isso, "Rosânia" vira "RosÃ¢nia".
SET NAMES utf8mb4;

DROP DATABASE IF EXISTS crud_manager;

CREATE DATABASE crud_manager CHARACTER SET utf8mb4;

USE crud_manager;

-- ---------------------------------------------------------------------
--  Tabelas que já existiam no projeto base
--  (users ganhou a coluna password_hash para o login)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id            INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    sexo          ENUM('M', 'F'),
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NULL,      -- hash PBKDF2 (nunca a senha em texto puro)
    UNIQUE KEY uq_users_email (email)
);

CREATE TABLE posts (
    id        INT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content   TEXT NOT NULL,
    post_date DATE NOT NULL,
    user_id   INT  NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE companies (
    id      INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name`  VARCHAR(128) NOT NULL,
    `role`  VARCHAR(128) NOT NULL,
    `start` DATE         NOT NULL,
    `end`   DATE,
    user_id INT          NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------
--  NOVAS TABELAS da atividade 3
-- ---------------------------------------------------------------------
-- Propriedade rural: cada uma pertence a um usuário (owner_id -> users.id)
CREATE TABLE rural_properties (
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

-- Plantio: acontece em uma propriedade (property_id -> rural_properties.id)
-- e tem um usuário responsável (manager_id -> users.id)
CREATE TABLE plantings (
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

-- ---------------------------------------------------------------------
--  Dados de exemplo
--  (as datas dos plantios são relativas a CURDATE(): o painel sempre
--   mostra uma safra "viva", não importa o dia em que o script rodar)
-- ---------------------------------------------------------------------
INSERT INTO users (id, nome, sexo, email, password_hash) VALUES
    (1, 'Emerson Carvalho', 'M', 'emerson@mail.com', 'pbkdf2_sha256$600000$TAiz/HWCz1OFoFhzkuTC/g==$R9/f5AZXa98OzSyI0ZppOz8YTYx2iPjx7TiaPKGfDis='),
    (2, 'Luiza Carvalho', 'F', 'lu@mail.com', 'pbkdf2_sha256$600000$sG0cOa+jK+XHBldMRJC9Yg==$9AahWnNkxBpNJvmeYeDhi5wX9FT1WX6kRUyTS0t4hR8='),
    (3, 'Elenice Carvalho', 'F', 'le@mail.com', 'pbkdf2_sha256$600000$O8nqjd4mxgN9fHqMU68mww==$V24ctUf7t+JzGShCPZlbITAOVH+c9p9Wb8aZI9kJtiU='),
    (4, 'Noé Carvalho', 'M', 'noe@mail.com', 'pbkdf2_sha256$600000$vwaGqx5uA/y2HttsEbLGFA==$1GBSYvHXQ8x9yt5xYg24kIC2M2M0jR2kisEkItDmGEs='),
    (5, 'Rosânia Carvalho', 'F', 'ro@mail.com', 'pbkdf2_sha256$600000$aaGv2k8quWzFjCOzh4H88g==$RqCN4kvRl4WLtx9ztT22AV/p706Hm8zsAHLiCANm6tg='),
    (6, 'Ana Beatriz Souza', 'F', 'ana.souza@mail.com', 'pbkdf2_sha256$600000$0kPxGyTDJgAXW51f8MAVTA==$vJRvsawREJkb9b/CDbUBBssKzsebe84I6NCZB+f+URg='),
    (7, 'Carlos Eduardo Lima', 'M', 'carlos.lima@mail.com', 'pbkdf2_sha256$600000$aHYWq9PmUWcBzf/JqBmadw==$AxLymECrkljIdkuoPSQDGPZoLkCKd1QBGbYdy2rbz5E='),
    (8, 'Fernanda Alves', 'F', 'fernanda.alves@mail.com', 'pbkdf2_sha256$600000$kNhW5Y25EhWKPHBDReyayQ==$PjJu09wjmdCBZI6rKU2AyXrAmDNSWHtpdSTSvr60fnA='),
    (9, 'Joaquim Pereira', 'M', 'joaquim.pereira@mail.com', 'pbkdf2_sha256$600000$QMnmX7uL+tC04eyw44TdbA==$JDvUmm7lgRWR9gglWRblaK7ALeCcbzJycWmXZ0iSexQ='),
    (10, 'Marta Ribeiro', 'F', 'marta.ribeiro@mail.com', 'pbkdf2_sha256$600000$BKK75+v4BAB71txsRk8j6Q==$Klx9IG3DfynKg+zE00BEKSlzX6delnX5BJTvIADhoZU='),
    (11, 'Paulo Henrique Silva', 'M', 'paulo.silva@mail.com', 'pbkdf2_sha256$600000$s3/NkAIX/VXVO7kzNlmrbg==$n0rmkD//uA258BEjJStchvv6XPv+LlSf8Vgfs9P3vRU='),
    (12, 'Teresa Oliveira', 'F', 'teresa.oliveira@mail.com', 'pbkdf2_sha256$600000$DVBeZKSpNX1IjCenW7G38w==$sQyYV5WhAKnGle4pxdBzZNMxVOe8K9Mtxiatgx2YvFk=');

INSERT INTO posts (content, post_date, user_id) VALUES
    ('Olá do Emerson', CURDATE(), 1),
    ('Olá da Luiza', CURDATE(), 2),
    ('Olá da Denise', CURDATE(), 3),
    ('Olá do Noé', CURDATE(), 4),
    ('Olá da Rosânia', CURDATE(), 5),
    ('Olá da Rosânia 1', CURDATE(), 5),
    ('Olá da Rosânia 2', CURDATE(), 5),
    ('Olá da Rosânia 3', CURDATE(), 5),
    ('Começou a florada do café na Boa Esperança. Se o tempo ajudar, a safra vem forte.', DATE_ADD(CURDATE(), INTERVAL -1 DAY), 1),
    ('Choveu 38 mm ontem à noite. Chuva boa para o plantio do feijão das águas.', DATE_ADD(CURDATE(), INTERVAL -2 DAY), 2),
    ('Alguém tem indicação de regulagem de colhedora de café em terreno inclinado?', DATE_ADD(CURDATE(), INTERVAL -3 DAY), 6),
    ('Terminamos a adubação de cobertura do milho. Agora é torcer pela chuva.', DATE_ADD(CURDATE(), INTERVAL -3 DAY), 7),
    ('Primeira colheita da horta da chácara: alface, couve e cebolinha.', DATE_ADD(CURDATE(), INTERVAL -4 DAY), 8),
    ('A soja do Ouro Verde emergiu uniforme em cinco dias. Stand muito bom.', DATE_ADD(CURDATE(), INTERVAL -5 DAY), 9),
    ('Dica do dia: faça a análise de solo antes de comprar o calcário.', DATE_ADD(CURDATE(), INTERVAL -6 DAY), 10),
    ('Dia de campo na cooperativa sobre manejo de pragas no cafezal.', DATE_ADD(CURDATE(), INTERVAL -7 DAY), 11);

INSERT INTO companies (`name`, `role`, `start`, `end`, user_id) VALUES
    ('Cooperativa Cafeeira do Sul', 'Técnico agrícola', '2015-02-02', '2019-12-20', 1),
    ('Agropecuária Boa Safra', 'Gerente comercial', '2020-01-15', NULL, 1),
    ('Armazém São José', 'Conferente de grãos', '2018-06-01', '2021-03-31', 4),
    ('Sementes Santa Fé', 'Consultora de vendas', '2019-09-10', NULL, 2),
    ('Laticínios Vale Verde', 'Assistente de qualidade', '2017-04-03', '2022-08-15', 3),
    ('Fazenda Escola Monte Alto', 'Estagiária', '2023-02-01', '2023-12-20', 5),
    ('Cerealista Três Pontas', 'Operador de máquinas', '2016-03-14', '2020-02-28', 7),
    ('Cooperativa Cafeeira do Sul', 'Analista de qualidade', '2021-05-03', NULL, 6),
    ('Insumos Campo Forte', 'Representante técnico', '2019-07-22', '2024-01-31', 9),
    ('Viveiro Mudas do Sul', 'Viveirista', '2014-10-06', '2018-09-28', 10),
    ('Transportadora Rota Verde', 'Motorista', '2022-04-11', NULL, 11),
    ('Escritório Contábil Rural Alves', 'Contadora', '2018-02-19', NULL, 8);

INSERT INTO rural_properties
    (name, property_type, city, state, area, acquired_on, irrigated, map_url, notes, owner_id)
VALUES
    ('Fazenda Boa Esperança', 'FAZENDA', 'Poço Fundo', 'MG', 86.40, '2009-03-12', FALSE, 'https://www.google.com/maps/search/?api=1&query=Po%C3%A7o+Fundo%2C+MG', 'Sede com terreiro e tulha. Lavoura de café em meia encosta.', 1),
    ('Sítio Santa Luzia', 'SITIO', 'Machado', 'MG', 12.75, '2016-08-20', TRUE, 'https://www.google.com/maps/search/?api=1&query=Machado%2C+MG', 'Nascente protegida e pequeno açude para irrigação.', 2),
    ('Chácara Recanto do Ipê', 'CHACARA', 'Alfenas', 'MG', 3.20, '2021-05-02', TRUE, 'https://www.google.com/maps/search/?api=1&query=Alfenas%2C+MG', 'Pomar e horta para consumo da família.', 3),
    ('Fazenda Cachoeirinha', 'FAZENDA', 'Campestre', 'MG', 142.00, '2004-11-30', FALSE, 'https://www.google.com/maps/search/?api=1&query=Campestre%2C+MG', 'Maior área do grupo. Parte em reserva legal.', 4),
    ('Granja Vale Verde', 'GRANJA', 'Varginha', 'MG', 18.50, '2018-01-15', TRUE, 'https://www.google.com/maps/search/?api=1&query=Varginha%2C+MG', 'Aviário de postura e área de pastagem.', 5),
    ('Sítio Três Barras', 'SITIO', 'Três Pontas', 'MG', 27.80, '2012-06-09', FALSE, 'https://www.google.com/maps/search/?api=1&query=Tr%C3%AAs+Pontas%2C+MG', 'Café e milho em rotação com feijão.', 1),
    ('Fazenda Santa Rita', 'FAZENDA', 'Guaxupé', 'MG', 64.30, '2011-02-14', TRUE, 'https://www.google.com/maps/search/?api=1&query=Guaxup%C3%A9%2C+MG', 'Irrigação por gotejamento nos talhões mais novos.', 6),
    ('Sítio Água Limpa', 'SITIO', 'Carmo do Rio Claro', 'MG', 9.60, '2019-10-05', FALSE, 'https://www.google.com/maps/search/?api=1&query=Carmo+do+Rio+Claro%2C+MG', 'Pequena propriedade familiar com milho e feijão.', 7),
    ('Chácara Bela Vista', 'CHACARA', 'Poços de Caldas', 'MG', 2.40, '2022-03-27', TRUE, NULL, 'Uso misto: lazer e horta orgânica.', 8),
    ('Fazenda Ouro Verde', 'FAZENDA', 'Boa Esperança', 'MG', 210.00, '2001-07-19', TRUE, NULL, 'Colheita mecanizada nos talhões planos.', 9),
    ('Sítio Pé de Serra', 'SITIO', 'Cabo Verde', 'MG', 15.20, '2015-12-01', FALSE, NULL, 'Café de altitude, perto da serra.', 10),
    ('Granja Primavera', 'GRANJA', 'Ipuiúna', 'MG', 11.80, '2020-06-18', TRUE, NULL, 'Criação de aves e cultivo de sorgo para ração.', 11);

INSERT INTO plantings
    (crop, variety, planting_date, expected_harvest, area, expected_yield, status, notes, property_id, manager_id)
VALUES
    ('Café', 'Catuaí Vermelho IAC 144', DATE_ADD(CURDATE(), INTERVAL -2100 DAY), DATE_ADD(CURDATE(), INTERVAL 205 DAY), 24.00, 38.00, 'GROWING', 'Talhão 03, meia encosta. Poda programada para depois da colheita.', 1, 1),
    ('Café', 'Mundo Novo', DATE_ADD(CURDATE(), INTERVAL -3300 DAY), DATE_ADD(CURDATE(), INTERVAL 215 DAY), 18.50, 32.00, 'GROWING', 'Talhão 01, lavoura antiga. Recepa prevista para 2028.', 1, 1),
    ('Café', 'Arara', DATE_ADD(CURDATE(), INTERVAL -900 DAY), DATE_ADD(CURDATE(), INTERVAL 190 DAY), 12.00, 45.00, 'GROWING', 'Talhão 05, variedade resistente à ferrugem.', 1, 6),
    ('Café', 'Bourbon Amarelo', DATE_ADD(CURDATE(), INTERVAL -2500 DAY), DATE_ADD(CURDATE(), INTERVAL 225 DAY), 9.50, 28.00, 'GROWING', 'Café especial, colheita seletiva.', 6, 1),
    ('Café', 'Catuaí Amarelo IAC 62', DATE_ADD(CURDATE(), INTERVAL -1800 DAY), DATE_ADD(CURDATE(), INTERVAL 210 DAY), 30.00, 36.00, 'GROWING', 'Gotejamento no talhão 02.', 7, 7),
    ('Café', 'Topázio MG 1190', DATE_ADD(CURDATE(), INTERVAL -1200 DAY), DATE_ADD(CURDATE(), INTERVAL 215 DAY), 14.00, 40.00, 'GROWING', NULL, 7, 7),
    ('Café', 'Catuaí Vermelho IAC 144', DATE_ADD(CURDATE(), INTERVAL -2800 DAY), DATE_ADD(CURDATE(), INTERVAL 200 DAY), 60.00, 34.00, 'GROWING', 'Maior talhão da fazenda, colheita mecanizada.', 4, 4),
    ('Café', 'Paraíso MG H 419-1', DATE_ADD(CURDATE(), INTERVAL -1500 DAY), DATE_ADD(CURDATE(), INTERVAL 205 DAY), 75.00, 42.00, 'GROWING', 'Secagem em terreiro suspenso.', 10, 9),
    ('Café', 'Acaiá', DATE_ADD(CURDATE(), INTERVAL -3600 DAY), DATE_ADD(CURDATE(), INTERVAL -115 DAY), 8.00, 30.00, 'HARVESTED', 'Safra colhida, secagem concluída.', 11, 10),
    ('Soja', 'Ciclo médio', DATE_ADD(CURDATE(), INTERVAL -40 DAY), DATE_ADD(CURDATE(), INTERVAL 105 DAY), 40.00, 62.00, 'GROWING', 'Plantio direto sobre palhada de milho.', 4, 4),
    ('Soja', 'Ciclo precoce', DATE_ADD(CURDATE(), INTERVAL -30 DAY), DATE_ADD(CURDATE(), INTERVAL 95 DAY), 55.00, 60.00, 'GROWING', 'Dessecação feita 15 dias antes do plantio.', 10, 9),
    ('Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL -35 DAY), DATE_ADD(CURDATE(), INTERVAL 120 DAY), 35.00, 150.00, 'GROWING', 'Adubação de cobertura aos 25 dias.', 4, 4),
    ('Milho', 'Híbrido duplo', DATE_ADD(CURDATE(), INTERVAL -25 DAY), DATE_ADD(CURDATE(), INTERVAL 125 DAY), 45.00, 140.00, 'GROWING', NULL, 10, 9),
    ('Milho', 'Crioulo', DATE_ADD(CURDATE(), INTERVAL -128 DAY), DATE_ADD(CURDATE(), INTERVAL -3 DAY), 3.50, 90.00, 'GROWING', 'Variedade crioula para silagem e consumo próprio.', 2, 2),
    ('Feijão', 'Carioca (das águas)', DATE_ADD(CURDATE(), INTERVAL -75 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 6.00, 38.00, 'GROWING', 'Irrigado por aspersão. Colheita chegando.', 5, 5),
    ('Feijão', 'Preto', DATE_ADD(CURDATE(), INTERVAL -92 DAY), DATE_ADD(CURDATE(), INTERVAL -6 DAY), 4.00, 32.00, 'GROWING', 'Colheita adiada pelas chuvas. Atualizar a situação quando terminar.', 8, 7),
    ('Sorgo', 'Granífero', DATE_ADD(CURDATE(), INTERVAL -100 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), 5.00, 90.00, 'GROWING', 'Para ração das aves.', 12, 11),
    ('Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL 120 DAY), DATE_ADD(CURDATE(), INTERVAL 260 DAY), 8.00, 140.00, 'PLANNED', 'Safrinha depois do feijão das águas.', 5, 5),
    ('Soja', 'Ciclo precoce', DATE_ADD(CURDATE(), INTERVAL 35 DAY), DATE_ADD(CURDATE(), INTERVAL 140 DAY), 20.00, 58.00, 'PLANNED', 'Aguardando a regularização das chuvas.', 7, 7),
    ('Feijão', 'Carioca (da seca)', DATE_ADD(CURDATE(), INTERVAL 130 DAY), DATE_ADD(CURDATE(), INTERVAL 205 DAY), 3.00, 30.00, 'PLANNED', NULL, 2, 2),
    ('Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL 150 DAY), DATE_ADD(CURDATE(), INTERVAL 290 DAY), 30.00, 120.00, 'PLANNED', 'Safrinha depois da soja.', 4, 4),
    ('Café', 'Arara', DATE_ADD(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 900 DAY), 1.20, 20.00, 'PLANNED', 'Mudas encomendadas. Primeira colheita em cerca de 2,5 anos.', 9, 8),
    ('Milho', 'Híbrido simples', DATE_ADD(CURDATE(), INTERVAL -260 DAY), DATE_ADD(CURDATE(), INTERVAL -130 DAY), 50.00, 145.00, 'HARVESTED', 'Safra anterior colhida.', 10, 9),
    ('Soja', 'Ciclo médio', DATE_ADD(CURDATE(), INTERVAL -300 DAY), DATE_ADD(CURDATE(), INTERVAL -170 DAY), 38.00, 61.00, 'HARVESTED', NULL, 4, 4),
    ('Feijão', 'Carioca (da seca)', DATE_ADD(CURDATE(), INTERVAL -240 DAY), DATE_ADD(CURDATE(), INTERVAL -150 DAY), 2.80, 34.00, 'HARVESTED', NULL, 2, 2),
    ('Milho', 'Híbrido duplo', DATE_ADD(CURDATE(), INTERVAL -250 DAY), DATE_ADD(CURDATE(), INTERVAL -120 DAY), 14.00, 135.00, 'HARVESTED', 'Colheita mecânica.', 6, 1),
    ('Soja', 'Ciclo precoce', DATE_ADD(CURDATE(), INTERVAL -310 DAY), DATE_ADD(CURDATE(), INTERVAL -180 DAY), 5.00, 60.00, 'HARVESTED', NULL, 8, 7),
    ('Feijão', 'Preto', DATE_ADD(CURDATE(), INTERVAL -170 DAY), DATE_ADD(CURDATE(), INTERVAL -95 DAY), 3.00, 28.00, 'LOST', 'Perda total por geada em julho.', 12, 11),
    ('Milho', 'Safrinha', DATE_ADD(CURDATE(), INTERVAL -230 DAY), DATE_ADD(CURDATE(), INTERVAL -105 DAY), 6.00, 100.00, 'LOST', 'Estiagem prolongada na fase de enchimento de grãos.', 11, 10);
