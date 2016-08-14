package pnr.components.blif;

import pnr.BlifDom;
import pnr.components.fpga.BlifItemRepr;
import pnr.components.fpga.Element;

public class BlifDff extends Element implements BlifItemRepr {
  private static int lastId = 0;
  private int id;
  private String initValue;
  
  public BlifWire[] i = new BlifWire[1];
  public BlifWire[] o = new BlifWire[1];
  public BlifWire[] c = new BlifWire[1];
  
  public BlifDff(String c, String i, String o, BlifDom descriptorToAddTo) {
    this.id = lastId++;
    this.c[0] = descriptorToAddTo.assignWireOutput(c, this);
    this.i[0] = descriptorToAddTo.assignWireOutput(i, this);
    this.o[0] = descriptorToAddTo.assignWireInput(o, this);
  }
  
  public BlifWire[] getInputs() {
    return i;
  }
  public BlifWire[] getOutputs() {
    return o;
  }
  
  public void setInit(String value) {
    this.initValue = value;
  }
  
  public String getInit() {
    return this.initValue;
  }
  
  public String getName() {
    return "BlifDff" + this.id;
  }
}
