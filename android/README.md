# Discipline+ · appli Android (blocage réel)

Appli native Kotlin qui affiche l'appli web Discipline+ dans une WebView et bloque
réellement TikTok, YouTube, Instagram, Snapchat et X tant que la mission du jour
n'est pas validée.

## Comment ça marche

1. `MainActivity` charge `https://abdelkaderyahia05-png.github.io/discipline-plus/` dans une WebView.
2. Un petit script injecté lit `localStorage['disciplineplus.v1']` et l'envoie au pont
   `DisciplinePont.etat(json)` à chaque changement (champs `missionFaite`, `appsBloquees`, `historique`).
   L'appli web n'a pas besoin d'être modifiée.
3. `BlocageService` (service d'accessibilité) est prévenu quand une appli passe au premier plan.
   Si elle est dans la liste et que la mission n'est pas validée **aujourd'hui**, il ouvre
   `BlocageActivity` : écran violet « Termine ta mission d'abord » + bouton « Aller à ma mission ».
4. `AccueilActivity` explique l'autorisation d'accessibilité (et le cas « Paramètre restreint »).

Autorisations : Internet (automatique) et Accessibilité (activée à la main par l'utilisateur).
Le service ne lit pas le contenu de l'écran (`canRetrieveWindowContent=false`).

## Construire l'APK de test

Il faut un JDK 17 et le SDK Android (plateforme 34, build-tools 35).

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.repunlock-tools\jdk17"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`

## Installer sur le téléphone

- Par câble USB (débogage USB activé) : `adb install -r app\build\outputs\apk\debug\app-debug.apk`
- Ou copier l'APK sur le téléphone, l'ouvrir et accepter « Installer des applis inconnues ».

Puis ouvrir Discipline+ et suivre l'écran d'installation. Sur Android 13+, si l'interrupteur
d'accessibilité est grisé (« Paramètre restreint ») : fiche de l'appli → ⋮ → « Autoriser les
paramètres restreints ».

## Limites connues

- Les sites web (youtube.com, instagram.com… dans Chrome) ne sont pas bloqués, seulement les applis.
- L'utilisateur peut désactiver le service d'accessibilité ou désinstaller l'appli.
- Certains téléphones (Xiaomi, Huawei, Oppo…) coupent les services en arrière-plan : il faut
  alors autoriser le démarrage automatique / retirer l'économie de batterie pour Discipline+.
- L'état vient du `localStorage` de la WebView : il est séparé de celui de l'appli web ouverte
  dans Chrome (la progression ne se partage pas entre les deux).
- Photo de preuve : ouvre le sélecteur de fichiers / galerie, pas directement l'appareil photo.
- Version Play Store : `bundleRelease` produit un AAB signé par la clé d'envoi (voir PLAY-STORE.md).
- iPhone : impossible depuis Windows (il faut un Mac, Xcode et l'autorisation Family Controls d'Apple).
