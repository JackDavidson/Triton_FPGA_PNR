package pnr;
import pnr.components.blif.Wire;
import pnr.components.blif.WireFactory;
import pnr.components.fpga.BlifItemRepr;
import pnr.components.fpga.Element;
import pnr.components.fpga.GateFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class BlifDom {

  private HashMap<String, Wire> wireLookup = new HashMap<>();
  private ArrayList<BlifItemRepr> gateList = new ArrayList<>();

  public void addGate(BlifItemRepr gate) {
    gateList.add(gate);
  }

  public Wire assignWireInput(String name, Element input) {
    Wire wire = wireLookup.get(name);
    if (wire == null) {
      wire = new Wire(name);
      wireLookup.put(name, wire);
    }
    if (wire.input != null) {
      System.out.println("[warn] wire " + name + " alredy has an input defined!");
    }
    wire.input = input;
    return wire;
  }

  public Wire assignWireOutput(String name, Element output) {
    Wire wire = wireLookup.get(name);
    if (wire == null) {
      wire = new Wire(name);
      wireLookup.put(name, wire);
    }
    wire.outputs.add(output);
    //System.out.println("defined new wire output : " + name);
    return wire;
  }

  public HashMap<String, Wire> getWireLookup() {
    return wireLookup;
  }

  public ArrayList<BlifItemRepr> getGateList() {
    return gateList;
  }
  
  public String describeCircuit() {
    String result = "";
    result += WireFactory.describeWires(wireLookup);
    result += "\n\n";
    result += GateFactory.describe(gateList);
    return result;
  }
  
}
