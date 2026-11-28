package app.disciplineplus

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView

/** Écran d'installation : explique chaque autorisation et pourquoi elle est demandée. */
class AccueilActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accueil)
        Bords.proteger(this)

        findViewById<Button>(R.id.ouvrirAccessibilite).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.ouvrirFiche).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.continuer).setOnClickListener { finish() }
        findViewById<Button>(R.id.plusTard).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        val actif = Etat.blocageActif(this)
        findViewById<TextView>(R.id.etat).setText(if (actif) R.string.perm1_ok else R.string.perm1_ko)
        findViewById<Button>(R.id.continuer).visibility = if (actif) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.plusTard).visibility = if (actif) View.GONE else View.VISIBLE
    }
}
