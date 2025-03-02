package gamo.villalba.sergio;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GmailServerUI extends JFrame {
    private final GmailServer labeler = new GmailServer();
    private JPanel mainPanel;

    public GmailServerUI() {
        setTitle("Etiquetador de Gmail");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        initUI();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        // Panel principal con BorderLayout
        mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Botón para etiquetar correos
        JButton labelButton = new JButton("Etiquetar Correos");
        labelButton.addActionListener(e -> labelEmails());

        // Panel para el botón
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(labelButton);

        // Añadir componentes al JFrame
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }

    private void labelEmails() {
        // Ejecutar el etiquetado en un hilo separado para no bloquear la interfaz
        SwingWorker<Map<String, List<String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, List<String>> doInBackground() {
                labeler.labelEmails(); // Etiquetar los correos
                return labeler.fetchLabeledMessages(); // Obtener los correos etiquetados
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<String>> messages = get();
                    updateUI(messages); // Actualizar la interfaz con los correos etiquetados
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GmailServerUI.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateUI(Map<String, List<String>> messages) {
        // Limpiar el panel principal antes de añadir nuevos componentes
        mainPanel.removeAll();

        // Añadir paneles para cada etiqueta
        for (Map.Entry<String, List<String>> entry : messages.entrySet()) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));

            DefaultListModel<String> listModel = new DefaultListModel<>();
            entry.getValue().forEach(listModel::addElement);

            JList<String> list = new JList<>(listModel);
            panel.add(new JScrollPane(list), BorderLayout.CENTER);
            panel.add(new JLabel("Total: " + entry.getValue().size()), BorderLayout.NORTH);

            mainPanel.add(panel);
        }

        // Actualizar la interfaz
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}