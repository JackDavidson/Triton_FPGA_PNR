package pnr.components.fpga;
// this class is the device speciffic class which actually performs the final place and route.

import pnr.components.GlobalInput;
import pnr.components.GlobalOutput;
import pnr.components.blif.Wire;
import pnr.components.blif.SB_LUT4;

public class TCI_LogicCell extends Element {
  private static int lastId = 0;
  private int id;
  public Boolean canMove = true;
  public Boolean isRegistered = false;
  public GlobalInput clk = null;
  public int location = 0;

  private SB_LUT4 lookup;
  private Element[] inputs = new Element[4];

  public TCI_LogicCell(SB_LUT4 lookup) {
    this.id = lastId++;
    this.lookup = lookup;
  }

  public void applyRegister(GlobalInput globalInput) {
    isRegistered = true;
    this.clk = globalInput;
  }

  // takes the generic LUT, etc. and transforms them into the TCI_LogicCell,
  // etc.
  private void performTransforms() {

  }

  public Wire[] getWireInputs() {
    return lookup.getInputs();
  }

  public String getName() {
    return "LC_" + id;
  }

  // this returns all the logic cells/global pins inputs in a string format.
  public String getTCIInputs() {
    String result = "";
    for (Element input : inputs) {
      if (input == null)
        result += " null";
      else
        result += " " + input.getName();
    }
    return result;
  }

  public void setTCIInput(int idx, Element e) {
    this.inputs[idx] = e;
  }

  public String getInitializationValue() {
    String result = lookup.getInit();
    for (int i = 0; i < 5 && result.length() < 16; i++) {
      if (i == 5) {
        System.out.println("[error] bit initialization for logic cell " + getName() + " is no good!");
      }
      result += result;
    }
    return result;
  }

  public String getOutputWire() {
    return this.lookup.o[0].getName();
  }

  public Boolean isOutputLUT() {
    for (Wire out : this.lookup.o) {
      for (Element e : out.outputs) {
        if (e.getClass() == GlobalOutput.class)
          return true;
      }
    }
    return false;
  }

  // this method swaps the inputs, updating the programming bits and the
  // (linked!) inputs
  // a and b should start counting from 0.
  public void swapInputs(int a, int b) {
    String originalBits = this.getInitializationValue();
    String newBits = "";
    for (int i = 0; i < 16; i++) {
      int j = ((i & (1 << a)) >> a) << b;
      int k = ((i & (1 << b)) >> b) << a;
      int l = (i & (~(1 << a)) & (~(1 << b))) | j | k;
      newBits += originalBits.charAt(l);
    }
    Element tmp = this.inputs[a];
    this.inputs[a] = this.inputs[b];
    this.inputs[b] = tmp;
    this.lookup.setInit(newBits);
  }

  // this simply reorganizes the
  public void moveGlobalInputTo3() {
    if (this.inputs[3] != null)
      if (this.inputs[3].getClass() == GlobalInput.class)
        return; // if the input at pos 3 is already a global input, we're done.
    for (int i = 2; i >= 0; i--) {
      if (this.inputs[i] != null) {
        if (this.inputs[i].getClass() == GlobalInput.class) {
          // if we have a global input, it needs to be at pos 3.
          swapInputs(i, 3);
          return;
        }
      }
    }
  }
}
