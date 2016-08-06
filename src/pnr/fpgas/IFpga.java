package pnr.fpgas;

import pnr.components.circuit.ICircuitComponent;
import pnr.components.fpga.IFpgaComponent;

import java.util.ArrayList;

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
 * orderPlacements(), which is given an array to re-order. The normal
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
public interface IFpga {

    ArrayList<IFpgaComponent> getComponents();

    void placeInitialComponentsHard();
    // numTries is the number of times the algorithm has called this function
    // and failed somewhere further along
    void placeInitialComponentsSoft(int numTries);

    // return false if you choose not to implement this method, and would like
    // to use the default implementation.
    boolean orderPlacements(ArrayList<ICircuitComponent> placements);

    // numTries is the number of times the algorithm has called this function
    // and failed somewhere further along. Make sure that you try different placements
    // every time, or simply don't make any placements if numTries != 0
    void inferPlacements(ArrayList<ICircuitComponent> placements, int numTries);

    // return false if you choose not to implement this method, and would like
    // to use the default implementation.
    // also, note that you can return false at any time and the stock algorithm will
    // simply begin trying every permutation of placements. A good implementation may
    // be to return false if numTries != 0, but otherwise make your best first guess.
    boolean makePlacement(ICircuitComponent component, int numTries) throws CannotPlaceException;

    // this function should return true when we are done placing, or may throw
    // an error if somewhere along the way the algorithm has realized that this
    // will never work. A DoesNotMapException means that the program will
    // immediately stop and display an error message.
    boolean isDone() throws DoesNotMapException;

    // after we are done, we will retrieve the bit stream
    String getBitstream();
}