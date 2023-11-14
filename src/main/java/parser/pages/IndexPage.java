package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class IndexPage extends BaseAbstractPage{
    @FindBy(id = "search-field")
    public WebElement searchInput;
    By searchInputId = By.id("search-field");
    public IndexPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }
    public void sendIsbnAndSubmit(String isbn) {
        driver.getShortWait10().until(ExpectedConditions.elementToBeClickable(searchInput));
        searchInput.sendKeys(isbn, Keys.ENTER);
    }

    public void clickSearchInput() {
        driver.getShortWait10().until(ExpectedConditions.elementToBeClickable(searchInput));
        searchInput.click();
    }

}
