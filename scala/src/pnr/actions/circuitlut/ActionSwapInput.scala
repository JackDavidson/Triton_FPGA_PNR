package pnr.actions.circuitlut

import pnr.actions.{ActionUnmap, IAction}
import pnr.components.circuit.CircuitLut
import pnr.fpgas.PnrState
import pnr.misc.Helpers

/**
  * Created by jack on 8/28/16.
  */
class ActionSwapInput(circuitLut: CircuitLut, a: Int, b: Int) extends IAction {
  override def inverse: IAction = {
    new ActionSwapInput(circuitLut, a, b)
  }

  def perform(pnrState: PnrState) = {
    println("performing swap of inputs: " + a + " and " + b + " on: " + Helpers.getComponentName(circuitLut))
    circuitLut.swapInputValues(a, b);
  }

  override def toString = "for item: " + Helpers.getComponentName(circuitLut) + " we are going to swap input " + a + " and " + b
}
