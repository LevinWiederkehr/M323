package logic

import models.Album

object AlbumService:

  def addAlbum(albums: List[Album], album: Album): List[Album] =
    albums :+ album  // Neue Liste = alte Liste + neues Album

  def removeAlbum(albums: List[Album], id: String): List[Album] =
    albums.filterNot(_.id == id)

  def rateAlbum(albums: List[Album], id: String, rating: Int): List[Album] =
    albums.map { album =>
      if album.id == id then
        album.copy(rating = Some(rating))  // Neue Instanz, nicht mutation!
      else
        album
    }

  def findByTitle(albums: List[Album], title: String): Option[Album] =
    albums match
      case Nil =>
        None  // Abbruchbedingung: nicht gefunden
      case head :: tail =>
        if head.title.toLowerCase.contains(title.toLowerCase) then
          Some(head)  // Abbruchbedingung: gefunden
        else
          findByTitle(tail, title)  // REKURSIVER AUFRUF mit Rest der Liste

  def averageRating(albums: List[Album]): Option[Double] =
    // Innere Hilfsfunktion: gibt (Summe, Anzahl) zurück
    def sumRatings(remaining: List[Album], acc: (Int, Int)): (Int, Int) =
      remaining match
        case Nil =>
          acc  // Abbruchbedingung: Liste erschöpft
        case head :: tail =>
          head.rating match
            case Some(r) => sumRatings(tail, (acc._1 + r, acc._2 + 1))  // REKURSIV
            case None    => sumRatings(tail, acc)                         // REKURSIV, kein Rating → überspringen

    val (sum, count) = sumRatings(albums, (0, 0))
    if count == 0 then None
    else Some(sum.toDouble / count)

  def filterByArtist(albums: List[Album], artist: String): List[Album] =
    albums.filter(_.artist.toLowerCase.contains(artist.toLowerCase))

  def filterByGenre(albums: List[Album], genre: String): List[Album] =
    albums.filter(_.genre.toLowerCase.contains(genre.toLowerCase))

  def sortByRating(albums: List[Album]): List[Album] =
    albums.sortBy(_.rating.getOrElse(-1))(Ordering[Int].reverse)

  def sortByTitle(albums: List[Album]): List[Album] =
    albums.sortBy(_.title.toLowerCase)

  def ratedAlbums(albums: List[Album]): List[Album] =
    albums.filter(_.rating.isDefined)

  def unratedAlbums(albums: List[Album]): List[Album] =
    albums.filter(_.rating.isEmpty)