import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Alert, Button, Card, EmptyState, Loading } from '../../../shared/components/index.js';
import { RegistroAnexos } from '../components/RegistroAnexos.jsx';
import { RegistroDadosDenunciante } from '../components/RegistroDadosDenunciante.jsx';
import { RegistroHeader } from '../components/RegistroHeader.jsx';
import { RegistroRelato } from '../components/RegistroRelato.jsx';
import * as registrosService from '../services/registrosService.js';
import './registroDetalhe.css';

export function RegistroDetalhePage() {
  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const [registro, setRegistro] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [anexoError, setAnexoError] = useState('');
  const [loadingAnexoId, setLoadingAnexoId] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadRegistro() {
      try {
        setLoading(true);
        setError('');
        setNotFound(false);
        const data = await registrosService.buscarPorId(id);

        if (active) {
          setRegistro(data);
        }
      } catch (requestError) {
        if (active) {
          if (requestError.response?.status === 404) {
            setNotFound(true);
            setRegistro(null);
          } else {
            setError(requestError.response?.data?.message ?? 'Nao foi possivel carregar o registro.');
          }
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadRegistro();

    return () => {
      active = false;
    };
  }, [id, reloadKey]);

  function backToList() {
    navigate(location.state?.returnTo || '/app/registros');
  }

  function retryLoad() {
    setReloadKey((current) => current + 1);
  }

  async function handleVisualizarAnexo(anexo) {
    if (!registro?.id || !anexo?.id) {
      return;
    }

    try {
      setAnexoError('');
      setLoadingAnexoId(anexo.id);
      const arquivo = await registrosService.visualizarAnexo(registro.id, anexo.id);
      const blob = arquivo.contentType ? new Blob([arquivo.blob], { type: arquivo.contentType }) : arquivo.blob;
      const url = URL.createObjectURL(blob);
      const opened = window.open(url, '_blank', 'noopener,noreferrer');

      if (!opened) {
        const link = document.createElement('a');
        link.href = url;
        link.download = getFilename(arquivo.contentDisposition) || anexo.nomeOriginal || 'anexo';
        document.body.appendChild(link);
        link.click();
        link.remove();
      }

      window.setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (requestError) {
      setAnexoError(requestError.response?.data?.message ?? 'Nao foi possivel abrir o anexo.');
    } finally {
      setLoadingAnexoId(null);
    }
  }

  return (
    <div className="bp-registro-detail-page bp-list-page">
      <nav aria-label="Breadcrumb" className="bp-registro-breadcrumb">
        <span>Canal de Etica</span>
        <span aria-hidden="true">&gt;</span>
        <span>Registros</span>
        <span aria-hidden="true">&gt;</span>
        <strong>Detalhe</strong>
      </nav>

      <section className="bp-list-page__header">
        <div>
          <h1>Detalhe do registro</h1>
          <p>Consulta completa e somente leitura do registro recebido pelo Canal de Etica.</p>
        </div>
        <Button type="button" variant="secondary" onClick={backToList}>
          Voltar
        </Button>
      </section>

      {loading ? (
        <Card>
          <Card.Body>
            <Loading label="Carregando dados do registro e anexos..." />
          </Card.Body>
        </Card>
      ) : null}

      {!loading && error ? (
        <Alert variant="error" title="Nao foi possivel carregar o registro">
          <div className="bp-registro-detail-error">
            <span>{error}</span>
            <Button size="sm" type="button" variant="secondary" onClick={retryLoad}>
              Tentar novamente
            </Button>
          </div>
        </Alert>
      ) : null}

      {!loading && notFound ? (
        <EmptyState
          actionLabel="Voltar"
          description="O registro solicitado nao foi encontrado ou nao esta mais disponivel para consulta."
          onAction={backToList}
          title="Registro nao encontrado"
        />
      ) : null}

      {!loading && registro ? (
        <>
          <Card>
            <Card.Body>
              <RegistroHeader registro={registro} />
            </Card.Body>
          </Card>

          <RegistroDadosDenunciante registro={registro} />
          <RegistroRelato relato={registro.relato} />
          {anexoError ? <Alert variant="error">{anexoError}</Alert> : null}
          <RegistroAnexos
            anexos={registro.anexos ?? []}
            loadingAnexoId={loadingAnexoId}
            onVisualizarAnexo={handleVisualizarAnexo}
          />
        </>
      ) : null}
    </div>
  );
}

function getFilename(contentDisposition) {
  if (!contentDisposition) {
    return '';
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const filenameMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  return filenameMatch?.[1] ?? '';
}
