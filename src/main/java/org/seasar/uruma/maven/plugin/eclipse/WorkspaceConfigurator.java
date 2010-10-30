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

import java.io.File;

import org.apache.maven.project.MavenProject;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class WorkspaceConfigurator {
    public static final String ECLIPSE_PLUGINS_METADATA_DIR = ".metadata/.plugins";

    public static final String ECLIPSE_CORE_RUNTIME_SETTINGS_DIR = ECLIPSE_PLUGINS_METADATA_DIR
            + "/org.eclipse.core.runtime/.settings";

    public static final String ECLIPSE_JDT_CORE_PREFS_FILE = "org.eclipse.jdt.core.prefs";

    public static final String CLASSPATH_VARIABLE_M2_REPO = "org.eclipse.jdt.core.classpathVariable.M2_REPO";

    private final File workspaceDir;

    private String localRepositoryDir;

    private PropertiesFile eclipseJdtCorePrefs;

    /**
     * @param workspaceDir
     */
    public WorkspaceConfigurator(MavenProject project) {
        this.workspaceDir = getWorkspaceDir(project);
    }

    /**
     * 
     */
    public void loadConfiguration() {
        eclipseJdtCorePrefs = new PropertiesFile(createEclipseJdtCorePrefsFile());
        eclipseJdtCorePrefs.load();
    }

    /**
     * 
     */
    public void configure() {
        String localRepositoryDir = normalizePath(this.localRepositoryDir);
        eclipseJdtCorePrefs.put(CLASSPATH_VARIABLE_M2_REPO, localRepositoryDir);
        eclipseJdtCorePrefs.store();
    }

    /**
     * @return
     */
    public String getClasspathVariableM2REPO() {
        return eclipseJdtCorePrefs.get(CLASSPATH_VARIABLE_M2_REPO);
    }

    protected File createEclipseJdtCorePrefsFile() {
        String workspacePath = normalizePath(workspaceDir.getAbsolutePath()) + "/";
        String path = workspacePath + ECLIPSE_CORE_RUNTIME_SETTINGS_DIR + "/"
                + ECLIPSE_JDT_CORE_PREFS_FILE;
        return new File(path);
    }

    protected String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    protected File getWorkspaceDir(MavenProject project) {
        // TODO マルチプロジェクト対応
        File workspaceDir = project.getFile().getParentFile().getParentFile();
        checkWorkspaceLocation(workspaceDir);
        return workspaceDir;
    }

    protected void checkWorkspaceLocation(File dir) {
        String path = normalizePath(dir.getAbsolutePath()) + "/"
                + ECLIPSE_CORE_RUNTIME_SETTINGS_DIR;
        File pluginDir = new File(path);
        if (pluginDir.exists()) {
            return;
        } else {
            throw new PluginRuntimeException("Directory is not eclipse workspace. : "
                    + dir.getAbsolutePath());
        }
    }

    /**
     * Setter for {@code localRepositoryDir}.
     */
    public void setLocalRepositoryDir(String localRepositoryDir) {
        this.localRepositoryDir = localRepositoryDir;
    }
}
