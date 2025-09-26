/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 *
 * @author Administrator
 */
public class Juego {
    public int code;
    public String titulo;
    public String genero;
    public char sistemaOp;
    public int edadMinima;
    public double precio;
    public int contadorDownloads;
    public String caratulaPath;
    
    public static String sistemaOperativoText(char so){
        switch(Character.toUpperCase(so)){
            case 'W': return "Windows";
            case 'M': return "Mac";
            case 'L': return "Linux";
            default: return "Desconocido";
        }
    }
    
     @Override public String toString() {
        return ""+code+" | "+titulo+" | "+genero+" | "+sistemaOperativoText(sistemaOp)
                +" | +"+edadMinima+" | $"+precio+" | dls:"+contadorDownloads;
    }
}
