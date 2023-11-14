package org.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SeriesPage extends BaseAbstractPage{
    By bookOfSeriesXpath = By.xpath("//div[@class='product-cover']//a[@class='cover']");
    public SeriesPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }

}
