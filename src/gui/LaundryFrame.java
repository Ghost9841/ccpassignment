package gui;

import manager.StatisticsManager;
import manager.ResourceManager;
import event.SimulationListener;
import event.MachineType;
import util.Logger;
import util.TimeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

/**
 * Modern, responsive Swing GUI for the Smart Laundry Facility Simulation.
 * Real-time visualization of 6 Washers, 4 Dryers, 2 Payment Kiosks,
 * live queues, synchronized statistics dashboard, and embedded activity log.
 */
public class LaundryFrame extends JFrame implements SimulationListener {
    private final StatisticsManager stats;
    private final ResourceManager resourceManager;

    private final MachineTile[] washerTiles = new MachineTile[6];
    private final MachineTile[] dryerTiles = new MachineTile[4];
    private final MachineTile[] kioskTiles = new MachineTile[2];

    // Queue count labels
    private final JLabel lblWasherQueue = new JLabel("Waiting Queue: 0");
    private final JLabel lblDryerQueue = new JLabel("Waiting Queue: 0");
    private final JLabel lblKioskQueue = new JLabel("Waiting Queue: 0");

    // Dashboard metrics cards
    private final JLabel lblArrived = new JLabel("0 / 50", SwingConstants.CENTER);
    private final JLabel lblServed = new JLabel("0 / 50", SwingConstants.CENTER);
    private final JLabel lblInShop = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblAvgTime = new JLabel("0.0s", SwingConstants.CENTER);
    private final JLabel lblMaxWashers = new JLabel("0 / 6", SwingConstants.CENTER);
    private final JLabel lblMaxDryers = new JLabel("0 / 4", SwingConstants.CENTER);
    private final JLabel lblMaxKiosks = new JLabel("0 / 2", SwingConstants.CENTER);
    private final JLabel lblFailures = new JLabel("0W / 0P", SwingConstants.CENTER);
    private final JLabel lblClock = new JLabel("⏱ 00:00", SwingConstants.RIGHT);

    // Chaos Alert Banner
    private final JPanel chaosAlertPanel = new JPanel(new BorderLayout());
    private final JLabel lblChaosAlert = new JLabel("", SwingConstants.CENTER);

    // Real-time activity log
    private final JTextArea logArea = new JTextArea(12, 60);
    private final JCheckBox chkAutoScroll = new JCheckBox("Auto-Scroll", true);
    private final Consumer<String> logListener = this::appendLog;

    // Simulation run trigger callbacks
    private Runnable onStartNormalSimulation;
    private Runnable onStartChaosSimulation;
    private JButton btnStartNormal;
    private JButton btnStartChaos;

    private long simulationStartTime = 0;
    private volatile boolean isRunning = false;
    private javax.swing.Timer clockTimer;

    public LaundryFrame(StatisticsManager stats, ResourceManager resourceManager) {
        this.stats = stats;
        this.resourceManager = resourceManager;

        // Register with centralized Logger so logs appear live in the GUI
        Logger.addListener(logListener);

        setupModernUI();
        setupClockTimer();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Logger.removeListener(logListener);
                if (clockTimer != null) clockTimer.stop();
                System.exit(0);
            }
        });

        setTitle("Smart Laundry Facility Concurrent Simulation - APU CT074-3-2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 750));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setSimulationTriggers(Runnable startNormal, Runnable startChaos) {
        this.onStartNormalSimulation = startNormal;
        this.onStartChaosSimulation = startChaos;
    }

    private void setupModernUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBackground(new Color(241, 245, 249)); // Slate-100
        rootPanel.setBorder(new EmptyBorder(12, 14, 12, 14));

        // 1. Top Bar: Header & Controls
        rootPanel.add(buildHeaderPanel(), BorderLayout.NORTH);

        // 2. Center: Live Machine Grid & Zones
        JPanel centerPanel = new JPanel(new BorderLayout(10, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(buildDashboardCards(), BorderLayout.NORTH);
        centerPanel.add(buildMachineZones(), BorderLayout.CENTER);
        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom: Real-Time Activity Log Panel
        rootPanel.add(buildLogPanel(), BorderLayout.SOUTH);

        add(rootPanel);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);

        // Left: Branding Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("SMART SELF-SERVICE LAUNDROMAT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Concurrent Multi-Threaded Simulation (Strict Wash → Dry → Pay Order)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(71, 85, 105));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        // Right: Control buttons & Clock
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlsPanel.setOpaque(false);

        btnStartNormal = new JButton("▶ Run Normal (50 Cust)");
        btnStartNormal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStartNormal.setBackground(new Color(37, 99, 235));
        btnStartNormal.setForeground(Color.WHITE);
        btnStartNormal.setFocusPainted(false);
        btnStartNormal.addActionListener(e -> {
            if (onStartNormalSimulation != null) {
                btnStartNormal.setEnabled(false);
                btnStartChaos.setEnabled(false);
                onStartNormalSimulation.run();
            }
        });

        btnStartChaos = new JButton("⚡ Run Congested Chaos (Bonus)");
        btnStartChaos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStartChaos.setBackground(new Color(217, 119, 6));
        btnStartChaos.setForeground(Color.WHITE);
        btnStartChaos.setFocusPainted(false);
        btnStartChaos.addActionListener(e -> {
            if (onStartChaosSimulation != null) {
                btnStartNormal.setEnabled(false);
                btnStartChaos.setEnabled(false);
                onStartChaosSimulation.run();
            }
        });

        lblClock.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblClock.setForeground(new Color(30, 41, 59));
        lblClock.setPreferredSize(new Dimension(100, 30));

        controlsPanel.add(btnStartNormal);
        controlsPanel.add(btnStartChaos);
        controlsPanel.add(lblClock);
        header.add(controlsPanel, BorderLayout.EAST);

        // Chaos Alert Banner setup (initially hidden)
        chaosAlertPanel.setBackground(new Color(254, 226, 226));
        chaosAlertPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(239, 68, 68), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        lblChaosAlert.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChaosAlert.setForeground(new Color(185, 28, 28));
        chaosAlertPanel.add(lblChaosAlert, BorderLayout.CENTER);
        chaosAlertPanel.setVisible(false);

        JPanel northWrapper = new JPanel(new BorderLayout(5, 5));
        northWrapper.setOpaque(false);
        northWrapper.add(header, BorderLayout.NORTH);
        northWrapper.add(chaosAlertPanel, BorderLayout.SOUTH);

        return northWrapper;
    }

    private JPanel buildDashboardCards() {
        JPanel dashboard = new JPanel(new GridLayout(1, 8, 8, 8));
        dashboard.setOpaque(false);

        dashboard.add(createCard("Arrived", lblArrived, new Color(37, 99, 235)));
        dashboard.add(createCard("Served", lblServed, new Color(22, 163, 74)));
        dashboard.add(createCard("In Facility", lblInShop, new Color(79, 70, 229)));
        dashboard.add(createCard("Avg Duration", lblAvgTime, new Color(13, 148, 136)));
        dashboard.add(createCard("Max Washers", lblMaxWashers, new Color(2, 132, 199)));
        dashboard.add(createCard("Max Dryers", lblMaxDryers, new Color(16, 185, 129)));
        dashboard.add(createCard("Max Kiosks", lblMaxKiosks, new Color(245, 158, 11)));
        dashboard.add(createCard("Failures", lblFailures, new Color(225, 29, 72)));

        return dashboard;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel titleLbl = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(100, 116, 139));
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildMachineZones() {
        JPanel zones = new JPanel(new GridLayout(3, 1, 8, 8));
        zones.setOpaque(false);

        // Zone 1: Washers (6 machines)
        zones.add(buildZonePanel("WASHING AREA (6 WASHERS • 4-6s • 5% Mid-Cycle Failure)",
                washerTiles, 6, MachineType.WASHER, lblWasherQueue, new Color(37, 99, 235)));

        // Zone 2: Dryers (4 machines)
        zones.add(buildZonePanel("DRYING AREA (4 DRYERS • 3-5s • Zero Failure)",
                dryerTiles, 4, MachineType.DRYER, lblDryerQueue, new Color(16, 185, 129)));

        // Zone 3: Payment Kiosks (2 kiosks)
        zones.add(buildZonePanel("PAYMENT KIOSKS (2 KIOSKS • 1-2s • 5% Retry / Chaos Mode)",
                kioskTiles, 2, MachineType.KIOSK, lblKioskQueue, new Color(245, 158, 11)));

        return zones;
    }

    private JPanel buildZonePanel(String titleText, MachineTile[] tiles, int count, 
                                  MachineType type, JLabel queueLabel, Color zoneColor) {
        JPanel zone = new JPanel(new BorderLayout(6, 6));
        zone.setBackground(Color.WHITE);
        zone.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        // Zone Header
        JPanel zoneHeader = new JPanel(new BorderLayout());
        zoneHeader.setOpaque(false);

        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(zoneColor);
        zoneHeader.add(lblTitle, BorderLayout.WEST);

        queueLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        queueLabel.setForeground(new Color(71, 85, 105));
        zoneHeader.add(queueLabel, BorderLayout.EAST);

        zone.add(zoneHeader, BorderLayout.NORTH);

        // Grid of machine tiles
        JPanel tilesGrid = new JPanel(new GridLayout(1, count, 8, 8));
        tilesGrid.setOpaque(false);
        for (int i = 0; i < count; i++) {
            tiles[i] = new MachineTile(type, i + 1);
            tilesGrid.add(tiles[i]);
        }
        zone.add(tilesGrid, BorderLayout.CENTER);

        return zone;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Real-Time System Activity & Concurrency Execution Log");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(30, 41, 59));
        logHeader.add(lblTitle, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        chkAutoScroll.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkAutoScroll.setOpaque(false);

        JButton btnClear = new JButton("Clear Log");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnClear.addActionListener(e -> logArea.setText(""));

        controls.add(chkAutoScroll);
        controls.add(btnClear);
        logHeader.add(controls, BorderLayout.EAST);
        panel.add(logHeader, BorderLayout.NORTH);

        // Terminal text area
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(15, 23, 42)); // Slate-900 terminal
        logArea.setForeground(new Color(226, 232, 240)); // Slate-200 text
        logArea.setCaretColor(Color.WHITE);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(800, 160));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void setupClockTimer() {
        clockTimer = new javax.swing.Timer(100, e -> {
            if (isRunning && simulationStartTime > 0) {
                long elapsed = System.currentTimeMillis() - simulationStartTime;
                lblClock.setText("⏱ " + TimeUtil.formatClock(elapsed));
            }
            updateLiveStatsDisplay();
        });
        clockTimer.start();
    }

    public void startSimulationClock() {
        this.simulationStartTime = System.currentTimeMillis();
        this.isRunning = true;
        chaosAlertPanel.setVisible(false);
        if (btnStartNormal != null) btnStartNormal.setEnabled(false);
        if (btnStartChaos != null) btnStartChaos.setEnabled(false);
    }

    public void simulationComplete() {
        this.isRunning = false;
        SwingUtilities.invokeLater(() -> {
            updateLiveStatsDisplay();
            appendLog("=== SIMULATION COMPLETED: ALL CUSTOMERS SUCCESSFULLY SERVED ===");
            if (btnStartNormal != null) btnStartNormal.setEnabled(true);
            if (btnStartChaos != null) btnStartChaos.setEnabled(true);
        });
    }

    private void updateLiveStatsDisplay() {
        int arrived = stats.getCustomersArrived();
        int served = stats.getCustomersServed();
        int inShop = Math.max(0, arrived - served);

        lblArrived.setText(arrived + " / 50");
        lblServed.setText(served + " / 50");
        lblInShop.setText(String.valueOf(inShop));
        lblAvgTime.setText(String.format("%.1fs", stats.getAverageCompletionTimeMs() / 1000.0));
        lblMaxWashers.setText(stats.getMaxConcurrentWashers() + " / 6");
        lblMaxDryers.setText(stats.getMaxConcurrentDryers() + " / 4");
        lblMaxKiosks.setText(stats.getMaxConcurrentKiosks() + " / 2");
        lblFailures.setText(stats.getWasherFailures() + "W / " + stats.getPaymentFailures() + "P");
    }

    public void appendLog(String logLine) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(logLine + "\n");
            if (chkAutoScroll.isSelected()) {
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    // --- SimulationListener Handlers (Marshaled to EDT) ---

    @Override
    public void onMachineBusy(MachineType type, int machineId, int customerId, int durationMs) {
        SwingUtilities.invokeLater(() -> {
            MachineTile tile = getTile(type, machineId);
            if (tile != null) {
                tile.setBusy(customerId, durationMs);
            }
        });
    }

    @Override
    public void onMachineIdle(MachineType type, int machineId) {
        SwingUtilities.invokeLater(() -> {
            MachineTile tile = getTile(type, machineId);
            if (tile != null) {
                tile.setIdle();
            }
        });
    }

    @Override
    public void onMachineFailed(MachineType type, int machineId, int customerId) {
        SwingUtilities.invokeLater(() -> {
            MachineTile tile = getTile(type, machineId);
            if (tile != null) {
                tile.setFailed(customerId);
            }
        });
    }

    @Override
    public void onQueueLengthChanged(MachineType type, int queueLength) {
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case WASHER:
                    lblWasherQueue.setText("Waiting Queue: " + queueLength);
                    lblWasherQueue.setForeground(queueLength > 3 ? new Color(220, 38, 38) : new Color(71, 85, 105));
                    break;
                case DRYER:
                    lblDryerQueue.setText("Waiting Queue: " + queueLength);
                    lblDryerQueue.setForeground(queueLength > 2 ? new Color(220, 38, 38) : new Color(71, 85, 105));
                    break;
                case KIOSK:
                    lblKioskQueue.setText("Waiting Queue: " + queueLength);
                    lblKioskQueue.setForeground(queueLength >= 30 ? new Color(220, 38, 38) : new Color(71, 85, 105));
                    break;
            }
        });
    }

    @Override
    public void onCongestionAlert(String message, int queueLength) {
        SwingUtilities.invokeLater(() -> {
            lblChaosAlert.setText("🚨 " + message);
            chaosAlertPanel.setVisible(true);
        });
    }

    @Override
    public void onCustomerArrival(int customerId, int totalArrived) {
        SwingUtilities.invokeLater(this::updateLiveStatsDisplay);
    }

    @Override
    public void onCustomerCompleted(int customerId, int totalServed, long durationMs) {
        SwingUtilities.invokeLater(this::updateLiveStatsDisplay);
    }

    private MachineTile getTile(MachineType type, int machineId) {
        int index = machineId - 1;
        switch (type) {
            case WASHER:
                return (index >= 0 && index < washerTiles.length) ? washerTiles[index] : null;
            case DRYER:
                return (index >= 0 && index < dryerTiles.length) ? dryerTiles[index] : null;
            case KIOSK:
                return (index >= 0 && index < kioskTiles.length) ? kioskTiles[index] : null;
            default:
                return null;
        }
    }
}