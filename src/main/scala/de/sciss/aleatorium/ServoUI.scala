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

import com.pi4j.component.servo.impl.PCA9685GpioServoProvider
import com.pi4j.gpio.extension.pca.{PCA9685GpioProvider, PCA9685Pin}
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.i2c.I2CFactory
import de.sciss.numbers.Implicits.doubleNumberWrapper
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import scala.swing.event.ValueChanged
import scala.swing.{BoxPanel, Button, Frame, Orientation, Slider, Swing, TextField}

object ServoUI {
  case class Config(
                     i2cBus   : Int             = 1,
                     pwmMin   : Int             = 560,
                     pwmMax   : Int             = 2500,
                     freq     : Double          = 50.0,
                   )

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      printedName = "ServoUI"
      private val default = Config()

      val i2cBus: Opt[Int] = opt(default = Some(default.i2cBus),
        descr = s"I2C bus id 0 to 17 (default: ${default.i2cBus}).",
        validate = x => x >= 0 && x <= 17
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

      verify()
      val config: Config = Config(
        i2cBus  = i2cBus(),
        pwmMin  = pwmMin(),
        pwmMax  = pwmMax(),
        freq    = freq(),
      )
    }

    Swing.onEDT {
      run(p.config)
    }
  }

  def run(config: Config): Unit = {
    val gpioProvider = createProvider(i2cBus = config.i2cBus, freq = config.freq)
    val gpio = GpioFactory.getInstance
    val pins = Seq(
      PCA9685Pin.PWM_00,
      PCA9685Pin.PWM_01,
      PCA9685Pin.PWM_02,
      PCA9685Pin.PWM_03,
      PCA9685Pin.PWM_04,
      PCA9685Pin.PWM_05,
    )
    pins.foreach { pin =>
      /* val pin = */ gpio.provisionPwmOutputPin(gpioProvider, pin)
    }

    val servoProvider = new PCA9685GpioServoProvider(gpioProvider)

    def calculatePwmDuration(angle: Double, lo: Int = 0, hi: Int = 180): Int =
      (angle.clip(lo, hi).linLin(lo, hi, config.pwmMin, config.pwmMax) + 0.5).toInt

    val sliders = pins.map { pin =>
      val servoDriver = servoProvider.getServoDriver(pin)
      val tx = new TextField(4)
      tx.editable = false
      val sl = new Slider
      sl.min = 0
      sl.max = 180
      sl.paintLabels = true
      def updateText(): Unit = {
        tx.text = sl.value.toString
      }
      sl.reactions += {
        case ValueChanged(_) =>
          updateText()
      }
      updateText()
      val bSet = Button("Set") {
        val micros = calculatePwmDuration(sl.value)
        servoDriver.setServoPulseWidth(micros)
      }
      val bOff = Button("Off") {
//        servoDriver.setServoPulseWidth(0)
        servoDriver.getProvider.setAlwaysOff(pin)
      }
      val bp = new BoxPanel(Orientation.Horizontal)
      bp.contents += sl
      bp.contents += tx
      bp.contents += bSet
      bp.contents += bOff
      bp
    }

    val p = new BoxPanel(Orientation.Vertical)
    p.contents ++= sliders

    new Frame {
      title     = "Server UI"
      contents  = p
      pack().centerOnScreen()
      open()

      override def closeOperation(): Unit = sys.exit()
    }
  }

  def createProvider(i2cBus: Int, freq: Double): PCA9685GpioProvider = {
    val bus = I2CFactory.getInstance(i2cBus)
    new PCA9685GpioProvider(bus, 0x40, new java.math.BigDecimal(freq))
  }
}
