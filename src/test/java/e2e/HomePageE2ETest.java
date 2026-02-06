package e2e;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class HomePageE2ETest {

    private static final String BASE_URL = System.getenv().getOrDefault("E2E_BASE_URL", "http://localhost:8080");
    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeEach
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
    }

    @AfterEach
    public void tearDown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    public void testHomePageLoads() {
        page.navigate(BASE_URL);
        assertThat(page.locator("h1")).isVisible();
        assertThat(page.locator("h1")).hasText("Welcome to Our Website");
    }

    @Test
    public void testNavigationMenu() {
        page.navigate(BASE_URL);
        assertThat(page.locator("nav")).isVisible();
        
        page.click("text=About");
        assertThat(page.locator("h2")).hasText("About Us");

        page.click("text=Contact");
        assertThat(page.locator("h2")).hasText("Contact Us");
    }

    @Test
    public void testLoginForm() {
        page.navigate(BASE_URL + "/login");
        
        page.fill("#username", "testuser");
        page.fill("#password", "testpass");
        page.click("button[type=submit]");

        assertThat(page.locator("#login-success")).isVisible();
        assertThat(page.locator("#login-success")).hasText("Login Successful");
    }
}
