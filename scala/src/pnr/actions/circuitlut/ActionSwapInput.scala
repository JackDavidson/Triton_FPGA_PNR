package pnr.actions.circuitlut

import pnr.actions.{ActionUnmap, IAction}
import pnr.components.circuit.CircuitLut
import pnr.fpgas.PnrState

/**
  * Created by jack on 8/28/16.
  */
class ActionSwapInput(circuitLut: CircuitLut, a: Int, b: Int) extends IAction {
  override def inverse: IAction = {
    new ActionSwapInput(circuitLut, a, b)
  }

  def perform(pnrState: PnrState) = {
    circuitLut.swapInputValues(a, b);
  }
}
