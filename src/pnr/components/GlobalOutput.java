package pnr.components;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.Element;

import java.util.ArrayList;
import java.util.List;

// global outputs can be a destination for wires, but not a source.
public class GlobalOutput extends Element implements ICircuitComponent {

  ArrayList<ICircuitComponent> outputs = new ArrayList<>();
  @Override
  public void addOutput(ICircuitComponent component) {
    outputs.add(component);
  }
  @Override
  public void addInput(ICircuitComponent component) {
    outputs.add(component);
  }

  private static int lastAssignedNumber = 0;
  public int pinNumber;
  
  public String getName() {
    return "GlobalOutput_" + pinNumber;
  }
  
  public GlobalOutput() {
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
    return null;
  }
  @Override
  public List<ICircuitComponent> getOutputs() {
    return null;
  }
  @Override
  public String threeLetterType() {
    return "GLO";
  }
}
