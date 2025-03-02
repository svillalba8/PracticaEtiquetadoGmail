package gamo.villalba.sergio;

import java.awt.*;

public class GmailApp {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new GmailServerUI().setVisible(true));
    }
}
