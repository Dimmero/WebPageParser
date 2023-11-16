package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class SeriesPage extends BaseAbstractPage{
    public SeriesPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }
}
