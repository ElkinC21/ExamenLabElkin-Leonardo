package gui;

import clases.Jugador;
import com.toedter.calendar.JDateChooser;  // <-- JCalendar
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Date;
import steamapp.Steam;
import steamapp.Util;

public class Loginpantalla extends JFrame {
    private final Steam steam;

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginBtn, registerBtn;

    public Loginpantalla(Steam steam) {
        super("Mini-Steam — Login");
        this.steam = steam;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setContentPane(buildUI());
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(16,16,16,16));
        root.setBackground(new Color(245, 248, 250));

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel form = new JPanel(new GridLayout(0,1,8,8));
        form.setOpaque(false);

        userField = new JTextField();
        passField = new JPasswordField();

        stylizeField(userField);
        stylizeField(passField);

        form.add(new JLabel("Usuario:"));
        form.add(userField);
        form.add(new JLabel("Contraseña:"));
        form.add(passField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setOpaque(false);
        registerBtn = pillButton("Registrar usuario");
        loginBtn = primaryButton("Entrar");
        buttons.add(registerBtn);
        buttons.add(loginBtn);

        loginBtn.addActionListener(this::onLogin);
        registerBtn.addActionListener(this::onRegister);

        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private void onLogin(ActionEvent e) {
        try {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            Jugador pl = steam.loginUsuario(u, p);
            if (pl == null) {
                JOptionPane.showMessageDialog(this, "Credenciales inválidas o usuario inactivo.", "Login", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("ADMIN".equalsIgnoreCase(pl.rolUsuario)) {
                new Adminpantalla(steam, pl).setVisible(true);
            } else {
                new Userpantalla(steam, pl).setVisible(true);
            }
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error IO", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister(ActionEvent e) {
    JDialog d = new JDialog(this, "Registrar usuario", true);
    d.setSize(480, 520);
    d.setLocationRelativeTo(this);

    JPanel panel = new JPanel(new BorderLayout(12,12));
    panel.setBorder(new EmptyBorder(12,12,12,12));

    JPanel form = new JPanel(new GridLayout(0,1,8,8));
    JTextField tfUser = new JTextField();
    JPasswordField tfPass = new JPasswordField();
    JTextField tfNombre = new JTextField();

    com.toedter.calendar.JDateChooser dcNacimiento = new com.toedter.calendar.JDateChooser();
    dcNacimiento.setDate(new java.util.Date());
    dcNacimiento.setDateFormatString("dd/MM/yyyy");

    JRadioButton rbAdmin = new JRadioButton("ADMIN");
    JRadioButton rbNormal = new JRadioButton("NORMAL", true);
    ButtonGroup bg = new ButtonGroup(); bg.add(rbAdmin); bg.add(rbNormal);
    JCheckBox cbActivo = new JCheckBox("Activo", true);
    JTextField tfFoto = new JTextField();

    JLabel preview = new JLabel("Sin foto", SwingConstants.CENTER);
    preview.setPreferredSize(new Dimension(180, 180));
    preview.setBorder(BorderFactory.createLineBorder(new Color(210,220,230)));
    preview.setOpaque(true);
    preview.setBackground(Color.WHITE);

    JButton pick = new JButton("Elegir foto...");

    form.add(new JLabel("Username:")); form.add(tfUser);
    form.add(new JLabel("Password:")); form.add(tfPass);
    form.add(new JLabel("Nombre:"));   form.add(tfNombre);
    form.add(new JLabel("Nacimiento:")); form.add(dcNacimiento);
    JPanel rol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    rol.add(new JLabel("Tipo:")); rol.add(rbAdmin); rol.add(rbNormal);
    form.add(rol);
    form.add(cbActivo);

    JPanel fotoRow = new JPanel(new BorderLayout(6,6));
    fotoRow.add(new JLabel("Foto (ruta):"), BorderLayout.WEST);
    fotoRow.add(tfFoto, BorderLayout.CENTER);
    fotoRow.add(pick, BorderLayout.EAST);

    JPanel center = new JPanel(new BorderLayout(12,12));
    center.add(form, BorderLayout.CENTER);
    center.add(preview, BorderLayout.EAST);

    panel.add(center, BorderLayout.CENTER);
    panel.add(fotoRow, BorderLayout.SOUTH);

    // actualizar preview al elegir
    Runnable refreshPreview = () -> {
        String path = tfFoto.getText().trim();
        if (path.isEmpty()) { preview.setIcon(null); preview.setText("Sin foto"); return; }
        ImageIcon ic = Util.scaled(path, 220, 220);
        if (ic != null) { preview.setIcon(ic); preview.setText(""); }
        else { preview.setIcon(null); preview.setText("Imagen inválida"); }
    };
    pick.addActionListener(ev -> {
        JFileChooser fc = new JFileChooser(Util.ROOT);
        if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
            tfFoto.setText(fc.getSelectedFile().getAbsolutePath());
            refreshPreview.run();
        }
    });
    tfFoto.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e1){ refreshPreview.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e1){ refreshPreview.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e1){ refreshPreview.run(); }
    });

    JButton ok = new JButton("Crear");
    ok.addActionListener(ev -> {
        try {
            java.util.Date date = dcNacimiento.getDate();
            String tipo = rbAdmin.isSelected() ? "ADMIN" : "NORMAL";
            steam.addJugador(
                tfUser.getText().trim(),
                new String(tfPass.getPassword()),
                tfNombre.getText().trim(),
                (date != null ? date.getTime() : System.currentTimeMillis()),
                tfFoto.getText().trim(),
                tipo,
                cbActivo.isSelected()
            );
            JOptionPane.showMessageDialog(d, "Usuario creado");
            d.dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(d, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    south.add(ok);

    d.getContentPane().add(panel, BorderLayout.CENTER);
    d.getContentPane().add(south, BorderLayout.SOUTH);
    d.setVisible(true);
}
    private void stylizeField(JComponent c){
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,220,230)),
                new EmptyBorder(6,8,6,8)
        ));
    }
    private JButton primaryButton(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(0,122,204));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }
    private JButton pillButton(String txt){
        JButton b = new JButton(txt);
        b.setBackground(new Color(230,240,248));
        b.setForeground(new Color(0,90,160));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        return b;
    }
}
