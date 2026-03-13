package storage

import models.Album
import ujson.*

import java.io.{File, PrintWriter}
import scala.io.Source

object JsonStorage:

  private val dataPath = "data/albums.json"

  def loadAlbums(): List[Album] =
    val file = File(dataPath)
    if !file.exists() then
      List.empty  // Noch keine Daten gespeichert
    else
      val content = Source.fromFile(file).mkString
      val json = ujson.read(content)
      json.arr.toList.map(parseAlbum)

  def saveAlbums(albums: List[Album]): Unit =
    val json = ujson.Arr(albums.map(albumToJson)*)
    val writer = PrintWriter(File(dataPath))
    try
      writer.write(ujson.write(json, indent = 2))
    finally
      writer.close()

  private def parseAlbum(json: Value): Album =
    Album(
      id     = json("id").str,
      title  = json("title").str,
      artist = json("artist").str,
      year   = json("year").num.toInt,
      genre  = json("genre").str,
      rating = json("rating").strOpt.flatMap(_.toIntOption)
    )

  private def albumToJson(album: Album): Value =
    ujson.Obj(
      "id"     -> album.id,
      "title"  -> album.title,
      "artist" -> album.artist,
      "year"   -> album.year,
      "genre"  -> album.genre,
      "rating" -> album.rating.map(_.toString).getOrElse("null")
    )