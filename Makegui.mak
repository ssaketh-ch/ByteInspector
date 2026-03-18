# Java compiler
JAVAC = javac

# Java runtime
JAVA = java

# JavaFX dependencies (modify this path if needed)
PATH_TO_FX = /home/saketh/Downloads/javafx-sdk-24/lib
JAVAFX_MODULES = javafx.controls,javafx.fxml

# Source and Binary Directories
SRC_DIR = cpp-analyzer/src/gui
BIN_DIR = bin

# Main GUI Class
MAIN_CLASS = gui.AnalysisDashboard

# Find all Java source files in gui/
JAVA_FILES = $(wildcard $(SRC_DIR)/*.java)

# Default target
all: compile run

# Compile JavaFX GUI
compile:
	@mkdir -p $(BIN_DIR)
	cd cpp-analyzer/src && \
	$(JAVAC) --module-path $(PATH_TO_FX) --add-modules $(JAVAFX_MODULES) -d ../../$(BIN_DIR) gui/*.java

# Run the GUI application (Fixes warnings)
run: compile
	cd $(BIN_DIR) && \
	$(JAVA) --enable-native-access=javafx.graphics \
	        --add-opens=java.base/java.lang=ALL-UNNAMED \
	        --module-path $(PATH_TO_FX) \
	        --add-modules $(JAVAFX_MODULES) \
	        -cp . $(MAIN_CLASS)

# Clean output directory
clean:
	rm -rf $(BIN_DIR)/*.class

# Help message
help:
	@echo "Usage: make -f Makegui.mak run"
	@echo "       make -f Makegui.mak compile"
	@echo "       make -f Makegui.mak clean"

.PHONY: all compile run clean help
