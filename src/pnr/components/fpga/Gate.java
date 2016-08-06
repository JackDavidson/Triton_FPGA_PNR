package pnr.components.fpga;

import pnr.components.blif.Wire;

public interface Gate {
  Wire[] getInputs();
  Wire[] getOutputs();
  void setInit(String value);
  String getInit();
}
