
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import config.ConfigLoader;
import config.GcloudConfig;

/**
 * This test class, SignedUrlTests, is designed to validate the functionality of
 * generating and verifying signed URLs using Google Cloud's gcloud CLI. It
 * contains two test methods:
 *
 * 1. `generateSignedUrlTest`: - Generates a signed URL for a private object in
 * Google Cloud Storage using the gcloud CLI. - Captures the signed URL from the
 * command output and writes it to a file for later use. - Ensures the signed
 * URL is successfully generated and saved.
 *
 * 2. `verifyAccessToSignedUrlTest`: - Reads the signed URL from the file
 * generated in the previous test. - Navigates to the signed URL using a
 * Playwright browser instance. - Verifies that the page content does not
 * indicate phishing or dangerous site warnings.
 *
 * The class also includes a helper method, `isPhish`, to detect phishing or
 * dangerous site warnings in the page content.
 *
 * Prerequisites: - The gcloud CLI must be installed and configured with
 * appropriate permissions. - A valid configuration file (config.yaml) must be
 * present with the required details. - The Playwright library must be available
 * for browser automation.
 *
 * Note: - The tests are dependent on the operating system for executing shell
 * commands. - The second test depends on the successful execution of the first
 * test.
 */
public class SignedUrlTests {

    private final Path SIGNED_URL_FILE = Path.of("target/signed_url.txt");

    public boolean isPhish(String content) {
        return content.contains("Attackers on the site you tried visiting might trick you into installing software or revealing things like your passwords, phone, or credit card numbers. Chrome strongly recommends going back to safety.")
                || content.contains("Dangerous site")
                || content.contains("Deceptive site")
                || content.contains("Back to safety")
                || content.contains("This site is dangerous")
                || content.contains("This site may harm your computer")
                || content.contains("This site may be hacked")
                || content.contains("Phishing")
                || content.contains("This site may be impersonating");
    }

    @Test(priority = 1)
    public void generateSignedUrlTest() throws Exception {
        GcloudConfig config = ConfigLoader.loadConfig("src/main/resources/config.yaml");
        String objectPath = config.getPrivate_object_path();
        String duration = config.getDuration();
        String command = String.format("gcloud storage sign-url %s --duration=%s", objectPath, duration);
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
        String line;
        String signedUrl = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (signedUrl == null && line.startsWith("signed_url:")) {
                signedUrl = line.trim();
                signedUrl = signedUrl.substring(signedUrl.indexOf(":") + 1).trim();
            }
        }
        System.out.println("Signed URL: " + signedUrl);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String errorMessage = "gcloud command failed with exit code " + exitCode;
            System.out.println(errorMessage);
            Assert.fail(errorMessage);
        }

        Assert.assertNotNull(signedUrl, "Failed to capture signed URL from gcloud output");
        Files.createDirectories(SIGNED_URL_FILE.getParent());

        Files.writeString(SIGNED_URL_FILE, signedUrl);
        System.out.println("Signed URL written to: " + SIGNED_URL_FILE.toAbsolutePath());
    }

    @Test(priority = 2, dependsOnMethods = "generateSignedUrlTest")
    public void verifyAccessToSignedUrlTest() throws Exception {
        Assert.assertTrue(Files.exists(SIGNED_URL_FILE), "Signed URL file does not exist");
        String signedUrl = Files.readString(SIGNED_URL_FILE).trim();
        Assert.assertTrue(signedUrl.startsWith("https://"));
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(signedUrl);
            Thread.sleep(10000);
            String content = page.content();
            Assert.assertFalse(isPhish(content), "The site is flagged as phishing or dangerous.");

        }
    }

}
