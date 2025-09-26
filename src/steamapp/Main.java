/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package steamapp;

import gui.Loginpantalla;

/**
 *
 * @author Administrator
 */
public class Main {
    public static void main(String[] args) {
        Steam steam = new Steam();
        new Loginpantalla(steam).setVisible(true);
    }
}
