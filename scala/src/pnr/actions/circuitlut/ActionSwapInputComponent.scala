package pnr.actions.circuitlut

import pnr.actions.IAction
import pnr.components.circuit.ICircuitComponent
import pnr.fpgas.PnrState
import pnr.misc.{Defs, Helpers}

/**
  * Created by jack on 9/1/16.
  */
class ActionSwapInputComponent(from: ICircuitComponent, to: ICircuitComponent, on: ICircuitComponent) extends IAction {
  override def inverse(): IAction = {
    new ActionSwapInputComponent(to, from, on)
  }

  override def perform(pnrState: PnrState): Unit = {
    val replacingIdx = on.getInputs.indexOf(from)
    if (Defs.debug)
      println("replacing on input: " + replacingIdx + " (" +  Helpers.getComponentName(from) + ") for component: " + Helpers.getComponentName(on))
    to.getInputs.remove(replacingIdx)
    to.getInputs.add(replacingIdx, to)
  }
}
