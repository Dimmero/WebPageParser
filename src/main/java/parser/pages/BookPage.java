package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class BookPage extends BaseAbstractPage {
    public BookPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }
}
