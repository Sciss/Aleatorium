/*
 *  Alpha.scala
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

import de.sciss.aleatorium.ServoUI.Preset
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.io.File
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import scala.util.control.NonFatal

object Alpha {
//  case object ParkOLD extends ArmPos(
//    base     =  90,
//    lowArm   = 124,
//    highArm  =  20,
//    ankle    =  35,
//    gripRota = 170,
//    gripOpen =  74,
//  )

  object Park extends ArmPos(
    base     =  90,
    lowArm   = 118, //
    highArm  =  10,
    ankle    =  34,
    gripRota = 170,
    gripOpen =  74,
  )

  object Raise1 extends ArmPos(
    base     =  90,
    lowArm   = 119, //
    highArm  =  20, //
    ankle    =  30, //
    gripRota = 170,
    gripOpen =  79, //
  )

  object Raise2 extends ArmPos(
    base     =  90,
    lowArm   = 119,
    highArm  =  34, //
    ankle    =  20, //
    gripRota = 170,
    gripOpen = 100, //
  )

  object Reach1 extends ArmPos(
    base     =  90,
    lowArm   = 130, //
    highArm  =  51, //
    ankle    =  15, //
    gripRota = 170,
    gripOpen = 105, //
  )

  object Reach2 extends ArmPos(
    base     =  90,
    lowArm   = 138, //
    highArm  =  59, //
    ankle    =  15,
    gripRota = 170,
    gripOpen = 110,
  )

  object Reach3 extends ArmPos(
    base     =  90,
    lowArm   = 146, //
    highArm  =  71, //
    ankle    =  10, //
    gripRota = 170,
    gripOpen = 110,
  )

  object Grab1 extends ArmPos(
    base     =  90,
    lowArm   = 150, //
    highArm  =  71,
    ankle    =  10,
    gripRota = 170,
    gripOpen =  90, //
  )

  object Grab2 extends ArmPos(
    base     =  90,
    lowArm   = 150,
    highArm  =  71,
    ankle    =  10,
    gripRota = 170,
    gripOpen =  74, //
  )

  object Lift1 extends ArmPos(
    base     =  90,
    lowArm   = 137, //
    highArm  =  71,
    ankle    =  10,
    gripRota = 170,
    gripOpen =  74,
  )

  object Lift2 extends ArmPos(
    base     =  90,
    lowArm   = 120, //
    highArm  =  80, //
    ankle    =  30, //
    gripRota = 170,
    gripOpen =  74,
  )

  object Swerve1 extends ArmPos(
    base     = 134, //
    lowArm   = 112, //
    highArm  =  80,
    ankle    =  30,
    gripRota = 170,
    gripOpen =  74,
  )

  object Swerve2 extends ArmPos(
    base     = 170, //
    lowArm   = 112,
    highArm  =  80, //
    ankle    =  30,
    gripRota = 135, //
    gripOpen =  74,
  )

  object Drop extends ArmPos(
    base     = 170,
    lowArm   = 112,
    highArm  =  80,
    ankle    =  30,
    gripRota = 135,
    gripOpen = 110,
  )

  object Return1 extends ArmPos(
    base     =  90, //
    lowArm   = 112,
    highArm  =  80,
    ankle    =  30,
    gripRota = 170, //
    gripOpen = 110,
  )

  object Return2 extends ArmPos(
    base     =  90,
    lowArm   = 124, //
    highArm  =  20, //
    ankle    =  35, //
    gripRota = 170,
    gripOpen =  74, //
  )

  object Return3 extends ArmPos(
    base     =  90,
    lowArm   = 110, //
    highArm  =  10, //
    ankle    =  26, //
    gripRota = 170, //
    gripOpen =  74, //
  )

  object Return4 extends ArmPos(
    base     =  90,
    lowArm   = 116, //
    highArm  =  10,
    ankle    =  34, //
    gripRota = 170,
    gripOpen =  74,
  )

  def presets: Seq[Preset] = Seq(
    Preset("Park", Park),
    Preset("Raise1", Raise1),
    Preset("Raise2", Raise2),
    Preset("Reach1", Reach1),
    Preset("Reach2", Reach2),
    Preset("Reach3", Reach3),
    Preset("Grab1", Grab1),
    Preset("Grab2", Grab2),
    Preset("Lift1", Lift1),
    Preset("Lift2", Lift2),
    Preset("Swerve1", Swerve1),
    Preset("Swerve2", Swerve2),
    Preset("Drop", Drop),
    Preset("Return1", Return1),
    Preset("Return2", Return2),
    Preset("Return3", Return3),
    Preset("Return4", Return4),
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
      printedName = Alpha.nameAndVersion

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
    println(Alpha.nameAndVersion)
    val sCfg = ServoUI.Config(
      /*dryRun = true*/
      presets = presets
    )
    ServoUI.run(sCfg, ArmModel(Park))
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
