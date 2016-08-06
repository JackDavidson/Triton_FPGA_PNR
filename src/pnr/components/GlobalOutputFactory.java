package pnr.components;
import pnr.CircuitDescriptor;

//global inputs can be a source for wires, but not a destination.
public class GlobalOutputFactory {
  
  public static GlobalOutput makeGlobalOutput(String name, CircuitDescriptor circuitDescriptorToAddTo) {
   GlobalOutput in = new GlobalOutput();
      circuitDescriptorToAddTo.assignWireOutput(name, in);
   return in;
  }
  
  public static void makeInputs(String line, CircuitDescriptor circuitDescriptorToAddTo) {
    //System.out.println("found outputs: ");
    String[] outputNames = line.substring(".outputs".length()).split("\\s+");
    for (String outputName : outputNames) {
      if (outputName.trim().length() > 0) {
        makeGlobalOutput(outputName, circuitDescriptorToAddTo);
      }
    }
  }
}
