package pnr.components.blif;

import pnr.components.fpga.Element;
import pnr.components.fpga.Gate;

public class SB_LUT4 extends Element implements Gate {
  private static int lastId = 0;
  private int id;
  private String initValue;
  
  public Wire[] i = new Wire[4];
  public Wire[] o = new Wire[1];
  
  public SB_LUT4(String i0, String i1, String i2, String i3, String o) {
    this.id = lastId++;
    this.i[0] = WireFactory.assignWireOutput(i0, this);
    this.i[1] = WireFactory.assignWireOutput(i1, this);
    this.i[2] = WireFactory.assignWireOutput(i2, this);
    this.i[3] = WireFactory.assignWireOutput(i3, this);
    this.o[0] = WireFactory.assignWireInput(o, this);
  }
  
  public Wire[] getInputs() {
    return i;
  }
  public Wire[] getOutputs() {
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
