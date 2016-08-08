package pnr.components;

import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.BlifItemRepr;
import pnr.components.fpga.Element;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

//global inputs can be a source for wires, but not a destination.
public class GlobalInput extends Element implements ICircuitComponent {

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
  ArrayList<ICircuitComponent> inputs = new ArrayList<>();
  @Override
  public void addInput(ICircuitComponent component) {
    inputs.add(component);
  }

  private static int lastAssignedNumber = 0;
  public int pinNumber;
  
  public String getName() {
    return "GI_" + pinNumber;
  }
  
  public GlobalInput() {
    this.pinNumber = lastAssignedNumber++;
  }


  private boolean isPlaced = false;
  @Override
  public boolean isPlaced() {
    return isPlaced;
  }
  @Override
  public void setIsPlaced(boolean isPlaced) {
    this.isPlaced = isPlaced;
  }

  static int count = 0;
  int id = count++;
  @Override
  public int getId() {
    return id;
  }
  @Override
  public List<ICircuitComponent> getInputs() {
    return inputs;
  }
  @Override
  public AbstractMap<Integer, ArrayList<ICircuitComponent>> getOutputs() {
    return outputs;
  }

  @Override
  public String threeLetterType() {
    return "GLI";
  }

}
