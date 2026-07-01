alter table registro add column fato text;

update registro
set fato = tipo_fato_nome
where fato is null;

alter table registro alter column fato set not null;
alter table registro alter column tipo_fato_id drop not null;
alter table registro alter column tipo_fato_nome drop not null;
