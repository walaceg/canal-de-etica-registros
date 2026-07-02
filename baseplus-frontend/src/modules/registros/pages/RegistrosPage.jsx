import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Eye } from 'lucide-react';
import {
  ActionIconButton,
  Alert,
  Badge,
  Button,
  Card,
  EmptyState,
  Loading,
  Pagination,
  Table,
} from '../../../shared/components/index.js';
import { RegistroFilters } from '../components/RegistroFilters.jsx';
import * as registrosService from '../services/registrosService.js';
import './registros.css';

const DEFAULT_PAGE_SIZE = 20;

const STATUS_LABELS = {
  RECEBIDO: 'Recebido',
  EM_ANALISE: 'Em analise',
  CONCLUIDO: 'Concluido',
  ARQUIVADO: 'Arquivado',
};

const STATUS_VARIANTS = {
  RECEBIDO: 'primary',
  EM_ANALISE: 'warning',
  CONCLUIDO: 'success',
  ARQUIVADO: 'secondary',
};

const TIPO_FATO_LABELS = {
  1: 'Conduta',
  2: 'Assedio sexual',
  3: 'Violencia fisica',
  4: 'Corrupcao',
  5: 'Pratica de crimes hediondos',
  6: 'Desvios de dinheiro e/ou material',
  7: 'Violacao de leis ambientais',
  8: 'Destruicao ou danificacao de patrimonio da empresa',
  9: 'Porte de arma nao autorizado',
  10: 'Uso de substancias ilicitas',
  11: 'Retaliacao a denunciantes de boa-fe',
  12: 'Agiotagem',
  13: 'Decisoes em desacordo com a lei',
  14: 'Informacoes caluniosas ou difamatorias',
  15: 'Assedio moral',
  16: 'Desrespeito a normas de seguranca',
  17: 'Discriminacao e preconceito',
  18: 'Uso indevido de dados e informacoes',
  19: 'Adulteracao de cadastro',
  20: 'Informacoes falsas sobre a empresa',
  21: 'Desrespeito aos colegas',
  22: 'Abuso do Canal de Denuncia',
  23: 'Descumprimento de processos',
  24: 'Decisoes em desacordo com normas internas',
  25: 'Outros',
};

function initialFiltersFromParams(searchParams) {
  return {
    protocolo: searchParams.get('protocolo') ?? '',
    tipoFatoId: searchParams.get('tipoFatoId') ?? '',
    status: searchParams.get('status') ?? '',
    dataInicio: toInputDateTime(searchParams.get('dataInicio')),
    dataFim: toInputDateTime(searchParams.get('dataFim')),
  };
}

function toApiDateTime(value) {
  if (!value) {
    return undefined;
  }
  return value.length === 16 ? `${value}:00` : value;
}

function toInputDateTime(value) {
  if (!value) {
    return '';
  }
  return value.slice(0, 16);
}

function formatDateTime(value) {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date);
}

function formatValue(value) {
  return value || '-';
}

function formatCount(value) {
  const count = Number(value ?? 0);
  return count === 1 ? '1 registro encontrado' : `${count} registros encontrados`;
}

function normalizePage(value) {
  const parsed = Number(value ?? '0');
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0;
}

function normalizeSize(value) {
  const parsed = Number(value ?? String(DEFAULT_PAGE_SIZE));
  return Number.isFinite(parsed) && parsed > 0 ? parsed : DEFAULT_PAGE_SIZE;
}

export function RegistrosPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const searchKey = searchParams.toString();
  const pageValue = normalizePage(searchParams.get('page'));
  const sizeValue = normalizeSize(searchParams.get('size'));
  const appliedFilters = useMemo(() => initialFiltersFromParams(new URLSearchParams(searchKey)), [searchKey]);
  const [filters, setFilters] = useState(appliedFilters);
  const [pageData, setPageData] = useState({
    content: [],
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterErrors, setFilterErrors] = useState({});
  const [lastAppliedAt, setLastAppliedAt] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    setFilters(appliedFilters);
  }, [appliedFilters]);

  useEffect(() => {
    document.getElementById('registros-protocolo')?.focus();
  }, []);

  useEffect(() => {
    let active = true;

    async function loadRegistros() {
      try {
        setLoading(true);
        setError('');
        const data = await registrosService.getRegistros({
          protocolo: appliedFilters.protocolo.trim() || undefined,
          tipoFatoId: appliedFilters.tipoFatoId || undefined,
          status: appliedFilters.status || undefined,
          dataInicio: toApiDateTime(appliedFilters.dataInicio),
          dataFim: toApiDateTime(appliedFilters.dataFim),
          page: pageValue,
          size: sizeValue,
          sort: 'criadoEm,desc',
        });

        if (active) {
          setPageData({
            content: data.content ?? [],
            page: data.page ?? 0,
            size: data.size ?? DEFAULT_PAGE_SIZE,
            totalElements: data.totalElements ?? 0,
            totalPages: data.totalPages ?? 0,
          });
        }
      } catch (requestError) {
        if (active) {
          setError(requestError.response?.data?.message ?? 'Nao foi possivel carregar os registros.');
          setPageData({
            content: [],
            page: 0,
            size: DEFAULT_PAGE_SIZE,
            totalElements: 0,
            totalPages: 0,
          });
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadRegistros();

    return () => {
      active = false;
    };
  }, [appliedFilters, pageValue, reloadKey, sizeValue]);

  function applyFilters() {
    const validationErrors = validateFilters(filters);
    setFilterErrors(validationErrors);

    if (Object.keys(validationErrors).length) {
      document.getElementById(validationErrors.dataInicio ? 'registros-data-inicio' : 'registros-data-fim')?.focus();
      return;
    }

    const next = new URLSearchParams();

    if (filters.protocolo.trim()) {
      next.set('protocolo', filters.protocolo.trim());
    }
    if (filters.tipoFatoId) {
      next.set('tipoFatoId', filters.tipoFatoId);
    }
    if (filters.status) {
      next.set('status', filters.status);
    }
    if (filters.dataInicio) {
      next.set('dataInicio', toApiDateTime(filters.dataInicio));
    }
    if (filters.dataFim) {
      next.set('dataFim', toApiDateTime(filters.dataFim));
    }

    next.set('page', '0');
    next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    setSearchParams(next, { replace: true });
    setLastAppliedAt(new Date());
  }

  function clearFilters() {
    setFilterErrors({});
    setFilters({
      protocolo: '',
      tipoFatoId: '',
      status: '',
      dataInicio: '',
      dataFim: '',
    });
    const next = new URLSearchParams();
    next.set('page', '0');
    next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    setSearchParams(next, { replace: true });
    setLastAppliedAt(null);
    document.getElementById('registros-protocolo')?.focus();
  }

  function changePage(nextPage) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(nextPage));
    next.set('size', String(sizeValue || DEFAULT_PAGE_SIZE));
    setSearchParams(next, { replace: true });
  }

  function retryLoad() {
    setReloadKey((current) => current + 1);
  }

  const columns = [
    {
      key: 'protocolo',
      header: 'Protocolo',
      render: (row) => <strong>{row.protocolo}</strong>,
    },
    {
      key: 'fato',
      header: 'Fato',
      render: (row) => <span>{formatValue(row.fato ?? row.tipoFatoNome)}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => (
        <Badge variant={STATUS_VARIANTS[row.status] ?? 'neutral'}>
          {STATUS_LABELS[row.status] ?? formatValue(row.status)}
        </Badge>
      ),
    },
    {
      key: 'origem',
      header: 'Origem',
      render: (row) => <Badge variant="secondary">{formatValue(row.origem)}</Badge>,
    },
    {
      key: 'nome',
      header: 'Nome',
      render: (row) => <span>{formatValue(row.nome)}</span>,
    },
    {
      key: 'criadoEm',
      header: 'Criado em',
      render: (row) => <span>{formatDateTime(row.criadoEm)}</span>,
    },
    {
      key: 'quantidadeAnexos',
      header: 'Anexos',
      render: (row) => <Badge>{row.quantidadeAnexos ?? 0}</Badge>,
    },
    {
      key: 'acoes',
      header: 'Acoes',
      render: (row) => (
        <div className="bp-action-group">
          <ActionIconButton
            icon={Eye}
            label="Visualizar"
            title="Visualizar"
            onClick={() =>
              navigate(`/app/registros/${row.id}`, {
                state: { returnTo: `/app/registros${searchKey ? `?${searchKey}` : ''}` },
              })
            }
          />
        </div>
      ),
    },
  ];

  const hasFilters = Boolean(
    appliedFilters.protocolo ||
      appliedFilters.tipoFatoId ||
      appliedFilters.status ||
      appliedFilters.dataInicio ||
      appliedFilters.dataFim,
  );
  const hasFilterInput = Boolean(
    filters.protocolo ||
      filters.tipoFatoId ||
      filters.status ||
      filters.dataInicio ||
      filters.dataFim,
  );
  const activeFilters = buildActiveFilters(appliedFilters);
  const loadingLabel = hasFilters
    ? 'Pesquisando registros com os filtros informados...'
    : 'Carregando registros recebidos...';
  const pageStart = pageData.totalElements ? pageData.page * pageData.size + 1 : 0;
  const pageEnd = Math.min((pageData.page + 1) * pageData.size, pageData.totalElements);

  return (
    <div className="bp-registros-page bp-list-page">
      <nav aria-label="Breadcrumb" className="bp-breadcrumb">
        Canal de Etica / Registros
      </nav>

      <section className="bp-list-page__header">
        <div>
          <h1>Registros</h1>
          <p>Consulte os registros recebidos pelo Canal de Etica e acompanhe os dados essenciais para triagem interna.</p>
        </div>
      </section>

      <Card>
        <Card.Body>
          <p className="bp-registros-help">
            Use os filtros para localizar protocolos, tipos de fato ou periodos especificos. Pressione Enter para pesquisar
            e Esc para limpar os filtros.
          </p>
          <RegistroFilters
            disabled={loading}
            errors={filterErrors}
            filters={filters}
            hasFilters={hasFilters || hasFilterInput}
            onChange={setFilters}
            onClear={clearFilters}
            onSubmit={applyFilters}
          />
          {activeFilters.length ? (
            <div aria-label="Filtros aplicados" className="bp-registros-active-filters">
              <span>Filtros aplicados:</span>
              {activeFilters.map((filter) => (
                <Badge key={filter.label} variant="secondary">
                  {filter.label}
                </Badge>
              ))}
            </div>
          ) : null}
          {lastAppliedAt ? (
            <p className="bp-registros-feedback" role="status">
              Pesquisa aplicada. Resultados atualizados.
            </p>
          ) : null}
        </Card.Body>
      </Card>

      {error ? (
        <Alert variant="error" title="Nao foi possivel carregar os registros">
          <div className="bp-registros-error">
            <span>{error}</span>
            <Button size="sm" type="button" variant="secondary" onClick={retryLoad}>
              Tentar novamente
            </Button>
          </div>
        </Alert>
      ) : null}

      <Card>
        <Card.Body>
          {loading ? (
            <Loading label={loadingLabel} />
          ) : pageData.content.length ? (
            <>
              <div className="bp-registros-result-summary" role="status">
                <strong>{formatCount(pageData.totalElements)}</strong>
                <span>
                  Exibindo {pageStart}-{pageEnd} de {pageData.totalElements}. Ordenacao: mais recentes primeiro.
                </span>
              </div>
              <Table columns={columns} rows={pageData.content} />
              <Pagination
                page={pageData.page}
                size={pageData.size}
                totalElements={pageData.totalElements}
                totalPages={pageData.totalPages}
                onChangePage={changePage}
              />
            </>
          ) : (
            <EmptyState
              actionLabel={hasFilters ? 'Limpar filtros' : undefined}
              description={
                hasFilters
                  ? 'Nenhum registro corresponde aos filtros atuais. Revise o periodo, o tipo de fato ou o protocolo informado.'
                  : 'Quando o Canal de Etica receber registros, eles aparecerao aqui para consulta administrativa.'
              }
              onAction={hasFilters ? clearFilters : undefined}
              title="Nenhum registro encontrado"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
}

function validateFilters(filters) {
  const errors = {};
  if (filters.dataInicio && filters.dataFim) {
    const dataInicio = new Date(filters.dataInicio);
    const dataFim = new Date(filters.dataFim);
    if (!Number.isNaN(dataInicio.getTime()) && !Number.isNaN(dataFim.getTime()) && dataInicio > dataFim) {
      errors.dataInicio = 'A data inicial deve ser anterior ou igual a data final.';
      errors.dataFim = 'Revise o fim do periodo.';
    }
  }
  return errors;
}

function buildActiveFilters(filters) {
  const active = [];
  if (filters.protocolo) {
    active.push({ label: `Protocolo: ${filters.protocolo}` });
  }
  if (filters.tipoFatoId) {
    active.push({ label: `Tipo: ${TIPO_FATO_LABELS[filters.tipoFatoId] ?? filters.tipoFatoId}` });
  }
  if (filters.status) {
    active.push({ label: `Status: ${STATUS_LABELS[filters.status] ?? filters.status}` });
  }
  if (filters.dataInicio) {
    active.push({ label: `Inicio: ${formatDateTime(filters.dataInicio)}` });
  }
  if (filters.dataFim) {
    active.push({ label: `Fim: ${formatDateTime(filters.dataFim)}` });
  }
  return active;
}
