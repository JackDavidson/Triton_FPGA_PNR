package pnr.components;

import pnr.components.circuit.AlreadyMappedException;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.IFpgaComponent;

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
