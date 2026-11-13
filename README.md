# CRUD Manager — Propriedade Rural + Plantio

Atividade 3 de Técnicas de Programação para Web II (IFMG). Projeto base: *crud-manager-public*
(Servlets + JSP + JSTL, padrão DAO, MySQL), ampliado com **dois novos cadastros do tema
agro**, **login** e **paginação em todos os cadastros**.

## O que o sistema faz

| Tela | Endereço | O que tem |
|---|---|---|
| Início | `/` | Painel da safra: números, situação dos plantios, alerta de colheitas atrasadas e **calendário da safra** (julho a junho, uma linha por propriedade) |
| Propriedades | `/properties` | Cadastro novo. Busca por nome, cidade, UF ou dono |
| Plantios | `/plantings` | Cadastro novo. Busca + abas por situação (Todos, Planejado, Em desenvolvimento, Colhido, Perdido) |
| Usuários | `/users` | Cadastro do projeto base, agora com senha, busca e paginação |
| Posts | `/posts` | Cadastro do projeto base, agora com busca e paginação |
| Empresas | `/companies` | Cadastro do projeto base, agora com busca e paginação |
| Entrar / Sair | `/login`, `/logout` | Tela de login e encerramento da sessão |

## Onde está cada item da atividade

| Exigência | Onde |
|---|---|
| Script SQL das novas tabelas | `script-sql.sql` (banco completo) e `script-migracao-atividade3.sql` (só acrescenta, não apaga nada) |
| Classes do modelo | `model/RuralProperty.java`, `model/Planting.java` (+ enums `PropertyType`, `PlantingStatus`) |
| Interfaces e classes DAO | `model/dao/RuralPropertyDAO` + `MySQLRuralPropertyDAO`, `PlantingDAO` + `MySQLPlantingDAO` |
| Registro no DAOFactory | `model/dao/DAOFactory.java` (bloco `static`) |
| Controllers (Servlets) | `controller/RuralPropertiesController.java`, `controller/PlantingsController.java` |
| JSPs (lista, formulário, exclusão) | `rural-properties.jsp`, `form-rural-property.jsp`, `plantings.jsp`, `form-planting.jsp`; a exclusão é a janela de confirmação `WEB-INF/fragments/delete-dialog.jspf` |
| Novos cadastros no cabeçalho | `nav-menu.jsp` |
| Login | `login.jsp`, `controller/LoginController.java`, `controller/AuthenticationFilter.java`, `model/PasswordUtil.java` |
| Paginação em todos os cadastros | `controller/Pagination.java`, `WEB-INF/fragments/pagination.jspf`, métodos `listPage` e `count` em todos os DAOs |

### Os dois cadastros novos

Cada um tem 10 campos, de vários tipos de `input`, e **dois campos que são chave estrangeira**
escolhidos em um `select` (a exigência era ao menos um).

**Propriedade rural** (`rural_properties`)

| Campo | Tipo de entrada | Observação |
|---|---|---|
| Nome | `text` | obrigatório |
| Tipo | `radio` (cartões) | Fazenda, Sítio, Chácara ou Granja |
| Cidade | `text` | obrigatório |
| Estado | `select` | as 27 UFs |
| Área (ha) | `number` | maior que zero |
| Data de aquisição | `date` | opcional, não pode ser futura |
| Irrigada | `checkbox` (interruptor) | |
| Link do mapa | `url` | opcional, só `http://` ou `https://` |
| Observações | `textarea` | opcional |
| **Dono** | `select` → `users` | **chave estrangeira** |

**Plantio** (`plantings`)

| Campo | Tipo de entrada | Observação |
|---|---|---|
| Cultura | `text` + `datalist` | sugestões (Café, Soja, Milho…), aceita outras |
| Variedade | `text` | opcional |
| Data do plantio | `date` | |
| Colheita prevista | `date` | não pode ser antes do plantio |
| Área plantada (ha) | `number` | não pode passar da área da propriedade |
| Produtividade (sacas/ha) | `number` | |
| Situação | `radio` (botões) | Planejado, Em desenvolvimento, Colhido, Perdido |
| Anotações | `textarea` | opcional |
| **Propriedade** | `select` → `rural_properties` | **chave estrangeira** |
| **Responsável** | `select` → `users` | **chave estrangeira** |

Regras de integridade: uma propriedade que tem plantios **não pode ser excluída**; um usuário que é dono,
responsável, autor de post ou tem empresa **não pode ser excluído**, nem a própria pessoa logada.
Em todos os casos aparece uma mensagem explicando o motivo.

## Como rodar

**Requisitos:** Java 11 ou superior (compila também com Java 8), **Tomcat 10.1** (usa o namespace
`jakarta.servlet`; testado no 10.1, e o código só usa recursos que o Tomcat 11 também oferece, mas ele não foi testado),
MySQL 8 e o Eclipse (Dynamic Web Project) ou outra IDE com Tomcat.

1. **Banco de dados.** Escolha um dos scripts:
   * banco novo ou pode apagar tudo: `script-sql.sql`
     (começa com `DROP DATABASE crud_manager` — **apaga o banco `crud_manager` se ele existir** e não mexe em nenhum outro banco);
   * manter os dados atuais: `script-migracao-atividade3.sql`
     (pode ser executado mais de uma vez; quem ainda não tinha senha passa a ter `agro123`).

   O jeito mais simples de executar (vale para `cmd` e PowerShell no Windows): abra o cliente com `mysql -u root -p` e
   rode `source C:/caminho/da/pasta/script-sql.sql` (com barras normais `/`). No `cmd` também funciona
   `mysql -u root -p < script-sql.sql`; no PowerShell o `<` não funciona.
2. **Conexão.** Confira usuário e senha do MySQL em `src/main/java/model/dao/MySQLConnectionFactory.java`.
3. **Codificação UTF-8.** Todos os arquivos estão em UTF-8. No Eclipse, confira em *Project → Properties → Resource →
   Text file encoding* se está **UTF-8** (no Windows o padrão antigo é Cp1252 e as mensagens com acento ficam
   como "nÃ£o"). Depois use *Project → Clean*.
4. **Tomcat.** Publique o projeto no Tomcat 10.1 e abra `http://localhost:8080/<contexto>/`
   (o endereço funciona em qualquer contexto, inclusive `ROOT`, porque todos os links usam o caminho do contexto).
5. **Entrar.** Usuário `emerson@mail.com`, senha `agro123`. Todos os usuários de exemplo usam a mesma senha.

O script cria 12 usuários, 16 posts, 12 empresas, 12 propriedades e 29 plantios (as datas dos plantios são
relativas ao dia em que o script roda, para o painel sempre mostrar uma safra em andamento).

## Estrutura

```
src/main/java
  controller/   Servlets (um por cadastro), filtro de login, Pagination, ControllerUtil, painel (HomeController)
  model/        entidades, enums, PasswordUtil (hash de senha), DateUtil, BrazilianStates
  model/dao/    interfaces DAO, implementações MySQL, DAOFactory, DBHandler (conexão/PreparedStatement)
src/main/webapp
  *.jsp         telas (login, painel, 5 listas e 5 formulários, página de erro)
  WEB-INF/      web.xml, lib/ (JSTL e driver MySQL), tags/ (<ui:icon>, <ui:field>), fragments/ (csrf, flash, paginação, exclusão)
  css/app.css   todo o visual (sem Bootstrap)
  js/app.js     janela de exclusão, aviso (toast), mostrar senha, detalhes dos formulários
  fonts/        Zilla Slab e Source Sans 3 (licença OFL, hospedadas aqui: funciona sem internet)
  images/       ícones Lucide (sprite SVG, licença ISC/MIT: LICENSE-Lucide.txt), favicon, ilustração do login
script-sql.sql  script-migracao-atividade3.sql
```

## Segurança (resumo)

* Senhas com **PBKDF2-HMAC-SHA256** (600 mil repetições, sal aleatório por senha). Nada em texto puro.
* **Filtro de login** em `/*`: sem sessão, qualquer tela vai para `/login`; os `.jsp` não abrem direto pela URL.
* **Token CSRF** em todo formulário `POST` (verificado no filtro). Sair da conta também é `POST`.
* Id da sessão trocado no login (contra *session fixation*), cookie `HttpOnly`, sessão de 60 minutos.
* Mensagem única "E-mail ou senha incorretos" (não revela quais e-mails existem).
* Todo SQL usa `PreparedStatement`; o `%` e o `_` digitados na busca são escapados; toda saída usa `<c:out>`.
* Em produção com HTTPS, acrescente `<secure>true</secure>` ao `<cookie-config>` do `web.xml`.

## Mudanças em relação ao projeto base

* `users` ganhou `password_hash` e e-mail único; `DBHandler` agora fecha as conexões (antes elas vazavam).
* `CharacterEncodingFilter` foi substituído pelo `AuthenticationFilter` (que também define UTF-8).
* Bootstrap, jQuery, `index.jsp` e `modal.html` saíram; o visual agora é `css/app.css` + `js/app.js`.
* Os três `taglibs-standard-*.jar` (versão `javax`) foram removidos de `WEB-INF/lib`: conflitavam com o JSTL
  `jakarta` (`jakarta.servlet.jsp.jstl-*.jar`) e só funcionavam no Windows por causa da ordem dos jars.
* `web.xml`: idioma `pt_BR` para números e datas, UTF-8 para todos os JSPs, sessão e páginas de erro.

## Limitações conhecidas

* Não há limite de tentativas de login (um *rate limit* seria o próximo passo).
* Na lista de propriedades, o número de plantios é um link para a lista de plantios filtrada pelo **nome** da
  propriedade; se duas propriedades tiverem nomes parecidos, aparecem plantios das duas. Um filtro por id seria mais exato.
* Selects de chave estrangeira listam todos os registros: com milhares de usuários, o ideal seria uma busca com autocompletar.
* O cookie de sessão não é `Secure` porque o sistema roda em HTTP no desenvolvimento.
