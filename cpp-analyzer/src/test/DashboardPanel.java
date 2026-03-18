package com.cppanalyzer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardPanel extends JPanel {
    // Chart components
    private JPanel metricsPanel;
    private JPanel warningsPanel;
    private JPanel codeQualityPanel;
    private JPanel memoryLeaksPanel;
    private JPanel overviewPanel;
    
    // Data display components
    private JLabel fileNameLabel;
    private JLabel analysisDateLabel;
    private JLabel qualityScoreLabel;
    private JLabel lineCountLabel;
    private JLabel codeLineCountLabel;
    private JTextArea warningsTextArea;
    private JPanel memoryChart;
    private JPanel complexityChart;
    private JPanel codeMetricsChart;
    
    // Current report
    private AnalysisReport currentReport;
    
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        
        // Create dashboard content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        // Create dashboard sections
        createDashboardSections(contentPanel);
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        
        // Create scrollable content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Initialize with empty data
        displayEmptyDashboard();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 245));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("C++ Code Analysis Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JPanel fileInfoPanel = new JPanel();
        fileInfoPanel.setLayout(new GridLayout(2, 1));
        fileInfoPanel.setBackground(new Color(240, 240, 245));
        
        fileNameLabel = new JLabel("No file analyzed");
        fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        analysisDateLabel = new JLabel("");
        analysisDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        fileInfoPanel.add(fileNameLabel);
        fileInfoPanel.add(analysisDateLabel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(fileInfoPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private void createDashboardSections(JPanel contentPanel) {
        // Overview section
        overviewPanel = createPanel("Overview");
        overviewPanel.setLayout(new GridLayout(1, 3, 15, 0));
        overviewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Create overview cards
        JPanel qualityCard = createMetricCard("Code Quality Score", "0/100", "A");
        qualityScoreLabel = (JLabel) qualityCard.getComponent(1);
        
        JPanel linesCard = createMetricCard("Total Lines", "0", "");
        lineCountLabel = (JLabel) linesCard.getComponent(1);
        
        JPanel codeCard = createMetricCard("Code Lines", "0 (0%)", "");
        codeLineCountLabel = (JLabel) codeCard.getComponent(1);
        
        overviewPanel.add(qualityCard);
        overviewPanel.add(linesCard);
        overviewPanel.add(codeCard);
        
        // Code Metrics section
        metricsPanel = createPanel("Code Metrics");
        metricsPanel.setLayout(new BorderLayout());
        metricsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        codeMetricsChart = new JPanel();
        codeMetricsChart.setBackground(Color.WHITE);
        codeMetricsChart.setLayout(new BorderLayout());
        JLabel metricsPlaceholder = new JLabel("No metrics data to display", SwingConstants.CENTER);
        codeMetricsChart.add(metricsPlaceholder, BorderLayout.CENTER);
        
        metricsPanel.add(codeMetricsChart, BorderLayout.CENTER);
        
        // Warnings section
        warningsPanel = createPanel("Warnings & Issues");
        warningsPanel.setLayout(new BorderLayout());
        warningsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        warningsTextArea = new JTextArea();
        warningsTextArea.setEditable(false);
        warningsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane warningsScrollPane = new JScrollPane(warningsTextArea);
        warningsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        warningsPanel.add(warningsScrollPane, BorderLayout.CENTER);
        
        // Memory Management section
        memoryLeaksPanel = createPanel("Memory Management");
        memoryLeaksPanel.setLayout(new BorderLayout());
        memoryLeaksPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        memoryChart = new JPanel();
        memoryChart.setBackground(Color.WHITE);
        memoryChart.setLayout(new BorderLayout());
        JLabel memoryPlaceholder = new JLabel("No memory data to display", SwingConstants.CENTER);
        memoryChart.add(memoryPlaceholder, BorderLayout.CENTER);
        
        memoryLeaksPanel.add(memoryChart, BorderLayout.CENTER);
        
        // Complexity section
        codeQualityPanel = createPanel("Complexity Analysis");
        codeQualityPanel.setLayout(new BorderLayout());
        codeQualityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        complexityChart = new JPanel();
        complexityChart.setBackground(Color.WHITE);
        complexityChart.setLayout(new BorderLayout());
        JLabel complexityPlaceholder = new JLabel("No complexity data to display", SwingConstants.CENTER);
        complexityChart.add(complexityPlaceholder, BorderLayout.CENTER);
        
        codeQualityPanel.add(complexityChart, BorderLayout.CENTER);
        
        // Add all sections to content panel
        contentPanel.add(overviewPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(metricsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(warningsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(memoryLeaksPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(codeQualityPanel);
    }
    
    private JPanel createPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        return panel;
    }
    
    private JPanel createMetricCard(String title, String value, String grade) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel gradeLabel = new JLabel(grade);
        if (!grade.isEmpty()) {
            gradeLabel.setFont(new Font("Arial", Font.BOLD, 18));
            gradeLabel.setForeground(new Color(70, 130, 180));
        } else {
            gradeLabel.setText(" ");
        }
        gradeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gradeLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        
        card.add(titleLabel);
        card.add(valueLabel);
        card.add(gradeLabel);
        
        return card;
    }
    
    private void displayEmptyDashboard() {
        fileNameLabel.setText("No file analyzed");
        analysisDateLabel.setText("");
        qualityScoreLabel.setText("0/100");
        lineCountLabel.setText("0");
        codeLineCountLabel.setText("0 (0%)");
        warningsTextArea.setText("No warnings to display. Please analyze a C++ file first.");
        
        // Reset charts
        resetChart(memoryChart, "No memory data to display");
        resetChart(complexityChart, "No complexity data to display");
        resetChart(codeMetricsChart, "No metrics data to display");
    }
    
    private void resetChart(JPanel chart, String message) {
        chart.removeAll();
        JLabel placeholder = new JLabel(message, SwingConstants.CENTER);
        chart.add(placeholder, BorderLayout.CENTER);
        chart.revalidate();
        chart.repaint();
    }
    
    public void displayReport(AnalysisReport report) {
        if (report == null) {
            displayEmptyDashboard();
            return;
        }
        
        currentReport = report;
        
        // Update header info
        fileNameLabel.setText("File: " + report.getFileName());
        analysisDateLabel.setText("Analyzed: " + report.getAnalysisDate());
        
        // Update overview metrics
        qualityScoreLabel.setText(report.getQualityScore() + "/100");
        ((JLabel)((JPanel)overviewPanel.getComponent(0)).getComponent(2)).setText(report.getQualityGrade());
        lineCountLabel.setText(String.valueOf(report.getTotalLines()));
        codeLineCountLabel.setText(report.getCodeLines() + " (" + report.getCodeLinesPercentage() + "%)");
        
        // Update warnings
        StringBuilder warningText = new StringBuilder();
        for (String warning : report.getWarnings()) {
            warningText.append("⚠️ ").append(warning).append("\n");
        }
        warningsTextArea.setText(warningText.toString());
        
        // Update charts
        updateMemoryChart(report);
        updateComplexityChart(report);
        updateCodeMetricsChart(report);
    }
    
    private void updateMemoryChart(AnalysisReport report) {
        memoryChart.removeAll();
        
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        
        // Memory metrics panel
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        metricsPanel.setBackground(Color.WHITE);
        
        // Create memory metrics cards
        JPanel newOpsCard = createMetricCard("New Operations", 
                                           String.valueOf(report.getNewOperations()), "");
        JPanel deleteOpsCard = createMetricCard("Delete Operations", 
                                              String.valueOf(report.getDeleteOperations()), "");
        JPanel mallocCallsCard = createMetricCard("Malloc Calls", 
                                                String.valueOf(report.getMallocCalls()), "");
        JPanel freeCallsCard = createMetricCard("Free Calls", 
                                              String.valueOf(report.getFreeCalls()), "");
        
        metricsPanel.add(newOpsCard);
        metricsPanel.add(deleteOpsCard);
        metricsPanel.add(mallocCallsCard);
        metricsPanel.add(freeCallsCard);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(new EmptyBorder(15, 10, 10, 10));
        
        double ratio = report.getMemoryDeletionRatio();
        String statusMessage = "Memory Deletion/Allocation Ratio: " + ratio;
        if (ratio < 0.5) {
            statusMessage += " ⚠️ Potential memory leaks detected!";
        }
        
        JLabel statusLabel = new JLabel(statusMessage);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        if (ratio < 0.5) {
            statusLabel.setForeground(Color.RED);
        } else if (ratio < 1.0) {
            statusLabel.setForeground(new Color(255, 165, 0)); // Orange
        } else {
            statusLabel.setForeground(new Color(0, 128, 0)); // Green
        }
        
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        // Add components to chart panel
        chartPanel.add(metricsPanel, BorderLayout.CENTER);
        chartPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Add to memory chart
        memoryChart.add(chartPanel, BorderLayout.CENTER);
        
        memoryChart.revalidate();
        memoryChart.repaint();
    }
    
    private void updateComplexityChart(AnalysisReport report) {
        complexityChart.removeAll();
        
        JPanel chartPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create complexity metrics cards
        JPanel functionsCard = createMetricCard("Total Functions", 
                                             String.valueOf(report.getTotalFunctions()), "");
        JPanel avgComplexityCard = createMetricCard("Avg Cyclomatic Complexity", 
                                                  String.valueOf(report.getAvgCyclomaticComplexity()), "");
        JPanel controlDepthCard = createMetricCard("Max Control Nesting", 
                                                String.valueOf(report.getMaxControlNesting()), "");
        JPanel recursiveCard = createMetricCard("Recursive Functions", 
                                             String.valueOf(report.getRecursiveFunctions()), "");
        
        chartPanel.add(functionsCard);
        chartPanel.add(avgComplexityCard);
        chartPanel.add(controlDepthCard);
        chartPanel.add(recursiveCard);
        
        // Add to complexity chart
        complexityChart.add(chartPanel, BorderLayout.CENTER);
        
        complexityChart.revalidate();
        complexityChart.repaint();
    }
    
    private void updateCodeMetricsChart(AnalysisReport report) {
        codeMetricsChart.removeAll();
        
        // Create the chart panel with 2 rows
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        
        // Top section - code distribution
        JPanel distributionPanel = new JPanel(new BorderLayout());
        distributionPanel.setBackground(Color.WHITE);
        distributionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(240, 240, 240), 1, true),
            "Code Distribution",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12)
        ));
        
        // Create a simple bar chart for code distribution
        JPanel barChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight() - 40; // Leave room for labels
                int barHeight = height - 40;
                int totalLines = report.getTotalLines();
                
                // Calculate bar widths based on percentages
                int codeWidth = (int)((double)report.getCodeLines() / totalLines * width);
                int commentWidth = (int)((double)report.getCommentLines() / totalLines * width);
                int emptyWidth = (int)((double)report.getEmptyLines() / totalLines * width);
                int preprocessorWidth = width - codeWidth - commentWidth - emptyWidth;
                
                // Draw bars
                g2d.setColor(new Color(70, 130, 180)); // Code lines - blue
                g2d.fillRect(0, 20, codeWidth, barHeight);
                
                g2d.setColor(new Color(60, 179, 113)); // Comment lines - green
                g2d.fillRect(codeWidth, 20, commentWidth, barHeight);
                
                g2d.setColor(new Color(240, 240, 240)); // Empty lines - light gray
                g2d.fillRect(codeWidth + commentWidth, 20, emptyWidth, barHeight);
                
                g2d.setColor(new Color(255, 165, 0)); // Preprocessor lines - orange
                g2d.fillRect(codeWidth + commentWidth + emptyWidth, 20, preprocessorWidth, barHeight);
                
                // Draw percentages
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                double codePercent = (double)report.getCodeLines() / totalLines * 100;
                if (codeWidth > 50) {
                    g2d.drawString(String.format("%.1f%%", codePercent), 10, 20 + barHeight/2);
                }
                
                double commentPercent = (double)report.getCommentLines() / totalLines * 100;
                if (commentWidth > 50) {
                    g2d.drawString(String.format("%.1f%%", commentPercent), codeWidth + 10, 20 + barHeight/2);
                }
                
                // Draw legend
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 11));
                
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(10, height, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Code: " + report.getCodeLines() + " lines", 25, height + 10);
                
                g2d.setColor(new Color(60, 179, 113));
                g2d.fillRect(150, height, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Comments: " + report.getCommentLines() + " lines", 165, height + 10);
                
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRect(320, height, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Empty: " + report.getEmptyLines() + " lines", 335, height + 10);
                
                g2d.setColor(new Color(255, 165, 0));
                g2d.fillRect(450, height, 10, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Preprocessor: " + report.getPreprocessorLines() + " lines", 465, height + 10);
            }
        };
        barChartPanel.setPreferredSize(new Dimension(600, 100));
        
        distributionPanel.add(barChartPanel, BorderLayout.CENTER);
        
        // Bottom section - additional metrics cards
        JPanel additionalPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        additionalPanel.setBackground(Color.WHITE);
        
        // Create cards for additional metrics
        JPanel avgLineCard = createMetricCard("Avg Line Length", 
                                           report.getAvgLineLength() + " chars", "");
        JPanel maxLineCard = createMetricCard("Max Line Length", 
                                           report.getMaxLineLength() + " chars", "");
        JPanel longLinesCard = createMetricCard("Long Lines (>100)", 
                                             String.valueOf(report.getLongLines()), "");
        JPanel controlPanel = createMetricCard("Control Statements", 
                                            String.valueOf(report.getTotalControlStatements()), "");
        
        additionalPanel.add(avgLineCard);
        additionalPanel.add(maxLineCard);
        additionalPanel.add(longLinesCard);
        additionalPanel.add(controlPanel);
        
        // Add sections to chart panel
        chartPanel.add(distributionPanel, BorderLayout.CENTER);
        chartPanel.add(additionalPanel, BorderLayout.SOUTH);
        
        // Add to code metrics chart
        codeMetricsChart.add(chartPanel, BorderLayout.CENTER);
        
        codeMetricsChart.revalidate();
        codeMetricsChart.repaint();
    }
}