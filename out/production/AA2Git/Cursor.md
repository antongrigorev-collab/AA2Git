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

---

## 9. Coding Style Guidelines

- All comments in the source code (including Javadoc and inline comments) **must be written in English**.
- Use clear and concise wording and avoid mixing German and English within the same comment.

### 9.1 Comments and documentation

- **Only comment what is non-obvious**  
  - Do not add comments that simply repeat what the code already says in plain language.  
  - Do not add personal remarks, complaints (e.g. about Checkstyle), or other information that is irrelevant for understanding the code.  
  - A good rule of thumb: comments should explain only those places that an experienced developer cannot understand at a first glance.

- **No commented-out code in committed / final versions**  
  - Temporary debug prints or test helpers are fine while developing, but must be **deleted**, not just commented out, once they are no longer needed.  
  - In submitted or “finished” code there must be no old, commented-out code blocks.

- **TODO / FIXME usage**  
  - `// TODO` and `// FIXME` comments may be used to mark work-in-progress during development.  
  - Before handing in / marking the program as finished, all TODO/FIXME comments must either be resolved (code implemented/fixed) or removed.  

- **General cleanliness**  
  - Final code must be **readable, tidy and consistent**.  
  - Avoid redundant comments and dead or unused code.

### 9.2 Constants

- **Name constants after the concept, not the value**  
  - Constant names must describe the *role* or *concept* they represent, not their concrete value.  
  - Bad: `public static final String SPACE = " ";` (names the value).  
  - Good: `public static final String COMMAND_KEYWORD_SEPARATOR = " ";` (names the concept).  
  - It is fine if multiple constants share the same value, as long as they model different concepts and therefore have different, meaningful names.

- **No magic numbers / inline literals**  
  - All numbers and string literals that appear in the source code (except `0`, `1`, `-1` in obvious arithmetic contexts and empty string in tests) should be declared as `public static final` (or at least `private static final`) constants at the top of the class.  
  - This also applies to UI texts, error message prefixes, separators, default values, timeouts, etc.  
  - The same literal must not be copy-pasted in multiple places; instead, a single well-named constant should be reused.
  - A “magic literal” is any value (number or string) that is embedded directly in the code instead of being defined as a named constant, even if it currently appears only once. Assigning the literal to a `static final` field with a meaningful name is enough to avoid it being “magic”.  
  - The only accepted exception is the literal `0` as the start value in a standard for-loop pattern like `for (int i = 0; i < ...; i++)`. All other fixed numbers and strings that influence logic, behaviour or output must be extracted into constants.  
  - The same rule applies to “magic strings”: user-visible messages, error texts, keywords, and other fixed strings must not be inlined in multiple places, but declared as well-named constants.

- **No “constants holder” classes**  
  - Do **not** introduce dedicated “constants classes” whose only (or primary) purpose is to group unrelated constants (e.g. a class `Constants` that contains UI texts, parsing messages and algorithm limits all together).  
  - Constants must be declared **where they conceptually belong** – typically in the class that uses them (e.g. matrix limits in `Matrix`, UI button texts in a UI class, exception messages in the corresponding exception type).  
  - If a constant is used by several closely related classes, a better place is a common abstraction that already has domain meaning (e.g. a shared superclass or interface) instead of a generic constants container.  
  - A telltale sign of an unwanted constants class is that it does not model any concrete domain concern, but is only a bag of values that many other classes reach into across package or class boundaries.

### 9.3 Safe casting (equals only)

- **Casts are only allowed as part of the `equals` mechanism.**  
  - In normal code paths (outside of `equals`) do not downcast from a general type (e.g. `Object`, interface, superclass) to a specific implementation type.  
  - Prefer polymorphism, interfaces, and proper method signatures instead of casts.

- **Guarded cast in `equals`**  
  - If a cast is required in `equals`, it must be guarded by type checks before the cast is performed. A typical pattern is:  
    - return `false` if the argument is `null`;  
    - return `false` if `object.getClass() != this.getClass()`;  
    - only then perform the cast.  
  - This avoids `ClassCastException` at runtime and makes the cast logically safe.

- **Avoid using `Object` as a general-purpose type**  
  - Types should be chosen **as general as necessary, but as specific as possible**. Declaring fields, parameters or return types as `Object` is almost always too general, because no meaningful domain methods can be called without casting.  
  - Using `Object` for domain data typically leads to repeated casts and unsafe code; instead, model the domain properly with concrete types, interfaces or Java generics.  
  - If you need a generic container (e.g. a pair), prefer a generic type such as `Pair<T>` (or `Pair<L, R>`) over a raw `Object`-based implementation. This keeps type safety and avoids casting at call sites.

### 9.4 Enums

- **Use enums for closed sets of concepts**  
  - Whenever a value comes from a small, closed set (e.g. months, colors, days of week, seasons, roles, states, categories, modes), model it as an `enum` instead of scattered constants or string values.  
  - Enums should be the single, central type for such concepts; avoid separate “constants classes” for the same domain.

- **Enum design and conventions**  
  - Enum constants are written in `UPPER_SNAKE_CASE` (e.g. `WINTER`, `BEGINNER`, `ALTITUDE`).  
  - Enum instances are immutable: any additional fields must be `final`, and there must be no public setters.  
  - If an enum is only used inside a single class, declare it as a nested enum with the minimal necessary visibility (prefer `private`); otherwise, put it in its own file.

- **Do not abuse enums for logic branching**  
  - Avoid large `switch` statements on an enum to control complex business logic or behaviour of other classes. Prefer polymorphism and proper class hierarchies instead.  
  - Enums may carry data and simple helper methods, but rich domain behaviour should live in dedicated classes.

### 9.5 Polymorphism and `instanceof`

- **Prefer polymorphism over explicit type checks**  
  - When behaviour depends on the concrete kind of an object, model this through inheritance, interfaces and overriding methods, **not** through chains of `instanceof` checks and downcasts.  
  - A sign of missing polymorphism is a method that takes a very general type (e.g. `Object` or a broad interface) and then uses multiple `instanceof` branches to distinguish specific implementations.

- **Use abstract base classes or interfaces for shared behaviour**  
  - If several classes share a common concept (e.g. different kinds of routes, commands, or area nodes), introduce an abstract superclass or interface that declares the common operations.  
  - Each concrete subclass implements the behaviour in its own way; client code only depends on the abstraction (e.g. calls `animal.speak()` instead of checking whether it is a `Cat` or `Dog`).  
  - This keeps code extensible: neue Varianten erfordern nur neue Implementierungen, nicht das Anpassen zentraler `if/else`-Ketten.

- **Limit `instanceof` to rare, well-justified cases**  
  - Occasional `instanceof` checks can be acceptable at system boundaries (e.g. reflection, deserialization, legacy APIs), but they must not be the dominant mechanism for domain logic.  
  - In normal application code, repeated `instanceof` branches are a strong indication that the design should be refactored towards proper polymorphism.

### 9.6 Fehlermeldungen und Ausgaben im Fehlerfall

- **Fehler klar als solche kennzeichnen**  
  - Jede Fehlermeldung muss bereits am Anfang eindeutig als Fehler erkennbar sein (in diesem Projekt: Präfix `Error,`).  
  - Vage oder umgangssprachliche Ausgaben wie `No` oder `I don't like that` sind nicht zulässig, weil sie weder den Fehler eindeutig kennzeichnen noch dessen Ursache vermitteln.

- **Fehlerursache konkret beschreiben**  
  - Die Meldung muss inhaltlich erklären, *was genau* schiefgelaufen ist (z. B. „ID nicht gefunden“, „Zeitpunkt außerhalb des gültigen Bereichs“, „Piste nicht befahrbar im aktuellen Zeitfenster“).  
  - Der Text soll der benutzenden Person helfen zu verstehen, *warum* die Eingabe nicht akzeptiert oder eine Aktion nicht ausgeführt wurde.

- **Konsistenter Stil und Format**  
  - Halten Sie sich strikt an die in diesem Projekt vorgegebene Formatierung der Fehlermeldungen (z. B. exakter Präfix, erlaubte Zeichen, keine zusätzlichen Leerzeilen).  
  - Formulierungen müssen sachlich, klar und neutral sein; Scherze, persönliche Kommentare oder emotional gefärbte Sprache gehören nicht in Fehlermeldungen.

### 9.7 Verwendung von `final`

- **Felder nach Möglichkeit `final` machen**  
  - Attribute/Felder sollen so weit wie möglich als `final` deklariert werden, insbesondere für alle Bestandteile des Objektzustands, die nach der Initialisierung nicht mehr auf ein anderes Objekt zeigen sollen.  
  - Ziel ist, den veränderlichen Zustand (Mutability) eines Objektes so klein wie möglich zu halten und unbeabsichtigte Änderungen zu verhindern.

- **`final` bezieht sich auf die Referenz, nicht auf das Objekt**  
  - Bei Referenztypen (z. B. `List`, `Map`) verhindert `final`, dass die Variable auf ein anderes Objekt zeigt, **nicht** dass der Inhalt des Objekts verändert wird.  
  - Beispiel: Eine `final List<Employee> employees` darf weiter Elemente hinzufügen/entfernen (`employees.add(...)`), aber nicht durch eine neue Liste ersetzt werden.  
  - Methoden wie `clear()` sollen daher den Inhalt leeren (z. B. `employees.clear()`), nicht eine neue Liste zuweisen.

- **Unsichtbare Abhängigkeiten durch veränderlichen Zustand vermeiden**  
  - Der interne Zustand eines Objektes ist eine versteckte Abhängigkeit; je mehr Felder nachträglich verändert werden können, desto schwieriger wird es, Fehler und Randfälle zu erkennen.  
  - Durch konsequenten Einsatz von `final` bei Feldern wird klar, welche Teile des Zustands nach der Konstruktion stabil bleiben und welche bewusst veränderlich sind.

- **Methodenparameter und lokale Variablen**  
  - Für Methodenparameter ist `final` in diesem Projekt **nicht erforderlich** und sollte in der Regel nicht verwendet werden, um die Signaturen lesbar zu halten.  
  - Lokale Variablen müssen nicht standardmäßig `final` sein; der Einsatz von `final` ist primär für Felder (Attribute) und Konstanten gedacht.  
  - Konstante Werte (z. B. fixe Gebühren, Präfixe, Standardwerte) sollen als `static final` Konstanten definiert werden (siehe auch Abschnitt 9.2 zu Konstanten).

### 9.8 Trennung von Benutzerinteraktion und Programmlogik

- **Keine Ein-/Ausgabe in Logik-/Modellklassen**  
  - Klassen, die die eigentliche Programmlogik oder das Domänenmodell repräsentieren (z. B. `RoutePlanner`, `SkiArea`, `Route`, `Preferences`, `SkierContext`), dürfen **weder Eingaben lesen noch Ausgaben erzeugen**.  
  - Insbesondere sind `System.out.println`, `System.err.println` oder das direkte Arbeiten mit `Scanner` in solchen Klassen nicht erlaubt.  
  - Diese Klassen stellen eine **öffentliche, wohldefinierte API** (Methoden) bereit, über die der Rest des Programms mit ihnen interagiert.

- **Benutzerinteraktion auf zentrale Klassen beschränken**  
  - Klassen für Benutzerinteraktion (in diesem Projekt vor allem `Main` und `CommandProcessor`) sind dafür verantwortlich, Eingaben zu lesen und Ausgaben zu schreiben.  
  - Sie nutzen ausschließlich die öffentliche Schnittstelle der Logik-/Modellklassen, um den Programmablauf zu steuern.  
  - Die Syntax der Eingaben (z. B. „ist dies ein Integer?“, „passt das Eingabeformat?“) wird in diesen UI-nahen Klassen geprüft; die Semantik (z. B. „ist die ID existent?“, „ist die Zeit im gültigen Bereich?“) wird in der Logik geprüft.

- **Klare Verantwortlichkeiten und Wiederverwendbarkeit**  
  - Die Trennung stellt sicher, dass Logik-/Modellklassen in verschiedenen Kontexten wiederverwendet werden können (z. B. andere UI, Tests), ohne an eine konkrete Ein-/Ausgabeform (Terminal) gebunden zu sein.  
  - Eine Modellklasse darf niemals die einzige Stelle sein, an der ein bestimmter Zustand „sichtbar“ wird (z. B. nur über `System.out.println` im Modell). Stattdessen muss der Zustand über geeignete Getter/Methoden abgefragt werden, damit unterschiedliche UIs ihn präsentieren können.

- **Debugging-Ausgaben sind keine finale Schnittstelle**  
  - Temporäre Debug-Ausgaben (z. B. `System.out.println` innerhalb der Logik) können während der Entwicklung hilfreich sein, müssen aber vor Abgabe/Finalisierung entfernt werden (siehe auch Abschnitt 9.1 zu kommentiertem Code und Sauberkeit).  
  - Dauerhafte, für Benutzende bestimmte Ausgaben gehören ausschließlich in die dafür vorgesehenen UI-/Command-Klassen und folgen dort den definierten Formatvorgaben für Meldungen und Fehler.

### 9.9 Javadoc-Kommentare

- **Zweck von Javadoc**  
  - Javadoc-Kommentare dokumentieren die *Verwendung* von Klassen, Methoden, Konstruktoren und Feldern – nicht deren Implementierungsdetails.  
  - Sie sollen beschreiben, was eine Einheit tut, welche Parameter und Rückgabewerte sie hat und unter welchen Bedingungen Fehler (Exceptions) auftreten.

- **Grundformat und Aufbau**  
  - Ein Javadoc-Kommentar beginnt mit `/**` und endet mit `*/`; jede Zeile beginnt idealerweise mit `*`.  
  - Der Kommentar besteht aus genau **einem Beschreibungsblock** (kurze Zusammenfassung + ggf. ausführlichere Beschreibung) und anschließend einem Block von **Javadoc-Tags** (`@param`, `@return`, `@throws`, …).  
  - Zwischen Beschreibung und Tag-Block steht eine **Leerzeile**. Nach den Tags folgt keine weitere Beschreibung.  
  - Die erste Zeile der Beschreibung ist ein vollständiger Satz, der kurz, aber vollständig erklärt, was die Einheit tut; dieser Satz wird u. a. in Übersichtsseiten wiederverwendet.

- **Wichtige Tags und Reihenfolge**  
  - Häufig verwendete Tags sind insbesondere:  
    - `@author` – Autor:in der Klasse bzw. des Members. Übernommener Code (z. B. aus dem Programmier-Team) wird durch einen zusätzlichen `@author`-Eintrag gekennzeichnet.  
    - `@param` – Beschreibung eines Parameters (Bedeutung, zulässige Werte, Besonderheiten).  
    - `@return` – Beschreibung des Rückgabewertes (Bedeutung und mögliche Wertebereiche).  
    - `@throws` – beschreibt, welche Exception in welchen Fällen geworfen wird (statt des veralteten `@exception`).  
    - `@see` – Verweise auf verwandte Klassen, Methoden oder externe Dokumentation.  
  - Die Tags sollen in einer konsistenten Reihenfolge stehen, orientiert an der Oracle-Empfehlung (z. B. `@author`, `@version`, `@param`, `@return`, `@throws`, `@see`, …).

- **Stilrichtlinien**  
  - Beschreibungen werden in der **3. Person** formuliert und Methodenbeschreibungen beginnen mit einem Verb (z. B. „Calculates…“, „Returns…“, „Simulates…“).  
  - Javadoc-Texte sollen **implementierungsunabhängig** sein: interne Details nur erwähnen, wenn sie für die Nutzung zwingend relevant sind.  
  - Inline-Links (`{@link ...}`) und HTML-Tags (z. B. `<p>`) dürfen verwendet werden, aber sparsam und zielgerichtet – nur die wichtigsten Begriffe/erste Vorkommen verlinken, nicht jeden Treffer.  
  - Zeilen sollten sinnvoll umbrochen werden (Richtwert: ca. 80 Zeichen), damit sie gut lesbar bleiben.

- **Wo Javadoc verwendet wird**  
  - Alle Elemente mit Sichtbarkeit **größer als `private`** (öffentliche Klassen, `public`/`protected`/`package-private` Methoden und Felder) sollen bei Bedarf mit Javadoc dokumentiert werden, insbesondere öffentlich nutzbare APIs.  
  - Für komplexe `private`-Methoden kann Javadoc ebenfalls sinnvoll sein, ist aber nicht verpflichtend; hier können auch reguläre Kommentare verwendet werden.  
  - Bei Überschreibungen/Implementierungen (z. B. Methoden aus Superklassen oder Interfaces) wird die Dokumentation in vielen Fällen automatisch „mitvererbt“; Javadoc muss dort nur ergänzt werden, wenn zusätzliche oder abweichende Informationen notwendig sind.

### 9.10 Use the Java standard library instead of reimplementing basics

- **Prefer existing JDK data structures and algorithms**  
  - Before implementing your own collections, sorting algorithms or utility classes, always check whether the Java standard library (e.g. `java.util` and related packages) already provides the needed functionality. In most cases it does.  
  - Standard APIs are well-tested, widely understood and significantly less error-prone than ad-hoc implementations of common patterns such as sorting, searching, stacks, queues or maps.

- **Avoid custom implementations of common algorithms without a strong, explicit reason**  
  - Implementations of basic algorithms like bubble sort, selection sort, manual array search, custom stacks/queues, etc. should **not** be introduced in the project when equivalent methods like `Arrays.sort`, `Collections.sort`, or the JDK collection types and utilities are available.  
  - A typical **negative example** is implementing your own `bubbleSort(int[])` method and calling it from `main` instead of using `Arrays.sort(int[])`.  
  - Custom low-level algorithms are only justified if the task explicitly requires them (e.g. for learning purposes or performance experiments); in that case they must be clearly separated from the production code and well documented.

- **Avoid parallel “alternative” APIs that mirror the JDK**  
  - Do not introduce types or methods that replicate existing JDK concepts under different names (e.g. your own `MyList`, custom sort helpers, or utility classes that merely wrap existing `java.util` functionality).  
  - Multiple, slightly different variants of the same concept make the overall architecture unstable and confusing, because readers cannot immediately see what is standard behaviour and what is project-specific.

- **Keep the codebase consistent and recognizable**  
  - Using the JDK’s well-known types and utilities makes the code easier to read for others: readers immediately know what `List`, `Map`, `Arrays.sort` or `Collections` do, instead of having to learn your own variants.  
  - Prefer “using the library correctly” over “reinventing building blocks”; this reduces the amount of code to maintain and lowers the risk of subtle bugs.

### 9.11 Wahl des passenden Schleifentyps

- **Schleifentyp nach Einsatzzweck wählen**  
  - Verwende eine `while`-Schleife, wenn es im Wesentlichen nur eine boolesche Bedingung gibt und keine klar strukturierte Vor- und Nachbereitung nötig ist.  
  - Verwende eine `do-while`-Schleife, wenn mindestens ein Durchlauf garantiert stattfinden muss (z. B. Menü, das mindestens einmal angezeigt wird).  
  - Verwende eine klassische `for`-Schleife, wenn eine Zählvariable bzw. ein Index mit klarer Initialisierung, Abbruchbedingung und Inkrementierung benötigt wird.  
  - Wenn ein Array oder eine `Collection` vollständig durchlaufen werden soll **ohne** dass der Index explizit benötigt wird, ist ein „enhanced for loop“ (`for-each`) zu verwenden.

- **Negativbeispiel – unnötige Index-Schleife**  
  - Schleifen, die nur über Indizes iterieren, um anschließend sofort das Element an dieser Position zu verwenden, sind zu vermeiden:

```java
Person[] people = new Person[10];
// We imagine this array is filled after this point
for (int i = 0; i < people.length; i++) {
    System.out.print(people[i].getName());
}
```

- **Positivbeispiel – `for-each` bei vollständiger Iteration**  
  - In solchen Fällen ist ein `for-each` vorzuziehen, da direkt über die Objekte iteriert wird, der Code kompakter ist und sich besser lesen lässt:

```java
Person[] people = new Person[10];
// We imagine this array is filled after this point
for (Person person : people) {
    System.out.print(person.getName());
}
```

### 9.12 Exceptions nicht für Kontrollfluss verwenden

- **Exceptions sind nicht für normalen Kontrollfluss gedacht**  
  - Exceptions signalisieren **außergewöhnliche Fehlerzustände** und sollen nicht zur Steuerung des normalen Programmablaufs benutzt werden.  
  - Insbesondere dürfen Exceptions nicht absichtlich innerhalb eines `try`-Blocks geworfen werden, nur um denselben Block durch einen dazugehörigen `catch` frühzeitig zu verlassen oder einfache Eingabevalidierungen zu ersetzen.

- **Guard-Clauses statt „kontrollierender“ Exceptions**  
  - Verwende **Guard-Clauses** (frühe Rückgaben) für einfache Vorbedingungsprüfungen, anstatt eine Exception zu werfen und direkt wieder zu fangen.  
  - Typische Anwendung: Eingaben validieren, ungültige Zustände früh erkennen und durch `return` abbrechen, bevor komplexere Logik ausgeführt wird.

- **Negativbeispiel – Exception als Kontrollfluss**  
  - Hier wird eine `IllegalArgumentException` absichtlich geworfen, nur um sie direkt wieder zu fangen und damit den Kontrollfluss zu steuern:

```java
public class Example {
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("Expected exactly one argument.");
            }

            System.out.printf("First Argument: %s%n", args[0]);
        } catch (IllegalArgumentException exception) {
            System.err.println("Error: " + exception.getMessage());
        }
    }
}
```

- **Positivbeispiel – Guard-Clauses und gezieltes Exception-Handling**  
  - Hier werden einfache Vorbedingungen ohne Exceptions überprüft; Exceptions werden nur dort verwendet, wo sie tatsächlich aus einer API (z. B. `Integer.parseInt`) herausfallen:

```java
public class Example {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.printf("Expected one argument. Got %d%n", args.length);
            return;
        }

        int number;
        try {
            number = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            System.err.println("Error: " + exception.getMessage());
            return;
        }

        System.out.printf("Parsed number: %d%n", number);
    }
}
```

### 9.13 Sinnvoller Zuschnitt von `try-catch`-Blöcken

- **`try-catch` nur um die tatsächlich kritische Stelle legen**  
  - Ein `try-catch`-Block soll **nur** die kleinste Code-Stelle umschließen, an der die betreffende Exception tatsächlich auftreten kann (typischerweise eine oder wenige Zeilen).  
  - Je größer der `try`-Block, desto schwerer ist nachvollziehbar, **welche** Zeile die Exception ausgelöst hat und welche Teile des Codes im Fehlerfall übersprungen wurden.

- **Negativbeispiel – zu großer `try`-Block**  
  - Hier werden Lesen, Parsen, Spiellogik und Ausgabe gemeinsam in einen `try`-Block gepackt, obwohl nur das Parsen die `NumberFormatException` wirft:

```java
try {
    String line = scanner.nextLine();
    int number = Integer.parseInt(line);
    boolean valid = game.doMove(number);
    if (!valid) {
        System.out.println("That move was not valid.");
    }
} catch (NumberFormatException exception) {
    System.out.println("The input was not a valid number.");
}
```

- **Positivbeispiel – schmaler `try`-Block**  
  - Im positiven Beispiel umfasst der `try`-Block nur den tatsächlichen Fehlerkandidaten (`Integer.parseInt`); die restliche Logik bleibt außerhalb und ist dadurch klarer strukturiert:

```java
String line = scanner.nextLine();
int number;
try {
    number = Integer.parseInt(line);
} catch (NumberFormatException exception) {
    System.out.println("The input was not a valid number.");
    return;
}

boolean valid = game.doMove(number);
if (!valid) {
    System.out.println("That move was not valid.");
}
```

### 9.14 Programming to an Interface

- **Immer gegen Schnittstellen programmieren, nicht gegen Implementierungen**  
  - Für Variablen-, Parameter- und Rückgabetypen soll nach Möglichkeit ein **Interface** (oder eine abstrakte Oberklasse) verwendet werden, nicht die konkrete Implementierungsklasse.  
  - Beispiel bei Collections: lieber `List<String>` als Typ verwenden statt `ArrayList<String>` oder `LinkedList<String>` in der Signatur.

- **Vorteile: bessere Austauschbarkeit und Wartbarkeit**  
  - Nutzer des Codes müssen nur wissen, **dass** eine Liste (`List`) verwendet wird, nicht **welche** konkrete Implementierung.  
  - Die konkrete Datenstruktur kann später intern gewechselt werden (z. B. von `ArrayList` zu `LinkedList`), ohne dass alle Aufrufstellen angepasst werden müssen, solange das Interface (`List`) gleich bleibt.

- **Negativbeispiel – an Implementierungstyp binden**  
  - Hier ist der Typ sowohl links als auch rechts eine konkrete Implementierung; ein späterer Wechsel erzeugt direkt Kompilierfehler:

```java
ArrayList<String> list = new ArrayList<>();
// I do not like this, let's change it
ArrayList<String> list = new LinkedList<>(); // compile error: incompatible types
```

- **Positivbeispiel – gegen das Interface programmieren**  
  - Durch die Verwendung von `List` als Typ bleibt der Code flexibel, auch wenn die konkrete Implementierung gewechselt wird:

```java
List<String> list = new ArrayList<>();
// Later we decide to change the implementation
List<String> list = new LinkedList<>(); // still compiles, interface stays the same
```

- **Faustregel**  
  - Verwende Interfaces oder abstrakte Typen überall dort, wo Aufrufende keine Kenntnis über die konkrete Implementierung benötigen.  
  - Konkrete Klassen sind hauptsächlich bei der **Erzeugung** (`new ArrayList<>()`) und in Implementierungsdetails innerhalb einer Klasse relevant, nicht als öffentlich sichtbare Typen in der API.

### 9.15 Umgang mit `static`

- **Statische Methoden – wenn kein Objektzustand nötig ist**  
  - Statische Methoden werden auf der Klasse und nicht auf einer Instanz aufgerufen. Sie haben **keinen Zugriff auf Instanzfelder** der Klasse.  
  - Verwende eine Methode nur dann als `static`, wenn ihre Funktionalität **nicht vom Zustand eines konkreten Objekts** abhängt (d. h. sie könnte sinnvoll auch in einer reinen Hilfsklasse stehen).  
  - Typische Anwendungsfälle: Utility-/Helper-Methoden (z. B. einfache Berechnungen oder Konvertierungen), die ausschließlich mit ihren Parametern arbeiten.  
  - Für `private`-Methoden gilt: deklariere sie nur dann als `static`, wenn sie von anderen `static`-Methoden derselben Klasse aufgerufen werden sollen.

- **Statische Attribute – globale Zustände vermeiden**  
  - Ein `static`-Attribut gehört zur **Klasse als Ganzes**; alle Instanzen teilen sich denselben Wert. Änderungen durch ein Objekt sind für alle anderen Objekte sichtbar.  
  - Solche geteilten, veränderlichen Zustände führen schnell zu **schwer nachvollziehbaren Abhängigkeiten** zwischen Objekten und erschweren Tests und Fehleranalyse.  
  - Deshalb sollen **veränderliche** statische Attribute (ohne `final`) im Regelfall vermieden werden. Objektzustand gehört in Instanzfelder, nicht in `static`-Felder.

- **Ausnahme: Konstanten als `static final`**  
  - Konstanten sollen (wie in Abschnitt 9.2 beschrieben) als `static final` deklariert werden:  
    - `static`, damit alle Instanzen der Klasse denselben, gemeinsam genutzten Wert sehen,  
    - `final`, damit dieser Wert nach der Initialisierung **nicht mehr verändert** werden kann.  
  - Beispiele sind feste Präfixe für Fehlermeldungen, Separatoren, Standardlimits, etc. – alle mit **sprechenden, konzeptbezogenen Namen**.

- **Negativbeispiel – versteckte Interaktion über `static`-Zustand**  

```java
public class Receipt {

    private static int idCounter = 0;

    private final int id;
    private final List<Product> products;

    public Receipt(List<Product> products) {
        // All Receipt instances implicitly interact by modifying the shared idCounter
        this.id = idCounter++;
        this.products = products;
    }

    // getTotalPrice conceptually belongs to the object state, but is declared static
    private static double getTotalPrice(List<Product> products) {
        double total = 0.0;

        for (Product product : products) {
            total += product.getPrice();
        }

        return total;
    }
}
```

- **Positivbeispiel – Objektzustand und Erzeugungslogik trennen**  

```java
public class Receipt {

    private final int id;
    private final List<Product> products;

    public Receipt(int id, List<Product> products) {
        // The id counter is managed elsewhere (e.g. in a repository or service)
        this.id = id;
        this.products = products;
    }

    private double getTotalPrice() {
        double total = 0.0;

        for (Product product : this.products) {
            total += product.getPrice();
        }

        return total;
    }
}
```

- **Merksatz**  
  - Verwende `static` bewusst und sparsam:  
    - **Methoden** nur dann `static`, wenn sie keinen Objektzustand benötigen.  
    - **Attribute** nur als `static final`-Konstanten oder in wenigen, gut begründeten Fällen als globaler, klar dokumentierter Zustand.

