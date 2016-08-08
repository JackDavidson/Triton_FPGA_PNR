package pnr.fpgas.tci;

// this class is the device speciffic class which actually performs the final place and route.
import pnr.BlifDom;
import pnr.components.GlobalInput;
import pnr.components.GlobalOutput;
import pnr.components.blif.BlifDff;
import pnr.components.blif.BlifWire;
import pnr.components.blif.BlifLut4;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.BlifItemRepr;
import pnr.components.fpga.Element;
import pnr.misc.Defs;

import java.util.*;

public class InternalDom {
  InternalDescriptor descriptor;
  HashMap<String, BlifWire> wireLookup;
  ArrayList<BlifItemRepr> gates;

  public InternalDom(BlifDom descriptor) {
    this.wireLookup = descriptor.getWireLookup();
    this.gates = descriptor.getGateList();

    performTransforms();
    resolveConnections();
    placeHardcodedNodes();
  }

  // takes the generic LUT, etc. and transforms them into the TCI_LogicCell,
  // etc.
  public void performTransforms() {
    descriptor = new InternalDescriptor();

    for (BlifWire wire : wireLookup.values()) { // add the global inputs
      if (wire.input == null) {
        System.out.println("[warn] assuming false for wire: " + wire.getName());
        descriptor.addFalse(wire.getName());
        continue;
      }
      if (wire.input.getClass() == GlobalInput.class) {
        descriptor.add(wire.getName(), (GlobalInput) wire.input);
      }
      if (wire.outputs.size() > 0) {
        for (Element wireOutput : wire.outputs) {
          if (wireOutput.getClass() == GlobalOutput.class) {
            if (Defs.debug) {
              System.out.println("In InternalDom, performTransforms. Found GlobalOutput. ");
            }
            descriptor.add(wire.getName(), (GlobalOutput) wireOutput);
          }
        }
      }
    }

    for (BlifItemRepr gate : gates) { // add the LUT4s
      if (Defs.debug) {
        System.out.println("converting gate: " + gate.getClass().getName());
      }
      if (gate.getClass() == BlifDff.class) {
        if (Defs.debug) {
          System.out.println("its a DFF: adding to internal DOM.");
        }

        descriptor.add((BlifDff) gate);
      } else if (gate.getClass() == BlifLut4.class) {

        if (Defs.debug) {
          System.out.println("its a LUT4: adding to InternalDom.");
        }

        descriptor.add((BlifLut4) gate);

        if (Defs.debug) {
          System.out.println("the lut4 has: " + ((BlifLut4) gate).getInputs().length + " inputs");
          for (BlifWire w : ((BlifLut4) gate).getInputs()) {
            System.out.print(" " + w.getName());
          }
          System.out.print("\n");
          System.out.println("the lut4 has: " + ((BlifLut4) gate).getOutputs().length + " outputs");
          for (BlifWire w : ((BlifLut4) gate).getOutputs()) {
            System.out.print(" " + w.getName());
          }
          System.out.print("\n");
        }
      }
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

  public void printRepresentation() {
    System.out.println(descriptor.describeUnlinkedLogicCells());
    System.out.println(descriptor.describeLinkedLogicCells());
  }

  // this just places the output logic cells on Tritoncore-I
  public void placeHardcodedNodes() {
    descriptor.placeOutputs();
  }

  public ArrayList<ICircuitComponent> getComponentsList() {
    return descriptor.getComponentsList();
  }
}
