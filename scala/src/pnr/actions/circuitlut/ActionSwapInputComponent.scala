package pnr.actions.circuitlut

import pnr.actions.IAction
import pnr.components.circuit.ICircuitComponent
import pnr.fpgas.PnrState

/**
  * Created by jack on 9/1/16.
  */
class ActionSwapInputComponent(from: ICircuitComponent, to: ICircuitComponent, on: ICircuitComponent) extends IAction {
  override def inverse(): IAction = {
    new ActionSwapInputComponent(to, from, on)
  }

  override def perform(pnrState: PnrState): Unit = {
    val replacingIdx = on.getInputs.indexOf(from)
    to.getInputs.remove(replacingIdx)
    to.getInputs.add(replacingIdx, to)
  }
}
