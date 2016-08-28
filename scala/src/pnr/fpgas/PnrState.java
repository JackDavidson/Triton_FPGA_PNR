package pnr.fpgas;

import pnr.actions.ActionMapTo;
import pnr.actions.IAction;
import pnr.components.circuit.CircuitLut;
import pnr.components.circuit.ICircuitComponent;
import pnr.fpgas.tci.InternalDom;
import pnr.misc.Defs;
import pnr.misc.Helpers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

/**
 * Created by jack on 8/28/16.
 */
public class PnrState {
    public Stack<IAction> reversals = new Stack<>();
    public Stack<Integer> numberOfAttempts = new Stack<>();
    public ArrayList<ICircuitComponent> toPlace; // initalized in PlaceAndRoute
    public ArrayList<ICircuitComponent> allComponents = new ArrayList<>();
}
