import { Badge } from '../../../shared/components/index.js';

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

export function RegistroHeader({ registro }) {
  return (
    <section className="bp-registro-detail-header" aria-label="Resumo do registro">
      <InfoItem label="Protocolo" value={registro.protocolo} strong />
      <InfoItem
        label="Status"
        value={
          <Badge variant={STATUS_VARIANTS[registro.status] ?? 'neutral'}>
            {STATUS_LABELS[registro.status] ?? formatValue(registro.status)}
          </Badge>
        }
      />
      <InfoItem label="Origem" value={<Badge variant="secondary">{formatValue(registro.origem)}</Badge>} />
      <InfoItem label="Fato" value={registro.fato ?? registro.tipoFatoNome} />
      <InfoItem label="Criado em" value={formatDateTime(registro.criadoEm)} />
      <InfoItem label="Atualizado em" value={formatDateTime(registro.atualizadoEm)} />
    </section>
  );
}

function InfoItem({ label, strong = false, value }) {
  return (
    <div className="bp-registro-info-item">
      <span>{label}</span>
      {strong ? <strong>{formatValue(value)}</strong> : <div>{value || 'Nao informado'}</div>}
    </div>
  );
}

function formatValue(value) {
  return value || 'Nao informado';
}

function formatDateTime(value) {
  if (!value) {
    return 'Nao informado';
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
