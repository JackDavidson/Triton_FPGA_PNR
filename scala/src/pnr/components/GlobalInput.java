package pnr.components;

import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.Element;
import pnr.components.fpga.IFpgaComponent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

//global inputs can be a source for wires, but not a destination.
public class GlobalInput extends Element implements ICircuitComponent, IFpgaComponent {

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
  public int pinNumber; // actually i
  
  public String getName() {
    return "GI_" + pinNumber;
  }
  
  public GlobalInput() {
    this.pinNumber = lastAssignedNumber++;
  }


  IFpgaComponent mappedTo = null;
  @Override
  public IFpgaComponent getPlacedOn() {
    return mappedTo;
  }
  @Override
  public void mapTo(IFpgaComponent c) {
    mappedTo = c;
    c.setIsMapped(true);
  }
  @Override
  public void unMap() {
    mappedTo = null;
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

  private boolean usedAsFpgaComponent = false;
  @Override
  public boolean getIsMapped() {
    return usedAsFpgaComponent;
  }
  @Override
  public void setIsMapped(boolean isMapped) {
    usedAsFpgaComponent = isMapped;
  }
}
