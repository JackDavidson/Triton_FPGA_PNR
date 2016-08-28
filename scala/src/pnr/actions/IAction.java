package pnr.actions;

import pnr.fpgas.PnrState;

/**
 * Created by jack on 8/28/16.
 */
public interface IAction {

    // gets the action which reverses this action
    IAction inverse();

    // modifies the pnrState with the changes, and writes all necessary history
    void perform(PnrState pnrState);
}
