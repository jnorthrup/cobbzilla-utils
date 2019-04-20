package org.cobbzilla.util.chef;

public class VendorDatabagSetting {

    private String path;
    private String shasum;
    private boolean block_ssh = false;

    public VendorDatabagSetting(String path, String shasum) {
        setPath(path);
        setShasum(shasum);
    }

    @java.beans.ConstructorProperties({"path", "shasum", "block_ssh"})
    public VendorDatabagSetting(String path, String shasum, boolean block_ssh) {
        this.path = path;
        this.shasum = shasum;
        this.block_ssh = block_ssh;
    }

    public VendorDatabagSetting() {
    }

    public String getPath() {
        return this.path;
    }

    public String getShasum() {
        return this.shasum;
    }

    public boolean isBlock_ssh() {
        return this.block_ssh;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setShasum(String shasum) {
        this.shasum = shasum;
    }

    public void setBlock_ssh(boolean block_ssh) {
        this.block_ssh = block_ssh;
    }
}
