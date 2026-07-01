import { Card } from '../../../shared/components/index.js';

export function RegistroDadosDenunciante({ registro }) {
  return (
    <Card>
      <Card.Header>
        <h2>Identificacao do denunciante</h2>
      </Card.Header>
      <Card.Body>
        <div className="bp-registro-detail-grid">
          <InfoItem label="Nome" value={registro.nome} />
          <InfoItem label="Email" value={registro.email} />
          <InfoItem label="Telefone" value={registro.telefone} />
        </div>
      </Card.Body>
    </Card>
  );
}

function InfoItem({ label, value }) {
  return (
    <div className="bp-registro-info-item">
      <span>{label}</span>
      <strong>{value || 'Nao informado'}</strong>
    </div>
  );
}
