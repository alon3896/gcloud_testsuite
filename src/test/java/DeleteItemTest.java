
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import config.ConfigLoader;
import config.GcloudConfig;

/**
 * This class contains a test case for verifying the deletion of an object from
 * a Google Cloud Storage bucket and ensuring that the object becomes
 * inaccessible after deletion.
 *
 * The test performs the following steps: - Loads configuration details from a
 * YAML file. - Constructs and executes a `gcloud` command to delete the
 * specified object. - Verifies that the `gcloud` command executes successfully.
 * - Uses Playwright to navigate to the object's public URL and checks that the
 * object is no longer accessible (expects HTTP status 403 or 404).
 *
 * This test is designed to validate the proper functioning of object deletion
 * in Google Cloud Storage and ensure that deleted objects are no longer
 * publicly accessible.
 *
 * Dependencies: - Google Cloud SDK (`gcloud` CLI) must be installed and
 * configured. - Playwright library for browser automation. - TestNG for test
 * execution and assertions.
 */
public class DeleteItemTest {

    @Test
    public void testDeleteObjectAndVerifyInaccessible() throws Exception {
        GcloudConfig config = ConfigLoader.loadConfig("src/main/resources/config.yaml");
        String junkPath = config.getJunk_file_url();
        String https_junk = junkPath.replace("gs://", "https://storage.googleapis.com/");
        String deleteCommand = "gcloud storage rm " + junkPath;
        ProcessBuilder builder = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
            builder.command("cmd.exe", "/c", deleteCommand);
        } else {
            builder.command("sh", "-c", deleteCommand);
        }
        Process process = builder.start();
        int exitCode = process.waitFor();
        Assert.assertEquals(exitCode, 0, "gcloud delete command failed");
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            final int[] statusCode = {200};
            page.onResponse(response -> {
                if (response.url().equals(https_junk)) {
                    statusCode[0] = response.status();
                }
            });

            page.navigate(https_junk);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Expecting 403 or 404 depending on GCP bucket config
            Assert.assertTrue(
                    statusCode[0] == 403 || statusCode[0] == 404,
                    "Expected inaccessible object (403/404), but got: " + statusCode[0]
            );
        }

    }
}
