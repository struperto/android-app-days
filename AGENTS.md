# AGENTS

1. Nach Code-Aenderungen und nach relevanten Builds die App auf Emulator und auf alle zusaetzlich verbundenen Android-Geraete aktualisieren.
2. Vor groesseren Aenderungen zuerst die relevanten Dateien fuer Navigation, Theme, State, Datenhaltung und betroffene Screens gesammelt lesen.
3. Bei mobilen Compose-/Kotlin-/Material-Screens zuerst die eigentliche Arbeitsflaeche bauen: wenig Text, keine unnoetigen Erklaerungen, keine ueberfluessigen Unterkacheln.
4. Wenn ein Screen als Dashboard oder Kachel gedacht ist, die visuelle Struktur ueber Form, Abstand, Orbit, Status und Bewegung loesen statt ueber lange Texte.
5. Auf hohen mobilen Screens keine unnoetig quadratischen Dashboards bauen; Hoehe und Breite bewusst ausnutzen, wenn das Muster davon profitiert.
6. Bei groesseren UI/UX-Aenderungen zuerst einen lauffaehigen Screen bauen, dann einen echten Screenshot aus dem Emulator aufnehmen, ihn analysieren und erst danach weiter iterieren.
7. Wenn ein Layout im Screenshot unruhig, unbalanciert oder stilistisch daneben wirkt, nicht mit mehr Elementen retten; erst das Grundmuster hinterfragen und vereinfachen.
8. Wenn ein Sheet mehr als schnelle Einzelaktionen tragen soll, frueh in eigene Screens oder klare Teilflaechen aufteilen; keine halben Komplettflows in Bottom Sheets druecken.
9. Bei identitaetsstiftenden Bereichen Bearbeitung sichtbar verankern, z. B. Icon, Titel oder Bedeutung direkt mit klar erkennbarem Editieranker statt versteckter Settings-Logik.
10. Dauerhafte Produktregeln gehoeren in wenige klare Markdown-Dateien unter `docs/`. Screenshots, XML-Dumps, DB-Files und andere Zwischenstaende gehoeren nach `tmp/` oder in ignorierte Pfade und bleiben nicht im Repo-Root liegen.
11. Am Ende jeder Aufgabe knapp berichten: was geaendert wurde, was verifiziert wurde und was offen bleibt.
