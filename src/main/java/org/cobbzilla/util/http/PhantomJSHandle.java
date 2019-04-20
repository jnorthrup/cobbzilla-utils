package org.cobbzilla.util.http;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.ErrorHandler;
import org.slf4j.Logger;

import java.io.Closeable;

public class PhantomJSHandle extends ErrorHandler implements Closeable {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PhantomJSHandle.class);
    final PhantomJSDriver driver;

    @java.beans.ConstructorProperties({"driver"})
    public PhantomJSHandle(PhantomJSDriver driver) {
        this.driver = driver;
    }

    @Override public void close() {
        if (driver != null) {
            driver.close();
            driver.quit();
        }
    }

    public PhantomJSDriver getDriver() {
        return this.driver;
    }
}