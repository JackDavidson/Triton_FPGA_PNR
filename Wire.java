package pnr;
import java.util.*;


public class Wire extends Element {
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
}
