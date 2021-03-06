package pnr.components.blif;

import java.util.HashMap;
import java.util.Map;

public class WireFactory {
  
  public static String describeWires(HashMap<String, BlifWire> wireLookup) {
    String result = "------WIRES------\n";
    result += "wire: [name] [source] [dest1] [dest2] [...\n\n";
    
    for (Map.Entry<String, BlifWire> entry : wireLookup.entrySet()) {
      result += "wire: " + entry.getKey() + " " + entry.getValue() + "\n";
    }
    return result;
  }
  
}
