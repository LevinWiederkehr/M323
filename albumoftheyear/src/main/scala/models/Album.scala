package models

case class Album(
                  id: String, // MusicBrainz ID
                  title: String,
                  artist: String,
                  year: Int,
                  genre: String,
                  rating: Option[Int]  // Bewertung: None = noch nicht bewertet, Some(1-10) = bewertet
                )
