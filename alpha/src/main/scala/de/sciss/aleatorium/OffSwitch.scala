/*
 *  OffSwitch.scala
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

import com.pi4j.io.gpio.event.{GpioPinDigitalStateChangeEvent, GpioPinListenerDigital}
import com.pi4j.io.gpio.{GpioFactory, Pin, PinPullResistance, RaspiPin}
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.util.{Timer, TimerTask}

/*
  wire 1 : GND
  wire 2 : GPIO 6

 */
object OffSwitch {
  case class Config(
                     pin      : Pin               = RaspiPin.GPIO_06,
                     pull     : PinPullResistance = PinPullResistance.PULL_UP,
                     debounce : Int               = 20,
                     duration : Float             = 3.0f,
                     log      : Boolean           = false,
                   )

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      printedName = "OffSwitch Test"

      private val default = Config()

      val pin: Opt[Int] = opt("pin", default = Some(default.pin.getAddress),
        descr = s"GPIO pin for foot switch (default: ${default.pin.getAddress})"
      )
      val pull: Opt[String] = opt("pull", default = Some(default.pull.toString),
        descr = s"Resistor pull mode, one of 'UP', 'DOWN, 'OFF' (default ${default.pull})"
      )
      val log: Opt[Boolean] = toggle(default = Some(default.log),
        descrYes = "Print logging"
      )
      val debounce: Opt[Int] = opt(default = Some(default.debounce),
        descr = s"Debounce period in milliseconds. Zero to turn off (default: ${default.debounce})"
      )
      val duration: Opt[Float] = opt(default = Some(default.duration),
        descr = s"Duration in seconds, for keeping pressed to issue shutdown. (default: ${default.duration})"
      )
      verify()

      val config: Config = Config(
        pin       = Switch.parsePin (pin()  , default.pin ),
        pull      = Switch.parsePull(pull() , default.pull),
        debounce  = debounce(),
        duration  = duration(),
        log       = log(),
      )
    }

    println("OffSwitch test")
    val state = Var(false)
    state.addListener {
      case v =>
        val name = if (v) "HIGH" else "LOW"
        println(s"Detected $name")
    }
    run(p.config) { () =>
      println("Off switch activated")
    }
    while (true) {
      Thread.sleep(500)
    }
  }

  def run(config: Config)(trig: () => Unit): Unit = {
    val gpio      = GpioFactory.getInstance
    println(s"provisionDigitalInputPin(${config.pin}, ${config.pull})")
    val button    = gpio.provisionDigitalInputPin(config.pin, config.pull)
    if (config.debounce > 0) button.setDebounce(config.debounce)

    if (config.log) println(s"Initial state: ${button.getState} ; resistance ${button.getPullResistance}")

//    var timePressed = Long.MaxValue

    button.addListener(new GpioPinListenerDigital() {
      private var scheduled   = Option.empty[TimerTask]
      private val timer       = new Timer
      private val durMillis   = (config.duration * 1000).toLong

      override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent): Unit = {
        if (config.log) println(s"button: ${event.getState}")
        val pressed = event.getState.isLow
//        val t       = System.currentTimeMillis()
        scheduled.foreach(_.cancel())
        scheduled = None
        if (pressed) {
          val tt = new TimerTask {
            override def run(): Unit = trig()
          }
          timer.schedule(tt, durMillis)
//          timePressed = t
          scheduled = Some(tt)
//        } else {
//          if ((t - durMillis) > timePressed) {
//            trig()
//          }
        }
      }
    })
  }
}
