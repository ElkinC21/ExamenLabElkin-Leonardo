package gui;



import clases.Jugador;
import javax.swing.*;
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
        super("Steam - Login");
        this.steam = steam;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 300);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Iniciar sesion");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0,1,6,6));
        userField = new JTextField();
        passField = new JPasswordField();

        form.add(new JLabel("Usuario:"));
        form.add(userField);
        form.add(new JLabel("Contraseña:"));
        form.add(passField);
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loginBtn = new JButton("Entrar");
        registerBtn = new JButton("Registrar usuario");
        buttons.add(registerBtn);
        buttons.add(loginBtn);
        root.add(buttons, BorderLayout.SOUTH);

        loginBtn.addActionListener(this::onLogin);
        registerBtn.addActionListener(this::onRegister);

        setContentPane(root);
    }

    private void onLogin(ActionEvent e) {
        try {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            Jugador pl = steam.loginUsuario(u, p);
            if (pl == null) {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado", "Login", JOptionPane.WARNING_MESSAGE);
                return;
            }
           
            if ("ADMIN".equalsIgnoreCase(pl.rolUsuario)) {
                new Adminpantalla(steam, pl).setVisible(true);
            } else {
                new Userpantalla(steam, pl).setVisible(true);
            }
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister(ActionEvent e) {
        JDialog d = new JDialog(this, "Registrar usuario", true);
        d.setSize(420, 470);
        d.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridLayout(0,1,6,6));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JTextField tfUser = new JTextField();
        JPasswordField tfPass = new JPasswordField();
        JTextField tfNombre = new JTextField();
        JSpinner spDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JRadioButton rbAdmin = new JRadioButton("ADMIN");
        JRadioButton rbNormal = new JRadioButton("NORMAL", true);
        ButtonGroup bg = new ButtonGroup(); bg.add(rbAdmin); bg.add(rbNormal);
        JCheckBox cbActivo = new JCheckBox("Activo", true);
        JTextField tfFoto = new JTextField();
        JButton pick = new JButton("Elegir foto");

        panel.add(new JLabel("Username:"));    panel.add(tfUser);
        panel.add(new JLabel("Password:"));    panel.add(tfPass);
        panel.add(new JLabel("Nombre:"));      panel.add(tfNombre);
        panel.add(new JLabel("Fecha de Nacimiento :")); panel.add(spDate);
        JPanel rol = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rol.add(new JLabel("Tipo:")); rol.add(rbAdmin); rol.add(rbNormal);
        panel.add(rol);
        panel.add(cbActivo);
        JPanel pf = new JPanel(new BorderLayout(6,6));
        pf.add(new JLabel("Foto (ruta):"), BorderLayout.WEST);
        pf.add(tfFoto, BorderLayout.CENTER);
        pf.add(pick, BorderLayout.EAST);
        panel.add(pf);

        pick.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser(Util.Cpadre);
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                tfFoto.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JButton ok = new JButton("Crear");
        ok.addActionListener(ev -> {
            try {
                java.util.Date date = (java.util.Date) spDate.getValue();
                String tipo = rbAdmin.isSelected() ? "ADMIN" : "NORMAL";
                steam.addJugador(tfUser.getText().trim(), new String(tfPass.getPassword()),
                        tfNombre.getText().trim(), date.getTime(), tfFoto.getText().trim(), tipo, cbActivo.isSelected());
                JOptionPane.showMessageDialog(d, "Usuario creado");
                d.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        d.getContentPane().add(panel, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok);
        d.getContentPane().add(south, BorderLayout.SOUTH);
        d.setVisible(true);
    }
}