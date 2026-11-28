package app.disciplineplus

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

/** Écran principal : l'appli web Discipline+ dans une WebView, reliée au blocage par un pont JavaScript. */
class MainActivity : Activity() {

    private lateinit var web: WebView
    private var choixFichier: ValueCallback<Array<Uri>>? = null

    /** Objet exposé à la page sous le nom `DisciplinePont`. */
    inner class Pont {
        @JavascriptInterface
        fun etat(json: String?) {
            Etat.enregistrer(this@MainActivity, json ?: "")
        }

        /** Permet à l'appli web de savoir si le vrai blocage est actif (facultatif). */
        @JavascriptInterface
        fun blocageActif(): Boolean = Etat.blocageActif(this@MainActivity)

        /** Permet à l'appli web de rouvrir l'écran des autorisations (facultatif). */
        @JavascriptInterface
        fun ouvrirAutorisations() {
            runOnUiThread { startActivity(Intent(this@MainActivity, AccueilActivity::class.java)) }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        Bords.proteger(this)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true // localStorage : c'est là que vit l'état de l'appli web
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.addJavascriptInterface(Pont(), "DisciplinePont")

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Seule l'appli Discipline+ reste dans la WebView (et a accès au pont) ; le reste part au navigateur.
                if (request.url.host == Etat.HOTE_APPLI) return false
                try { startActivity(Intent(Intent.ACTION_VIEW, request.url)) } catch (e: Exception) {}
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (Uri.parse(url).host == Etat.HOTE_APPLI) view.evaluateJavascript(SCRIPT_PONT, null)
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    Toast.makeText(this@MainActivity, R.string.hors_ligne, Toast.LENGTH_LONG).show()
                }
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            // Photo de preuve : <input type="file"> de l'appli web.
            override fun onShowFileChooser(view: WebView, rappel: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                choixFichier?.onReceiveValue(null)
                choixFichier = rappel
                return try {
                    startActivityForResult(params.createIntent(), DEMANDE_FICHIER)
                    true
                } catch (e: Exception) {
                    choixFichier = null
                    false
                }
            }
        }

        if (savedInstanceState == null) {
            web.loadUrl(Etat.URL_APPLI)
            // Premier lancement, ou blocage coupé : on (ré)explique l'autorisation.
            if (!Etat.blocageActif(this)) startActivity(Intent(this, AccueilActivity::class.java))
        } else {
            web.restoreState(savedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()
        // Resynchronise l'état à chaque retour dans l'appli (par exemple depuis l'écran de blocage).
        web.evaluateJavascript(SCRIPT_PONT, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEMANDE_FICHIER) {
            choixFichier?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            choixFichier = null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    companion object {
        private const val DEMANDE_FICHIER = 1

        /**
         * Injecté dans la page : envoie localStorage['disciplineplus.v1'] au pont tout de suite,
         * puis à chaque écriture (l'appli web réécrit cette clé à chaque changement d'état).
         * Aucune modification de l'appli web n'est nécessaire.
         */
        private const val SCRIPT_PONT = """
(function () {
  if (!window.DisciplinePont) return;
  var CLE = 'disciplineplus.v1';
  function envoyer() { try { window.DisciplinePont.etat(localStorage.getItem(CLE) || ''); } catch (e) {} }
  if (!window.__disciplinePont) {
    window.__disciplinePont = true;
    var ecrire = Storage.prototype.setItem;
    Storage.prototype.setItem = function (cle) {
      ecrire.apply(this, arguments);
      if (cle === CLE) envoyer();
    };
  }
  envoyer();
})();
"""
    }
}
