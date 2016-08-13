package pnr;

import pnr.components.circuit.CircuitLut;
import pnr.components.circuit.ICircuitComponent;
import pnr.fpgas.CannotPlaceException;
import pnr.fpgas.DoesNotMapException;
import pnr.fpgas.Fpga;
import pnr.fpgas.tci.InternalDom;
import pnr.fpgas.tci.TritoncoreI;
import pnr.misc.Defs;
import pnr.misc.Helpers;
import pnr.tools.BliffReader;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

public class PlaceAndRoute {

  private static String swapInputs(int a, int b, String originalBits) {
    String newBits = "";
    for (int i = 0; i < 16; i++) {
      int j = ((i & (1 << a)) >> a) << b;
      int k = ((i & (1 << b)) >> b) << a;
      int l = (i & (~(1 << a)) & (~(1 << b))) | j | k;
      newBits += originalBits.charAt(l);
    }
    return newBits;
  }
  
  public static void main(String[] args) {

    Stack<Integer> numberOfAttempts = new Stack<>();
    Stack<ICircuitComponent> placedItems = new Stack<>();

    numberOfAttempts.push(0);
    InternalDom internalDom;
    ArrayList<ICircuitComponent> toPlace;
    ArrayList<ICircuitComponent> originalComponents = new ArrayList<>();
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
      toPlace = internalDom.getComponentsList();
      originalComponents.addAll(toPlace);
    }

    if (Defs.stepByStep)
      printAllComponents(originalComponents);

    boolean success = true;
    Fpga fpga = new TritoncoreI(internalDom);
    try {
      fpga.placeInitialComponentsHard();
      while (toPlace.size() != 0) {
        ICircuitComponent nextComponent = fpga.getNextItemToPlace(toPlace);
        if (nextComponent == null) {
          // just gets the next one. TODO: should be smarter, and handle
          nextComponent = toPlace.get(0);
        }
        if (toPlace == null)
          break; // done.
        try {
          fpga.makePlacement(nextComponent, numberOfAttempts.peek());
          saveState(numberOfAttempts, placedItems, nextComponent);
          toPlace.remove(nextComponent); // Remove the newly placed item from the list of items to place. TODO: optomize the choice of data structure
        } catch (CannotPlaceException e) {
          toPlace = backtrack(toPlace, numberOfAttempts, placedItems, e.getMessage());
        }
        if (numberOfAttempts.size() == 0) {
          // if we popped everything off teh number of attempts stack, we know its impossible to map.
          throw new DoesNotMapException("We tried everything we could, but we just could not get your design to map" +
                  " to the FPGA's components.");
        }
        if (Defs.stepByStep)
          printAllComponents(originalComponents);
      }
    } catch (DoesNotMapException e) {
      System.out.println("the input does not map to the fpga: " + e.getMessage());
      success = false;
    }
    if (success)
      System.out.println(fpga.getBitstream());
  }
  private static void saveState(Stack<Integer> numberOfAttempts, Stack<ICircuitComponent> placedItems,
                         ICircuitComponent justPlaced) {
    placedItems.push(justPlaced);
    numberOfAttempts.push(0);
    addedComponents.push(new ArrayList<>());
  }
  private static ArrayList<ICircuitComponent> backtrack(ArrayList<ICircuitComponent> circuitComponents,
                                                        Stack<Integer> numberOfAttempts,
                                                        Stack<ICircuitComponent> placedItems, String reason) {
    if (Defs.stepByStep) {
      System.out.println("\n\nBACKTRACK!!!\n\n" + reason);
    }

    numberOfAttempts.pop(); // pop the latest one off the stack (it failed)
    numberOfAttempts.push(numberOfAttempts.pop() + 1);
    ICircuitComponent justPlaced = placedItems.pop();
    justPlaced.unMap();
    circuitComponents.add(justPlaced);
    ArrayList<ICircuitComponent> componentsToRemove = addedComponents.pop();
    for (ICircuitComponent toRemove : componentsToRemove) {
      if (Defs.stepByStep) {
        System.out.print("removing an extra component: " + Helpers.getComponentName(toRemove) + toRemove.getId());
      }
      toRemove.unMap();
    }
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
