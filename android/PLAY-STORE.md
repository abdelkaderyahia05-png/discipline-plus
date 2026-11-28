# Discipline+ sur Google Play : ce qu'il reste à faire

## Ce qui est prêt

- Politique de confidentialité : https://abdelkaderyahia05-png.github.io/discipline-plus/confidentialite.html
- Page de suppression de compte : https://abdelkaderyahia05-png.github.io/discipline-plus/supprimer-compte.html
- Bouton « Supprimer mon compte » dans Paramètres → Compte (fonction Supabase `supprimer-compte`, à déployer).
- AAB signé : `android/app/build/outputs/bundle/release/app-release.aab` (targetSdk 36, versionCode 1, versionName 1.0).

## Clé d'envoi (à sauvegarder !)

- Keystore : `C:\Users\Kader\Documents\Discipline-Plus-cles\discipline-plus-envoi.jks` (alias `envoi`)
- Mots de passe : `C:\Users\Kader\Documents\Discipline-Plus-cles\keystore.properties` (copie dans `android/keystore.properties`, ignoré par git)
- Copie ces deux fichiers sur une clé USB **et** dans un coffre (gestionnaire de mots de passe, Drive privé…).
  Avec « Play App Signing » (activé par défaut), Google garde la vraie clé de signature : si la clé d'envoi est perdue,
  on peut demander à Google de la réinitialiser, mais c'est long. Ne la perds pas.

## Construire une nouvelle version

1. Dans `app/build.gradle.kts`, augmenter `versionCode` de 1 (et `versionName` si besoin).
2. ```powershell
   $env:JAVA_HOME = "$env:USERPROFILE\.repunlock-tools\jdk17"; $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"; .\gradlew.bat bundleRelease
   ```
3. Envoyer `app/build/outputs/bundle/release/app-release.aab` dans la Play Console.

## Déclaration « API d'accessibilité » (Play Console → Contenu de l'appli → Accessibilité)

Discipline+ n'est **pas** un outil d'accessibilité (`isAccessibilityTool` non déclaré) : il faut remplir la déclaration
et fournir une vidéo courte qui montre l'écran d'explication puis l'activation.

**Fonctionnalité principale et usage de l'API (texte à coller) :**

> Discipline+ est une application de bien-être numérique : l'utilisateur choisit des applications (TikTok, YouTube,
> Instagram, Snapchat, X) qui restent bloquées tant qu'il n'a pas validé sa mission du jour (rangement, sport, lecture…).
> Le service d'accessibilité BlocageService écoute uniquement les événements TYPE_WINDOW_STATE_CHANGED pour connaître
> le nom du paquet de l'application qui passe au premier plan. Si ce paquet fait partie des applications choisies par
> l'utilisateur et que la mission du jour n'est pas validée, Discipline+ affiche son propre écran « Termine ta mission
> d'abord ». Le service ne lit pas le contenu des fenêtres (canRetrieveWindowContent=false), n'effectue aucune action
> à la place de l'utilisateur, n'enregistre pas et ne transmet aucune donnée issue de l'API : le nom du paquet est
> comparé à la liste sur l'appareil puis oublié. Aucune autre API ne permet à une application tierce de détecter de
> manière fiable l'ouverture d'une autre application pour la bloquer.

**Divulgation et consentement :** au premier lancement, l'écran « Active le vrai blocage » (AccueilActivity) explique,
avant toute activation, ce que voit le service (seulement le nom de l'appli au premier plan), pourquoi il est nécessaire
et ce qu'il ne fait pas. L'utilisateur donne son accord en touchant « J'accepte : ouvrir les réglages d'accessibilité »,
puis active lui-même le service dans les réglages Android. Il peut choisir « Plus tard (sans blocage réel) ».

**Vidéo à filmer (30 s, à mettre sur YouTube en « non répertoriée ») :** lancer l'appli → écran d'explication →
« J'accepte » → réglages d'accessibilité → activer Discipline+ → revenir → ouvrir TikTok → écran « Termine ta mission d'abord ».

## Fiche du store (à remplir)

- Nom : Discipline+ · Description courte (80 caractères max) : « Une mission par jour pour débloquer tes applis. »
- Icône 512×512 : `discipline-plus/icons/icon-512.png` · Image de présentation 1024×500 · au moins 2 captures d'écran de téléphone.
- Catégorie : Productivité (ou Santé et remise en forme) · Coordonnées : e-mail de contact.
- Sécurité des données : e-mail (compte, facultatif), progression (fonctionnalité de l'appli), photos (traitées de
  façon éphémère, non conservées, pour une fonctionnalité de l'appli), chiffrement en transit : oui, suppression possible : oui
  + URL de la page de suppression. Aucune donnée partagée à des fins publicitaires.
- Classification du contenu (questionnaire), public cible (indiquer 13 ans et plus si le public visé inclut des ados :
  Google applique alors ses règles « Familles »), annonces : non.
- Compte personnel récent : Google impose un test fermé (au moins 12 testeurs pendant 14 jours) avant la production.
