package pnr.components;
import pnr.BlifDom;

//global inputs can be a source for wires, but not a destination.
public class GlobalInputFactory {
  
  public static GlobalInput makeGlobalInput(String name, BlifDom descriptor) {
   GlobalInput in = new GlobalInput();
   descriptor.assignWireInput(name, in);
   return in;
  }
  
  public static void makeInputs(String line, BlifDom descriptor) {
    //System.out.println("found inputs: ");
    String[] inputNames = line.substring(".inputs".length()).split("\\s+");
    for (String inputName : inputNames) {
      if (inputName.trim().length() > 0) {
        makeGlobalInput(inputName, descriptor);
      }
    }
  }
}
