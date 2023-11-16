package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class AuthorPage extends BaseAbstractPage {
    public AuthorPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }

}
