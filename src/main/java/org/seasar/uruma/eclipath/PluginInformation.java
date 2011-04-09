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
package org.seasar.uruma.eclipath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.seasar.uruma.eclipath.exception.PluginRuntimeException;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public class PluginInformation {
    private static final String KEY_VERSION = "VERSION";

    private static final String INFO_PROP = "/info.properties";

    private final Properties infoProp;

    public PluginInformation() {
        InputStream is = getClass().getResourceAsStream(INFO_PROP);
        infoProp = new Properties();
        try {
            infoProp.load(is);
        } catch (IOException ex) {
            throw new PluginRuntimeException(ex);
        }
    }

    public String getVersion() {
        return infoProp.getProperty(KEY_VERSION);
    }
}
