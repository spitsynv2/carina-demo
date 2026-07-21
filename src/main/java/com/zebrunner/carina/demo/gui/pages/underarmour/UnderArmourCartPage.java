package com.zebrunner.carina.demo.gui.pages.underarmour;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;

public class UnderArmourCartPage extends AbstractPage {

    @FindBy(xpath = "//*[self::h1 or self::h2 or self::h3][contains(., 'Twój koszyk')]")
    private ExtendedWebElement cartHeading;

    @FindBy(xpath = "//*[contains(text(), 'Suma cząstkowa')]")
    private ExtendedWebElement subtotalLabel;

    @FindBy(css = "[data-testid='checkout-order-checkout-button']")
    private ExtendedWebElement checkoutButton;

    public UnderArmourCartPage(WebDriver driver) {
        super(driver);
    }

    public boolean isCartHeadingVisible(long timeout) {
        return cartHeading.isVisible(timeout);
    }

    public boolean isSubtotalVisible(long timeout) {
        return subtotalLabel.isVisible(timeout);
    }

    public boolean isCheckoutButtonEnabled(long timeout) {
        return checkoutButton.isClickable(timeout);
    }

    public UnderArmourCheckoutPage startCheckout(long timeout) {
        checkoutButton.scrollTo();
        checkoutButton.isClickable(timeout);
        checkoutButton.click(timeout);
        return new UnderArmourCheckoutPage(getDriver());
    }
}
