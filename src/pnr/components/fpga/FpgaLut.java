package pnr.components.fpga;

/**
 * Created by jack on 8/12/16.
 */
public class FpgaLut implements IFpgaComponent {
    private boolean usedAsFpgaComponent = false;
    @Override
    public boolean getIsMapped() {
        return usedAsFpgaComponent;
    }
    @Override
    public void setIsMapped(boolean isMapped) {
        usedAsFpgaComponent = isMapped;
    }
}
