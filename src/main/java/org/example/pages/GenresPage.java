package org.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

public class GenresPage extends BaseAbstractPage{
    By bookOfSeriesXpath = By.xpath("//div[@class='product-cover']//a[@class='cover']");
    public GenresPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }

}
