/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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
package org.seasar.uruma.maven.plugin.eclipse;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Logger {
    private final Log log;
    
    private static final String PREFIX = "[uruma-eclipse] ";

    public static final String SEPARATOR = StringUtils.repeat("-", 56);

    public Logger(Log log) {
        if (log == null) {
            throw new NullArgumentException("log");
        }
        this.log = log;
    }
    
    public void debug(String message) {
        log.debug(PREFIX + message);
    }

    public void info(String message) {
        log.info(PREFIX + message);
    }

    public void warn(String message) {
        log.warn(PREFIX + message);
    }

    public void error(String message) {
        log.error(PREFIX + message);
    }

    public void error(String message, Throwable throwable) {
        log.error(PREFIX + message, throwable);
    }
}
