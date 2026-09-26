package gui;

import manager.StatisticsManager;
import manager.ResourceManager;
import event.SimulationListener;
import event.MachineType;
import util.Logger;
import util.TimeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main Swing GUI for the Laundry Facility Simulation.
 * Implements SimulationListener to receive real-time updates.
 */
public class LaundryFrame extends JFrame implements SimulationListener {
    private final StatisticsManager stats;
    private final ResourceManager resourceManager;
    private final MachineTile[] washerTiles;
    private final MachineTile[] dryerTiles;
    private final MachineTile[] kioskTiles;
    
    // Statistics labels
    private final JLabel lblArrived;
    private final JLabel lblServed;
    private final JLabel lblAvgTime;
    private final JLabel lblWashers;
    private final JLabel lblDryers;
    private final JLabel lblKiosks;
    private final JLabel lblMaxWashers;
    private final JLabel lblMaxDryers;
    private final JLabel lblFailures;
    private final JLabel lblElapsed;
    private final JTextArea logArea;
    
    private long startTime;
    private javax.swing.Timer refreshTimer;
    private boolean simulationComplete = false;

    public LaundryFrame(StatisticsManager stats, ResourceManager resourceManager) {
        this.stats = stats;
        this.resourceManager = resourceManager;
        this.startTime = System.currentTimeMillis();
        
        // Initialize tiles
        washerTiles = new MachineTile[6];
        dryerTiles = new MachineTile[4];
        kioskTiles = new MachineTile[2];
        
        // Create UI components
        lblArrived = new JLabel("Arrived: 0");
        lblServed = new JLabel("Served: 0");
        lblAvgTime = new JLabel("Avg: 0s");
        lblWashers = new JLabel("Washers: 0/6");
        lblDryers = new JLabel("Dryers: 0/4");
        lblKiosks = new JLabel("Kiosks: 0/2");
        lblMaxWashers = new JLabel("Max Washers: 0");
        lblMaxDryers = new JLabel("Max Dryers: 0");
        lblFailures = new JLabel("Failures: 0W/0P");
        lblElapsed = new JLabel("Elapsed: 00:00");
        logArea = new JTextArea(10, 40);
        
        setupUI();
        setupRefreshTimer();
        
        // Ensure proper shutdown
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupUI() {
        setTitle("Smart Laundry Facility Simulation");
        setLayout(new BorderLayout(10, 10));
        
        // Main panel with border
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        
        // Machine sections
        mainPanel.add(createMachineSections(), BorderLayout.CENTER);
        
        // Log panel
        mainPanel.add(createLogPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createTitledBorder("Statistics"));
        
        // Left side - title
        JLabel title = new JLabel("SMART LAUNDRY FACILITY");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        header.add(title, BorderLayout.WEST);
        
        // Right side - elapsed time
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.add(lblElapsed);
        header.add(timePanel, BorderLayout.EAST);
        
        // Center - statistics
        JPanel statsPanel = new JPanel(new GridLayout(2, 5, 10, 5));
        statsPanel.add(lblArrived);
        statsPanel.add(lblServed);
        statsPanel.add(lblAvgTime);
        statsPanel.add(lblWashers);
        statsPanel.add(lblDryers);
        statsPanel.add(lblKiosks);
        statsPanel.add(lblMaxWashers);
        statsPanel.add(lblMaxDryers);
        statsPanel.add(lblFailures);
        statsPanel.add(new JLabel()); // Empty placeholder
        
        header.add(statsPanel, BorderLayout.CENTER);
        
        return header;
    }

    private JPanel createMachineSections() {
        JPanel sections = new JPanel(new GridLayout(3, 1, 10, 10));
        
        // Washing Machines
        sections.add(createMachineSection("Washing Machines", washerTiles, 6, 
                                         MachineType.WASHER, Color.BLUE));
        
        // Dryers
        sections.add(createMachineSection("Dryers", dryerTiles, 4, 
                                         MachineType.DRYER, Color.GREEN));
        
        // Payment Kiosks
        sections.add(createMachineSection("Payment Kiosks", kioskTiles, 2, 
                                         MachineType.KIOSK, Color.ORANGE));
        
        return sections;
    }

    private JPanel createMachineSection(String title, MachineTile[] tiles, 
                                       int count, MachineType type, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JPanel gridPanel = new JPanel(new GridLayout(1, count, 5, 5));
        for (int i = 0; i < count; i++) {
            int machineId = i + 1;
            tiles[i] = new MachineTile(type, machineId, color);
            gridPanel.add(tiles[i]);
        }
        
        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(800, 150));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void setupRefreshTimer() {
        refreshTimer = new javax.swing.Timer(250, e -> refreshStats());
        refreshTimer.start();
    }

    /**
     * Refresh statistics display - called on EDT by timer.
     */
    private void refreshStats() {
        if (simulationComplete) return;
        
        int arrived = stats.getCustomersArrived();
        int served = stats.getCustomersServed();
        double avgMs = stats.getAverageCompletionTimeMs();
        int currentWashers = stats.getCurrentWashers();
        int currentDryers = stats.getCurrentDryers();
        int currentKiosks = stats.getCurrentKiosks();
        int maxWashers = stats.getMaxConcurrentWashers();
        int maxDryers = stats.getMaxConcurrentDryers();
        int washerFails = stats.getWasherFailures();
        int paymentFails = stats.getPaymentFailures();
        
        lblArrived.setText("Arrived: " + arrived);
        lblServed.setText("Served: " + served);
        lblAvgTime.setText("Avg: " + TimeUtil.formatDuration((long) avgMs));
        lblWashers.setText("Washers: " + currentWashers + "/6");
        lblDryers.setText("Dryers: " + currentDryers + "/4");
        lblKiosks.setText("Kiosks: " + currentKiosks + "/2");
        lblMaxWashers.setText("Max Washers: " + maxWashers);
        lblMaxDryers.setText("Max Dryers: " + maxDryers);
        lblFailures.setText("Failures: " + washerFails + "W/" + paymentFails + "P");
        
        long elapsed = System.currentTimeMillis() - startTime;
        lblElapsed.setText("Elapsed: " + TimeUtil.formatDuration(elapsed));
    }

    /**
     * Append a log line on the EDT.
     */
    public void appendLog(String line) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(line + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Called when simulation completes.
     */
    public void simulationComplete() {
        simulationComplete = true;
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        refreshStats();
        appendLog("=== SIMULATION COMPLETE ===");
    }

    private void shutdown() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        System.exit(0);
    }

    // SimulationListener implementations - all called on worker threads, must marshal to EDT

    @Override
    public void onMachineBusy(MachineType type, int machineId, String customerName) {
        SwingUtilities.invokeLater(() -> {
            MachineTile tile = getTile(type, machineId);
            if (tile != null) {
                tile.setBusy(customerName);
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