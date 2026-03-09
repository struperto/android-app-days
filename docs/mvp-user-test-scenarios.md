# MVP User Test Scenarios

Diese Datei beschreibt `10` Test-Personas fuer den aktuellen MVP. Ziel ist nicht statistische Validierung, sondern strukturierte Produktforschung.

## Testrahmen

- Jede Persona wird fuer mindestens `1` Tagesdurchlauf und idealerweise fuer `3` Rueckblick-Tage gespielt.
- Geprueft werden immer dieselben Ebenen:
  - Versteht man `Home` ohne Erklaerung?
  - Bleibt `Unknown` neutral?
  - Sind Quellen, Ziele und manuelle Werte in `Settings` klar genug?
  - Wirken Hypothesen vorsichtig und glaubwuerdig?
- Bei jeder Persona sollten Screenshots von `Home`, `Settings / Ziele` und `Settings / Hypothesen` gesammelt werden.
- Die visuelle Regression fuer `Home` laeuft ueber `scripts/capture_persona_dashboard_suite.sh`.

## Pilot-Fokus

Fuer die naechste MVP-Iteration werden vier Personas als Leitset behandelt:

- `Lena / Early Athlete`: gute Tage muessen ruhig und nicht alarmierend aussehen.
- `Mara / Busy Parent`: `Unknown` und Sparse Data muessen neutral bleiben.
- `Jonas / Night Owl Creative`: Abendfenster muessen sichtbar produktiv sein duerfen.
- `Paul / Overloaded Lead`: vernetzte Belastung muss schnell lesbar werden.

Akzeptanzfragen fuer jede Home-Iteration:

- Erkennt man das dominante Tagesproblem in unter `10` Sekunden?
- Wirkt ein guter Tag wirklich ruhig?
- Bleibt `Unknown` ohne moralischen Alarm?
- Unterscheiden sich `Vormittag`, `Mittag` und `Abend` visuell genug?

## Personas

### 1. Lena / Early Athlete
- Kernfrage: Bleibt ein stabiler guter Tag ruhig und unaufgeregt?
- Quellen: `Health Connect`, `Manual`
- Erwartung:
  - Schlaf und Bewegung oft `On track`
  - wenig visuelle Dramatik
  - `Home` darf nicht wie ein Problem-Dashboard wirken

### 2. Mara / Busy Parent
- Kernfrage: Vertraegt der MVP lueckenhafte Daten und hohe Unterbrechung?
- Quellen: `Calendar`, `Notifications`, `Manual`
- Erwartung:
  - viele neutrale Bereiche
  - Schlaf-/Stoerdruck-Hinweise nur als moegliche Muster
  - keine Bestrafung fuer fehlende Watch-Daten

### 3. Jonas / Night Owl Creative
- Kernfrage: Kann der MVP ungewoehnliche Fokusfenster aushalten?
- Quellen: `Health Connect`, `Manual`
- Erwartung:
  - Fokus darf im Abendfenster sinnvoll wirken
  - Schlaf nicht pauschal moralisch bewerten

### 4. Alex / Desk PM
- Kernfrage: Zeigt die App Kalenderdruck als Kontext statt als Schuld?
- Quellen: `Calendar`, `Health Connect`, `Manual`
- Erwartung:
  - Fokus reagiert auf Meetinglast
  - Bewegung sinkt sichtbar, aber nicht ueberdramatisch

### 5. Sara / Recovery Week
- Kernfrage: Werden Kernziele vor Leistung priorisiert?
- Quellen: `Manual`
- Erwartung:
  - Schlaf und Hydration dominieren
  - wenig Fokus oder Bewegung wird nicht wie Totalausfall dargestellt

### 6. Tim / Nutrition Optimizer
- Kernfrage: Tragen manuelle Daten im MVP schon genug?
- Quellen: `Manual`, `Health Connect`
- Erwartung:
  - Protein-Range und Hydration klar lesbar
  - `Settings` muss fuer manuelle Werte schnell genug sein

### 7. Nico / Freelance Switcher
- Kernfrage: Kommt die App mit fluiden Tagen und Kontextwechseln klar?
- Quellen: `Calendar`, `Notifications`, `Manual`
- Erwartung:
  - Fokus- und Stoerdruck-Hinweise duennen sich nicht zu stark aus
  - Hypothesen bleiben vorsichtig

### 8. Lea / Shift Worker
- Kernfrage: Wo bricht das aktuelle Tagesmodell bei Sonderrhythmen?
- Quellen: `Health Connect`, `Manual`
- Erwartung:
  - testet bewusst eine Modellgrenze
  - liefert Lernpunkte fuer spaetere flexiblere Tagesanker

### 9. Ben / Minimal Manual Tracker
- Kernfrage: Bleibt die App ohne Integrationen nutzbar?
- Quellen: `Manual`
- Erwartung:
  - schneller Einstieg
  - `Unknown` ist okay
  - Home bleibt trotzdem sinnvoll

### 10. Paul / Overloaded Lead
- Kernfrage: Wie gut zeigt der MVP vernetzte Belastung?
- Quellen: `Health Connect`, `Calendar`, `Notifications`, `Manual`
- Erwartung:
  - Schlaf, Fokus und Bewegung kippen gemeinsam
  - Hypothesen zu Stoerdruck und Kalenderlast entstehen plausibel

## Durchspiel-Protokoll pro Persona

1. Quellen gemaess Persona aktivieren oder deaktivieren.
2. Zielwerte gemaess Persona in `Settings` setzen.
3. Manuelle Werte des Tages eintragen, falls vorgesehen.
4. `Home` ansehen:
   - Sind die Kern-Domaenen intuitiv?
   - Sind die Stundenhinweise ruhig genug?
5. Hypothesen pruefen:
   - klingen sie weich genug?
   - fuehlen sie sich plausibel an?
6. Offene Punkte notieren:
   - Verstaendnisproblem
   - visuelle Ueberladung
   - fehlende Quelle
   - falsche Hypothese
   - unklare Priorisierung

## Wichtigste Lernziele

- Ob `Home` auch ohne Erklaertexte selbsterklaerend genug ist
- Ob `Settings` als Kalibrierungsraum funktioniert
- Ob `Unknown` wirklich vertrauensbildend bleibt
- Ob erste Hypothesen glaubwuerdig statt besserwisserisch wirken
- Wo das Modell fuer Sonderfaelle noch zu starr ist
