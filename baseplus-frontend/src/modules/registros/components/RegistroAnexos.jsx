import { Eye } from 'lucide-react';
import { ActionIconButton, Card, EmptyState, Table } from '../../../shared/components/index.js';

export function RegistroAnexos({ anexos = [], loadingAnexoId, onVisualizarAnexo }) {
  const columns = [
    {
      key: 'nomeOriginal',
      header: 'Nome original',
      render: (row) =>
        onVisualizarAnexo ? (
          <button
            className="bp-registro-anexo-link"
            disabled={loadingAnexoId === row.id}
            onClick={() => onVisualizarAnexo(row)}
            type="button"
          >
            {row.nomeOriginal || '-'}
          </button>
        ) : (
          <strong>{row.nomeOriginal || '-'}</strong>
        ),
    },
    {
      key: 'contentType',
      header: 'Tipo',
      render: (row) => <span>{formatAttachmentType(row.contentType, row.nomeOriginal)}</span>,
    },
    {
      key: 'tamanho',
      header: 'Tamanho',
      render: (row) => <span>{formatFileSize(row.tamanho)}</span>,
    },
    {
      key: 'criadoEm',
      header: 'Data',
      render: (row) => <span>{formatDateTime(row.criadoEm)}</span>,
    },
    {
      key: 'acoes',
      header: 'Acoes',
      render: (row) => (
        <div className="bp-action-group">
          <ActionIconButton
            disabled={loadingAnexoId === row.id}
            icon={Eye}
            label="Visualizar"
            title="Visualizar"
            onClick={() => onVisualizarAnexo?.(row)}
          />
        </div>
      ),
    },
  ];

  return (
    <Card>
      <Card.Header>
        <h2>Anexos</h2>
      </Card.Header>
      <Card.Body>
        {anexos.length ? (
          <Table columns={columns} rows={anexos} />
        ) : (
          <EmptyState title="Nenhum anexo informado" description="Nenhum anexo informado." />
        )}
      </Card.Body>
    </Card>
  );
}

function formatFileSize(value) {
  const size = Number(value ?? 0);
  if (!Number.isFinite(size) || size <= 0) {
    return '-';
  }
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function formatAttachmentType(contentType, filename) {
  const normalizedType = String(contentType ?? '').toLowerCase();
  const extension = String(filename ?? '').toLowerCase().split('.').pop();

  if (normalizedType.startsWith('image/') || ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'ico'].includes(extension)) {
    return 'Imagem';
  }
  if (normalizedType.startsWith('video/') || ['mp4', 'mov', 'avi', 'mkv', 'webm'].includes(extension)) {
    return 'Video';
  }
  if (normalizedType.startsWith('audio/') || ['mp3', 'wav', 'm4a', 'ogg'].includes(extension)) {
    return 'Audio';
  }
  if (
    normalizedType.startsWith('text/') ||
    normalizedType.includes('pdf') ||
    normalizedType.includes('word') ||
    normalizedType.includes('excel') ||
    normalizedType.includes('spreadsheet') ||
    normalizedType.includes('powerpoint') ||
    normalizedType.includes('presentation') ||
    ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'csv'].includes(extension)
  ) {
    return 'Documento';
  }

  return contentType || '-';
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
