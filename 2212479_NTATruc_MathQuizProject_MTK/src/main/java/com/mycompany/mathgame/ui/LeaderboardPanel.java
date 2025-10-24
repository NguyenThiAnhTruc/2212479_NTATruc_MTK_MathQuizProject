package com.mycompany.mathgame.ui;

import com.mycompany.mathgame.leaderboard.LeaderboardEntry;
import com.mycompany.mathgame.leaderboard.LeaderboardPort;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Bảng xếp hạng – dùng SQL Server thông qua LeaderboardPort
 * Cột: #, Người chơi, Điểm số, Chuỗi đúng, Thời gian (s)
 * Nút: Xóa dòng đã chọn / Xóa toàn bộ / Tải lại
 */
public class LeaderboardPanel extends JPanel {
    private final LeaderboardPort service;

    // Model có thêm cột "Chuỗi đúng" (int)
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"#", "Người chơi", "Điểm số", "Chuỗi đúng", "Thời gian"}, 0) {
        @Override public boolean isCellEditable(int r, int c){ return false; }
        @Override public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 0 -> Integer.class; // #
                case 2 -> Integer.class; // Điểm
                case 3 -> Integer.class; // Chuỗi đúng
                case 4 -> Double.class;  // Thời gian(ms)
                default -> String.class; // Người chơi
            };
        }
    };

    private final JTable table = new JTable(model);
    private final JButton btnReload    = new JButton("Tải lại");
    private final JButton btnClearAll  = new JButton("Xóa toàn bộ");
    private final JButton btnDeleteRow = new JButton("Xóa dòng đã chọn");

    public LeaderboardPanel(LeaderboardPort service){
        super(new BorderLayout(10,10));
        this.service = service;

        setBorder(new EmptyBorder(12,12,12,12));

        // ===== Tiêu đề =====
        JLabel title = new JLabel("BẢNG XẾP HẠNG NGƯỜI CHƠI");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(35,70,130));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        // ===== Bảng =====
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 240, 255));
        table.getTableHeader().setForeground(new Color(20, 50, 120));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Căn giữa cột thứ hạng
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        // Căn phải điểm & chuỗi đúng
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right); // Điểm
        table.getColumnModel().getColumn(3).setCellRenderer(right); // Chuỗi đúng

        // Hiển thị thời gian có 2 chữ số thập phân
        table.getColumnModel().getColumn(4).setCellRenderer(new Double2Renderer());

        // Kẻ sọc hàng xen kẽ
        table.setDefaultRenderer(Object.class, new ZebraRenderer());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new EmptyBorder(0,0,0,0));
        add(sp, BorderLayout.CENTER);

        // ===== Nút thao tác =====
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        styleButton(btnDeleteRow, new Color(255,140,0)); // cam
        styleButton(btnClearAll,  new Color(190, 75, 75));
        styleButton(btnReload,    new Color(70,130,180));
        actions.add(btnDeleteRow);
        actions.add(btnClearAll);
        actions.add(btnReload);
        add(actions, BorderLayout.SOUTH);

        // ===== Sự kiện =====
        btnReload.addActionListener(e -> reload());
        btnClearAll.addActionListener(e -> clearAllConfirm());
        btnDeleteRow.addActionListener(e -> deleteSelected());

        reload(); // nạp dữ liệu ban đầu
    }

    /** Nạp dữ liệu từ DB và hiển thị trên bảng */
    public void reload(){
        List<LeaderboardEntry> list = service.load();
        model.setRowCount(0);
        for (int i = 0; i < list.size(); i++){
            var e = list.get(i);
            double seconds = e.millis() / 1000.0;
            // e.maxStreak() cần có trong LeaderboardEntry (int)
            model.addRow(new Object[]{ i + 1, e.playerName(), e.score(), e.maxStreak(), seconds });
        }
        autoResizeColumns();
    }

    /** Xóa tất cả bản ghi GameSession trong SQL (không xóa Player) */
    private void clearAllConfirm() {
        int ans = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn XÓA TOÀN BỘ bảng xếp hạng (SQL) không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (ans == JOptionPane.YES_OPTION) {
            service.clearAll();
            reload();
            JOptionPane.showMessageDialog(this, "Đã xóa bảng xếp hạng trong SQL Server.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Xóa các phiên chơi của người chơi ở dòng đang chọn */
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String player = model.getValueAt(row, 1).toString();
        int ans = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa TẤT CẢ điểm của người chơi \"" + player + "\"?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (ans == JOptionPane.YES_OPTION) {
            service.deleteByPlayer(player);
            reload();
            JOptionPane.showMessageDialog(this, "Đã xóa bản ghi của " + player + " khỏi SQL Server.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ======= Hỗ trợ giao diện =======
    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void autoResizeColumns() {
        TableColumnModel cm = table.getColumnModel();
        if (cm.getColumnCount() < 5) return;
        cm.getColumn(0).setPreferredWidth(60);   // #
        cm.getColumn(1).setPreferredWidth(230);  // Người chơi
        cm.getColumn(2).setPreferredWidth(110);  // Điểm
        cm.getColumn(3).setPreferredWidth(110);  // Chuỗi đúng
        cm.getColumn(4).setPreferredWidth(120);  // Thời gian
    }

    /** Renderer hiển thị số thực 2 chữ số thập phân */
    static class Double2Renderer extends DefaultTableCellRenderer {
        public Double2Renderer(){ setHorizontalAlignment(SwingConstants.RIGHT); }
        @Override
        protected void setValue(Object value) {
            if (value instanceof Number n) {
                setText(String.format(java.util.Locale.US, "%.2f", n.doubleValue()));
            } else {
                super.setValue(value);
            }
        }
    }

    /** Renderer nền xen kẽ trắng – xanh nhạt cho hàng */
    static class ZebraRenderer extends DefaultTableCellRenderer {
        private final Color even = new Color(249, 251, 255);
        private final Color odd  = Color.WHITE;
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) c.setBackground((row % 2 == 0) ? even : odd);
            return c;
        }
    }
}
