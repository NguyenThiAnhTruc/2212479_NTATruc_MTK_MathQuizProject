package com.mycompany.mathgame.core;
public class Timer {

    // Biến lưu thời điểm bắt đầu (đơn vị: nano giây)
    private long start;

    /**
     * Phương thức bắt đầu đo thời gian.
     * Gọi ngay khi hiển thị câu hỏi mới.
     */
    public void start() {
        start = System.nanoTime();
    }

    /**
     * Trả về thời gian đã trôi qua tính bằng mili giây kể từ khi gọi start().
     *
     * @return số mili giây đã trôi qua
     */
    public long elapsedMillis() {
        return (System.nanoTime() - start) / 1_000_000L;
    }
}