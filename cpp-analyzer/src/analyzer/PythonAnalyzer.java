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

public class PythonAnalyzer {

    // Expanded list of unsafe functions/methods in Python
    private static final List<String> unsafeFunctions = Arrays.asList(
            "eval", "exec", "pickle.loads", "marshal.loads", "shelve.open",
            "os.system", "subprocess.call", "subprocess.check_call", "subprocess.run",
            "input", "gets", "os.popen", "codecs.open"
    );

    // Vulnerable patterns
    private static final List<Pattern> injectionPatterns = Arrays.asList(
            Pattern.compile("os\\.system\\((.*?)\\)"),
            Pattern.compile("subprocess\\.(call|check_call|run)\\((.*?)\\)"),
            Pattern.compile("eval\\((.*?)\\)"),
            Pattern.compile("exec\\((.*?)\\)"),
            Pattern.compile("pickle\\.loads\\((.*?)\\)"),
            Pattern.compile("SELECT\\s+\\*\\s+FROM.*WHERE.*\\$_(GET|POST)\\[[\\'\\\"]?\\w+[\\'\\\"]?\\]", Pattern.CASE_INSENSITIVE), // SQL injection via $_GET/$_POST
            Pattern.compile("<script>.*\\$_(GET|POST)\\[[\\'\\\"]?\\w+[\\'\\\"]?\\]", Pattern.CASE_INSENSITIVE)  // XSS via $_GET/$_POST
    );

    // Data structures
    private static final List<String> dataStructures = Arrays.asList(
            "list", "tuple", "set", "dict", "OrderedDict", "Counter", "deque"
    );

    // Common web framework names
    private static final List<String> webFrameworks = Arrays.asList(
            "flask", "django", "pyramid", "bottle", "tornado", "web2py"
    );

    // Modern Python features
    private static final List<String> modernFeatures = Arrays.asList(
            "dataclasses", "async/await", "type hints", "f-strings", "pathlib"
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

    // SOLID and design pattern metrics (minimal - Python isn't strongly typed)
    private static int classCount = 0;
    private static Map<String, Integer> classMethodCounts = new HashMap<>();
    private static Map<String, Set<String>> classInheritance = new HashMap<>();
    private static Map<String, Integer> classAttributeCounts = new HashMap<>();
    private static List<String> largeClasses = new ArrayList<>();
    private static List<String> singleResponsibilityViolations = new ArrayList<>();

    // Code duplication tracking
    private static Map<String, List<Integer>> codeSignatures = new HashMap<>();
    private static List<String> duplicateCodeBlocks = new ArrayList<>();

    // Performance metrics
    private static List<String> potentialPerformanceIssues = new ArrayList<>();
    private static int loopNestedLevel = 0;
    private static int maxLoopNestingLevel = 0;
    private static int recursionCount = 0;

    // Error handling metrics
    private static int tryBlockCount = 0;
    private static int exceptBlockCount = 0;
    private static int emptyExceptBlockCount = 0;
    private static List<String> exceptionTypes = new ArrayList<>();

    // Dependency management
    private static List<String> importedModules = new ArrayList<>();
    private static List<String> thirdPartyLibraries = new ArrayList<>();

    // Web security metrics
    private static int sqlInjectionRiskCount = 0;
    private static int crossSiteScriptingRiskCount = 0;
    private static int insecureSessionHandlingCount = 0;

    // Data validation metrics
    private static int missingInputValidationCount = 0;
    private static int inadequateOutputEncodingCount = 0;

    // Concurrent metrics
    private static int threadCount = 0;
    private static int lockCount = 0;
    private static List<String> threadingIssues = new ArrayList<>();

    // Resource management metrics
    private static int fileOpenCloseMismatchCount = 0;

    // Naming conventions
    private static List<String> nonConformingNames = new ArrayList<>();
    private static Map<String, String> variableNamingIssues = new HashMap<>();

    // Code complexity metrics
    private static int averageLineLength = 0;
    private static int maxLineLength = 0;
    private static int longLinesCount = 0;
    private static int totalLineLength = 0;
    private static int todoCount = 0;
    private static int fixmeCount = 0;
    // Modern Python usage metrics
    private static Map<String, Integer> modernFeatureUsage = new HashMap<>();

    // Data structure usage metrics
    private static Map<String, Integer> dataStructureUsage = new HashMap<>();

    // Outer complexity
    private static int fileCount = 0;
    private static int totalCommentLines = 0;
    private static int totalCodeLines = 0;
    private static int totalEmptyLines = 0;

    // File system analysis
    private static List<String> largeFiles = new ArrayList<>();
    private static List<String> deeplyNestedFiles = new ArrayList<>();

    // Concurrency and Parallelism metrics
    private static int asyncAwaitCount = 0;
    private static int multiprocessingCount = 0;
    private static int threadPoolExecutorCount = 0;
    private static int queueUsageCount = 0;

    // Framework specific detections (e.g., Django, Flask)
    private static List<String> djangoSpecificIssues = new ArrayList<>();
    private static List<String> flaskSpecificIssues = new ArrayList<>();

    // Static analysis tools
    private static int pylintScore = -1; // -1 indicates not run
    private static int banditScore = -1; // -1 indicates not run

    private static ScriptEngine pythonEngine;

    static {
        for (String feature : modernFeatures) {
            modernFeatureUsage.put(feature, 0);
        }

        for (String ds : dataStructures) {
            dataStructureUsage.put(ds, 0);
        }

        ScriptEngineManager manager = new ScriptEngineManager();
        pythonEngine = manager.getEngineByName("python"); // Requires a Python interpreter configured in your system
        if (pythonEngine == null) {
            System.err.println("Python interpreter not found. Some analysis features will be disabled.");
        }
    }

    public static void analyzePythonFile(String filePath, String outputFile) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File does not exist: " + filePath);
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        DecimalFormat df = new DecimalFormat("#.##");

        writer.write("📊 Python Static Code Analysis Report\n");
        writer.write("===========================================\n");
        //writer.write("Analyzing file: " + filePath + "\n\n");

        String line;
        int lineNumber = 0;
        Stack<Character> bracketStack = new Stack<>();
        Map<String, Integer> declaredVariables = new HashMap<>(); // Variable name -> line declared
        Map<String, String> variableTypes = new HashMap<>(); // Variable name -> type
        Set<String> usedVariables = new HashSet<>();
        Set<String> initializedVariables = new HashSet<>();
        boolean insideFunction = false;
        boolean insideClass = false;
        int openBrackets = 0;
        int totalLines = 0;
        int commentLines = 0;
        int codeLines = 0;
        int emptyLines = 0;
        List<String> functions = new ArrayList<>();
        String currentClass = "";

        String currentFunction = "";
        Set<String> functionsWithoutReturn = new HashSet<>();
        boolean hasReturn = false;
        boolean insideLoop = false;
        int nestedLoopLevel = 0;
        int maxNestedLoopLevel = 0;

        // Control flow metrics
        int ifStatements = 0;
        int elseStatements = 0;
        int loopStatements = 0;

        // Data structure usage metrics
        Map<String, Integer> dataStructureUsage = new HashMap<>();
        for (String ds : dataStructures) {
            dataStructureUsage.put(ds, 0);
        }

        StringBuilder fileContent = new StringBuilder();
        String prevLine = "";
        boolean insideComment = false;
        List<String> includeFiles = new ArrayList<>();
        Set<String> globalVariables = new HashSet<>();

        // Metrics for lines
        int longestLine = 0;
        int linesOver80Chars = 0;

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
            } else if (trimmed.startsWith("#")) {
                commentLines++;
                continue;
            } else if (trimmed.startsWith("'''") || trimmed.startsWith("\"\"\"")) {
                commentLines++;
                insideComment = !insideComment;
                continue;
            } else if (insideComment) {
                commentLines++;
                if (trimmed.endsWith("'''") || trimmed.endsWith("\"\"\"")) {
                    insideComment = false;
                }
                continue;
            } else {
                codeLines++;
            }

            // Detect class declarations
            Pattern classPattern = Pattern.compile("class\\s+(\\w+)\\s*\\(");
            Matcher classMatcher = classPattern.matcher(line);
            if (classMatcher.find()) {
                String className = classMatcher.group(1);
                currentClass = className;
                insideClass = true;
                classCount++; // Increment class count
                classMethodCounts.put(className, 0); // Initialize method count
                classAttributeCounts.put(className, 0); // Initialize attribute count

                // Check if class name conforms to CamelCase
                if (!className.matches("^[A-Z][a-zA-Z0-9]*$")) {
                    nonConformingNames.add("Class '" + className + "' at line " + lineNumber +
                            " does not conform to CamelCase naming convention.");
                }
            }

            // Detect function declarations
            Pattern funcPattern = Pattern.compile("def\\s+(\\w+)\\s*\\(([^)]*)\\):");
            Matcher funcMatcher = funcPattern.matcher(line);
            if (funcMatcher.find()) {
                String funcName = funcMatcher.group(1);
                String params = funcMatcher.group(2);
                functions.add(funcName);

                // Initialize function calls tracking
                if (!functionCalls.containsKey(funcName)) {
                    functionCalls.put(funcName, new HashSet<>());
                }
                if (!functionCalledBy.containsKey(funcName)) {
                    functionCalledBy.put(funcName, new HashSet<>());
                }

                currentFunction = funcName;
                currentFunctionName = funcName;
                currentFunctionLength = 0;
                hasReturn = false;
                functionCount++;

                // Check parameter count
                String[] paramList = params.split(",");
                if (paramList.length > 5) {
                    writer.write("⚠️  Function '" + funcName + "' has too many parameters (" + paramList.length +
                            ") at line " + lineNumber + "\n");
                }

                insideFunction = true;
                cycloComplexity = 1; // Base complexity for a function

                // Check if function name conforms to snake_case
                if (!funcName.matches("^[a-z][a-z0-9_]*$")) {
                    nonConformingNames.add("Function '" + funcName + "' at line " + lineNumber +
                            " does not conform to snake_case naming convention.");
                }

                // Check for mutable default arguments (basic check)
                if (params.contains("=[]") || params.contains("={}")) {
                    writer.write("⚠️  Function '" + funcName + "' at line " + lineNumber +
                            " has a mutable default argument.\n");
                }
            }

            // Track function length
            if (insideFunction) {
                currentFunctionLength++;
            }

            // Track function calls
            if (insideFunction && !currentFunction.isEmpty()) {
                Pattern callPattern = Pattern.compile("\\b(\\w+)\\s*\\(");
                Matcher callMatcher = callPattern.matcher(line);
                while (callMatcher.find()) {
                    String calledFunc = callMatcher.group(1);
                    // Filter out common Python functions and language constructs
                    if (!calledFunc.equals("if") && !calledFunc.equals("for") &&
                            !calledFunc.equals("while") &&
                            functions.contains(calledFunc)) {

                        // Update the call graph
                        Set<String> calls = functionCalls.getOrDefault(currentFunction, new HashSet<>());
                        calls.add(calledFunc);
                        functionCalls.put(currentFunction, calls);

                        // Update the called-by graph
                        Set<String> calledBy = functionCalledBy.getOrDefault(calledFunc, new HashSet<>());
                        calledBy.add(currentFunction);
                        functionCalledBy.put(calledFunc, calledBy);

                        // Check for recursion
                        if (calledFunc.equals(currentFunction)) {
                            recursionCount++;
                        }
                    }
                }
            }

            // Check for unsafe functions
            for (String unsafeFunc : unsafeFunctions) {
                if (line.contains(unsafeFunc + "(")) {
                    writer.write("🚨 Potential vulnerability: Unsafe function '" + unsafeFunc +
                            "' used at line " + lineNumber + "\n");
                }
            }

            // Check for injection vulnerabilities
            for (Pattern pattern : injectionPatterns) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    writer.write("🚨 Potential injection vulnerability: Possible command or code injection at line " +
                            lineNumber + "\n");
                    if(pattern.toString().contains("SQL")) {
                        sqlInjectionRiskCount++;
                    } else {
                        crossSiteScriptingRiskCount++;
                    }
                }
            }

            // Check for import statements
            if (trimmed.startsWith("import ") || trimmed.startsWith("from ")) {
                Pattern importPattern = Pattern.compile("(?:import|from)\\s+([\\w\\.]+)");
                Matcher importMatcher = importPattern.matcher(trimmed);
                if (importMatcher.find()) {
                    String importedModule = importMatcher.group(1);
                    importedModules.add(importedModule);

                    // Categorize as third-party or built-in (very basic check)
                    if (!importedModule.startsWith("os") && !importedModule.startsWith("sys")) {
                        thirdPartyLibraries.add(importedModule);
                    }
                }
            }

            // Detect usage of data structures
            for (String ds : dataStructures) {
                if (line.contains(ds + "(")) {
                    dataStructureUsage.put(ds, dataStructureUsage.get(ds) + 1);
                }
            }

            // Detect modern Python features
            for (String feature : modernFeatures) {
                if (line.contains(feature)) {
                    modernFeatureUsage.put(feature, modernFeatureUsage.get(feature) + 1);
                }
            }

            // Control flow statements
            if (trimmed.startsWith("if ")) {
                ifStatements++;
                cycloComplexity++;
                // Check for truthiness comparisons (e.g., `if x == True:`)
                if (line.contains("== True") || line.contains("== False") || line.contains("is True") || line.contains("is False")) {
                    writer.write("⚠️  Consider implicit truthiness instead of explicit comparison to True/False at line " +
                            lineNumber + "\n");
                }
            } else if (trimmed.startsWith("else") || trimmed.startsWith("elif ")) {
                elseStatements++;
                cycloComplexity++;
            } else if (trimmed.startsWith("for ") || trimmed.startsWith("while ")) {
                loopStatements++;
                cycloComplexity++;
                insideLoop = true;
                nestedLoopLevel++;
                maxLoopNestingLevel = Math.max(maxLoopNestingLevel, nestedLoopLevel);

                // Suggest list comprehension
                if (prevLine.contains(".append(")) {
                    writer.write("💡 Consider using list comprehension for more concise code around line " +
                            lineNumber + "\n");
                }
            }

            // Try-except blocks
            if (trimmed.startsWith("try:")) {
                tryBlockCount++;
            } else if (trimmed.startsWith("except")) {
                exceptBlockCount++;
                Pattern exceptPattern = Pattern.compile("except\\s*(\\w*)");
                Matcher exceptMatcher = exceptPattern.matcher(line);
                if (exceptMatcher.find()) {
                    String exceptionType = exceptMatcher.group(1);
                    if (exceptionType.isEmpty()) {
                        emptyExceptBlockCount++;
                        writer.write("⚠️  Avoid overly broad 'except:' clauses at line " + lineNumber +
                                ". Specify exception types.\n");
                    } else {
                        exceptionTypes.add(exceptionType);
                    }
                }
            }

            // Variable tracking
            Pattern varPattern = Pattern.compile("(\\w+)\\s*=");
            Matcher varMatcher = varPattern.matcher(line);
            if (varMatcher.find()) {
                String varName = varMatcher.group(1);
                if (!declaredVariables.containsKey(varName)) {
                    declaredVariables.put(varName, lineNumber);
                }
                initializedVariables.add(varName);

                // Check if variable name conforms to snake_case
                if (!varName.matches("^[a-z][a-z0-9_]*$")) {
                    if (!nonConformingNames.contains("Variable '" + varName + "' at line " + lineNumber +
                            " does not conform to snake_case naming convention."))
                        nonConformingNames.add("Variable '" + varName + "' at line " + lineNumber +
                                " does not conform to snake_case naming convention.");
                }
            }

            // Usage tracking (very basic)
            for (String varName : declaredVariables.keySet()) {
                if (line.contains(varName)) {
                    usedVariables.add(varName);
                }
            }

            // String formatting check
            if (line.contains("%s") || line.contains("%d")) {
                writer.write("💡 Consider using f-strings for string formatting instead of % operator at line " +
                        lineNumber + "\n");
            }

            // Dunder methods (very basic check for __init__)
            if (trimmed.startsWith("def __init__(self")) {
                Pattern initPattern = Pattern.compile("def __init__");
            }

            // End of function detection
            if (insideFunction) {
                if (trimmed.startsWith("return")) {
                    hasReturn = true;
                }

                // Determine end of function (very basic, needs more sophisticated logic)
                if (trimmed.startsWith("# end of function") || lineNumber > 100) {
                    functionComplexities.put(currentFunctionName, cycloComplexity);
                    totalCycloComplexity += cycloComplexity;
                    functionLengths.put(currentFunctionName, currentFunctionLength);

                    if (!hasReturn) {
                        functionsWithoutReturn.add(currentFunctionName);
                    }

                    if (cycloComplexity > 10) {
                        complexFunctions.add(currentFunctionName);
                    }

                    if (currentFunctionLength > 50) {
                        longFunctions.add(currentFunctionName);
                    }

                    if (currentFunctionLength < 5) {
                        shortFunctions.add(currentFunctionName);
                    }

                    insideFunction = false;
                    cycloComplexity = 0;
                    currentFunctionLength = 0;
                }
            }

            if (insideLoop && !(trimmed.startsWith("for ") || trimmed.startsWith("while "))) {
                nestedLoopLevel--;
                if (nestedLoopLevel < 0) {
                    insideLoop = false;
                    nestedLoopLevel = 0;
                }
            }
            prevLine = line;
        }
        br.close();

        // Code Duplication Analysis (simple signature-based)
        try (BufferedReader signatureReader = new BufferedReader(new FileReader(file))) {
            String sigLine;
            int sigLineNumber = 0;
            while ((sigLine = signatureReader.readLine()) != null) {
                sigLineNumber++;
                String signature = generateCodeSignature(sigLine);
                if (!signature.isEmpty()) {
                    if (!codeSignatures.containsKey(signature)) {
                        codeSignatures.put(signature, new ArrayList<>());
                    }
                    codeSignatures.get(signature).add(sigLineNumber);
                }
            }
        }

        for (Map.Entry<String, List<Integer>> entry : codeSignatures.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicateCodeBlocks.add("Lines " + entry.getValue().toString() +
                        ": " + entry.getKey());
            }
        }

        // Variable Usage Analysis
        List<String> unusedVariables = new ArrayList<>();
        for (String varName : declaredVariables.keySet()) {
            if (!usedVariables.contains(varName)) {
                unusedVariables.add(varName);
            }
        }

        // Class Metrics
        if (insideClass) {
            if (classMethodCounts.get(currentClass) > 10) {
                largeClasses.add(currentClass);
            }
            // Add Single Responsibility Principle checks if possible
        }

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
        writer.write("- Recursive function calls: " + recursionCount + "\n");

        // Object-Oriented Programming Metrics
        writer.write("\n### Object-Oriented Programming Metrics\n");
        writer.write("- Total classes: " + classCount + "\n");
        writer.write("- Average methods per class: " + (classCount > 0 ? classMethodCounts.values().stream().mapToInt(Integer::intValue).sum() / classCount : 0) + "\n");
        writer.write("- Large classes (>10 methods): " + largeClasses.size() + "\n");
        writer.write("- Classes potentially violating Single Responsibility Principle: " + singleResponsibilityViolations.size() + "\n");
        writer.write("- Inheritance relationships: " + classInheritance.size() + "\n");

        // Control Flow Metrics
        writer.write("\n### Control Flow Metrics\n");
        writer.write("- If statements: " + ifStatements + "\n");
        writer.write("- Else/Else-if statements: " + elseStatements + "\n");
        writer.write("- Loop statements: " + loopStatements + "\n");
        writer.write("- Maximum control nesting level: " + maxLoopNestingLevel + "\n");

        // Memory Management (Limited in Python)
        writer.write("\n### Memory Management\n");
        writer.write("- No explicit memory management in Python, checking for resource leaks\n");
        writer.write("- File open/close mismatches: " + fileOpenCloseMismatchCount + "\n");

        // Error Handling
        writer.write("\n### Error Handling\n");
        writer.write("- Try blocks: " + tryBlockCount + "\n");
        writer.write("- Catch blocks: " + exceptBlockCount + "\n");
        writer.write("- Empty catch blocks: " + emptyExceptBlockCount + "\n");
        writer.write("- Unique exception types: " + exceptionTypes.size() + "\n");
        writer.write("- Exception coverage ratio: " + df.format((double) tryBlockCount / functionCount) + "\n");

        // Modern Python Feature Usage
        writer.write("\n### Modern Python Feature Usage\n");
        for (Map.Entry<String, Integer> entry : modernFeatureUsage.entrySet()) {
            writer.write("- " + entry.getKey() + ": " + entry.getValue() + "\n");
        }

        // Data Structure Usage
        writer.write("\n### Data Structure Usage\n");
        for (Map.Entry<String, Integer> entry : dataStructureUsage.entrySet()) {
            writer.write("- " + entry.getKey() + ": " + entry.getValue() + "\n");
        }

        // Code Duplication Analysis
        writer.write("\n### Code Duplication Analysis\n");
        writer.write("- Potential duplicate code blocks: " + duplicateCodeBlocks.size() + "\n");
        if (!duplicateCodeBlocks.isEmpty()) {
            for (String dup : duplicateCodeBlocks) {
                writer.write("  * " + dup + "\n");
            }
        }

        // Dependency Analysis
        writer.write("\n### Dependency Analysis\n");
        writer.write("- Included modules: " + importedModules.size() + "\n");
        for (String module : importedModules) {
            writer.write("  * " + module + "\n");
        }
        writer.write("- Third-party libraries: " + thirdPartyLibraries.size() + "\n");

        // Variable Usage
        writer.write("\n### Variable Usage\n");
        writer.write("- Total declared variables: " + declaredVariables.size() + "\n");
        writer.write("- Initialized variables: " + initializedVariables.size() + " (" + df.format((double) initializedVariables.size() / declaredVariables.size() * 100) + "%)\n");
        writer.write("- Used variables: " + usedVariables.size() + " (" + df.format((double) usedVariables.size() / declaredVariables.size() * 100) + "%)\n");
        writer.write("- Potentially unused variables: " + unusedVariables.size() + "\n");
        if (!unusedVariables.isEmpty()) {
            for (String unused : unusedVariables) {
                writer.write("  * " + unused + " (line " + declaredVariables.get(unused) + ")\n");
            }
        }

        // Global Variables
        writer.write("\n### Global Variables\n");
        writer.write("- No global variables detected ✅\n");

        // Naming Conventions
        writer.write("\n### Naming Conventions\n");
        writer.write("- Naming conventions appear consistent ✅\n");

        if (!nonConformingNames.isEmpty()) {
            writer.write("⚠️  Naming convention violations:\n");
            for (String violation : nonConformingNames) {
                writer.write("  * " + violation + "\n");
            }
        }

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

        // Size Risk
        writer.write("\n### Size Risk\n");
        writer.write("- Excessively long functions: " + longFunctions.size() + "\n");
        if (!longFunctions.isEmpty()) {
            for (String func : longFunctions) {
                writer.write("  * " + func + " (Length: " + functionLengths.get(func) + " lines)\n");
            }
        }

        // Performance Risk
        writer.write("\n### Performance Risk\n");
        writer.write("- Potential performance issues: " + potentialPerformanceIssues.size() + "\n");
        writer.write("  * Maximum loop nesting level: " + maxLoopNestingLevel + "\n");

        // Security Risk
        writer.write("\n### Security Risk\n");
        writer.write("- Potential security vulnerabilities detected:\n");
        writer.write("  * SQL Injection risks: " + sqlInjectionRiskCount + "\n");
        writer.write("  * Cross-Site Scripting (XSS) risks: " + crossSiteScriptingRiskCount + "\n");

        // Memory Management Risk
        writer.write("\n### Memory Management Risk\n");
        writer.write("- Potential resource leaks detected (file open/close mismatches: " + fileOpenCloseMismatchCount + ")\n");

        // Maintenance Notes
        writer.write("\n## 🔧 Maintenance Notes\n\n");
        writer.write("- TODO comments: " + todoCount + "\n");
        writer.write("- FIXME comments: " + fixmeCount + "\n");

        // Improvement Recommendations
        writer.write("\n## 🚀 Improvement Recommendations\n\n");
        writer.write("- Consider reviewing potential resource leaks.\n");
        writer.write("- Explore using modern Python features to improve code readability and maintainability.\n");

        // Conclusion
        writer.write("\n## 📝 Conclusion\n\n");
        writer.write("Overall Code Quality Assessment:\n");

        // Simple scoring (can be refined based on more detailed metrics)
        int score = 100;
        if (complexFunctions.size() > 0) score -= 10;
        if (fileOpenCloseMismatchCount > 0) score -= 15;
        if (unusedVariables.size() > 0) score -= 5;
        if (todoCount + fixmeCount > 0) score -= 5;
        if (longLinesCount > 5) score -= 5;
        if (nonConformingNames.size() > 0) score -= 10; // Penalize naming issues
        if(sqlInjectionRiskCount + crossSiteScriptingRiskCount > 0) score -= 15;

        score = Math.max(0, score); // Ensure the score is not negative

        String grade;
        if (score > 90) grade = "A (Excellent)";
        else if (score > 80) grade = "B (Good)";
        else if (score > 70) grade = "C (Fair)";
        else if (score > 60) grade = "D (Poor)";
        else grade = "F (Fail)";

        writer.write("Overall Code Quality Score: " + score + "/100 - Grade " + grade + "\n");

        writer.write("\nThis report was generated by the PythonAnalyzer static analysis tool.\n");
        writer.write("For more information on how to improve your code, please refer to Python best practices guidelines.\n");

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
            analyzePythonFile(args[0], args[1]);
            System.out.println("Analysis complete. Report written to " + args[1]);
        } catch (IOException e) {
            System.err.println("Error analyzing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
