package pnr.components.circuit;

import pnr.components.fpga.IFpgaComponent;

import java.util.*;

/**
 * Created by jack on 7/31/16.
 */
public class CircuitLut implements ICircuitComponent {

    String initValues;
    public String getInitValues() {
        return initValues;
    }
    public CircuitLut(String initValues) {
        // TODO: this assumes that the MSB values are what are set to false.
        this.initValues = initValues;
    }

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
    public void addInput(ICircuitComponent component) {
        inputs.add(component);
    }

    static int count = 0;
    int id = count++;
    @Override
    public int getId() {
        return id;
    }

    IFpgaComponent mappedTo = null;
    @Override
    public IFpgaComponent getPlacedOn() {
        return mappedTo;
    }
    @Override
    public void mapTo(IFpgaComponent c) {
        mappedTo = c;
        c.setIsMapped(true);
    }
    @Override
    public void unMap() {
        mappedTo = null;
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
        return "LUT";
    }
}
