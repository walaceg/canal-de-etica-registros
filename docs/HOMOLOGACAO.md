# Homologacao

Documento operacional da homologacao da versao `v1.0.0-MVP` do Canal de Etica Registros.

## Ambiente Homologado

- Sistema operacional: openSUSE Leap 15.5.
- Docker: 27.
- Docker Compose: 2.
- Banco de dados: PostgreSQL 16.
- Backend: Spring Boot com Java 21.
- Frontend: React servido por Nginx.

## Portas

- Frontend: `18080`.
- Backend: `18081`.
- Banco de dados: interno na rede Docker.

## Volumes Docker

Volumes persistentes homologados:

- PostgreSQL: dados do banco.
- Uploads: arquivos enviados como anexos de registros.

Os volumes nao devem ser removidos em atualizacoes rotineiras, pois armazenam dados de negocio e arquivos enviados.

## Rede Docker

A stack utiliza rede Docker interna para comunicacao entre frontend, backend e PostgreSQL.

O PostgreSQL deve permanecer acessivel apenas internamente, salvo necessidade operacional controlada.

## Variaveis Obrigatorias do `.env`

As variaveis devem ser definidas no ambiente ou arquivo `.env` utilizado pelo Docker Compose. Nao versionar segredos reais.

- `DB_NAME`: nome do banco da aplicacao.
- `DB_USER`: usuario do banco.
- `DB_PASSWORD`: senha do banco.
- `JWT_SECRET`: segredo forte para assinatura dos tokens JWT.
- `UPLOAD_DIR`: diretorio interno usado pelo backend para armazenar uploads.
- `BASEPLUS_CORS_ALLOWED_ORIGINS`: origens permitidas para acesso ao backend.
- `BASEPLUS_BOOTSTRAP_ADMIN_ENABLED`: habilita bootstrap administrativo apenas quando necessario.
- `BASEPLUS_BOOTSTRAP_ADMIN_NAME`: nome do administrador inicial.
- `BASEPLUS_BOOTSTRAP_ADMIN_EMAIL`: email do administrador inicial.
- `BASEPLUS_BOOTSTRAP_ADMIN_PASSWORD`: senha temporaria do administrador inicial.

## Deploy Resumido

1. Preparar o arquivo `.env` com variaveis do ambiente.
2. Subir a stack Docker Compose com build das imagens.
3. Aguardar healthcheck do backend.
4. Validar frontend em `http://<host>:18080`.
5. Validar backend em `http://<host>:18081`.
6. Validar Swagger em `http://<host>:18081/swagger-ui/index.html`.
7. Executar bootstrap administrativo somente se ainda nao existir administrador.

## Atualizacao Resumida

1. Gerar backup do banco e dos uploads.
2. Atualizar o codigo/imagens da aplicacao.
3. Recriar containers preservando volumes.
4. Acompanhar execucao das migrations Flyway.
5. Validar login administrativo.
6. Validar criacao publica de registro com API Key de homologacao.
7. Validar consulta administrativa e visualizacao de anexos.

## Backup Resumido

Itens obrigatorios:

- Dump do PostgreSQL.
- Conteudo do volume de uploads.
- Arquivo `.env` ou registro seguro das variaveis utilizadas, sem expor segredos em repositorio.

Procedimento recomendado:

1. Colocar a aplicacao em janela controlada de manutencao quando necessario.
2. Gerar dump consistente do PostgreSQL.
3. Copiar o volume de uploads.
4. Armazenar os artefatos em local seguro.
5. Registrar data, versao e responsavel pelo backup.

## Restore Resumido

1. Subir ambiente limpo com a mesma versao da aplicacao.
2. Restaurar o dump do PostgreSQL.
3. Restaurar o volume de uploads.
4. Conferir variaveis do `.env`.
5. Iniciar a stack.
6. Validar healthcheck, login, consulta de registros e abertura de anexos.

## Cuidados Operacionais

- Nao remover volumes persistentes durante atualizacoes.
- Nao versionar API Keys, senhas ou `JWT_SECRET`.
- Manter o banco sem exposicao publica sempre que possivel.
- Executar bootstrap administrativo apenas de forma explicita e controlada.
- Validar CORS e portas antes de liberar o ambiente para usuarios de homologacao.
