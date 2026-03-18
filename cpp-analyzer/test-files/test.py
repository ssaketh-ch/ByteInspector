import time

# Inefficient loop (should be optimized using list comprehension)
def slow_function():
    result = []
    for i in range(10000):
        result.append(i * 2)  # Inefficient loop operation
    return result

# Unused variable
unused_variable = 42

# Unnecessary sleep (Bad practice in production code)
def delayed_function():
    time.sleep(5)  # This delays execution and might be flagged
    return "Done"

# Potential division by zero
def risky_function(x):
    return 100 / x  # Should handle x == 0 case

# Calling functions
if __name__ == "__main__":
    print("Running test script...")
    data = slow_function()
    print("Processed", len(data), "items")
    
    try:
        print("Risky function result:", risky_function(0))  # Intentional error
    except ZeroDivisionError:
        print("Caught division by zero error!")

    print(delayed_function())
