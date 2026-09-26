package gui;

import event.MachineType;

import javax.swing.*;
import java.awt.*;

/**
 * Modern visual representation of an individual machine card in the laundromat GUI.
 * Shows machine name, live operational status, active customer ID, and animated progress.
 */
public class MachineTile extends JPanel {
    private final MachineType type;
    private final int machineId;

    private final JLabel lblName;
    private final JLabel lblStatus;
    private final JLabel lblCustomer;
    private final JProgressBar progressBar;

    private Timer progressTimer;
    private long cycleStartTime;
    private int cycleDurationMs;

    // Palette
    private static final Color COLOR_BG_IDLE = new Color(248, 250, 252);
    private static final Color COLOR_BG_BUSY = new Color(238, 246, 255);
    private static final Color COLOR_BG_FAILED = new Color(254, 242, 242);

    private static final Color COLOR_STATUS_IDLE = new Color(22, 163, 74);
    private static final Color COLOR_STATUS_BUSY = new Color(37, 99, 235);
    private static final Color COLOR_STATUS_FAILED = new Color(220, 38, 38);

    private static final Color COLOR_BORDER = new Color(226, 232, 240);

    public MachineTile(MachineType type, int machineId) {
        this.type = type;
        this.machineId = machineId;

        setLayout(new BorderLayout(4, 6));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        setPreferredSize(new Dimension(115, 88));
        setBackground(COLOR_BG_IDLE);

        // Header label
        String typePrefix;
        switch (type) {
            case WASHER: typePrefix = "🧺 Washer"; break;
            case DRYER:  typePrefix = "🌀 Dryer"; break;
            case KIOSK:  typePrefix = "💳 Kiosk"; break;
            default:     typePrefix = "Machine";
        }
        lblName = new JLabel(typePrefix + " #" + machineId, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(new Color(51, 65, 85));
        add(lblName, BorderLayout.NORTH);

        // Center panel (Status badge & Customer)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        centerPanel.setOpaque(false);

        lblStatus = new JLabel("● IDLE", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(COLOR_STATUS_IDLE);
        centerPanel.add(lblStatus);

        lblCustomer = new JLabel("Available", SwingConstants.CENTER);
        lblCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCustomer.setForeground(new Color(100, 116, 139));
        centerPanel.add(lblCustomer);

        add(centerPanel, BorderLayout.CENTER);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(100, 6));
        progressBar.setForeground(COLOR_STATUS_BUSY);
        progressBar.setBackground(new Color(226, 232, 240));
        progressBar.setBorderPainted(false);
        add(progressBar, BorderLayout.SOUTH);

        setIdle();
    }

    /**
     * Mark machine as BUSY with customer ID and cycle duration.
     */
    public void setBusy(int customerId, int durationMs) {
        if (progressTimer != null) {
            progressTimer.stop();
        }

        setBackground(COLOR_BG_BUSY);
        lblStatus.setText("● BUSY");
        lblStatus.setForeground(COLOR_STATUS_BUSY);
        lblCustomer.setText("Customer #" + customerId);
        lblCustomer.setForeground(new Color(30, 41, 59));
        progressBar.setForeground(COLOR_STATUS_BUSY);

        this.cycleDurationMs = Math.max(durationMs, 500);
        this.cycleStartTime = System.currentTimeMillis();
        progressBar.setValue(0);

        progressTimer = new Timer(50, e -> {
            long elapsed = System.currentTimeMillis() - cycleStartTime;
            int progress = (int) Math.min(100, (elapsed * 100) / cycleDurationMs);
            progressBar.setValue(progress);
            if (progress >= 100) {
                ((Timer) e.getSource()).stop();
            }
        });
        progressTimer.start();
    }

    /**
     * Mark machine as IDLE.
     */
    public void setIdle() {
        if (progressTimer != null) {
            progressTimer.stop();
            progressTimer = null;
        }

        setBackground(COLOR_BG_IDLE);
        lblStatus.setText("● IDLE");
        lblStatus.setForeground(COLOR_STATUS_IDLE);
        lblCustomer.setText("Available");
        lblCustomer.setForeground(new Color(100, 116, 139));
        progressBar.setValue(0);
    }

    /**
     * Mark machine as FAILED with customer ID.
     */
    public void setFailed(int customerId) {
        if (progressTimer != null) {
            progressTimer.stop();
        }

        setBackground(COLOR_BG_FAILED);
        lblStatus.setText("⚠ FAILED");
        lblStatus.setForeground(COLOR_STATUS_FAILED);
        lblCustomer.setText("Cust #" + customerId + " Retrying");
        lblCustomer.setForeground(COLOR_STATUS_FAILED);
        progressBar.setForeground(COLOR_STATUS_FAILED);
        progressBar.setValue(100);
    }
}