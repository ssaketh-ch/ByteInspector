package analyzer;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.DecimalFormat;

public class CppAnalyzer {
    
    // Expanded list of unsafe functions
    private static final List<String> unsafeFunctions = Arrays.asList(
        "gets", "strcpy", "strcat", "sprintf", "scanf", "vsprintf", 
        "strtok", "strncat", "strncpy", "memcpy", "memmove", "rand",
        "printf", "sscanf", "realpath", "getwd", "tmpnam"
    );
    
    // Buffer overflow vulnerable functions with their safe alternatives
    private static final Map<String, String> safeAlternatives = new HashMap<>();
    static {
        safeAlternatives.put("strcpy", "strncpy or strlcpy");
        safeAlternatives.put("strcat", "strncat or strlcat");
        safeAlternatives.put("gets", "fgets");
        safeAlternatives.put("sprintf", "snprintf");
        safeAlternatives.put("scanf", "scanf with field width limits");
        safeAlternatives.put("printf", "printf with format string validation");
        safeAlternatives.put("memcpy", "memcpy with bounds checking");
        safeAlternatives.put("rand", "more secure RNG like getrandom() or /dev/urandom");
    }
    
    // Common integer overflow patterns
    private static final List<Pattern> integerOverflowPatterns = Arrays.asList(
        Pattern.compile("\\b(\\w+)\\s*\\+=\\s*(\\w+)"),
        Pattern.compile("\\b(\\w+)\\s*=\\s*(\\w+)\\s*\\+\\s*(\\w+)"),
        Pattern.compile("\\b(\\w+)\\s*\\*=\\s*(\\w+)"),
        Pattern.compile("\\b(\\w+)\\s*=\\s*(\\w+)\\s*\\*\\s*(\\w+)")
    );
    
    // Common C++ data structures
    private static final List<String> dataStructures = Arrays.asList(
        "vector", "map", "set", "list", "deque", "queue", "stack", 
        "priority_queue", "unordered_map", "unordered_set", "array"
    );
    
    // Modern C++ features
    private static final List<String> modernFeatures = Arrays.asList(
        "auto", "nullptr", "constexpr", "decltype", "lambda", "move", 
        "forward", "unique_ptr", "shared_ptr", "weak_ptr", "tuple"
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
    
    // SOLID and design pattern metrics
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
    private static int catchBlockCount = 0;
    private static int emptyCatchBlockCount = 0;
    private static List<String> exceptionTypes = new ArrayList<>();
    
    public static void analyzeCppFile(String filePath, String outputFile) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        
        DecimalFormat df = new DecimalFormat("#.##");

        writer.write("📊 C++ Static Code Analysis Report\n");
        writer.write("===========================================\n");
        writer.write("Analyzing file: " + filePath + "\n\n");

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
        int preprocessorLines = 0;
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
        int switchCases = 0;
        int loopStatements = 0;
        int gotoStatements = 0;
        
        // Naming conventions
        List<String> nonConformingNames = new ArrayList<>();
        Map<String, String> variableNamingIssues = new HashMap<>();
        
        // Code complexity metrics
        int averageLineLength = 0;
        int maxLineLength = 0;
        int longLinesCount = 0;
        int totalLineLength = 0;
        int todoCount = 0;
        int fixmeCount = 0;
        
        // Modern C++ usage metrics
        Map<String, Integer> modernFeatureUsage = new HashMap<>();
        for (String feature : modernFeatures) {
            modernFeatureUsage.put(feature, 0);
        }
        
        // Data structure usage metrics
        Map<String, Integer> dataStructureUsage = new HashMap<>();
        for (String ds : dataStructures) {
            dataStructureUsage.put(ds, 0);
        }
        
        // Memory management metrics
        int newCount = 0;
        int deleteCount = 0;
        int mallocCount = 0;
        int freeCount = 0;
        int smartPointerCount = 0;
        
        StringBuilder fileContent = new StringBuilder();
        String prevLine = "";
        boolean insideComment = false;
        List<String> includeFiles = new ArrayList<>();
        Set<String> globalVariables = new HashSet<>();
        
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
            
            if (line.length() > maxLineLength) {
                maxLineLength = line.length();
            }
            
            if (line.length() > 100) {
                longLinesCount++;
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
                if (trimmed.endsWith("*/")) {
                    insideComment = false;
                }
                continue;
            } else if (insideComment) {
                commentLines++;
                if (trimmed.endsWith("*/")) {
                    insideComment = false;
                }
                continue;
            } else if (trimmed.contains("//")) {
                // Line with code and comment
                codeLines++;
                commentLines++;
            } else if (trimmed.startsWith("#")) {
                preprocessorLines++;
                
                // Track include files
                if (trimmed.startsWith("#include")) {
                    Pattern includePattern = Pattern.compile("#include\\s+[<\"]([^>\"]+)[>\"]");
                    Matcher includeMatcher = includePattern.matcher(trimmed);
                    if (includeMatcher.find()) {
                        includeFiles.add(includeMatcher.group(1));
                    }
                }
            } else {
                codeLines++;
            }
            
            // Detect class declarations
            Pattern classPattern = Pattern.compile("class\\s+(\\w+)(?:\\s+:\\s+(?:public|protected|private)\\s+(\\w+))?");
            Matcher classMatcher = classPattern.matcher(line);
            if (classMatcher.find()) {
                String className = classMatcher.group(1);
                classCount++;
                currentClass = className;
                insideClass = true;
                classMethodCounts.put(className, 0);
                classAttributeCounts.put(className, 0);
                
                // Check inheritance
                if (classMatcher.group(2) != null) {
                    String parentClass = classMatcher.group(2);
                    Set<String> parents = classInheritance.getOrDefault(className, new HashSet<>());
                    parents.add(parentClass);
                    classInheritance.put(className, parents);
                }
            }
            
            // Detect class attributes
            if (insideClass && !insideFunction && trimmed.endsWith(";") && 
                !trimmed.startsWith("public:") && !trimmed.startsWith("private:") && 
                !trimmed.startsWith("protected:")) {
                Pattern attrPattern = Pattern.compile("\\b(int|float|char|double|bool|string|\\w+)\\s+(\\w+);");
                Matcher attrMatcher = attrPattern.matcher(line);
                if (attrMatcher.find()) {
                    classAttributeCounts.put(currentClass, classAttributeCounts.getOrDefault(currentClass, 0) + 1);
                }
            }

            // Detect function declarations
            Pattern funcPattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
            Matcher funcMatcher = funcPattern.matcher(line);
            if (funcMatcher.find()) {
                String returnType = funcMatcher.group(1);
                String funcName = funcMatcher.group(2);
                String params = funcMatcher.group(3);
                functions.add(funcName);
                
                // Initialize function calls tracking
                if (!functionCalls.containsKey(funcName)) {
                    functionCalls.put(funcName, new HashSet<>());
                }
                if (!functionCalledBy.containsKey(funcName)) {
                    functionCalledBy.put(funcName, new HashSet<>());
                }
                
                // If inside a class, increment method count
                if (insideClass) {
                    classMethodCounts.put(currentClass, classMethodCounts.getOrDefault(currentClass, 0) + 1);
                }
                
                currentFunction = funcName;
                currentFunctionName = funcName;
                currentFunctionLength = 0;
                hasReturn = false;
                functionCount++;
                
                // Check return type for non-void functions
                if (!returnType.equals("void")) {
                    functionsWithoutReturn.add(funcName);
                }
                
                // Check parameter count
                String[] paramList = params.split(",");
                if (paramList.length > 5) {
                    writer.write("⚠️  Function '" + funcName + "' has too many parameters (" + paramList.length + 
                                ") at line " + lineNumber + "\n");
                }
                
                // Check naming conventions for functions
                if (!funcName.matches("[a-z][a-zA-Z0-9]*") && !funcName.matches("[A-Z][a-zA-Z0-9]*")) {
                    nonConformingNames.add("Function '" + funcName + "' at line " + lineNumber + 
                                          " doesn't follow naming conventions");
                }
                
                insideFunction = true;
                cycloComplexity = 1; // Base complexity for a function
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
                    // Filter out common C/C++ functions and language constructs
                    if (!calledFunc.equals("if") && !calledFunc.equals("for") && 
                        !calledFunc.equals("while") && !calledFunc.equals("switch") &&
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
            
            // Check for global variables
            if (!insideFunction && !insideClass && !trimmed.startsWith("#") && trimmed.endsWith(";")) {
                Pattern globalPattern = Pattern.compile("\\b(int|float|char|double|bool|string|\\w+)\\s+(\\w+)\\s*=");
                Matcher globalMatcher = globalPattern.matcher(line);
                if (globalMatcher.find()) {
                    globalVariables.add(globalMatcher.group(2));
                }
            }
            
            // Check for function ending
            if (insideFunction && line.contains("}") && openBrackets == 1) {
                insideFunction = false;
                
                // Record function length
                functionLengths.put(currentFunction, currentFunctionLength);
                if (currentFunctionLength > 50) {
                    longFunctions.add(currentFunction + " (" + currentFunctionLength + " lines)");
                }
                if (currentFunctionLength < 5 && currentFunctionLength > 1) {
                    shortFunctions.add(currentFunction + " (" + currentFunctionLength + " lines)");
                }
                
                if (currentFunctionLength > maxFunctionLength) {
                    maxFunctionLength = currentFunctionLength;
                }
                
                // Check if function has a return statement
                if (functionsWithoutReturn.contains(currentFunction) && !hasReturn) {
                    writer.write("⚠️  Function '" + currentFunction + "' may be missing return statement\n");
                }
                
                // Record function complexity
                functionComplexities.put(currentFunction, cycloComplexity);
                totalCycloComplexity += cycloComplexity;
                
                if (cycloComplexity > 10) {
                    complexFunctions.add(currentFunction + " (complexity: " + cycloComplexity + ")");
                }
                
                currentFunction = "";
            }
            
            // End of class check
            if (insideClass && line.contains("};")) {
                insideClass = false;
                
                // Check for SRP violations
                int methodCount = classMethodCounts.getOrDefault(currentClass, 0);
                if (methodCount > 10) {
                    singleResponsibilityViolations.add(currentClass + " (" + methodCount + " methods)");
                    largeClasses.add(currentClass + " (" + methodCount + " methods)");
                }
                
                currentClass = "";
            }
            
            // Track modern C++ feature usage
            for (String feature : modernFeatures) {
                if (line.contains(feature)) {
                    modernFeatureUsage.put(feature, modernFeatureUsage.get(feature) + 1);
                }
            }
            
            // Track data structure usage
            for (String ds : dataStructures) {
                if (line.matches(".*\\b" + ds + "<.*")) {
                    dataStructureUsage.put(ds, dataStructureUsage.get(ds) + 1);
                }
            }
            
            // Memory management tracking
            if (line.contains("new ")) newCount++;
            if (line.contains("delete ")) deleteCount++;
            if (line.contains("malloc(")) mallocCount++;
            if (line.contains("free(")) freeCount++;
            if (line.contains("unique_ptr") || line.contains("shared_ptr") || line.contains("weak_ptr")) {
                smartPointerCount++;
            }
            
            // Error handling tracking
            if (line.contains("try {") || line.contains("try\n")) {
                tryBlockCount++;
            }
            if (line.contains("catch(") || line.contains("catch (")) {
                catchBlockCount++;
                
                // Extract exception type
                Pattern exceptionPattern = Pattern.compile("catch\\s*\\(([^)]+)\\)");
                Matcher exceptionMatcher = exceptionPattern.matcher(line);
                if (exceptionMatcher.find()) {
                    String exceptionType = exceptionMatcher.group(1).trim();
                    if (!exceptionTypes.contains(exceptionType)) {
                        exceptionTypes.add(exceptionType);
                    }
                }
                
                // Check for empty catch blocks
                if (line.matches(".*catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}.*")) {
                    emptyCatchBlockCount++;
                }
            }
            
            // Count control structures for cyclomatic complexity
            if (line.matches(".*\\bif\\s*\\(.*")) {
                ifStatements++;
                cycloComplexity++;
            }
            if (line.matches(".*\\belse if\\s*\\(.*")) {
                elseStatements++;
                cycloComplexity++;
            }
            if (line.matches(".*\\belse\\b.*")) {
                elseStatements++;
            }
            if (line.matches(".*\\bfor\\s*\\(.*")) {
                loopStatements++;
                cycloComplexity++;
                loopNestedLevel++;
                if (loopNestedLevel > maxLoopNestingLevel) {
                    maxLoopNestingLevel = loopNestedLevel;
                }
                
                // Performance check for loops
                if (loopNestedLevel > 2) {
                    potentialPerformanceIssues.add("Deeply nested loop at line " + lineNumber + 
                                                  " (nesting level: " + loopNestedLevel + ")");
                }
            }
            if (line.matches(".*\\bwhile\\s*\\(.*")) {
                loopStatements++;
                cycloComplexity++;
                loopNestedLevel++;
                if (loopNestedLevel > maxLoopNestingLevel) {
                    maxLoopNestingLevel = loopNestedLevel;
                }
            }
            if (line.matches(".*\\bswitch\\s*\\(.*")) {
                cycloComplexity++;
            }
            if (line.matches(".*\\bcase\\s+.*:.*")) {
                switchCases++;
                cycloComplexity++;
            }
            if (line.matches(".*\\bgoto\\s+.*")) {
                gotoStatements++;
                writer.write("⚠️  Usage of goto statement at line " + lineNumber + " - consider refactoring\n");
            }
            
            // Check for loop exit
            if (insideLoop && line.contains("}") && (prevLine.contains("for") || prevLine.contains("while"))) {
                loopNestedLevel--;
            }
            
            // Detect return statements
            if (line.contains("return ")) {
                hasReturn = true;
            }
            
            // 1. Missing semicolons detection (improved)
            if (!trimmed.isEmpty() && 
                !trimmed.endsWith(";") && 
                !trimmed.endsWith("{") && 
                !trimmed.endsWith("}") && 
                !trimmed.startsWith("#") &&
                !trimmed.endsWith(":") &&
                !trimmed.matches(".*(if|else|for|while|switch|catch)\\s*\\(.*\\)\\s*")) {
                writer.write("⚠️  Possible missing semicolon at line " + lineNumber + ": " + trimmed + "\n");
            }

            // 2. Detect unsafe functions with recommendations
            for (String unsafe : unsafeFunctions) {
                if (line.matches(".*\\b" + unsafe + "\\s*\\(.*")) {
                    String recommendation = safeAlternatives.containsKey(unsafe) ? 
                        " (Recommended: " + safeAlternatives.get(unsafe) + ")" : "";
                    writer.write("🔴 SECURITY: Unsafe function '" + unsafe + "' used at line " + lineNumber + 
                               recommendation + "\n");
                }
            }

            // 3. Improved memory leak detection
            if ((line.contains("malloc(") || line.contains("new ")) && !line.contains("=")) {
                writer.write("⚠️  Possible memory allocation without assignment at line " + lineNumber + "\n");
            }
            
            // 4. Variable tracking with advanced type inference
            Pattern varPattern = Pattern.compile("\\b(int|float|char|double|bool|string|auto|long|short|unsigned|size_t|\\w+\\s*\\*|const\\s+\\w+)\\s+(\\w+)\\s*(=\\s*[^;]+)?;");
            Matcher varMatcher = varPattern.matcher(line);
            while (varMatcher.find()) {
                String varType = varMatcher.group(1).trim();
                String varName = varMatcher.group(2).trim();
                declaredVariables.put(varName, lineNumber);
                variableTypes.put(varName, varType);
                
                // Check if variable is initialized
                if (varMatcher.group(3) != null) {
                    initializedVariables.add(varName);
                }
                
                // Check variable naming conventions
                if (!varName.matches("[a-z][a-zA-Z0-9]*") && !varName.matches("[A-Z][a-zA-Z0-9]*")) {
                    variableNamingIssues.put(varName, "at line " + lineNumber);
                }
            }
            
            // Track variable usage
            for (String var : declaredVariables.keySet()) {
                Pattern usagePattern = Pattern.compile("\\b" + var + "\\b");
                Matcher usageMatcher = usagePattern.matcher(line);
                if (usageMatcher.find() && !line.contains("=") && !line.contains(var + ";")) {
                    usedVariables.add(var);
                }
            }
            
            // 5. Track code signatures for duplication detection
            String codeSignature = line.replaceAll("\\s+", "").replaceAll("[0-9]+", "N");
            if (codeSignature.length() > 10 && !trimmed.startsWith("//") && !trimmed.isEmpty()) {
                if (!codeSignatures.containsKey(codeSignature)) {
                    codeSignatures.put(codeSignature, new ArrayList<>());
                }
                codeSignatures.get(codeSignature).add(lineNumber);
            }
            
            // 6. Track brackets for syntax errors
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    bracketStack.push(c);
                    openBrackets++;
                } else if (c == '}') {
                    if (bracketStack.isEmpty()) {
                        writer.write("🔴 SYNTAX ERROR: Unmatched closing bracket at line " + lineNumber + "\n");
                    } else {
                        bracketStack.pop();
                        openBrackets--;
                    }
                }
            }
            
            prevLine = line;
        }
        
        // ----- Now generate the comprehensive report -----
        
        // 1. Summary of key metrics
        writer.write("\n## 📈 Code Metrics Summary\n\n");
        writer.write("### Size Metrics\n");
        writer.write("- Total lines: " + totalLines + "\n");
        writer.write("- Code lines: " + codeLines + " (" + df.format((double)codeLines/totalLines*100) + "%)\n");
        writer.write("- Comment lines: " + commentLines + " (" + df.format((double)commentLines/totalLines*100) + "%)\n");
        writer.write("- Empty lines: " + emptyLines + " (" + df.format((double)emptyLines/totalLines*100) + "%)\n");
        writer.write("- Preprocessor lines: " + preprocessorLines + " (" + df.format((double)preprocessorLines/totalLines*100) + "%)\n");
        writer.write("- Average line length: " + (totalLineLength/totalLines) + " characters\n");
        writer.write("- Maximum line length: " + maxLineLength + " characters\n");
        writer.write("- Lines exceeding 100 characters: " + longLinesCount + "\n");
        
        // 2. Function metrics
        writer.write("\n### Function Metrics\n");
        writer.write("- Total functions: " + functionCount + "\n");
        writer.write("- Average function length: " + (functionCount > 0 ? totalLines / functionCount : 0) + " lines\n");
        writer.write("- Maximum function length: " + maxFunctionLength + " lines\n");
        writer.write("- Average cyclomatic complexity: " + (functionCount > 0 ? df.format((double)totalCycloComplexity / functionCount) : 0) + "\n");
        writer.write("- Functions with high complexity (>10): " + complexFunctions.size() + "\n");
        writer.write("- Long functions (>50 lines): " + longFunctions.size() + "\n");
        writer.write("- Very short functions (<5 lines): " + shortFunctions.size() + "\n");
        writer.write("- Recursive function calls: " + recursionCount + "\n");
        
        // 3. OOP metrics
        writer.write("\n### Object-Oriented Programming Metrics\n");
        writer.write("- Total classes: " + classCount + "\n");
        writer.write("- Average methods per class: " + (classCount > 0 ? df.format(functionCount / (double)classCount) : 0) + "\n");
        writer.write("- Large classes (>10 methods): " + largeClasses.size() + "\n");
        writer.write("- Classes potentially violating Single Responsibility Principle: " + singleResponsibilityViolations.size() + "\n");
        writer.write("- Inheritance relationships: " + classInheritance.size() + "\n");
        
        // 4. Control flow metrics
        writer.write("\n### Control Flow Metrics\n");
        writer.write("- If statements: " + ifStatements + "\n");
        writer.write("- Else/Else-if statements: " + elseStatements + "\n");
        writer.write("- Loop statements: " + loopStatements + "\n");
        writer.write("- Switch statements: " + switchCases + "\n");
        writer.write("- Goto statements: " + gotoStatements + "\n");
        writer.write("- Maximum control nesting level: " + maxNestedLoopLevel + "\n");
        
        // 5. Memory management
        writer.write("\n### Memory Management\n");
        writer.write("- 'new' operators: " + newCount + "\n");
        writer.write("- 'delete' operators: " + deleteCount + "\n");
        writer.write("- malloc() calls: " + mallocCount + "\n");
        writer.write("- free() calls: " + freeCount + "\n");
        writer.write("- Smart pointer usage: " + smartPointerCount + "\n");
        
        double memoryBalanceRatio = 0;
        if (newCount + mallocCount > 0) {
            memoryBalanceRatio = (double)(deleteCount + freeCount) / (newCount + mallocCount);
        }
        writer.write("- Memory deletion/allocation ratio: " + df.format(memoryBalanceRatio) + 
                   (memoryBalanceRatio < 0.9 ? " ⚠️ Potential memory leaks" : " ✅") + "\n");
        
        // 6. Error handling
        writer.write("\n### Error Handling\n");
        writer.write("- Try blocks: " + tryBlockCount + "\n");
        writer.write("- Catch blocks: " + catchBlockCount + "\n");
        writer.write("- Empty catch blocks: " + emptyCatchBlockCount + "\n");
        writer.write("- Unique exception types: " + exceptionTypes.size() + "\n");
        
        double exceptionCoverage = 0;
        if (functionCount > 0) {
            exceptionCoverage = (double)tryBlockCount / functionCount;
        }
        writer.write("- Exception coverage ratio: " + df.format(exceptionCoverage) + 
                   (exceptionCoverage < 0.2 ? " ⚠️ Low exception handling" : "") + "\n");
        
        // 7. Modern C++ usage
        writer.write("\n### Modern C++ Feature Usage\n");
        boolean anyModernFeatures = false;
        for (Map.Entry<String, Integer> entry : modernFeatureUsage.entrySet()) {
            if (entry.getValue() > 0) {
                writer.write("- " + entry.getKey() + ": " + entry.getValue() + " occurrences\n");
                anyModernFeatures = true;
            }}
        
            if (!anyModernFeatures) {
                writer.write("- No modern C++ features detected ⚠️\n");
            }
            
            // 8. Data structure usage
            writer.write("\n### Data Structure Usage\n");
            boolean anyDataStructures = false;
            for (Map.Entry<String, Integer> entry : dataStructureUsage.entrySet()) {
                if (entry.getValue() > 0) {
                    writer.write("- " + entry.getKey() + ": " + entry.getValue() + " occurrences\n");
                    anyDataStructures = true;
                }
            }
            
            if (!anyDataStructures) {
                writer.write("- No STL data structures detected\n");
            }
            
            // 9. Code duplication analysis
            writer.write("\n### Code Duplication Analysis\n");
            int duplicationCount = 0;
            for (Map.Entry<String, List<Integer>> entry : codeSignatures.entrySet()) {
                if (entry.getValue().size() > 1) {
                    duplicationCount++;
                    duplicateCodeBlocks.add("Similar code at lines: " + entry.getValue().toString());
                }
            }
            
            writer.write("- Potential duplicate code blocks: " + duplicationCount + "\n");
            if (duplicationCount > 0) {
                writer.write("- Top duplications:\n");
                int count = 0;
                for (String duplicate : duplicateCodeBlocks) {
                    writer.write("  * " + duplicate + "\n");
                    count++;
                    if (count >= 5) break; // Show only top 5
                }
            }
            
            // 10. Dependency analysis
            writer.write("\n### Dependency Analysis\n");
            writer.write("- Included files: " + includeFiles.size() + "\n");
            for (String include : includeFiles) {
                writer.write("  * " + include + "\n");
            }
            
            // Identify most depended-on functions
            List<Map.Entry<String, Set<String>>> mostCalledFunctions = new ArrayList<>(functionCalledBy.entrySet());
            mostCalledFunctions.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));
            
            writer.write("- Most depended-on functions:\n");
            int mostCalledCount = 0;
            for (Map.Entry<String, Set<String>> entry : mostCalledFunctions) {
                if (entry.getValue().size() > 0) {
                    writer.write("  * " + entry.getKey() + ": Called by " + entry.getValue().size() + " functions\n");
                    mostCalledCount++;
                    if (mostCalledCount >= 5) break; // Top 5
                }
            }
            
            // 11. Risk analysis
            writer.write("\n## 🔍 Code Quality and Risk Analysis\n\n");
            
            // Complexity risk
            writer.write("### Complexity Risk\n");
            writer.write("- Functions with high complexity:\n");
            for (String complex : complexFunctions) {
                writer.write("  * " + complex + "\n");
            }
            
            // Size risk
            writer.write("\n### Size Risk\n");
            writer.write("- Excessively long functions:\n");
            for (String longFunc : longFunctions) {
                writer.write("  * " + longFunc + "\n");
            }
            
            // Performance risk
            writer.write("\n### Performance Risk\n");
            writer.write("- Potential performance issues:\n");
            if (potentialPerformanceIssues.isEmpty()) {
                writer.write("  * No major performance issues detected\n");
            } else {
                for (String issue : potentialPerformanceIssues) {
                    writer.write("  * " + issue + "\n");
                }
            }
            
            // Memory risks
            writer.write("\n### Memory Management Risk\n");
            if (memoryBalanceRatio < 0.9) {
                writer.write("- ⚠️ Potential memory leaks detected (allocation to deallocation ratio: " + 
                           df.format(memoryBalanceRatio) + ")\n");
            } else {
                writer.write("- Memory management appears balanced ✅\n");
            }
            
            // Variable usage statistics
            writer.write("\n### Variable Usage\n");
            writer.write("- Total declared variables: " + declaredVariables.size() + "\n");
            writer.write("- Initialized variables: " + initializedVariables.size() + " (" + 
                       df.format((double)initializedVariables.size()/Math.max(1, declaredVariables.size())*100) + "%)\n");
            writer.write("- Used variables: " + usedVariables.size() + " (" + 
                       df.format((double)usedVariables.size()/Math.max(1, declaredVariables.size())*100) + "%)\n");
            
            int unusedVars = 0;
            writer.write("- Potentially unused variables:\n");
            for (String var : declaredVariables.keySet()) {
                if (!usedVariables.contains(var)) {
                    writer.write("  * " + var + " (line " + declaredVariables.get(var) + "): " + variableTypes.get(var) + "\n");
                    unusedVars++;
                }
            }
            if (unusedVars == 0) {
                writer.write("  * No unused variables detected ✅\n");
            }
            
            // Global variables
            writer.write("\n### Global Variables\n");
            if (globalVariables.isEmpty()) {
                writer.write("- No global variables detected ✅\n");
            } else {
                writer.write("- Global variables detected: " + globalVariables.size() + "\n");
                for (String global : globalVariables) {
                    writer.write("  * " + global + "\n");
                }
            }
            
            // Naming conventions
            writer.write("\n### Naming Conventions\n");
            if (variableNamingIssues.isEmpty() && nonConformingNames.isEmpty()) {
                writer.write("- Naming conventions appear consistent ✅\n");
            } else {
                writer.write("- Variables with non-standard naming: " + variableNamingIssues.size() + "\n");
                for (Map.Entry<String, String> entry : variableNamingIssues.entrySet()) {
                    writer.write("  * " + entry.getKey() + " " + entry.getValue() + "\n");
                }
                
                writer.write("- Functions/classes with non-standard naming: " + nonConformingNames.size() + "\n");
                for (String name : nonConformingNames) {
                    writer.write("  * " + name + "\n");
                }
            }
            
            // 12. Maintenance notes
            writer.write("\n## 🔧 Maintenance Notes\n\n");
            writer.write("- TODO comments: " + todoCount + "\n");
            writer.write("- FIXME comments: " + fixmeCount + "\n");
            
            // 13. Recommendation summary
            writer.write("\n## 🚀 Improvement Recommendations\n\n");
            
            // Check overall code complexity
            if (functionCount > 0 && (double)totalCycloComplexity / functionCount > 7) {
                writer.write("- ⚠️ Overall code complexity is high. Consider refactoring complex functions.\n");
            }
            
            // Check class design
            if (singleResponsibilityViolations.size() > 0) {
                writer.write("- ⚠️ Several classes may violate the Single Responsibility Principle. Consider splitting large classes.\n");
            }
            
            // Check memory management
            if (memoryBalanceRatio < 0.9) {
                writer.write("- ⚠️ Memory management imbalance detected. Check for memory leaks.\n");
            }
            
            // Check error handling
            if (exceptionCoverage < 0.2) {
                writer.write("- ⚠️ Exception handling coverage is low. Consider adding more robust error handling.\n");
            }
            
            if (emptyCatchBlockCount > 0) {
                writer.write("- ⚠️ Empty catch blocks detected. These should be avoided or documented.\n");
            }
            
            // Check modern C++ usage
            if (!anyModernFeatures) {
                writer.write("- ⚠️ No modern C++ features detected. Consider updating code to use modern C++ patterns.\n");
            }
            
            // Check for long functions
            if (longFunctions.size() > 0) {
                writer.write("- ⚠️ Several long functions detected. Consider breaking them into smaller, focused functions.\n");
            }
            
            // Check for code duplication
            if (duplicationCount > 5) {
                writer.write("- ⚠️ Significant code duplication detected. Consider refactoring to reduce duplication.\n");
            }
            
            // Check for deeply nested loops
            if (maxLoopNestingLevel > 2) {
                writer.write("- ⚠️ Deeply nested loops detected. Consider refactoring to improve performance and readability.\n");
            }
            
            // Security recommendations
            for (String unsafe : unsafeFunctions) {
                // Check if this unsafe function is used in the file
                if (fileContent.toString().matches(".*\\b" + unsafe + "\\s*\\(.*")) {
                    writer.write("- 🔴 Replace usage of unsafe function '" + unsafe + "' with " + 
                               (safeAlternatives.containsKey(unsafe) ? safeAlternatives.get(unsafe) : "safer alternatives") + "\n");
                }
            }
            
            writer.write("\n## 📝 Conclusion\n\n");
            
            // Calculate overall quality score (simplified)
            double qualityScore = 100;
            
            // Reduce score for various issues
            if (functionCount > 0) {
                qualityScore -= Math.min(20, complexFunctions.size() * 2); // Complexity issues
                qualityScore -= Math.min(20, longFunctions.size()); // Size issues
                qualityScore -= Math.min(10, potentialPerformanceIssues.size() * 2); // Performance issues
                qualityScore -= Math.min(15, (1 - memoryBalanceRatio) * 30); // Memory issues
                qualityScore -= Math.min(10, singleResponsibilityViolations.size() * 2); // OOP issues
                qualityScore -= Math.min(10, gotoStatements * 2); // Control flow issues
                qualityScore -= Math.min(10, duplicationCount); // Duplication issues
                
                // Add points for good practices
                if (commentLines > 0 && (double)commentLines/codeLines > 0.1) {
                    qualityScore += 5; // Good commenting ratio
                }
                if (anyModernFeatures) {
                    qualityScore += 5; // Modern C++ usage
                }
                if (memoryBalanceRatio > 0.95) {
                    qualityScore += 5; // Good memory management
                }
            }
            
            // Limit the score to 0-100 range
            qualityScore = Math.max(0, Math.min(100, qualityScore));
            
            // Determine quality grade
            String qualityGrade;
            if (qualityScore >= 90) {
                qualityGrade = "A (Excellent)";
            } else if (qualityScore >= 80) {
                qualityGrade = "B (Good)";
            } else if (qualityScore >= 70) {
                qualityGrade = "C (Average)";
            } else if (qualityScore >= 60) {
                qualityGrade = "D (Below Average)";
            } else {
                qualityGrade = "F (Needs Significant Improvement)";
            }
            
            writer.write("Overall Code Quality Score: " + df.format(qualityScore) + "/100 - Grade " + qualityGrade + "\n\n");
            writer.write("This report was generated by the CppAnalyzer static analysis tool.\n");
            writer.write("For more information on how to improve your code, please refer to C++ best practices guidelines.\n");
            
            writer.close();
            br.close();
        }
        
        public static void main(String[] args) {
            if (args.length < 2) {
                System.out.println("Usage: java CppAnalyzer <input_file> <output_file>");
                return;
            }
            
            try {
                analyzeCppFile(args[0], args[1]);
                System.out.println("Analysis complete. Report written to " + args[1]);
            } catch (IOException e) {
                System.err.println("Error analyzing file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }