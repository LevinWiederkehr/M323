import models.Album
import logic.{AlbumService, Validation}
import api.MusicBrainzClient
import storage.JsonStorage

@main def run(): Unit =
  System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))

  println("Willkommen bei AlbumoftheYear!")
  println("Lade gespeicherte Alben...")

  // IMPURE: Alben aus Datei laden
  var albums: List[Album] = JsonStorage.loadAlbums()
  println(s"${albums.length} Album(s) geladen.\n")

  var running = true
  while running do
    printMenu()
    val choice = scala.io.StdIn.readLine().trim

    choice match
      case "1" => albums = handleAddAlbum(albums)
      case "2" => handleShowAlbums(albums)
      case "3" => albums = handleRateAlbum(albums)
      case "4" => handleSearch(albums)
      case "5" => handleStats(albums)
      case "6" => albums = handleDeleteAlbum(albums)
      case "7" =>
        // IMPURE: Alben in Datei speichern
        JsonStorage.saveAlbums(albums)
        println("Gespeichert. Auf Wiedersehen!")
        running = false
      case _ =>
        println("Ungueltige Eingabe. Bitte 1–7 waehlen.\n")

def printMenu(): Unit =
  println("=" * 40)
  println("  AlbumoftheYear - Hauptmenue")
  println("=" * 40)
  println("  1  Album hinzufuegen")
  println("  2  Alle Alben anzeigen")
  println("  3  Album bewerten")
  println("  4  Album suchen")
  println("  5  Statistiken")
  println("  6  Album loeschen")
  println("  7  Speichern & Beenden")
  println("=" * 40)
  print("Auswahl: ")

def handleAddAlbum(albums: List[Album]): List[Album] =
  println("\n-- Album hinzufuegen --")
  val title = Validation.askNonEmptyInput("Albumtitel: ")
  print("Kuenstlername (optional, Enter ueberspringen): ")
  val artistInput = scala.io.StdIn.readLine().trim
  val artist = if artistInput.isEmpty then None else Some(artistInput)

  println("Suche in MusicBrainz...")
  // IMPURE: API-Anfrage
  MusicBrainzClient.searchAlbum(title, artist) match
    case Some(album) =>
      println(s"\nGefunden: ${album.title} - ${album.artist} (${album.year}, ${album.genre})")
      print("Hinzufuegen? (j/n): ")
      val confirm = scala.io.StdIn.readLine().trim.toLowerCase
      if confirm == "j" then
        val updated = AlbumService.addAlbum(albums, album)  // PURE
        println("Album hinzugefuegt!\n")
        updated
      else
        println("Abgebrochen.\n")
        albums

    case None =>
      println("Kein Ergebnis gefunden. Album manuell erfassen:")
      val manualArtist = Validation.askNonEmptyInput("Kuenstler: ")
      print("Erscheinungsjahr: ")
      val year = scala.io.StdIn.readLine().trim.toIntOption.getOrElse(0)
      val genre = Validation.askNonEmptyInput("Genre: ")
      val newAlbum = Album(
        id     = MusicBrainzClient.generateFallbackId(),
        title  = title,
        artist = manualArtist,
        year   = year,
        genre  = genre,
        rating = None
      )
      val updated = AlbumService.addAlbum(albums, newAlbum)  // PURE
      println("Album manuell hinzugefuegt!\n")
      updated

def handleShowAlbums(albums: List[Album]): Unit =
  println("\n-- Alben anzeigen --")
  println("Sortieren nach: (1) Rating  (2) Titel")
  print("Auswahl: ")
  val sort = scala.io.StdIn.readLine().trim

  // PURE: Sortierung via AlbumService
  val sorted = sort match
    case "1" => AlbumService.sortByRating(albums)
    case "2" => AlbumService.sortByTitle(albums)
    case _   => albums

  if sorted.isEmpty then
    println("Keine Alben vorhanden.\n")
  else
    println()
    sorted.zipWithIndex.foreach { (album, i) =>
      val rating = album.rating.map(r => s"$r/10").getOrElse("-")
      println(s"  ${i + 1}. ${album.title} - ${album.artist} (${album.year}) [${album.genre}] * $rating")
    }
    println()

def handleRateAlbum(albums: List[Album]): List[Album] =
  println("\n-- Album bewerten --")
  val title = Validation.askNonEmptyInput("Titel des Albums: ")

  AlbumService.findByTitle(albums, title) match
    case None =>
      println("Album nicht gefunden.\n")
      albums
    case Some(album) =>
      println(s"Album gefunden: ${album.title} - ${album.artist}")
      val rating = Validation.askValidRating()  // Rekursive Validierung
      val updated = AlbumService.rateAlbum(albums, album.id, rating)  // PURE
      println(s"Bewertet mit $rating/10\n")
      updated

def handleSearch(albums: List[Album]): Unit =
  println("\n-- Album suchen --")
  val query = Validation.askNonEmptyInput("Suchbegriff: ")

  // PURE: Rekursive Suche via AlbumService
  AlbumService.findByTitle(albums, query) match
    case None =>
      println("Kein Album gefunden.\n")
    case Some(album) =>
      val rating = album.rating.map(r => s"$r/10").getOrElse("noch nicht bewertet")
      println(s"\n  ${album.title}")
      println(s"     Kuenstler : ${album.artist}")
      println(s"     Jahr     : ${album.year}")
      println(s"     Genre    : ${album.genre}")
      println(s"     Bewertung: $rating\n")

def handleStats(albums: List[Album]): Unit =
  println("\n-- Statistiken --")

  val total   = albums.length
  val rated   = AlbumService.ratedAlbums(albums).length
  val unrated = AlbumService.unratedAlbums(albums).length
  val avg     = AlbumService.averageRating(albums)

  println(s"  Alben gesamt     : $total")
  println(s"  Davon bewertet   : $rated")
  println(s"  Noch unbewertet  : $unrated")
  avg match
    case Some(a) => println(f"  Durchschnitt     : $a%.1f / 10")
    case None    => println("  Durchschnitt     : -")

  val top3 = AlbumService.sortByRating(albums).take(3)
  if top3.nonEmpty then
    println("\n  Top 3:")
    top3.zipWithIndex.foreach { (album, i) =>
      val r = album.rating.map(r => s"$r/10").getOrElse("-")
      println(s"     ${i + 1}. ${album.title} - ${album.artist} [$r]")
    }
  println()

def handleDeleteAlbum(albums: List[Album]): List[Album] =
  println("\n-- Album loeschen --")
  val title = Validation.askNonEmptyInput("Titel des Albums: ")

  AlbumService.findByTitle(albums, title) match
    case None =>
      println("Album nicht gefunden.\n")
      albums
    case Some(album) =>
      println(s"Album gefunden: ${album.title} - ${album.artist}")
      print("Wirklich loeschen? (j/n): ")
      val confirm = scala.io.StdIn.readLine().trim.toLowerCase
      if confirm == "j" then
        val updated = AlbumService.removeAlbum(albums, album.id)  // PURE
        println("Album geloescht.\n")
        updated
      else
        println("Abgebrochen.\n")
        albums