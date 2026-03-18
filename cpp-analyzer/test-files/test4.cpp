#include <iostream>
#include <cstdlib>

class MemoryTest {
private:
    int* ptr;
public:
    MemoryTest() { ptr = new int[10]; } // Memory leak: No delete[]
    void setValue(int index, int value) {
        if (index >= 10) return; // Out of bounds protection
        ptr[index] = value;
    }
};

void complexFunction(int a, int b, int c, int d, int e, int f, int g) { // Too many parameters
    int x; // Unused variable
    int* memLeak = new int; // Memory leak

    for (int i = 0; i < 10; i++) {
        while (true) { // Infinite loop
            break;
        }
    }

    if (a > 0) {
        if (b > 0) {
            if (c > 0) {
                if (d > 0) {
                    if (e > 0) {
                        if (f > 0) {
                            std::cout << "Deeply nested conditions" << std::endl // Missing semicolon
                        }
                    }
                }
            }
        }
    }

    try {
        throw "An error occurred";
    } catch (int e) { // Incorrect exception type (throwing char*)
        std::cout << "Caught exception" << std::endl;
    }

    return;
    std::cout << "Unreachable code" << std::endl; // Unreachable code
}

int main() {
    MemoryTest obj; // No destructor, memory leak
    int y; // Unused variable
    int* ptr = (int*)malloc(sizeof(int)); // Memory leak (malloc without free)
    int j = 5;

    {
        int j = 10; // Shadowing warning
    }

    complexFunction(1, 2, 3, 4, 5, 6, 7);

    return 0;
}
