package org.cobbzilla.util.graphics;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class ImageTransformConfig {

    private int height;
    private int width;

    public ImageTransformConfig(String config) {
        final int xpos = config.indexOf('x');
        try {
            width = Integer.parseInt(config.substring(xpos + 1));
            height = Integer.parseInt(config.substring(0, xpos));
        } catch (Exception e) {
            die("invalid config (expected WxH): " + config + ": " + e, e);
        }
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
