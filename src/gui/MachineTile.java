package gui;

import event.MachineType;

import javax.swing.*;
import java.awt.*;

/**
 * Visual representation of a single machine in the GUI.
 */
public class MachineTile extends JPanel {
    private final MachineType type;
    private final int machineId;
    private final Color baseColor;
    
    private final JLabel lblStatus;
    private final JLabel lblCustomer;
    private final JProgressBar progressBar;
    
    private Timer animationTimer;
    private int progressValue = 0;
    private boolean isBusy = false;
    private String currentCustomer = "";

    public MachineTile(MachineType type, int machineId, Color baseColor) {
        this.type = type;
        this.machineId = machineId;
        this.baseColor = baseColor;
        
        setLayout(new BorderLayout(2, 2));
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        setPreferredSize(new Dimension(100, 70));
        setBackground(Color.WHITE);
        
        // Name label
        JLabel lblName = new JLabel(type.name() + "-" + machineId, JLabel.CENTER);
        lblName.setFont(new Font("Arial", Font.BOLD, 11));
        add(lblName, BorderLayout.NORTH);
        
        // Center panel for status and customer
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        
        lblStatus = new JLabel("IDLE", JLabel.CENTER);
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 10));
        centerPanel.add(lblStatus, BorderLayout.CENTER);
        
        lblCustomer = new JLabel("", JLabel.CENTER);
        lblCustomer.setFont(new Font("Arial", Font.PLAIN, 9));
        centerPanel.add(lblCustomer, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(80, 10));
        progressBar.setStringPainted(false);
        add(progressBar, BorderLayout.SOUTH);
        
        setIdle();
    }

    /**
     * Set this machine to BUSY state with a customer.
     */
    public void setBusy(String customerName) {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        isBusy = true;
        currentCustomer = customerName;
        progressValue = 0;
        progressBar.setValue(0);
        
        setBackground(baseColor.brighter());
        lblStatus.setText("BUSY");
        lblStatus.setForeground(Color.BLACK);
        lblCustomer.setText(customerName);
        
        // Animate progress bar smoothly
        animationTimer = new Timer(100, e -> {
            progressValue += 5;
            if (progressValue > 100) {
                progressValue = 100;
                animationTimer.stop();
            }
            progressBar.setValue(progressValue);
        });
        animationTimer.start();
    }

    /**
     * Set this machine to IDLE state.
     */
    public void setIdle() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        
        isBusy = false;
        currentCustomer = "";
        progressValue = 0;
        progressBar.setValue(0);
        
        setBackground(Color.WHITE);
        lblStatus.setText("IDLE");
        lblStatus.setForeground(Color.GREEN.darker());
        lblCustomer.setText("");
    }

    /**
     * Set this machine to FAILED state briefly.
     */
    public void setFailed(int customerId) {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        isBusy = false;
        setBackground(Color.RED);
        lblStatus.setText("FAILED");
        lblStatus.setForeground(Color.WHITE);
        lblCustomer.setText("Cust-" + customerId);
        progressBar.setValue(0);
        
        // Reset after 2 seconds
        Timer resetTimer = new Timer(2000, e -> {
            if (!isBusy) {
                setIdle();
            }
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }
}