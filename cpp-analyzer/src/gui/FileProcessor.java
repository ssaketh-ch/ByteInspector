package gui;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// Import the analyzer classes
import analyzer.CppAnalyzer;
import analyzer.CudaAnalyzer;
import analyzer.PythonAnalyzer;

public class FileProcessor {
    public static void main(String[] args) {
        // Use the absolute path for the output folder
        File outputFolder = new File("/home/saketh/Desktop/java/project9/cpp-analyzer/output");
        if (!outputFolder.exists()) {
            if (outputFolder.mkdir()) {
                System.out.println("Output folder created.");
            } else {
                System.err.println("Failed to create output folder.");
                return;
            }
        }

        // Use the updated absolute path for the uploads folder
        File uploadsFolder = new File("/home/saketh/Desktop/java/project9/uploads");
        if (!uploadsFolder.exists() || !uploadsFolder.isDirectory()) {
            System.out.println("Uploads folder does not exist or is not a directory.");
            return;
        }

        File[] files = uploadsFolder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in the uploads folder.");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                processFile(file, outputFolder);
            }
        }
    }

    private static void processFile(File file, File outputFolder) {
        String fileName = file.getName();
        if (fileName.endsWith(".cpp")) {
            invokeCppAnalyzer(file, outputFolder);
        } else if (fileName.endsWith(".cu")) {
            invokeCudaAnalyzer(file, outputFolder);
        } else if (fileName.endsWith(".py")) {
            invokePythonAnalyzer(file, outputFolder);
        } else {
            System.out.println("Unsupported file type: " + fileName);
        }
    }

    private static void invokeCppAnalyzer(File file, File outputFolder) {
        System.out.println("Processing C++ file: " + file.getName());
        try {
            // Define the output file path
            String outputFilePath = new File(outputFolder, "cpp-analysis-report-" + file.getName() + ".txt").getAbsolutePath();
            
            // Call the analyzeCppFile method with the correct arguments
            CppAnalyzer.analyzeCppFile(file.getAbsolutePath(), outputFilePath);
            
            System.out.println("C++ analysis complete. Report written to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing C++ file: " + e.getMessage());
        }
    }

    private static void invokeCudaAnalyzer(File file, File outputFolder) {
        System.out.println("Processing CUDA file: " + file.getName());
        try {
            // Define the output file path
            String outputFilePath = new File(outputFolder, "cuda-analysis-report-" + file.getName() + ".txt").getAbsolutePath();
            
            // Call the analyzeCudaFile method with the correct arguments
            CudaAnalyzer.analyzeCudaFile(file.getAbsolutePath(), outputFilePath);
            
            System.out.println("CUDA analysis complete. Report written to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing CUDA file: " + e.getMessage());
        }
    }

    private static void invokePythonAnalyzer(File file, File outputFolder) {
        System.out.println("Processing Python file: " + file.getName());
        try {
            // Define the output file path
            String outputFilePath = new File(outputFolder, "python-analysis-report-" + file.getName() + ".txt").getAbsolutePath();
            
            // Call the analyzePythonFile method with the correct arguments
            PythonAnalyzer.analyzePythonFile(file.getAbsolutePath(), outputFilePath);
            
            System.out.println("Python analysis complete. Report written to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing Python file: " + e.getMessage());
        }
    }

    private static void generateOutput(File inputFile, String outputFileName, String analysisResult) {
        File outputFolder = new File("output");
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }

        File outputFile = new File(outputFolder, outputFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            writer.write("Analysis result for file: " + inputFile.getName());
            writer.newLine();
            writer.write(analysisResult); // Write the actual analysis result
            writer.newLine();
            writer.flush();
            System.out.println("Output written to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }
}