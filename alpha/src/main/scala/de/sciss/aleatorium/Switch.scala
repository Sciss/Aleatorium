package de.sciss.aleatorium

import com.pi4j.io.gpio.{Pin, PinPullResistance, RaspiPin}

import java.util.Locale

object Switch {
  def parsePin(i: Int, default: Pin): Pin = i match {
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

  def parsePull(s: String, default: PinPullResistance): PinPullResistance = s.toUpperCase(Locale.US) match {
    case "DOWN" => PinPullResistance.PULL_DOWN
    case "UP"   => PinPullResistance.PULL_UP
    case "OFF"  => PinPullResistance.OFF
    case _ =>
      println(s"Illegal pull $s. Fall back to $default")
      default
  }
}
