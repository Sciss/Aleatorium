/*
 *  Light.scala
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

import com.github.mbelling.ws281x.{Color, LedStripType, Ws281xLedStrip}
import de.sciss.osc
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

trait Light {
  def setRGB(value: Int): Unit
}

object Light {
  val DefaultPort = 57120

  case class Config(
                     gpio       : Int       = 18,
                     stripType  : LedStripType = LedStripType.WS2811_STRIP_RGB,
                     invert     : Boolean   = false,
                     brightness : Int       = 0xFF,
                     verbose    : Boolean   = false,
                     flashDur   : Int       = 100,
                     flashRGB   : Int       = 0xFFFFFF,
                     oscPort    : Int       = DefaultPort,
                   )

  def apply(config: Config): Light = new Impl(config)

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      printedName = "Light"
      private val default = Config()

      val gpio: Opt[Int] = opt(default = Some(default.gpio),
        descr = s"GPIO pin for LED (default: ${default.gpio}).",
//        validate = x => x >= 0 && x <= 17
      )
      val flashDur: Opt[Int] = opt(
        name    = "duration",
        default = Some(default.flashDur),
        descr = s"Flash duration in milliseconds (default: ${default.flashDur}).",
        validate = x => x >= 0 && x <= 5000,
      )
      val flashRGB: Opt[Int] = opt(
        name    = "color",
        default = Some(default.flashRGB),
        descr   = s"Flash RGB colour (default: ${default.flashRGB}).",
        validate = x => x >= 0 && x <= 0xFFFFFF,
      )
      val oscPort: Opt[Int] = opt(
        name    = "port",
        default = Some(default.oscPort),
        descr   = s"OSC port or 0 to flash immediately (default: ${default.oscPort}).",
        validate = x => x >= 0 && x <= 0xFFFFFF,
      )
      val verbose: Opt[Boolean] = opt("verbose", short = 'V', default = Some(false),
        descr = "Verbose printing."
      )

      verify()
      val config: Config = Config(
        gpio      = gpio(),
        flashDur  = flashDur(),
        flashRGB  = flashRGB(),
        verbose   = verbose(),
        oscPort   = oscPort(),
      )
    }

    run(p.config)
  }

  def run(c: Config): Unit = {
    val light = Light(c)
    if (c.oscPort == 0) {
      light.setRGB(c.flashRGB)
      Thread.sleep(c.flashDur)
      light.setRGB(0)
    } else {
      val rCfg = osc.UDP.Config()
      rCfg.localIsLoopback = true
      rCfg.localPort       = c.oscPort
      val rcv = osc.UDP.Receiver(rCfg)
      rcv.action = {
        case (osc.Message("/flash", color: Int, dur: Int), _) =>
          light.setRGB(color)
          Thread.sleep(dur)
          light.setRGB(0)
        case (x, from) =>
          println(s"Unsupported OSC packet $x from $from")
      }
      rcv.connect()
      println(s"Light awaiting /flush messages on OSC port ${c.oscPort}")
      new Thread {
        private val forever = new AnyRef

        override def run(): Unit = forever.synchronized {
          forever.wait()
        }
      }
    }
  }

  private final class Impl(config: Config) extends Light {
//    private[this] val okBLA: Boolean = try {
//      if (config.verbose) {
//        println(s"WS2811Channel(/* GPIO = */ ${config.gpio}, /* LED count = */ 1, ${config.stripType}, /* invert = */ ${config.invert}, /* brightness = */ ${config.brightness})")
//      }
//      WS2811.init(new WS2811Channel(/* GPIO */ config.gpio, /* LED count */ 1,
//        config.stripType, /* invert */ config.invert, /* brightness */ config.brightness))
//      println("Light initialized.")
//      true
//    } catch {
//      case e: Exception =>
//        println("!! Failed to initialize WS2811:")
//        e.printStackTrace()
//        false
//    }

    private[this] val strip = try {
      if (config.verbose) {
        println(s"WS2811Channel(/* GPIO = */ ${config.gpio}, /* LED count = */ 1, ${config.stripType}, /* invert = */ ${config.invert}, /* brightness = */ ${config.brightness})")
      }
      val res = new Ws281xLedStrip(1 /*ledsCount*/, config.gpio /*gpioPin*/, 800000 /*frequencyHz*/,
        10 /*dma*/, config.brightness /*brightness*/, 0 /*pwmChannel*/, config.invert /*invert*/,
        config.stripType /*stripType*/, true /*clearOnExit*/)
      println("Light initialized.")
      res
    } catch {
      case e: Exception =>
        println("!! Failed to initialize WS2811:")
        e.printStackTrace()
        null
    }

    def setRGB(value: Int): Unit =
      if (strip != null) try {
//        WS2811.setPixel(0, value)
//        WS2811.render()
        strip.setPixel(0, new Color(value))
        strip.render()
      } catch {
        case _: Exception =>
          println("!! Failed to set RGB.")
      }
  }
}
