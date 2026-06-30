insert into permissions (name, description)
select 'REGISTROS_VIEW', 'Visualizar registros do Canal de Etica.'
where not exists (select 1 from permissions where name = 'REGISTROS_VIEW');

insert into permissions (name, description)
select 'REGISTROS_DETAIL', 'Visualizar detalhes de registros do Canal de Etica.'
where not exists (select 1 from permissions where name = 'REGISTROS_DETAIL');

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
cross join permissions p
where r.name = 'ADMIN'
  and p.name in ('REGISTROS_VIEW', 'REGISTROS_DETAIL')
  and not exists (
      select 1
      from role_permissions rp
      where rp.role_id = r.id
        and rp.permission_id = p.id
  );
