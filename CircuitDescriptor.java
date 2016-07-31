package pnr;
import java.io.*;
import java.util.*;

public class CircuitDescriptor {
  
  public static String describeCircuit() {
    String result = "";
    result += WireFactory.describeWires();
    result += "\n\n";
    result += GateFactory.describe();
    return result;
  }
  
}
