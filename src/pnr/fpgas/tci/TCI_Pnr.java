package pnr.fpgas.tci;

// this class is the device speciffic class which actually performs the final place and route.
import pnr.CircuitDescriptor;
import pnr.components.GlobalInput;
import pnr.components.blif.Wire;
import pnr.components.blif.SB_DFF;
import pnr.components.blif.SB_LUT4;
import pnr.components.fpga.Gate;

import java.util.*;

public class TCI_Pnr {
  TCI_Descriptor descriptor;
  HashMap<String, Wire> wireLookup;
  ArrayList<Gate> gates;

  public TCI_Pnr(CircuitDescriptor descriptor) {
    this.wireLookup = descriptor.getWireLookup();
    this.gates = descriptor.getGateList();
  }

  // takes the generic LUT, etc. and transforms them into the TCI_LogicCell,
  // etc.
  public void performTransforms() {
    descriptor = new TCI_Descriptor();

    for (Wire wire : wireLookup.values()) { // add the global inputs
      if (wire.input == null) {
        System.out.println("[warn] assuming false for wire: " + wire.getName());
        continue;
      }
      if (wire.input.getClass() == GlobalInput.class) {
        descriptor.add(wire.getName(), (GlobalInput) wire.input);
      }
    }

    ArrayList<SB_DFF> dffs = new ArrayList<SB_DFF>();
    for (Gate gate : gates) { // add the LUT4s
      System.out.println("converting gate: " + gate.getOutputs()[0].getName());
      if (gate.getClass() == SB_DFF.class) {
        System.out.println("its a DFF: " + gate.getOutputs()[0].getName() + " delaying.");
        dffs.add((SB_DFF) gate);
      } else if (gate.getClass() == SB_LUT4.class) {
        System.out.println("its a LUT4: " + gate.getOutputs()[0].getName() + " adding to gate list.");
        descriptor.add((SB_LUT4) gate);
      }
    }

    for (SB_DFF dff : dffs) { // add the DFFs
      descriptor.add(dff);
    }

  }

  public void resolveConnections() {
    descriptor.assignDirectLogicConnections();
    descriptor.makePreOptimizations();
  }

  public void place() {
    // placement is a breadth-first search from output to input.
    // this algorithm is NOT optimal, though it is probably possible to make
    // some modifications so that it will be.

    // first thing we need to do is find the list of most-used global inputs. We
    // will then, in order:
    // 1. place this input on the MSB of all LUTs which use it, if possible
    // 2. if not possible, place it on the next MSB by duplicating, if possible
    // (repeat this step as many times as we can)
    // 3. if that is also not possible, give up and just duplicate the input as
    // many times as we need.
    // repeat from #1 for all the rest of the global inputs, in order of most used to least used.

  }

  public void printBitstream() {
    System.out.println(descriptor.describeUnlinkedLogicCells());
    System.out.println(descriptor.describeLinkedLogicCells());
  }

  // this just places the output logic cells on Tritoncore-I
  public void placeHardcodedNodes() {
    descriptor.placeOutputs();
  }
}
