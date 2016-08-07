package pnr.components.circuit;

import pnr.components.fpga.IFpgaComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack on 7/31/16.
 */
public class CircuitFf implements ICircuitComponent {

    ArrayList<ICircuitComponent> outputs = new ArrayList<>();
    @Override
    public void addOutput(ICircuitComponent component) {
        outputs.add(component);
    }
    @Override
    public void addInput(ICircuitComponent component) {
        outputs.add(component);
    }

    private boolean isPlaced = false;
    @Override
    public boolean isPlaced() {
        return isPlaced;
    }
    @Override
    public void setIsPlaced(boolean isPlaced) {
        this.isPlaced = isPlaced;
    }

    static int count = 0;
    int id = count++;
    @Override
    public int getId() {
        return id;
    }
    @Override
    public List<ICircuitComponent> getInputs() {
        return null;
    }
    @Override
    public List<ICircuitComponent> getOutputs() {
        return null;
    }
    @Override
    public String threeLetterType() {
        return "FFP";
    }
}
