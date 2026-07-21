package com.zebrunner.carina.demo;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.zebrunner.agent.core.annotation.TestLabel;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.demo.gui.pages.underarmour.UnderArmourCartPage;
import com.zebrunner.carina.demo.gui.pages.underarmour.UnderArmourCheckoutPage;
import com.zebrunner.carina.demo.gui.pages.underarmour.UnderArmourHomePage;
import com.zebrunner.carina.demo.gui.pages.underarmour.UnderArmourProductPage;
import com.zebrunner.carina.demo.gui.pages.underarmour.UnderArmourSearchResultsPage;
import com.zebrunner.carina.utils.mobile.IMobileUtils;

/**
 * Carina Safari mirror of playwright-mobile-bridge-tests underarmour-buy-flow.spec.ts.
 * Used to benchmark Carina/Appium vs the Playwright iOS bridge on the same guest buy flow.
 */
public class UnderArmourBuyFlowTest implements IAbstractTest, IMobileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String BASE_URL = env("UA_BASE_URL", "ua.base.url", "https://www.underarmour.pl/pl-pl")
            .replaceAll("/+$", "");
    private static final String SEARCH_TERM = env("UA_SEARCH_TERM", "ua.search.term", "koszulka");
    private static final String PRODUCT_NAME = env("UA_PRODUCT_NAME", "ua.product.name", "koszulka");

    private static final String SHIP_FIRST_NAME = env("UA_SHIPPING_FIRST_NAME", "ua.shipping.firstName", "Jan");
    private static final String SHIP_LAST_NAME = env("UA_SHIPPING_LAST_NAME", "ua.shipping.lastName", "Kowalski");
    private static final String SHIP_ADDRESS1 = env("UA_SHIPPING_ADDRESS1", "ua.shipping.address1", "Marszalkowska 1");
    private static final String SHIP_CITY = env("UA_SHIPPING_CITY", "ua.shipping.city", "Warszawa");
    private static final String SHIP_POSTAL_CODE = env("UA_SHIPPING_POSTAL_CODE", "ua.shipping.postalCode", "00-001");

    private static final long INITIAL_BANNER_TIMEOUT = 15;
    private static final long EMAIL_BANNER_TIMEOUT = 25;
    private static final long DEFAULT_TIMEOUT = 20;
    private static final long LONG_TIMEOUT = 30;

    @Test
    @MethodOwner(owner = "qpsdemo")
    @TestLabel(name = "feature", value = { "web", "benchmark" })
    public void guestBuyFlowReachesCheckout() {


        WebDriver driver = getDriver();
        long flowStart = System.currentTimeMillis();

        UnderArmourHomePage homePage = new UnderArmourHomePage(driver);
        long stepStart = System.currentTimeMillis();
        homePage.openURL(BASE_URL + "/");
        Assert.assertTrue(driver.getCurrentUrl().contains("underarmour.pl/pl-pl"), "Home URL not reached");
        homePage.dismissInitialBanners(INITIAL_BANNER_TIMEOUT, EMAIL_BANNER_TIMEOUT);
        Assert.assertTrue(homePage.isSearchReady(DEFAULT_TIMEOUT), "Search box is not ready");
        logStep("01-open-home", stepStart);

        stepStart = System.currentTimeMillis();
        UnderArmourSearchResultsPage resultsPage = homePage.search(SEARCH_TERM, DEFAULT_TIMEOUT);
        Assert.assertTrue(waitForUrl(u -> u.contains("/search"), LONG_TIMEOUT),
                "Search results URL not reached: " + driver.getCurrentUrl());
        Assert.assertTrue(resultsPage.isResultHeadingVisible(SEARCH_TERM, DEFAULT_TIMEOUT), "Search heading not shown");
        logStep("02-search-catalog", stepStart);

        stepStart = System.currentTimeMillis();
        UnderArmourProductPage productPage = resultsPage.openProduct(PRODUCT_NAME, LONG_TIMEOUT);
        Assert.assertTrue(waitForUrl(u -> u.contains("/pl-pl/p/") && u.contains(".html"), LONG_TIMEOUT),
                "Product URL not reached: " + driver.getCurrentUrl());
        Assert.assertTrue(productPage.isProductTitleVisible(DEFAULT_TIMEOUT), "Product title not visible");
        logStep("03-open-product", stepStart);

        stepStart = System.currentTimeMillis();
        String selectedSize = productPage.selectAvailableSize(DEFAULT_TIMEOUT);
        LOGGER.info("Selected size: {}", selectedSize);
        logStep("04-select-size", stepStart);

        stepStart = System.currentTimeMillis();
        productPage.addToCart(selectedSize, LONG_TIMEOUT);
        logStep("05-add-to-cart", stepStart);

        stepStart = System.currentTimeMillis();
        productPage.viewCart(LONG_TIMEOUT);
        Assert.assertTrue(waitForUrl(u -> u.contains("/cart") || u.contains("/checkout"), LONG_TIMEOUT),
                "Cart/checkout URL not reached: " + driver.getCurrentUrl());
        String cartUrl = driver.getCurrentUrl();
        UnderArmourCartPage cartPage = new UnderArmourCartPage(driver);
        UnderArmourCheckoutPage checkoutPage;
        if (cartUrl.contains("/checkout")) {
            checkoutPage = new UnderArmourCheckoutPage(driver);
        } else {
            Assert.assertTrue(cartPage.isCartHeadingVisible(DEFAULT_TIMEOUT), "Cart heading not visible");
            Assert.assertTrue(cartPage.isSubtotalVisible(DEFAULT_TIMEOUT), "Subtotal not visible");
            Assert.assertTrue(cartPage.isCheckoutButtonEnabled(DEFAULT_TIMEOUT), "Checkout button not enabled");
            checkoutPage = cartPage.startCheckout(DEFAULT_TIMEOUT);
        }
        logStep("06-view-cart", stepStart);

        stepStart = System.currentTimeMillis();
        Assert.assertTrue(waitForUrl(u -> u.contains("/pl-pl/checkout"), LONG_TIMEOUT),
                "Checkout URL not reached: " + driver.getCurrentUrl());
        Assert.assertTrue(checkoutPage.isShippingHeadingVisible(LONG_TIMEOUT), "Shipping section not visible");
        Assert.assertTrue(checkoutPage.isPaymentSectionVisible(DEFAULT_TIMEOUT), "Payment section not visible");
        if (!checkoutPage.isFasterCheckoutLoginVisible(5)) {
            LOGGER.warn("Faster checkout login entry not visible (non-blocking on mobile checkout)");
        }
        pause(2);
        logStep("07-start-checkout", stepStart);

        stepStart = System.currentTimeMillis();
        if (checkoutPage.openCartSummary(DEFAULT_TIMEOUT)) {
            Assert.assertTrue(checkoutPage.isOrderSummaryHeadingVisible(DEFAULT_TIMEOUT), "Order summary heading not visible");
            Assert.assertTrue(checkoutPage.isProductListedInSummary(PRODUCT_NAME, DEFAULT_TIMEOUT), "Product not listed in summary");
            Assert.assertTrue(checkoutPage.isEstimatedTotalVisible(DEFAULT_TIMEOUT), "Estimated total not visible");
            checkoutPage.closeCartSummary(DEFAULT_TIMEOUT);
        } else {
            LOGGER.warn("Cart summary toggle not present (non-blocking on mobile checkout)");
        }
        logStep("08-verify-summary", stepStart);

        stepStart = System.currentTimeMillis();
        checkoutPage.fillShippingAddress(SHIP_FIRST_NAME, SHIP_LAST_NAME, SHIP_ADDRESS1, SHIP_CITY, SHIP_POSTAL_CODE,
                DEFAULT_TIMEOUT);
        Assert.assertTrue(checkoutPage.isContinueToPaymentVisible(DEFAULT_TIMEOUT), "Continue to payment not visible");
        checkoutPage.continueToPayment(DEFAULT_TIMEOUT);

        Assert.assertTrue(waitForUrl(u -> u.contains("/pl-pl/checkout/payment"), LONG_TIMEOUT),
                "Payment page URL not reached: " + driver.getCurrentUrl());
        Assert.assertTrue(checkoutPage.isPaymentStepActive(DEFAULT_TIMEOUT), "Payment step is not active");
        Assert.assertTrue(checkoutPage.isShippingAddressShown(SHIP_ADDRESS1, DEFAULT_TIMEOUT), "Shipping address not shown");
        Assert.assertTrue(checkoutPage.isShippingAddressShown(SHIP_CITY, DEFAULT_TIMEOUT), "Shipping city not shown");
        Assert.assertTrue(checkoutPage.isShippingAddressShown(SHIP_POSTAL_CODE, DEFAULT_TIMEOUT), "Shipping postal code not shown");
        logStep("09-fill-shipping", stepStart);

        LOGGER.info("TOTAL buy flow duration: {} ms", System.currentTimeMillis() - flowStart);
    }

    private boolean waitForUrl(java.util.function.Predicate<String> condition, long timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        String current;
        do {
            current = getDriver().getCurrentUrl();
            if (current != null && condition.test(current)) {
                return true;
            }
            pause(1L);
        } while (System.currentTimeMillis() < deadline);
        current = getDriver().getCurrentUrl();
        return current != null && condition.test(current);
    }

    private void logStep(String name, long stepStart) {
        LOGGER.info("STEP {} took {} ms", name, System.currentTimeMillis() - stepStart);
    }

    private static String env(String envKey, String sysKey, String defaultValue) {
        String value = System.getenv(envKey);
        if (value == null || value.isEmpty()) {
            value = System.getProperty(sysKey);
        }
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }
}
