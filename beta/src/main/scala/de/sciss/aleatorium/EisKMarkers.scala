/*
 *  EisKMarkers.scala
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

import de.sciss.osc
import de.sciss.osc.Implicits._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.util.Try

object EisKMarkers {
  def main(args: Array[String]): Unit = run()

  case class Marker(pos: Int, name: String)

  def run(): Unit = {
    val client = osc.TCP.Client("127.0.01" -> 17737)
    client.connect()
//    client.dump()

    var idCnt = 0
    def nextId(): Int = {
      idCnt += 1
      idCnt
    }

    def query [A](addr: String)(properties: String*)(reply: Function[Seq[Any], A]): Future[A] = {
      val p = Promise[A]()
      val Id = nextId()
      client.action = {
        case osc.Message("/query.reply", Id, args @ _*) =>
          val res = Try(reply(args))
          p.complete(res)
        case _ =>
      }
      client ! osc.Message(addr, "query" +: Id +: properties: _*)
      p.future
    }

    def get [A](addr: String)(args: Any*)(reply: Function[Seq[Any], A]): Future[A] = {
      val p = Promise[A]()
      val Id = nextId()
      client.action = {
        case osc.Message("/get.reply", Id, args @ _*) =>
          val res = Try(reply(args))
          p.complete(res)
        case _ =>
      }
      client ! osc.Message(addr, "get" +: Id +: args: _*)
      p.future
    }

    val addrMarkers = "/doc/active/markers"

    val futNum = query(addrMarkers)("count") { case Seq(num: Int) => num }
    val num     = Await.result(futNum, Duration.Inf)

    val exp = 792 * 2
    println(s"Number of markers: $num -- should be $exp")

    @tailrec
    def getRange(start: Int = 0, res: Vector[Marker] = Vector.empty): Vector[Marker] =
      if (start == num) res else {
        val stop = math.min(num, start + 100)
        val futRange = get(addrMarkers)("range", start, stop) { xs =>
          xs.grouped(2).map {
            case Seq(pos: Int, name: String) => Marker(pos, name)
          } .toVector
        }
        val marks = Await.result(futRange, Duration.Inf)
        getRange(start = stop, res = res ++ marks)
      }

    val marks = getRange()

    val words = {
      val in = io.Source.fromFile("notes/Wordlist.txt", "UTF-8")
      try {
        in.getLines().map(_.trim).filterNot(_.isBlank).toVector
      } finally {
        in.close()
      }
    }

    val dupOpt = marks.sliding(2, 1).collectFirst {
      case Seq(m1, m2) if m1.pos == m2.pos => m1
    }
    dupOpt.foreach { dup =>
      println(s"!!! DUPLICATE marker: $dup")
    }

    val misalignedOpt = marks.grouped(2).collectFirst {
      case Seq(_, m2) if m2.name != "Mark" => m2
    }
    misalignedOpt.foreach { m =>
      println(s"!!! MISALIGNED marker: $m")
    }

    println(s"Markers received: ${marks.size}; words ${words.size}")
    val wrongOpt = (words zip marks.sliding(1, 2).flatten.toVector).collectFirst {
      case (correct, m @ Marker(_, found)) if found != correct && found != "Mark" => (correct, m)
    }
    wrongOpt.foreach { case (correct, m) =>
      println(s"!!! MISTAKE. Word should be $correct, but found $m")
    }

    println()
    println(marks.map(_.pos).grouped(10).map(_.mkString(", ")).mkString(",\n"))
    sys.exit()
  }
}
