package pnr.actions.circuitlut

import pnr.actions.IAction
import pnr.components.GlobalFalseConst
import pnr.components.circuit.{CircuitLut, ICircuitComponent}
import pnr.fpgas.PnrState
import pnr.misc.{Defs, Helpers}

import scala.collection.JavaConverters._



/**
  * Created by jack on 8/29/16.
  *
  * this action takes a circuit component and extracts it to an 'identity' LUT wherever it is found as an input to any
  * LUTS in the curcuit that is getting mapped. Note that this results in the expansion of the number of LUTS that you
  * will need to map
  *
  * @param inputs the number of inputs which the newly created LUT will have
  * @param outputs the number of outputs which the newly created LUT will have (must be at least one)
  */
class ActionExtractToIdentityLut(curcuitComponentToDuplicate: ICircuitComponent, inputs: Int, outputs: Int) extends IAction {

  assert(outputs > 0, "You can't have 0 or less outputs to the LUT that you are requesting to be created.")

  val inputsToNewLut: List[ICircuitComponent] = curcuitComponentToDuplicate :: List.fill(inputs - 1)(GlobalFalseConst.get())

  val outputsToNewLut = curcuitComponentToDuplicate.getOutputs

  val lutToBeCreated = new CircuitLut("01", inputsToNewLut.asJava, outputsToNewLut)

  override def inverse: IAction = {
    // TODO: actually implement the inverse!!!!
    return new ActionIntegrateIdentityLut(lutToBeCreated)
  }

  override def perform(pnrState: PnrState): Unit = {
    for (component <- pnrState.allComponents.asScala) {
      // first, find the items that we want to replace with our duplication
      val itemsToReplace = component.getInputs.asScala.zipWithIndex.filter(_._1 == curcuitComponentToDuplicate)
      // then, make the swaps
      for (itemToReplace <- itemsToReplace) {
        if (Defs.debug) {
          println("on item: " + Helpers.getComponentName(component) + " we are replacing input: "
            + Helpers.getComponentName(itemToReplace._1) + " with: " + Helpers.getComponentName(lutToBeCreated))

          println("also, on item: " + Helpers.getComponentName(component.getInputs.get(itemToReplace._2)) + " we are replacing output: "
            + Helpers.getComponentName(component) + " with: " + Helpers.getComponentName(lutToBeCreated))
        }
        val outputListForComponentWeAreReplacing = component.getInputs.get(itemToReplace._2).getOutputs
        val entryToReplace = outputListForComponentWeAreReplacing.entrySet().asScala.find(_.getValue.contains(component))
        entryToReplace match {
          case Some(entryWithIndex) => val outputsListToRemoveFrom = outputListForComponentWeAreReplacing.get(entryWithIndex.getKey)
            outputsListToRemoveFrom.remove(component)
            outputsListToRemoveFrom.add(lutToBeCreated)
          case None => throw new Error("we failed to find the component: " + Helpers.getComponentName(component) +
          " on the list of outputs for one of its component listed as an input. The component listed as input is: "
            + Helpers.getComponentName(itemToReplace._1) + " This reveals a bug in the matrix.")
        }

        component.getInputs.remove(itemToReplace._2)
        component.getInputs.add(itemToReplace._2, lutToBeCreated)
      }
    }
    pnrState.allComponents.add(lutToBeCreated)
    pnrState.toPlace.add(lutToBeCreated)
  }
}
