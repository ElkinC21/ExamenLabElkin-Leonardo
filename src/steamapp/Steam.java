/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package steamapp;

import clases.DescargasSteam;
import clases.Juego;
import clases.Jugador;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class Steam {

    private RandomAccessFile codestm;
    private RandomAccessFile juegostm;
    private RandomAccessFile jugadorstm;

    public Steam() {
        try {
            Util.ensureFolders();

            boolean newCodes = !Util.Codigos.exists();
            codestm = new RandomAccessFile(Util.Codigos, "rw");
            juegostm = new RandomAccessFile(Util.Games, "rw");
            jugadorstm = new RandomAccessFile(Util.Players, "rw");

            if (newCodes || codestm.length() == 0) {
                codestm.seek(0);
                codestm.writeInt(1);
                codestm.writeInt(1);
                codestm.writeInt(1);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error inicializando archivos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void close() throws IOException {
        if (codestm != null) {
            codestm.close();
        }
        if (juegostm != null) {
            juegostm.close();
        }
        if (jugadorstm != null) {
            jugadorstm.close();
        }
    }

    public synchronized int gameCode() throws IOException {
        codestm.seek(0);
        int g = codestm.readInt();
        int p = codestm.readInt();
        int d = codestm.readInt();
        codestm.seek(0);
        codestm.writeInt(g + 1);
        codestm.writeInt(p);
        codestm.writeInt(d);
        return g;
    }

    public synchronized int playerCode() throws IOException {
        codestm.seek(0);
        int g = codestm.readInt();
        int p = codestm.readInt();
        int d = codestm.readInt();
        codestm.seek(0);
        codestm.writeInt(g);
        codestm.writeInt(p + 1);
        codestm.writeInt(d);
        return p;
    }

    public synchronized int downloadCode() throws IOException {
        codestm.seek(0);
        int g = codestm.readInt();
        int p = codestm.readInt();
        int d = codestm.readInt();
        codestm.seek(0);
        codestm.writeInt(g);
        codestm.writeInt(p);
        codestm.writeInt(d + 1);
        return d;
    }

    public synchronized Juego addJuego(String titulo, String genero, char so, int edadMinima, double precio, String caratulaPath) throws IOException {
        Juego juego = new Juego();
        juego.code = gameCode();
        juego.titulo = titulo;
        juego.genero = genero;
        juego.sistemaOp = Character.toUpperCase(so);
        juego.edadMinima = edadMinima;
        juego.precio = precio;
        juego.contadorDownloads = 0;
        juego.caratulaPath = caratulaPath == null ? "" : caratulaPath;

        juegostm.seek(juegostm.length());
        juegostm.writeInt(juego.code);
        juegostm.writeUTF(juego.titulo);
        juegostm.writeUTF(juego.genero);
        juegostm.writeChar(juego.sistemaOp);
        juegostm.writeInt(juego.edadMinima);
        juegostm.writeDouble(juego.precio);
        juegostm.writeInt(juego.contadorDownloads);
        juegostm.writeUTF(juego.caratulaPath);
        return juego;
    }

    public synchronized Jugador addJugador(String username, String password, String nombre, long fechaNacimiento, String fotoPath, String rol, boolean activo) throws IOException {
        Jugador jugador = new Jugador();
        jugador.code = playerCode();
        jugador.username = username;
        jugador.password = password;
        jugador.nombre = nombre;
        jugador.fechaNacimiento = fechaNacimiento;
        jugador.contadorDownloads = 0;
        jugador.fotoPath = fotoPath == null ? "" : fotoPath;
        jugador.rolUsuario = rol;
        jugador.estado = activo;

        jugadorstm.seek(jugadorstm.length());
        jugadorstm.writeInt(jugador.code);
        jugadorstm.writeUTF(jugador.username);
        jugadorstm.writeUTF(jugador.password);
        jugadorstm.writeUTF(jugador.nombre);
        jugadorstm.writeLong(jugador.fechaNacimiento);
        jugadorstm.writeInt(jugador.contadorDownloads);
        jugadorstm.writeUTF(jugador.fotoPath);
        jugadorstm.writeUTF(jugador.rolUsuario);
        jugadorstm.writeBoolean(jugador.estado);
        return jugador;
    }

    public synchronized Juego buscarJuego(int code) throws IOException {
        juegostm.seek(0);
        while (juegostm.getFilePointer() < juegostm.length()) {
            long recStart = juegostm.getFilePointer();
            int codeN = juegostm.readInt();
            String titulo = juegostm.readUTF();
            String genero = juegostm.readUTF();
            char so = juegostm.readChar();
            int edadMin = juegostm.readInt();
            double precio = juegostm.readDouble();
            int downloads = juegostm.readInt();
            String foto = juegostm.readUTF();
            if (codeN == code) {
                Juego juego = new Juego();
                juego.code = codeN;
                juego.titulo = titulo;
                juego.genero = genero;
                juego.sistemaOp = so;
                juego.edadMinima = edadMin;
                juego.precio = precio;
                juego.contadorDownloads = downloads;
                juego.caratulaPath = foto;
                return juego;
            }
        }
        return null;
    }

    public synchronized Jugador buscarJugador(int code) throws IOException {
        jugadorstm.seek(0);
        while (jugadorstm.getFilePointer() < jugadorstm.length()) {
            int codeN = jugadorstm.readInt();
            String username = jugadorstm.readUTF();
            String password = jugadorstm.readUTF();
            String nombre = jugadorstm.readUTF();
            long nacimiento = jugadorstm.readLong();
            int downloads = jugadorstm.readInt();
            String foto = jugadorstm.readUTF();
            String rol = jugadorstm.readUTF();
            boolean estado = jugadorstm.readBoolean();
            if (codeN == code) {
                Jugador jugador = new Jugador();
                jugador.code = codeN;
                jugador.username = username;
                jugador.password = password;
                jugador.nombre = nombre;
                jugador.fechaNacimiento = nacimiento;
                jugador.contadorDownloads = downloads;
                jugador.fotoPath = foto;
                jugador.rolUsuario = rol;
                jugador.estado = estado;
                return jugador;
            }
        }
        return null;
    }

    public synchronized Jugador loginUsuario(String username, String password) throws IOException {
        jugadorstm.seek(0);
        while (jugadorstm.getFilePointer() < jugadorstm.length()) {
            int codeN = jugadorstm.readInt();
            String usuario = jugadorstm.readUTF();
            String contra = jugadorstm.readUTF();
            String nombre = jugadorstm.readUTF();
            long nacimiento = jugadorstm.readLong();
            int downloads = jugadorstm.readInt();
            String foto = jugadorstm.readUTF();
            String rol = jugadorstm.readUTF();
            boolean estado = jugadorstm.readBoolean();
            if (usuario.equals(username) && contra.equals(password) && estado) {
                Jugador jugador = new Jugador();
                jugador.code = codeN;
                jugador.username = usuario;
                jugador.password = contra;
                jugador.nombre = nombre;
                jugador.fechaNacimiento = nacimiento;
                jugador.contadorDownloads = downloads;
                jugador.fotoPath = foto;
                jugador.rolUsuario = rol;
                jugador.estado = estado;
                return jugador;
            }
        }
        return null;
    }

    public synchronized List<Juego> listaJuegos() throws IOException {
        ArrayList<Juego> list = new ArrayList<>();
        juegostm.seek(0);
        while (juegostm.getFilePointer() < juegostm.length()) {
            Juego juego = new Juego();
            juego.code = juegostm.readInt();
            juego.titulo = juegostm.readUTF();
            juego.genero = juegostm.readUTF();
            juego.sistemaOp = juegostm.readChar();
            juego.edadMinima = juegostm.readInt();
            juego.precio = juegostm.readDouble();
            juego.contadorDownloads = juegostm.readInt();
            juego.caratulaPath = juegostm.readUTF();
            list.add(juego);
        }
        return list;
    }

    public synchronized boolean actualizarPrecio(int codeJuego, double nuevoPrecio) throws IOException {
        juegostm.seek(0);
        while (juegostm.getFilePointer() < juegostm.length()) {
            long pos = juegostm.getFilePointer();
            int code = juegostm.readInt();
            juegostm.readUTF();               
            juegostm.readUTF();               
            juegostm.readChar();             
            juegostm.readInt();               
            long posPrecio = juegostm.getFilePointer();
            double price = juegostm.readDouble();
            if (code == codeJuego) {
                juegostm.seek(posPrecio);
                juegostm.writeDouble(nuevoPrecio);
                return true;
            }
            juegostm.readInt();               
            juegostm.readUTF();               
        }
        return false;
    }

    public synchronized boolean setJugadorActivo(int codeJugador, boolean activo) throws IOException {
        jugadorstm.seek(0);
        while (jugadorstm.getFilePointer() < jugadorstm.length()) {
            long pos = jugadorstm.getFilePointer();
            int code = jugadorstm.readInt();
            jugadorstm.readUTF(); 
            jugadorstm.readUTF();
            jugadorstm.readUTF(); 
            jugadorstm.readLong(); 
            jugadorstm.readInt(); 
            jugadorstm.readUTF(); 
            jugadorstm.readUTF(); 
            long posEstado = jugadorstm.getFilePointer();
            boolean cur = jugadorstm.readBoolean();
            if (code == codeJugador) {
                jugadorstm.seek(posEstado);
                jugadorstm.writeBoolean(activo);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean downloadJuego(int juegoCode, int jugadorCode, char sistemaOperativo) throws IOException {
        Juego juego = buscarJuego(juegoCode);
        Jugador jugador = buscarJugador(jugadorCode);
        if (juego == null || jugador == null) {
            return false;
        }
        if (!jugador.estado) {
            return false;
        }
        if (Character.toUpperCase(sistemaOperativo) != Character.toUpperCase(juego.sistemaOp)) {
            return false;
        }

        int edad = Util.yearsSince(jugador.fechaNacimiento);
        if (edad < juego.edadMinima) {
            return false;
        }

        int downloadCode = downloadCode();
        long fechaHoy = System.currentTimeMillis();

        File fileSalida = new File(Util.Downloads, "download_" + downloadCode + ".stm");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileSalida)))) {
            out.writeInt(downloadCode);
            out.writeInt(jugador.code);
            out.writeUTF(jugador.nombre);
            out.writeInt(juego.code);
            out.writeUTF(juego.titulo);

            Util.writeImagePNG(out, juego.caratulaPath);

            out.writeDouble(juego.precio);
            out.writeLong(fechaHoy);
        }

        
        juegostm.seek(0);
        while (juegostm.getFilePointer() < juegostm.length()) {
            long pos = juegostm.getFilePointer();
            int code = juegostm.readInt();
            juegostm.readUTF(); 
            juegostm.readUTF(); 
            juegostm.readChar(); 
            juegostm.readInt(); 
            juegostm.readDouble(); 
            long posDownload = juegostm.getFilePointer();
            int download = juegostm.readInt();
            if (code == juego.code) {
                juegostm.seek(posDownload);
                juegostm.writeInt(download + 1);
                juegostm.readUTF();
                break;
            }
            juegostm.readUTF(); 
        }

        
        jugadorstm.seek(0);
        while (jugadorstm.getFilePointer() < jugadorstm.length()) {
            int code = jugadorstm.readInt();
            jugadorstm.readUTF(); 
            jugadorstm.readUTF(); 
            jugadorstm.readUTF(); 
            jugadorstm.readLong();
            long posDownload = jugadorstm.getFilePointer();
            int download = jugadorstm.readInt();
            jugadorstm.readUTF(); 
            jugadorstm.readUTF(); 
            jugadorstm.readBoolean(); 
            if (code == jugador.code) {
                jugadorstm.seek(posDownload);
                jugadorstm.writeInt(download + 1);
                break;
            }
        }

        String copy = null;
        try {
            if (juego.caratulaPath != null && !juego.caratulaPath.isEmpty()) {
                copy = Util.savePngCopy(juego.caratulaPath, "game_" + juego.code + "_download_" + downloadCode);
            }
        } catch (Exception ignore) {
        }

        return true;
    }

    public synchronized boolean reportJugador(int codeJugador, String txtFile) throws IOException {
        Jugador jugador = buscarJugador(codeJugador);
        if (jugador == null) {
            return false;
        }

        File[] files = Util.Downloads.listFiles((dir, name) -> name.startsWith("download_") && name.endsWith(".stm"));
        if (files == null) {
            files = new File[0];
        }

        ArrayList<DescargasSteam> historial = new ArrayList<>();
        for (File f : files) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
                int downloadCode = in.readInt();
                int jugadorCode = in.readInt();
                String jugadorName = in.readUTF();
                int juegoCode = in.readInt();
                String juegoName = in.readUTF();

                int imgLen = in.readInt();
                if (imgLen > 0) {
                    in.skipBytes(imgLen);
                }

                double precio = in.readDouble();
                long fecha = in.readLong();
                if (jugadorCode == jugador.code) {
                    DescargasSteam ds = new DescargasSteam();
                    ds.downloadCode = downloadCode;
                    ds.jugadorCode = jugadorCode;
                    ds.jugadorNombre = jugadorName;
                    ds.gameCode = juegoCode;
                    ds.gameName = juegoName;
                    ds.gamePrice = precio;
                    ds.fecha = fecha;
                    historial.add(ds);
                }
            } catch (Exception ignore) {
            }
        }
        historial.sort(Comparator.comparingLong(a -> a.fecha));

        // Escribir 
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"))) {
            int edad = Util.yearsSince(jugador.fechaNacimiento);
            pw.println("REPORTE CLIENTE: " + jugador.nombre + " (username: " + jugador.username + ")");
            pw.println("Código cliente: " + jugador.code);
            pw.println("Fecha de nacimiento: " + Util.DBirth.format(new java.util.Date(jugador.fechaNacimiento)) + " (" + edad + " años)");
            pw.println("Estado: " + (jugador.estado ? "ACTIVO" : "DESACTIVO"));
            pw.println("Total downloads: " + jugador.contadorDownloads);
            pw.println();
            pw.println("HISTORIAL DE DESCARGAS:");
            pw.println("FECHA(YYYY-MM-DD) | DOWNLOAD ID | GAME CODE | GAME NAME | PRICE | GENRE");
            for (DescargasSteam ds : historial) {
                Juego juego = buscarJuego(ds.gameCode);
                String genero = (juego != null ? juego.genero : "-");
                pw.printf("%s | %d | %d | %s | %.2f | %s%n",
                        Util.DBirth.format(new java.util.Date(ds.fecha)),
                        ds.downloadCode, ds.gameCode, ds.gameName, ds.gamePrice, genero);
            }
        }
        return true;
    }

    public synchronized List<Juego> listaJuegosPorGenero(String genero) throws IOException {
        ArrayList<Juego> r = new ArrayList<>();
        for (Juego juego : listaJuegos()) {
            if (juego.genero.equalsIgnoreCase(genero)) {
                r.add(juego);
            }
        }
        return r;
    }

    public synchronized List<DescargasSteam> listaDownloadsPorJugador(int jugadorCode) throws IOException {
        ArrayList<DescargasSteam> list = new ArrayList<>();
        File[] files = Util.Downloads.listFiles((dir, name) -> name.startsWith("download_") && name.endsWith(".stm"));
        if (files == null) {
            return list;
        }
        for (File f : files) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
                DescargasSteam ds = new DescargasSteam();
                ds.downloadCode = in.readInt();
                ds.jugadorCode = in.readInt();
                ds.jugadorNombre = in.readUTF();
                ds.gameCode = in.readInt();
                ds.gameName = in.readUTF();

                int imgLen = in.readInt();
                if (imgLen > 0) {
                    in.skipBytes(imgLen);
                }

                ds.gamePrice = in.readDouble();
                ds.fecha = in.readLong();

                if (ds.jugadorCode == jugadorCode) {
                    File guess = new File(Util.Downloads, "game_" + ds.gameCode + "_download_" + ds.downloadCode + ".png");
                    ds.caratulaDownloadPath = guess.exists() ? guess.getAbsolutePath() : null;
                    list.add(ds);
                }
            } catch (Exception ignore) {
            }
        }
        Collections.sort(list, new Comparator<DescargasSteam>() {
            @Override
            public int compare(DescargasSteam o1, DescargasSteam o2) {
                return Long.compare(o2.fecha, o1.fecha); 
            }
        });
        return list;
    }

    public synchronized List<Juego> imprimirJuego() throws IOException {
        return listaJuegos();
    }
}
