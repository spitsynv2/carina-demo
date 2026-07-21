package com.zebrunner.carina.demo.gui.pages.underarmour;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;

public class UnderArmourSearchResultsPage extends AbstractPage {

    @FindBy(xpath = "//main//h1[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')]")
    private ExtendedWebElement resultHeading;

    @FindBy(xpath = "(//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')])[1]")
    private ExtendedWebElement productLink;

    public UnderArmourSearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isResultHeadingVisible(String term, long timeout) {
        return resultHeading.format(term.toLowerCase()).isVisible(timeout);
    }

    public UnderArmourProductPage openProduct(String productName, long timeout) {
        ExtendedWebElement link = productLink.format(productName.toLowerCase());
        link.assertElementPresent(timeout);
        link.scrollTo();
        link.click(timeout);
        return new UnderArmourProductPage(getDriver());
    }
}
