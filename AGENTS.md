# AGENTS

## Produktkern

`Days` folgt aktuell dieser Kette:

`Quelle -> Signal -> Bereichslogik -> Bereichsfeed in Single -> Profillernen -> Multi`

Arbeite immer von dieser Kette aus, nicht von einer losen Featureliste.

## Aktive Source of Truth

Nutze zuerst diese Dateien:
- `docs/product-map.md`
- `docs/start-plan.md`
- `docs/single-plan.md`
- `docs/multi-plan.md`
- `docs/settings-plan.md`
- `docs/decisions.md`

Aeltere Varianten, Parallelplaene und alte Benennungen sollen in diese Dateien eingearbeitet oder entfernt werden.

## Repo-Regeln

1. Nach Code-Aenderungen und nach relevanten Builds die App auf Emulator und auf alle zusaetzlich verbundenen Android-Geraete aktualisieren.
2. Installierte Debug-Builds auf Emulator und verbundenen Geraeten nicht am Aufgabenende deinstallieren. Die App soll fuer die naechste Iteration installiert bleiben.
3. `installDebug` allein reicht nicht als Nachweis. Nach Installationen immer direkt per `adb shell pm list packages` pruefen, dass `com.struperto.androidappdays.debug` vorhanden ist, und danach die Launcher-Activity oder die aktuell resume-te Activity fuer dieses Paket verifizieren.
4. Vor groesseren Aenderungen zuerst die relevanten Dateien fuer Navigation, Theme, State, Datenhaltung und betroffene Screens gesammelt lesen.
5. Dauerhafte Produktregeln gehoeren in wenige klare Markdown-Dateien unter `docs/`.
6. Screenshots, XML-Dumps, DB-Files und andere Zwischenstaende gehoeren nach `tmp/` oder in ignorierte Pfade und bleiben nicht im Repo-Root liegen.
7. Alte Varianten, geparkte Texte, ungenutzter Code und offensichtliche Test- oder Explorationsreste sollen entfernt statt mitgeschleppt werden.
8. Am Ende jeder Aufgabe knapp berichten: was geaendert wurde, was verifiziert wurde und was offen bleibt.

## UI-Regeln

1. Bei mobilen Compose-/Kotlin-/Material-Screens zuerst die eigentliche Arbeitsflaeche bauen: wenig Text, keine unnoetigen Erklaerungen, keine ueberfluessigen Unterkacheln.
2. Wenn ein Screen als Feed, Werkbank oder Kachelflaeche gedacht ist, die visuelle Struktur ueber Form, Abstand, Status und klare Hierarchie loesen statt ueber lange Texte.
3. Auf hohen mobilen Screens keine unnoetig quadratischen Dashboards bauen; Hoehe und Breite bewusst ausnutzen, wenn das Muster davon profitiert.
4. Bei groesseren UI/UX-Aenderungen zuerst einen lauffaehigen Screen bauen, dann einen echten Screenshot aus dem Emulator aufnehmen, ihn analysieren und erst danach weiter iterieren.
5. Wenn ein Layout im Screenshot unruhig, unbalanciert oder stilistisch daneben wirkt, nicht mit mehr Elementen retten; erst das Grundmuster hinterfragen und vereinfachen.
6. Wenn ein Sheet mehr als schnelle Einzelaktionen tragen soll, frueh in eigene Screens oder klare Teilflaechen aufteilen; keine halben Komplettflows in Bottom Sheets druecken.
7. Bei identitaetsstiftenden Bereichen Bearbeitung sichtbar verankern, z. B. Icon, Titel oder Bedeutung direkt mit klar erkennbarem Editieranker statt versteckter Settings-Logik.
8. Den `Bereich erstellen`-Flow nicht nur leer beurteilen. Fuer echte Iterationen immer konkrete Beispielsaetze eingeben, den Flow komplett durchlaufen und daraus Interview-/Rollentest-Erkenntnisse ableiten.
9. Auf mobilen Screens keine Dreifach-Einstiege bauen. Eine klare Ueberschrift reicht meist; weitere Einfuehrung nur, wenn sie wirklich etwas entscheidet.
10. Erklaertext nur dort, wo er eine Entscheidung oder einen Status klaert. Wenn Form, Abstand, Status oder Platzierung reichen, Text weglassen.
11. In `Stand`, `Eingang`, `Fokus` und `Takt` zuerst direkte Menuefuehrung bauen: klare Liste, klarer Status, moeglichst kein vorgeschalteter Erklaerblock.
12. Bereichsanalyse innerhalb eines Bereichs still im Hintergrund laufen lassen; sichtbar bleibt sie primaer ueber das Icon, nicht als eigene Standardzeile.
13. In der ersten und zweiten Ebene von `Stand`, `Eingang`, `Fokus` und `Takt` Menuezeilen moeglichst kompakt halten: Name links, Wert rechts, Zusatztext nur wenn ohne ihn der Zustand unklar waere.
14. Im `Analyse`-Schritt von `Bereich erstellen` keine doppelte Innen-Ueberschrift bauen. Nach abgeschlossener Analyse nur eine kompakte Karte mit Bereichstitel, Status und moeglichen Quellen zeigen.

## Begriffe

- `Start` ist der Orchestrator und die Werkbank.
- `Bereich` ist die editierbare Logikflaeche mit eigener Absicht.
- `Single` bleibt der Modusname und zeigt den priorisierten `Bereichsfeed`.
- `Bereichsinfo` ist keine aktive Produktsprache mehr.
- `Multi` bleibt sichtbar, aber vorerst nachrangig.
