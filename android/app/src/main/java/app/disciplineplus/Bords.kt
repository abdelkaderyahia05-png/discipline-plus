package app.disciplineplus

import android.app.Activity
import android.os.Build
import android.view.WindowInsets

/**
 * Android 15 et plus (targetSdk 35+) dessine l'appli sous la barre d'état et la barre de navigation.
 * On décale le contenu d'autant (et du clavier quand il est ouvert) pour que rien ne passe dessous.
 * Avant Android 15, Android le fait déjà tout seul : on ne touche à rien.
 */
object Bords {
    fun proteger(a: Activity) {
        if (Build.VERSION.SDK_INT < 35) return
        val racine = a.findViewById<android.view.View>(android.R.id.content)
        racine.setOnApplyWindowInsetsListener { v, insets ->
            val barres = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            val clavier = insets.getInsets(WindowInsets.Type.ime())
            v.setPadding(barres.left, barres.top, barres.right, maxOf(barres.bottom, clavier.bottom))
            insets
        }
        racine.requestApplyInsets()
    }
}
