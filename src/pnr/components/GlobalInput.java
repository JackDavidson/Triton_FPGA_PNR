package pnr.components;

import pnr.components.fpga.Element;

//global inputs can be a source for wires, but not a destination.
public class GlobalInput extends Element {
  private static int lastAssignedNumber = 0;
  public int pinNumber;
  
  public String getName() {
    return "GI_" + pinNumber;
  }
  
  public GlobalInput() {
    this.pinNumber = lastAssignedNumber++;
  }
}
