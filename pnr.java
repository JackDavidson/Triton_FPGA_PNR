package pnr;
public class pnr {
  
  
  private static String swapInputs(int a, int b, String originalBits) {
    String newBits = "";
    for (int i = 0; i < 16; i++) {
      int j = ((i & (1 << a)) >> a) << b;
      int k = ((i & (1 << b)) >> b) << a;
      int l = (i & (~(1 << a)) & (~(1 << b))) | j | k;
      newBits += originalBits.charAt(l);
    }
    return newBits;
  }
  
  public static void main(String[] args) {
    BliffReader reader = null;
    // Prints "Hello, World" to the terminal window.
    if (args.length != 1) {
      System.out.println("usage: java pnr [file.bliff]");
      return;
    } else {
      reader = new BliffReader(args[0]);
    }
    
    TCI_Pnr placeAndRouter = new TCI_Pnr();
    placeAndRouter.performTransforms();
    placeAndRouter.resolveConnections();
    placeAndRouter.placeHardcodedNodes();
    placeAndRouter.printBitstream();
    
  }
}
