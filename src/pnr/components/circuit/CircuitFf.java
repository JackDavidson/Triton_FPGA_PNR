package pnr.components.circuit;

import pnr.components.fpga.IFpgaComponent;

/**
 * Created by jack on 7/31/16.
 */
public class CircuitFf implements ICircuitComponent {
    private boolean isPlaced = false;
    public boolean isPlaced() {
        return isPlaced;
    }
}
