package pnr.fpgas.tci;

import pnr.components.GlobalInput;
import pnr.components.GlobalOutput;
import pnr.components.circuit.CircuitLut;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.FpgaLut;
import pnr.components.fpga.IFpgaComponent;
import pnr.fpgas.CannotPlaceException;
import pnr.fpgas.DoesNotMapException;
import pnr.fpgas.Fpga;
import pnr.misc.Defs;
import pnr.misc.Helpers;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by jack on 7/31/16.
 */
public class TritoncoreI extends Fpga {


    InternalDom placeAndRouter;

    GlobalInput[] globalInputs = new GlobalInput[16]; // 16 global inputs
    GlobalOutput[] globalOutputs = new GlobalOutput[16]; // 16 global inputs

    HashSet<FpgaLut>[] lutGroups = new HashSet[4];

    public TritoncoreI(InternalDom placeAndRouter) {
        for (int i = 0; i < lutGroups.length; i++) {
            HashSet<FpgaLut> grouping = new HashSet<>();
            for (int j = 0; j < 60; j++) {
                grouping.add(new FpgaLut());
            }
            lutGroups[i] = grouping;
        }
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
    public ICircuitComponent getNextItemToPlace(ArrayList<ICircuitComponent> placements) throws DoesNotMapException {
        return null;
    }

    @Override
    public void inferPlacements(ArrayList<ICircuitComponent> placements, int numTries) {

    }

    @Override
    public boolean makePlacement(ICircuitComponent component, int numTries) throws CannotPlaceException, DoesNotMapException  {
        Class c = component.getClass();
        if (c == CircuitLut.class)
            return makePlacement((CircuitLut) component);
        if (c == GlobalInput.class)
            return makePlacement((GlobalInput) component);
        if (c == GlobalOutput.class)
            return makePlacement((GlobalOutput) component);
        throw new DoesNotMapException("Unrecognized component for TCI: " + c.getName());
    }

    private boolean makePlacement(CircuitLut l) throws CannotPlaceException {
        AbstractMap<Integer, ArrayList<ICircuitComponent>> ouputs = l.getOutputs();
        // count up the number of this circuit's outputs that want it to be in each group,
        // and place it in the grouping that most would like.
        int[] votesForWhereItShouldGo = new int[]{0,0,0,0};
        if (ouputs.size() == 1) { // only one output
            ArrayList<ICircuitComponent> outputsOnOne = ouputs.get(0);
            if (Defs.verbose) {
                System.out.println("deciding where to place CircuitLut: " + l.getId());
                System.out.println("the output goes to " + outputsOnOne.size() + " places.");
            }
            // lets look at what the outputs are for this LUT. If it goes to a GlobalOutput, there is only
            // one place we can put it. Otherwise, we should place it based on what other components the
            // output routes to.
            for (ICircuitComponent o : outputsOnOne) {
                IFpgaComponent objOutputIsPlacedOn = o.getPlacedOn();
                if (objOutputIsPlacedOn != null) { // if the output is placed
                    if (Defs.verbose) {
                        System.out.println("in TritoncoreI.MakePlacement, looking at the inputs for output: " +
                                Helpers.getComponentName(o) + " there are: " + o.getInputs().size());
                    }
                    for (int i = 0; i < o.getInputs().size(); i++) {
                        if (o.getInputs().get(i) == l) {
                            votesForWhereItShouldGo[i]++;
                        }
                    }
                } else {
                    if (Defs.verbose) {
                        System.out.println("output: " + Helpers.getComponentName(o) +
                                " is not placed. Ignoring.");
                    }
                }
            }
        } else {
            throw new CannotPlaceException("The LUT has more than one output wire! we only have single output wires" +
                    " for the tritoncore-I.");
        }

        int placementLocation = 0;
        int votesForPlacementLoc = 0;
        for (int i = 0; i < votesForWhereItShouldGo.length; i++) {
            if (votesForWhereItShouldGo[i] > votesForPlacementLoc) {
                votesForPlacementLoc = votesForWhereItShouldGo[i];
                placementLocation = i;
            }
        }
        return placeOnNextAvailableForGrouping(placementLocation, l);
    }

    private boolean placeOnNextAvailableForGrouping(int groupNum, CircuitLut toPlace) throws CannotPlaceException {
        boolean placed = false;
        HashSet<FpgaLut> group = lutGroups[groupNum];
        for (FpgaLut lutInGroup : group) {
            if (!lutInGroup.getIsMapped()) {
                toPlace.mapTo(lutInGroup);
                placed = true;
                break;
            }
        }
        if (!placed)
            throw new CannotPlaceException("The LUT grouping number: " + groupNum + " is already full.");
        return true;
    }

    private boolean makePlacement(GlobalInput gbi) throws DoesNotMapException {
        int i = 0;
        while (globalInputs[i] != null) {
            i++;
            if (i == globalInputs.length - 1) {
                throw new DoesNotMapException("We can't map more than " + globalInputs.length + " input pins.");
            }
        }
        globalInputs[i] = new GlobalInput();
        gbi.mapTo(globalInputs[i]);
        return true;
    }

    private boolean makePlacement(GlobalOutput gbo) throws DoesNotMapException {
        int i = 0;
        while (globalOutputs[i] != null) {
            i++;
            if (i == globalOutputs.length - 1) {
                throw new DoesNotMapException("We can't map more than " + globalOutputs.length + " output pins.");
            }
        }
        globalOutputs[i] = new GlobalOutput();
        gbo.mapTo(globalOutputs[i]);
        return true;
    }

    @Override
    public boolean isDone() throws DoesNotMapException {
        return true;
    }

    @Override
    public String getBitstream() {
        return "";
    }

    public String getDebuggingRepresentation() {
        StringBuilder result = new StringBuilder();

        return result.toString();
    }
}
