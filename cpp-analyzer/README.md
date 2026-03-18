# cpp-analyzer Directory

This module contains the static analysis engine for C++, CUDA, and Python code.

- `src/` — Java source code for analyzers, GUI, networking, and utilities
- `bin/` — Compiled classes
- `output/` — Analysis reports generated for uploaded files
- `test-files/` — Example code files for testing
- `instructions.md` — Detailed setup, usage, and troubleshooting instructions
- `README.md` — This file

## How to Use
**Always use the provided Makefiles for compilation and running.**

1. **Compile analyzers:**
   ```sh
   make -f ../../Makefile.mak compile
   ```
2. **Run the GUI:**
   ```sh
   make -f ../../Makegui.mak compile
   make -f ../../Makegui.mak run
   ```
3. **Run an analyzer (example):**
   ```sh
   make -f Makefilecpp.mak run INPUT_FILE=test.cpp
   make -f Makefilecuda.mak run INPUT_FILE=test.cu
   make -f Makefilepython.mak run INPUT_FILE=test.py
   ```
4. **View reports:**
   - Reports are saved in the `output/` directory.

For full details, see [instructions.md](instructions.md).

## Notes
- Used by the GUI and server for automated analysis.
- For more details, see the root README and `instructions.md`.
