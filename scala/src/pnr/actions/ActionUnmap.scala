package pnr.actions

import pnr.components.circuit.ICircuitComponent
import pnr.fpgas.PnrState

/**
  * Created by jack on 8/28/16.
  */
class ActionUnmap(circuitComponent: ICircuitComponent) extends IAction {
  val fpgaComponent = circuitComponent.getPlacedOn
  override def inverse: IAction = {
    new ActionMapTo(circuitComponent, fpgaComponent)
  }

  def perform(pnrState: PnrState) = {
    circuitComponent.mapTo(fpgaComponent)
  }
}