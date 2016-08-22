package pnr.components.fpga;

import pnr.components.circuit.ICircuitComponent;

/**
 * Created by jack on 8/12/16.
 */
public class FpgaLut implements IFpgaComponent {
    private ICircuitComponent circuitComponent;
    @Override
    public ICircuitComponent getCircuitMapping() {
        return circuitComponent;
    }
    @Override
    public void setCircuitMapping(ICircuitComponent isMapped) {
        circuitComponent = isMapped;
    }
}
