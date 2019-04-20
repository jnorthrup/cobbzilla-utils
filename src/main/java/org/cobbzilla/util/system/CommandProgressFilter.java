package org.cobbzilla.util.system;

import lombok.experimental.Accessors;
import org.apache.tools.ant.util.LineOrientedOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Accessors(chain=true)
public class CommandProgressFilter extends LineOrientedOutputStream {

    private int pctDone = 0;
    private int indicatorPos = 0;
    private boolean closed = false;
    private CommandProgressCallback callback;

    public int getPctDone() {
        return this.pctDone;
    }

    public int getIndicatorPos() {
        return this.indicatorPos;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public CommandProgressCallback getCallback() {
        return this.callback;
    }

    public CommandProgressFilter setCallback(CommandProgressCallback callback) {
        this.callback = callback;
        return this;
    }

    private class CommandProgressIndicator {
        private int percent;
        private Pattern pattern;

        @java.beans.ConstructorProperties({"percent", "pattern"})
        public CommandProgressIndicator(int percent, Pattern pattern) {
            this.percent = percent;
            this.pattern = pattern;
        }

        public int getPercent() {
            return this.percent;
        }

        public Pattern getPattern() {
            return this.pattern;
        }

        public CommandProgressIndicator setPercent(int percent) {
            this.percent = percent;
            return this;
        }

        public CommandProgressIndicator setPattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }
    }

    private List<CommandProgressIndicator> indicators = new ArrayList<>();

    public CommandProgressFilter addIndicator(String pattern, int pct) {
        indicators.add(new CommandProgressIndicator(pct, Pattern.compile(pattern)));
        return this;
    }

    @Override public void close() throws IOException { closed = true; }

    @Override protected void processLine(String line) throws IOException {
        for (int i=indicatorPos; i<indicators.size(); i++) {
            final CommandProgressIndicator indicator = indicators.get(indicatorPos);
            if (indicator.getPattern().matcher(line).find()) {
                pctDone = indicator.getPercent();
                indicatorPos++;
                if (callback != null) callback.updateProgress(new CommandProgressMarker(pctDone, indicator.getPattern(), line));
                return;
            }
        }
    }

}
