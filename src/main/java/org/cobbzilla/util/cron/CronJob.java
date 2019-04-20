package org.cobbzilla.util.cron;

import org.cobbzilla.util.reflect.ReflectionUtil;

import java.util.Properties;

public class CronJob {

    private String id;

    private String cronTimeString;

    private boolean startNow = false;

    private String commandClass;
    private Properties properties = new Properties();

    // todo
//    @Getter @Setter private String user;
//    @Getter @Setter private String shellCommand;

    public CronCommand getCommandInstance() {
        CronCommand command = ReflectionUtil.instantiate(commandClass);
        command.init(properties);
        return command;
    }

    public String getId() {
        return this.id;
    }

    public String getCronTimeString() {
        return this.cronTimeString;
    }

    public boolean isStartNow() {
        return this.startNow;
    }

    public String getCommandClass() {
        return this.commandClass;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCronTimeString(String cronTimeString) {
        this.cronTimeString = cronTimeString;
    }

    public void setStartNow(boolean startNow) {
        this.startNow = startNow;
    }

    public void setCommandClass(String commandClass) {
        this.commandClass = commandClass;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
