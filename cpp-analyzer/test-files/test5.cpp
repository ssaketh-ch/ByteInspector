#include <iostream>
#include <vector>
#include <memory>
#include <exception>

// Exception class for handling custom errors
class MyException : public std::exception {
public:
    const char* what() const noexcept override {
        return "Custom exception: Invalid operation!";
    }
};

// A well-designed class with proper memory management and Rule of 5
class DataContainer {
private:
    std::vector<int> data;
    std::unique_ptr<int> id; // Smart pointer ensures automatic cleanup

public:
    // Constructor
    explicit DataContainer(int size) : data(size, 0), id(std::make_unique<int>(size)) {
        std::cout << "DataContainer initialized with size: " << size << std::endl;
    }

    // Copy Constructor (Deleted to enforce move semantics)
    DataContainer(const DataContainer&) = delete;

    // Move Constructor
    DataContainer(DataContainer&& other) noexcept 
        : data(std::move(other.data)), id(std::move(other.id)) {
        std::cout << "Move constructor called!" << std::endl;
    }

    // Move Assignment Operator
    DataContainer& operator=(DataContainer&& other) noexcept {
        if (this != &other) {
            data = std::move(other.data);
            id = std::move(other.id);
            std::cout << "Move assignment called!" << std::endl;
        }
        return *this;
    }

    // Destructor
    ~DataContainer() {
        std::cout << "DataContainer destroyed." << std::endl;
    }

    // Function to modify data safely
    void setValue(size_t index, int value) {
        if (index >= data.size()) {
            throw MyException();
        }
        data[index] = value;
    }

    // Function to print stored data
    void print() const {
        std::cout << "Data: ";
        for (const auto& val : data) {
            std::cout << val << " ";
        }
        std::cout << std::endl;
    }
};

// Main function demonstrating safe memory and exception handling
int main() {
    try {
        DataContainer obj(5);  // Properly managed memory with smart pointers

        obj.setValue(2, 42);   // Valid index
        obj.print();           // Expected output: Data: 0 0 42 0 0

        DataContainer newObj = std::move(obj); // Move operation
        newObj.print(); // Ensures moved object still holds valid data

        obj.setValue(10, 100); // This will trigger an exception

    } catch (const MyException& e) {
        std::cerr << "Error: " << e.what() << std::endl;
    } catch (const std::exception& e) {
        std::cerr << "Standard exception: " << e.what() << std::endl;
    }

    return 0;
}
