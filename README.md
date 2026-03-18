# Byte Inspector

Byte Inspector is a professional static code analysis toolkit for C++, CUDA, and Python, developed as a project for the MSc Mathematics (Java course requirement).

## Features
- **C++ Analyzer**: Detects unsafe functions, code complexity, memory issues, OOP metrics, and more.
- **CUDA Analyzer**: Analyzes CUDA-specific code for performance, memory, and correctness.
- **Python Analyzer**: Flags unsafe Python patterns, code complexity, and best practices.
- **JavaFX GUI**: User-friendly dashboard for uploading code and viewing analysis reports.
- **SimpleServer**: Java server for handling file uploads and dispatching analysis.

## Project Structure
- `analyzer/` — Java source for analyzers (CppAnalyzer, CudaAnalyzer, PythonAnalyzer)
- `bin/` — Compiled Java classes and resources
- `cpp-analyzer/` — Standalone C++/CUDA/Python analyzer module
- `src/`, `uploads/` — (Legacy/empty or for future use)
- `automate.sh` — Script to automate compilation and GUI launch
- `Makefile.mak`, `Make.mak`, `Makegui.mak` — Build scripts for analyzers and GUI
- `SimpleServer.java` — Java server for file uploads and analysis

## Setup & Usage
For detailed setup and usage, see [cpp-analyzer/instructions.md](cpp-analyzer/instructions.md).

### Quick Start
1. **Compile analyzers:**
   ```sh
   make -f Makefile.mak compile
   ```
2. **Compile and run GUI:**
   ```sh
   make -f Makegui.mak compile
   make -f Makegui.mak run
   ```
3. **Automate everything:**
   ```sh
   ./automate.sh
   ```
4. **Upload files:**
   - Place files in the `uploads/` directory or use the GUI.
5. **View reports:**
   - Reports are generated in `cpp-analyzer/output/`.

## Requirements
- Java 17+
- JavaFX SDK (update `PATH_TO_FX` in `Makegui.mak`)
- Linux (for `inotifywait` in `automate.sh`)
- GNU Make

## Contributing
Contributions are welcome! Please open an issue or pull request.

## License
[MIT License](LICENSE)

## Acknowledgments
- Developed for MSc Mathematics, 1st year Java course, 2025-26.
- Author: Saketh S.

---
For detailed analyzer and GUI usage, troubleshooting, and advanced options, see [cpp-analyzer/instructions.md](cpp-analyzer/instructions.md).
