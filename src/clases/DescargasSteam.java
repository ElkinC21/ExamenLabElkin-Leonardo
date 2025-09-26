/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 *
 * @author Administrator
 */
public class DescargasSteam {
    public int downloadCode;
    public int jugadorCode;
    public String jugadorNombre;
    public int gameCode;
    public String gameName;
    public double gamePrice;
    public long fecha;
    public String caratulaDownloadPath;
    
     @Override public String toString() {
        return "Download #"+downloadCode+" | "+jugadorCode+" -> "+gameName+" $"+gamePrice;
    }
}
