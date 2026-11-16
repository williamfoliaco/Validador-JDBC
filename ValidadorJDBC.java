import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ValidadorJDBC extends JFrame {
   private final JTextField driverField = new JTextField("org.h2.Driver", 28);
   private final JTextField urlField = new JTextField("jdbc:h2:mem:prueba", 28);
   private final JTextField userField = new JTextField("sa", 28);
   private final JPasswordField passwordField = new JPasswordField(28);
   private final JTextArea output = new JTextArea(9, 48);
   private final JButton testButton = new JButton("Probar conexion");

    public ValidadorJDBC() {
        super("Validador de conexion JDBC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(690, 540);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        root.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Configuracion de base de datos");
        title.setFont(new Font("SansSerif", Font.BOLD, 21));
        JLabel subtitle = new JLabel("Ingrese los parametros JDBC y valide la conexion.");
        subtitle.setForeground(new Color(85, 95, 110));
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 5));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);
        root.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 5, 8, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 0;
        addField(fields, c, 0, "Clase del driver:", driverField);
        addField(fields, c, 1, "URL JDBC:", urlField);
        addField(fields, c, 2, "Usuario:", userField);
        addField(fields, c, 3, "Contrasena:", passwordField);
        testButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        testButton.setBackground(new Color(30, 93, 160));
        testButton.setForeground(Color.WHITE);
        testButton.setFocusPainted(false);
        testButton.addActionListener(e -> testConnection());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(testButton);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        output.setText("Resultado de la validacion: pendiente"
                + "Nota: debe agregar al proyecto el archivo JAR del driver JDBC correspondiente");
        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultado y detalles del error"));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(fields, BorderLayout.NORTH);
        center.add(buttonPanel, BorderLayout.CENTER);
        center.add(scroll, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addField(JPanel panel, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private void testConnection() {
        String driver = driverField.getText().trim();
        String url = urlField.getText().trim();
        String user = userField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (url.isEmpty()) {
            showError("El campo URL JDBC es obligatorio");
            return;
        }
        if (!url.startsWith("jdbc:")) {
            showError("La URL debe comenzar con jdbc: Verifique el formato indicado por el fabricante del SGBD");
            return;
        }

        testButton.setEnabled(false);
        output.setText("Intentando establecer la conexion");
        try {
            if (!driver.isEmpty()) {
                Class.forName(driver);
            }
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                String db = connection.getMetaData().getDatabaseProductName();
                String version = connection.getMetaData().getDatabaseProductVersion();
                output.setText("CONEXION EXITOSA"
                        + "Sistema gestor: " + db
                        + "Version: " + version
                        + "URL: " + url
                        + "La conexion se cerro automaticamente al finalizar la validacion");
                JOptionPane.showMessageDialog(this, "La conexion se establecio correctamente",
                        "Conexion exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ClassNotFoundException ex) {
            showError("No se encontró el driver JDBC: " + ex.getMessage()
                    + "Nota: verifique que la dependencia H2 este agregada al proyecto."");
        } catch (SQLException ex) {
            StringBuilder details = new StringBuilder("ERROR JDBC")
                    .append("Mensaje: ").append(ex.getMessage()).append()
                    .append("SQLState: ").append(ex.getSQLState()).append()
                    .append("Codigo del proveedor: ").append(ex.getErrorCode()).append();
            SQLException next = ex.getNextException();
            while (next != null) {
                details.append("Excepcion relacionada: ").append(next.getMessage()).append();
                next = next.getNextException();
            }
            showError(details.toString());
        } catch (RuntimeException ex) {
            showError("Error inesperado: " + ex.getMessage());
        } finally {
            testButton.setEnabled(true);
        }
    }

    private void showError(String message) {
        output.setText(message);
        JOptionPane.showMessageDialog(this, message, "No fue posible conectar", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ValidadorJDBC().setVisible(true));
    }
}