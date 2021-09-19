/*
 *  Wordlist.scala
 *  (Aleatorium)
 *
 *  Copyright (c) 2021 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Affero General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.aleatorium

import de.sciss.file._

import java.util.Locale
import scala.annotation.tailrec

object Wordlist {
  def main(args: Array[String]): Unit =
    run()

  def run(): Unit = {
    val base  = file("/data") / "projects" / "Aleatorium"
    val fIn   = base / "materials" / "barad-ch-8-last.txt"
    val raw   = {
      val s = io.Source.fromFile(fIn, "UTF-8")
      try {
        s.getLines().mkString(" ")
          .replace('—', ' ')
          .replace('/', ' ')
          .replace('(', ' ')
          .replace(')', ' ')
          .split("\\s").filterNot(_.isBlank)
      } finally {
        s.close()
      }
    }
    // println(raw.length)
    val alpha = raw.flatMap { in =>
      @tailrec
      def trimLeft(s: String): String =
        if (s.isEmpty) s else s.head match {
          case '"' | '\'' /*| '('*/ => trimLeft(s.tail)
          case _ => s
        }

      @tailrec
      def trimRight(s: String): String =
        if (s.isEmpty) s else s.last match {
          case '.' | ',' | ':' | ';' | '"' | '\'' /*| ')'*/ | '!' | '?' => trimRight(s.init)
          case _ => s
      }

      val t0  = trimRight(trimLeft(in)).toLowerCase(Locale.US)
      val t   = if (t0.endsWith("'s")) t0.dropRight(2) else t0
      if (t.isBlank || !t.head.isLetter) None else {
        if (t == "a" || t == "i" || t.length > 1) Some(t) else None   // remove orphaned letters
      }
    }
    val set0  = alpha.distinct.sorted
    val set   = set0 diff Vector("i.e", "ized")
    println(s"Set size: ${set.length}")
    set.foreach(println)
  }
}
