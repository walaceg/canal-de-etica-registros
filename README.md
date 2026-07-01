# Canal de Etica Registros

Aplicacao corporativa derivada da Base+ para preparacao do futuro Canal de Etica Registros.

## Status do Projeto

- Versao atual: `v1.0.0-MVP`
- Status: `Homologado`
- Data do marco: 2026-07-01

Ambiente homologado:

- openSUSE Leap 15.5
- Docker 27
- Docker Compose 2
- PostgreSQL 16
- Java 21
- React
- Spring Boot

## Funcionalidades Entregues

Backend:

- Cadastro publico de denuncias
- API Key
- JWT
- Swagger/OpenAPI
- Flyway
- Upload de anexos
- Download seguro de anexos
- Consulta administrativa
- Controle por permissoes

Frontend:

- Tela publica
- Consulta administrativa
- Detalhamento
- Visualizacao de anexos
- Filtros recolhiveis
- Controle de permissoes

Infraestrutura:

- Docker Compose
- PostgreSQL
- Volumes persistentes
- Deploy Linux
- Homologacao concluida

## Fundacao

A aplicacao preserva a arquitetura da Base+ como fundacao tecnica:

- autenticacao;
- autorizacao;
- perfis e permissoes;
- auditoria;
- branding;
- estrutura organizacional;
- uploads;
- Docker;
- documentacao operacional.

As referencias a Base+ em documentos de arquitetura, playbooks, templates e pacotes tecnicos representam a plataforma base e devem permanecer enquanto forem usadas como contrato arquitetural.

## Aplicacoes

- `baseplus-backend`: API Java/Spring Boot da aplicacao, com artefato Maven `canal-de-etica-registros-backend`.
- `baseplus-frontend`: aplicacao React/Vite, com pacote NPM `canal-de-etica-registros-frontend`.

Os nomes dos diretorios foram preservados neste bootstrap para evitar alteracao estrutural ampla. Uma eventual renomeacao fisica dos diretorios deve ser decidida separadamente.

## Stack

Backend:

- Java 21
- Spring Boot 3.3.5
- Spring Security com JWT
- JPA/Hibernate
- Flyway
- PostgreSQL como banco persistente padrao
- H2 apenas para desenvolvimento local

Frontend:

- React 19
- Vite
- React Router
- Axios
- lucide-react
- CSS variables/design tokens

Infraestrutura:

- Dockerfiles multi-stage para backend e frontend.
- Docker Compose integrado com PostgreSQL, backend e frontend.
- Nginx servindo o frontend e fazendo proxy de `/api` e `/uploads`.
- Volumes persistentes para PostgreSQL e uploads.

## Estrutura

```text
canal-de-etica-registros/
|-- engineering
|-- docs/
|-- scripts/
|-- baseplus-backend/
`-- baseplus-frontend/
```

A estrutura conceitual `core`, `shared`, `modules` e `engineering` deve permanecer preservada:

- backend: `baseplus-backend/src/main/java/com/baseplus/core`, `shared` e `modules`;
- frontend: `baseplus-frontend/src/core`, `shared` e `modules`;
- engenharia: `engineering/`.

Funcionalidades novas devem nascer em `modules`, conforme os principios arquiteturais.

## Execucao Local

Backend em modo `dev`:

```powershell
cd C:\devapps\canal-de-etica-registros\baseplus-backend
mvn spring-boot:run
```

Frontend:

```powershell
cd C:\devapps\canal-de-etica-registros\baseplus-frontend
npm run dev
```

URLs padrao:

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

Usuario dev herdado da fundacao:

- Email: `admin@baseplus.com`
- Senha: `Baseplus@123`

Essas credenciais sao apenas para desenvolvimento local e devem ser revisadas antes de ambientes persistentes.

## Docker

O `docker-compose.yml` fica em `baseplus-backend/` e usa identidade Docker da aplicacao:

- projeto Compose: `canal-de-etica-registros`;
- imagem backend: `canal-de-etica-registros-backend`;
- imagem frontend: `canal-de-etica-registros-frontend`;
- banco padrao: `canal_de_etica_registros`;
- usuario padrao do banco: `canal_de_etica`.

Comandos principais:

```powershell
cd C:\devapps\canal-de-etica-registros\baseplus-backend
docker compose config
docker compose up --detach --build --wait
docker compose ps
```

## Branding

A aplicacao ja esta preparada para identidade propria por meio do modulo Branding da fundacao.

Nesta etapa foram atualizados apenas nome, titulo e fallbacks textuais para `Canal de Etica Registros`. Cores, logos, favicon, fundo de login e guia visual ainda nao foram definidos.

Assets herdados da Base+ ainda existem como referencia/fallback e devem ser avaliados na etapa de identidade visual:

- `brand/`
- `baseplus-frontend/public/brand/`
- `BRAND_GUIDE.md`
- `docs/branding.md`

## Documentacao de Referencia

- `engineering/playbooks/10-new-application.md`
- `engineering/architecture/principles.md`
- `engineering/ai/architecture-context.md`
- `docs/new-application.md`
- `docs/module-development.md`
- `docs/docker.md`
- `docs/HOMOLOGACAO.md`
- `docs/releases/v1.0.0-MVP.md`
- `docs/setup.md`

## Historico do Bootstrap Inicial

O bootstrap inicial preparou a aplicacao sem implementar regras especificas do Canal de Etica. Esse historico permanece registrado para preservar a rastreabilidade da evolucao a partir da Base+.

Naquele momento nao haviam sido criados:

- entidades de Canal de Etica;
- controllers de Canal de Etica;
- migrations de Canal de Etica;
- modulos novos;
- regras de negocio novas;
- identidade visual final.

## Proxima Etapa

O proximo ciclo planejado e a Sprint 4.2, focada em UX, usabilidade e aplicacao das 10 Heuristicas de Nielsen nas telas do Canal de Etica.
