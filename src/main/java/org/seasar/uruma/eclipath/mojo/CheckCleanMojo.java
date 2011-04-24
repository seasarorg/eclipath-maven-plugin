/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.uruma.eclipath.mojo;

import static org.seasar.uruma.eclipath.util.PathUtil.*;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.classpath.EclipseClasspath;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.FileDependency;

/**
 * @goal check-clean
 * @requiresDependencyResolution test
 * @phase process-sources
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CheckCleanMojo extends AbstractEclipathMojo {

    /*
     * @see org.seasar.uruma.eclipath.mojo.AbstractEclipathMojo#doExecute()
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        // Load ".classpath" file
        EclipseClasspath eclipseClasspath = new EclipseClasspath(eclipseProjectDir);
        eclipseClasspath.load();

        // Get existing files
        List<File> existingFiles = getExistingFiles(getLibDirectories());
        Map<String, File> existingFileMap = new HashMap<String, File>(existingFiles.size());
        for (File file : existingFiles) {
            existingFileMap.put(file.getAbsolutePath(), file);
        }

        // Check existing files
        List<Dependency> dependencies = resolveArtifacts(getEclipathArtifacts());
        for (Dependency dependency : dependencies) {
            if (dependency instanceof FileDependency) {
                FileDependency fileDependency = (FileDependency) dependency;
                reconceile(existingFileMap, fileDependency.getLibraryFile());
                reconceile(existingFileMap, fileDependency.getSourceFile());
                reconceile(existingFileMap, fileDependency.getJavadocFile());
            }
        }

        for (File dealingFile : existingFileMap.values()) {
            deal(dealingFile);
        }
    }

    protected void reconceile(Map<String, File> existingFileMap, File file) {
        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();
        if (existingFileMap.containsKey(path)) {
            existingFileMap.remove(path);
        }
    }

    protected void deal(File file) {
        Logger.info("Will be deleted. : " + file.getAbsolutePath());
    }

    protected List<File> getLibDirectories() {
        List<File> result = new ArrayList<File>();
        String[] relPaths = libraryLayout.getAllRelativePath();
        for (String relPath : relPaths) {
            String path = concat(normalizePath(eclipseProjectDir.getAbsolutePath()), relPath);
            result.add(new File(path));
            result.add(new File(concat(path, FileDependency.SOURCES_PREFIX)));
            result.add(new File(concat(path, FileDependency.JAVADOC_PREFIX)));
        }
        return result;
    }

    protected List<File> getExistingFiles(List<File> dirs) {
        List<File> result = new ArrayList<File>();
        for (File dir : dirs) {
            if (!dir.exists() || !dir.isDirectory()) {
                break;
            }
            File[] files = dir.listFiles((FileFilter) (new SuffixFileFilter(".jar")));
            Collections.addAll(result, files);
        }
        return result;
    }
}
