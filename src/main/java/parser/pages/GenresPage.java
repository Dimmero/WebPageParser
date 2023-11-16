package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class GenresPage extends BaseAbstractPage{
    public GenresPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }
}
