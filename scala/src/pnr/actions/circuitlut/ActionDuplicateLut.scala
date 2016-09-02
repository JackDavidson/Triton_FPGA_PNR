package pnr.actions.circuitlut

import pnr.actions.IAction
import pnr.components.circuit.CircuitLut
import pnr.fpgas.PnrState

/**
  * Created by jack on 9/1/16.
  */
class ActionDuplicateLut(circuitLut: CircuitLut) extends IAction {
  val lutToBeCreated = new CircuitLut("01", circuitLut.getInputs, circuitLut.getOutputs)

  override def inverse(): IAction = {
    return null // TODO, or maybe just do nothing
  }

  override def perform(pnrState: PnrState): Unit = {


    pnrState.allComponents.add(lutToBeCreated)
    pnrState.toPlace.add(lutToBeCreated)
  }
}
