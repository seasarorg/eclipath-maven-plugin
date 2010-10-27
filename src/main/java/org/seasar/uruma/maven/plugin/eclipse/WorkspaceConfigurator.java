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

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 */
public class WorkspaceConfigurator {
    public static final String ECLIPSE_PLUGINS_METADATA_DIR = ".metadata/.plugins";

    public static final String ECLIPSE_CORE_RUNTIME_SETTINGS_DIR = ECLIPSE_PLUGINS_METADATA_DIR
            + "/org.eclipse.core.runtime/.settings";

    public static final String ECLIPSE_JDT_CORE_PREFS_FILE = "org.eclipse.jdt.core.prefs";

    public static final String CLASSPATH_VARIABLE_M2_REPO = "org.eclipse.jdt.core.classpathVariable.M2_REPO";

    private final File workspaceDir;

    private String localRepositoryDir;

    /**
     * @param workspaceDir
     */
    public WorkspaceConfigurator(File workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public void configure() {
        File eclipseJdtCorePrefsFile = createEclipseJdtCorePrefsFile();
        Properties eclipseJdtCorePrefs = loadProperties(eclipseJdtCorePrefsFile);
        boolean changed = false;

        String localRepositoryDir = normalizePath(this.localRepositoryDir);
        String current = eclipseJdtCorePrefs.getProperty(CLASSPATH_VARIABLE_M2_REPO);
        if (current == null || !current.equals(localRepositoryDir)) {
            eclipseJdtCorePrefs.setProperty(CLASSPATH_VARIABLE_M2_REPO, localRepositoryDir);
            changed = true;
        }

        if (changed) {
            writeProperties(eclipseJdtCorePrefsFile, eclipseJdtCorePrefs);
        }
    }

    protected File createEclipseJdtCorePrefsFile() {
        String workspacePath = normalizePath(workspaceDir.getAbsolutePath()) + "/";
        String path = workspacePath + ECLIPSE_CORE_RUNTIME_SETTINGS_DIR + "/"
                + ECLIPSE_JDT_CORE_PREFS_FILE;
        return new File(path);
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

    protected void writeProperties(File file, Properties props) {
        OutputStream os = null;
        try {
            os = createOutputStream(file);
            props.store(os, null);
        } catch (IOException ex) {
            throw new PluginRuntimeException("Failed to save file. : " + file.getAbsolutePath(), ex);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    protected Properties loadProperties(File file) {
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = createInputStream(file);
            props.load(is);
            return props;
        } catch (IOException ex) {
            throw new PluginRuntimeException("Failed to load file. : " + file.getAbsolutePath(), ex);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    /**
     * Setter for {@code localRepositoryDir}.
     */
    public void setLocalRepositoryDir(String localRepositoryDir) {
        this.localRepositoryDir = localRepositoryDir;
    }
}
