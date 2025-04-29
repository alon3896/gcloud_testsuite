# GCloud Test Suite

This project contains a suite of tests for validating GCloud functionality.

## How to Run the Tests

Follow the steps below to run the test suite:

1. **Set Up GCloud Authentication**  

2. **Build the Project**  
    Ensure the project is built using Maven:
    ```bash
    mvn clean install
    ```

3. **Run the Tests**  
    Execute the test suite using the following command:
    ```bash
    mvn test
    ```

4. **View Test Results**  
    Test results will be displayed in the terminal. For detailed output, use:
    ```bash
    mvn test -Dtest=Verbose
    ```

## Notes
- Ensure you have Java and Maven installed.
- Make sure your GCloud CLI is properly configured.

## License
This project is licensed under the MIT License.
