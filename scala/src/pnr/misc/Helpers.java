package pnr.misc;

import pnr.components.circuit.ICircuitComponent;

/**
 * Created by jack on 8/13/16.
 */
public class Helpers {
    public static final String getMethod() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

        //System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
        // return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
        return ste[ste.length - 1].getMethodName(); //Thank you Tom Tresansky
    }

    public static String getComponentName(ICircuitComponent component) {
        return component.threeLetterType() + padInt(component.getId());
    }

    public static String padInt(int i) {
        String padding = i < 10 ? "__" : i < 100 ? "_" : "";
        return padding + i;
    }
}
