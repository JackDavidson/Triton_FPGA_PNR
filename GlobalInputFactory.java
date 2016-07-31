package pnr;
import java.util.*;

//global inputs can be a source for wires, but not a destination.
public class GlobalInputFactory {
  
  public static GlobalInput makeGlobalInput(String name) {
   GlobalInput in = new GlobalInput();
   WireFactory.assignWireInput(name, in);
   return in;
  }
  
  public static void makeInputs(String line) {
    //System.out.println("found inputs: ");
    String[] inputNames = line.substring(".inputs".length()).split("\\s+");
    for (String inputName : inputNames) {
      if (inputName.trim().length() > 0) {
        makeGlobalInput(inputName);
      }
    }
  }
}
