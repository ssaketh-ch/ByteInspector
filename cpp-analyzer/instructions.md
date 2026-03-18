CppAnalyzer & GUI - Setup and Usage Guide

This project includes:

    CppAnalyzer – A static code analyzer for C++, CUDA, and Python.

    GUI (Analysis Dashboard) – A JavaFX-based graphical interface to interact with the analyzer.

    Makefiles – Automate compilation and execution.

📌 Prerequisites

Before running the tools, ensure you have:

    Java 17+ installed (java -version).

    JavaFX SDK installed and configured (echo $PATH_TO_FX).

    Make installed (make -v).

🛠️ Setting Up JavaFX Path

Find your JavaFX SDK location:

    find ~ -type d -name "javafx-sdk*" 2>/dev/null

    Set the PATH_TO_FX environment variable:

    export PATH_TO_FX=/path/to/javafx/lib

    #(Replace /path/to/javafx/lib with the actual path.)

Add it to your .zshrc or .bashrc:

    echo 'export PATH_TO_FX=/path/to/javafx/lib' >> ~/.zshrc
    source ~/.zshrc  # Or source ~/.bashrc if using bash

🚀 Running the Tools

Navigate to the project directory:

cd ~/Desktop/java/project

1️⃣ Running the GUI

To compile and run the JavaFX-based GUI:

make -f Makefilegui.mak compile
make -f Makefilegui.mak run

2️⃣ Running CppAnalyzer

To analyze C++ code:

make -f Makefilecpp.mak run INPUT_FILE=test.cpp

3️⃣ Running CUDA Analyzer

To analyze CUDA code:

make -f Makefilecuda.mak run INPUT_FILE=Cudatest.cu

4️⃣ Running Python Analyzer

To analyze Python code:

make -f Makefilepython.mak run INPUT_FILE=test.py

🧹 Cleaning Up

To remove compiled files:

make -f Makefilegui.mak clean
make -f Makefilecpp.mak clean
make -f Makefilecuda.mak clean
make -f Makefilepython.mak clean

🔧 Troubleshooting
1️⃣ JavaFX Errors (e.g., Module Not Found)

    Ensure PATH_TO_FX is set correctly:

    echo $PATH_TO_FX
    ls $PATH_TO_FX

    If it's empty, follow the setup steps above.

2️⃣ Makefile Not Found

    Ensure you're in the correct directory:

    ls | grep Makefile

    If missing, redownload the Makefiles or create them manually.

📌 Notes

    The output reports are stored in cpp-analyzer/output/.

    The test files should be placed in cpp-analyzer/test-files/.

    Modify Makefile.mak if paths need adjustments.

Now you're all set! 🚀 Happy coding! 🎯