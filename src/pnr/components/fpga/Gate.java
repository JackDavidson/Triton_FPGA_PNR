package pnr.components.fpga;

import pnr.components.blif.Wire;

public interface Gate {
  public Wire[] getInputs();
  public Wire[] getOutputs();
  public void setInit(String value);
  public String getInit();
}
