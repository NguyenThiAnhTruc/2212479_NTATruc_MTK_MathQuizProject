package com.mycompany.mathgame.ui;

import com.mycompany.mathgame.core.GameState;
import com.mycompany.mathgame.core.Question;
// Lưu ý: tránh trùng javax.swing.Timer -> dùng tên đầy đủ khi tạo mới Timer core
import com.mycompany.mathgame.leaderboard.LeaderboardPort;
import com.mycompany.mathgame.leaderboard.LeaderboardServiceDb;
import com.mycompany.mathgame.strategy.difficulty.DifficultyStrategy;
import com.mycompany.mathgame.strategy.difficulty.StreakBasedDifficulty;
import com.mycompany.mathgame.strategy.question.*;
import com.mycompany.mathgame.strategy.score.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class MathQuizFrame extends JFrame {
    private final Random rnd = new Random();
    private final GameState state = new GameState();
    private QuestionStrategy qGen;
    private final ScoreStrategy scoreStrategy;
    private final DifficultyStrategy difficulty = new StreakBasedDifficulty(1, 10);

    // === DÙNG SQL SERVER: thay JSON -> LeaderboardPort + LeaderboardServiceDb ===
    private final LeaderboardPort leaderboard = new LeaderboardServiceDb(10);

    private long sessionStart;

    private final JLabel lblTitle = new JLabel("🎯 Trò chơi Toán học");
    private final JLabel lblPrompt = new JLabel("Câu hỏi sẽ hiển thị ở đây");
    private final JTextField txtAnswer = new JTextField();
    private final JButton btnSubmit = new JButton("Trả lời");
    private final JButton btnNext = new JButton("Tiếp theo");
    private final JLabel lblInfo = new JLabel("Điểm: 0 | Chuỗi đúng: 0 | Thời gian: 0 ms | Cấp: 1");
    private final JProgressBar pb = new JProgressBar(0, 100);
    private final JLabel lblLevel = new JLabel("Cấp:");
    private final JSpinner spLevel = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JTextField txtPlayer = new JTextField("Người chơi");

    private final JTabbedPane tabs = new JTabbedPane();
    private LeaderboardPanel leaderboardPanel;

    private int total = 0, level = 1, questionsCount = 0;
    private Question current;
    private com.mycompany.mathgame.core.Timer timer; // dùng core.Timer

    public MathQuizFrame() {
        // Gom các chiến lược câu hỏi (Strategy)
        qGen = new RandomComposite(List.of(
                new AdditionStrategy(),
                new MultiplicationStrategy(),
                new DivisionStrategy(),
                new MixWithCarryStrategy()
        ));

        // Tính điểm = Cố định(10/-5) + Thưởng nhanh(<=6s +5) + Thưởng chuỗi(3 câu +10)
        ScoreStrategy fixed = new FixedScoreStrategy(10, 5);
        ScoreStrategy timed = new TimedBonusDecorator(fixed, 6000, 5);
        this.scoreStrategy = new StreakBonusDecorator(new SumScoreComposite(List.of(timed)), 3, 10);

        sessionStart = System.currentTimeMillis();
        caiDatCheDoSang();   // mặc định chế độ sáng
        xayDungGiaoDien();
        ganSuKien();
        cauHoiTiepTheo();
    }

    // ================== Xây dựng UI ==================
    private void xayDungGiaoDien() {
        setTitle("Math Quiz");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);

        // Tạo menu và giao diện chính
        setJMenuBar(taoMenu());

        // Panel chính: vùng câu hỏi và thông tin
        JPanel panelGame = new JPanel(new BorderLayout());
        panelGame.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ----- Thanh trên -----
        // Tiêu đề và thông tin người chơi
        JPanel top = new JPanel(new BorderLayout(10, 0));

        JPanel leftTop = new JPanel(new GridLayout(2, 1, 0, 6));
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 22f));
        leftTop.add(lblTitle);

        JPanel playerRow = new JPanel(new BorderLayout(6, 0));
        playerRow.add(new JLabel("Người chơi:"), BorderLayout.WEST);
        playerRow.add(txtPlayer, BorderLayout.CENTER);
        leftTop.add(playerRow);
        top.add(leftTop, BorderLayout.WEST);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        lblLevel.setFont(lblLevel.getFont().deriveFont(14f));
        rightTop.add(lblLevel);
        rightTop.add(spLevel);
        top.add(rightTop, BorderLayout.EAST);

        panelGame.add(top, BorderLayout.NORTH);

        // ----- Khu vực trung tâm -----
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        lblPrompt.setFont(lblPrompt.getFont().deriveFont(Font.BOLD, 28f));
        lblPrompt.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(lblPrompt, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(12, 0, 0, 0);
        txtAnswer.setFont(txtAnswer.getFont().deriveFont(20f));
        txtAnswer.setHorizontalAlignment(JTextField.CENTER);
        txtAnswer.setToolTipText("Nhập đáp án là số nguyên rồi nhấn Enter hoặc nút Trả lời");
        txtAnswer.setPreferredSize(new Dimension(220, 42));
        center.add(txtAnswer, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(12, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        styleButton(btnSubmit, new Color(70, 130, 180));
        styleButton(btnNext, new Color(60, 170, 90));
        btnRow.add(btnSubmit);
        btnRow.add(btnNext);
        center.add(btnRow, gbc);

        panelGame.add(center, BorderLayout.CENTER);

        // ----- Thanh dưới -----
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        bottom.add(lblInfo, BorderLayout.NORTH);
        pb.setStringPainted(true);
        pb.setToolTipText("Thanh đánh giá tốc độ trả lời (càng nhanh càng đầy)");
        bottom.add(pb, BorderLayout.SOUTH);
        panelGame.add(bottom, BorderLayout.SOUTH);

        btnNext.setEnabled(false);
        pb.setValue(0);

        // Tab "Xếp hạng" (dùng service DB qua LeaderboardPort)
        leaderboardPanel = new LeaderboardPanel(leaderboard);

        tabs.addTab("🎮 Chơi", panelGame);
        tabs.addTab("🏆 Xếp hạng", leaderboardPanel);

        setContentPane(tabs);
    }

    private JMenuBar taoMenu() {
        JMenuBar mb = new JMenuBar();

        JMenu mGame = new JMenu("Trò chơi");
        JMenuItem reset = new JMenuItem("Đặt lại phiên");
        reset.setToolTipText("Đặt lại điểm, chuỗi và cấp; bắt đầu lại từ đầu");
        reset.addActionListener(e -> datLaiPhien());
        mGame.add(reset);

        JMenu mView = new JMenu("Giao diện");
        JMenuItem light = new JMenuItem("Chế độ sáng");
        JMenuItem dark = new JMenuItem("Chế độ tối");
        light.addActionListener(e -> caiDatCheDoSang());
        dark.addActionListener(e -> caiDatCheDoToi());
        mView.add(light);
        mView.add(dark);

        mb.add(mGame);
        mb.add(mView);
        return mb;
    }

    private void ganSuKien() {
        spLevel.addChangeListener(e -> level = (Integer) spLevel.getValue());
        btnSubmit.addActionListener(e -> nopDapAn());
        btnNext.addActionListener(e -> cauHoiTiepTheo());
        txtAnswer.addActionListener(e -> nopDapAn());
    }

    // ================== Luồng hoạt động ==================
    private void cauHoiTiepTheo() {
        level = difficulty.levelFor(state, level, total);
        spLevel.setValue(level);
        current = qGen.next(rnd, level);

        timer = new com.mycompany.mathgame.core.Timer();
        timer.start();

        lblPrompt.setText(current.prompt());
        txtAnswer.setText("");
        txtAnswer.requestFocusInWindow();
        btnSubmit.setEnabled(true);
        btnNext.setEnabled(false);
        pb.setValue(0);
        capNhatThongTin(0);
        questionsCount++;
    }

    private void nopDapAn() {
        if (current == null) return;

        long ms = timer.elapsedMillis();
        boolean correct;
        try {
            int ans = Integer.parseInt(txtAnswer.getText().trim());
            correct = (ans == current.answer());
        } catch (Exception ex) {
            correct = false;
        }

        // Cập nhật chuỗi đúng (streak)
        if (correct) {
            state.incStreak();
        } else {
            state.resetStreak();
        }

        // Tính điểm theo chiến lược
        int earned = scoreStrategy.score(correct, ms, level, state);
        total += earned;

        // Cập nhật UI
        lblPrompt.setText((correct ? "✅ Đúng!" : "❌ Sai!") + "  Đáp án: " + current.answer());
        btnSubmit.setEnabled(false);
        btnNext.setEnabled(true);
        pb.setValue(Math.min(100, (int) Math.max(0, 100 - ms / 50)));
        capNhatThongTin(ms);

        // 🔽 CHỈ lưu/xếp hạng khi người chơi TRẢ LỜI SAI
        if (!correct) {
            themBanGhiXepHang();
            leaderboardPanel.reload();
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn đã trả lời sai.\nĐiểm của bạn đã được lưu vào Bảng xếp hạng!",
                    "Kết thúc lượt",
                    JOptionPane.INFORMATION_MESSAGE
            );
            // (tùy chọn) reset phiên để người chơi bắt đầu lại ngay:
            // datLaiPhien();
        }
    }

    private void themBanGhiXepHang() {
        String player = txtPlayer.getText().trim();
        if (player.isBlank()) player = "Người chơi";
        long took = System.currentTimeMillis() - sessionStart;
        leaderboard.add(player, total, took, state.maxStreak());
    }

    private void capNhatThongTin(long ms) {
        lblInfo.setText(String.format(
                "Điểm: %d | Chuỗi đúng: %d | Thời gian: %d ms | Cấp: %d",
                total, state.streak(), ms, level));
    }

    private void datLaiPhien() {
        total = 0;
        level = 1;
        questionsCount = 0;
        state.resetStreak();
        sessionStart = System.currentTimeMillis();
        cauHoiTiepTheo();
    }

    // ================== Theme / Giao diện ==================
    private void caiDatCheDoSang() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
        UIManager.put("control", new Color(245, 245, 250));
        UIManager.put("nimbusLightBackground", new Color(250, 250, 255));
        UIManager.put("text", Color.BLACK);
        UIManager.put("nimbusBase", new Color(220, 230, 255));
        SwingUtilities.updateComponentTreeUI(this);
        pack();
        setSize(820, 500);
    }

    private void caiDatCheDoToi() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}
        UIManager.put("control", new Color(40, 40, 45));
        UIManager.put("nimbusLightBackground", new Color(55, 55, 60));
        UIManager.put("text", Color.WHITE);
        UIManager.put("nimbusBase", new Color(70, 90, 160));
        UIManager.put("info", new Color(50, 50, 50));
        SwingUtilities.updateComponentTreeUI(this);
        pack();
        setSize(820, 500);
    }

    // ================== Tiện ích ==================
    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
