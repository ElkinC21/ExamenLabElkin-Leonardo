package steamapp;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;

public class Util {
    public static final File Cpadre = new File("steam");
    public static final File Downloads = new File(Cpadre, "downloads");
    public static final File Codigos = new File(Cpadre, "codes.stm");
    public static final File Games = new File(Cpadre, "games.stm");
    public static final File Players = new File(Cpadre, "player.stm");
    public static final SimpleDateFormat DTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final SimpleDateFormat DBirth = new SimpleDateFormat("yyyy-MM-dd");

    public static void ensureFolders() {
        if (!Cpadre.exists()) Cpadre.mkdirs();
        if (!Downloads.exists()) Downloads.mkdirs();
    }

    public static ImageIcon scaled(String path, int w, int h) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            Image d = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(d);
        } catch (Exception e) {
            return null;
        }
    }

    public static int yearsSince(long birthMillis) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        java.util.Calendar b = java.util.Calendar.getInstance();
        b.setTimeInMillis(birthMillis);
        int years = c.get(java.util.Calendar.YEAR) - b.get(java.util.Calendar.YEAR);
        if (c.get(java.util.Calendar.DAY_OF_YEAR) < b.get(java.util.Calendar.DAY_OF_YEAR)) years--;
        return years;
    }

    public static String soToText(char so) {
        switch (Character.toUpperCase(so)) {
            case 'W': return "Windows";
            case 'M': return "Mac";
            case 'L': return "Linux";
            default: return "Desconocido";
        }
    }

    public static void writeImagePNG(DataOutput out, String imagePath) throws IOException {
        if (imagePath == null) {
            out.writeInt(0);
            return;
        }
        File f = new File(imagePath);
        if (!f.exists()) {
            out.writeInt(0);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage img = ImageIO.read(f);
        if (img == null) {
            out.writeInt(0);
            return;
        }
        ImageIO.write(img, "png", baos);
        byte[] arr = baos.toByteArray();
        out.writeInt(arr.length);
        out.write(arr);
    }

    public static String savePngCopy(String srcPath, String destNameNoExt) throws IOException {
        File dest = new File(Downloads, destNameNoExt + ".png");
        BufferedImage img = ImageIO.read(new File(srcPath));
        ImageIO.write(img, "png", dest);
        return dest.getAbsolutePath();
    }
}