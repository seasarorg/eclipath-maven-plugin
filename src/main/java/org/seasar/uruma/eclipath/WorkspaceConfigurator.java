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

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;
import org.seasar.uruma.eclipath.util.PathUtil;
import org.seasar.uruma.eclipath.util.ProjectUtil;

/**
 * @author y-komori
 */
public class WorkspaceConfigurator {

    public static final String ECLIPSE_CORE_RUNTIME_SETTINGS_DIR = ProjectUtil.ECLIPSE_PLUGINS_METADATA_DIR
            + "/org.eclipse.core.runtime/.settings";

    public static final String ECLIPSE_JDT_CORE_PREFS_FILE = "org.eclipse.jdt.core.prefs";

    public static final String M2_REPO = "M2_REPO";

    public static final String CLASSPATH_VARIABLE_M2_REPO = "org.eclipse.jdt.core.classpathVariable." + M2_REPO;

    /**
     * Property key on pom.xml or settings.xml which indicate eclipse workspace
     * directory
     */
    public static final String PROP_KEY_ECLIPSE_WORKSPACE = "eclipse.workspace";

    private final File workspaceDir;

    private File localRepositoryDir;

    private PropertiesFile eclipseJdtCorePrefs;

    /**
     * @param workspaceDir
     */
    public WorkspaceConfigurator(MavenProject project) {
        this.workspaceDir = ProjectUtil.getWorkspaceDir(project);
        if (this.workspaceDir != null) {
            Logger.info("eclipse workspace directory deteceted. : " + this.workspaceDir.getAbsolutePath());
        }
    }

    /**
     *
     */
    public void loadConfiguration() {
        File eclipseJdtCorePrefsFile = createEclipseJdtCorePrefsFile();
        if (eclipseJdtCorePrefsFile != null) {
            eclipseJdtCorePrefs = new PropertiesFile(eclipseJdtCorePrefsFile);
            eclipseJdtCorePrefs.load();
        }
    }

    /**
     *
     */
    public void configure() {
        String localRepositoryDir = this.localRepositoryDir.getAbsolutePath();
        localRepositoryDir = PathUtil.normalizePath(localRepositoryDir);
        eclipseJdtCorePrefs.put(CLASSPATH_VARIABLE_M2_REPO, localRepositoryDir);
        eclipseJdtCorePrefs.store();
    }

    public void checkConfigure() {
        if (!isConfigured()) {
            throw new PluginRuntimeException("Workspace is not configured.\n"
                    + "       Please execute configure-workspace goal first.");
        }
    }

    public boolean isConfigured() {
        if (eclipseJdtCorePrefs != null) {
            String localRespositoryDir = eclipseJdtCorePrefs.get(CLASSPATH_VARIABLE_M2_REPO);
            return PathUtil.normalizePath(this.localRepositoryDir.getAbsolutePath()).equals(localRespositoryDir);
        } else {
            return false;
        }
    }

    /**
     * @return
     */
    public String getClasspathVariableM2REPO() {
        return eclipseJdtCorePrefs.get(CLASSPATH_VARIABLE_M2_REPO);
    }

    protected File createEclipseJdtCorePrefsFile() {
        if (workspaceDir != null) {
            String workspacePath = PathUtil.normalizePath(workspaceDir.getAbsolutePath()) + "/";
            String path = workspacePath + ECLIPSE_CORE_RUNTIME_SETTINGS_DIR + "/" + ECLIPSE_JDT_CORE_PREFS_FILE;
            return new File(path);
        } else {
            return null;
        }
    }

    /**
     * Setter for {@code localRepositoryDir}.
     */
    public void setLocalRepositoryDir(File localRepositoryDir) {
        this.localRepositoryDir = localRepositoryDir;
    }
}
