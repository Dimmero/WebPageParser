package parser.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class Cookies extends BaseAbstractPage {
    @FindBy(xpath = "//button[contains(@class,'cookie-policy__button')]")
    public static WebElement acceptCookies;

    public Cookies() {
        PageFactory.initElements(driver.getDriver(), this);
    }

    public void turnOffCookies() {
        try {
            driver.getShortWait10().pollingEvery(Duration.ofMillis(500)).until(ExpectedConditions.elementToBeClickable(acceptCookies));
            acceptCookies.click();
        } catch (Exception o) {
            System.out.println("no stupid cookies, go further");
        }
    }
}
