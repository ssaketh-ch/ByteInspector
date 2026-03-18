#include <iostream>

void loopTest() {
    int i = 0;
    for (int i = 0; i < 10; i++) { // Shadowing warning
        while (true) { // Infinite loop
            break;
        }
    }
}

int main() {
    int j = 5;
    {
        int j = 10; // Shadowing warning
    }
    
    return 0;
}
