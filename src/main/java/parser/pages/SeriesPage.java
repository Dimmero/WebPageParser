package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class SeriesPage extends BaseAbstractPage{
    By bookOfSeriesXpath = By.xpath("//div[@class='product-cover']//a[@class='cover']");
    public SeriesPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }

}
