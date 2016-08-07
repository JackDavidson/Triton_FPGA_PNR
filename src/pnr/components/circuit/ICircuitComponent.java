package pnr.components.circuit;

import java.util.List;

/**
 * Created by jack on 7/31/16.
 */
public interface ICircuitComponent {
    boolean isPlaced();
    void setIsPlaced(boolean isPlaced);
    int getId();
    List<ICircuitComponent> getInputs();
    List<ICircuitComponent> getOutputs();
    String threeLetterType();
    void addInput(ICircuitComponent component);
    void addOutput(ICircuitComponent component);
}
