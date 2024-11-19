package org.billing;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Show splash screen
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);

            // Schedule to hide splash screen and show login frame after 10 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    splash.dispose();
                    SwingUtilities.invokeLater(() -> {
                        login login = new login();
                        login.setVisible(true);
                    });
                }
            }, 5000); // 5seconds
        });
    }
}

class SplashScreen extends JWindow {
    public SplashScreen() {
        // Load image
        ImageIcon splashImage = new ImageIcon("C:\\Users\\karki\\IdeaProjects\\Billing system\\src\\main\\resources\\logo.jpg");

        // Create label to display image
        JLabel imageLabel = new JLabel(splashImage);
        add(imageLabel, BorderLayout.CENTER);

        // Set window size to screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);

        // Center the window on the screen
        setLocationRelativeTo(null);
    }
}

