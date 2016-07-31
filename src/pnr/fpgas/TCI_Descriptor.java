package pnr.fpgas;

// this class is the device speciffic class which actually performs the final place and route.
import pnr.components.fpga.Element;
import pnr.components.GlobalInput;
import pnr.components.fpga.TCI_LogicCell;
import pnr.components.blif.Wire;
import pnr.components.blif.SB_DFF;
import pnr.components.blif.SB_LUT4;

import java.util.*;

public class TCI_Descriptor {
  private TCI_LogicCell[] cells = new TCI_LogicCell[240];

  // in this hashMap, logic cells are organized by the names of their outputs
  private HashMap<String, TCI_LogicCell> unplacedLogicCells = new HashMap<String, TCI_LogicCell>();
  private HashMap<String, GlobalInput> inputs = new HashMap<String, GlobalInput>();

  public TCI_Descriptor() {

  }

  public void add(SB_LUT4 obj) {
    TCI_LogicCell newCell = new TCI_LogicCell(obj);
    unplacedLogicCells.put(obj.o[0].getName(), newCell);
  }

  public void add(SB_DFF obj) {
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

  // this takes all the logic cells, and points them to each other.
  public void linkLogicCells() {

  }

  public String describeUnlinkedLogicCells() {
    String result = "-----LOGIC CELLS (unlinked)-----\n\n";
    result += "logicCell: [output] [in0] [in1] [in2] [in3]\n\n";

    for (Map.Entry<String, TCI_LogicCell> entry : unplacedLogicCells.entrySet()) {
      result += "\nlogicCell: " + entry.getKey();
      for (Wire wire : entry.getValue().getWireInputs()) {
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

  public void add(String wireName, GlobalInput wire) {
    inputs.put(wireName, wire);
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
      Wire[] wireInputs = lc.getWireInputs();
      for (int i = 0; i < 4; i++) { // go through the inputs, and route them to the locic cells
        Element correspondingElem = this.findByInputName(wireInputs[i].getName());
        lc.setTCIInput(i, correspondingElem);
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
}
