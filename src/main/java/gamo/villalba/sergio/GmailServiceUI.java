package gamo.villalba.sergio;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GmailServiceUI extends JFrame {
    private final GmailService service = new GmailService();
    private JPanel mainPanel;

    public GmailServiceUI() {
        setTitle("Etiquetador de Gmails");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        iniciarUI();
        setLocationRelativeTo(null);

        service.borrarEtiquetas();
        mostrarEmailsSinEtiqueta();
    }

    private void iniciarUI() {
        mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton labelButton = new JButton("Etiquetar Correos");
        labelButton.addActionListener(e -> etiquetarEmails());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(labelButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }

    private void mostrarEmailsSinEtiqueta() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return service.buscarCorreosNoEtiquetados();
            }

            @Override
            protected void done() {
                try {
                    List<String> unlabeledEmails = get();
                    actualizarCorreosSinEtiquetaUI(unlabeledEmails);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void etiquetarEmails() {
        SwingWorker<Map<String, List<String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, List<String>> doInBackground() {
                service.etiquetarEmails();
                return service.obtenerMensajesEtiquetados();
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<String>> messages = get();
                    actualizarUI(messages);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GmailServiceUI.this, "Error: "
                            + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void actualizarUI(Map<String, List<String>> mensaje) {
        mainPanel.removeAll();

        for (Map.Entry<String, List<String>> entry : mensaje.entrySet()) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));

            DefaultListModel<String> listModel = new DefaultListModel<>();
            entry.getValue().forEach(listModel::addElement);

            JList<String> list = new JList<>(listModel);
            panel.add(new JScrollPane(list), BorderLayout.CENTER);
            panel.add(new JLabel("Total: " + entry.getValue().size()), BorderLayout.NORTH);

            mainPanel.add(panel);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void actualizarCorreosSinEtiquetaUI(List<String> emailsSinEtiquetar) {
        mainPanel.removeAll();
        DefaultListModel<String> unlabeledListModel;

        unlabeledListModel = new DefaultListModel<>();
        emailsSinEtiquetar.forEach(unlabeledListModel::addElement);

        JList<String> unlabeledList = new JList<>(unlabeledListModel);
        JScrollPane scrollPane = new JScrollPane(unlabeledList);

        mainPanel.add(scrollPane);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

}