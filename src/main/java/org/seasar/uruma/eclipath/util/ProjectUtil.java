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
package org.seasar.uruma.eclipath.util;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;

/**
 * Operations on eclipse projects.
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ProjectUtil {
    public static final String ECLIPSE_PLUGINS_METADATA_DIR = ".metadata/.plugins";

    public static final String DOT_PROJECT_FILENAME = ".project";

    public static final String JDT_PREFS_PATH = ".settings/org.eclipse.jdt.core.prefs";

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

    public static File getWorkspaceDir(MavenProject project) {
        // TODO Dealing with multi projects.
        File workspaceDir = project.getFile().getParentFile().getParentFile();
        if (isValidWorkspaceLocation(workspaceDir)) {
            return workspaceDir;
        } else {
            return null;
        }
    }

    protected static boolean isValidWorkspaceLocation(File dir) {
        String path = PathUtil.normalizePath(dir.getAbsolutePath()) + "/" + ECLIPSE_PLUGINS_METADATA_DIR;
        File pluginDir = new File(path);
        if (pluginDir.exists()) {
            return true;
        } else {
            Logger.warn("Directory is not eclipse workspace. : " + dir.getAbsolutePath());
            return false;
        }
    }

}
