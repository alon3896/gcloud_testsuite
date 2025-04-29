
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;

import config.ConfigLoader;
import config.GcloudConfig;

/**
 * This class contains test cases to validate the presence and accessibility of
 * objects in a Google Cloud Storage bucket. It performs the following:
 *
 * 1. Verifies that a public object exists in the bucket and can be accessed via
 * a browser. 2. Ensures that a private object exists in the bucket but cannot
 * be accessed publicly.
 *
 * The tests utilize: - Google Cloud CLI commands to list objects in the bucket.
 * - Playwright for browser-based testing to validate object accessibility. -
 * TestNG for assertions to ensure test outcomes meet expectations.
 *
 * Configuration details such as bucket name, public file URL, and private
 * object path are loaded from a YAML configuration file.
 */
public class ObjectListTests {

    @Test
    public void testPublicObjectExistsAndLoadsInBrowser() throws Exception {
        GcloudConfig config = ConfigLoader.loadConfig("src/main/resources/config.yaml");
        String bucket = config.getBucket_name();
        System.out.println("Bucket: " + bucket);
        String command = "gcloud storage objects list gs://" + bucket;
        System.out.println("Command: " + command);
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder;
        if (isWindows) {
            // On Windows, use cmd.exe
            builder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            // On Linux/MacOS, use /bin/sh
            builder = new ProcessBuilder("/bin/sh", "-c", command);
        }
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        boolean objectFound = false;
        String expected = config.getPublic_file_url();
        System.out.println("Expected URL: " + expected);
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("1" + line);
            if (line.contains(expected)) {

                objectFound = true;
                break;
            }
        }
        System.out.println("Object found: " + objectFound);
        Assert.assertTrue(objectFound, "Expected object not found in bucket");

        // UX: open public URL
        String https_url = config.getPublic_file_url().replace("gs://", "https://storage.googleapis.com/");
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            Response response = page.waitForResponse(
                    resp -> resp.url().equals(https_url),
                    () -> page.navigate(https_url) // Navigate to the URL
            );
            int status = response.status();
            Assert.assertEquals(status, 200, "Public file did not load successfully");
        }
    }

    @Test
    public void TestPrivateObjectExisitsAndNotLoad() throws Exception {
        GcloudConfig config = ConfigLoader.loadConfig("src/main/resources/config.yaml");
        String bucket = config.getBucket_name();
        System.out.println("Bucket: " + bucket);
        String command = "gcloud storage objects list gs://" + bucket;
        System.out.println("Command: " + command);
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder;
        if (isWindows) {
            // On Windows, use cmd.exe
            builder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            // On Linux/MacOS, use /bin/sh
            builder = new ProcessBuilder("/bin/sh", "-c", command);
        }
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        boolean objectFound = false;
        String expected = config.getPrivate_object_path();
        System.out.println("Expected URL: " + expected);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(expected)) {
                objectFound = true;
                break;
            }
        }
        System.out.println("Object found: " + objectFound);
        Assert.assertTrue(objectFound, "Expected object not found in bucket");
        String https_url = config.getPrivate_object_path().replace("gs://", "https://storage.googleapis.com/");
        System.out.println(https_url);
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            final int[] statusCode = {200};
            page.onResponse(response -> {
                if (response.url().equals(https_url)) {
                    statusCode[0] = response.status();
                }
            });

            page.navigate(https_url);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            Assert.assertEquals(statusCode[0], 403, "Expected HTTP 403 for private object access");
        }
    }

}
