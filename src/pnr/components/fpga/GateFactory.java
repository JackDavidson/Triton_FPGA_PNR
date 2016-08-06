package pnr.components.fpga;
import pnr.CircuitDescriptor;
import pnr.components.blif.Wire;
import pnr.components.blif.SB_DFF;
import pnr.components.blif.SB_LUT4;

import java.util.*;

public class GateFactory {

  private static Gate result;

  public static Gate makeGate(String description, CircuitDescriptor descriptor) {
    String[] gateDetails = description.substring(".gate".length())
        .split("\\s+");
    String gateType = null;
    ArrayList<String> gateIo = new ArrayList<String>();

    int i = 0; // find the starting position. ignore the empty stuff
    while (i < gateDetails.length && gateDetails[i].trim().isEmpty()) {
      i++;
    }

    gateType = gateDetails[i++];
    for (; i < gateDetails.length; i++) {
      gateIo.add(gateDetails[i]);
    }

    //Gate result;
    switch (gateType) {
    case "SB_LUT4":
      result = makeLUT4(gateIo, descriptor);
      descriptor.addGate(result);
      break;
    case "SB_DFF":
      result = makeSB_DFF(gateIo, descriptor);
      descriptor.addGate(result);
      break;
    default:
      System.out.println("[error] could not find a match to gate: " + gateType);
      result = null;
      break;
    }
    return result; // default is to return null
  }

  public static SB_LUT4 makeLUT4(ArrayList<String> io, CircuitDescriptor descriptor) {
    TreeMap<String, String> inputsToOutputs = new TreeMap<String, String>();
    for (String inOrOut : io) {
      String[] thisInToOut = inOrOut.split("=");
      //System.out.println("I is: " + thisInToOut[0] + "val is:" + thisInToOut[1]);
      inputsToOutputs.put(thisInToOut[0], thisInToOut[1]);
    }
    return new SB_LUT4(inputsToOutputs.get("I0"), inputsToOutputs.get("I1"), inputsToOutputs.get("I2"),
            inputsToOutputs.get("I3"), inputsToOutputs.get("O"), descriptor);
  }
  
  public static SB_DFF makeSB_DFF(ArrayList<String> io, CircuitDescriptor descriptor) {
    TreeMap<String, String> inputsToOutputs = new TreeMap<String, String>();
    for (String inOrOut : io) {
      String[] thisInToOut = inOrOut.split("=");
      //System.out.println("I is: " + thisInToOut[0] + "val is:" + thisInToOut[1]);
      inputsToOutputs.put(thisInToOut[0], thisInToOut[1]);
    }
    return new SB_DFF(inputsToOutputs.get("C"), inputsToOutputs.get("D"), inputsToOutputs.get("Q"), descriptor);
  }
  
  public static void handleParam(String description, Gate createdGate) {
    String[] pDetails = description.substring(".param".length())
        .split("\\s+");
    String pType = null;
    ArrayList<String> detailsList = new ArrayList<String>();

    int i = 0; // find the starting position. ignore the empty stuff
    while (i < pDetails.length && pDetails[i].trim().isEmpty()) {
      i++;
    }

    pType = pDetails[i++];
    for (; i < pDetails.length; i++) {
      detailsList.add(pDetails[i]);
    }

    switch (pType) {
    case "LUT_INIT":
      createdGate.setInit(detailsList.get(0));
      break;
    default:
      System.out.println("[error] could not find a match to param type: " + pType);
      break;
    }
  }
  
  public static String describe(ArrayList<Gate> gateList) {
    String result = "------GATES------\n";
    result += "gate: [type] [output] [in1] [in2] [in3] [...\n\n";
    for (Gate gate : gateList) {
      result += "gate: " + gate.getClass().getSimpleName() + " " + gate.getOutputs()[0].getName();
      for (Wire wire : gate.getInputs()) {
        result += " " + wire.getName();
      }
      result += "\n";
    }

    return result;
  }

}
