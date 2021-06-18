/*
 *  FootSwitchTest.scala
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

import java.util.Locale

/*
  wire 1 : GND
  wire 2 : GPIO 5

 */
object FootSwitchTest {
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

      private def parsePin(i: Int, default: Pin): Pin = i match {
        case  0 => RaspiPin.GPIO_00
        case  1 => RaspiPin.GPIO_01
        case  2 => RaspiPin.GPIO_02
        case  3 => RaspiPin.GPIO_03
        case  4 => RaspiPin.GPIO_04
        case  5 => RaspiPin.GPIO_05
        case  6 => RaspiPin.GPIO_06
        case  7 => RaspiPin.GPIO_07
        case  8 => RaspiPin.GPIO_08
        case  9 => RaspiPin.GPIO_09
        case 10 => RaspiPin.GPIO_10
        case 11 => RaspiPin.GPIO_11
        case 12 => RaspiPin.GPIO_12
        case 13 => RaspiPin.GPIO_13
        case 14 => RaspiPin.GPIO_14
        case 15 => RaspiPin.GPIO_15
        case 16 => RaspiPin.GPIO_16
        case 17 => RaspiPin.GPIO_17
        case 18 => RaspiPin.GPIO_18
        case 19 => RaspiPin.GPIO_19
        case 20 => RaspiPin.GPIO_20
        case 21 => RaspiPin.GPIO_21
        case 22 => RaspiPin.GPIO_22
        case 25 => RaspiPin.GPIO_25
        case 27 => RaspiPin.GPIO_27
        case 28 => RaspiPin.GPIO_28
        case 29 => RaspiPin.GPIO_29
        case _ =>
          println(s"Illegal pin $i. Fall back to ${default.getAddress}")
          default
      }

      private def parsePull(s: String): PinPullResistance = s.toUpperCase(Locale.US) match {
        case "DOWN" => PinPullResistance.PULL_DOWN
        case "UP"   => PinPullResistance.PULL_UP
        case "OFF"  => PinPullResistance.OFF
        case _ =>
          println(s"Illegal pull $s. Fall back to ${default.pull}")
          default.pull
      }

      val config: Config = Config(
        pin       = parsePin(pin(), default.pin),
        pull      = parsePull(pull()),
        debounce  = debounce(),
        log       = log(),
      )
    }

    run(p.config)
  }

  def run(config: Config): Unit = {
    println("FootSwitch test")

    val gpio      = GpioFactory.getInstance
    println(s"provisionDigitalInputPin(${config.pin}, ${config.pull})")
    val button    = gpio.provisionDigitalInputPin(config.pin, config.pull)
//    button.setPullResistance(config.pull)
    if (config.debounce > 0) button.setDebounce(config.debounce)

    println(s"Initial state: ${button.getState} ; resistance ${button.getPullResistance}")

    //    but.setShutdownOptions(true)

    button.addListener(new GpioPinListenerDigital() {
//      private var t0 = 0L
//      private var t1 = 0L
//      private var t2 = 0L

      override def handleGpioPinDigitalStateChangeEvent(event: GpioPinDigitalStateChangeEvent): Unit = {
        if (config.log) println(s"button: ${event.getState}")
        val state = if (event.getState.isHigh) "HIGH" else "LOW"
        println(s"Detected $state.")
      }
    })

    while (true) {
      //      println(s"RED: ${butRed.getState.getValue}")
      Thread.sleep(500)
    }
  }
}
