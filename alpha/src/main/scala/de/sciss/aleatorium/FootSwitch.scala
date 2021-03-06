/*
 *  FootSwitch.scala
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

/*
  wire 1 : GND
  wire 2 : GPIO 5

 */
object FootSwitch {
  case class Config(
                     pin      : Pin               = RaspiPin.GPIO_05,
                     pull     : PinPullResistance = PinPullResistance.PULL_UP,
                     debounce : Int               = 20,
                     log      : Boolean           = false,
                   )

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      printedName = "FootSwitch Test"

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
      verify()

      val config: Config = Config(
        pin       = Switch.parsePin (pin()  , default.pin ),
        pull      = Switch.parsePull(pull() , default.pull),
        debounce  = debounce(),
        log       = log(),
      )
    }

    println("FootSwitch test")
    val state = Var(false)
    state.addListener {
      case v =>
        val name = if (v) "HIGH" else "LOW"
        println(s"Detected $name")
    }
    run(p.config, state)
    while (true) {
      //      println(s"RED: ${butRed.getState.getValue}")
      Thread.sleep(500)
    }
  }

  def run(config: Config, state: Var[Boolean]): Unit = {
    val gpio      = GpioFactory.getInstance
    println(s"provisionDigitalInputPin(${config.pin}, ${config.pull})")
    val button    = gpio.provisionDigitalInputPin(config.pin, config.pull)
//    button.setPullResistance(config.pull)
    if (config.debounce > 0) button.setDebounce(config.debounce)

    if (config.log) println(s"Initial state: ${button.getState} ; resistance ${button.getPullResistance}")

    //    but.setShutdownOptions(true)

    button.addListener(new GpioPinListenerDigital() {
//      private var t0 = 0L
//      private var t1 = 0L
//      private var t2 = 0L

      override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent): Unit = {
        if (config.log) println(s"button: ${event.getState}")
//        val state = if (event.getState.isHigh) "HIGH" else "LOW"
//        println(s"Detected $state.")
        state() = event.getState.isHigh
      }
    })
  }
}
