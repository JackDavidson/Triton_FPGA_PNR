package pnr.actions.circuitlut

import pnr.actions.IAction
import pnr.components.circuit.CircuitLut
import pnr.fpgas.PnrState

/**
  * Created by jack on 8/29/16.
  */
class ActionIntegrateIdentityLut(circuitLut : CircuitLut) extends IAction {
  override def inverse(): IAction = {
    return new ActionExtractToIdentityLut(circuitLut.getInputs.get(0), circuitLut.getInputs.size, circuitLut.getOutputs.size)
  }

  override def perform(pnrState: PnrState): Unit = {
    throw new NotImplementedError("We have not actually yet implemented undoing this operation. Its a fairly simple task," +
      " luckily.")
  }
}
