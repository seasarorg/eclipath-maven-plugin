/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.uruma.eclipath;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;

/**
 * Log utility for this plugin.
 *
 * @author y-komori
 */
public class Logger {
    private static Log myLog;

    private static final String PREFIX = "[eclipath] ";

    private static final String PREFIX_D = "  " + PREFIX;

    private static final String PREFIX_I = "   " + PREFIX;

    private static final String PREFIX_W = PREFIX;

    private static final String PREFIX_E = "  " + PREFIX;

    public static final String SEPARATOR = StringUtils.repeat("-", 56);

    private Logger() {
    }

    public static void initialize(Log log) {
        if (log == null) {
            throw new NullArgumentException("log");
        }
        myLog = log;
    }

    public static void debug(String message) {
        checkInitialized();
        myLog.debug(PREFIX_D + message);
    }

    public static void info(String message) {
        checkInitialized();
        myLog.info(PREFIX_I + message);
    }

    public static void warn(String message) {
        checkInitialized();
        myLog.warn(PREFIX_W + message);
    }

    public static void warn(String message, Throwable throwable) {
        checkInitialized();
        myLog.warn(PREFIX_W + message, throwable);
    }

    public static void error(String message) {
        checkInitialized();
        myLog.error(PREFIX_E + message);
    }

    public static void error(String message, Throwable throwable) {
        checkInitialized();
        myLog.error(PREFIX_E + message, throwable);
    }

    private static void checkInitialized() {
        if (myLog == null) {
            throw new PluginRuntimeException("Logger is not initialized.");
        }
    }
}
