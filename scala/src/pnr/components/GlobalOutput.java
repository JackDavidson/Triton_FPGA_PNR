package pnr.components;
import pnr.components.circuit.AlreadyMappedException;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.Element;
import pnr.components.fpga.IFpgaComponent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

// global outputs can be a destination for wires, but not a source.
public class GlobalOutput extends Element implements ICircuitComponent, IFpgaComponent {

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
    return "GlobalOutput_" + pinNumber;
  }
  
  public GlobalOutput() {
    this.pinNumber = lastAssignedNumber++;
  }

  IFpgaComponent mappedTo = null;
  @Override
  public IFpgaComponent getPlacedOn() {
    return mappedTo;
  }
  @Override
  public void mapTo(IFpgaComponent c) throws AlreadyMappedException {
    if (mappedTo != null)
      throw new AlreadyMappedException(this.getClass().getName() + " has already been mapped!");
    mappedTo = c;
    c.setCircuitMapping(this);
  }
  @Override
  public void unMap() {
    mappedTo.setCircuitMapping(null);
    mappedTo = null;
  }

  static int count = 0;
  int id = count++;
  @Override
  public int getId() {
    return id;
  }
  @Override
  public ArrayList<ICircuitComponent> getInputs() {
    return inputs;
  }
  @Override
  public AbstractMap<Integer, ArrayList<ICircuitComponent>> getOutputs() {
    return outputs;
  }
  @Override
  public String threeLetterType() {
    return "GLO";
  }

  private ICircuitComponent circuitComponent;
  @Override
  public ICircuitComponent getCircuitMapping() {
    return circuitComponent;
  }
  @Override
  public void setCircuitMapping(ICircuitComponent isMapped) {
    circuitComponent = isMapped;
  }
}
