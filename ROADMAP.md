# Roadmap Base+

## Canal de Etica Registros

### Concluido

- Sprint 1 - Modelo de Dados do Canal de Etica.
- Sprint 2 - Seguranca por API Key.
- Sprint 3 - API Publica de Recebimento de Registros.
- Sprint 3.1 - Hardening da API Publica.
- Sprint 3.2 - Liberacao e Configuracao do Swagger.
- Sprint 3.3 - Swagger somente do Canal de Etica.
- Sprint 4A - Backend do BackOffice: consulta de registros.
- Sprint 4B - Frontend BackOffice: listagem de registros.
- Sprint 4C - Frontend BackOffice: detalhe do registro.

### Proximas fases

- Sprint 4.2 - Aplicacao das 10 Heuristicas de Nielsen.
- Sprint 5 - Workflow da denuncia.
- Sprint 6 - Dashboard.
- Sprint 7 - Auditoria.
- Sprint 8 - Notificacoes.

## Curto prazo

- Fazer o primeiro commit/tag da Base+ 1.0.0 quando a revisao estiver aprovada.
- Criar arquivos `.env.example` para backend e frontend, se forem necessarios.
- Definir estrategia oficial para H2, PostgreSQL e Flyway em dev/prod.
- Criar o primeiro modulo real de negocio usando `MODULE_TEMPLATE.md`.
- Aplicar validacao de escopo organizacional no primeiro modulo que possuir dados restritos por contexto de negocio.

## Medio prazo

- Evoluir dashboard com dados reais.
- Implementar exportacao de auditoria.
- Criar testes automatizados para fluxos principais do frontend.
- Documentar contrato da API.
- Implementar um modulo modelo real usando o padrao de CRUD Completo, como Empresas.
- Evoluir `scripts/check-project.ps1` com lint e testes frontend quando existirem.
- Revisar autorizacao granular por permission em todos os endpoints sensiveis.

## Longo prazo

- Suporte a multiempresa/tenant, se fizer sentido para o produto.
- Pipeline CI/CD.
- Observabilidade: logs estruturados, metricas e tracing.
- Marketplace ou registro de modulos reutilizaveis.
- Guia de extensao da plataforma Base+.
