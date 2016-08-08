package pnr.components;

import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.Element;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

//global inputs can be a source for wires, but not a destination.
public class GlobalFalseConst implements ICircuitComponent {

  TreeMap<Integer, ArrayList<ICircuitComponent>> outputs = new TreeMap<>();
  @Override
  public void addOutput(Integer outputNumber, ICircuitComponent component) {
    ArrayList<ICircuitComponent> componentsOnThisOutput = outputs.get(outputNumber);
    if (componentsOnThisOutput == null) {
      componentsOnThisOutput = new ArrayList<>();
      outputs.put(outputNumber, componentsOnThisOutput);
    }
    componentsOnThisOutput.add(component);
  }
  @Override
  public void addInput(ICircuitComponent component) {}


  public String getName() {
    return "FALSE";
  }
  @Override
  public boolean isPlaced() {
    return true;
  }
  @Override
  public void setIsPlaced(boolean isPlaced) {
    // do nothing. 'false' can never be placed, and that's O.K. (its always 'placed')
  }

  static int count = 0;
  int id = count++;
  @Override
  public int getId() {
    return id;
  }
  @Override
  public List<ICircuitComponent> getInputs() {
    return null;
  }
  @Override
  public AbstractMap<Integer, ArrayList<ICircuitComponent>> getOutputs() {
    return outputs;
  }

  @Override
  public String threeLetterType() {
    return "FLS";
  }

}
