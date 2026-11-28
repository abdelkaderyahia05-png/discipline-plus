package app.disciplineplus

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Service d'accessibilité : Android le prévient à chaque changement de fenêtre.
 * Si l'appli qui passe au premier plan est bloquée et que la mission du jour n'est
 * pas validée, on affiche l'écran violet par-dessus.
 */
class BlocageService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val paquet = event.packageName?.toString() ?: return
        if (paquet == packageName) return
        val nom = Etat.appliABloquer(this, paquet) ?: return
        startActivity(
            Intent(this, BlocageActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(BlocageActivity.EXTRA_NOM, nom)
        )
    }

    override fun onInterrupt() {}
}
