/*
 *  ArmPos.scala
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

import de.sciss.model.impl.ModelImpl
import de.sciss.numbers.Implicits.doubleNumberWrapper

import java.util.{Timer, TimerTask}

object ArmModel {
  def apply(init: ArmPos): ArmModel = new Impl(init)

  private final class VarImpl[A](init: A, sync: AnyRef) extends Var[A] with ModelImpl[A] {
    private var _value = init

    override def apply(): A = sync.synchronized(_value)

    override def update(value: A): Unit = {
      val change = sync.synchronized {
        (_value != value) && {
          _value = value
          true
        }
      }
      if (change) dispatch(value)
    }
  }

  private final class IntVarImpl(impl: Impl, init: Int, sync: AnyRef) extends LineVar[Int] with ModelImpl[Int] {
    private var _value        = init
    private var _startValue   = init
    private var _targetValue  = init
    private var _targetDur    = 0       // milliseconds
    private var _lineStart    = 0L      // absolute time

    override def apply(): Int = sync.synchronized(_value)

    private def stopLine(): Unit = {
      if (impl.removeAnim(this)) {
//        _targetValue = _value
      }
    }

    def animStep(): Unit = {
      val now = System.currentTimeMillis()
      val dt  = now - _lineStart
      if (dt >= _targetDur) {
        updateImpl(value = _targetValue, stop = true)
      } else {
        assert (dt >= 0)
        val v = (dt.toDouble.linLin(0, _targetDur, _startValue, _targetValue) + 0.5).toInt
        updateImpl(value = v, stop = false)
      }
    }

    override def update(value: Int): Unit =
      updateImpl(value = value, stop = true)

    private def updateImpl(value: Int, stop: Boolean): Unit = {
      val change = sync.synchronized {
        if (stop) stopLine()
        (_value != value) && {
          _value = value
          true
        }
      }
      if (change) dispatch(_value)
    }

    override def lineTo(value: Int, duration: Int): Unit = {
      require (duration > 0)
      sync.synchronized {
        stopLine()
        if (_value != value) {
          _lineStart    = System.currentTimeMillis()
          _startValue   = _value
          _targetValue  = value
          _targetDur    = duration
          impl.addAnim(this)
        }
      }
    }
  }

  private final class Impl(pos0: ArmPos) extends ArmModel {
    private val timer = new Timer("arm", true)

    private val sync = new AnyRef

    private val _base     = new IntVarImpl(this, pos0.base    , sync)
    private val _lowArm   = new IntVarImpl(this, pos0.lowArm  , sync)
    private val _highArm  = new IntVarImpl(this, pos0.highArm , sync)
    private val _ankle    = new IntVarImpl(this, pos0.ankle   , sync)
    private val _gripRota = new IntVarImpl(this, pos0.gripRota, sync)
    private val _gripOpen = new IntVarImpl(this, pos0.gripOpen, sync)

    override def base     : LineVar[Int] = _base
    override def lowArm   : LineVar[Int] = _lowArm
    override def highArm  : LineVar[Int] = _highArm
    override def ankle    : LineVar[Int] = _ankle
    override def gripRota : LineVar[Int] = _gripRota
    override def gripOpen : LineVar[Int] = _gripOpen

    override val name: Var[String] = new VarImpl("", sync = sync)

    private val _anim = new VarImpl(false, sync = sync)

    override def anim: Expr[Boolean] = _anim

    private var setAnim = Set.empty[IntVarImpl]
    private var _tt: TimerTask = null

    private def animStep(): Unit = sync.synchronized {
      setAnim.foreach { vr =>
        vr.animStep()
      }
    }

    def addAnim(vr: IntVarImpl): Unit = sync.synchronized {
      val start = setAnim.isEmpty
      setAnim += vr
      if (start) {
        assert (_tt == null)
        val tt = new TimerTask {
          override def run(): Unit = animStep()
        }
        _tt = tt
        timer.scheduleAtFixedRate(tt, 20L, 20L)
        _anim() = true
      }
    }

    def removeAnim(vr: IntVarImpl): Boolean = sync.synchronized {
      val res = setAnim.contains(vr)
      if (res) {
        setAnim -= vr
        val stop = setAnim.isEmpty
        if (stop) {
          assert (_tt != null)
          _tt.cancel()
          _tt = null
          _anim() = false
        }
      }
      res
    }

    def motors: Seq[LineVar[Int]] = Seq(
      base,
      lowArm,
      highArm,
      ankle,
      gripRota,
      gripOpen,
    )

    override def current: ArmPos = ArmPos(
      base      = base(),
      lowArm    = lowArm(),
      highArm   = highArm(),
      ankle     = ankle(),
      gripRota  = gripRota(),
      gripOpen  = gripOpen(),
    )
  }
}
trait ArmModel {
  def current: ArmPos

  def motors: Seq[LineVar[Int]]

  def base    : Var[Int]
  def lowArm  : Var[Int]
  def highArm : Var[Int]
  def ankle   : Var[Int]
  def gripRota: Var[Int]
  def gripOpen: Var[Int]

  def anim: Expr[Boolean]

  def name: Var[String]
}
