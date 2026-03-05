## Projekt-Wissensbasis – Skiroutenplaner (AA2Git)

Dieses Dokument fasst das fachliche und technische Wissen für die Umsetzung der Abschlussaufgabe 2 (Skiroutenplaner) in diesem Repository zusammen. Es dient als Referenz für Architektur, Domänenmodell, Algorithmen und die zu unterstützenden Befehle.

---

## 1. Ziel des Systems

Das Programm implementiert einen **interaktiven Skiroutenplaner**:

- **Eingabe**: Skigebiet aus Datei (Mermaid-ähnliche Syntax), Befehle über Standard-Eingabe.
- **Zustand**: Aktueller Fahrer (Können, Ziel, Präferenzen), geladenes Skigebiet, geplante/aktive Route.
- **Ausgabe**: Exakt spezifizierte Textausgaben (Liste von Liften/Pisten, Statusmeldungen, Fehlermeldungen).
- **Aufgabe**: Finde eine Route (Folge von Liften und Pisten) in einem gerichteten Graphen mit Zeitrestriktion, die je nach Ziel (Höhenmeter, Distanz, Anzahl Fahrten, Anzahl unterschiedlicher Pisten) optimal ist.

Das System läuft, bis der Benutzer `quit` eingibt.

---

## 2. Domänenmodell

### 2.1 Skigebiet und Knoten

- **Skigebiet (`SkiArea`)**:
  - Gerichteter Graph aus Knoten (Lifte und Pisten).
  - Es gibt mindestens **eine Talstation** (Transit-Lift) und mindestens **eine Piste**.
  - Jede Kante repräsentiert eine mögliche direkte Fahrt (Lift auf Lift/Piste, Piste auf Lift/Piste).

- **Lift (`Lift`)**:
  - Eigenschaften:
    - `id` (eindeutig)
    - `liftType` (`GONDOLA`, `CHAIRLIFT`)
    - `startTime`, `endTime` (Betriebszeiten, im Format `HH:MM`)
    - `rideDuration` (Fahrtdauer in Minuten)
    - `waitingTime` (Anstehzeit in Minuten)
    - `transit` (boolesches Flag für Talstationen)
  - Nutzungsvoraussetzung:
    - `startTime <= (aktuelleZeit + waitingTime) <= endTime`.

- **Piste (`Slope`)**:
  - Eigenschaften:
    - `id` (eindeutig)
    - `difficulty` (`BLUE`, `RED`, `BLACK`)
    - `surface` (`REGULAR`, `ICY`, `BUMPY`)
    - `length` (Meter)
    - `heightDifference` (Höhenmeter)
  - Fahrzeit wird über eine Formel (siehe Abschnitt 4.2) berechnet.

### 2.2 Skifahrer

- **Können (`Skill`)**:
  - `BEGINNER`, `INTERMEDIATE`, `EXPERT`
  - Beeinflusst die Fahrzeit auf einer Piste (Schwierigkeitsmodifikator).

- **Ziel (`Goal`)**:
  - `ALTITUDE` – maximale Höhenmeter
  - `DISTANCE` – maximale Strecke
  - `NUMBER` – maximale Anzahl Pistenfahrten
  - `UNIQUE` – maximale Anzahl unterschiedlicher Pisten

- **Präferenzen (`Preferences`)**:
  - Positiv (`like`) und negativ (`dislike`) für:
    - `difficulty` (BLUE/RED/BLACK)
    - `surface` (REGULAR/ICY/BUMPY)
  - Bewertungsfunktion:
    - +1 für jede Übereinstimmung mit einer positiven Präferenz
    - −1 für jede Übereinstimmung mit einer negativen Präferenz
  - Nutzung:
    - Auflösung von Gleichständen, wenn zwei Pisten für das Ziel gleich gut sind.
    - Bei weiterem Gleichstand: lexikografischer Vergleich der Pisten-ID.

---

## 3. Architektur

### 3.1 Hauptkomponenten

- **`Main`**
  - Einstiegspunkt (`public static void main`).
  - Initialisiert `Scanner` über `System.in`.
  - Startet eine Schleife, die jede Zeile an den `CommandProcessor` delegiert.

- **`CommandProcessor`**
  - Parst Befehle und verwaltet den globalen Programmzustand:
    - geladenes Skigebiet (`SkiArea`)
    - Fahrer-Zustand (`SkierContext`)
    - aktuelle/geplante Route (`Route`)
  - Verantwortlich für:
    - korrekte Einhaltung der Ein-/Ausgabeformate
    - Vorkonditionsprüfungen der Befehle
    - Fehlerbehandlung (Ausgabe von `Error, ...` Meldungen).

- **`SkiAreaParser`**
  - Liest die Datei im Mermaid-Format (`graph`, Knotendefinitionen, Kanten).
  - Baut `SkiArea`, `Lift`- und `Slope`-Objekte auf.
  - Führt Validierung des Skigebiets durch.

- **`SkiArea`**
  - Verwaltet:
    - `Map<String, AreaNode>`: ID → Knoten (`Lift` oder `Slope`).
    - Adjazenzliste: Knoten → Liste der Nachfolger.
  - Bietet Hilfsmethoden für:
    - Zugriff auf Knoten
    - Iteration über Lifte/Pisten
    - Graph-Validierung (Zusammenhang).

- **`AreaNode` (abstrakt oder Interface)**
  - Gemeinsame Basis für `Lift` und `Slope` mit zumindest:
    - `String getId()`.

- **`SkierContext`**
  - Enthält:
    - aktuelles `Skill`
    - aktuelles `Goal`
    - Präferenzen (`Preferences`)
    - bei laufender Route: aktuelle Zeit und Position (Knoten).

- **`Route`**
  - Repräsentiert eine geplante Route als geordnete Folge von Knoten-IDs.
  - Hält:
    - Liste der Knoten (oder Referenzen)
    - Index des nächsten Schritts
    - ggf. die Zeitpunkte pro Schritt.

- **`RoutePlanner`**
  - Kern der Routenplanung.
  - Kennt:
    - Skigebiet (`SkiArea`)
    - Fahrerzustand (`SkierContext`)
    - Zielfunktion (`Goal`)
  - Liefert:
    - optimale Route (oder sagt, dass keine existiert)
    - alternativ: neue Route, die einen bestimmten „nächsten“ Schritt vermeidet.

### 3.2 Zustandsverwaltung

Der `CommandProcessor` hält den Gesamtzustand:

- `currentArea` – aktuell geladenes Skigebiet oder `null`.
- `skierContext` – Zustand des Skifahrers.
- `currentRoute` – aktuell geplante/laufende Route oder `null`.
- Routenzustand (z. B. Enum): `NONE`, `PLANNED`, `RUNNING`, `FINISHED`.
- „Pending step“: der zuletzt von `next` vorgeschlagene Knoten für `take`/`alternative`.

---

## 4. Fachliche Logik

### 4.1 Zeitdarstellung

- Alle Zeiten werden intern als **Ganzzahl-Minuten ab 00:00** dargestellt.
- Konvertierung:
  - Eingabe `HH:MM` → `minutes = hour * 60 + minute`.
  - Umgekehrt für Ausgabe, falls benötigt (für Lifte in `list lifts`).

### 4.2 Fahrzeit auf Pisten

- Formel:
  - Sei:
    - `L` = Pistenlänge in Metern
    - `Δh` = Höhenmeter
    - `Mdifficulty` je nach Schwierigkeit
    - `Msurface` je nach Oberfläche
    - `Mskill` je nach Skill
    - `r = Δh / L`
  - Dann:
    - \\( T = ( L / 8 * Mdifficulty * Msurface * (1 + 2r) * Mskill ) \\)
    - \\( T \\) in **Sekunden**, anschließend nach **Minuten** (z. B. aufrunden).

### 4.3 Lift-Nutzbarkeit

- Ein Lift mit:
  - Startzeit `S`
  - Endzeit `E`
  - Wartezeit `W`
  - aktuelle Zeit `t`
- Ist nur erlaubt, wenn:
  - `S <= t + W <= E`
  - Gesamtdauer des Schritts ist dann `W + rideDuration`.

### 4.4 Zielfunktionen

Für eine Route `R` und die Menge der befahrenen Pisten `S(R)`:

- `ALTITUDE`:
  - \\( U_Δh(R) = Σ_{s ∈ S(R)} Δh(s) \\)
- `DISTANCE`:
  - \\( U_L(R) = Σ_{s ∈ S(R)} L(s) \\)
- `NUMBER`:
  - \\( U_#(R) = |S(R)| \\)
- `UNIQUE`:
  - \\( U_{unique}(R) = |{ s | s ∈ S(R) }| \\)

Mehrfachbefahrung einer Piste:

- Bei `ALTITUDE`, `DISTANCE`, `NUMBER`: jedes Mal zählend.
- Bei `UNIQUE`: jede Piste zählt höchstens einmal.

### 4.5 Präferenzen und Tie-Breaker

- Bei mehreren **gleichwertigen Kandidatenpfaden**:
  - Präferenzbewertung über `Preferences.score(Slope)`:
    - +1 pro positive Übereinstimmung
    - −1 pro negative Übereinstimmung
  - Auswahl der Piste mit höherem Score.
  - Bei weiterem Gleichstand: lexikografisch kleinere Pisten-ID.

---

## 5. Routenplanungs-Algorithmus

### 5.1 Grundidee

- Das Problem ist eine **Routenoptimierung in einem gerichteten Graphen mit Zeitrestriktion**.
- Erlaubt sind Zyklen, solange die Zeitbegrenzung nicht verletzt wird.

### 5.2 Zustandsraum

Ein Suchzustand umfasst:

- aktuellen Knoten (Lift oder Piste)
- aktuelle Zeit (Minuten)
- Liste der bisher befahrenen Pisten
- Menge der bisher einmal befahrenen Pisten (für `UNIQUE`)

### 5.3 Suchstrategie

- Empfohlene Basis:
  - Tiefensuche (DFS) oder best-first-Suche mit **Branch-and-Bound**.
- Vorgehen:
  1. Start an gewählter Talstation und `tstart`.
  2. Erweitere entlang aller ausgehenden Kanten, wenn:
     - Lifte zeitlich nutzbar sind.
     - Fahrzeit (Lift/Piste) die Endzeit `tend` nicht überschreitet.
  3. Jeder Zustand, der an einer Talstation bei Zeit `<= tend` endet, ist ein gültiger Routen-Kandidat.
  4. Halte während der Suche das **bisher beste Ergebnis** bzgl. Zielfunktion.
  5. Nutze einfache obere Schranken für weiteren möglichen Nutzen, um Zweige zu verwerfen, die das aktuelle Optimum nicht schlagen können.

### 5.4 Alternativ-Routing

- Für `alternative` wird eine neue Suche gestartet mit:
  - gleicher Startposition/-zeit
  - gleichem Ziel und Präferenzen
  - **zusätzlicher Nebenbedingung**, dass der aktuell vorgeschlagene „nächste“ Schritt nicht als sofortige Kante verwendet werden darf.
- Gibt es keine solche Route: Ausgabe `no alternative found`.

---

## 6. Befehle und Verhalten

### 6.1 Übersicht

- `quit`
- `load area <path>`
- `list lifts`
- `list slopes`
- `set skill <skill>`
- `set goal <goal>`
- `like <difficulty|surface>`
- `dislike <difficulty|surface>`
- `reset preferences`
- `plan <id> <time> <time>`
- `abort`
- `next`
- `take`
- `alternative`
- `show route`

### 6.2 Wichtige Regeln

- **`quit`**:
  - Programmende ohne `System.exit` – Rückkehr aus `main`.

- **`load area`**:
  - Liest Datei, gibt den Inhalt **verbatim** auf `System.out` aus.
  - Parsed und validiert Skigebiet.
  - Bei Erfolg: ersetzt vorhandenes Gebiet, bricht ggf. laufende Route ab.

- **`list lifts` / `list slopes`**:
  - Sortierung aufsteigend nach ID (String-Vergleich).

- **`set skill` / `set goal` / Präferenzbefehle**:
  - Ändern `SkierContext`.
  - Wenn Route geplant/begonnen: **dynamische Neuberechnung** der Route auf Basis aktueller Position/Zeit.

- **`plan`**:
  - Voraussetzungen:
    - Gebiet geladen
    - Skill und Goal gesetzt
    - aktuell keine laufende Route
    - Endzeit > Startzeit
    - Start-ID ist Talstation.
  - Bei Erfolg: `route planned`, `currentRoute` gesetzt.
  - Wenn alle Lifte im Zeitfenster geschlossen oder keine Route existiert: Fehlermeldung mit `Error, ...`.

- **`abort`**:
  - Bricht geplante oder laufende Route ab.
  - Entfernt Skifahrer aus dem Gebiet (kein aktueller Knoten/Zeit).

- **`next`**:
  - Gibt bei geplanter/laufender Route den nächsten Knoten aus.
  - Wenn Route beendet: `route finished!`.

- **`take`**:
  - Darf nur **direkt nach `next`** kommen.
  - Setzt Fahrer auf den vorgeschlagenen Knoten, aktualisiert Zeit, verschiebt Routenindex.

- **`alternative`**:
  - Darf nur direkt nach `next` kommen und nur, wenn die Route **mindestens gestartet** wurde (Talstation befahren).
  - Bei Erfolg: neue Route, Ausgabe `avoided <id>`.
  - Sonst: `no alternative found`.

- **`show route`**:
  - Gibt die **verbleibende** Route (ab nächstem Schritt) als Liste von IDs in einer Zeile aus.

---

## 7. Fehlerbehandlung und Formatvorgaben

- Jede Fehlermeldung:
  - beginnt mit `Error,`
  - enthält nur ASCII, keine Umlaute, keinen Zeilenumbruch innerhalb der Meldung.
- Nach jedem Fehler:
  - Programm bleibt lauffähig und wartet auf nächste Eingabe.
- Ein- und Ausgaben:
  - müssen exakt zu den Beispielen in der Aufgabenstellung passen (Groß-/Kleinschreibung, Leerzeichen, Zeilenumbrüche).
  - es dürfen **keine zusätzlichen Ausgaben** produziert werden.

---

## 8. Implementationsleitfaden (High-Level)

Diese Reihenfolge wird für die Umsetzung empfohlen:

1. **Basis-Setup**:
   - `Main` und `CommandProcessor` mit Kommando-Schleife und Platzhaltern für Befehle.
2. **Domänenklassen**:
   - `Lift`, `Slope`, `SkiArea`, Enums, `AreaNode`.
3. **Parsing & Validierung**:
   - `SkiAreaParser` und `load area`-Befehl.
4. **Fahrerzustand & Präferenzen**:
   - `SkierContext`, `Preferences`, `set skill`, `set goal`, `like`, `dislike`, `reset preferences`.
5. **Listenbefehle**:
   - `list lifts`, `list slopes`.
6. **Route & Zeitmodell**:
   - `Route`-Klasse, Statusverwaltung im `CommandProcessor`.
7. **Routenplanung**:
   - `RoutePlanner`, `plan`, inklusive Zielfunktionsberechnung.
8. **Interaktive Befehle**:
   - `next`, `take`, `show route`.
9. **Alternative & dynamische Replanung**:
   - `alternative` und Neuberechnung bei Änderungen von Skill/Goal/Preferences.
10. **Feinschliff & Tests**:
    - Testen mit Beispielinteraktionen aus dem Aufgabenblatt.
    - Checkstyle und Formatvorgaben sicherstellen.

