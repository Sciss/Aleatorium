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
    lowArm   = 118 + 1, //
    highArm  =  10,
    ankle    =  34,
    gripRota = 170,
    gripOpen =  74,
  )

  object Awake extends ArmPos(
    base     =  90,
    lowArm   = 117, //
    highArm  =  12,
    ankle    =  34,
    gripRota = 170,
    gripOpen =  74,
  )

  object Raise1 extends ArmPos(
    base     =  90,
    lowArm   = 117, // 119, //
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
    lowArm   = 138 + 1, //
    highArm  =  59, //
    ankle    =  15,
    gripRota = 170,
    gripOpen = 110,
  )

  object Reach3 extends ArmPos(
    base     =  90,
    lowArm   = 146 + 1, //
    highArm  =  71, //
    ankle    =  10, //
    gripRota = 170,
    gripOpen = 110,
  )

  object Grab1 extends ArmPos(
    base     =  90,
    lowArm   = 150 + 1, //
    highArm  =  71,
    ankle    =  10,
    gripRota = 170,
    gripOpen =  90, //
  )

  object Grab2 extends ArmPos(
    base     =  90,
    lowArm   = 150 + 1,
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
    base     = 162, // 170, //
    lowArm   = 114, // 109, // 112,
    highArm  =  89, // 80, //
    ankle    =  30,
    gripRota = 135, //
    gripOpen =  74,
  )

  // "pause"
  object Swerve2b extends ArmPos(
    base     = 162,
    lowArm   = 114, // 109, //
    highArm  =  89, // 82,
    ankle    =  30,
    gripRota = 170, // 135,
    gripOpen =  74,
  )

  object Drop extends ArmPos(
    base     = 162, // 170,
    lowArm   = 114, // 109, // 112,
    highArm  =  89, // 82,
    ankle    =  30,
    gripRota = 170, // 135,
    gripOpen = 110,
  )

  object Return1 extends ArmPos(
    base     =  90, //
    lowArm   = 112, //
    highArm  =  80, //
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

  val Gesture: Seq[KeyFrame] = Seq(
    KeyFrame("Park"   , Park    ),
    KeyFrame("Awake"  , Awake   ),
    KeyFrame("Raise1" , Raise1  ),
    KeyFrame("Raise2" , Raise2  ),
    KeyFrame("Reach1" , Reach1  ),
    KeyFrame("Reach2" , Reach2  ),
    KeyFrame("Reach3" , Reach3  ),
    KeyFrame("Grab1"  , Grab1   ),
    KeyFrame("Grab2"  , Grab2   ),
    KeyFrame("Lift1"  , Lift1   ),
    KeyFrame("Lift2"  , Lift2   ),
    KeyFrame("Swerve1", Swerve1 ),
    KeyFrame("Swerve2", Swerve2 ),
    KeyFrame("Swerve2b", Swerve2b, dur = 1000),
    KeyFrame("Drop"   , Drop    ),
    KeyFrame("Return1", Return1 ),
    KeyFrame("Return2", Return2 ),
    KeyFrame("Return3", Return3 ),
    KeyFrame("Return4", Return4 ),
  )

  val NameGesture = "Default"

  val Presets: Map[String, Seq[KeyFrame]] = Map(
    NameGesture -> Gesture
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

  @volatile
  private var shouldShutDown = false

  def run(c: Config): Unit = {
    println(Alpha.nameAndVersion)

    def quitOrShutdown(): Unit = {
      println("Going to shut down")
      if (c.shutdown) shutdown() else sys.exit()
    }

    val sCfg = ServoUI.Config(
      /*dryRun = true*/
      presets     = Presets,
      offAfterSeq = true,
    )
    val runSeq    = Var(false)
    val butState  = Var(true)
    val pstName   = Var(NameGesture)
    ServoUI.run(sCfg, ArmModel(Park), pstName, runSeq)
    runSeq.addListener {
      case false =>
        if (shouldShutDown) quitOrShutdown()
    }

    val fCfg = FootSwitch.Config()
    FootSwitch.run(fCfg, butState)
    butState.addListener {
      case false =>
        if (!runSeq() && !shouldShutDown) {
          println("Launch sequence")
          runSeq() = true
        }
    }

    val oCfg = OffSwitch.Config()
    OffSwitch.run(oCfg) { () =>
      if (!shouldShutDown) {
        shouldShutDown = true
        if (!runSeq()) quitOrShutdown()
      }
    }
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
