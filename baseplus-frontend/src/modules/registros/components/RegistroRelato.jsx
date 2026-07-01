import { Card } from '../../../shared/components/index.js';

export function RegistroRelato({ relato }) {
  return (
    <Card>
      <Card.Header>
        <h2>Relato</h2>
      </Card.Header>
      <Card.Body>
        <p className="bp-registro-relato">{relato || 'Nao informado'}</p>
      </Card.Body>
    </Card>
  );
}
