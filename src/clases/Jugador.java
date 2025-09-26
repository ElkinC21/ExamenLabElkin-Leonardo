/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 *
 * @author Administrator
 */
public class Jugador {

    public int code;
    public String username;
    public String password;
    public String nombre;
    public long fechaNacimiento;
    public int contadorDownloads;
    public String fotoPath;
    public String rolUsuario;
    public boolean estado;

    @Override
    public String toString() {
        return "#" + code + " | " + nombre + " (" + username + ") | " + rolUsuario + " | dls:" + contadorDownloads + " | " + (estado ? "ACTIVO" : "DESACTIVO");
    }

}
