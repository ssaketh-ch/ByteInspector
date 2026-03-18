package com.cppanalyzer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CppAnalyzerGUI extends JFrame {
    // Constants
    private static final String APP_TITLE = "C++ Code Analyzer";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    
    // Components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JButton uploadButton;
    private JButton connectButton;
    private JButton historyButton;
    private JButton dashboardButton;
    private JButton settingsButton;
    private JLabel statusLabel;
    
    // State
    private boolean isConnected = false;
    private File currentFile = null;
    private AnalysisReport currentReport = null;
    private List<AnalysisReport> reportHistory = new ArrayList<>();
    
    // Panels
    private DashboardPanel dashboardPanel;
    private HistoryPanel historyPanel;
    private SettingsPanel settingsPanel;
    private JPanel uploadPanel;
    
    // Server connection
    private ServerConnection serverConnection;
    
    public CppAnalyzerGUI() {
        // Set up frame
        setTitle(APP_TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize components
        initComponents();
        
        // Set up layout
        setupLayout();
        
        // Initialize server connection
        serverConnection = new ServerConnection("localhost", 8080);
        
        // Show dashboard by default
        showPanel(dashboardPanel);
    }
    
    private void initComponents() {
        // Initialize sidebar buttons
        dashboardButton = createSidebarButton("Dashboard", "/icons/dashboard.png");
        uploadButton = createSidebarButton("Upload File", "/icons/upload.png");
        connectButton = createSidebarButton("Connect Server", "/icons/server.png");
        historyButton = createSidebarButton("History", "/icons/history.png");
        settingsButton = createSidebarButton("Settings", "/icons/settings.png");
        
        // Status label
        statusLabel = new JLabel("Status: Not connected");
        statusLabel.setForeground(Color.RED);
        
        // Initialize panels
        dashboardPanel = new DashboardPanel();
        historyPanel = new HistoryPanel(this);
        settingsPanel = new SettingsPanel(this);
        uploadPanel = createUploadPanel();
        
        // Add action listeners
        dashboardButton.addActionListener(e -> showPanel(dashboardPanel));
        uploadButton.addActionListener(e -> showPanel(uploadPanel));
        historyButton.addActionListener(e -> {
            historyPanel.updateReportList(reportHistory);
            showPanel(historyPanel);
        });
        settingsButton.addActionListener(e -> showPanel(settingsPanel));
        connectButton.addActionListener(e -> toggleServerConnection());
    }
    
    private JButton createSidebarButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 40));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            button.setIcon(icon);
        } catch (Exception e) {
            // Fallback if icon not found
            button.setText("● " + text);
        }
        button.setFocusPainted(false);
        return button;
    }
    
    private void setupLayout() {
        // Main layout
        setLayout(new BorderLayout());
        
        // Sidebar panel
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(200, HEIGHT));
        sidebarPanel.setBackground(new Color(50, 50, 70));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(20, 10, 10, 10));
        
        // App title
        JLabel titleLabel = new JLabel(APP_TITLE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components to sidebar
        sidebarPanel.add(titleLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        addButtonToSidebar(dashboardButton);
        addButtonToSidebar(uploadButton);
        addButtonToSidebar(connectButton);
        addButtonToSidebar(historyButton);
        sidebarPanel.add(Box.createVerticalGlue());
        addButtonToSidebar(settingsButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(50, 50, 70));
        statusPanel.add(statusLabel);
        sidebarPanel.add(statusPanel);
        
        // Content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());
        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(uploadPanel, "Upload");
        contentPanel.add(historyPanel, "History");
        contentPanel.add(settingsPanel, "Settings");
        
        // Add main panels to frame
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void addButtonToSidebar(JButton button) {
        button.setMaximumSize(new Dimension(180, 40));
        button.setBackground(new Color(70, 70, 90));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        sidebarPanel.add(button);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    private JPanel createUploadPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(240, 240, 245));
        JLabel titleLabel = new JLabel("Upload C++ File for Analysis");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Main content panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(40, 60, 40, 60));
        centerPanel.setBackground(Color.WHITE);
        
        // File selection area
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BorderLayout());
        filePanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true));
        filePanel.setBackground(new Color(250, 250, 255));
        filePanel.setMaximumSize(new Dimension(600, 200));
        filePanel.setPreferredSize(new Dimension(600, 200));
        
        JPanel filePanelContent = new JPanel();
        filePanelContent.setLayout(new BoxLayout(filePanelContent, BoxLayout.Y_AXIS));
        filePanelContent.setBackground(new Color(250, 250, 255));
        filePanelContent.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JLabel fileIconLabel = new JLabel(new ImageIcon(getClass().getResource("/icons/file.png")));
        fileIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel dragDropLabel = new JLabel("Drag and drop your C++ file here or");
        dragDropLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton browseButton = new JButton("Browse Files");
        browseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseButton.addActionListener(e -> browseFile());
        
        JLabel fileNameLabel = new JLabel("No file selected");
        fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        filePanelContent.add(fileIconLabel);
        filePanelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        filePanelContent.add(dragDropLabel);
        filePanelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        filePanelContent.add(browseButton);
        filePanelContent.add(Box.createRigidArea(new Dimension(0, 10)));
        filePanelContent.add(fileNameLabel);
        
        filePanel.add(filePanelContent, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton analyzeButton = new JButton("Analyze Code");
        analyzeButton.setPreferredSize(new Dimension(150, 40));
        analyzeButton.setBackground(new Color(70, 130, 180));
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setEnabled(false);
        
        analyzeButton.addActionListener(e -> {
            if (currentFile != null && isConnected) {
                SwingWorker<AnalysisReport, Void> worker = new SwingWorker<>() {
                    @Override
                    protected AnalysisReport doInBackground() {
                        JOptionPane.showMessageDialog(CppAnalyzerGUI.this, 
                            "Sending file to server for analysis...", 
                            "Analysis in Progress", 
                            JOptionPane.INFORMATION_MESSAGE);
                        return serverConnection.analyzeFile(currentFile);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            currentReport = get();
                            if (currentReport != null) {
                                reportHistory.add(currentReport);
                                dashboardPanel.displayReport(currentReport);
                                showPanel(dashboardPanel);
                                JOptionPane.showMessageDialog(CppAnalyzerGUI.this, 
                                    "Analysis completed successfully!", 
                                    "Success", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(CppAnalyzerGUI.this, 
                                "Error analyzing file: " + ex.getMessage(), 
                                "Analysis Error", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            } else if (!isConnected) {
                JOptionPane.showMessageDialog(CppAnalyzerGUI.this, 
                    "Please connect to the server first.", 
                    "Not Connected", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(analyzeButton);
        
        // Update UI when file is selected
        browseButton.addActionListener(e -> {
            if (currentFile != null) {
                fileNameLabel.setText("Selected: " + currentFile.getName());
                analyzeButton.setEnabled(isConnected);
            }
        });
        
        centerPanel.add(filePanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(buttonPanel);
        
        // Assemble panel
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select C++ File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("C++ Files", "cpp", "cc", "cxx", "h", "hpp"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            // Update UI to show selected file
            Component[] components = ((JPanel)uploadPanel.getComponent(1)).getComponents();
            JPanel filePanel = (JPanel) components[0];
            JPanel filePanelContent = (JPanel) filePanel.getComponent(0);
            ((JLabel)filePanelContent.getComponent(6)).setText("Selected: " + currentFile.getName());
            
            // Enable analyze button if connected
            Component[] buttonPanelComponents = ((JPanel)uploadPanel.getComponent(1)).getComponents();
            JPanel buttonPanel = (JPanel) buttonPanelComponents[2];
            ((JButton)buttonPanel.getComponent(0)).setEnabled(isConnected);
        }
    }
    
    private void toggleServerConnection() {
        if (!isConnected) {
            // Show connection dialog
            String host = JOptionPane.showInputDialog(this, "Enter server host:", "localhost");
            String portStr = JOptionPane.showInputDialog(this, "Enter server port:", "8080");
            
            if (host != null && portStr != null && !host.isEmpty() && !portStr.isEmpty()) {
                try {
                    int port = Integer.parseInt(portStr);
                    serverConnection = new ServerConnection(host, port);
                    
                    // Try to connect
                    if (serverConnection.connect()) {
                        isConnected = true;
                        connectButton.setText("Disconnect Server");
                        statusLabel.setText("Status: Connected");
                        statusLabel.setForeground(new Color(0, 150, 0));
                        
                        // Enable analyze button if file selected
                        if (currentFile != null) {
                            Component[] components = ((JPanel)uploadPanel.getComponent(1)).getComponents();
                            JPanel buttonPanel = (JPanel) components[2];
                            ((JButton)buttonPanel.getComponent(0)).setEnabled(true);
                        }
                        
                        JOptionPane.showMessageDialog(this, 
                            "Successfully connected to server " + host + ":" + port, 
                            "Connected", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Failed to connect to server " + host + ":" + port, 
                            "Connection Failed", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid port number", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            // Disconnect
            serverConnection.disconnect();
            isConnected = false;
            connectButton.setText("Connect Server");
            statusLabel.setText("Status: Not connected");
            statusLabel.setForeground(Color.RED);
            
            // Disable analyze button
            Component[] components = ((JPanel)uploadPanel.getComponent(1)).getComponents();
            JPanel buttonPanel = (JPanel) components[2];
            ((JButton)buttonPanel.getComponent(0)).setEnabled(false);
        }
    }
    
    public void showPanel(JPanel panel) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        
        if (panel == dashboardPanel) {
            cl.show(contentPanel, "Dashboard");
        } else if (panel == uploadPanel) {
            cl.show(contentPanel, "Upload");
        } else if (panel == historyPanel) {
            cl.show(contentPanel, "History");
        } else if (panel == settingsPanel) {
            cl.show(contentPanel, "Settings");
        }
    }
    
    public void displayReport(AnalysisReport report) {
        currentReport = report;
        dashboardPanel.displayReport(report);
        showPanel(dashboardPanel);
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            CppAnalyzerGUI app = new CppAnalyzerGUI();
            app.setVisible(true);
        });
    }
}