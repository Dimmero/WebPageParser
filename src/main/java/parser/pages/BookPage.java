package parser.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class BookPage extends BaseAbstractPage{
    @FindBy(xpath = "//div[@class='authors']")
    public WebElement author;
    @FindBy(xpath = "//div[@class='authors']/a")
    public List<WebElement> authorLink;
    @FindBy(xpath = "//div[@id='product-title']/h1")
    public WebElement title;
    @FindBy(xpath = "//div[@class='publisher']")
    public WebElement publisher;
    @FindBy(xpath = "//div[@class='series']")
    public WebElement series;
    @FindBy(xpath = "//div[@class='series']/a")
    public WebElement seriesLink;
    @FindBy(xpath = "//div[@class='articul']")
    public WebElement bookId;
    @FindBy(xpath = "//div[@class='isbn']")
    public WebElement isbn;

    By booksAuthorXpath = By.xpath("//div[@data-key-carousel='this_author']//div[contains(@class,'product need-watch')]//a[@class='cover' and contains(@href,'/books/')]");
    By booksOfSeries = By.xpath("//div[@data-key-carousel='also_buy'][1]//div[contains(@class,'product need-watch')]//a[@class='cover' and contains(@href,'/books/')]");
    By bookOfSameGenre = By.xpath("//div[@data-key-carousel='also_buy'][2]//div[contains(@class,'product need-watch')]//a[@class='cover' and contains(@href,'/books/')]");

    public BookPage() {
        PageFactory.initElements(driver.getDriver(), this);
    }

    public String getAuthor() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(author));
        return author.getText().substring(7);
    }

    public String getTitle() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(title));
        return title.getText().replaceAll("^.*?:", "");
    }

    public String getPublisher() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(publisher));
        return publisher.getText().replaceAll("^.*?:", "");
    }

    public String getSeries() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(series));
        return series.getText().replaceAll("^.*?:", "");
    }

    public String getBookId() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(bookId));
        return bookId.getText().replaceAll("^.*?:", "");
    }

    public String getIsbn() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(isbn));
        return isbn.getText().replaceAll("^.*?:", "");
    }

    public String getSeriesHrefAttr() {
        driver.getShortWait10().until(ExpectedConditions.visibilityOf(seriesLink));
        return series.getAttribute("href");
    }

}
