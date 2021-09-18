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

import de.sciss.osc
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.io.File
import java.net.InetSocketAddress
import java.text.SimpleDateFormat
import java.util.{Date, Locale}
import scala.util.control.NonFatal

object Beta {
  final val DefaultPort = 57121

  case class Config(
                     initDelay  : Int       = 120,
                     verbose    : Boolean   = false,
                     shutdown   : Boolean   = true,
                     light      : Boolean   = true,
                     sound      : Boolean   = true,
                     oscPort    : Int       = DefaultPort,
                   )

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
    lowArm   = 105, //
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

  object Dice1 extends ArmPos(
    base     = 176, //
    lowArm   =  91,
    highArm  =  82,
    ankle    =  83,
    gripRota = 175,
    gripOpen =  80,
  )

  object Dice2 extends ArmPos(
    base     = 176,
    lowArm   = 100, //
    highArm  =  60, //
    ankle    =  60, //
    gripRota = 175,
    gripOpen =  85, //
  )

  object Dice3 extends ArmPos(
    base     = 176,
    lowArm   = 110, //
    highArm  =  50, //
    ankle    =  30, //
    gripRota = 175,
    gripOpen =  90, //
  )

  object Dice4 extends ArmPos(
    base     = 176,
    lowArm   = 122, //
    highArm  =  25, //
    ankle    =  23, //
    gripRota = 175,
    gripOpen =  90,
  )

  object Dice5 extends ArmPos(
    base     = 176,
    lowArm   = 122,
    highArm  =  25,
    ankle    =  23,
    gripRota = 175,
    gripOpen =  69, //
  )

  object Dice6 extends ArmPos(
    base     = 176,
    lowArm   = 122,
    highArm  =  40, //
    ankle    =  23,
    gripRota = 175,
    gripOpen =  69,
  )

  object Dice7 extends ArmPos(
    base     = 176,
    lowArm   =  97, //
    highArm  =  74, //
    ankle    =  23,
    gripRota = 175,
    gripOpen =  69,
  )

  object Dice8 extends ArmPos(
    base     = 134, //
    lowArm   =  97, //
    highArm  =  74,
    ankle    =  23,
    gripRota = 134, //
    gripOpen =  69,
  )

  // "hold"
  object Dice8b extends ArmPos(
    base     = 134,
    lowArm   =  97,
    highArm  =  74,
    ankle    =  23,
    gripRota = 133,
    gripOpen =  69,
  )

  object DiceThrow extends ArmPos(
    base     = 134,
    lowArm   =  97,
    highArm  =  74,
    ankle    =  23,
    gripRota = 134,
    gripOpen =  95,
  )

  val GestureNo: Seq[KeyFrame] = Seq(
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

  val NameNo = "No"

  val GestureYes: Seq[KeyFrame] = Seq(
    KeyFrame("Park"   , Park    ),
    KeyFrame("Awake"  , Awake   ),
    KeyFrame("Orient1" , Orient1  ),
    KeyFrame("Orient2" , Orient2 ),
    KeyFrame("Yes1" , Yes1, dur =  500  ),
    KeyFrame("Yes2" , Yes2, dur = 1000  ),
    KeyFrame("Yes3" , Yes3, dur =  500  ),
    KeyFrame("Yes4" , Yes4, dur =  500  ),
    KeyFrame("Return1"  , Return1   ),
    KeyFrame("Return2"  , Return2   ),
    KeyFrame("Return3"  , Return3   ),
  )

  val NameYes = "Yes"

  val GestureDice: Seq[KeyFrame] = Seq(
    KeyFrame("Park"   , Park    ),
    KeyFrame("Awake"  , Awake   ),
    KeyFrame("Dice1" , Dice1, dur = 1000  ),
    KeyFrame("Dice2" , Dice2, dur = 1000  ),
    KeyFrame("Dice3" , Dice3, dur = 1000  ),
    KeyFrame("Dice4" , Dice4, dur = 1000  ),
    KeyFrame("Dice5" , Dice5  ),
    KeyFrame("Dice6" , Dice6, dur = 1500  ),
    KeyFrame("Dice7" , Dice7, dur = 1500  ),
    KeyFrame("Dice8" , Dice8, dur = 1000  ),
    KeyFrame("Dice8b", Dice8b, dur = 1000  ),
    KeyFrame("DiceThrow", DiceThrow, dur = 500  ),
    KeyFrame("Return3"  , Return3   ),
  )

  val NameDice = "Dice"

  val Presets: Map[String, Seq[KeyFrame]] = Map(
    NameNo    -> GestureNo,
    NameYes   -> GestureYes,
    NameDice  -> GestureDice,
  )

  val PresetNames = Seq(NameNo, NameYes, NameDice)

  //  object Park extends ArmPos(
  //    base     =  90,
  //    lowArm   = 118, //
  //    highArm  =  10,
  //    ankle    =  34,
  //    gripRota = 170,
  //    gripOpen =  74,
  //  )

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
      val noShutdown: Opt[Boolean] = opt("no-shutdown", descr = "Do not shutdown Pi after completion.", default = Some(false))

      val noSound: Opt[Boolean] = opt("no-sound", descr = "Do not play sound.", default = Some(false))
      val noLight: Opt[Boolean] = opt("no-light", descr = "Do not flash light.", default = Some(false))

      val oscPort: Opt[Int] = opt(
        name    = "port",
        default = Some(default.oscPort),
        descr   = s"OSC port or 0 to disable OSC input (default: ${default.oscPort}).",
        validate = x => x >= 0 && x <= 0xFFFFFF,
      )

      verify()
      val config: Config = Config(
        initDelay = initDelay(),
        verbose   = verbose(),
        shutdown  = !noShutdown(),
        sound     = !noSound(),
        light     = !noLight(),
        oscPort   = oscPort(),
      )
    }
    run(p.config)
  }

  def run(c: Config): Unit = {
    println(Beta.nameAndVersion)
    val uiCfg = ServoUI.Config(
      /*dryRun = true*/
      presets     = Presets,
      offAfterSeq = false,    // too fragile in that position
    )
    val runSeq  = Var(false)
    val pstName = Var(NameNo)
    ServoUI.run(uiCfg, ArmModel(Park), pstName, runSeq)
//    butState.addListener {
//      case false =>
//        if (!runSeq()) {
//          println("Launch sequence")
//          runSeq() = true
//        }
//    }

    val lightOpt = if (c.light) {
      val tCfg = osc.UDP.Config()
      tCfg.localIsLoopback = true
      val t = osc.UDP.Transmitter(tCfg)
      t.connect()
      Some(t)
    } else None

    def setRunSeq(gesture: Int): Unit = {
      pstName() = PresetNames(gesture)
      runSeq()  = true
    }

    if (c.oscPort > 0) {
      val rCfg = osc.UDP.Config()
      rCfg.localIsLoopback = true
      rCfg.localPort       = c.oscPort
      val rcv = osc.UDP.Receiver(rCfg)
      rcv.action = {
        case (osc.Message("/arm", gesture: Int), _) =>
          setRunSeq(gesture)
        case (x, from) =>
          println(s"Unsupported OSC packet $x from $from")
      }
      rcv.connect()
      println(s"Beta awaiting /arm messages on OSC port ${c.oscPort}")
    }

    if (c.sound) {
      val tgt   = new InetSocketAddress("127.0.0.1", Light.DefaultPort)
      val sCfg  = Sound.Config(
        onTrig = { () =>
          println("Bang!")
          lightOpt.foreach { t =>
            try {
              t.send(osc.Message("/flash", 0xFFFFFF, 100), tgt)
            } catch {
              case NonFatal(ex) =>
                Console.err.println("While sending light OSC:")
                ex.printStackTrace()
            }
            setRunSeq(0)
          }
        },
      )
      Sound.run(sCfg)
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
