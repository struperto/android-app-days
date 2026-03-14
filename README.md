# Days

`Days` ist eine Android-App, die den Alltag nicht als Buzzword-Dashboard liest, sondern als klare Kette:

`Bereiche -> Single -> Profil -> spaeter kontextuelles Matching`

Im Produkt entstehen zuerst `Bereiche` mit eigener Absicht, Quellenlogik und Einstellungen. Daraus entsteht in `Single` der taegliche sichtbare Zustand: was in einem Bereich gerade laeuft, was wichtig ist und was verfolgt werden soll. Aus diesem echten Alltag lernt spaeter ein lebendes Profil, das nicht auf flachen Stammdaten basiert, sondern auf dem, was wirklich gelesen, verfolgt und umgesetzt wird. Erst darauf baut spaeter `Multi` auf: passende Menschen frueher sichtbar machen, die sonst oft nur zufaellig oder viel zu spaet erkannt werden.

Kurz gesagt: `Days` hilft erst dem Nutzer selbst, aus seinen Bereichen taeglich Klarheit zu gewinnen, und nutzt genau daraus spaeter besseres Matching.

## Tech

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- WorkManager

## Wichtige Befehle

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:installDebug
```

## Aktive Produkt-Doku

- `docs/product-map.md`
- `docs/start-plan.md`
- `docs/single-plan.md`
- `docs/multi-plan.md`
- `docs/settings-plan.md`
- `docs/decisions.md`
