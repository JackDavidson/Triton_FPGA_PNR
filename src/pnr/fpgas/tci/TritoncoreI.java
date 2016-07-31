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


    TCI_Pnr placeAndRouter;

    public TritoncoreI(TCI_Pnr placeAndRouter) {
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
    public boolean orderPlacements(ArrayList<ICircuitComponent> placements) {
        return false;
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
        placeAndRouter.performTransforms();
        placeAndRouter.resolveConnections();
        placeAndRouter.placeHardcodedNodes();
        placeAndRouter.printBitstream();
        return "";
    }
}
