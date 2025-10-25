package com.mycompany.mathgame.app;

import com.mycompany.mathgame.ui.MathQuizFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Khởi động ứng dụng với Splash Screen có logo (2 giây),
 * sau đó mở cửa sổ chính MathQuizFrame.
 * - Hỗ trợ tiếng Việt
 * - Tự động hiển thị logo từ src/main/resources/logo.png
 * - Có thanh tiến trình hiển thị %
 */
public class MainUI {

    public static void main(String[] args) {
        // Việt hóa nút hộp thoại mặc định
        UIManager.put("OptionPane.okButtonText", "Đồng ý");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");

        // Nimbus Look&Feel (hiện đại)
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        // Chạy UI trên luồng EDT
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.showSplash(() -> {
                MathQuizFrame frame = new MathQuizFrame();
                frame.setVisible(true);
            });
        });
    }

    /** Splash có logo ảnh (nếu có), fallback sang logo text nếu không tìm thấy */
    static class SplashScreen extends JWindow {
        private final JProgressBar progress = new JProgressBar(0, 100);
        private final JLabel title = new JLabel("Math Quiz", SwingConstants.CENTER);
        private final JLabel subtitle = new JLabel("Trò chơi luyện tập Toán học", SwingConstants.CENTER);

        SplashScreen() {
            buildUI();
        }

        private void buildUI() {
            JPanel root = new JPanel(new BorderLayout());
            root.setBorder(new EmptyBorder(18, 18, 18, 18));
            root.setBackground(new Color(245, 245, 250));

            // ===== Header =====
            title.setFont(new Font("Segoe UI", Font.BOLD, 28));
            title.setForeground(new Color(40, 80, 160));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setForeground(new Color(90, 100, 120));

            JPanel top = new JPanel(new GridLayout(2, 1, 0, 6));
            top.setOpaque(false);
            top.add(title);
            top.add(subtitle);
            root.add(top, BorderLayout.NORTH);

            // ===== Center (Logo ảnh hoặc fallback text) =====
            JLabel logoLabel;
            java.net.URL url = getClass().getResource("/logo.png");

            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
            } else {
                logoLabel = new JLabel("Σ  √  ∞  π", SwingConstants.CENTER);
                logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 44));
                logoLabel.setForeground(new Color(60, 120, 200));
            }

            logoLabel.setBorder(new EmptyBorder(18, 0, 18, 0));
            root.add(logoLabel, BorderLayout.CENTER);

            // ===== Bottom progress =====
            progress.setStringPainted(true);
            progress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            root.add(progress, BorderLayout.SOUTH);

            setContentPane(root);
            setSize(560, 280);
            setLocationRelativeTo(null);
        }

        /** Hiển thị splash khoảng 2 giây rồi mở game */
        void showSplash(Runnable onDone) {
            setVisible(true);

            final int durationMs = 2000;
            final int stepMs = 40;
            final int steps = durationMs / stepMs;
            final int[] tick = {0};

            new javax.swing.Timer(stepMs, e -> {
                tick[0]++;
                int percent = Math.min(100, (tick[0] * 100) / steps);
                progress.setValue(percent);
                progress.setString("Đang khởi động... " + percent + "%");

                if (tick[0] >= steps) {
                    ((javax.swing.Timer) e.getSource()).stop();
                    dispose();
                    if (onDone != null) onDone.run();
                }
            }).start();
        }
    }
}
