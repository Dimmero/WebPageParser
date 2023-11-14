package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SearchPage extends BaseAbstractPage{
    @FindBy(xpath = "//div[@class='product-card__price-container']")
    public WebElement productCard;
    @FindBy(xpath = "//div[@class='product-card__out-of-stock']")
    public WebElement outOfStock;
    @FindBy(xpath = "//div[@class='product-card gtm-watched']")
    public WebElement productCardElement;
    @FindBy(xpath = "//a[@class='product-card__img']")
    public WebElement productCardLink;
    By productCardLinkXpath = By.xpath("//a[@class='product-card__img']");
    public SearchPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }
    public void clickBookCard() {
        driver.getShortWait10().until(ExpectedConditions.elementToBeClickable(productCardLinkXpath));
        productCardLink.click();
    }

    public String getOutOfStockInfo() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(outOfStock));
        return outOfStock.getText();
    }

}
