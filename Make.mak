# Variables
SRC_DIR=/home/saketh/Desktop/java/project9/cpp-analyzer/src
BIN_DIR=/home/saketh/Desktop/java/project9/cpp-analyzer/bin
MAIN_CLASS=gui.FileProcessor

# Default target
all: compile

# Compile only FileProcessor.java and its dependencies
compile:
	javac -d $(BIN_DIR) -cp $(BIN_DIR) $(SRC_DIR)/gui/FileProcessor.java $(SRC_DIR)/analyzer/*.java

# Run the FileProcessor class
run: compile
	java -cp $(BIN_DIR) $(MAIN_CLASS)

# Clean the bin directory
clean:
	rm -rf $(BIN_DIR)/gui/FileProcessor.class
