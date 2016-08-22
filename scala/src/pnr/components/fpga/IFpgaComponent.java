package pnr.components.fpga;

import pnr.components.circuit.ICircuitComponent;

/**
 * Created by jack on 7/31/16.
 */
public interface IFpgaComponent {
    ICircuitComponent getCircuitMapping();
    void setCircuitMapping(ICircuitComponent c);
}
