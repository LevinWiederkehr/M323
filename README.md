# 🎵 AlbumoftheYear — Persönliche Album-Bewertungs-App

> Schulprojekt zur funktionalen Programmierung in Scala  
> Lernziele: Pure/Impure Functions, Immutable Values, Rekursion

---

## 📌 Projektbeschreibung

AlbumoftheYear ist eine Konsolenanwendung, mit der Nutzer ihre Lieblingsalben erfassen, von 1–10 bewerten und verwalten können. Albumdaten (Künstler, Erscheinungsjahr, Genre) werden automatisch über die **MusicBrainz API** abgerufen, sodass der Nutzer nur den Albumtitel eingeben muss.

Das Projekt ist bewusst iterativ aufgebaut: Phase 1 legt ein sauberes Scala-Backend mit funktionalen Prinzipien als Fundament, auf dem in späteren Phasen ein Frontend und weitere Features aufgebaut werden können.

---

## 🎯 Lernziele & Umsetzung

### Pure & Impure Functions
Reine Funktionen werden konsequent von Funktionen mit Seiteneffekten getrennt.

- **Pure:** Berechnung des Durchschnittsratings, Filtern/Sortieren der Albumliste, Validierung der Bewertungseingabe (1–10)
- **Impure:** API-Anfragen an MusicBrainz, Lesen/Schreiben der JSON-Datei, Konsoleneingabe und -ausgabe

Alle Funktionen sind im Code entsprechend kommentiert, damit der Unterschied klar erkennbar ist.

### Immutable Values
Statt bestehende Datenstrukturen zu verändern, wird bei jeder Änderung eine neue Instanz zurückgegeben.

- Alben werden als `case class` definiert (in Scala von Haus aus immutable)
- Die Albumliste ist eine unveränderliche `List[Album]` — beim Hinzufügen oder Löschen entsteht eine neue Liste
- Es gibt keine `var`-Deklarationen in der Kernlogik; nur `val`

### Rekursion
Rekursion wird an mehreren Stellen bewusst eingesetzt:

- Rekursive Eingabevalidierung: Fragt solange erneut nach, bis der Nutzer eine gültige Bewertung (1–10) eingibt
- Rekursive Berechnung des Durchschnitts über die Albumliste
- Rekursive Suche nach einem Album anhand des Titels in der Liste

---

## 🏗️ Projektstruktur

```
Albumoftheyear/
├── src/
│   └── main/
│       └── scala/
│           ├── Main.scala              # Einstiegspunkt, Hauptmenü (impure)
│           ├── models/
│           │   └── Album.scala         # Case Class: Album, Rating
│           ├── logic/
│           │   ├── AlbumService.scala  # Pure Functions: Filter, Sort, Stats
│           │   └── Validation.scala    # Pure Functions: Eingabevalidierung
│           ├── api/
│           │   └── MusicBrainzClient.scala  # Impure: HTTP-Anfragen
│           └── storage/
│               └── JsonStorage.scala   # Impure: Lesen/Schreiben JSON
├── data/
│   └── albums.json                     # Lokale Datenspeicherung
├── build.sbt
└── README.md
```

---

## 🔧 Technologien (Phase 1)

| Komponente | Technologie |
|---|---|
| Sprache | Scala 3 |
| Build-Tool | sbt |
| API | MusicBrainz (kostenlos, kein API-Key nötig) |
| HTTP-Client | sttp oder requests-scala |
| JSON | ujson oder circe |
| Datenspeicherung | Lokale JSON-Datei |

---

## ✨ Features — Phase 1 (Backend / Konsole)

- [ ] Album hinzufügen (Titel eingeben → Daten automatisch via MusicBrainz laden)
- [ ] Album manuell bewerten (1–10)
- [ ] Alle Alben anzeigen (sortiert nach Rating oder Alphabetisch)
- [ ] Alben nach Künstler oder Genre filtern
- [ ] Durchschnittsbewertung aller Alben berechnen
- [ ] Album suchen (nach Titel)
- [ ] Album löschen
- [ ] Daten in JSON-Datei speichern und beim Start laden

---

## 🚀 Geplante Erweiterungen (Phase 2+)

### Frontend
Mögliche Ansätze, die noch evaluiert werden:
- **Web-Frontend** mit React/TypeScript, das gegen das Scala-Backend als REST-API kommuniziert
- **Scala-native UI** mit einem Framework wie Scala.js oder http4s + Play

### Weitere Features
- Eigene Playlisten / Sammlungen erstellen
- Empfehlungen basierend auf Genres der Top-bewerteten Alben
- Statistiken (meistgehörte Genres, Bewertungsverteilung)
- Nutzerkonten / mehrere Profile

---

## 📚 Datenmodell

```scala
// Pure, immutable Datenstruktur
case class Album(
  id: String,           // MusicBrainz ID
  title: String,
  artist: String,
  year: Int,
  genre: String,
  rating: Option[Int]   // None = noch nicht bewertet; Some(1-10)
)
```

---

## 🔌 MusicBrainz API

Die [MusicBrainz API](https://musicbrainz.org/doc/MusicBrainz_API) ist eine offene, kostenlose Musikdatenbank.

- Keine Registrierung oder API-Key für Basisanfragen nötig
- Liefert: Albumtitel, Künstler, Erscheinungsjahr, Genre (Tags), Cover-Art
- Beispielanfrage: `https://musicbrainz.org/ws/2/release/?query=album:Nevermind+artist:Nirvana&fmt=json`

---

## 📝 Hinweise zum Projektaufbau

Dieses Projekt ist als iterativer Prozess geplant. Phase 1 (dieses Dokument) fokussiert sich auf ein funktionierendes, sauber strukturiertes Backend, das alle Lernziele der Prüfung abdeckt. Erweiterungen werden schrittweise hinzugefügt, sobald die Basis stabil ist.
