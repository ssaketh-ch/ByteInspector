# Makefile for CppAnalyzer, PythonAnalyzer, and CudaAnalyzer

# Java compiler
JAVAC = javac

# Java runtime
JAVA = java

# Source directory
SRC_DIR = cpp-analyzer/src/analyzer

# Output directory for compiled classes
BIN_DIR = cpp-analyzer/bin/analyzer

# Main class names (with package)
MAIN_CPP = analyzer.CppAnalyzer
MAIN_PYTHON = analyzer.PythonAnalyzer
MAIN_CUDA = analyzer.CudaAnalyzer

# Source files
JAVA_FILES = $(wildcard $(SRC_DIR)/*.java)

# Test files directory
TEST_FILES_DIR = cpp-analyzer/test-files

# Output reports
OUTPUT_CPP = cpp-analyzer/output/cpp-analysis-report.txt
OUTPUT_PYTHON = cpp-analyzer/output/python-analysis-report.txt
OUTPUT_CUDA = cpp-analyzer/output/cuda-analysis-report.txt

# Default target
all: compile

# Compile all Java files
compile:
	@mkdir -p $(BIN_DIR)
	$(JAVAC) -d cpp-analyzer/bin $(JAVA_FILES)


# Run the CppAnalyzer
run-cpp: compile
	$(JAVA) -classpath cpp-analyzer/bin $(MAIN_CPP) $(TEST_FILES_DIR)/$(INPUT_FILE) $(OUTPUT_CPP)

# Run the PythonAnalyzer
run-python: compile
	$(JAVA) -classpath cpp-analyzer/bin $(MAIN_PYTHON) $(TEST_FILES_DIR)/$(INPUT_FILE) $(OUTPUT_PYTHON)

# Run the CudaAnalyzer
run-cuda: compile
	$(JAVA) -classpath cpp-analyzer/bin $(MAIN_CUDA) $(TEST_FILES_DIR)/$(INPUT_FILE) $(OUTPUT_CUDA)

# Clean up the output directory
clean:
	rm -rf $(BIN_DIR)
	rm -f $(OUTPUT_CPP) $(OUTPUT_PYTHON) $(OUTPUT_CUDA)

# Help message
help:
	@echo "Usage:"
	@echo "  make -f Makefile.mak compile        # Compile all Java files"
	@echo "  make -f Makefile.mak run-cpp INPUT_FILE=<filename>    # Run CppAnalyzer"
	@echo "  make -f Makefile.mak run-python INPUT_FILE=<filename> # Run PythonAnalyzer"
	@echo "  make -f Makefile.mak run-cuda INPUT_FILE=<filename>   # Run CudaAnalyzer"
	@echo "  make -f Makefile.mak clean          # Clean compiled files and output"

.PHONY: all compile run-cpp run-python run-cuda clean help
