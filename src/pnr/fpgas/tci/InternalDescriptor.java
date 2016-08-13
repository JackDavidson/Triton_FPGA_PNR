package pnr.fpgas.tci;

// this class is the device speciffic class which actually performs the final place and route.
import pnr.components.GlobalFalseConst;
import pnr.components.GlobalOutput;
import pnr.components.circuit.CircuitFf;
import pnr.components.circuit.CircuitLut;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.Element;
import pnr.components.GlobalInput;
import pnr.components.fpga.BlifItemRepr;
import pnr.components.fpga.TCI_LogicCell;
import pnr.components.blif.BlifWire;
import pnr.components.blif.BlifDff;
import pnr.components.blif.BlifLut4;
import pnr.misc.Defs;

import java.util.*;

public class InternalDescriptor {
  private TCI_LogicCell[] cells = new TCI_LogicCell[240];

  // in this hashMap, logic cells are organized by the names of their outputs
  private HashMap<String, TCI_LogicCell> unplacedLogicCells = new HashMap<>();
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

    TCI_LogicCell newCell = new TCI_LogicCell(obj);
    unplacedLogicCells.put(obj.o[0].getName(), newCell);
  }

  public void add(BlifDff obj) {


    CircuitFf flipflop = new CircuitFf();
    copyToInternalRep(obj, flipflop);

    // when we recieve a DFF, we add it to the corresponding lookup table, and
    // update that lookup table to the flipflop value
    String sbInput = obj.i[0].getName();
    TCI_LogicCell correspondingCell = unplacedLogicCells.get(sbInput);
    unplacedLogicCells.remove(sbInput);
    correspondingCell.applyRegister(inputs.get(obj.c[0].getName()));
    if (!inputs.containsKey(obj.c[0].getName())) {
      System.out.println("[error] clock not set off of a global pin");
    }
    unplacedLogicCells.put(obj.o[0].getName(), correspondingCell); // re-insert
                                                                   // the logic
                                                                   // cell, with
                                                                   // its new
                                                                   // name.
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

  public String describeUnlinkedLogicCells() {
    String result = "-----LOGIC CELLS (unlinked)-----\n\n";
    result += "logicCell: [output] [in0] [in1] [in2] [in3]\n\n";

    for (Map.Entry<String, TCI_LogicCell> entry : unplacedLogicCells.entrySet()) {
      result += "\nlogicCell: " + entry.getKey();
      for (BlifWire wire : entry.getValue().getWireInputs()) {
        result += " " + wire.getName();
      }
    }
    return result;
  }

  public String describeLinkedLogicCells() {
    String result = "-----LOGIC CELLS (linked)-----\n\n";
    result += "[placed] [name]: [in0] [in1] [in2] [in3] [initializationValues] [isReg:reg] Optional[globalOut]\n\n";
    for (Map.Entry<String, TCI_LogicCell> entry : unplacedLogicCells.entrySet()) {
      result += "\n";

      if (entry.getValue().canMove == false) {
        result += "[X] ";
      } else {
        result += "[ ] ";
      }

      result += entry.getValue().getName() + ":"; // get this LC name
      result += " " + entry.getValue().getTCIInputs(); // get the names of all
                                                       // its inputs
      result += " " + entry.getValue().getInitializationValue();
      if (entry.getValue().isRegistered) {
        if (entry.getValue().clk != null) {
          result += " T:" + entry.getValue().clk.getName();
        } else {
          result += " T:null";
        }
      } else {
        result += " F:null";
      }

      if (entry.getValue().isOutputLUT()) {
        result += " " + entry.getValue().getOutputWire();
      }
    }
    return result;
  }

  private static final GlobalFalseConst falseConstanctVal = new GlobalFalseConst();
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

  private Element findByInputName(String name) {
    TCI_LogicCell correspondingLC = this.unplacedLogicCells.get(name);
    if (correspondingLC != null)
      return correspondingLC;

    GlobalInput gbi = this.inputs.get(name);
    if (gbi != null)
      return gbi;

    System.out.println("[warn] did not find match in descriptor global inputs or cells for input: " + name);

    return null;

  }

  /*
   * this method resolves the input connections in each logic cell
   */
  public void assignDirectLogicConnections() {
    for (TCI_LogicCell lc : unplacedLogicCells.values()) {
      BlifWire[] wireInputs = lc.getWireInputs();
      for (int i = 0; i < 4; i++) { // go through the inputs, and route them to the locic cells
        Element correspondingElem = this.findByInputName(wireInputs[i].getName());
        lc.setTCIInput(i, correspondingElem);
      }
    }

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
  
  public void makePreOptimizations() {
    for (TCI_LogicCell lc : unplacedLogicCells.values()) {
      lc.moveGlobalInputTo3();
    }
  }

  // places the logic cells which will be out putputs
  public void placeOutputs() {
    int i = 0;
    for (Map.Entry<String, TCI_LogicCell> entry : unplacedLogicCells.entrySet()) {
      if (entry.getValue().isOutputLUT()) {
        cells[i++] = entry.getValue();
        entry.getValue().canMove = false; // can't move any more, because its tethered to output
        if (i > 15) {
          System.out.println("[error] there are more than 16 outputs!");
        }
      }
    }
  }

  public ArrayList<ICircuitComponent> getComponentsList() {
    return itemsToPlace;
  }
}
