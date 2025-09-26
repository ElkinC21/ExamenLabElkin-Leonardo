
package steamapp;

import gui.Loginpantalla;


public class Main {
    public static void main(String[] args) {
        Steam steam = new Steam();
        new Loginpantalla(steam).setVisible(true);
    }
   
}
