## Öffnungszeiten-Testszenarien

- **Einfacher Lift innerhalb des Zeitfensters**
  - Eingabegebiet mit einem Transit-Lift von 08:00 bis 10:00.
  - `plan LiftA 08:30 09:30` → Route wird geplant, da `08:30 + waitingTime` im Fenster liegt.

- **Ankunft vor Öffnung**
  - Gleiches Gebiet.
  - `plan LiftA 07:30 09:00` → Es gibt keine Route, weil `07:30 + waitingTime < 08:00` und kein anderer Lift nutzbar ist.
  - Erwartete Ausgabe: `Error, no route possible in given time window`.

- **Ankunft nach Schließung**
  - `plan LiftA 09:45 11:00` mit so großem `waitingTime`, dass `09:45 + waitingTime > 10:00`.
  - Routeplanung muss diesen Lift verwerfen; wenn kein alternativer Pfad existiert, wieder dieselbe Fehlermeldung.

- **Mehrere Lifte, nur Teil nutzbar**
  - Gebiet mit zwei Liften, von denen einer im Zeitfenster niemals nutzbar ist.
  - Testen, dass die Route ausschließlich über den nutzbaren Lift führt und niemals einen Schritt über den geschlossenen Lift enthält.

- **Komplett geschlossene Lifte im Zeitfenster**
  - Zeitfenster so wählen, dass für alle Lifte `startTime > t + waitingTime` oder `t + waitingTime > endTime` gilt.
  - Erwartung: Keine Route wird gefunden, Ausgabe `Error, no route possible in given time window`.

