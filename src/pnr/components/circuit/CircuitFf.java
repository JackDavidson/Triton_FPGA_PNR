package pnr.components.circuit;

import pnr.components.fpga.IFpgaComponent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by jack on 7/31/16.
 */
public class CircuitFf implements ICircuitComponent {

    TreeMap<Integer, ArrayList<ICircuitComponent>> outputs = new TreeMap<>();
    @Override
    public void addOutput(Integer outputNumber, ICircuitComponent component) {
        ArrayList<ICircuitComponent> componentsOnThisOutput = outputs.get(outputNumber);
        if (componentsOnThisOutput == null) {
            componentsOnThisOutput = new ArrayList<>();
            outputs.put(outputNumber, componentsOnThisOutput);
        }
        componentsOnThisOutput.add(component);
    }
    ArrayList<ICircuitComponent> inputs = new ArrayList<>();
    @Override
    public void addInput(ICircuitComponent component) {
        inputs.add(component);
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
        return inputs;
    }
    @Override
    public AbstractMap<Integer, ArrayList<ICircuitComponent>> getOutputs() {
        return outputs;
    }
    @Override
    public String threeLetterType() {
        return "FFP";
    }
}
