package api

import models.Album
import sttp.client3.*
import ujson.*

import java.util.UUID
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration.*

object MusicBrainzClient:

  private val backend   = HttpURLConnectionBackend()
  private val baseUrl   = "https://musicbrainz.org/ws/2"
  private val userAgent = "AlbumVault/0.1 (schulprojekt)"

  // ExecutionContext stellt den Thread-Pool für Futures bereit
  given ExecutionContext = ExecutionContext.global

  def searchAlbum(title: String, artist: Option[String] = None): Option[Album] =
    val query = artist match
      case Some(a) => s"release:$title AND artist:$a"
      case None    => s"release:$title"

    val url = s"$baseUrl/release/?query=$query&fmt=json&limit=1"

    try
      val request = basicRequest
        .get(uri"$url")
        .header("User-Agent", userAgent)
        .header("Accept", "application/json")

      val response = request.send(backend)

      // Rate Limit einhalten (IMPURE — Seiteneffekt: Warten)
      Thread.sleep(1000)

      response.body match
        case Right(body) => parseFirstResult(body)
        case Left(error) =>
          println(s"API-Fehler: $error")
          None

    catch
      case e: Exception =>
        println(s"Netzwerkfehler: ${e.getMessage}")
        None

  def searchManyAlbums(titles: List[String]): List[Option[Album]] =
    println(s"🔍 Suche ${titles.length} Alben parallel...")

    // Für jedes Album einen Future starten (PARALLEL)
    val futures: List[Future[Option[Album]]] =
      titles.map(title => Future {
        searchAlbum(title)  // Läuft im Hintergrund
      })

    // Warten bis alle Futures fertig sind (max. 30 Sekunden)
    val allResults: Future[List[Option[Album]]] = Future.sequence(futures)
    Await.result(allResults, 30.seconds)

  private def parseFirstResult(body: String): Option[Album] =
    try
      val json     = ujson.read(body)
      val releases = json("releases").arr

      if releases.isEmpty then
        None
      else
        val release = releases.head
        val id      = release("id").str
        val title   = release("title").str
        val artist  = release("artist-credit")(0)("artist")("name").str
        val year    = release("date").strOpt
          .flatMap(_.split("-").headOption)
          .flatMap(_.toIntOption)
          .getOrElse(0)
        val genre   = release("release-group")("primary-type").strOpt.getOrElse("Unbekannt")

        Some(Album(
          id     = id,
          title  = title,
          artist = artist,
          year   = year,
          genre  = genre,
          rating = None
        ))

    catch
      case e: Exception =>
        println(s"Fehler beim Parsen der API-Antwort: ${e.getMessage}")
        None

  def generateFallbackId(): String =
    UUID.randomUUID().toString