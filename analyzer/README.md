# Analyzer Directory

This folder contains the core Java source code for the static analyzers:

- **CppAnalyzer.java** — Analyzes C++ code for unsafe functions, complexity, memory issues, OOP metrics, and more.
- **CudaAnalyzer.java** — Analyzes CUDA code for performance, memory, and correctness.
- **PythonAnalyzer.java** — Analyzes Python code for unsafe patterns, complexity, and best practices.

## Usage
These classes are compiled and run as part of the Byte Inspector toolkit. See the root README for build and usage instructions.

## For Developers
- Each analyzer can be run from the command line (see the `main` method in each file).
- Output is a detailed static analysis report for the input file.

---
_This directory is part of the MSc Mathematics Java course project._
