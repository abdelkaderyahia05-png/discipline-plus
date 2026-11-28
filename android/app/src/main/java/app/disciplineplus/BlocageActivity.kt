package app.disciplineplus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

/** Écran violet « Termine ta mission d'abord », affiché par-dessus une appli bloquée. */
class BlocageActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocage)
        afficher(intent)

        findViewById<Button>(R.id.aller).setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }
        findViewById<Button>(R.id.quitter).setOnClickListener { versAccueil() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        afficher(intent)
    }

    private fun afficher(intent: Intent?) {
        val nom = intent?.getStringExtra(EXTRA_NOM) ?: getString(R.string.app_generique)
        val modele = if (Etat.valideeUnAutreJour(this)) R.string.blocage_texte_jour else R.string.blocage_texte
        findViewById<TextView>(R.id.texte).text = getString(modele, nom)
    }

    /** Retour arrière : on va à l'écran d'accueil du téléphone, pas dans l'appli bloquée. */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = versAccueil()

    private fun versAccueil() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    companion object {
        const val EXTRA_NOM = "nom"
    }
}
