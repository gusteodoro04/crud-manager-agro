# CRUD Manager — Rural Property + Planting

Assignment 3 of Web Programming Techniques II (IFMG). Base project: *crud-manager-public*
(Servlets + JSP + JSTL, DAO pattern, MySQL), extended with **two new agribusiness-themed registries**,
**login**, and **pagination in every registry**.

## What the system does

| Screen | Address | What it has |
|---|---|---|
| Home | `/` | Harvest dashboard: key numbers, planting status, overdue harvest alert, and a **harvest calendar** (July to June, one row per property) |
| Properties | `/properties` | New registry. Search by name, city, state, or owner |
| Plantings | `/plantings` | New registry. Search + tabs by status (All, Planned, Growing, Harvested, Lost) |
| Users | `/users` | Base project registry, now with password, search, and pagination |
| Posts | `/posts` | Base project registry, now with search and pagination |
| Companies | `/companies` | Base project registry, now with search and pagination |
| Sign in / Sign out | `/login`, `/logout` | Login screen and session logout |

> The user interface is in Brazilian Portuguese (pt-BR). Labels in this README are translated for readability.

## Where each assignment item is

| Requirement | Where |
|---|---|
| SQL script for the new tables | `script-sql.sql` (full database) and `script-migracao-atividade3.sql` (only adds, deletes nothing) |
| Model classes | `model/RuralProperty.java`, `model/Planting.java` (+ enums `PropertyType`, `PlantingStatus`) |
| DAO interfaces and classes | `model/dao/RuralPropertyDAO` + `MySQLRuralPropertyDAO`, `PlantingDAO` + `MySQLPlantingDAO` |
| Registration in DAOFactory | `model/dao/DAOFactory.java` (`static` block) |
| Controllers (Servlets) | `controller/RuralPropertiesController.java`, `controller/PlantingsController.java` |
| JSPs (list, form, delete) | `rural-properties.jsp`, `form-rural-property.jsp`, `plantings.jsp`, `form-planting.jsp`; deletion is the confirmation dialog `WEB-INF/fragments/delete-dialog.jspf` |
| New registries in the header | `nav-menu.jsp` |
| Login | `login.jsp`, `controller/LoginController.java`, `controller/AuthenticationFilter.java`, `model/PasswordUtil.java` |
| Pagination in all registries | `controller/Pagination.java`, `WEB-INF/fragments/pagination.jspf`, `listPage` and `count` methods in all DAOs |

### The two new registries

Each one has 10 fields, using several `input` types, and **two fields that are foreign keys**
chosen from a `select` (the requirement was at least one).

**Rural property** (`rural_properties`)

| Field | Input type | Notes |
|---|---|---|
| Name | `text` | required |
| Type | `radio` (cards) | Farm (*Fazenda*), Small farm (*Sítio*), Country house (*Chácara*), or Poultry farm (*Granja*) |
| City | `text` | required |
| State | `select` | the 27 Brazilian states |
| Area (ha) | `number` | greater than zero |
| Acquisition date | `date` | optional, cannot be in the future |
| Irrigated | `checkbox` (switch) | |
| Map link | `url` | optional, only `http://` or `https://` |
| Notes | `textarea` | optional |
| **Owner** | `select` → `users` | **foreign key** |

**Planting** (`plantings`)

| Field | Input type | Notes |
|---|---|---|
| Crop | `text` + `datalist` | suggestions (Coffee, Soybean, Corn…), accepts others |
| Variety | `text` | optional |
| Planting date | `date` | |
| Expected harvest | `date` | cannot be before the planting date |
| Planted area (ha) | `number` | cannot exceed the property's area |
| Yield (bags/ha) | `number` | |
| Status | `radio` (buttons) | Planned, Growing, Harvested, Lost |
| Notes | `textarea` | optional |
| **Property** | `select` → `rural_properties` | **foreign key** |
| **Responsible** | `select` → `users` | **foreign key** |

Integrity rules: a property that has plantings **cannot be deleted**; a user who is an owner, is responsible
for a planting, authored a post, or has a company **cannot be deleted**, nor can the logged-in user themselves.
In every case a message explains why.

## How to run

**Requirements:** Java 11 or higher (also compiles with Java 8), **Tomcat 10.1** (uses the `jakarta.servlet`
namespace; tested on 10.1, and the code only uses features that Tomcat 11 also offers, but it has not been tested there),
MySQL 8, and Eclipse (Dynamic Web Project) or another IDE with Tomcat support.

1. **Database.** Choose one of the scripts:
   * new database, or you can erase everything: `script-sql.sql`
     (starts with `DROP DATABASE crud_manager` — **it deletes the `crud_manager` database if it exists** and does not touch any other database);
   * keep the current data: `script-migracao-atividade3.sql`
     (can be run more than once; users who did not have a password get `agro123`).

   The simplest way to run it (works for `cmd` and PowerShell on Windows): open the client with `mysql -u root -p` and
   run `source C:/path/to/the/folder/script-sql.sql` (using forward slashes `/`). In `cmd`,
   `mysql -u root -p < script-sql.sql` also works; in PowerShell the `<` operator does not.
2. **Connection.** Check the MySQL user and password in `src/main/java/model/dao/MySQLConnectionFactory.java`.
3. **UTF-8 encoding.** All files are in UTF-8. In Eclipse, check *Project → Properties → Resource →
   Text file encoding* is set to **UTF-8** (on Windows the old default is Cp1252 and accented messages show up
   as "nÃ£o"). Then use *Project → Clean*.
4. **Tomcat.** Deploy the project to Tomcat 10.1 and open `http://localhost:8080/<context>/`
   (the address works under any context, including `ROOT`, because all links use the context path).
5. **Sign in.** User `emerson@mail.com`, password `agro123`. All sample users share the same password.

The script creates 12 users, 16 posts, 12 companies, 12 properties, and 29 plantings (planting dates are
relative to the day the script runs, so the dashboard always shows a harvest season in progress).

## Structure

```
src/main/java
  controller/   Servlets (one per registry), login filter, Pagination, ControllerUtil, dashboard (HomeController)
  model/        entities, enums, PasswordUtil (password hashing), DateUtil, BrazilianStates
  model/dao/    DAO interfaces, MySQL implementations, DAOFactory, DBHandler (connection/PreparedStatement)
src/main/webapp
  *.jsp         screens (login, dashboard, 5 lists and 5 forms, error page)
  WEB-INF/      web.xml, lib/ (JSTL and MySQL driver), tags/ (<ui:icon>, <ui:field>), fragments/ (csrf, flash, pagination, delete)
  css/app.css   all the styling (no Bootstrap)
  js/app.js     delete dialog, toast notification, show password, form details
  fonts/        Zilla Slab and Source Sans 3 (OFL license, hosted here: works offline)
  images/       Lucide icons (SVG sprite, ISC/MIT license: LICENSE-Lucide.txt), favicon, login illustration
script-sql.sql  script-migracao-atividade3.sql
```

## Security (summary)

* Passwords hashed with **PBKDF2-HMAC-SHA256** (600,000 iterations, random salt per password). Nothing in plain text.
* **Login filter** on `/*`: without a session, every screen redirects to `/login`; `.jsp` files cannot be opened directly by URL.
* **CSRF token** on every `POST` form (checked in the filter). Signing out is also a `POST`.
* Session id is rotated on login (against *session fixation*), `HttpOnly` cookie, 60-minute session.
* Single message "Incorrect email or password" (does not reveal which emails exist).
* All SQL uses `PreparedStatement`; `%` and `_` typed in the search are escaped; all output uses `<c:out>`.
* In production with HTTPS, add `<secure>true</secure>` to the `<cookie-config>` in `web.xml`.

## Changes from the base project

* `users` gained `password_hash` and a unique email; `DBHandler` now closes connections (before, they leaked).
* `CharacterEncodingFilter` was replaced by `AuthenticationFilter` (which also sets UTF-8).
* Bootstrap, jQuery, `index.jsp`, and `modal.html` were removed; the look is now `css/app.css` + `js/app.js`.
* The three `taglibs-standard-*.jar` files (`javax` version) were removed from `WEB-INF/lib`: they conflicted with
  the `jakarta` JSTL (`jakarta.servlet.jsp.jstl-*.jar`) and only worked on Windows because of jar ordering.
* `web.xml`: `pt_BR` locale for numbers and dates, UTF-8 for all JSPs, session, and error pages.

## Known limitations

* There is no login attempt limit (rate limiting would be the next step).
* In the property list, the planting count links to the planting list filtered by the property **name**;
  if two properties have similar names, plantings from both appear. Filtering by id would be more accurate.
* Foreign key selects list all records: with thousands of users, a search with autocomplete would be ideal.
* The session cookie is not `Secure` because the system runs over HTTP in development.
