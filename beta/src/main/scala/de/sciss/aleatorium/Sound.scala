package de.sciss.aleatorium

import de.sciss.audiofile.AudioFile
import de.sciss.numbers.Implicits._
import de.sciss.osc
import de.sciss.synth.{Buffer, Server, ServerConnection, Synth, SynthDef}
import de.sciss.synth.Ops._
import de.sciss.synth.message.Responder
import de.sciss.synth.ugen
import de.sciss.synth.message
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.io.File

object Sound {
  case class Config(
                   onsetThresh: Double = -6.dbAmp,
                   path: String = "/home/pi/Music/shouldhalde-210606-selva.aif",
//                   fileChannels: Int = 2,
                   gain: Double = +12.dbAmp,
                   limiter: Boolean = true,
                   verbose: Boolean = false,
                   dumpOSC: Boolean = false,
                   )

  def main(args: Array[String]): Unit = {
    object p extends ScallopConf(args) {
      printedName = "Light"
      private val default = Config()

      val onsetThresh: Opt[Double] = opt(
        name    = "thresh",
        default = Some(default.onsetThresh),
        descr = s"Onset detection threshold (default: ${default.onsetThresh}).",
      )
      val path: Opt[String] = opt(
        name    = "path",
        default = Some(default.path),
        descr   = s"Sound file path (default: ${default.path}).",
        validate = x => new File(x).isFile,
      )
//      val fileChannels: Opt[Boolean] = opt("channels", default = Some(default.fileChannels),
//        descr = "Verbose printing."
//      )
      val verbose: Opt[Boolean] = opt("verbose", short = 'V', default = Some(default.verbose),
        descr = "Verbose printing."
      )
      val dumpOSC: Opt[Boolean] = opt("dump-osc", default = Some(default.dumpOSC),
        descr = "Enable SuperCollider OSC printing."
      )
      val limiter: Opt[Boolean] = opt("limiter", default = Some(default.limiter),
        descr = s"Volume limiter (default: ${default.limiter})."
      )
      val gain: Opt[Double] = opt("gain", default = Some(default.gain),
        descr = s"Volume boost factor (default: ${default.gain})."
      )
      verify()
      val config: Config = Config(
        onsetThresh = onsetThresh(),
        path        = path(),
        verbose     = verbose(),
        dumpOSC     = dumpOSC(),
        gain        = gain(),
        limiter     = limiter(),
      )
    }

    run(p.config)
  }

  def run(c: Config): Unit = {
    val sCfg = Server.Config()
    sCfg.transport          = osc.TCP
    sCfg.inputBusChannels   = 1
    sCfg.outputBusChannels  = 1
    sCfg.pickPort()
    Server.boot(config = sCfg) {
      case ServerConnection.Running(s) =>
        if (c.dumpOSC) s.dumpOSC()
        booted(c, s) {
          println("Bang!")
        }
    }
  }

  def any2stringadd: Any = ()

  def booted(c: Config, s: Server)(onTrig: => Unit): Unit = {
    val spec  = AudioFile.readSpec(c.path)
    val fileChannels = spec.numChannels
    val b     = Buffer(s)
    val dn    = "play"
    val syn   = Synth(s)
    val sd = SynthDef("play") {
      import ugen._

      // generator
      val in  = DiskIn.ar(/*1*/ fileChannels, "buf".ir, loop = 1)
      val in0 = in.out(0)
      if (c.dumpOSC) in0.poll(1, "test")
      val lvl = in0 * "amp".kr(1.0)
      val sig = if (c.limiter) Limiter.ar(lvl) else lvl // + WhiteNoise.ar(0.05)
      Out.ar(0, sig)

      // analysis
      val mic     = In.ar(NumOutputBuses.ir)
//      val peakTr  = Impulse.kr(10)
//      val peak    = Peak.kr(mic, peakTr)
//      peak.ampDb.poll(peakTr, "peak")
      val thresh  = "thresh".kr
      val loud    = Trig.ar(mic.abs > thresh, dur = 1.0)
      if (c.dumpOSC) mic.poll(loud, "mic")
      SendTrig.ar(loud)
    }
    val m = b.allocMsg(numFrames = 32768, numChannels = fileChannels, completion =
      b.readMsg /*readChannelMsg*/(c.path, leaveOpen = true, /*channels = 0 :: Nil,*/ completion =
        sd.recvMsg(completion =
          syn.newMsg(dn, args = Seq("buf" -> b.id, "amp" -> c.gain, "thresh" -> c.onsetThresh))
        )
      )
    )
    s ! m

    Responder.add(s) {
      case message.Trigger(syn.id, _, _) =>
        onTrig
    }
  }
}
