package analyzer;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.DecimalFormat;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;
import javax.script.*;

public class CudaAnalyzer {

    // CUDA-specific keywords and functions
    private static final List<String> cudaKeywords = Arrays.asList(
            "__global__", "__device__", "__host__", "__shared__", "cudaMalloc", "cudaFree",
            "cudaMemcpy", "cudaDeviceSynchronize", "dim3", "threadIdx", "blockIdx", "blockDim",
            "gridDim", "atomicAdd", "atomicSub", "cudaError_t"
    );

    // Common CUDA errors
    private static final List<String> cudaErrorChecks = Arrays.asList(
            "cudaSuccess", "cudaErrorInvalidValue", "cudaErrorMemoryAllocation",
            "cudaErrorDeviceNotPresent", "cudaErrorInvalidDevice"
    );

    // CUDA memory management functions
    private static final List<String> cudaMemoryFunctions = Arrays.asList(
            "cudaMalloc", "cudaFree", "cudaMemcpy"
    );

    // CUDA atomic functions
    private static final List<String> cudaAtomicFunctions = Arrays.asList(
            "atomicAdd", "atomicSub", "atomicExch", "atomicMin", "atomicMax"
    );

    private static final List<String> memoryAllocationFunctions = Arrays.asList(
            "malloc", "calloc", "realloc", "new"
    );

    private static final List<String> memoryFreeFunctions = Arrays.asList(
            "free", "delete"
    );

    // Metrics
    private static int cycloComplexity = 0;
    private static int maxFunctionLength = 0;
    private static int totalCycloComplexity = 0;
    private static int functionCount = 0;
    private static Map<String, Integer> functionComplexities = new HashMap<>();
    private static List<String> complexFunctions = new ArrayList<>();
    private static int currentFunctionLength = 0;
    private static String currentFunctionName = "";
    private static Map<String, Integer> functionLengths = new HashMap<>();
    private static List<String> longFunctions = new ArrayList<>();
    private static List<String> shortFunctions = new ArrayList<>();
    private static Map<String, Set<String>> functionCalls = new HashMap<>();
    private static Map<String, Set<String>> functionCalledBy = new HashMap<>();

    // CUDA-specific metrics
    private static int globalFunctionCount = 0;
    private static int deviceFunctionCount = 0;
    private static int hostFunctionCount = 0;
    private static int sharedMemoryUsage = 0;
    private static int cudaMallocCount = 0;
    private static int cudaFreeCount = 0;
    private static int cudaMemcpyCount = 0;
    private static int atomicOperationCount = 0;
    private static int memoryAllocationMismatchCount = 0;

    // Thread and block dimension metrics
    private static int maxThreadsPerBlock = 0;
    private static int maxBlocksPerGrid = 0;
    private static Map<String, Integer> kernelLaunchCounts = new HashMap<>();

    // Potential performance issues
    private static List<String> potentialPerformanceIssues = new ArrayList<>();
    private static int loopNestedLevel = 0;
    private static int maxLoopNestingLevel = 0;
    private static int warpDivergenceRisk = 0;
    private static int uncoalescedMemoryAccesses = 0;
    private static int bankConflicts = 0;
    private static boolean insufficientParallelism = false;

    // Error handling metrics
    private static int cudaErrorCheckCount = 0;
    private static int missingErrorChecks = 0;
    private static List<String> errorLinesWithoutChecks = new ArrayList<>();

    // Resource management metrics
    private static int fileOpenCloseMismatchCount = 0;

    // Naming conventions
    private static List<String> nonConformingNames = new ArrayList<>();

    // Code complexity metrics
    private static int averageLineLength = 0;
    private static int maxLineLength = 0;
    private static int longLinesCount = 0;
    private static int totalLineLength = 0;
    private static int todoCount = 0;
    private static int fixmeCount = 0;

    // Outer complexity
    private static int fileCount = 0;
    private static int totalCommentLines = 0;
    private static int totalCodeLines = 0;
    private static int totalEmptyLines = 0;

    // File system analysis
    private static List<String> largeFiles = new ArrayList<>();
    private static List<String> deeplyNestedFiles = new ArrayList<>();

    public static void analyzeCudaFile(String filePath, String outputFile) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File does not exist: " + filePath);
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        DecimalFormat df = new DecimalFormat("#.##");

        writer.write("📊 CUDA C++ Static Code Analysis Report\n");
        writer.write("===========================================\n");
        //writer.write("Analyzing file: " + filePath + "\n\n");

        String line;
        int lineNumber = 0;
        Stack<Character> bracketStack = new Stack<>();
        Map<String, Integer> declaredVariables = new HashMap<>();
        Set<String> usedVariables = new HashSet<>();
        boolean insideFunction = false;
        int totalLines = 0;
        int commentLines = 0;
        int codeLines = 0;
        int emptyLines = 0;
        List<String> functions = new ArrayList<>();

        String currentFunction = "";
        boolean hasReturn = false;
        boolean insideLoop = false;
        int nestedLoopLevel = 0;
        int maxNestedLoopLevel = 0;

        StringBuilder fileContent = new StringBuilder();
        String prevLine = "";
        boolean insideComment = false;

        // Metrics for lines
        int longestLine = 0;
        int linesOver80Chars = 0;

        // Memory allocation tracking
        int mallocCalls = 0;
        int freeCalls = 0;

        // CUDA kernels and shared memory usage
        boolean insideKernel = false;
        String currentKernel = "";
        int kernelSharedMemory = 0;

        // Thread and block dimension tracking
        int currentThreadsPerBlock = 0;
        int currentBlocksPerGrid = 0;

        // Loop Analysis
        boolean isParallelizableLoop = false;
        int loopIterations = 0;

        // Read once to build complete file content
        BufferedReader contentReader = new BufferedReader(new FileReader(file));
        String contentLine;
        while ((contentLine = contentReader.readLine()) != null) {
            fileContent.append(contentLine).append("\n");
        }
        contentReader.close();

        // First pass for symbol collection
        while ((line = br.readLine()) != null) {
            lineNumber++;
            totalLines++;
            String trimmed = line.trim();
            totalLineLength += line.length();
            averageLineLength = totalLineLength / totalLines;

            if (line.length() > maxLineLength) {
                maxLineLength = line.length();
            }

            if (line.length() > 100) {
                longLinesCount++;
            }

            // Metrics for lines
            if (line.length() > longestLine) {
                longestLine = line.length();
            }
            if (line.length() > 80) {
                linesOver80Chars++;
            }

            // Track TODO and FIXME comments
            if (line.contains("TODO")) {
                todoCount++;
            }
            if (line.contains("FIXME")) {
                fixmeCount++;
            }

            // Track types of lines
            if (trimmed.isEmpty()) {
                emptyLines++;
                continue;
            } else if (trimmed.startsWith("//")) {
                commentLines++;
                continue;
            } else if (trimmed.startsWith("/*")) {
                commentLines++;
                insideComment = true;
                continue;
            } else if (insideComment) {
                commentLines++;
                if (line.contains("*/")) {
                    insideComment = false;
                }
                continue;
            } else {
                codeLines++;
            }

            // Detect function declarations with CUDA qualifiers
            Pattern funcPattern = Pattern.compile("(?:__global__|__device__|__host__)?\\s*(\\w+)\\s+(\\w+)\\s*\\(([^)]*)\\)");
            Matcher funcMatcher = funcPattern.matcher(line);
            if (funcMatcher.find()) {
                String qualifier = funcMatcher.group(1);
                String funcName = funcMatcher.group(2);
                String params = funcMatcher.group(3);
                functions.add(funcName);

                currentFunction = funcName;
                currentFunctionName = funcName;
                currentFunctionLength = 0;
                hasReturn = false;
                functionCount++;

                insideFunction = true;
                cycloComplexity = 1; // Base complexity for a function

                // Count function types
                if (qualifier.equals("__global__")) {
                    globalFunctionCount++;
                    insideKernel = true;
                    currentKernel = funcName;
                    kernelSharedMemory = 0; // Reset shared memory usage
                } else if (qualifier.equals("__device__")) {
                    deviceFunctionCount++;
                } else if (qualifier.equals("__host__")) {
                    hostFunctionCount++;
                }
            }

            // Track function length
            if (insideFunction) {
                currentFunctionLength++;
            }

            // CUDA specific checks
            for (String keyword : cudaKeywords) {
                if (line.contains(keyword)) {
                    // Basic keyword detection
                }
            }

            // CUDA memory allocation
            if (trimmed.startsWith("cudaMalloc")) {
                cudaMallocCount++;
            }
            if (trimmed.startsWith("cudaFree")) {
                cudaFreeCount++;
            }
            if (trimmed.startsWith("cudaMemcpy")) {
                cudaMemcpyCount++;
            }

            // Check memory allocations and frees (CPU)
            if (trimmed.contains("malloc(") || trimmed.contains("calloc(") || trimmed.contains("realloc(")) {
                mallocCalls++;
            }
            if (trimmed.contains("free(")) {
                freeCalls++;
            }

            // Detect shared memory usage
            if (trimmed.contains("__shared__")) {
                Pattern sharedMemPattern = Pattern.compile("__shared__\\s+\\w+\\s+\\w+\\[(\\d+)\\]");
                Matcher sharedMemMatcher = sharedMemPattern.matcher(line);
                if (sharedMemMatcher.find()) {
                    try {
                        kernelSharedMemory += Integer.parseInt(sharedMemMatcher.group(1));
                    } catch (NumberFormatException e) {
                        // Ignore if the size is not a simple integer
                    }
                }
            }

            // Detect atomic operations
            for (String atomicFunc : cudaAtomicFunctions) {
                if (line.contains(atomicFunc + "(")) {
                    atomicOperationCount++;
                }
            }

            // Detect CUDA error checks
            boolean hasCudaErrorCheck = false;
            for (String errorCheck : cudaErrorChecks) {
                if (line.contains(errorCheck)) {
                    cudaErrorCheckCount++;
                    hasCudaErrorCheck = true;
                    break;
                }
            }

            // Check for missing error checks
            if (line.startsWith("cuda") && !hasCudaErrorCheck) {
                missingErrorChecks++;
                errorLinesWithoutChecks.add("Line " + lineNumber + ": " + line.trim());
            }

            // Warp divergence risk (very basic, needs more sophisticated analysis)
            if (insideKernel && (line.contains("if (threadIdx.x") || line.contains("if (threadIdx.y") || line.contains("if (threadIdx.z"))) {
                warpDivergenceRisk++;
                potentialPerformanceIssues.add("Potential warp divergence in kernel '" + currentKernel + "' at line " + lineNumber);
            }

            // Check for uncoalesced memory accesses (very basic)
            if (insideKernel && line.contains(" = ") && line.contains("[threadIdx.x]")) {
                uncoalescedMemoryAccesses++;
                potentialPerformanceIssues.add("Potential uncoalesced memory access in kernel '" + currentKernel + "' at line " + lineNumber);
            }

            // Detect loop nesting
            if (line.contains("for ") || line.contains("while ")) {
                insideLoop = true;
                nestedLoopLevel++;
                maxLoopNestingLevel = Math.max(maxLoopNestingLevel, nestedLoopLevel);

                // Check if the loop is parallelizable (very basic check)
                if (!line.contains("threadIdx.x") && !line.contains("threadIdx.y") && !line.contains("threadIdx.z") && insideKernel) {
                    isParallelizableLoop = true;

                    // Attempt to determine number of iterations (very limited)
                    Pattern loopIterationPattern = Pattern.compile("for \\s*\\(.*\\s*<\\s*(\\d+)\\s*");
                    Matcher loopIterationMatcher = loopIterationPattern.matcher(line);
                    if (loopIterationMatcher.find()) {
                        try {
                            loopIterations = Integer.parseInt(loopIterationMatcher.group(1));
                        } catch (NumberFormatException e) {
                            loopIterations = 0;
                        }
                    }
                }
            } else {
                isParallelizableLoop = false;
                loopIterations = 0;
            }

            // Kernel Launch Configuration (Very Basic)
            Pattern kernelLaunchPattern = Pattern.compile("<<<\\s*(\\w+)\\s*,\\s*(\\w+)\\s*>>>");
            Matcher kernelLaunchMatcher = kernelLaunchPattern.matcher(line);

            if (kernelLaunchMatcher.find()) {
                String blocks = kernelLaunchMatcher.group(1);
                String threads = kernelLaunchMatcher.group(2);

                // Store launch configuration
                try {
                    currentBlocksPerGrid = Integer.parseInt(blocks);
                    currentThreadsPerBlock = Integer.parseInt(threads);
                    maxBlocksPerGrid = Math.max(maxBlocksPerGrid, currentBlocksPerGrid);
                    maxThreadsPerBlock = Math.max(maxThreadsPerBlock, currentThreadsPerBlock);
                } catch (NumberFormatException e) {
                    // Handle if the configurations are variables
                    potentialPerformanceIssues.add("Kernel launch with variable block/thread dimensions at line " + lineNumber);
                }

                // Track kernel launch frequency (very basic)
                kernelLaunchCounts.put(currentKernel, kernelLaunchCounts.getOrDefault(currentKernel, 0) + 1);

                // Check for insufficient parallelism
                if (currentThreadsPerBlock * currentBlocksPerGrid < 1024) {
                    insufficientParallelism = true;
                    potentialPerformanceIssues.add("Potential insufficient parallelism: thread count * block count < 1024 at line " + lineNumber);
                }

            }

            // Look for shared memory bank conflicts (simplified)
            if (insideKernel && line.contains("sharedMemory[threadIdx.x % 32]")) {
                bankConflicts++;
                potentialPerformanceIssues.add("Potential shared memory bank conflict at line " + lineNumber + " in kernel " + currentKernel);
            }

            // End of function detection
            if (insideFunction) {
                if (trimmed.startsWith("return")) {
                    hasReturn = true;
                }

                // Determine end of function (very basic, needs more sophisticated logic)
                if (trimmed.startsWith("}") || lineNumber > 200) {
                    functionComplexities.put(currentFunctionName, cycloComplexity);
                    totalCycloComplexity += cycloComplexity;
                    functionLengths.put(currentFunctionName, currentFunctionLength);

                    insideFunction = false;
                    cycloComplexity = 0;
                    currentFunctionLength = 0;
                    if (insideKernel && currentKernel.equals(currentFunctionName)) {
                        sharedMemoryUsage += kernelSharedMemory;
                        insideKernel = false;
                        currentKernel = "";
                    }
                }
            }

            if (insideLoop && !(line.contains("for ") || line.contains("while "))) {
                nestedLoopLevel--;
                if (nestedLoopLevel < 0) {
                    insideLoop = false;
                    nestedLoopLevel = 0;
                }
            }
            prevLine = line;
        }
        br.close();

        memoryAllocationMismatchCount = Math.abs(mallocCalls - freeCalls);

        // End of Analysis, Write Report
        writer.write("\n## 📈 Code Metrics Summary\n\n");

        // Size Metrics
        writer.write("### Size Metrics\n");
        writer.write("- Total lines: " + totalLines + "\n");
        writer.write("- Code lines: " + codeLines + " (" + df.format((double) codeLines / totalLines * 100) + "%)\n");
        writer.write("- Comment lines: " + commentLines + " (" + df.format((double) commentLines / totalLines * 100) + "%)\n");
        writer.write("- Empty lines: " + emptyLines + " (" + df.format((double) emptyLines / totalLines * 100) + "%)\n");
        writer.write("- Average line length: " + averageLineLength + " characters\n");
        writer.write("- Maximum line length: " + maxLineLength + " characters\n");
        writer.write("- Lines exceeding 100 characters: " + longLinesCount + "\n");

        // Function Metrics
        writer.write("\n### Function Metrics\n");
        writer.write("- Total functions: " + functionCount + "\n");
        writer.write("- Average function length: " + (functionCount > 0 ? totalLines / functionCount : 0) + " lines\n");
        writer.write("- Maximum function length: " + maxFunctionLength + " lines\n");
        writer.write("- Average cyclomatic complexity: " + (functionCount > 0 ? totalCycloComplexity / functionCount : 0) + "\n");
        writer.write("- Functions with high complexity (>10): " + complexFunctions.size() + "\n");
        writer.write("- Long functions (>50 lines): " + longFunctions.size() + "\n");
        writer.write("- Very short functions (<5 lines): " + shortFunctions.size() + "\n");

        // CUDA Specific Metrics
        writer.write("\n### CUDA Specific Metrics\n");
        writer.write("- Global functions (__global__): " + globalFunctionCount + "\n");
        writer.write("- Device functions (__device__): " + deviceFunctionCount + "\n");
        writer.write("- Host functions (__host__): " + hostFunctionCount + "\n");
        writer.write("- cudaMalloc calls: " + cudaMallocCount + "\n");
        writer.write("- cudaFree calls: " + cudaFreeCount + "\n");
        writer.write("- cudaMemcpy calls: " + cudaMemcpyCount + "\n");
        writer.write("- Atomic operations: " + atomicOperationCount + "\n");
        writer.write("- Shared memory usage: " + sharedMemoryUsage + " bytes\n");

        // Thread and Block Dimension Metrics
        writer.write("\n### Thread and Block Dimension Metrics\n");
        writer.write("- Max threads per block: " + maxThreadsPerBlock + "\n");
        writer.write("- Max blocks per grid: " + maxBlocksPerGrid + "\n");
        writer.write("- Kernel launch counts:\n");

        for (Map.Entry<String, Integer> entry : kernelLaunchCounts.entrySet()) {
            writer.write("  * " + entry.getKey() + ": " + entry.getValue() + "\n");
        }

        // Memory Management
        writer.write("\n### Memory Management\n");
        writer.write("- Malloc calls: " + mallocCalls + "\n");
        writer.write("- Free calls: " + freeCalls + "\n");
        writer.write("- Memory allocation mismatches: " + memoryAllocationMismatchCount + "\n");

        // Error Handling
        writer.write("\n### Error Handling\n");
        writer.write("- CUDA error checks: " + cudaErrorCheckCount + "\n");
        writer.write("- Missing CUDA error checks: " + missingErrorChecks + "\n");
        if (!errorLinesWithoutChecks.isEmpty()) {
            writer.write("  * Lines without error checks:\n");
            for (String errorLine : errorLinesWithoutChecks) {
                writer.write("    - " + errorLine + "\n");
            }
        }

        // Control Flow Metrics
        writer.write("\n### Control Flow Metrics\n");
        writer.write("- Maximum loop nesting level: " + maxLoopNestingLevel + "\n");

        // Code Quality and Risk Analysis
        writer.write("\n## 🔍 Code Quality and Risk Analysis\n\n");

        // Complexity Risk
        writer.write("### Complexity Risk\n");
        writer.write("- Functions with high complexity: " + complexFunctions.size() + "\n");
        if (!complexFunctions.isEmpty()) {
            for (String func : complexFunctions) {
                writer.write("  * " + func + " (Cyclomatic Complexity: " + functionComplexities.get(func) + ")\n");
            }
        }

        // Performance Risk
        writer.write("\n### Performance Risk\n");
        writer.write("- Potential performance issues: " + potentialPerformanceIssues.size() + "\n");
        if (!potentialPerformanceIssues.isEmpty()) {
            for (String issue : potentialPerformanceIssues) {
                writer.write("  * " + issue + "\n");
            }
        }
        writer.write("  * Warp divergence risk: " + warpDivergenceRisk + "\n");
        writer.write("  * Uncoalesced memory accesses: " + uncoalescedMemoryAccesses + "\n");
        writer.write("  * Shared memory bank conflicts: " + bankConflicts + "\n");

        // Insufficient Parallelism
        writer.write("### Parallelism Risk\n");
        if (insufficientParallelism) {
            writer.write("⚠️  Potential for increased parallelism not utilized.\n");
        } else {
            writer.write("✅  Parallelism appears to be adequately utilized.\n");
        }

        // Memory Management Risk
        writer.write("\n### Memory Management Risk\n");
        writer.write("- Potential memory leaks detected (CPU malloc/free imbalance: " + memoryAllocationMismatchCount + ")\n");

        // Improvement Recommendations
        writer.write("\n## 🚀 Improvement Recommendations\n\n");
        writer.write("- Check for missing CUDA error checks.\n");
        writer.write("- Review potential warp divergence issues.\n");
        writer.write("- Investigate uncoalesced memory accesses.\n");
        writer.write("- Reduce shared memory bank conflicts.\n");
        writer.write("- Balance CPU memory allocations and deallocations.\n");
        writer.write("- Consider optimizing kernel launch frequency.\n");
        writer.write("- Evaluate opportunities for increasing parallelism.\n");

        // Conclusion
        writer.write("\n## 📝 Conclusion\n\n");
        writer.write("Overall Code Quality Assessment:\n");

        // Simple scoring (can be refined based on more detailed metrics)
        int score = 100;
        if (complexFunctions.size() > 0) score -= 10;
        if (missingErrorChecks > 0) score -= 15;
        if (memoryAllocationMismatchCount > 0) score -= 10;
        if (warpDivergenceRisk > 0) score -= 5;
        if (uncoalescedMemoryAccesses > 0) score -= 10;
        if (bankConflicts > 0) score -= 10;
        if (insufficientParallelism) score -= 10;
        if (todoCount + fixmeCount > 0) score -= 5;

        score = Math.max(0, score); // Ensure the score is not negative

        String grade;
        if (score > 90) grade = "A (Excellent)";
        else if (score > 80) grade = "B (Good)";
        else if (score > 70) grade = "C (Fair)";
        else if (score > 60) grade = "D (Poor)";
        else grade = "F (Fail)";

        writer.write("Overall Code Quality Score: " + score + "/100 - Grade " + grade + "\n");

        writer.write("\nThis report was generated by the CudaAnalyzer static analysis tool.\n");
        writer.write("For more information on how to improve your code, please refer to CUDA best practices guidelines.\n");

        writer.close();
    }

    // Helper function to generate a simple code signature
    private static String generateCodeSignature(String line) {
        String cleanLine = line.trim().replaceAll("\\s+", " ");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(cleanLine.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return cleanLine; // Fallback if MD5 is not available
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java CppAnalyzer <input_file> <output_file>");
            return;
        }
        
        try {
            analyzeCudaFile(args[0], args[1]);
            System.out.println("Analysis complete. Report written to " + args[1]);
        } catch (IOException e) {
            System.err.println("Error analyzing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

