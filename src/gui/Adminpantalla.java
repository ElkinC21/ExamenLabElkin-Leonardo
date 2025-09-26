package gui;

import clases.Juego;
import clases.Jugador;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import steamapp.Steam;
import steamapp.Util;

public class Adminpantalla extends JFrame {
    private final Steam steam;
    private final Jugador admin;

    private DefaultListModel<Juego> gamesModel = new DefaultListModel<>();
    private JList<Juego> gamesList = new JList<>(gamesModel);

    private JTextField tfTitle, tfGenre, tfPrice, tfAge, tfSO, tfFoto;
    private JButton btnAddGame, btnUpdPrice, btnRefresh;

    private JTextField tfPlayerCode;
    private JCheckBox cbActivo;
    private JButton btnSetEstado, btnReporte;

    public Adminpantalla(Steam steam, Jugador admin) {
        super("Steam - Admin ("+admin.username+")");
        this.steam = steam;
        this.admin = admin;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initUI();
        refreshGames();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();

       
        JPanel games = new JPanel(new BorderLayout(10,10));
        games.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spList = new JScrollPane(gamesList);

        JPanel right = new JPanel(new GridLayout(0,1,6,6));
        tfTitle = new JTextField();
        tfGenre = new JTextField();
        tfSO = new JTextField(); 
        tfAge = new JTextField();
        tfPrice = new JTextField();
        tfFoto = new JTextField();
        JButton pick = new JButton("Elegir foto");

        right.add(new JLabel("Titulo:")); right.add(tfTitle);
        right.add(new JLabel("Genero:")); right.add(tfGenre);
        right.add(new JLabel("SO (W/M/L):")); right.add(tfSO);
        right.add(new JLabel("Edad minima:")); right.add(tfAge);
        right.add(new JLabel("Precio:")); right.add(tfPrice);
        JPanel fotoRow = new JPanel(new BorderLayout(6,6));
        fotoRow.add(new JLabel("Foto path:"), BorderLayout.WEST);
        fotoRow.add(tfFoto, BorderLayout.CENTER);
        fotoRow.add(pick, BorderLayout.EAST);
        right.add(fotoRow);

        pick.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser(Util.Cpadre);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tfFoto.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel buttons = new JPanel(new GridLayout(1,0,6,6));
        btnAddGame = new JButton("Registrar juego");
        btnUpdPrice = new JButton("Actualizar precio");
        btnRefresh = new JButton("Refrescar catálogo");
        buttons.add(btnAddGame); buttons.add(btnUpdPrice); buttons.add(btnRefresh);

        btnAddGame.addActionListener(this::onAddGame);
        btnUpdPrice.addActionListener(this::onUpdPrice);
        btnRefresh.addActionListener(ev -> refreshGames());

        games.add(spList, BorderLayout.CENTER);
        JPanel east = new JPanel(new BorderLayout(10,10));
        east.add(right, BorderLayout.CENTER);
        east.add(buttons, BorderLayout.SOUTH);
        games.add(east, BorderLayout.EAST);

       
        JPanel players = new JPanel(new GridLayout(0,1,6,6));
        players.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        tfPlayerCode = new JTextField();
        cbActivo = new JCheckBox("Activo", true);
        btnSetEstado = new JButton("Aplicar estado");
        btnReporte = new JButton("Generar reporte cliente ");

        btnSetEstado.addActionListener(this::onSetEstado);
        btnReporte.addActionListener(this::onReporte);

        players.add(new JLabel("Codigo de jugador:")); players.add(tfPlayerCode);
        players.add(cbActivo);
        players.add(btnSetEstado);
        players.add(new JLabel("Reporte: se guardara donde indiques"));
        players.add(btnReporte);

        tabs.addTab("Gestion de juegos", games);
        tabs.addTab("Gestión de jugadores / Reportes", players);

        setContentPane(tabs);
    }

    private void refreshGames() {
        gamesModel.clear();
        try {
            List<Juego> all = steam.imprimirJuego();
            for (Juego g : all) gamesModel.addElement(g);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error IO", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddGame(ActionEvent e) {
        try {
            String t = tfTitle.getText().trim();
            String ge = tfGenre.getText().trim();
            char so = tfSO.getText().trim().isEmpty() ? 'W' : tfSO.getText().trim().toUpperCase().charAt(0);
            int edad = Integer.parseInt(tfAge.getText().trim());
            double price = Double.parseDouble(tfPrice.getText().trim());
            String foto = tfFoto.getText().trim();
            Juego g = steam.addJuego(t, ge, so, edad, price, foto);
            JOptionPane.showMessageDialog(this, "Juego agregado: #"+g.code);
            refreshGames();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdPrice(ActionEvent e) {
        Juego sel = gamesList.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Selecciona un juego"); return; }
        String np = JOptionPane.showInputDialog(this, "Nuevo precio:", sel.precio);
        if (np == null) return;
        try {
            double price = Double.parseDouble(np);
            if (steam.actualizarPrecio(sel.code, price)) {
                JOptionPane.showMessageDialog(this, "Precio actualizado");
                refreshGames();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSetEstado(ActionEvent e) {
        try {
            int code = Integer.parseInt(tfPlayerCode.getText().trim());
            boolean ok = steam.setJugadorActivo(code, cbActivo.isSelected());
            JOptionPane.showMessageDialog(this, ok ? "Estado actualizado" : "No se encontro jugador");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReporte(ActionEvent e) {
        try {
            int code = Integer.parseInt(tfPlayerCode.getText().trim());
            JFileChooser fc = new JFileChooser(Util.Cpadre);
            fc.setSelectedFile(new java.io.File("REPORTE_"+code+".txt"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                boolean ok = steam.reportJugador(code, fc.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, ok? "REPORTE CREADO" : "NO SE PUEDE CREAR REPORTE");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}