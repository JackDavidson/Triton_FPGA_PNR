package pnr.components.fpga;

import pnr.components.blif.BlifWire;

public interface BlifItemRepr {
  BlifWire[] getInputs();
  BlifWire[] getOutputs();
  void setInit(String value);
  String getInit();
}
