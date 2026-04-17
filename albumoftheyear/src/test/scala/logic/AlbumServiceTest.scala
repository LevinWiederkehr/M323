import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import models.Album
import logic.AlbumService

class AlbumServiceTest extends AnyFunSuite with Matchers:

  // Testdaten — wiederverwendbar in allen Tests
  val album1: Album = Album("1", "Nevermind",          "Nirvana",       1991, "Grunge",    Some(9))
  val album2: Album = Album("2", "OK Computer",        "Radiohead",     1997, "Art Rock",  Some(10))
  val album3: Album = Album("3", "Thriller",           "Michael Jackson",1982, "Pop",      Some(7))
  val album4: Album = Album("4", "In Rainbows",        "Radiohead",     2007, "Art Rock",  None)
  val album5: Album = Album("5", "The Dark Side of the Moon", "Pink Floyd", 1973, "Rock",  Some(8))

  val albums: List[Album] = List(album1, album2, album3, album4, album5)

  // addAlbum
  test("addAlbum fügt ein Album zur Liste hinzu"):
    // Arrange
    val newAlbum = Album("6", "Abbey Road", "The Beatles", 1969, "Rock", None)
    // Act
    val result = AlbumService.addAlbum(albums, newAlbum)
    // Assert
    result should have length 6
    result.last shouldBe newAlbum

  test("addAlbum verändert die originale Liste nicht (immutable)"):
    val newAlbum = Album("6", "Abbey Road", "The Beatles", 1969, "Rock", None)
    AlbumService.addAlbum(albums, newAlbum)
    // Original muss unverändert sein
    albums should have length 5

  // removeAlbum
  test("removeAlbum entfernt das richtige Album anhand der ID"):
    val result = AlbumService.removeAlbum(albums, "1")
    result should have length 4
    result.exists(_.id == "1") shouldBe false

  test("removeAlbum bei nicht-existierender ID ändert nichts"):
    val result = AlbumService.removeAlbum(albums, "999")
    result should have length 5

  // rateAlbum
  test("rateAlbum setzt das Rating des richtigen Albums"):
    val result = AlbumService.rateAlbum(albums, "4", 8)
    val rated  = result.find(_.id == "4").get
    rated.rating shouldBe Some(8)

  test("rateAlbum verändert andere Alben nicht"):
    val result = AlbumService.rateAlbum(albums, "4", 8)
    result.find(_.id == "1").get.rating shouldBe Some(9)  // Unverändert
    result.find(_.id == "2").get.rating shouldBe Some(10) // Unverändert

  test("rateAlbum verändert das originale Album-Objekt nicht (immutable)"):
    AlbumService.rateAlbum(albums, "4", 8)
    album4.rating shouldBe None  // Original unverändert

  // findByTitle — REKURSION
  test("findByTitle findet ein Album anhand eines Teilstrings"):
    val result = AlbumService.findByTitle(albums, "never")
    result shouldBe Some(album1)

  test("findByTitle ist case-insensitive"):
    val result = AlbumService.findByTitle(albums, "THRILLER")
    result shouldBe Some(album3)

  test("findByTitle gibt None zurück wenn nichts gefunden"):
    val result = AlbumService.findByTitle(albums, "xyz_existiert_nicht")
    result shouldBe None

  test("findByTitle auf leerer Liste gibt None zurück"):
    val result = AlbumService.findByTitle(List.empty, "Nevermind")
    result shouldBe None

  // averageRating — REKURSION
  test("averageRating berechnet den Durchschnitt korrekt"):
    // album1=9, album2=10, album3=7, album5=8 → (9+10+7+8)/4 = 8.5
    val result = AlbumService.averageRating(albums)
    result shouldBe Some(8.5)

  test("averageRating ignoriert Alben ohne Rating"):
    // album4 hat kein Rating → darf den Durchschnitt nicht beeinflussen
    val result = AlbumService.averageRating(albums)
    result should not be Some(0.0)

  test("averageRating gibt None zurück wenn keine Alben bewertet sind"):
    val unratedOnly = List(album4)
    val result      = AlbumService.averageRating(unratedOnly)
    result shouldBe None

  test("averageRating auf leerer Liste gibt None zurück"):
    val result = AlbumService.averageRating(List.empty)
    result shouldBe None

  // filterAlbums — HIGHER ORDER FUNCTION
  test("filterAlbums mit Predicate filtert korrekt"):
    val result = AlbumService.filterAlbums(albums, _.artist == "Radiohead")
    result should have length 2
    result.forall(_.artist == "Radiohead") shouldBe true

  test("filterAlbums mit kombiniertem Predicate funktioniert"):
    val result = AlbumService.filterAlbums(albums, a => a.year > 1990 && a.rating.exists(_ >= 9))
    result should have length 2
    result.map(_.title) should contain allOf ("Nevermind", "OK Computer")

  test("filterAlbums gibt leere Liste zurück wenn nichts passt"):
    val result = AlbumService.filterAlbums(albums, _.genre == "Jazz")
    result shouldBe empty

  // filterByArtist / filterByGenre
  test("filterByArtist findet alle Alben eines Künstlers"):
    val result = AlbumService.filterByArtist(albums, "radiohead")
    result should have length 2

  test("filterByGenre findet alle Alben eines Genres"):
    val result = AlbumService.filterByGenre(albums, "Art Rock")
    result should have length 2

  // ratedAlbums / unratedAlbums
  test("ratedAlbums gibt nur bewertete Alben zurück"):
    val result = AlbumService.ratedAlbums(albums)
    result should have length 4
    result.forall(_.rating.isDefined) shouldBe true

  test("unratedAlbums gibt nur unbewertete Alben zurück"):
    val result = AlbumService.unratedAlbums(albums)
    result should have length 1
    result.head shouldBe album4

  // sortByRating / sortByTitle
  test("sortByRating sortiert absteigend (höchstes zuerst)"):
    val result = AlbumService.sortByRating(albums)
    result.head.rating shouldBe Some(10)  // OK Computer zuerst

  test("sortByTitle sortiert alphabetisch"):
    val result  = AlbumService.sortByTitle(albums)
    val titles  = result.map(_.title.toLowerCase)
    titles shouldBe titles.sorted

  // applyToRated — HIGHER ORDER FUNCTION
  test("applyToRated wendet Funktion nur auf bewertete Alben an"):
    val result    = AlbumService.applyToRated(albums, a => a.copy(genre = "Klassiker"))
    val unrated   = result.find(_.id == "4").get
    val rated     = result.find(_.id == "1").get
    unrated.genre shouldBe "Art Rock"    // Unbewertet → unverändert
    rated.genre   shouldBe "Klassiker" // Bewertet → verändert