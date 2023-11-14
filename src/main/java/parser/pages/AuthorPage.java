package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class AuthorPage extends BaseAbstractPage{
    By bookOfSeriesXpath = By.xpath("//div[@class='product-cover']//a[@class='cover']");
    public AuthorPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }

}
