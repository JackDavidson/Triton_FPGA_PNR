package pnr.fpgas.tci;

// this class is the device speciffic class which actually performs the final place and route.

import pnr.components.GlobalFalseConst;
import pnr.components.GlobalInput;
import pnr.components.GlobalOutput;
import pnr.components.blif.BlifDff;
import pnr.components.blif.BlifLut4;
import pnr.components.blif.BlifWire;
import pnr.components.circuit.CircuitFf;
import pnr.components.circuit.CircuitLut;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.BlifItemRepr;
import pnr.components.fpga.Element;
import pnr.misc.Defs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class InternalDescriptor {
  private HashMap<String, GlobalInput> inputs = new HashMap<>(); // TODO: delete?


  private ArrayList<ICircuitComponent> itemsToPlace = new ArrayList<>();

  // wire -> outputs
  private TreeMap<String, ArrayList<ICircuitComponent>> wireOutLookUp = new TreeMap<>();
  // wire -> driver
  private TreeMap<String, ICircuitComponent> wireDriveLookUp = new TreeMap<>();

  private HashMap<ICircuitComponent, ArrayList<String>> circuitToInputs = new HashMap<>();
  private HashMap<ICircuitComponent, ArrayList<String>> circuitToOutputs = new HashMap<>();


  public void add(BlifLut4 obj) {

    CircuitLut lut = new CircuitLut(obj.getInit());
    copyToInternalRep(obj, lut);

  }

  public void add(BlifDff obj) {


    CircuitFf flipflop = new CircuitFf();
    copyToInternalRep(obj, flipflop);

    // when we recieve a DFF, we add it to the corresponding lookup table, and
    // update that lookup table to the flipflop value
  }

  private void copyToInternalRep(BlifItemRepr obj, ICircuitComponent component) {

    if (Defs.debug) {
      System.out.println("inside InternalDescriptor.copyToInternalRep.");
    }
    // make a copy with the internal representation.
    itemsToPlace.add(component);

    BlifWire[] blifouts = obj.getOutputs();
    ArrayList<String> objOutputStrs = new ArrayList<>();
    circuitToOutputs.put(component, objOutputStrs);
    if (blifouts != null) {
      // first, outputs from the component are what drive the wires

      if (Defs.debug) {
        System.out.println("found output wires. count: " + blifouts.length);
      }
      for (BlifWire output : blifouts) {
        String outputName = output.getName();
        objOutputStrs.add(outputName);
        if (Defs.debug) {
          System.out.println("wire: " + outputName + " driven by: " + component.getClass().getName());
        }
        wireDriveLookUp.put(outputName, component);
      }
    }
    BlifWire[] blifIns = obj.getInputs();

    if (Defs.debug)
      System.out.println("Copying to internal representation: " + obj.getClass().getName());

    ArrayList<String> objInputStrs = new ArrayList<>();
    circuitToInputs.put(component, objInputStrs);
    if (blifIns != null) {
      if (Defs.debug) {
        System.out.println("found input wires. count: " + blifIns.length);
      }
      // first, outputs from the component are what drive the wires
      for (BlifWire in : blifIns) {
        ArrayList<ICircuitComponent> existingWireOutputs = wireOutLookUp.get(in.getName());
        String inputName = in.getName();
        objInputStrs.add(inputName);
        if (existingWireOutputs == null) {
          existingWireOutputs = new ArrayList<>();
          wireOutLookUp.put(inputName, existingWireOutputs);
        }
        if (Defs.debug) {
          System.out.println("wire: " + inputName + " route to: " + component.getClass().getName());
        }
        existingWireOutputs.add(component); // add this flipflop to the list of items driven by the wire.
      }
    }
  }


  private static final GlobalFalseConst falseConstanctVal = GlobalFalseConst.get();
  public void addFalse(String wireName) {
    if (Defs.warn) {
      if ((wireDriveLookUp.get(wireName) != null)) {
        if (wireDriveLookUp.get(wireName) != falseConstanctVal)
          System.out.print("[warn] the wire " + wireName + " will be reassigned to false from: "
                  + wireDriveLookUp.get(wireName).getClass().getName());
      }
    }
    wireDriveLookUp.put(wireName, falseConstanctVal);
  }

  public void add(String wireName, GlobalInput globalInput) {
    if (Defs.debug) {
      System.out.println("adding global: " + wireName);
    }
    itemsToPlace.add(globalInput);
    wireDriveLookUp.put(wireName, globalInput);
    inputs.put(wireName, globalInput);

    ArrayList<String> outputWireNames = circuitToOutputs.get(globalInput);
    if (outputWireNames == null) {
      outputWireNames = new ArrayList<>();
      circuitToOutputs.put(globalInput, outputWireNames);
    }
    outputWireNames.add(wireName);

  }

  public void add(String wireName, GlobalOutput globalOutput) {
    if (Defs.debug) {
      System.out.println("adding global output: " + wireName);
    }
    ArrayList<String> inputStrs = new ArrayList<>();
    inputStrs.add(wireName);
    circuitToInputs.put(globalOutput, inputStrs);
    itemsToPlace.add(globalOutput);
    addWireOutput(wireName, globalOutput);
  }

  private void addWireOutput(String wireName, ICircuitComponent component) {
    ArrayList<ICircuitComponent> wireOutputsSoFar = wireOutLookUp.get(wireName);
    if (wireOutputsSoFar == null) {
      wireOutputsSoFar = new ArrayList<>();
      wireOutLookUp.put(wireName, wireOutputsSoFar);
    }
    wireOutputsSoFar.add(component);
  }


  /*
   * this method resolves the input connections in each logic cell
   */
  public void assignDirectLogicConnections() {

    for (ICircuitComponent item : itemsToPlace) {
      if (Defs.debug)
        System.out.println("adding inputs on item: " + item.getClass().getName());
      ArrayList<String> wireInputNames = circuitToInputs.get(item);
      if (wireInputNames != null) {
        for (String wireInputName : circuitToInputs.get(item)) {
          if (Defs.debug) {
            System.out.println("adding input to: " + item.getClass().getName() + " wire: " + wireInputName);
            System.out.println("found: " + wireDriveLookUp.get(wireInputName));
          }
          item.addInput(wireDriveLookUp.get(wireInputName));
        }
      }
      ArrayList<String> outputWireNames = circuitToOutputs.get(item);
      if (outputWireNames != null) {
        for (int i = 0; i < outputWireNames.size(); i++) { // normally will only be one item, but more for mult units/adders
          String outputWireName = outputWireNames.get(i);
          if (Defs.debug) {
            System.out.print("About to look for wire's outputs: " + outputWireName);
          }
          for (ICircuitComponent outputComponent : wireOutLookUp.get(outputWireName)) {
            if (Defs.debug) {
              System.out.println("adding output to: " + item.getClass().getName() + " wire: " + outputWireName);
            }
            item.addOutput(i, outputComponent);
          }
        }
      }
    }
  }



  public ArrayList<ICircuitComponent> getComponentsList() {
    return itemsToPlace;
  }
}
