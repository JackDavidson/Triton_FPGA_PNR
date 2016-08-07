package pnr;

import pnr.components.circuit.ICircuitComponent;
import pnr.fpgas.CannotPlaceException;
import pnr.fpgas.DoesNotMapException;
import pnr.fpgas.IFpga;
import pnr.fpgas.tci.InternalDom;
import pnr.fpgas.tci.TritoncoreI;
import pnr.tools.BliffReader;

import java.util.ArrayList;
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

    printAllComponents(originalComponents);

    boolean success = true;
    IFpga fpga = new TritoncoreI(internalDom);
    try {
      fpga.placeInitialComponentsHard();
      while (!fpga.isDone()) {
        ICircuitComponent nextComponent = fpga.getNextItemToPlace(toPlace);
        if (nextComponent == null)
          nextComponent = toPlace.get(numberOfAttempts.peek());
        //fpga.inferPlacements(toPlace, 0); // todo: count the number of retries
        if (toPlace == null)
          break; // done.
        try {
          fpga.makePlacement(nextComponent, 0); // todo: count of retries
          saveState(numberOfAttempts, placedItems, nextComponent);
        } catch (CannotPlaceException e) {
          toPlace = backtrack(toPlace, numberOfAttempts, placedItems);
        }
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
    placedItems.push(justPlaced);
    justPlaced.setIsPlaced(true);
  }
  private static ArrayList<ICircuitComponent> backtrack(ArrayList<ICircuitComponent> circuitComponents,
                                                        Stack<Integer> numberOfAttempts,
                                                        Stack<ICircuitComponent> placedItems) {
    numberOfAttempts.pop(); // pop the latest one off the stack (it failed)
    numberOfAttempts.push(numberOfAttempts.pop() + 1);
    ICircuitComponent justPlaced =placedItems.pop();
    justPlaced.setIsPlaced(false);
    circuitComponents.add(justPlaced);
    return circuitComponents;
  }

  private static void printAllComponents(ArrayList<ICircuitComponent> circuitComponents) {
    System.out.print("\n\n\n\n");
    for (ICircuitComponent component : circuitComponents) {
      System.out.print("[");
      System.out.print(component.isPlaced() ? "X" : " ");
      System.out.print("] ");
      System.out.print(getComponentName(component) + ": ");
      if (component.getInputs() != null) {
        for (ICircuitComponent input : component.getInputs()) {
          System.out.print(" I-" + getComponentName(input));
        }
      }
      if (component.getOutputs() != null) {
        for (ICircuitComponent input : component.getOutputs()) {
          System.out.print(" O-" + getComponentName(input));
        }
      }
      System.out.print("\n");
    }
  }

  private static String getComponentName(ICircuitComponent component) {
    int id = component.getId();
    String idPadding = id < 10 ? "__" : id < 100 ? "_" : "";
    return component.threeLetterType() + idPadding + id;
  }
}
