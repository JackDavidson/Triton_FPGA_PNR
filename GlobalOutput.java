package pnr;
import java.util.*;

// global outputs can be a destination for wires, but not a source.
public class GlobalOutput extends Element {
  private static int lastAssignedNumber = 0;
  public int pinNumber;
  
  public String getName() {
    return "GlobalOutput_" + pinNumber;
  }
  
  public GlobalOutput() {
    this.pinNumber = lastAssignedNumber++;
  }
}
