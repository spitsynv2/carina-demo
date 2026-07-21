package com.zebrunner.carina.demo.gui.pages.underarmour;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;

public class UnderArmourCheckoutPage extends AbstractPage {

    @FindBy(xpath = "//*[self::h1 or self::h2 or self::h3][normalize-space(.)='Wysyłka']")
    private ExtendedWebElement shippingHeading;

    @FindBy(xpath = "//*[self::h1 or self::h2 or self::h3][normalize-space(.)='Płatność']")
    private ExtendedWebElement paymentHeading;

    @FindBy(xpath = "//button[contains(normalize-space(.), 'Zaloguj się, aby płacić szybciej')]")
    private ExtendedWebElement fasterCheckoutLogin;

    @FindBy(xpath = "//*[(self::button or self::summary or @role='button' or @aria-expanded) and contains(normalize-space(.), 'Podsumowanie koszyka') and not(.//*[(self::button or self::summary or @role='button' or @aria-expanded) and contains(normalize-space(.), 'Podsumowanie koszyka')])]")
    private ExtendedWebElement cartSummaryToggle;

    @FindBy(xpath = "//*[self::h1 or self::h2 or self::h3][contains(., 'Podsumowanie zamówienia')]")
    private ExtendedWebElement orderSummaryHeading;

    @FindBy(xpath = "//*[contains(text(), 'Szacowana suma')]")
    private ExtendedWebElement estimatedTotalLabel;

    @FindBy(css = "input[name='firstName']")
    private ExtendedWebElement firstNameInput;

    @FindBy(css = "input[name='lastName']")
    private ExtendedWebElement lastNameInput;

    @FindBy(css = "input[name='address1']")
    private ExtendedWebElement address1Input;

    @FindBy(css = "input[name='city']")
    private ExtendedWebElement cityInput;

    @FindBy(css = "input[name='postalCode'], input[name='zipCode']")
    private ExtendedWebElement postalCodeInput;

    @FindBy(xpath = "(//*[contains(text(), 'DHL Express Priority')])[1]")
    private ExtendedWebElement dhlPriorityOption;

    @FindBy(xpath = "//button[contains(normalize-space(.), 'Przejdź do płatności')]")
    private ExtendedWebElement continueToPaymentButton;

    @FindBy(css = "#payment-header")
    private ExtendedWebElement paymentHeader;

    public UnderArmourCheckoutPage(WebDriver driver) {
        super(driver);
    }

    public boolean isShippingHeadingVisible(long timeout) {
        return shippingHeading.isVisible(timeout);
    }

    public boolean isPaymentSectionVisible(long timeout) {
        return paymentHeading.isVisible(timeout);
    }

    public boolean isFasterCheckoutLoginVisible(long timeout) {
        return fasterCheckoutLogin.isVisible(timeout);
    }

    public boolean isCartSummaryTogglePresent(long timeout) {
        return cartSummaryToggle.isVisible(timeout);
    }

    public boolean openCartSummary(long timeout) {
        if (!cartSummaryToggle.isVisible(timeout)) {
            return false;
        }
        cartSummaryToggle.scrollTo();
        cartSummaryToggle.isClickable(timeout);
        cartSummaryToggle.click(timeout);
        return true;
    }

    public void closeCartSummary(long timeout) {
        if (!cartSummaryToggle.isVisible(timeout)) {
            return;
        }
        cartSummaryToggle.isClickable(timeout);
        cartSummaryToggle.click(timeout);
    }

    public boolean isOrderSummaryHeadingVisible(long timeout) {
        return orderSummaryHeading.isVisible(timeout);
    }

    public boolean isProductListedInSummary(String productName, long timeout) {
        ExtendedWebElement product = findExtendedWebElement(
                By.xpath("(//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                        + productName.toLowerCase() + "')])[1]"),
                timeout);
        return product.isVisible(timeout);
    }

    public boolean isEstimatedTotalVisible(long timeout) {
        return estimatedTotalLabel.isVisible(timeout);
    }

    public void fillShippingAddress(String firstName, String lastName, String address1, String city, String postalCode,
            long timeout) {
        typeInto(firstNameInput, firstName, timeout);
        typeInto(lastNameInput, lastName, timeout);
        typeInto(address1Input, address1, timeout);
        typeInto(cityInput, city, timeout);
        typeInto(postalCodeInput, postalCode, timeout);

        dhlPriorityOption.assertElementPresent(timeout);
        dhlPriorityOption.scrollTo();
        dhlPriorityOption.isClickable(timeout);
        dhlPriorityOption.click(timeout);
    }

    private void typeInto(ExtendedWebElement input, String value, long timeout) {
        input.assertElementPresent(timeout);
        input.scrollTo();
        input.click(timeout);
        input.type(value, timeout);
    }

    public boolean isContinueToPaymentVisible(long timeout) {
        return continueToPaymentButton.isVisible(timeout);
    }

    public void continueToPayment(long timeout) {
        continueToPaymentButton.scrollTo();
        continueToPaymentButton.isClickable(timeout);
        continueToPaymentButton.click(timeout);
    }

    public boolean isPaymentStepActive(long timeout) {
        if (!paymentHeader.isElementPresent(timeout)) {
            return false;
        }
        return "true".equals(paymentHeader.getAttribute("data-active"));
    }

    public boolean isShippingAddressShown(String value, long timeout) {
        ExtendedWebElement line = findExtendedWebElement(
                By.xpath("(//*[contains(text(), '" + value + "')])[1]"), timeout);
        return line.isVisible(timeout);
    }
}
