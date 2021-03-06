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
package org.seasar.uruma.eclipath.model;

import static org.seasar.uruma.eclipath.Constants.*;
import static org.seasar.uruma.eclipath.util.PathUtil.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.seasar.uruma.eclipath.Logger;

/**
 * @author y-komori
 */
public class FileDependency extends AbstractDependency {
    public static final String SOURCES_PREFIX = "sources";

    public static final String JAVADOC_PREFIX = "javadoc";

    private final File projectDir;

    private final File libDir;

    private final File sourceDir;

    private final File javadocDir;

    public FileDependency(EclipathArtifact artifact, File projectDir, String libDir) {
        super(artifact);
        this.projectDir = projectDir;
        String path = normalizePath(projectDir.getAbsolutePath()) + "/" + libDir;
        this.libDir = new File(path);
        this.sourceDir = new File(path + "/" + SOURCES_PREFIX);
        this.javadocDir = new File(path + "/" + JAVADOC_PREFIX);
    }

    @Override
    public String getLibraryPath() {
        String parent = normalizePath(getRelativePath(projectDir, libDir));
        return parent + "/" + libraryArtifact.getFileName();
    }

    @Override
    public String getSourcePath() {
        if (sourceArtifact != null && sourceArtifact.isResolved()) {
            String parent = normalizePath(getRelativePath(projectDir, sourceDir));
            return parent + "/" + sourceArtifact.getFileName();
        } else {
            return null;
        }
    }

    @Override
    public String getJavadocPath() {
        if (javadocArtifact != null && javadocArtifact.isResolved()) {
            String path = "jar:platform:/resource/";
            path += projectDir.getName();
            path += "/";
            path += normalizePath(getRelativePath(projectDir, javadocDir));
            path += "/";
            path += javadocArtifact.getFileName();
            path += "!/";
            return path;
        } else {
            return null;
        }
    }

    @Override
    public File copyLibraryArtifact() throws IOException {
        return copyDependency(libraryArtifact, libDir);
    }

    @Override
    public File copySourceArtifact() throws IOException {
        return copyDependency(sourceArtifact, sourceDir);
    }

    @Override
    public File copyJavadocArtifact() throws IOException {
        return copyDependency(javadocArtifact, javadocDir);
    }

    private File copyDependency(EclipathArtifact artifact, File toDir) throws IOException {
        if (artifact == null || !artifact.isResolved()) {
            return null;
        }

        File srcFile = artifact.getFile();
        File destFile = new File(toDir.getAbsolutePath() + SEP + srcFile.getName());
        if (!destFile.exists() || srcFile.lastModified() > destFile.lastModified()) {
            FileUtils.copyFile(srcFile, destFile, true);
            Logger.info("Dependency copied to " + destFile.getAbsolutePath());
        }
        return destFile;
    }

    @Override
    public ClasspathKind getClasspathKind() {
        return ClasspathKind.LIB;
    }

    public File getLibraryFile() {
        if (libraryArtifact != null) {
            return new File(libDir.getAbsolutePath() + SEP + libraryArtifact.getFileName());
        } else {
            return null;
        }
    }

    public File getSourceFile() {
        if (sourceArtifact != null) {
            return new File(sourceDir.getAbsolutePath() + SEP + sourceArtifact.getFileName());
        } else {
            return null;
        }
    }

    public File getJavadocFile() {
        if (javadocArtifact != null) {
            return new File(javadocDir.getAbsolutePath() + SEP + javadocArtifact.getFileName());
        } else {
            return null;
        }
    }
}
