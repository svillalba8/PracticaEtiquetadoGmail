package gamo.villalba.sergio;

import javax.mail.*;
import java.util.*;
import java.util.stream.Collectors;

public class GmailService {
    private static final String HOST = "imap.gmail.com";
    private static final String GMAIL_USER_NAME = "";
    private static final String PASSWORD = "";
    private static final int NUM_MAX_MENSAJES = 7;
    private static final String LABEL_DONE = "Done";
    private static final String LABEL_WORK_IN_PROGRESS = "Work.in.Progress";
    private static final String LABEL_TO_BE_DONE = "To.be.Done";
    private static final List<String> LIST_LABELS = Arrays.asList(LABEL_DONE, LABEL_WORK_IN_PROGRESS, LABEL_TO_BE_DONE);

    public void etiquetarEmails() {
        Store store = null;
        Folder inbox = null;
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);

            store = session.getStore();
            store.connect(HOST, GMAIL_USER_NAME, PASSWORD);

            crearCarpetaEtiqueta(store, LABEL_DONE);
            crearCarpetaEtiqueta(store, LABEL_WORK_IN_PROGRESS);
            crearCarpetaEtiqueta(store, LABEL_TO_BE_DONE);

            inbox = store.getDefaultFolder().getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            int totalMessages = Math.min(messages.length, NUM_MAX_MENSAJES);

            for (int i = 0; i < totalMessages; i++) {
                if (!estaEtiquetado(store, messages[i])) {
                    String label = asignarEtiqueta(i);
                    copiarMensajeParaEtiqueta(store, inbox, messages[i], label);
                }
                else System.out.println("El correo ya está etiquetado: " + messages[i].getSubject());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            cerrarResources(inbox, store);
        }
    }

    private boolean estaEtiquetado(Store store, Message mensaje) throws MessagingException {
        String messageId = obtenerMensajeID(mensaje);

        for (String label : LIST_LABELS) {
            Folder labelFolder = store.getDefaultFolder().getFolder(label);

            if (labelFolder.exists()) {
                labelFolder.open(Folder.READ_ONLY);

                Message[] labeledMessages = labelFolder.getMessages();

                for (Message labeledMessage : labeledMessages) {
                    assert messageId != null;
                    if (messageId.equals(obtenerMensajeID(labeledMessage))) {
                        labelFolder.close(false);
                        return true;
                    }
                }

                labelFolder.close(false);
            }
        }
        return false;
    }

    private String obtenerMensajeID(Message mensaje) throws MessagingException {
        String[] messageIds = mensaje.getHeader("Message-ID");
        return (messageIds != null && messageIds.length > 0) ? messageIds[0] : null;
    }

    public void borrarEtiquetas() {
        Store store = null;
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);

            store = session.getStore();
            store.connect(HOST, GMAIL_USER_NAME, PASSWORD);

            for (String label : LIST_LABELS) {
                Folder labelFolder = store.getDefaultFolder().getFolder(label);
                if (labelFolder.exists()) {
                    labelFolder.delete(true);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al borrar etiquetas: " + e.getMessage());
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    System.out.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    private String asignarEtiqueta(int index) {
        String label = LABEL_TO_BE_DONE;

        if (index >= 4) label = LABEL_DONE;
        if (index == 3) label = LABEL_WORK_IN_PROGRESS;

        return label;
    }

    private void copiarMensajeParaEtiqueta(Store store, Folder inbox, Message mensaje, String etiqueta) throws MessagingException {
        Folder rootFolder = store.getDefaultFolder();
        Folder labelFolder = rootFolder.getFolder(etiqueta);

        if (labelFolder.exists()) inbox.copyMessages(new Message[]{mensaje}, labelFolder);
    }

    private void crearCarpetaEtiqueta(Store store, String etiqueta) throws MessagingException {
        Folder rootFolder = store.getDefaultFolder();
        Folder labelFolder = rootFolder.getFolder(etiqueta);

        if (!labelFolder.exists()) labelFolder.create(Folder.HOLDS_MESSAGES);
    }

    public Map<String, List<String>> obtenerMensajesEtiquetados() {
        Map<String, List<String>> labeledMessages = new HashMap<>();
        Store store = null;
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);

            store = session.getStore();
            store.connect(HOST, GMAIL_USER_NAME, PASSWORD);

            for (String label : LIST_LABELS) {
                Folder rootFolder = store.getDefaultFolder();
                Folder folder = rootFolder.getFolder(label);

                if (folder.exists()) {
                    folder.open(Folder.READ_ONLY);
                    List<String> subjects = Arrays.stream(folder.getMessages())
                            .map(m -> {
                                try {
                                    return m.getSubject() != null ? m.getSubject() : "(Sin Asunto)";
                                } catch (MessagingException e) {
                                    return "(Error al obtener asunto)";
                                }
                            })
                            .collect(Collectors.toList());
                    labeledMessages.put(label, subjects);
                    folder.close(false);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        return labeledMessages;
    }

    private void cerrarResources(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) folder.close(false);
            if (store != null && store.isConnected()) store.close();
        } catch (MessagingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public List<String> buscarCorreosNoEtiquetados() {
        List<String> unlabeledEmails = new ArrayList<>();
        Store store = null;
        Folder inbox = null;
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);

            store = session.getStore();
            store.connect(HOST, GMAIL_USER_NAME, PASSWORD);

            inbox = store.getDefaultFolder().getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Obtener los correos de la bandeja de entrada
            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                String subject = message.getSubject() != null ? message.getSubject() : "(Sin Asunto)";
                unlabeledEmails.add(subject);
            }
        } catch (Exception e) {
            System.out.println("Error al obtener correos sin etiquetar: " + e.getMessage());
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    System.out.println("Error al cerrar la bandeja de entrada: " + e.getMessage());
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    System.out.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
        return unlabeledEmails;
    }
}
