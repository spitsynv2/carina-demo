package com.zebrunner.carina.demo.gui.pages.underarmour;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;

public class UnderArmourProductPage extends AbstractPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FindBy(xpath = "(//main//h1)[1]")
    private ExtendedWebElement productTitle;

    @FindBy(css = "[aria-label^='Rozmiar:']")
    private List<ExtendedWebElement> sizeOptions;

    @FindBy(css = "[data-testid='add-to-bag-button']")
    private ExtendedWebElement addToBagButton;

    @FindBy(xpath = "//*[@role='dialog'][.//*[contains(., 'Produkt dodany do koszyka')]]")
    private ExtendedWebElement addToCartConfirmation;

    @FindBy(css = "[data-testid='view-bag-btn']")
    private ExtendedWebElement viewBagButton;

    public UnderArmourProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean isProductTitleVisible(long timeout) {
        return productTitle.isVisible(timeout);
    }

    public String selectAvailableSize(long timeout) {
        List<ExtendedWebElement> options = findExtendedWebElements(By.cssSelector("[aria-label^='Rozmiar:']"), timeout);
        if (options.size() < 2) {
            throw new RuntimeException("Expected at least 2 size options, found " + options.size());
        }
        ExtendedWebElement option = options.get(1);
        String label = option.getAttribute("aria-label");
        option.scrollTo();
        option.click(timeout);
        return label == null ? "" : label.replaceFirst("(?i)^Rozmiar:\\s*", "").trim();
    }

    public void addToCart(String selectedSize, long timeout) {
        addToBagButton.scrollTo();
        addToBagButton.isClickable(timeout);
        addToBagButton.click(timeout);
        addToCartConfirmation.assertElementPresent(timeout);
        String confirmation = addToCartConfirmation.getText();
        if (confirmation == null || !confirmation.matches("(?is).*Rozmiar:\\s*" + java.util.regex.Pattern.quote(selectedSize) + ".*")) {
            LOGGER.warn("Selected size '{}' not found in add-to-cart confirmation text", selectedSize);
        }
    }

    public void viewCart(long timeout) {
        viewBagButton.isClickable(timeout);
        viewBagButton.click(timeout);
    }
}
