package org.cobbzilla.util.system;

import lombok.experimental.Accessors;

import java.util.regex.Pattern;

@Accessors(chain=true)
public class CommandProgressMarker {

    private int percent;
    private Pattern pattern;
    private String line;

    @java.beans.ConstructorProperties({"percent", "pattern", "line"})
    public CommandProgressMarker(int percent, Pattern pattern, String line) {
        this.percent = percent;
        this.pattern = pattern;
        this.line = line;
    }

    public CommandProgressMarker() {
    }

    public int getPercent() {
        return this.percent;
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    public String getLine() {
        return this.line;
    }

    public CommandProgressMarker setPercent(int percent) {
        this.percent = percent;
        return this;
    }

    public CommandProgressMarker setPattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public CommandProgressMarker setLine(String line) {
        this.line = line;
        return this;
    }
}
