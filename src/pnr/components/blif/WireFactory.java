package pnr.components.blif;

import java.util.*;

public class WireFactory {
  
  public static String describeWires(HashMap<String, Wire> wireLookup) {
    String result = "------WIRES------\n";
    result += "wire: [name] [source] [dest1] [dest2] [...\n\n";
    
    for (Map.Entry<String, Wire> entry : wireLookup.entrySet()) {
      result += "wire: " + entry.getKey() + " " + entry.getValue() + "\n";
    }
    return result;
  }
  
}
