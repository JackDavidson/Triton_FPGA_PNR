package pnr;
public class SB_DFF extends Element implements Gate {
  private static int lastId = 0;
  private int id;
  private String initValue;
  
  public Wire[] i = new Wire[1];
  public Wire[] o = new Wire[1];
  public Wire[] c = new Wire[1];
  
  public SB_DFF(String c, String i, String o) {
    this.id = lastId++;
    this.c[0] = WireFactory.assignWireOutput(c, this);
    this.i[0] = WireFactory.assignWireOutput(i, this);
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
    return "SB_DFF" + this.id;
  }
}