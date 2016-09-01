package pnr.fpgas;

import pnr.PlaceAndRoute;
import pnr.actions.IAction;
import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.IFpgaComponent;
import pnr.misc.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * The IFpga class is used by your speciffic FPGA class. It must implement a
 *   first implement getComponents(), a function which returns an array
 *   containing all of its components. The components are expected to be
 *   connected correctly.
 *
 * placeInitialComponentsHard() is then called, which should place all
 *   components that can be placed 'perfectly'. These components will not
 *   be moved again.
 *
 * placeInitialComponentsSoft() is called after this, and you can place your
 *   components which may or may not work out, but which you feel you have a
 *   good guess as to where they should go. These components may be moved later
 *   on through the algorithm.
 *
 * the following four functions will then be called in a loop:
 *
 * inferPlacements(), which gives you a chance to infer any placements based on
 *   what was placed last.
 *
 * getNextItemToPlace(), which is given an array to re-order. The normal
 *   implementation will order by fewest number of input and output
 *   dependencies left to place. (e.g. a component which depends on only other
 *   components which have already been assigned to will be first in the array)
 *
 * makePlacement(), which takes the next component to be placed and places it
 *   as best it can. This function may throw a CannotPlaceException if it
 *   decides that previous placements have put it in a situation where it
 *   cannot continue to place the next component.
 *
 * isDone(), which simply returns true if every element has been mapped.
 *
 *
 * note: it is usually a good idea to maintain an internal representation of
 *    your FPGA to help you organize and optimize your code.
 */
public abstract class Fpga {

    // this should return all the FPGA's components, set up with their
    // connections.
    public abstract ArrayList<IFpgaComponent> getComponents();

    // this is to give you a chance to perform actions for transforming the initial input into a format
    // that is more easily parsed for later iterations. return true for the boolean if you would like this
    // method to be called again after performing the actions you specified.
    // the input that will be passed to this method is the entire list of components that need to be mapped to
    // your FPGA. If your actions contain instructions to generate new components, they will be included in
    // subsequent calls. Thre is no particular ordering to circuitItems.
    public abstract Pair<Boolean, List<IAction>> performInitialActions(List<ICircuitComponent> circutItems);

    // return null if you choose not to implement this method, and would like
    // to use the default implementation.
    // if you realize there is a problem, such as a component that your FPGA does not have an implementation for,
    // simply throw a DoesNotMapException, and
    public abstract ICircuitComponent getNextItemToPlace(ArrayList<ICircuitComponent> placements)
            throws DoesNotMapException;

    // numTries is the number of times the algorithm has called this function
    // and failed somewhere further along. Make sure that you try different placements
    // every time, or simply don't make any placements if numTries != 0
    public abstract void inferPlacements(ArrayList<ICircuitComponent> placements, int numTries);

    // return false if you choose not to implement this method, and would like
    // to use the default implementation.
    // also, note that you can return false at any time and the stock algorithm will
    // simply begin trying every permutation of placements. A good implementation may
    // be to return false if numTries != 0, but otherwise make your best first guess.
    // throw a CannotPlaceException if you cant get this component to place, and want to backtrack.
    // throw a DoesNotMapException if in this step you realize you can't map the circuit or component at all.
    public abstract List<IAction> makePlacement(ICircuitComponent component, List<ICircuitComponent> components, int numTries) throws CannotPlaceException,
            DoesNotMapException;

    // after we are done, we will retrieve the bit stream
    public abstract String getBitstream();

    PlaceAndRoute pnr;
    public void setPlaceAndRoute(PlaceAndRoute pnr) {
        this.pnr = pnr;
    }
}
