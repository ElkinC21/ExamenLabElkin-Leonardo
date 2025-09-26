package gui;

import clases.Juego;
import clases.Jugador;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import steamapp.Steam;
import steamapp.Util;

public class Adminpantalla extends JFrame {
    private final Steam steam;
    private final Jugador admin;

    private DefaultListModel<Juego> gamesModel = new DefaultListModel<>();
    private JList<Juego> gamesList = new JList<>(gamesModel);
    private JLabel coverPreview = new JLabel();
    private JTextArea gameInfo = new JTextArea(8, 24);

    private DefaultListModel<Jugador> playersModel = new DefaultListModel<>();
    private JList<Jugador> playersList = new JList<>(playersModel);
    private JLabel lblUser = new JLabel("-");
    private JLabel lblNombre = new JLabel("-");
    private JLabel lblRol = new JLabel("-");
    private JCheckBox cbActivo = new JCheckBox("Activo");
    private JDateChooser dcNacimiento = new JDateChooser();

    private JTextField tfTitle, tfGenre, tfSO, tfAge, tfPrice, tfFoto;
    private JButton btnAddGame, btnUpdPrice, btnRefresh;

    
    private JSplitPane allSplit;       
    private JSplitPane midRightSplit; 
    private JSplitPane playersSplit; 

    public Adminpantalla(Steam steam, Jugador admin) {
        super("Mini-Steam — Admin ("+admin.username+")");
        this.steam = steam;
        this.admin = admin;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(940, 610);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);

        setContentPane(buildUI());

        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { setDividers(); }
            @Override public void componentResized(ComponentEvent e) { setDividers(); }
        });

        refreshGames();
        refreshPlayers();
    }

    private void setDividers() {
       
        if (allSplit != null) {
            allSplit.setResizeWeight(0.22);
            int w = allSplit.getWidth();
            if (w > 0) allSplit.setDividerLocation((int)(w * 0.22));
        }
        if (midRightSplit != null) {
            midRightSplit.setResizeWeight(0.48);
            int w = midRightSplit.getWidth();
            if (w > 0) midRightSplit.setDividerLocation((int)(w * 0.48));
        }
        if (playersSplit != null) {
            playersSplit.setResizeWeight(0.52); 
            int w2 = playersSplit.getWidth();
            if (w2 > 0) playersSplit.setDividerLocation((int)(w2 * 0.52));
        }
    }

    private JComponent buildUI(){
        // Top bar con logout
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(8,10,8,10));
        JLabel title = new JLabel("Panel de administración");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JButton logout = uiPrimary("Cerrar sesión");
        logout.addActionListener(e -> { dispose(); new Loginpantalla(steam).setVisible(true); });
        top.add(title, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        JPanel gamesRoot = paddedPanel();

        JPanel form = new JPanel(new GridLayout(0,1,5,5));
        form.setOpaque(false);
        tfTitle = new JTextField();
        tfGenre = new JTextField();
        tfSO = new JTextField();
        tfAge = new JTextField();
        tfPrice = new JTextField();
        tfFoto = new JTextField();

        stylizeField(tfTitle); stylizeField(tfGenre); stylizeField(tfSO);
        stylizeField(tfAge);   stylizeField(tfPrice); stylizeField(tfFoto);

        JButton pick = uiPill("Elegir foto...");

        form.add(new JLabel("Título:")); form.add(tfTitle);
        form.add(new JLabel("Género:")); form.add(tfGenre);
        form.add(new JLabel("SO (W/M/L):")); form.add(tfSO);
        form.add(new JLabel("Edad mínima:")); form.add(tfAge);
        form.add(new JLabel("Precio:")); form.add(tfPrice);

        JPanel fotoRow = new JPanel(new BorderLayout(6,6));
        fotoRow.setOpaque(false);
        fotoRow.add(new JLabel("Foto path:"), BorderLayout.WEST);
        fotoRow.add(tfFoto, BorderLayout.CENTER);
        fotoRow.add(pick, BorderLayout.EAST);
        form.add(fotoRow);

        pick.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser(Util.Cpadre);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tfFoto.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel buttons = new JPanel(new GridLayout(1,0,6,6));
        buttons.setOpaque(false);
        btnAddGame = uiPrimary("Registrar");
        btnUpdPrice = uiPill("Actualizar precio (seleccionado)");
        btnRefresh = uiPill("Refrescar");
        buttons.add(btnAddGame); buttons.add(btnUpdPrice); buttons.add(btnRefresh);
        btnAddGame.addActionListener(this::onAddGame);
        btnUpdPrice.addActionListener(this::onUpdPrice);
        btnRefresh.addActionListener(ev -> refreshGames());

        JPanel left = new JPanel(new BorderLayout(6,6));
        left.setOpaque(false);
        left.add(form, BorderLayout.CENTER);
        left.add(buttons, BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(230, 100)); 

        gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesList.addListSelectionListener(e -> updateGamePreview());
        gamesList.setFixedCellHeight(22);
        JScrollPane centerList = new JScrollPane(gamesList);
        centerList.setBorder(BorderFactory.createTitledBorder("Catálogo"));

        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setOpaque(false);
        coverPreview.setHorizontalAlignment(SwingConstants.CENTER);
        coverPreview.setPreferredSize(new Dimension(260, 330)); 
        gameInfo.setEditable(false);
        gameInfo.setLineWrap(true); gameInfo.setWrapStyleWord(true);
        right.add(coverPreview, BorderLayout.NORTH);
        right.add(wrapTitled(new JScrollPane(gameInfo), "Información"), BorderLayout.CENTER);

        midRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerList, right);
        midRightSplit.setContinuousLayout(true);
        midRightSplit.setBorder(BorderFactory.createEmptyBorder());

        allSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, midRightSplit);
        allSplit.setContinuousLayout(true);
        allSplit.setBorder(BorderFactory.createEmptyBorder());

        JPanel gamesTab = new JPanel(new BorderLayout(10,10));
        gamesTab.setOpaque(false);
        gamesTab.add(allSplit, BorderLayout.CENTER);

        gamesRoot.add(top, BorderLayout.NORTH);
        gamesRoot.add(gamesTab, BorderLayout.CENTER);

        JPanel playersRoot = paddedPanel();

        playersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playersList.setFixedCellHeight(22);
        playersList.setCellRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Jugador j) {
                    l.setText(j.nombre + "  ("+j.username+")  —  " + (j.estado ? "ACTIVO":"DESACTIVADO"));
                }
                return l;
            }
        });
        playersList.addListSelectionListener(e -> onPlayerSelected());

        JScrollPane leftPlayers = new JScrollPane(playersList);
        leftPlayers.setBorder(BorderFactory.createTitledBorder("Jugadores"));

        JPanel detail = new JPanel(new GridLayout(0,1,6,6));
        detail.setOpaque(false);
        dcNacimiento.setDateFormatString("dd/MM/yyyy");
        detail.add(new JLabel("Usuario:")); detail.add(lblUser);
        detail.add(new JLabel("Nombre:"));  detail.add(lblNombre);
        detail.add(new JLabel("Rol:"));     detail.add(lblRol);
        detail.add(new JLabel("Nacimiento:")); detail.add(dcNacimiento);
        detail.add(cbActivo);

        JButton btnGuardar = uiPrimary("Guardar");
        btnGuardar.addActionListener(this::onGuardarJugador);
        JButton btnReporte = uiPill("Reporte (.txt)");
        btnReporte.addActionListener(this::onReporte);

        JPanel detailSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        detailSouth.setOpaque(false);
        detailSouth.add(btnReporte);
        detailSouth.add(btnGuardar);

        JPanel rightPlayers = new JPanel(new BorderLayout(6,6));
        rightPlayers.setOpaque(false);
        rightPlayers.add(wrapTitled(detail, "Detalle"), BorderLayout.CENTER);
        rightPlayers.add(detailSouth, BorderLayout.SOUTH);

        playersSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPlayers, rightPlayers);
        playersSplit.setContinuousLayout(true);
        playersSplit.setBorder(BorderFactory.createEmptyBorder());

        playersRoot.add(playersSplit, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Gestión de juegos", gamesRoot);
        tabs.addTab("Gestión de jugadores / Reportes", playersRoot);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
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

    private void refreshPlayers() {
        playersModel.clear();
        try {
            List<Jugador> all = steam.listaJugadores();
            for (Jugador j : all) playersModel.addElement(j);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error IO", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGamePreview() {
        Juego sel = gamesList.getSelectedValue();
        coverPreview.setIcon(null);
        gameInfo.setText("");
        if (sel == null) return;
        if (sel.caratulaPath != null && !sel.caratulaPath.isEmpty()) {
            ImageIcon ic = Util.scaled(sel.caratulaPath, 260, 330);
            coverPreview.setIcon(ic);
        }
        String info = """
                Título: %s
                Género: %s
                SO: %s
                Edad mínima: %d
                Precio: $%.2f
                Descargas: %d
                """.formatted(
                sel.titulo, sel.genero, Juego.sistemaOperativoText(sel.sistemaOp),
                sel.edadMinima, sel.precio, sel.contadorDownloads
        );
        gameInfo.setText(info);
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
            clearForm();
            refreshGames();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdPrice(ActionEvent e) {
        Juego sel = gamesList.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Selecciona un juego."); return; }
        String np = JOptionPane.showInputDialog(this, "Nuevo precio:", sel.precio);
        if (np == null) return;
        try {
            double price = Double.parseDouble(np);
            boolean ok = steam.actualizarPrecio(sel.code, price);
            JOptionPane.showMessageDialog(this, ok ? "Precio actualizado." : "No se pudo actualizar.");
            refreshGames();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPlayerSelected(){
        Jugador j = playersList.getSelectedValue();
        if (j == null) {
            lblUser.setText("-"); lblNombre.setText("-"); lblRol.setText("-");
            cbActivo.setSelected(false); dcNacimiento.setDate(null);
            return;
        }
        lblUser.setText(j.username);
        lblNombre.setText(j.nombre);
        lblRol.setText(j.rolUsuario);
        cbActivo.setSelected(j.estado);
        dcNacimiento.setDate(new Date(j.fechaNacimiento));
    }

    private void onGuardarJugador(ActionEvent e){
        Jugador j = playersList.getSelectedValue();
        if (j == null) { JOptionPane.showMessageDialog(this, "Selecciona un jugador."); return; }
        try {
            Date d = dcNacimiento.getDate();
            long nuevo = (d != null ? d.getTime() : j.fechaNacimiento);
            boolean activo = cbActivo.isSelected();

            boolean ok1 = steam.setJugadorActivo(j.code, activo);
            boolean ok2 = steam.actualizarNacimiento(j.code, nuevo);
            JOptionPane.showMessageDialog(this, (ok1||ok2) ? "Cambios guardados." : "No se pudieron aplicar cambios.");
            refreshPlayers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReporte(ActionEvent e) {
        Jugador j = playersList.getSelectedValue();
        if (j == null) { JOptionPane.showMessageDialog(this, "Selecciona un jugador."); return; }
        try {
            JFileChooser fc = new JFileChooser(Util.Cpadre);
            fc.setSelectedFile(new java.io.File("REPORTE_"+j.username+".txt"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                boolean ok = steam.reportJugador(j.code, fc.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, ok? "REPORTE CREADO" : "NO SE PUEDE CREAR REPORTE");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel paddedPanel(){
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(new EmptyBorder(10,10,10,10));
        p.setOpaque(true);
        p.setBackground(new Color(245,248,250));
        return p;
    }

    private JComponent wrapTitled(JComponent c, String title){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(c, BorderLayout.CENTER);
        return panel;
    }

    private JButton uiPrimary(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(0,122,204));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7,12,7,12));
        return b;
    }

    private JButton uiPill(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(230,240,248));
        b.setForeground(new Color(0,90,160));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7,12,7,12));
        return b;
    }

    private void stylizeField(JComponent c){
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,220,230)),
                new EmptyBorder(5,7,5,7)
        ));
    }

    private void clearForm(){
        tfTitle.setText("");
        tfGenre.setText("");
        tfSO.setText("");
        tfAge.setText("");
        tfPrice.setText("");
        tfFoto.setText("");
    }
}
