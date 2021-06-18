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
import com.pi4j.io.i2c.I2CFactory
import de.sciss.numbers.Implicits.doubleNumberWrapper
import de.sciss.swingplus.ComboBox
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}
import pi4j.component.servo.impl.PCA9685GpioServoProvider
import pi4j.gpio.extension.pca.{PCA9685GpioProvider, PCA9685Pin}

import scala.swing.event.{ButtonClicked, SelectionChanged, ValueChanged}
import scala.swing.{BoxPanel, Button, Dimension, FlowPanel, Frame, Label, Orientation, Slider, Swing, TextField}

object ServoUI {
  case class Config(
                     i2cBus   : Int             = 1,
                     pwmMin   : Int             = 560,
                     pwmMax   : Int             = 2500,
                     freq     : Double          = 50.0,
                     dryRun   : Boolean         = false,
                     presets  : Seq[NamedPos]     = Seq.empty,
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
      run(p.config, ArmModel(ArmPos.Unknown), Var(false))
    }
  }

  def run(config: Config, model: ArmModel, runSeq: Var[Boolean]): Unit = {
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

//    var seqRunning  = false
    var seqRunIdx   = 0

    runSeq.addListener {
      case false =>
        model.name() = "Stopped"
    }

    def stopSeq(): Unit = {
      //      if (seqRunning) {
      //        seqRunning    = false
      //        model.name() = "Stopped"
      //      }
      runSeq() = false
    }

    val txLine = new TextField(4)
    txLine.editable = false

    val slLine = new Slider
    slLine.min    = 100
    slLine.max    = 5000
    slLine.value  = 2000
    slLine.paintLabels = true
    slLine.preferredSize = new Dimension(500, 24)

    def updateTxLine(): Unit =
      txLine.text = slLine.value.toString

    updateTxLine()

    slLine.reactions += {
      case ValueChanged(_) =>
        updateTxLine()
    }

    val pLine = new FlowPanel(
      new Label("Line Duration:"), slLine, txLine
    )

    val ggPresets = new ComboBox[NamedPos](config.presets)
    val lbSeqName = new TextField(8)
    lbSeqName.editable = false

    model.name.addListener {
      case n =>
        Swing.onEDT {
          lbSeqName.text = n
        }
    }

    val bRunSeq   = new Button("Run Seq")
    val pPresets  = new FlowPanel(ggPresets, bRunSeq, lbSeqName)

    val p = new BoxPanel(Orientation.Vertical)
    p.contents += pPresets
    p.contents += pLine

    val sliders: Seq[Slider] = pins.zipWithIndex.zip(model.motors).map { case ((pin, idx), vr) =>
      val servoDriver = servoProvider.getServoDriver(pin)
      val txSl = new TextField(4)
      txSl.editable = false
      val txModel = new TextField(4)
      txModel.editable = false
      val sl = new Slider
      sl.min    = 0
      sl.max    = 180
      sl.value  = vr()
      sl.paintLabels = true

      def updateTextSl(): Unit = {
        txSl.text = sl.value.toString
      }
      def updateTextModel(): Unit = {
        txModel.text = vr().toString
      }

      sl.reactions += {
        case ValueChanged(_) =>
          updateTextSl()
      }

      updateTextSl()
      updateTextModel()

      vr.addListener {
        case value =>
          val micros = calculatePwmDuration(value)
          if (!config.dryRun) servoDriver.setServoPulseWidth(micros)
          Swing.onEDT {
            updateTextModel()
          }
      }

      val bSet = Button("Set") {
//        val micros = calculatePwmDuration(sl.value)
//        servoDriver.setServoPulseWidth(micros)
        stopSeq()
        vr() = sl.value
      }
      val bLine = Button("Line") {
        stopSeq()
        vr.lineTo(sl.value, slLine.value)
      }
      val bOff = Button("Off") {
//        servoDriver.setServoPulseWidth(0)
        stopSeq()
        servoDriver.getProvider.setAlwaysOff(pin)
      }
      val bp = new BoxPanel(Orientation.Horizontal)
      bp.contents += new Label((idx + 1).toString)
      bp.contents += sl
      bp.contents += txSl
      bp.contents += bSet
      bp.contents += bLine
      bp.contents += txModel
      bp.contents += bOff

      p.contents += bp
      sl
    }

    ggPresets.listenTo(ggPresets.selection)
    ggPresets.reactions += {
      case SelectionChanged(_) =>
        val newPos = ggPresets.selection.item.pos
        sliders.zip(newPos.seq).foreach { case (sl, v) =>
          sl.value = v
        }
    }


    def stepRunSeq(): Unit = {
      val pstSeq  = config.presets
      val pst     = pstSeq(seqRunIdx % pstSeq.size)
      model.name() = pst.name
      sliders.zip(pst.pos.seq).zip(model.motors).foreach { case ((sl, v), vr) =>
        sl.value = v
        if (seqRunIdx == 0) {
          vr() = v
          Thread.sleep(500) // XXX TODO ugly
          nextSeqStep()
        } else {
          vr.lineTo(v, duration = 2000)
        }
      }
    }

    def nextSeqStep(): Unit = {
      seqRunIdx += 1
      if (seqRunIdx <= config.presets.size) {
        stepRunSeq()
      } else {
        stopSeq()
      }
    }

    model.anim.addListener {
      case false =>
        Swing.onEDT {
          if (runSeq()) {
            nextSeqStep()
          }
        }
    }

    bRunSeq.reactions += {
      case ButtonClicked(_) =>
        stopSeq()
        val pstSeq = config.presets
        if (pstSeq.nonEmpty) {
          if (pstSeq.head.pos != model.current) {
            model.name() = "Not in initial!"
          } else {
            runSeq()    = true
            seqRunIdx   = 0
            stepRunSeq()
          }
        }
    }

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
