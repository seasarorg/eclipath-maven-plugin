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

import java.io.File;

import org.seasar.uruma.eclipath.util.PathUtil;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public class FileDependency extends AbstractDependency {
    private final File libDir;

    private final File sourceDir;

    private final File javadocDir;

    public FileDependency(EclipathArtifact artifact, File libDir) {
        super(artifact);
        String path = PathUtil.normalizePath(libDir.getAbsolutePath());
        this.libDir = new File(path);
        this.sourceDir = new File(path + "/source");
        this.javadocDir = new File(path + "/javadoc");
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getLibraryPath()
     */
    @Override
    public String getLibraryPath() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getSourcePath()
     */
    @Override
    public String getSourcePath() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getJavadocPath()
     */
    @Override
    public String getJavadocPath() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }


    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#copyLibraryArtifact()
     */
    @Override
    public File copyLibraryArtifact() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#copySourceArtifact()
     */
    @Override
    public File copySourceArtifact() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#copyJavadocArtifact()
     */
    @Override
    public File copyJavadocArtifact() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }
}
