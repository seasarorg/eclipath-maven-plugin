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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.seasar.uruma.maven.plugin.eclipse.exception.PluginRuntimeException;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 */
public class PropertiesFile {
    private final File file;

    private Properties properties;

    private boolean changed;

    /**
     * @param file
     */
    public PropertiesFile(File file) {
        this.file = file;
    }

    /**
     * @param file
     * @return
     */
    public void load() {
        properties = new Properties();
        InputStream is = null;
        try {
            is = createInputStream(file);
            properties.load(is);
            changed = false;
        } catch (IOException ex) {
            throw new PluginRuntimeException("Failed to load file. : " + file.getAbsolutePath(), ex);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * 
     */
    public void store() {
        if (!changed) {
            return;
        }

        OutputStream os = null;
        try {
            os = createOutputStream(file);
            properties.store(os, null);
        } catch (IOException ex) {
            throw new PluginRuntimeException("Failed to save file. : " + file.getAbsolutePath(), ex);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * @param key
     * @return
     */
    public String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * @param key
     * @param value
     */
    public boolean put(String key, String value) {
        String currentValue = properties.getProperty(key);
        if (currentValue == null) {
            if (value == null) {
                return false;
            }
        } else if (currentValue.equals(value)) {
            return false;
        }

        properties.setProperty(key, value);
        changed = true;
        return true;
    }

    protected InputStream createInputStream(File file) {
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ignore) {
            throw new PluginRuntimeException("File not found. : " + file.getAbsolutePath());
        }
    }

    protected OutputStream createOutputStream(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            return new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException ex) {
            throw new PluginRuntimeException("File not found. : " + file.getAbsolutePath());
        } catch (IOException ex) {
            throw new PluginRuntimeException("File could not create. : " + file.getAbsolutePath());
        }
    }
}
