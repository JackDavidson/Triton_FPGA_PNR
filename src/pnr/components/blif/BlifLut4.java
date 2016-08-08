package pnr.components.blif;

import pnr.BlifDom;
import pnr.components.fpga.BlifItemRepr;
import pnr.components.fpga.Element;

public class BlifLut4 extends Element implements BlifItemRepr {
  private static int lastId = 0;
  private int id;
  private String initValue;
  
  public BlifWire[] i = new BlifWire[4];
  public BlifWire[] o = new BlifWire[1];
  
  public BlifLut4(String i0, String i1, String i2, String i3, String o, BlifDom circuitDescriptorToAddTo) {
    this.id = lastId++;
    this.i[0] = circuitDescriptorToAddTo.assignWireOutput(i0, this);
    this.i[1] = circuitDescriptorToAddTo.assignWireOutput(i1, this);
    this.i[2] = circuitDescriptorToAddTo.assignWireOutput(i2, this);
    this.i[3] = circuitDescriptorToAddTo.assignWireOutput(i3, this);
    this.o[0] = circuitDescriptorToAddTo.assignWireInput(o, this);
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
    return "LUT4_" + this.id;
  }
}
