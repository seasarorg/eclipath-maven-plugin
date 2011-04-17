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
package org.seasar.uruma.eclipath.model;

import java.io.File;

import org.seasar.uruma.eclipath.WorkspaceConfigurator;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;
import org.seasar.uruma.eclipath.util.PathUtil;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public class M2Dependency extends AbstractDependency {

    private File m2repoFile;

    public M2Dependency(EclipathArtifact artifact) {
        super(artifact);
    }

    public void setM2repo(String m2repo) {
        this.m2repoFile = new File(m2repo);
        if (!m2repoFile.exists() || !m2repoFile.isDirectory()) {
            throw new PluginRuntimeException("Directory not found. : " + m2repoFile.getAbsolutePath());
        }
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#getLibraryPath()
     */
    @Override
    public String getLibraryPath() {
        File libFile = libraryArtifact.getFile();
        String libPath = WorkspaceConfigurator.M2_REPO + "/" + PathUtil.getRelativePath(m2repoFile, libFile);
        return libPath;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#getSourcePath()
     */
    @Override
    public String getSourcePath() {
        if (sourceArtifact != null && sourceArtifact.isResolved()) {
            File srcFile = sourceArtifact.getFile();
            String srcPath = WorkspaceConfigurator.M2_REPO + "/" + PathUtil.getRelativePath(m2repoFile, srcFile);
            return srcPath;
        }
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#getJavadocPath()
     */
    @Override
    public String getJavadocPath() {
        if (javadocArtifact != null && javadocArtifact.isResolved()) {
            File javadocFile = javadocArtifact.getFile();
            String javadocPath = "jar:file:/" + PathUtil.normalizePath(javadocFile.getAbsolutePath()) + "!/";
            return javadocPath;
        }
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#copyLibraryArtifact()
     */
    @Override
    public File copyLibraryArtifact() {
        // Do nothing.
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#copySourceArtifact()
     */
    @Override
    public File copySourceArtifact() {
        // Do nothing.
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#copyJavadocArtifact()
     */
    @Override
    public File copyJavadocArtifact() {
        // Do nothing.
        return null;
    }
}
