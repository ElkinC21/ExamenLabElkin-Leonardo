package gui;

import clases.DescargasSteam;
import clases.Juego;
import clases.Jugador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import steamapp.Steam;
import steamapp.Util;

public class Userpantalla extends JFrame {
    private final Steam steam;
    private final Jugador user;

    private DefaultListModel<Juego> catalogModel = new DefaultListModel<>();
    private JList<Juego> catalogList = new JList<>(catalogModel);
    private JTextField tfGenreFilter;
    private JComboBox<String> cbSO;
    private JLabel coverPreview = new JLabel();
    private JTextArea gameInfo = new JTextArea(8, 24);

    private DefaultListModel<DescargasSteam> libraryModel = new DefaultListModel<>();
    private JList<DescargasSteam> libraryList = new JList<>(libraryModel);
    private JLabel libCover = new JLabel();
    private JTextArea libInfo = new JTextArea(8, 24);

    public Userpantalla(Steam steam, Jugador user) {
        super("Mini-Steam — Usuario ("+user.username+")");
        this.steam = steam;
        this.user = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 640);
        setLocationRelativeTo(null);
        setContentPane(buildUI());
        loadCatalog(null);
        loadLibrary();
    }

    private JComponent buildUI(){
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Catálogo y Biblioteca");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JButton logout = uiPrimary("Cerrar sesión");
        logout.addActionListener(e -> { dispose(); new Loginpantalla(steam).setVisible(true); });
        top.add(title, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Catálogo", buildCatalogTab());
        tabs.addTab("Mi Biblioteca", buildLibraryTab());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildCatalogTab(){
        JPanel root = paddedPanel();

        JPanel filt = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filt.setOpaque(false);
        tfGenreFilter = new JTextField(12);
        JButton btnFilter = uiPill("Ver por género");
        JButton btnAll = uiPill("Ver todo");
        cbSO = new JComboBox<>(new String[]{"W","M","L"});
        JButton btnDownload = uiPrimary("Descargar seleccionado");

        filt.add(new JLabel("Género:")); filt.add(tfGenreFilter);
        filt.add(btnFilter); filt.add(btnAll);
        filt.add(new JLabel("SO:")); filt.add(cbSO);
        filt.add(btnDownload);

        btnFilter.addActionListener(ev -> loadCatalog(tfGenreFilter.getText().trim()));
        btnAll.addActionListener(ev -> loadCatalog(null));
        btnDownload.addActionListener(this::onDownload);

        catalogList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogList.addListSelectionListener(e -> updateGamePreview());
        JScrollPane scList = new JScrollPane(catalogList);

        JPanel preview = new JPanel(new BorderLayout(8,8));
        preview.setOpaque(false);
        coverPreview.setHorizontalAlignment(SwingConstants.CENTER);
        coverPreview.setPreferredSize(new Dimension(300, 380));
        gameInfo.setEditable(false);
        gameInfo.setLineWrap(true); gameInfo.setWrapStyleWord(true);
        preview.add(coverPreview, BorderLayout.NORTH);
        preview.add(new JScrollPane(gameInfo), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scList,
                preview
        );
        split.setResizeWeight(0.50); 

        root.add(split, BorderLayout.CENTER);
        root.add(filt, BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildLibraryTab(){
        JPanel root = paddedPanel();

        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryList.setCellRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DescargasSteam d) {
                    l.setText(Util.DTime.format(new java.util.Date(d.fecha)) + "  |  " + d.gameName + "  |  $" + String.format("%.2f", d.gamePrice));
                }
                return l;
            }
        });
        libraryList.addListSelectionListener(e -> updateLibraryPreview());

        JScrollPane scList = new JScrollPane(libraryList);

        JPanel preview = new JPanel(new BorderLayout(8,8));
        preview.setOpaque(false);
        libCover.setHorizontalAlignment(SwingConstants.CENTER);
        libCover.setPreferredSize(new Dimension(300, 380));
        libInfo.setEditable(false);
        libInfo.setLineWrap(true); libInfo.setWrapStyleWord(true);
        preview.add(libCover, BorderLayout.NORTH);
        preview.add(new JScrollPane(libInfo), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scList,
                preview
        );
        split.setResizeWeight(0.50); 
        root.add(split, BorderLayout.CENTER);
        return root;
    }

    private void updateGamePreview() {
        Juego sel = catalogList.getSelectedValue();
        coverPreview.setIcon(null);
        gameInfo.setText("");
        if (sel == null) return;
        if (sel.caratulaPath != null && !sel.caratulaPath.isEmpty()) {
            ImageIcon ic = Util.scaled(sel.caratulaPath, 320, 420);
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

    private void updateLibraryPreview(){
        DescargasSteam d = libraryList.getSelectedValue();
        libCover.setIcon(null);
        libInfo.setText("");
        if (d == null) return;

        ImageIcon ic = null;
        if (d.caratulaDownloadPath != null) {
            ic = Util.scaled(d.caratulaDownloadPath, 320, 420);
        } else {
            try {
                Juego g = steam.getJuegoPorCodigo(d.gameCode);
                if (g != null && g.caratulaPath != null && !g.caratulaPath.isEmpty())
                    ic = Util.scaled(g.caratulaPath, 320, 420);
            } catch (IOException ignore) {}
        }
        if (ic != null) libCover.setIcon(ic);

        String soTxt = "-";
        try {
            Juego g = steam.getJuegoPorCodigo(d.gameCode);
            if (g != null) soTxt = Juego.sistemaOperativoText(g.sistemaOp);
        } catch (IOException ignore) {}

        String info = """
                Título: %s
                Precio: $%.2f
                SO: %s
                Fecha descarga: %s
                Download ID: %d
                Código juego: %d
                """.formatted(
                d.gameName, d.gamePrice, soTxt,
                Util.DTime.format(new java.util.Date(d.fecha)),
                d.downloadCode, d.gameCode
        );
        libInfo.setText(info);
    }

    private void loadCatalog(String genre) {
        catalogModel.clear();
        try {
            List<Juego> list = (genre==null || genre.isEmpty()) ? steam.imprimirJuego() : steam.listaJuegosPorGenero(genre);
            for (Juego g : list) catalogModel.addElement(g);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error IO", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLibrary() {
        libraryModel.clear();
        try {
            List<DescargasSteam> dls = steam.listaDownloadsPorJugador(user.code);
            for (DescargasSteam d : dls) libraryModel.addElement(d);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error IO", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDownload(ActionEvent e) {
        Juego sel = catalogList.getSelectedValue();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Selecciona un juego."); return; }
        char so = cbSO.getSelectedItem().toString().charAt(0);
        try {
            if (steam.jugadorYaDescargo(user.code, sel.code)) {
                JOptionPane.showMessageDialog(this, "Ya tienes este juego en tu biblioteca.", "Descarga no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog progress = new JDialog(this, "Descargando...", true);
            progress.setSize(420,150);
            progress.setLocationRelativeTo(this);
            JProgressBar bar = new JProgressBar(0,100);
            bar.setStringPainted(true);
            JButton goLib = uiPill("Ir a Biblioteca");
            goLib.setEnabled(false);
            JPanel p = new JPanel(new BorderLayout(10,10));
            p.setBorder(new EmptyBorder(10,10,10,10));
            JLabel info = new JLabel("Descargando "+sel.titulo+"...");
            p.add(info, BorderLayout.NORTH);
            p.add(bar, BorderLayout.CENTER);
            p.add(goLib, BorderLayout.SOUTH);
            progress.setContentPane(p);

            new Thread(() -> {
                try {
                    for (int i=0;i<=100;i+=7) {
                        final int v=i;
                        SwingUtilities.invokeLater(() -> bar.setValue(v));
                        Thread.sleep(80);
                    }
                    boolean ok = steam.downloadJuego(sel.code, user.code, so);
                    SwingUtilities.invokeLater(() -> {
                        bar.setValue(100);
                        info.setText(ok ? "Descarga completada" : "Descarga no permitida o requisitos no cumplen.");
                        goLib.setEnabled(true);
                        if (ok) {
                            JOptionPane.showMessageDialog(progress,
                                    "FECHA: "+Util.DTime.format(new java.util.Date())+"\n"+
                                    user.nombre+" ha bajado "+sel.titulo+" a un precio de $"+sel.precio,
                                    "Descarga completada", JOptionPane.INFORMATION_MESSAGE);
                            loadLibrary();
                        }
                    });
                } catch (Exception ex2) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(progress, ex2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
            }).start();

            goLib.addActionListener(ev -> { progress.dispose(); });
            progress.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel paddedPanel(){
        JPanel p = new JPanel(new BorderLayout(12,12));
        p.setBorder(new EmptyBorder(12,12,12,12));
        p.setOpaque(true);
        p.setBackground(new Color(245,248,250));
        return p;
    }
    private JButton uiPrimary(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(0,122,204));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }
    private JButton uiPill(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(230,240,248));
        b.setForeground(new Color(0,90,160));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }
}
