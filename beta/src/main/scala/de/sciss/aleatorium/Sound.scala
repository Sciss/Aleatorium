package de.sciss.aleatorium

import de.sciss.numbers.Implicits._
import de.sciss.osc
import de.sciss.synth.{Buffer, Server, ServerConnection, Synth, SynthDef}
import de.sciss.synth.Ops._
import de.sciss.synth.ugen
import org.rogach.scallop.{ScallopConf, ScallopOption => Opt}

import java.io.File

object Sound {
  case class Config(
                   onsetThresh: Double = -12.dbAmp,
                   path: String = "/home/pi/Music/shouldhalde-210606-selva.aif",
//                   fileChannels: Int = 2,
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
      val verbose: Opt[Boolean] = opt("verbose", short = 'V', default = Some(default.verbose),
        descr = "Verbose printing."
      )
      val dumpOSC: Opt[Boolean] = opt("dump-osc", default = Some(default.dumpOSC),
        descr = "Enable SuperCollider OSC printing."
      )

      verify()
      val config: Config = Config(
        onsetThresh = onsetThresh(),
        path        = path(),
        verbose     = verbose(),
        dumpOSC     = dumpOSC(),
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
        booted(c, s)
    }
  }

  def any2stringadd: Any = ()

  def booted(c: Config, s: Server): Unit = {
    val b     = Buffer(s)
    val dn    = "play"
    val syn   = Synth(s)
    val sd = SynthDef("play") {
      import ugen._
      val in  = DiskIn.ar(1 /*c.fileChannels*/, "buf".ir, loop = 1)
      val in0 = in // .out(0)
      in0.poll(1, "test")
      val sig = in0 + WhiteNoise.ar(0.05)
      Out.ar(0, sig)
    }
    val m = b.allocMsg(32768, completion =
      b.readChannelMsg(c.path, leaveOpen = true, channels = 0 :: Nil, completion =
        sd.recvMsg(completion =
          syn.newMsg(dn, args = Seq("buf" -> b.id))
        )
      )
    )
    s ! m
  }
}
