# GCloud Test Suite made by Alon Haim Chitlaru for mend.io

This project contains a suite of tests for validating GCloud functionality.



## ✅ Prerequisites

1. **Java 17+**  
2. **Maven 3.8+**  
3. **Node.js & npm**
4. **gcloud CLI** (authenticated: `gcloud init`)
5. **Playwright**

## How to Run the Tests
Follow the steps below to run the test suite:
1. **Set Up GCloud Authentication**  
2. **modify  src/main/resources/config.yaml with your bucket and file info
3. **Build the Project**  
    Ensure the project is built using Maven:
    ```bash
    mvn clean install
    ```

4. **Run the Tests**  
    Execute the test suite using the following command:
    ```bash
    mvn test
    ```

## Notes
- Make sure your GCloud CLI is properly configured.

## License
This project is licensed under the MIT License.
