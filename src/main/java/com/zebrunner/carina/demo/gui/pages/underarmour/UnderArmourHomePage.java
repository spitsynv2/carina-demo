package com.zebrunner.carina.demo.gui.pages.underarmour;

import java.lang.invoke.MethodHandles;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;

public class UnderArmourHomePage extends AbstractPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FindBy(xpath = "//*[@data-testid='dialog-base'][contains(., 'właściwej stronie Under Armour')]")
    private ExtendedWebElement locationDialog;

    @FindBy(xpath = "//*[@data-testid='dialog-base'][contains(., 'właściwej stronie Under Armour')]//*[@data-testid='dialog-close-button' or contains(@aria-label, 'Zamknij')]")
    private ExtendedWebElement locationDialogClose;

    @FindBy(xpath = "/html/body/div[13]/div")
    private ExtendedWebElement cookieBanner;

    @FindBy(xpath = "/html/body/div[13]/div/div[2]//div/div/div[1]/div/div[4]/button[2]")
    private ExtendedWebElement cookieBannerClose;

    @FindBy(xpath = "//*[@data-testid='dialog-base'][contains(@class, 'email-signup')]")
    private ExtendedWebElement emailDialog;

    @FindBy(xpath = "//*[@data-testid='dialog-base'][contains(@class, 'email-signup')]//*[contains(@class, 'modal-close-button') or @data-testid='dialog-close-button' or contains(@aria-label, 'Zamknij')]")
    private ExtendedWebElement emailDialogClose;

    @FindBy(xpath = "(//input[contains(@aria-label, 'Szukaj według słowa kluczowego') or contains(@placeholder, 'Szukaj według słowa kluczowego')])[1]")
    private ExtendedWebElement searchField;

    public UnderArmourHomePage(WebDriver driver) {
        super(driver);
    }

    public void dismissInitialBanners(long locationTimeout, long emailTimeout) {
        dismissDialog("location", locationDialog, locationDialogClose, locationTimeout);
        dismissCookieBanner(locationTimeout);
        dismissDialog("email", emailDialog, emailDialogClose, emailTimeout);
    }

    private void dismissDialog(String name, ExtendedWebElement dialog, ExtendedWebElement closeControl, long timeout) {
        if (!dialog.isVisible(timeout)) {
            return;
        }
        LOGGER.info("Dismissing {} dialog", name);
        closeControl.isClickable(timeout);
        closeControl.click(timeout);
        dialog.waitUntilElementDisappear(timeout);
    }

    // TrustArc consent lives inside the host's shadow root; XPath cannot cross it, so enter shadowRoot explicitly.
    private static final By COOKIE_HOST = By.cssSelector(".truste_popframe, [id^='pop-frame']");

    private void dismissCookieBanner(long timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (clickRejectViaJs()) {
                LOGGER.info("Dismissing cookie dialog (TrustArc shadow DOM)");
                return;
            }
            pause(1L);
        }
        LOGGER.info("Cookie dialog not present within {}s", timeoutSeconds);
    }

    private boolean clickRejectViaShadowRoot() {
        try {
            for (WebElement host : getDriver().findElements(COOKIE_HOST)) {
                for (WebElement button : host.getShadowRoot().findElements(By.cssSelector("button.required"))) {
                    button.click();
                    return true;
                }
            }
        } catch (Exception ignored) {
            // shadow root not ready or getShadowRoot unsupported on this driver
        }
        return false;
    }

    private boolean clickRejectViaJs() {
        String clickJs = "var host=document.querySelector('.truste_popframe,[id^=pop-frame]');"
                + "if(!host||!host.shadowRoot){return 'none';}"
                + "var b=host.shadowRoot.querySelector('button.required')"
                + "||[].find.call(host.shadowRoot.querySelectorAll('button,a'),function(e){return /odrzu/i.test(e.textContent||'');});"
                + "if(!b){return 'nobtn';}b.click();return 'clicked';";
        try {
            return "clicked".equals(((JavascriptExecutor) getDriver()).executeScript(clickJs));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSearchReady(long timeout) {
        return searchField.isVisible(timeout);
    }

    public UnderArmourSearchResultsPage search(String term, long timeout) {
        searchField.isClickable(timeout);
        searchField.type(term, timeout);
        searchField.sendKeys(Keys.ENTER);
        return new UnderArmourSearchResultsPage(getDriver());
    }
}
