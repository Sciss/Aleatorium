package de.sciss.aleatorium

import de.sciss.numbers.Implicits._
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
      val verbose: Opt[Boolean] = opt("verbose", short = 'V', default = Some(false),
        descr = "Verbose printing."
      )

      verify()
      val config: Config = Config(
        onsetThresh = onsetThresh(),
        path        = path(),
        verbose     = verbose(),
      )
    }

    run(p.config)
  }

  def run(c: Config): Unit = {
    val sCfg = Server.Config()
    sCfg.inputBusChannels   = 1
    sCfg.outputBusChannels  = 1
    Server.boot(config = sCfg) {
      case ServerConnection.Running(s) =>
        booted(c, s)
    }
  }

  def booted(c: Config, s: Server): Unit = {
    val b     = Buffer(s)
    val dn    = "play"
    val syn   = Synth(s)
    val sd = SynthDef("play") {
      import ugen._
      val in  = DiskIn.ar(1 /*c.fileChannels*/, "buf".ir, loop = 1)
      val in0 = in // .out(0)
      val sig = in0
      PhysicalOut.ar(0, sig)
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
