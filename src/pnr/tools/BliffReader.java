package pnr.tools;

import pnr.CircuitDescriptor;
import pnr.components.GlobalOutputFactory;
import pnr.components.fpga.Gate;
import pnr.components.fpga.GateFactory;
import pnr.components.GlobalInputFactory;

import java.io.*;
import java.util.*;

public class BliffReader {
  
  private CircuitDescriptor resultDescriptor = new CircuitDescriptor();

  public CircuitDescriptor getResultDescriptor() {
    return resultDescriptor;
  }
  
  public BliffReader(String filename) {
    String fileContents = null;
    try {
      fileContents = readFile(filename);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    String[] fileLines = fileContents.split("\n");
    Gate lastGateCreated = null;
    for (String line : fileLines) {
      String lineToParse = line.trim();
      if (!lineToParse.startsWith(".")) { // ignore anything that is meaningless
        continue;
      }
      if (lineToParse.startsWith(".inputs")) { // process input names
        GlobalInputFactory.makeInputs(lineToParse, resultDescriptor);
      } else if (lineToParse.startsWith(".outputs")) { // process output names
        GlobalOutputFactory.makeInputs(lineToParse, resultDescriptor);
      } else if (lineToParse.startsWith(".param")) { // process output names
        GateFactory.handleParam(lineToParse, lastGateCreated);
      } else if (lineToParse.startsWith("#")) { // ignore comments
        continue;
      } else if (lineToParse.startsWith(".model")) { // ignore model
        continue;
      } else if (lineToParse.startsWith(".attr")) { // attributes are ignored, too
        continue;
      } else if (lineToParse.startsWith(".names")) { // names also ignored
        continue;
      } else if (lineToParse.startsWith(".gate")) { // probably a lookup table
        lastGateCreated = GateFactory.makeGate(lineToParse, resultDescriptor);
      } else if (lineToParse.startsWith(".end")) { // end means break.
        break;
      } else {
        System.out.println(line);
      }
    }
    System.out.println(resultDescriptor.describeCircuit());
  }
  
  static String readFile(String path) throws IOException 
  {
    Scanner scanner = new Scanner( new File(path) );
    String text = scanner.useDelimiter("\\A").next();
    scanner.close();
    return text;
  }
}
