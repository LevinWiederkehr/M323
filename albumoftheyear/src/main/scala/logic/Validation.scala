package logic

object Validation:

  def isValidRating(rating: Int): Boolean =
    rating >= 1 && rating <= 10

  def isNonEmpty(input: String): Boolean =
    input.trim.nonEmpty

  def parseInteger(input: String): Option[Int] =
    input.trim.toIntOption

  def askValidRating(): Int =
    print("Bewertung eingeben (1-10): ")
    val input = scala.io.StdIn.readLine()

    parseInteger(input) match
      case Some(n) if isValidRating(n) =>
        n  // Gültige Eingabe → Rekursion endet hier
      case _ =>
        println("Ungültige Eingabe. Bitte eine Zahl zwischen 1 und 10 eingeben.")
        askValidRating()  // REKURSIVER AUFRUF

  def askNonEmptyInput(prompt: String): String =
    print(prompt)
    val input = scala.io.StdIn.readLine()

    if isNonEmpty(input) then
      input.trim  // Gültige Eingabe → Rekursion endet hier
    else
      println("Eingabe darf nicht leer sein.")
      askNonEmptyInput(prompt)  // REKURSIVER AUFRUF