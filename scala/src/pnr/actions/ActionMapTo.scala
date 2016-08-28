package pnr.actions

import pnr.components.circuit.ICircuitComponent
import pnr.components.fpga.IFpgaComponent
import pnr.fpgas.PnrState

/**
  * Created by jack on 8/28/16.
  */
class ActionMapTo(circuitComponent: ICircuitComponent, fpgaComponent: IFpgaComponent) extends IAction {
  override def inverse: IAction = {
    new ActionUnmap(circuitComponent)
  }

  def perform(pnrState: PnrState) = {
    circuitComponent.mapTo(fpgaComponent)
    pnrState.toPlace.remove(circuitComponent) // Remove the newly placed item from the list of items to place.
  }
}