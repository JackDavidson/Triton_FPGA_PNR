package pnr.fpgas.tci;

import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.IFpgaComponent;
import pnr.fpgas.CannotPlaceException;
import pnr.fpgas.DoesNotMapException;
import pnr.fpgas.IFpga;

import java.util.ArrayList;

/**
 * Created by jack on 7/31/16.
 */
public class TritoncoreI implements IFpga {


    InternalDom placeAndRouter;

    public TritoncoreI(InternalDom placeAndRouter) {
      this.placeAndRouter = placeAndRouter;
    }

    @Override
    public ArrayList<IFpgaComponent> getComponents() {
        return new ArrayList<IFpgaComponent>();
    }

    @Override
    public void placeInitialComponentsHard() {

    }

    @Override
    public void placeInitialComponentsSoft(int numTries) {

    }

    @Override
    public ICircuitComponent getNextItemToPlace(ArrayList<ICircuitComponent> placements) {
        return null;
    }

    @Override
    public void inferPlacements(ArrayList<ICircuitComponent> placements, int numTries) {

    }

    @Override
    public boolean makePlacement(ICircuitComponent component, int numTries) throws CannotPlaceException {
        return false;
    }

    @Override
    public boolean isDone() throws DoesNotMapException {
        return true;
    }

    @Override
    public String getBitstream() {
        return "";
    }
}
