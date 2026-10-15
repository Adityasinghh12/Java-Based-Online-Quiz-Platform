import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * PerformanceChartPanel.java
 * A small reusable bar chart drawn with plain Java2D (Graphics2D) -
 * no external charting library needed. Used by both the Admin's
 * "Performance Reports" tab and the Quiz Creator's "Performance
 * Overview" tab to show average score percentage per quiz.
 */
public class PerformanceChartPanel extends JPanel {
    private List<String> labels;
    private List<Integer> values; // 0-100 percentages

    public PerformanceChartPanel(List<String> labels, List<Integer> values) {
        this.labels = labels;
        this.values = values;
        setPreferredSize(new Dimension(600, 320));
        setBackground(Color.WHITE);
    }

    public void updateData(List<String> labels, List<Integer> values) {
        this.labels = labels;
        this.values = values;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int marginLeft = 50, marginBottom = 70, marginTop = 30, marginRight = 20;
        int chartW = width - marginLeft - marginRight;
        int chartH = height - marginTop - marginBottom;

        // Axes
        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartH);
        g2.drawLine(marginLeft, marginTop + chartH, marginLeft + chartW, marginTop + chartH);

        // Y-axis gridlines (0,25,50,75,100 %)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int pct = 0; pct <= 100; pct += 25) {
            int y = marginTop + chartH - (int) (chartH * (pct / 100.0));
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(marginLeft, y, marginLeft + chartW, y);
            g2.setColor(Color.GRAY);
            g2.drawString(pct + "%", 10, y + 4);
        }

        if (labels == null || labels.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("No quiz attempts yet - chart will populate once students take quizzes.", marginLeft + 10, marginTop + chartH / 2);
            return;
        }

        int n = labels.size();
        int slot = chartW / Math.max(n, 1);
        int barWidth = Math.min(60, slot - 20);

        Color barColor = new Color(26, 79, 160);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));

        for (int i = 0; i < n; i++) {
            int value = Math.max(0, Math.min(100, values.get(i)));
            int barHeight = (int) (chartH * (value / 100.0));
            int x = marginLeft + i * slot + (slot - barWidth) / 2;
            int y = marginTop + chartH - barHeight;

            g2.setColor(barColor);
            g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

            g2.setColor(Color.BLACK);
            g2.drawString(value + "%", x + barWidth / 2 - 10, y - 6);

            // Wrapped label under the bar
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            String label = labels.get(i);
            if (label.length() > 12) label = label.substring(0, 11) + "..";
            FontMetrics fm = g2.getFontMetrics();
            int labelX = x + barWidth / 2 - fm.stringWidth(label) / 2;
            g2.drawString(label, labelX, marginTop + chartH + 16);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        }
    }
}
