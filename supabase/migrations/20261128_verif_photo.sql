-- Compteur d'analyses photo par compte et par jour (aucune photo stockée).
create table if not exists public.verif_photo (
  user_id uuid not null references auth.users on delete cascade,
  jour date not null default (now() at time zone 'utc')::date,
  nb int not null default 0,
  primary key (user_id, jour)
);
alter table public.verif_photo enable row level security;
revoke all on public.verif_photo from anon, authenticated;

-- Incrémente le compteur du jour ; renvoie le nouveau total, ou -1 si la limite est atteinte.
create or replace function public.compter_verif_photo(p_user uuid, p_limite int)
returns int language plpgsql security definer set search_path = public as $$
declare n int;
begin
  insert into public.verif_photo (user_id, jour, nb) values (p_user, (now() at time zone 'utc')::date, 1)
  on conflict (user_id, jour) do update set nb = verif_photo.nb + 1
    where verif_photo.nb < p_limite
  returning nb into n;
  return coalesce(n, -1);
end $$;
revoke all on function public.compter_verif_photo(uuid, int) from public, anon, authenticated;
grant execute on function public.compter_verif_photo(uuid, int) to service_role;
