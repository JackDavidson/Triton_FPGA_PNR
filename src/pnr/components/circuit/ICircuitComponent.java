package pnr.components.circuit;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack on 7/31/16.
 */
public interface ICircuitComponent {
    boolean isPlaced();
    void setIsPlaced(boolean isPlaced);
    int getId();
    List<ICircuitComponent> getInputs();
    AbstractMap<Integer, ArrayList<ICircuitComponent>> getOutputs();
    String threeLetterType();
    void addInput(ICircuitComponent component);
    void addOutput(Integer outputNumber, ICircuitComponent component);
}
