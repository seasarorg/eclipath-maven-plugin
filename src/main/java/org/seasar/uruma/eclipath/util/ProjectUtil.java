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
package org.seasar.uruma.eclipath.util;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;

/**
 * Operations on eclipse projects.
 *
 * @author y-komori
 */
public class ProjectUtil {
    public static final String ECLIPSE_PLUGINS_METADATA_DIR = ".metadata/.plugins";

    public static final String DOT_PROJECT_FILENAME = ".project";

    public static final String JDT_PREFS_PATH = ".settings/org.eclipse.jdt.core.prefs";

    /**
     * Property key on pom.xml or settings.xml which indicate eclipse workspace
     * directory
     */
    public static final String PROP_KEY_ECLIPSE_WORKSPACE = "eclipse.workspace";

    private ProjectUtil() {

    }

    public static File getProjectDir(MavenProject project) {
        File dir = project.getFile().getParentFile();
        while (true) {
            File projectFile = new File(PathUtil.normalizePath(dir.getAbsolutePath()) + "/" + DOT_PROJECT_FILENAME);
            if (projectFile.exists()) {
                return dir;
            } else {
                dir = dir.getParentFile();
                if (dir == null) {
                    throw new PluginRuntimeException("eclipse project directory is not found.");
                }
            }
        }
    }

    public static File getJdtPrefsFile(MavenProject project) {
        File projectDir = getProjectDir(project);
        String path = projectDir.getAbsolutePath() + System.getProperty("file.separator") + JDT_PREFS_PATH;
        return new File(path);
    }

    /**
     * Returns eclipse workspace directory. First, this method determine the
     * eclipse workspace directory from pom.xml's directory. Second, determines
     * from "eclipse.workspace" property key which defined in the pom.xml or
     * settinegs.xml. If, this method can't find the workspace directory,
     * returns {@code null}.
     *
     * @param project
     *        Maven project
     * @return workspace directory, or {@code null}
     */
    public static File getWorkspaceDir(MavenProject project) {
        // TODO Dealing with multi projects.
        File workspaceDir = project.getFile().getParentFile().getParentFile();
        if (isValidWorkspaceLocation(workspaceDir)) {
            return workspaceDir;
        } else {
            String prop = project.getProperties().getProperty(PROP_KEY_ECLIPSE_WORKSPACE);
            if (prop != null) {
                workspaceDir = new File(prop);
                if (isValidWorkspaceLocation(workspaceDir)) {
                    return workspaceDir;
                }
            }

            Logger.warn("Can't find eclipse workspace.");
            return null;
        }
    }

    protected static boolean isValidWorkspaceLocation(File dir) {
        String path = PathUtil.normalizePath(dir.getAbsolutePath()) + "/" + ECLIPSE_PLUGINS_METADATA_DIR;
        File pluginDir = new File(path);
        return pluginDir.exists();
    }

}
