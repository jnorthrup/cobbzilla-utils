package org.cobbzilla.util.string;

import org.slf4j.Logger;

import java.util.ResourceBundle;

public abstract class ResourceMessages  {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ResourceMessages.class);

    protected String getBundleName() { return "labels/"+getClass().getSimpleName(); }

    private final ResourceBundle bundle = ResourceBundle.getBundle(getBundleName());

    // todo: add support for locale-specific bundles and messages
    public String translate(String messageTemplate) {

        // strip leading/trailing curlies if they are there
        while (messageTemplate.startsWith("{")) messageTemplate = messageTemplate.substring(1);
        while (messageTemplate.endsWith("}")) messageTemplate = messageTemplate.substring(0, messageTemplate.length()-1);

        try {
            return getBundle().getString(messageTemplate);
        } catch (Exception e) {
            log.error("translate: Error looking up "+messageTemplate+": "+e);
            return messageTemplate;
        }
    }

    public ResourceBundle getBundle() {
        return this.bundle;
    }
}
