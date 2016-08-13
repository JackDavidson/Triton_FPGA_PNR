package pnr.components.circuit;

import pnr.components.fpga.IFpgaComponent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack on 7/31/16.
 */
public interface ICircuitComponent {
    IFpgaComponent getPlacedOn();
    //void setIsPlaced(boolean isPlaced);
    void mapTo(IFpgaComponent c);
    void unMap();
    int getId();
    List<ICircuitComponent> getInputs();
    AbstractMap<Integer, ArrayList<ICircuitComponent>> getOutputs();
    String threeLetterType();
    void addInput(ICircuitComponent component);
    void addOutput(Integer outputNumber, ICircuitComponent component);
}
