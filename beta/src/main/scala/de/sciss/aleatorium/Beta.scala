/*
 *  Beta.scala
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

import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.io.File
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import scala.util.control.NonFatal

object Beta {
  object Park extends ArmPos(
    base     = 145,
    lowArm   =  92,
    highArm  =  83,
    ankle    =  84,
    gripRota = 176,
    gripOpen =  81,
  )

  object Awake extends ArmPos(
    base     = 144, //
    lowArm   =  91, //
    highArm  =  82, //
    ankle    =  83, //
    gripRota = 175, //
    gripOpen =  80, //
  )

  object Orient1 extends ArmPos(
    base     =  90, //
    lowArm   =  93, //
    highArm  =  80, //
    ankle    =  76, //
    gripRota = 175,
    gripOpen =  78, //
  )

  object Orient2 extends ArmPos(
    base     =  90, //
    lowArm   =  93,
    highArm  =  80,
    ankle    =  41, //
    gripRota = 175,
    gripOpen =  78, //
  )

  // fast
  object No1 extends ArmPos(
    base     =  66, //
    lowArm   =  93,
    highArm  =  80,
    ankle    =  41,
    gripRota = 175,
    gripOpen =  72, //
  )

  object No2 extends ArmPos(
    base     = 124, //
    lowArm   =  93,
    highArm  =  80,
    ankle    =  41,
    gripRota = 175,
    gripOpen =  72, //
  )

  // fast
  object No3 extends ArmPos(
    base     =  90,
    lowArm   =  93,
    highArm  =  80,
    ankle    =  41,
    gripRota = 175,
    gripOpen =  72, //
  )

  val Return1: ArmPos = Orient2
  val Return2: ArmPos = Orient1
  val Return3: ArmPos = Awake

  object Yes1 extends ArmPos(
    base     =  90,
    lowArm   =  93,
    highArm  =  65, //
    ankle    =  41,
    gripRota = 175,
    gripOpen =  64, //
  )

  object Yes2 extends ArmPos(
    base     =  93, //
    lowArm   =  94, //
    highArm  =  64, //
    ankle    =  13, //
    gripRota = 175,
    gripOpen =  64,
  )

  // fast
  object Yes3 extends ArmPos(
    base     =  93,
    lowArm   = 102, //
    highArm  =  64,
    ankle    =  13,
    gripRota = 175,
    gripOpen =  64,
  )

  // fast
  object Yes4 extends ArmPos(
    base     =  93,
    lowArm   =  85, //
    highArm  =  64,
    ankle    =  13,
    gripRota = 175,
    gripOpen =  64,
  )

  val Gesture: Seq[KeyFrame] = Seq(
    KeyFrame("Park"   , Park    ),
    KeyFrame("Awake"  , Awake   ),
    KeyFrame("Orient1" , Orient1  ),
    KeyFrame("Orient2" , Orient2 ),
    KeyFrame("No1" , No1, dur =  500  ),
    KeyFrame("No2" , No2, dur = 1000  ),
    KeyFrame("No3" , No3, dur =  500  ),
    KeyFrame("Return1"  , Return1   ),
    KeyFrame("Return2"  , Return2   ),
    KeyFrame("Return3"  , Return3   ),
  )

  //  object Park extends ArmPos(
  //    base     =  90,
  //    lowArm   = 118, //
  //    highArm  =  10,
  //    ankle    =  34,
  //    gripRota = 170,
  //    gripOpen =  74,
  //  )

  case class Config(
                     initDelay  : Int     = 120,
                     verbose    : Boolean = false,
                     shutdown   : Boolean = true,
                   )

  private def buildInfString(key: String): String = try {
    val clazz = Class.forName("de.sciss.aleatorium.BuildInfo")
    val m = clazz.getMethod(key)
    m.invoke(null).toString
  } catch {
    case NonFatal(_) => "?"
  }

  final def name: String = "Aleatorium"

  final def version: String = buildInfString("version")

  final def builtAt: String = buildInfString("builtAtString")

  final def fullVersion: String = s"v$version, built $builtAt"

  final def nameAndVersion: String = s"$name $fullVersion"

  def main(args: Array[String]): Unit = {
    Locale.setDefault(Locale.US)

    object p extends ScallopConf(args) {
      printedName = Beta.nameAndVersion

      private val default = Config()

      val verbose: Opt[Boolean] = opt("verbose", short = 'V', default = Some(false),
        descr = "Verbose printing."
      )
      val initDelay: Opt[Int] = opt("init-delay", default = Some(default.initDelay),
        descr = s"Initial delay in seconds (to make sure date-time is synced) (default: ${default.initDelay})."
      )
      val noShutdown: Opt[Boolean] = opt("no-shutdown", descr = "Do not shutdown Pi after compleition.", default = Some(false))

      verify()
      val config: Config = Config(
        initDelay = initDelay(),
        verbose   = verbose(),
        shutdown  = !noShutdown(),
      )
    }
    run(p.config)
  }

  def run(c: Config): Unit = {
    println(Beta.nameAndVersion)
    val sCfg = ServoUI.Config(
      /*dryRun = true*/
      presets     = Gesture,
      offAfterSeq = true,
    )
    val runSeq    = Var(false)
    ServoUI.run(sCfg, ArmModel(Park), runSeq)
//    butState.addListener {
//      case false =>
//        if (!runSeq()) {
//          println("Launch sequence")
//          runSeq() = true
//        }
//    }
  }

  def shutdown(): Unit = {
    import sys.process._
    Seq("sudo", "shutdown", "now").!
  }

  def stampedFile(folder: File, pre: String, ext: String, date: Date): File = {
    val extP = if (ext.startsWith(".")) ext else s".$ext"
    require(!pre.contains('\''), "Invalid characters " + pre)
    require(!ext.contains('\''), "Invalid characters " + ext)
    val df = new SimpleDateFormat(s"'$pre-'yyMMdd'_'HHmmss'$extP'", Locale.US)
    new File(folder, df.format(date))
  }
}
