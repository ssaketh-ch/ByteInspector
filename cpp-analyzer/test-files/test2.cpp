#include <iostream>
#include <cstdlib>

void badFunction(int a, int b, int c, int d, int e, int f) { // Too many parameters
    int x; // Unused variable
    int *ptr = (int*)malloc(sizeof(int)); // Memory leak
    std::cout << "Hello, world!" << std::endl
}

int main() {
    int y; // Unused variable
    int* leak = new int; // Memory leak
    std::cout << "Testing C++ Analyzer" << std::endl;

    while(true) { // Infinite loop
        break;
    }

    try {
        throw 5;
    } catch (...) {} // Empty catch block

    return 0;
    std::cout << "This is unreachable code"; // Unreachable code
}
