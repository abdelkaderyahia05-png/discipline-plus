package app.disciplineplus

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import org.json.JSONObject
import java.util.Calendar

/**
 * État partagé entre la WebView (qui le reçoit de l'appli web) et le service de blocage.
 * Stocké dans les SharedPreferences : survit au redémarrage du téléphone.
 */
object Etat {
    const val URL_APPLI = "https://abdelkaderyahia05-png.github.io/discipline-plus/"
    const val HOTE_APPLI = "abdelkaderyahia05-png.github.io"

    private const val PREFS = "etat"
    private const val K_FAITE = "missionFaite"
    private const val K_VALIDEE_LE = "valideeLe"
    private const val K_APPS = "appsBloquees"

    /** Identifiant de l'appli web -> nom affiché + paquets Android correspondants. */
    class Appli(val nom: String, val paquets: List<String>)

    val APPLIS = mapOf(
        "tiktok" to Appli("TikTok", listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill", "com.zhiliaoapp.musically.go", "com.tiktok.lite.go")),
        "youtube" to Appli("YouTube", listOf("com.google.android.youtube", "com.google.android.apps.youtube.mango")),
        "instagram" to Appli("Instagram", listOf("com.instagram.android", "com.instagram.lite")),
        "snapchat" to Appli("Snapchat", listOf("com.snapchat.android")),
        "x" to Appli("X", listOf("com.twitter.android", "com.twitter.android.lite")),
    )

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Reçoit le JSON de localStorage['disciplineplus.v1'] envoyé par le pont JavaScript. */
    fun enregistrer(c: Context, json: String) {
        if (json.isBlank()) return
        try {
            val o = JSONObject(json)
            val faite = o.optBoolean("missionFaite", false)
            val apps = HashSet<String>()
            val tab = o.optJSONArray("appsBloquees")
            if (tab != null) {
                for (i in 0 until tab.length()) tab.optString(i).takeIf { it in APPLIS }?.let { apps.add(it) }
            } else {
                apps.addAll(APPLIS.keys)
            }
            // Date de validation : dernière entrée de l'historique de l'appli web.
            var valideeLe = 0L
            val hist = o.optJSONArray("historique")
            if (faite && hist != null && hist.length() > 0) {
                valideeLe = hist.optJSONObject(hist.length() - 1)?.optLong("date", 0L) ?: 0L
            }
            if (faite && valideeLe == 0L) valideeLe = System.currentTimeMillis()
            prefs(c).edit()
                .putBoolean(K_FAITE, faite)
                .putLong(K_VALIDEE_LE, valideeLe)
                .putStringSet(K_APPS, apps)
                .apply()
        } catch (e: Exception) {
            // JSON illisible : on garde le dernier état connu.
        }
    }

    private fun debutDuJour(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** La mission est validée dans l'appli web, mais un jour précédent. */
    fun valideeUnAutreJour(c: Context): Boolean {
        val p = prefs(c)
        return p.getBoolean(K_FAITE, false) && p.getLong(K_VALIDEE_LE, 0L) < debutDuJour()
    }

    /** Mission du jour validée = validée dans l'appli web ET validée aujourd'hui. */
    fun missionValideeAujourdhui(c: Context): Boolean {
        val p = prefs(c)
        return p.getBoolean(K_FAITE, false) && p.getLong(K_VALIDEE_LE, 0L) >= debutDuJour()
    }

    /** Renvoie le nom de l'appli à bloquer, ou null s'il ne faut rien bloquer. */
    fun appliABloquer(c: Context, paquet: String): String? {
        val entree = APPLIS.entries.firstOrNull { paquet in it.value.paquets } ?: return null
        // Tant que l'appli web n'a rien envoyé : tout est bloqué (même réglage par défaut que le web).
        val choisies = prefs(c).getStringSet(K_APPS, null) ?: APPLIS.keys
        if (entree.key !in choisies) return null
        if (missionValideeAujourdhui(c)) return null
        return entree.value.nom
    }

    /** Le service d'accessibilité Discipline+ est-il activé dans les réglages ? */
    fun blocageActif(c: Context): Boolean {
        val actifs = Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val moi = ComponentName(c, BlocageService::class.java)
        return actifs.split(':').any { ComponentName.unflattenFromString(it) == moi }
    }
}
