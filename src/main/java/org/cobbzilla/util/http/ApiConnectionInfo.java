package org.cobbzilla.util.http;

import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

public class ApiConnectionInfo {

    private String baseUri;
    private String user;
    private String password;

    public ApiConnectionInfo (String baseUri) { this.baseUri = baseUri; }

    public ApiConnectionInfo (ApiConnectionInfo other) { copy(this, other); }

    @java.beans.ConstructorProperties({"baseUri", "user", "password"})
    public ApiConnectionInfo(String baseUri, String user, String password) {
        this.baseUri = baseUri;
        this.user = user;
        this.password = password;
    }

    public ApiConnectionInfo() {
    }

    // alias for when this is used in json with snake_case naming conventions
    public String getBase_uri () { return getBaseUri(); }
    public void setBase_uri (String uri) { setBaseUri(uri); }

    public java.lang.String toString() {
        return "ApiConnectionInfo(baseUri=" + this.baseUri + ", user=" + this.user + ")";
    }

    public String getBaseUri() {
        return this.baseUri;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
