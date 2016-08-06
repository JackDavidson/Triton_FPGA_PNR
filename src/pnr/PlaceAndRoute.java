package pnr;

import pnr.components.blif.WireFactory;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.GateFactory;
import pnr.components.fpga.IFpgaComponent;
import pnr.fpgas.CannotPlaceException;
import pnr.fpgas.DoesNotMapException;
import pnr.fpgas.IFpga;
import pnr.fpgas.tci.TCI_Pnr;
import pnr.fpgas.tci.TritoncoreI;
import pnr.tools.BliffReader;

import java.util.ArrayList;

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
    BliffReader reader = null;
    // Prints "Hello, World" to the terminal window.
    if (args.length != 1) {
      System.out.println("usage: java pnr [file.bliff]");
      return;
    } else {
      reader = new BliffReader(args[0]);
    }


    boolean success = true;
    TCI_Pnr TciPnr = new TCI_Pnr(reader.getResultDescriptor());
    IFpga fpga = new TritoncoreI(TciPnr);
    try {
      ArrayList<ICircuitComponent> toPlace = reader.getResultDescriptor().getElements();
      ArrayList<IFpgaComponent> fpgaComponents = fpga.getComponents();
      fpga.placeInitialComponentsHard();
      while (!fpga.isDone()) {
        if (!fpga.orderPlacements(toPlace))
          defaultOrderPlacements(toPlace);
        fpga.inferPlacements(toPlace, 0); // todo: count the number of retries
        ICircuitComponent nextComponent = getNextItemToPlace(toPlace);
        if (toPlace == null)
          break; // done.
        try {
          fpga.makePlacement(nextComponent, 0); // todo: count of retries
        } catch (CannotPlaceException e) {
          toPlace = backtrack(fpgaComponents);
        }
      }
    } catch (DoesNotMapException e) {
      System.out.println("the input does not map to the fpga: " + e.getMessage());
      success = false;
    }
    if (success)
      System.err.println(fpga.getBitstream());
  }

  private void saveState(ArrayList<ICircuitComponent> components, ArrayList<IFpgaComponent> fpgaComponents) {
    // todo: save the state
  }
  private static ArrayList<ICircuitComponent> backtrack(ArrayList<IFpgaComponent> fpgaComponents) {
    // todo: back track
    return new ArrayList<ICircuitComponent>();
  }
  private static void defaultOrderPlacements(ArrayList<ICircuitComponent> placements) {
    // TODO: perform ordering
  }
  private static ICircuitComponent getNextItemToPlace(ArrayList<ICircuitComponent> toPlace) {
    if (toPlace.size() == 0)
      return null;
    else {
      for (ICircuitComponent component : toPlace) {
        if (!component.isPlaced()) {
          return component;
        }
      }
    }
    return null;
  }
}
