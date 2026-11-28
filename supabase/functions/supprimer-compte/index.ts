// Discipline+ : supprime le compte de l'utilisateur connecté et toutes ses données.
// Appelée depuis l'appli (Paramètres → Compte → Supprimer mon compte) avec le jeton de session.
// La clé service_role reste ici, côté serveur : elle n'est jamais dans index.html.
// Secrets : SUPABASE_URL et SUPABASE_SERVICE_ROLE_KEY sont fournis automatiquement par Supabase.
import { createClient } from "npm:@supabase/supabase-js@2.49.4";

const ENTETES = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};
const repondre = (corps: unknown, statut = 200) =>
  new Response(JSON.stringify(corps), { status: statut, headers: { ...ENTETES, "Content-Type": "application/json" } });

const admin = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!, {
  auth: { persistSession: false, autoRefreshToken: false },
});

// Toutes les tables qui contiennent des lignes d'un utilisateur (colonne user_id).
const TABLES = ["progression", "verif_photo"];

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: ENTETES });
  if (req.method !== "POST") return repondre({ erreur: "methode" }, 405);

  // 1. Qui demande ? Le jeton est vérifié auprès de Supabase Auth : on ne peut supprimer que son propre compte.
  const jeton = (req.headers.get("Authorization") || "").replace(/^Bearer\s+/i, "");
  if (!jeton) return repondre({ erreur: "non_connecte" }, 401);
  const { data: u, error: errU } = await admin.auth.getUser(jeton);
  if (errU || !u?.user) return repondre({ erreur: "non_connecte" }, 401);
  const id = u.user.id;

  // 2. Garde-fou contre un appel par erreur : l'appli envoie { confirmation: "SUPPRIMER" }.
  let corps: { confirmation?: string } = {};
  try { corps = await req.json(); } catch { /* corps vide */ }
  if (corps.confirmation !== "SUPPRIMER") return repondre({ erreur: "confirmation" }, 400);

  // 3. Effacer les lignes de l'utilisateur, puis le compte lui-même.
  for (const table of TABLES) {
    const { error } = await admin.from(table).delete().eq("user_id", id);
    if (error) {
      console.error(`supprimer-compte: ${table}:`, error.message);
      return repondre({ erreur: "indisponible" }, 503);
    }
  }
  const { error: errD } = await admin.auth.admin.deleteUser(id);
  if (errD) {
    console.error("supprimer-compte: auth:", errD.message);
    return repondre({ erreur: "indisponible" }, 503);
  }
  return repondre({ supprime: true });
});
