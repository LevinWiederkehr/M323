package logic

import models.Album

object AlbumService:

  def filterAlbums(albums: List[Album], predicate: Album => Boolean): List[Album] =
    albums.filter(predicate)

  def transformAlbums(albums: List[Album], transform: Album => Album): List[Album] =
    albums.map(transform)

  def sortAlbumsBy[B: Ordering](albums: List[Album], key: Album => B): List[Album] =
    albums.sortBy(key)

  def applyToRated(albums: List[Album], f: Album => Album): List[Album] =
    albums.map(album => if album.rating.isDefined then f(album) else album)

  def addAlbum(albums: List[Album], album: Album): List[Album] =
    albums :+ album

  def removeAlbum(albums: List[Album], id: String): List[Album] =
    filterAlbums(albums, _.id != id)

  def rateAlbum(albums: List[Album], id: String, rating: Int): List[Album] =
    transformAlbums(albums, album =>
      if album.id == id then
        album.copy(rating = Some(rating))  // Neue Instanz, keine Mutation!
      else
        album
    )

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

  // SPEZIFISCHE FILTER — nutzen alle filterAlbums() (HOF)
  def filterByArtist(albums: List[Album], artist: String): List[Album] =
    filterAlbums(albums, _.artist.toLowerCase.contains(artist.toLowerCase))

  def filterByGenre(albums: List[Album], genre: String): List[Album] =
    filterAlbums(albums, _.genre.toLowerCase.contains(genre.toLowerCase))

  def ratedAlbums(albums: List[Album]): List[Album] =
    filterAlbums(albums, _.rating.isDefined)

  def unratedAlbums(albums: List[Album]): List[Album] =
    filterAlbums(albums, _.rating.isEmpty)

  // SORTIERUNG — nutzen sortAlbumsBy() (HOF)
  def sortByRating(albums: List[Album]): List[Album] =
    sortAlbumsBy(albums, _.rating.getOrElse(-1))(Ordering[Int].reverse)

  def sortByTitle(albums: List[Album]): List[Album] =
    sortAlbumsBy(albums, _.title.toLowerCase)