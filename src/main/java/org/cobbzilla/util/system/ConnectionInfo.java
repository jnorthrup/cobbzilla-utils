package org.cobbzilla.util.system;

public class ConnectionInfo {

    private String host;
    private Integer port;

    @java.beans.ConstructorProperties({"host", "port", "username", "password"})
    public ConnectionInfo(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public ConnectionInfo() {
    }

    public boolean hasPort () { return port != null; }

    private String username;
    private String password;

    // convenience methods
    public String getUser () { return getUsername(); }
    public void setUser (String user) { setUsername(user); }

    public ConnectionInfo (String host, Integer port) {
        this(host, port, null, null);
    }

    public String getHost() {
        return this.host;
    }

    public Integer getPort() {
        return this.port;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
