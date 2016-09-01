package pnr;

import pnr.actions.ActionUnmap;
import pnr.actions.IAction;
import pnr.components.circuit.CircuitLut;
import pnr.components.circuit.ICircuitComponent;
import pnr.fpgas.CannotPlaceException;
import pnr.fpgas.DoesNotMapException;
import pnr.fpgas.Fpga;
import pnr.fpgas.PnrState;
import pnr.fpgas.tci.InternalDom;
import pnr.fpgas.tci.TritoncoreI;
import pnr.misc.Defs;
import pnr.misc.Helpers;
import pnr.misc.Pair;
import pnr.tools.BliffReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class PlaceAndRoute {


  private static PnrState pnrState = new PnrState();

  public static void main(String[] args) {
    pnrState.numberOfAttempts.push(0);
    InternalDom internalDom;
    if (args.length != 1) {
      System.out.println("usage: java pnr [file.bliff]");
      return;
    } else {
      // 1. load from blif into a blif DOM
      BliffReader reader = new BliffReader(args[0]);
      // 2. take the resulting descriptor
      BlifDom blifDom = reader.getResultDom();
      blifDom.describeCircuit();
      // 3. get the internal representation for the items we need to place.


      internalDom = new InternalDom(blifDom);
      internalDom.printRepresentation();
      pnrState.toPlace = internalDom.getComponentsList();
      pnrState.allComponents.addAll(pnrState.toPlace);
    }

    if (Defs.stepByStep)
      printAllComponents(pnrState.allComponents);

    boolean success = true;
    Fpga fpga = new TritoncoreI(internalDom);
    try {

      Pair<Boolean, List<IAction>> initialActions = fpga.performInitialActions(pnrState.allComponents);
      do {
        if (initialActions.v != null) {
          for (IAction action : initialActions.v) {
            action.perform(pnrState);
          }
        }
        initialActions = fpga.performInitialActions(pnrState.allComponents);
      } while (initialActions.k);

      List<IAction> actionsToPerform = null;
      while (pnrState.toPlace.size() != 0 || (actionsToPerform != null && actionsToPerform.size() == 0)) {
        ICircuitComponent nextComponent = fpga.getNextItemToPlace(pnrState.toPlace);
        if (nextComponent == null) {
          // just gets the next one. TODO: should be smarter, and handle retrying in different ways
          nextComponent = pnrState.toPlace.get(0);
        }
        if (pnrState.toPlace == null)
          break; // done.
        try {
          actionsToPerform = fpga.makePlacement(nextComponent, pnrState.allComponents, pnrState.numberOfAttempts.peek());
          if (actionsToPerform != null) {
            for (IAction action : actionsToPerform) {
              performAction(pnrState.numberOfAttempts, pnrState.reversals, action);
            }
          }
        } catch (CannotPlaceException e) {
          pnrState.toPlace = backtrack(pnrState.toPlace, pnrState.numberOfAttempts, pnrState.reversals, e.getMessage());
        }
        if (pnrState.numberOfAttempts.size() == 0) {
          // if we popped everything off teh number of attempts stack, we know its impossible to map.
          throw new DoesNotMapException("We tried everything we could, but we just could not get your design to map" +
                  " to the FPGA's components.");
        }
        if (Defs.stepByStep)
          printAllComponents(pnrState.allComponents);
      }
    } catch (DoesNotMapException e) {
      System.out.println("the input does not map to the fpga: " + e.getMessage());
      success = false;
    }
    if (success)
      System.out.println(fpga.getBitstream());
  }
  private static void performAction(Stack<Integer> numberOfAttempts, Stack<IAction> reversals, IAction action) {
    reversals.push(action.inverse());
    action.perform(pnrState);
    numberOfAttempts.push(0);
  }
  private static ArrayList<ICircuitComponent> backtrack(ArrayList<ICircuitComponent> circuitComponents,
                                                        Stack<Integer> numberOfAttempts, Stack<IAction> reversals, String reason) {
    if (Defs.stepByStep) {
      System.out.println("\n\nBACKTRACK!!!\n\n" + reason);
    }

    IAction lastAction;
    do {
      lastAction = reversals.pop();
      lastAction.perform(pnrState);
      numberOfAttempts.pop(); // pop the latest one off the stack (it failed)
      numberOfAttempts.push(numberOfAttempts.pop() + 1);
    } while (lastAction.getClass() != ActionUnmap.class);

    return circuitComponents;
  }

  private static void printAllComponents(ArrayList<ICircuitComponent> circuitComponents) {
    System.out.print("\n\n\n\n");
    for (ICircuitComponent component : circuitComponents) {
      System.out.print("[");
      System.out.print(component.getPlacedOn() != null ? "X" : " ");
      System.out.print("] ");
      System.out.print(Helpers.getComponentName(component) + ": ");
      if (component.getInputs() != null) {
        for (ICircuitComponent input : component.getInputs()) {
          System.out.print(" I-" + Helpers.getComponentName(input));
        }
      }
      if (component.getOutputs() != null) {
        for (Map.Entry<Integer, ArrayList<ICircuitComponent>> input : component.getOutputs().entrySet()) {
          for (ICircuitComponent inputObj : input.getValue()) {
            System.out.print(" O-" + Helpers.getComponentName(inputObj) + Helpers.padInt(input.getKey()));
          }
        }
      }
      if (component.getClass() == CircuitLut.class) {
        CircuitLut circuitLut = (CircuitLut) component;
        circuitLut.getInitValues();
        System.out.print(" INIT_" + circuitLut.getInitValues());
      }
      System.out.print("\n");
    }
  }



  private static Stack<ArrayList<ICircuitComponent>> addedComponents = new Stack<>();
  public void addComponent(ICircuitComponent cComponent) {
    if (Defs.stepByStep) {
      System.out.print("adding an extra component: " + Helpers.getComponentName(cComponent) + cComponent.getId());
    }
    addedComponents.peek().add(cComponent);
  }
}
