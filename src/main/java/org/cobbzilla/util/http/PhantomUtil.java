package org.cobbzilla.util.http;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;

import java.io.File;

import static org.cobbzilla.util.io.FileUtil.abs;

public class PhantomUtil {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PhantomUtil.class);

    static { WebDriverManager.phantomjs().setup(); }

    // ensures static-initializer above gets run
    public static void init () {}

    private PhantomJSDriver defaultDriver() {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        return new PhantomJSDriver(capabilities);
    }

    public PhantomJSHandle execJs(String script) {
        final PhantomJSDriver driver = defaultDriver();
        final PhantomJSHandle handle = new PhantomJSHandle(driver);
        driver.setErrorHandler(handle);
        driver.executePhantomJS(script);
        return handle;
    }

    public static final String LOAD_AND_EXEC = "var page = require('webpage').create();\n" +
            "page.open('@@URL@@', function() {\n" +
            "  page.evaluateJavaScript('@@JS@@');\n" +
            "});\n";

    public void loadPage (File file) { loadPageAndExec(file, "console.log('successfully loaded "+abs(file)+"')"); }
    public void loadPage(String url) { loadPageAndExec(url, "console.log('successfully loaded "+url+"')"); }

    public void loadPageAndExec(File file, String script) { loadPageAndExec("file://"+abs(file), script); }

    public void loadPageAndExec(String url, String script) {
        execJs(LOAD_AND_EXEC.replace("@@URL@@", url).replace("@@JS@@", script));
    }
}
