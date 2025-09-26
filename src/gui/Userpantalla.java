package gui;


import clases.DescargasSteam;
import clases.Juego;
import clases.Jugador;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import steamapp.Steam;
import steamapp.Util;

public class Userpantalla extends JFrame {
    private final Steam steam;
    private final Jugador user;

    private DefaultListModel<Juego> catalogoModel = new DefaultListModel<>();
    private JList<Juego> catalogoList = new JList<>(catalogoModel);
    private JTextField GeneroFiltro;
    private JComboBox<String> SO;

    private DefaultListModel<String> perfilModel = new DefaultListModel<>();
    private JList<String> perfilList = new JList<>(perfilModel);

    public Userpantalla(Steam steam, Jugador user) throws IOException {
        super("Steam - Usuario ("+user.username+")");
        this.steam = steam;
        this.user = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);
        initUI();
        loadCatalog(null);
        loadProfile();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();

     
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        GeneroFiltro = new JTextField(12);
        JButton btnFiltro = new JButton("Ver por genero");
        JButton btntodo = new JButton("Ver todo");
        SO = new JComboBox<>(new String[]{"W","M","L"});
        JButton btnDownload = new JButton("Descargar seleccionado");

        top.add(new JLabel("Genero:"));
        top.add(GeneroFiltro);
        top.add(btnFiltro);
        top.add(btntodo);
        top.add(new JLabel("SO:"));
        top.add(SO);
        top.add(btnDownload);

        btnFiltro.addActionListener(ev -> loadCatalog(GeneroFiltro.getText().trim()));
        btntodo.addActionListener(ev -> loadCatalog(null));
        btnDownload.addActionListener(this::Descarga);

        JScrollPane sc = new JScrollPane(catalogoList);
        panel.add(top, BorderLayout.NORTH);
        panel.add(sc, BorderLayout.CENTER);

       
        JPanel prof = new JPanel(new BorderLayout(10,10));
        prof.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel header = new JLabel("Perfil de "+user.nombre+"      Descargas totales: "+user.contadorDownloads);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        JButton btnRefresh = new JButton("Refrescar historial");
        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.WEST); north.add(btnRefresh, BorderLayout.EAST);
        btnRefresh.addActionListener(ev -> {
            try {
                loadProfile();
            } catch (IOException ex) {
                Logger.getLogger(Userpantalla.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        JScrollPane sp = new JScrollPane(perfilList);
        prof.add(north, BorderLayout.NORTH);
        prof.add(sp, BorderLayout.CENTER);

        tabs.addTab("Catalogo", panel);
        tabs.addTab("Mi Perfil / Biblioteca", prof);

        setContentPane(tabs);
    }

    private void loadCatalog(String genre) {
        catalogoModel.clear();
        try {
            List<Juego> list = (genre==null || genre.isEmpty()) ? steam.imprimirJuego() : steam.listaJuegosPorGenero(genre);
            for (Juego g : list) {
                catalogoModel.addElement(g);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error IO", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProfile() throws IOException {
        perfilModel.clear();
        List<DescargasSteam> dls = steam.listaDownloadsPorJugador(user.code);
        for (DescargasSteam d : dls) {
            perfilModel.addElement(
                    Util.DTime.format(new java.util.Date(d.fecha)) + " | Download #"+d.downloadCode+" | "+d.gameName+" | $"+d.gamePrice
            );
        }
    }

    private void Descarga(ActionEvent e) {
        Juego sel = catalogoList.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Selecciona un juego"); return; }
        char so = SO.getSelectedItem().toString().charAt(0);
        try {
            
            JDialog progress = new JDialog(this, "Descargando...", true);
            progress.setSize(420,150);
            progress.setLocationRelativeTo(this);
            JProgressBar barra = new JProgressBar(0,100);
            barra.setStringPainted(true);
            JButton goLib = new JButton("Ir a Biblioteca");
            goLib.setEnabled(false);
            JPanel p = new JPanel(new BorderLayout(10,10));
            p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            JLabel info = new JLabel("Descargando "+sel.titulo+"...");
            p.add(info,BorderLayout.NORTH);
            p.add(barra, BorderLayout.CENTER);
            p.add(goLib, BorderLayout.SOUTH);
            progress.setContentPane(p);

           
            new Thread(() -> {
                try {
                    for (int i=0;i<=100;i+=7) {
                        final int v=i;
                        SwingUtilities.invokeLater(() -> barra.setValue(v));
                        Thread.sleep(80);
                    }
                    boolean ok = steam.downloadJuego(sel.code, user.code, so);
                    SwingUtilities.invokeLater(() -> {
                        barra.setValue(100);
                        info.setText(ok ? "Descarga completada" : "Descarga fallida");
                        goLib.setEnabled(true);
                        if (ok) {
                            JOptionPane.showMessageDialog(progress,
                                    "FECHA: "+Util.DTime.format(new java.util.Date())+"\n"+
                                    "Download completado\n"+
                                    user.nombre+" ha bajado "+sel.titulo+" a un precio de $"+sel.precio,
                                    "Descarga completada", JOptionPane.INFORMATION_MESSAGE);
                            try {
                                loadProfile();
                            } catch (IOException ex) {
                                Logger.getLogger(Userpantalla.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                } catch (Exception ex2) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(progress, ex2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
            }).start();

            goLib.addActionListener(ev -> {
                progress.dispose();
            });

            progress.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}