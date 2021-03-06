package pnr.components.circuit;

import pnr.components.fpga.IFpgaComponent;
import pnr.misc.Helpers;

import java.util.*;

/**
 * Created by jack on 7/31/16.
 */
public class CircuitLut implements ICircuitComponent {

    String initValues;
    public String getInitValues() {
        return initValues;
    }

    private String swapInputs(int a, int b, String originalBits) {
        String newBits = "";
        for (int i = 0; i < initValues.length(); i++) {
            int j = ((i & (1 << a)) >> a) << b;
            int k = ((i & (1 << b)) >> b) << a;
            int l = (i & (~(1 << a)) & (~(1 << b))) | j | k;
            newBits += originalBits.charAt(l);
        }
        if (a == b) {
            throw new RuntimeException("you cant swap an input with itself.");
        }
        if (b < a) {
            int tmpB = b;
            b = a;
            a = tmpB;
        }
        ICircuitComponent tmpA = inputs.get(a);
        ICircuitComponent tmpB = inputs.get(b);
        inputs.remove(b);
        inputs.remove(a);
        inputs.add(a, tmpB);
        inputs.add(b, tmpA);
        return newBits;
    }

    public void swapInputValues(int a, int b) {
        this.initValues = swapInputs(a, b, initValues);
    }

    public CircuitLut(String initValues) {
        // TODO: this assumes that the MSB values are what are set to false.
        this.initValues = initValues;
    }

    public CircuitLut(String initValues, List<ICircuitComponent> inputs,
                      AbstractMap<Integer, ArrayList<ICircuitComponent>> outputs) {
        this.initValues = initValues;
        this.inputs.addAll(inputs);
        for (Map.Entry<Integer, ArrayList<ICircuitComponent>> output : outputs.entrySet())
            for (ICircuitComponent component : output.getValue())
                addOutput(output.getKey(), component);
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

        System.out.println(Helpers.getComponentName(this) + " inputs: " + inputs.size() + " outputs: " + outputs.size());

        long requiredInitValLength = (long) Math.pow(2, inputs.size()) * outputs.size();
        while (initValues.length() < requiredInitValLength) {
            initValues += initValues; // double up the init value, since only part was specified.
        }
    }
    ArrayList<ICircuitComponent> inputs = new ArrayList<>();
    public void addInput(ICircuitComponent component) {
        inputs.add(component);
        long requiredInitValLength = (long) Math.pow(2, inputs.size()) * outputs.size();
        while (initValues.length() < requiredInitValLength) {
            initValues += initValues; // double up the init value, since only part was specified.
        }
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
    public void mapTo(IFpgaComponent c) throws AlreadyMappedException {
        if (mappedTo != null)
            throw new AlreadyMappedException(this.getClass().getName() + " has already been mapped!");
        mappedTo = c;
        c.setCircuitMapping(this);
    }
    @Override
    public void unMap() {
        mappedTo.setCircuitMapping(null);
        mappedTo = null;
    }

    @Override
    public ArrayList<ICircuitComponent> getInputs() {
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
