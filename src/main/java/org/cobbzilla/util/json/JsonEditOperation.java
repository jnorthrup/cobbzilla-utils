package org.cobbzilla.util.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.List;

import static org.cobbzilla.util.json.JsonUtil.FULL_MAPPER;

@Accessors(chain=true)
public class JsonEditOperation {

    private JsonEditOperationType type;
    private String path;
    private String json;

    public boolean isRead() { return type == JsonEditOperationType.read; }

    public JsonNode getNode () throws IOException { return FULL_MAPPER.readTree(json); }

    public boolean hasIndex () { return getIndex() != null; }

    private List<String> initTokens() { return JsonUtil.tokenize(path); }
    @JsonIgnore
    private List<String> tokens = null;

    public boolean isEmptyBrackets () {
        int bracketPos = path.indexOf("[");
        int bracketClosePos = path.indexOf("]");
        return bracketPos != -1 && bracketClosePos != -1 && bracketClosePos == bracketPos+1;
    }

    public Integer getIndex() {
        List<String> tokens = getTokens();
        if (tokens.size() <= 1) return index(path);
        return index(tokens.get(tokens.size()-1));
    }

    private Integer index(String path) {
        try {
            int bracketPos = path.indexOf("[");
            int bracketClosePos = path.indexOf("]");
            if (bracketPos != -1 && bracketClosePos != -1 && bracketClosePos > bracketPos) {
                return new Integer(path.substring(bracketPos + 1, bracketClosePos));
            }
        } catch (Exception ignored) {}
        return null;
    }

    public String getName() {
        final List<String> tokens = getTokens();
        if (tokens.size() <= 1) return stripEmptyTrailingBrackets(path);
        return stripEmptyTrailingBrackets(tokens.get(tokens.size() - 1));
    }

    private String stripEmptyTrailingBrackets(String path) {
        return path.endsWith("[]") ? path.substring(0, path.length()-2) : path;
    }

    public int getNumPathSegments() { return getTokens().size(); }

    public String getName(int part) {
        return getTokens().get(part); }

    public JsonEditOperationType getType() {
        return this.type;
    }

    public String getPath() {
        return this.path;
    }

    public String getJson() {
        return this.json;
    }

    public List<String> getTokens() {
        if(tokens==null)tokens=initTokens();
        return this .tokens;
    }

    public JsonEditOperation setType(JsonEditOperationType type) {
        this.type = type;
        return this;
    }

    public JsonEditOperation setPath(String path) {
        this.path = path;
        return this;
    }

    public JsonEditOperation setJson(String json) {
        this.json = json;
        return this;
    }
}
