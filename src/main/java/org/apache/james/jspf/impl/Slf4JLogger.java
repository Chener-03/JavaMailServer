package org.apache.james.jspf.impl;

import org.apache.james.jspf.core.Logger;

public class Slf4JLogger implements Logger {

    private org.slf4j.Logger logger = null;

    private String name = null;

    public Slf4JLogger(String name) {
        this.name = name;
        logger = org.slf4j.LoggerFactory.getLogger(name);
    }

    @Override
    public void debug(String message) {

    }

    @Override
    public void debug(String message, Throwable throwable) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void info(String message) {

    }

    @Override
    public void info(String message, Throwable throwable) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void warn(String message) {

    }

    @Override
    public void warn(String message, Throwable throwable) {

    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String message, Throwable throwable) {

    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void fatalError(String message) {

    }

    @Override
    public void fatalError(String message, Throwable throwable) {

    }

    @Override
    public boolean isFatalErrorEnabled() {
        return false;
    }

    @Override
    public Logger getChildLogger(String name) {
        return new Slf4JLogger(this.name + "." + name);
    }
}
