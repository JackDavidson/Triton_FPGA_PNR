package pnr.components.fpga;

import pnr.components.blif.Wire;

public interface BlifItemRepr {
  Wire[] getInputs();
  Wire[] getOutputs();
  void setInit(String value);
  String getInit();
}
