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
package org.seasar.uruma.eclipath.dependency;

import static org.seasar.uruma.eclipath.Constants.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.util.PathUtil;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public class FileDependency extends AbstractDependency {
    public static final String SOURCES_PREFIX = "sources";

    public static final String JAVADOC_PREFIX = "javadoc";

    private final File libDir;

    private final File sourceDir;

    private final File javadocDir;

    public FileDependency(EclipathArtifact artifact, File projectDir, String libDir) {
        super(artifact);
        String path = PathUtil.normalizePath(projectDir.getAbsolutePath()) + "/" + libDir;
        this.libDir = new File(path);
        this.sourceDir = new File(path + "/" + SOURCES_PREFIX);
        this.javadocDir = new File(path + "/" + JAVADOC_PREFIX);
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getLibraryPath()
     */
    @Override
    public String getLibraryPath() {
        return libDir + "/" + libraryArtifact.getFileName();
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getSourcePath()
     */
    @Override
    public String getSourcePath() {
        return libDir + "/" + SOURCES_PREFIX + "/" + sourceArtifact.getFileName();
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getJavadocPath()
     */
    @Override
    public String getJavadocPath() {
        return libDir + "/" + JAVADOC_PREFIX + "/" + javadocArtifact.getFileName();
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#copyLibraryArtifact()
     */
    @Override
    public File copyLibraryArtifact() throws IOException {
        return copyDependency(libraryArtifact, libDir);
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#copySourceArtifact()
     */
    @Override
    public File copySourceArtifact() throws IOException {
        return copyDependency(sourceArtifact, sourceDir);
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#copyJavadocArtifact()
     */
    @Override
    public File copyJavadocArtifact() throws IOException {
        return copyDependency(javadocArtifact, javadocDir);
    }

    private File copyDependency(EclipathArtifact artifact, File toDir) throws IOException {
        File srcFile = artifact.getFile();
        File destFile = new File(toDir.getAbsolutePath() + SEP + srcFile.getName());
        if (!destFile.exists() || srcFile.lastModified() > destFile.lastModified()) {
            FileUtils.copyFile(srcFile, destFile, true);
            Logger.info("Dependency copied to " + destFile.getAbsolutePath());
        }
        return destFile;
    }
}
