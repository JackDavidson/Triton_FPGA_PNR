package pnr;
import pnr.components.blif.WireFactory;
import pnr.components.fpga.GateFactory;

public class CircuitDescriptor {
  
  public static String describeCircuit() {
    String result = "";
    result += WireFactory.describeWires();
    result += "\n\n";
    result += GateFactory.describe();
    return result;
  }
  
}
