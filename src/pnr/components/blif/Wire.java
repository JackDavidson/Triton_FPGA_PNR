package pnr.components.blif;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.Element;

import java.util.*;


public class Wire extends Element implements ICircuitComponent {
  public ArrayList<Element> outputs = new ArrayList<Element>();
  public Element input;
  private String name;
  
  public Wire(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public String toString() {
    String in = null;
    if (input == null)
      in = "null";
    else
      in = input.getName();
    String result = "" + in + " ";
    for (Element e : outputs) {
      result += e.getName() + " ";
    }
    return result;
  }

  @Override
  public boolean isPlaced() {
    return false;
  }
}
