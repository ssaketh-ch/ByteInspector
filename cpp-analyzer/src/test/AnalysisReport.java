package com.cppanalyzer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnalysisReport {
    // File information
    private String fileName;
    private String filePath;
    private LocalDateTime analysisDate;
    
    // Overview metrics
    private int qualityScore;
    private String qualityGrade;
    private int totalLines;
    private int codeLines;
    private int commentLines;
    private int emptyLines;
    private int preprocessorLines;
    
    // Line metrics
    private int avgLineLength;
    private int maxLineLength;
    private int longLines;
    
    // Function metrics
    private int totalFunctions;
    private int avgFunctionLength;
    private int maxFunctionLength;
    private int avgCyclomaticComplexity;
    private int highComplexityFunctions;
    private int longFunctions;
    private int shortFunctions;
    private int recursiveFunctions;
    
    // OOP metrics
    private int totalClasses;
    private int avgMethodsPerClass;
    private int largeClasses;
    private int srPotentiallyViolationClasses;
    private int inheritanceRelationships;
    
    // Control flow metrics
    private int ifStatements;
    private int elseStatements;
    private int loopStatements;
    private int switchStatements;
    private int gotoStatements;
    private int maxControlNesting;
    
    // Memory management
    private int newOperations;
    private int deleteOperations;
    private int mallocCalls;
    private int freeCalls;
    private int smartPointerUsage;
    private double memoryDeletionRatio;
    
    // Error handling
    private int tryBlocks;
    private int catchBlocks;
    private int emptyCatchBlocks;
    private int uniqueExceptionTypes;
    private double exceptionCoverage;
    
    // Warnings and issues
    private List<String> warnings;
    
    // Additional metrics
    private int unusedVariables;
    private int totalVariables;
    
    public AnalysisReport() {
        warnings = new ArrayList<>();
        analysisDate = LocalDateTime.now();
    }
    
    // Method to parse the report from text content
    public static AnalysisReport fromReportText(String reportText, String fileName) {
        AnalysisReport report = new AnalysisReport();
        report.setFileName(fileName);
        
        // Parse warnings
        List<String> lines = List.of(reportText.split("\n"));
        List<String> warningLines = new ArrayList<>();
        
        for (String line : lines) {
            if (line.startsWith("⚠️")) {
                report.addWarning(line.substring(3).trim());
                warningLines.add(line);
            }
        }
        
        // Parse metrics sections
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // Size Metrics
            if (line.contains("Size Metrics")) {
                for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("###"); j++) {
                    String metricLine = lines.get(j);
                    if (metricLine.contains("Total lines:")) {
                        report.setTotalLines(extractInt(metricLine));
                    } else if (metricLine.contains("Code lines:")) {
                        report.setCodeLines(extractInt(metricLine));
                    } else if (metricLine.contains("Comment lines:")) {
                        report.setCommentLines(extractInt(metricLine));
                    } else if (metricLine.contains("Empty lines:")) {
                        report.setEmptyLines(extractInt(metricLine));
                    } else if (metricLine.contains("Preprocessor lines:")) {
                        report.setPreprocessorLines(extractInt(metricLine));
                    } else if (metricLine.contains("Average line length:")) {
                        report.setAvgLineLength(extractInt(metricLine));
                    } else if (metricLine.contains("Maximum line length:")) {
                        report.setMaxLineLength(extractInt(metricLine));
                    } else if (metricLine.contains("Lines exceeding 100 characters:")) {
                        report.setLongLines(extractInt(metricLine));
                    }
                }
            }
            
            // Function Metrics
            if (line.contains("Function Metrics")) {
                for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("###"); j++) {
                    String metricLine = lines.get(j);
                    if (metricLine.contains("Total functions:")) {
                        report.setTotalFunctions(extractInt(metricLine));
                    } else if (metricLine.contains("Average function length:")) {
                        report.setAvgFunctionLength(extractInt(metricLine));
                    } else if (metricLine.contains("Maximum function length:")) {
                        report.setMaxFunctionLength(extractInt(metricLine));
                    } else if (metricLine.contains("Average cyclomatic complexity:")) {
                        report.setAvgCyclomaticComplexity(extractInt(metricLine));
                    } else if (metricLine.contains("Functions with high complexity")) {
                        report.setHighComplexityFunctions(extractInt(metricLine));
                    } else if (metricLine.contains("Long functions")) {
                        report.setLongFunctions(extractInt(metricLine));
                    } else if (metricLine.contains("Very short functions")) {
                        report.setShortFunctions(extractInt(metricLine));
                    } else if (metricLine.contains("Recursive function calls:")) {
                        report.setRecursiveFunctions(extractInt(metricLine));
                    }
                }
            }
            
            // OOP Metrics
            if (line.contains("Object-Oriented Programming Metrics")) {
                for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("###"); j++) {
                    String metricLine = lines.get(j);
                    if (metricLine.contains("Total classes:")) {
                        report.setTotalClasses(extractInt(metricLine));
                    } else if (metricLine.contains("Average methods per class:")) {
                        report.setAvgMethodsPerClass(extractInt(metricLine));
                    } else if (metricLine.contains("Large classes")) {
                        report.setLargeClasses(extractInt(metricLine));
                    } else if (metricLine.contains("Classes potentially violating Single Responsibility")) {
                        report.setSrPotentiallyViolationClasses(extractInt(metricLine));
                    } else if (metricLine.contains("Inheritance relationships:")) {
                        report.setInheritanceRelationships(extractInt(metricLine));
                    }
                }
            }
            
            // Control Flow Metrics
            if (line.contains("Control Flow Metrics")) {
                for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("###"); j++) {
                    String metricLine = lines.get(j);
                    if (metricLine.contains("If statements:")) {
                        report.setIfStatements(extractInt(metricLine));
                    } else if (metricLine.contains("Else/Else-if statements:")) {
                        report.setElseStatements(extractInt(metricLine));
                    } else if (metricLine.contains("Loop statements:")) {
                        report.setLoopStatements(extractInt(metricLine));
                    } else if (metricLine.contains("Switch statements:")) {
                        report.setSwitchStatements(extractInt(metricLine));
                    } else if (metricLine.contains("Goto statements:")) {
                        report.setGotoStatements(extractInt(metricLine));
                    } else if (metricLine.contains("Maximum control nesting level:")) {
                        report.setMaxControlNesting(extractInt(metricLine));
                    }
                }
            }
            
            // Memory Management
            if (line.contains("Memory Management")) {
                for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("###"); j++) {
                    String metricLine = lines.get(j);
                    if (metricLine.contains("'new' operators:")) {
                        report.setNewOperations(extractInt(metricLine));
                    } else if (metricLine.contains("'delete' operators:")) {
                        report.setDeleteOperations(extractInt(metricLine));
                    } else if (metricLine.contains("malloc() calls:")) {
                        report.setMallocCalls(extractInt(metricLine));
                    } else if (metricLine.contains("free() calls:")) {
                        report.setFreeCalls(extractInt(metricLine));
                    // Memory Management (continued)
                    } else if (metricLine.contains("Smart pointer usage:")) {
                        report.setSmartPointerUsage(extractInt(metricLine));
                    } else if (metricLine.contains("Memory deletion ratio:")) {
                        report.setMemoryDeletionRatio(extractDouble(metricLine));
                    }
                    }
                    }

                    // Error Handling
                    if (line.contains("Error Handling")) {
                    for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("###"); j++) {
                    String metricLine = lines.get(j);
                    if (metricLine.contains("Try blocks:")) {
                        report.setTryBlocks(extractInt(metricLine));
                    } else if (metricLine.contains("Catch blocks:")) {
                        report.setCatchBlocks(extractInt(metricLine));
                    } else if (metricLine.contains("Empty catch blocks:")) {
                        report.setEmptyCatchBlocks(extractInt(metricLine));
                    } else if (metricLine.contains("Unique exception types:")) {
                        report.setUniqueExceptionTypes(extractInt(metricLine));
                    } else if (metricLine.contains("Exception coverage:")) {
                        report.setExceptionCoverage(extractDouble(metricLine));
                    }
                    }
                    }

                    // Quality Score
                    if (line.contains("Code Quality Score:")) {
                    report.setQualityScore(extractInt(line));
                    }

                    // Quality Grade
                    if (line.contains("Code Quality Grade:")) {
                    report.setQualityGrade(extractString(line));
                    }

                    // Additional Metrics
                    if (line.contains("Additional Metrics")) {
                    for (int j = i + 1; j < lines.size() && !lines.get(j).startsWith("###"); j++) {
                    String metricLine = lines.get(j);
                    if (metricLine.contains("Unused variables:")) {
                        report.setUnusedVariables(extractInt(metricLine));
                    } else if (metricLine.contains("Total variables:")) {
                        report.setTotalVariables(extractInt(metricLine));
                    }
                    }
                    }
                    }

                    return report;
                    }

                    // Helper method to extract integer values from report lines
                    private static int extractInt(String line) {
                    try {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                    String value = parts[1].trim();
                    // Remove any non-numeric characters like '%'
                    value = value.replaceAll("[^0-9]", "");
                    return Integer.parseInt(value);
                    }
                    } catch (NumberFormatException e) {
                    // Handle parsing errors
                    return 0;
                    }
                    return 0;
                    }

                    // Helper method to extract double values from report lines
                    private static double extractDouble(String line) {
                    try {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                    String value = parts[1].trim();
                    // Remove any non-numeric characters except for decimal point
                    value = value.replaceAll("[^0-9.]", "");
                    return Double.parseDouble(value);
                    }
                    } catch (NumberFormatException e) {
                    // Handle parsing errors
                    return 0.0;
                    }
                    return 0.0;
                    }

                    // Helper method to extract string values from report lines
                    private static String extractString(String line) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                    return parts[1].trim();
                    }
                    return "";
                    }

                    // Generate a report summary in markdown format
                    public String generateMarkdownReport() {
                    StringBuilder sb = new StringBuilder();

                    // Title and file information
                    sb.append("# C++ Code Analysis Report\n\n");
                    sb.append("## File Information\n");
                    sb.append("- **File Name:** ").append(fileName).append("\n");
                    sb.append("- **Analysis Date:** ").append(analysisDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

                    // Quality score
                    sb.append("## Quality Summary\n");
                    sb.append("- **Quality Score:** ").append(qualityScore).append("/100\n");
                    sb.append("- **Quality Grade:** ").append(qualityGrade).append("\n\n");

                    // Size metrics
                    sb.append("## Size Metrics\n");
                    sb.append("- Total Lines: ").append(totalLines).append("\n");
                    sb.append("- Code Lines: ").append(codeLines).append(" (").append(calculatePercentage(codeLines, totalLines)).append("%)\n");
                    sb.append("- Comment Lines: ").append(commentLines).append(" (").append(calculatePercentage(commentLines, totalLines)).append("%)\n");
                    sb.append("- Empty Lines: ").append(emptyLines).append(" (").append(calculatePercentage(emptyLines, totalLines)).append("%)\n");
                    sb.append("- Preprocessor Lines: ").append(preprocessorLines).append(" (").append(calculatePercentage(preprocessorLines, totalLines)).append("%)\n");
                    sb.append("- Average Line Length: ").append(avgLineLength).append(" characters\n");
                    sb.append("- Maximum Line Length: ").append(maxLineLength).append(" characters\n");
                    sb.append("- Lines > 100 characters: ").append(longLines).append("\n\n");

                    // Function metrics
                    sb.append("## Function Metrics\n");
                    sb.append("- Total Functions: ").append(totalFunctions).append("\n");
                    sb.append("- Average Function Length: ").append(avgFunctionLength).append(" lines\n");
                    sb.append("- Maximum Function Length: ").append(maxFunctionLength).append(" lines\n");
                    sb.append("- Average Cyclomatic Complexity: ").append(avgCyclomaticComplexity).append("\n");
                    sb.append("- High Complexity Functions: ").append(highComplexityFunctions).append("\n");
                    sb.append("- Long Functions: ").append(longFunctions).append("\n");
                    sb.append("- Very Short Functions: ").append(shortFunctions).append("\n");
                    sb.append("- Recursive Functions: ").append(recursiveFunctions).append("\n\n");

                    // OOP metrics
                    sb.append("## Object-Oriented Metrics\n");
                    sb.append("- Total Classes: ").append(totalClasses).append("\n");
                    sb.append("- Average Methods Per Class: ").append(avgMethodsPerClass).append("\n");
                    sb.append("- Large Classes: ").append(largeClasses).append("\n");
                    sb.append("- Classes Potentially Violating SRP: ").append(srPotentiallyViolationClasses).append("\n");
                    sb.append("- Inheritance Relationships: ").append(inheritanceRelationships).append("\n\n");

                    // Control flow metrics
                    sb.append("## Control Flow Metrics\n");
                    sb.append("- If Statements: ").append(ifStatements).append("\n");
                    sb.append("- Else/Else-if Statements: ").append(elseStatements).append("\n");
                    sb.append("- Loop Statements: ").append(loopStatements).append("\n");
                    sb.append("- Switch Statements: ").append(switchStatements).append("\n");
                    sb.append("- Goto Statements: ").append(gotoStatements).append("\n");
                    sb.append("- Maximum Control Nesting Level: ").append(maxControlNesting).append("\n\n");

                    // Memory management
                    sb.append("## Memory Management\n");
                    sb.append("- New Operations: ").append(newOperations).append("\n");
                    sb.append("- Delete Operations: ").append(deleteOperations).append("\n");
                    sb.append("- Malloc Calls: ").append(mallocCalls).append("\n");
                    sb.append("- Free Calls: ").append(freeCalls).append("\n");
                    sb.append("- Smart Pointer Usage: ").append(smartPointerUsage).append("\n");
                    sb.append("- Memory Deletion Ratio: ").append(String.format("%.2f", memoryDeletionRatio)).append("\n\n");

                    // Error handling
                    sb.append("## Error Handling\n");
                    sb.append("- Try Blocks: ").append(tryBlocks).append("\n");
                    sb.append("- Catch Blocks: ").append(catchBlocks).append("\n");
                    sb.append("- Empty Catch Blocks: ").append(emptyCatchBlocks).append("\n");
                    sb.append("- Unique Exception Types: ").append(uniqueExceptionTypes).append("\n");
                    sb.append("- Exception Coverage: ").append(String.format("%.2f", exceptionCoverage)).append("%\n\n");

                    // Warnings
                    sb.append("## Warnings and Issues\n");
                    if (warnings.isEmpty()) {
                    sb.append("No warnings found.\n\n");
                    } else {
                    for (String warning : warnings) {
                    sb.append("- ⚠️ ").append(warning).append("\n");
                    }
                    sb.append("\n");
                    }

                    // Additional metrics
                    sb.append("## Additional Metrics\n");
                    sb.append("- Unused Variables: ").append(unusedVariables).append(" out of ").append(totalVariables);
                    if (totalVariables > 0) {
                    sb.append(" (").append(calculatePercentage(unusedVariables, totalVariables)).append("%)");
                    }
                    sb.append("\n\n");

                    return sb.toString();
                    }

                    private int calculatePercentage(int part, int whole) {
                    if (whole == 0) return 0;
                    return (int) Math.round((double) part / whole * 100);
                    }

                    // Getters and setters
                    public String getFileName() {
                    return fileName;
                    }

                    public void setFileName(String fileName) {
                    this.fileName = fileName;
                    }

                    public String getFilePath() {
                    return filePath;
                    }

                    public void setFilePath(String filePath) {
                    this.filePath = filePath;
                    }

                    public LocalDateTime getAnalysisDate() {
                    return analysisDate;
                    }

                    public void setAnalysisDate(LocalDateTime analysisDate) {
                    this.analysisDate = analysisDate;
                    }

                    public int getQualityScore() {
                    return qualityScore;
                    }

                    public void setQualityScore(int qualityScore) {
                    this.qualityScore = qualityScore;
                    }

                    public String getQualityGrade() {
                    return qualityGrade;
                    }

                    public void setQualityGrade(String qualityGrade) {
                    this.qualityGrade = qualityGrade;
                    }

                    public int getTotalLines() {
                    return totalLines;
                    }

                    public void setTotalLines(int totalLines) {
                    this.totalLines = totalLines;
                    }

                    public int getCodeLines() {
                    return codeLines;
                    }

                    public void setCodeLines(int codeLines) {
                    this.codeLines = codeLines;
                    }

                    public int getCommentLines() {
                    return commentLines;
                    }

                    public void setCommentLines(int commentLines) {
                    this.commentLines = commentLines;
                    }

                    public int getEmptyLines() {
                    return emptyLines;
                    }

                    public void setEmptyLines(int emptyLines) {
                    this.emptyLines = emptyLines;
                    }

                    public int getPreprocessorLines() {
                    return preprocessorLines;
                    }

                    public void setPreprocessorLines(int preprocessorLines) {
                    this.preprocessorLines = preprocessorLines;
                    }

                    public int getAvgLineLength() {
                    return avgLineLength;
                    }

                    public void setAvgLineLength(int avgLineLength) {
                    this.avgLineLength = avgLineLength;
                    }

                    public int getMaxLineLength() {
                    return maxLineLength;
                    }

                    public void setMaxLineLength(int maxLineLength) {
                    this.maxLineLength = maxLineLength;
                    }

                    public int getLongLines() {
                    return longLines;
                    }

                    public void setLongLines(int longLines) {
                    this.longLines = longLines;
                    }

                    public int getTotalFunctions() {
                    return totalFunctions;
                    }

                    public void setTotalFunctions(int totalFunctions) {
                    this.totalFunctions = totalFunctions;
                    }

                    public int getAvgFunctionLength() {
                    return avgFunctionLength;
                    }

                    public void setAvgFunctionLength(int avgFunctionLength) {
                    this.avgFunctionLength = avgFunctionLength;
                    }

                    public int getMaxFunctionLength() {
                    return maxFunctionLength;
                    }

                    public void setMaxFunctionLength(int maxFunctionLength) {
                    this.maxFunctionLength = maxFunctionLength;
                    }

                    public int getAvgCyclomaticComplexity() {
                    return avgCyclomaticComplexity;
                    }

                    public void setAvgCyclomaticComplexity(int avgCyclomaticComplexity) {
                    this.avgCyclomaticComplexity = avgCyclomaticComplexity;
                    }

                    public int getHighComplexityFunctions() {
                    return highComplexityFunctions;
                    }

                    public void setHighComplexityFunctions(int highComplexityFunctions) {
                    this.highComplexityFunctions = highComplexityFunctions;
                    }

                    public int getLongFunctions() {
                    return longFunctions;
                    }

                    public void setLongFunctions(int longFunctions) {
                    this.longFunctions = longFunctions;
                    }

                    public int getShortFunctions() {
                    return shortFunctions;
                    }

                    public void setShortFunctions(int shortFunctions) {
                    this.shortFunctions = shortFunctions;
                    }

                    public int getRecursiveFunctions() {
                    return recursiveFunctions;
                    }

                    public void setRecursiveFunctions(int recursiveFunctions) {
                    this.recursiveFunctions = recursiveFunctions;
                    }

                    public int getTotalClasses() {
                    return totalClasses;
                    }

                    public void setTotalClasses(int totalClasses) {
                    this.totalClasses = totalClasses;
                    }

                    public int getAvgMethodsPerClass() {
                    return avgMethodsPerClass;
                    }

                    public void setAvgMethodsPerClass(int avgMethodsPerClass) {
                    this.avgMethodsPerClass = avgMethodsPerClass;
                    }

                    public int getLargeClasses() {
                    return largeClasses;
                    }

                    public void setLargeClasses(int largeClasses) {
                    this.largeClasses = largeClasses;
                    }

                    public int getSrPotentiallyViolationClasses() {
                    return srPotentiallyViolationClasses;
                    }

                    public void setSrPotentiallyViolationClasses(int srPotentiallyViolationClasses) {
                    this.srPotentiallyViolationClasses = srPotentiallyViolationClasses;
                    }

                    public int getInheritanceRelationships() {
                    return inheritanceRelationships;
                    }

                    public void setInheritanceRelationships(int inheritanceRelationships) {
                    this.inheritanceRelationships = inheritanceRelationships;
                    }

                    public int getIfStatements() {
                    return ifStatements;
                    }

                    public void setIfStatements(int ifStatements) {
                    this.ifStatements = ifStatements;
                    }

                    public int getElseStatements() {
                    return elseStatements;
                    }

                    public void setElseStatements(int elseStatements) {
                    this.elseStatements = elseStatements;
                    }

                    public int getLoopStatements() {
                    return loopStatements;
                    }

                    public void setLoopStatements(int loopStatements) {
                    this.loopStatements = loopStatements;
                    }

                    public int getSwitchStatements() {
                    return switchStatements;
                    }

                    public void setSwitchStatements(int switchStatements) {
                    this.switchStatements = switchStatements;
                    }

                    public int getGotoStatements() {
                    return gotoStatements;
                    }

                    public void setGotoStatements(int gotoStatements) {
                    this.gotoStatements = gotoStatements;
                    }

                    public int getMaxControlNesting() {
                    return maxControlNesting;
                    }

                    public void setMaxControlNesting(int maxControlNesting) {
                    this.maxControlNesting = maxControlNesting;
                    }

                    public int getNewOperations() {
                    return newOperations;
                    }

                    public void setNewOperations(int newOperations) {
                    this.newOperations = newOperations;
                    }

                    public int getDeleteOperations() {
                    return deleteOperations;
                    }

                    public void setDeleteOperations(int deleteOperations) {
                    this.deleteOperations = deleteOperations;
                    }

                    public int getMallocCalls() {
                    return mallocCalls;
                    }

                    public void setMallocCalls(int mallocCalls) {
                    this.mallocCalls = mallocCalls;
                    }

                    public int getFreeCalls() {
                    return freeCalls;
                    }

                    public void setFreeCalls(int freeCalls) {
                    this.freeCalls = freeCalls;
                    }

                    public int getSmartPointerUsage() {
                    return smartPointerUsage;
                    }

                    public void setSmartPointerUsage(int smartPointerUsage) {
                    this.smartPointerUsage = smartPointerUsage;
                    }

                    public double getMemoryDeletionRatio() {
                    return memoryDeletionRatio;
                    }

                    public void setMemoryDeletionRatio(double memoryDeletionRatio) {
                    this.memoryDeletionRatio = memoryDeletionRatio;
                    }

                    public int getTryBlocks() {
                    return tryBlocks;
                    }

                    public void setTryBlocks(int tryBlocks) {
                    this.tryBlocks = tryBlocks;
                    }

                    public int getCatchBlocks() {
                    return catchBlocks;
                    }

                    public void setCatchBlocks(int catchBlocks) {
                    this.catchBlocks = catchBlocks;
                    }

                    public int getEmptyCatchBlocks() {
                    return emptyCatchBlocks;
                    }

                    public void setEmptyCatchBlocks(int emptyCatchBlocks) {
                    this.emptyCatchBlocks = emptyCatchBlocks;
                    }

                    public int getUniqueExceptionTypes() {
                    return uniqueExceptionTypes;
                    }

                    public void setUniqueExceptionTypes(int uniqueExceptionTypes) {
                    this.uniqueExceptionTypes = uniqueExceptionTypes;
                    }

                    public double getExceptionCoverage() {
                    return exceptionCoverage;
                    }

                    public void setExceptionCoverage(double exceptionCoverage) {
                    this.exceptionCoverage = exceptionCoverage;
                    }

                    public List<String> getWarnings() {
                    return warnings;
                    }

                    public void addWarning(String warning) {
                    this.warnings.add(warning);
                    }

                    public int getUnusedVariables() {
                    return unusedVariables;
                    }

                    public void setUnusedVariables(int unusedVariables) {
                    this.unusedVariables = unusedVariables;
                    }

                    public int getTotalVariables() {
                    return totalVariables;
                    }

                    public void setTotalVariables(int totalVariables) {
                    this.totalVariables = totalVariables;
                    }
                    }