import { useEffect, useMemo, useState } from 'react';
import { Button, Input, Select } from '../../../shared/components/index.js';

const TIPO_FATO_OPTIONS = [
  { value: '1', label: 'Conduta' },
  { value: '2', label: 'Assedio sexual' },
  { value: '3', label: 'Violencia fisica' },
  { value: '4', label: 'Corrupcao' },
  { value: '5', label: 'Pratica de crimes hediondos' },
  { value: '6', label: 'Desvios de dinheiro e/ou material' },
  { value: '7', label: 'Violacao de leis ambientais' },
  { value: '8', label: 'Destruicao ou danificacao de patrimonio da empresa' },
  { value: '9', label: 'Porte de arma nao autorizado' },
  { value: '10', label: 'Uso de substancias ilicitas' },
  { value: '11', label: 'Retaliacao a denunciantes de boa-fe' },
  { value: '12', label: 'Agiotagem' },
  { value: '13', label: 'Decisoes em desacordo com a lei' },
  { value: '14', label: 'Informacoes caluniosas ou difamatorias' },
  { value: '15', label: 'Assedio moral' },
  { value: '16', label: 'Desrespeito a normas de seguranca' },
  { value: '17', label: 'Discriminacao e preconceito' },
  { value: '18', label: 'Uso indevido de dados e informacoes' },
  { value: '19', label: 'Adulteracao de cadastro' },
  { value: '20', label: 'Informacoes falsas sobre a empresa' },
  { value: '21', label: 'Desrespeito aos colegas' },
  { value: '22', label: 'Abuso do Canal de Denuncia' },
  { value: '23', label: 'Descumprimento de processos' },
  { value: '24', label: 'Decisoes em desacordo com normas internas' },
  { value: '25', label: 'Outros' },
];

const STATUS_OPTIONS = [
  { value: 'RECEBIDO', label: 'Recebido' },
];

const ADVANCED_FILTERS_STORAGE_KEY = 'canal-etica-registros-filtros-avancados-aberto';

export function RegistroFilters({
  disabled = false,
  errors = {},
  filters,
  hasFilters,
  onChange,
  onClear,
  onSubmit,
}) {
  const advancedFiltersCount = useMemo(
    () =>
      [filters.tipoFatoId, filters.status, filters.dataInicio, filters.dataFim].filter((value) =>
        Boolean(String(value ?? '').trim()),
      ).length,
    [filters.dataFim, filters.dataInicio, filters.status, filters.tipoFatoId],
  );
  const [advancedOpen, setAdvancedOpen] = useState(() => {
    if (advancedFiltersCount > 0) {
      return true;
    }
    return window.localStorage.getItem(ADVANCED_FILTERS_STORAGE_KEY) === 'true';
  });

  useEffect(() => {
    if (advancedFiltersCount > 0) {
      setAdvancedOpen(true);
    }
  }, [advancedFiltersCount]);

  useEffect(() => {
    window.localStorage.setItem(ADVANCED_FILTERS_STORAGE_KEY, String(advancedOpen));
  }, [advancedOpen]);

  function updateField(field, value) {
    onChange({ ...filters, [field]: value });
  }

  function handleKeyDown(event) {
    if (event.key === 'Escape' && hasFilters) {
      event.preventDefault();
      onClear();
      document.getElementById('registros-protocolo')?.focus();
    }
  }

  function toggleAdvancedFilters() {
    setAdvancedOpen((current) => !current);
  }

  const advancedButtonLabel = advancedFiltersCount
    ? `Filtros avancados (${advancedFiltersCount})`
    : 'Filtros avancados';

  return (
    <CardlessForm onKeyDown={handleKeyDown} onSubmit={onSubmit}>
      <div className="bp-list-page__toolbar">
        <div className="bp-list-page__toolbar-row">
          <div className="bp-list-page__search">
            <Input
              autoComplete="off"
              id="registros-protocolo"
              label="Protocolo"
              placeholder="Ex.: CE-2026-000001"
              value={filters.protocolo}
              onChange={(event) => updateField('protocolo', event.target.value)}
            />
          </div>
          <div className="bp-list-page__actions">
            <Button disabled={disabled} size="sm" type="submit">
              Pesquisar
            </Button>
            <Button
              aria-controls="registros-filtros-avancados"
              aria-expanded={advancedOpen}
              disabled={disabled}
              size="sm"
              type="button"
              variant="secondary"
              onClick={toggleAdvancedFilters}
            >
              {advancedButtonLabel} {advancedOpen ? '▲' : '▼'}
            </Button>
          </div>
        </div>

        {advancedOpen ? (
          <div className="bp-registros-advanced-panel" id="registros-filtros-avancados">
            <div className="bp-list-page__filters">
              <Select
                id="registros-tipo-fato"
                label="Tipo de fato"
                options={TIPO_FATO_OPTIONS}
                placeholder="Todos os tipos"
                value={filters.tipoFatoId}
                onChange={(event) => updateField('tipoFatoId', event.target.value)}
              />
              <Select
                id="registros-status"
                label="Status"
                options={STATUS_OPTIONS}
                placeholder="Todos os status"
                value={filters.status}
                onChange={(event) => updateField('status', event.target.value)}
              />
              <Input
                error={errors.dataInicio}
                id="registros-data-inicio"
                label="Inicio do periodo"
                type="datetime-local"
                value={filters.dataInicio}
                onChange={(event) => updateField('dataInicio', event.target.value)}
              />
              <Input
                error={errors.dataFim}
                id="registros-data-fim"
                label="Fim do periodo"
                type="datetime-local"
                value={filters.dataFim}
                onChange={(event) => updateField('dataFim', event.target.value)}
              />
            </div>

            <div className="bp-registros-advanced-actions">
              <Button disabled={disabled || !hasFilters} size="sm" type="button" variant="secondary" onClick={onClear}>
                Limpar filtros
              </Button>
            </div>
          </div>
        ) : null}
      </div>
    </CardlessForm>
  );
}

function CardlessForm({ children, onKeyDown, onSubmit }) {
  return (
    <form
      onKeyDown={onKeyDown}
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit();
      }}
    >
      {children}
    </form>
  );
}
