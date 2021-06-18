/*
 *  ServoTest.scala
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

import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.i2c.{I2CBus, I2CFactory}
import de.sciss.numbers.Implicits.doubleNumberWrapper
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}
import pi4j.component.servo.impl.PCA9685GpioServoProvider
import pi4j.gpio.extension.pca.{PCA9685GpioProvider, PCA9685Pin}

object ServoTest {
  case class Config(
                     channel  : Int             = 0,
                     angle    : Option[Double]  = None,
                     pwm      : Option[Int]     = None,
                     pwmMin   : Int             = 560,
                     pwmMax   : Int             = 2500,
                     freq     : Double          = 50.0,
                     waitMs   : Int             = 1000,
                   )

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      printedName = "ServoTest"
      private val default = Config()

      val channel: Opt[Int] = opt(required = true, default = Some(default.channel),
        descr = s"Servo controller channel 0 to 15 (default: ${default.channel}).",
        validate = x => x >= 0 && x < 16
      )
      val angle: Opt[Double] = opt(
        descr = "Target servo angle 0 to 180 degrees.",
        validate = x => x >= 0 && x <= 180,
      )
      val pwm: Opt[Int] = opt(
        descr = "PWM value in microseconds.",
        validate = x => x >= 0 && x <= 5000,
      )
      val pwmMin: Opt[Int] = opt(
        default = Some(default.pwmMin),
        descr = s"PWM minimum value in microseconds (default: ${default.pwmMin}).",
        validate = x => x >= 0 && x <= 5000,
      )
      val pwmMax: Opt[Int] = opt(
        default = Some(default.pwmMax),
        descr = s"PWM maximum value in microseconds (default: ${default.pwmMax}).",
        validate = x => x >= 0 && x <= 5000,
      )
      val freq: Opt[Double] = opt(
        default = Some(default.freq),
        descr = s"Oscillator frequency in Hz (default: ${default.freq}).",
        validate = x => x >= 0.0 && x <= 2000.0,
      )
      val waitMs: Opt[Int] = opt(
        name = "wait",
        default = Some(default.waitMs),
        descr = s"Wait time in milliseconds before quite (default: ${default.waitMs}).",
        validate = x => x >= 0,
      )

      verify()
      val config: Config = Config(
        channel = channel(),
        angle = angle.toOption,
        pwm = pwm.toOption,
        pwmMin = pwmMin(),
        pwmMax = pwmMax(),
        freq = freq(),
        waitMs = waitMs(),
      )
    }

    run(p.config)
  }

  def run(config: Config): Unit = {
    println("ServoTest")
    val gpioProvider = createProvider(config.freq)
    println(s"period duration in microseconds: ${gpioProvider.getPeriodDurationMicros}")
    val gpio = GpioFactory.getInstance
    val pin = config.channel match {
      case  0 => PCA9685Pin.PWM_00
      case  1 => PCA9685Pin.PWM_01
      case  2 => PCA9685Pin.PWM_02
      case  3 => PCA9685Pin.PWM_03
      case  4 => PCA9685Pin.PWM_04
      case  5 => PCA9685Pin.PWM_05
      case  6 => PCA9685Pin.PWM_06
      case  7 => PCA9685Pin.PWM_07
      case  8 => PCA9685Pin.PWM_08
      case  9 => PCA9685Pin.PWM_09
      case 10 => PCA9685Pin.PWM_10
      case 11 => PCA9685Pin.PWM_11
      case 12 => PCA9685Pin.PWM_12
      case 13 => PCA9685Pin.PWM_13
      case 14 => PCA9685Pin.PWM_14
      case 15 => PCA9685Pin.PWM_15
    }
    val servoName = s"Servo_${config.channel}"
    /* val pin = */ gpio.provisionPwmOutputPin(gpioProvider, pin, servoName)
    val servoProvider = new PCA9685GpioServoProvider(gpioProvider)
    val servoDriver = servoProvider.getServoDriver(pin)

    def calculatePwmDuration(angle: Double): Int =
      (angle.clip(0.0, 180.0).linLin(0.0, 180.0, config.pwmMin, config.pwmMax) + 0.5).toInt

    config.angle match {
      case Some(a) =>
        val pwmDuration = calculatePwmDuration(a)
        println(s"angle $a corresponds to pwm duration $pwmDuration")
        //        servo.setPosition(pos)
        servoDriver.setServoPulseWidth(pwmDuration)

      case None =>
    }

    config.pwm match {
      case Some(v) =>
        println(s"setServoPulseWidth($v)")
        servoDriver.setServoPulseWidth(v)

      case None =>
    }

    if (config.waitMs > 0) {
      println("Wait")
      Thread.sleep(config.waitMs)
    }
    println("Ok")
  }

  def createProvider(freq: Double): PCA9685GpioProvider = {
    val bus = I2CFactory.getInstance(I2CBus.BUS_1)
    new PCA9685GpioProvider(bus, 0x40, new java.math.BigDecimal(freq))
  }
}
