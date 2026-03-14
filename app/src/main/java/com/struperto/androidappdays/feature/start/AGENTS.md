# AGENTS

## Start

`Start` ist die Werkbank von `Days`.

Der aktive Soll-Flow ist:

`Freitext -> Analyse -> Blocker / Questions -> Output-Vorschau -> Bereich anlegen`

## Regeln

1. Debug-Builds nach relevanten Builds auf Emulator und verbundenen Android-Geraeten installiert halten; nicht am Ende wieder deinstallieren.
2. Installation im `Start`-Flow nicht nur aus Gradle ableiten. Nach `installDebug` immer direkt Paket und laufende Activity fuer `com.struperto.androidappdays.debug` per `adb` verifizieren.
3. Behandle Analyse als Pflichtschritt, nicht als optionale Verzierung.
4. Wenn Analyse blockiert, den Blocker klar benennen und nach Moeglichkeit einen `Loesung finden`-Pfad anbieten.
5. Grosse Flow-Phasen bekommen eigene Screens statt ueberladene Sheets.
6. Der Create-Flow startet mit Freitext und darf danach je nach Analyse unterschiedlich verzweigen.
7. Die Vorschau soll mehrere Beispielkacheln zeigen und den spaeteren `Single`-Output spueren lassen.
8. `Start` ist keine stille Home-Flaeche, sondern die Orchestrierung fuer Bereiche, Quellen, Ziele und Regeln.
9. Bereiche zuerst als Signal-System denken, nicht als Spezialconnectoren fuer einzelne Apps.

## UI-Richtung

- klare Kacheln
- wenig Erklaertext
- bewusst sichtbare Analysephasen
- vertrauenswuerdige Blocker- und Fragen-Screens
- keine Settings-Hoehle im ersten Eindruck
