import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import config.ConfigLoader;
import config.GcloudConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
/**
 * This test class, UpdateAccessTests, is designed to validate access control behavior
 * on a publicly available object in Google Cloud Storage (GCS) using the gcloud CLI and
 * verify the user-facing result through Playwright.
 *
 * It contains one test method:
 *
 * 1. `testUpdateAccessPublicToPrivate`:
 *    - Loads configuration values such as the GCS bucket and public object path from a YAML config file.
 *    - Executes a gcloud CLI command to change the object's access level from public to private.
 *    - Constructs the expected public URL based on the object's path.
 *    - Navigates to the public URL using a Playwright browser instance.
 *    - Asserts that accessing the now-private object results in an HTTP 403 Forbidden response.
 *    - Finally, resets the object’s access control back to public to maintain consistent testability.
 *
 * The class also includes a helper method:
 *
 * - `executeCommand`: A utility function that runs a given shell command using Java’s ProcessBuilder,
 *   automatically selecting the appropriate shell for Windows or Unix-like systems.
 *
 * Prerequisites:
 * - The gcloud CLI must be installed and authenticated with access to the relevant GCS bucket.
 * - The object used in the test must initially be publicly accessible (publicRead).
 * - A valid configuration file (`config.yaml`) must exist and include:
 *     - `bucket_name`
 *     - `public_file_url`
 * - The Playwright Java library must be available for browser-based user access validation.
 *
 * Note:
 * - This test validates the **end-user experience** by simulating how a user would access
 *   a public file through a browser, not just backend access or ACL changes.
 * - The access is reset at the end of the test to preserve consistent test environments.
 */
public class UpdateAccessTests {
    private static GcloudConfig config;
    private void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed: " + command);
        }
    }
    @Test
    public void testUpdateAccessPublicToPrivate() throws Exception{

        config = ConfigLoader.loadConfig("src/main/resources/config.yaml");
        String command = "gcloud storage objects update "+config.getPublic_file_url()+" --canned-acl=private";
        System.out.println(command);
        executeCommand(command);
        String https_url = config.getPublic_file_url().replace("gs://","https://storage.googleapis.com/" );
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
            String reset = "gcloud storage objects update "+config.getPublic_file_url()+" --canned-acl=publicRead";
            executeCommand(reset);

        }
    }

}
