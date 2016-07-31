package pnr;
import java.util.*;

public class WireFactory {
  public static HashMap<String, Wire> wireLookup = new HashMap<String, Wire>();
  
  public static Wire assignWireInput(String name, Element input) {
    Wire wire = wireLookup.get(name);
    if (wire == null) {
      wire = new Wire(name);
      wireLookup.put(name, wire);
    }
    if (wire.input != null) {
      System.out.println("[warn] wire " + name + " alredy has an input defined!");
    }
    wire.input = input;
    //System.out.println("defined new wire input : " + name);
    return wire;
  }
  
  public static Wire assignWireOutput(String name, Element output) {
    Wire wire = wireLookup.get(name);
    if (wire == null) {
      wire = new Wire(name);
      wireLookup.put(name, wire);
    }
    wire.outputs.add(output);
    //System.out.println("defined new wire output : " + name);
    return wire;
  }
  
  public static String describeWires() {
    String result = "------WIRES------\n";
    result += "wire: [name] [source] [dest1] [dest2] [...\n\n";
    
    for (Map.Entry<String, Wire> entry : wireLookup.entrySet()) {
      result += "wire: " + entry.getKey() + " " + entry.getValue() + "\n";
    }
    return result;
  }
  
}
