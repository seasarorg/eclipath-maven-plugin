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
package org.seasar.uruma.eclipath.model;


/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public abstract class AbstractDependency implements Dependency {
    protected EclipathArtifact libraryArtifact;

    protected EclipathArtifact sourceArtifact;

    protected EclipathArtifact javadocArtifact;

    private ClasspathPolicy classpathPolicy;

    /**
     * Constructs a new {@link AbstractDependency} object with
     * {@link EclipathArtifact} object.
     * 
     * @param artifact
     *        related {@link EclipathArtifact} object
     */
    public AbstractDependency(EclipathArtifact artifact) {
        super();
        this.libraryArtifact = artifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#getLibraryArtifact()
     */
    @Override
    public EclipathArtifact getLibraryArtifact() {
        return libraryArtifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#setLibraryArtifact(org.apache.maven.artifact.Artifact)
     */
    @Override
    public void setLibraryArtifact(EclipathArtifact artifact) {
        this.libraryArtifact = artifact;
    }


    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#getSourceArtifact()
     */
    @Override
    public EclipathArtifact getSourceArtifact() {
        return sourceArtifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#setSourceArtifact(org.apache.maven.artifact.Artifact)
     */
    @Override
    public void setSourceArtifact(EclipathArtifact srcArtifact) {
        this.sourceArtifact = srcArtifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#getJavadocArtifact()
     */
    @Override
    public EclipathArtifact getJavadocArtifact() {
        return javadocArtifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.Dependency#setJavadocArtifact(org.apache.maven.artifact.Artifact)
     */
    @Override
    public void setJavadocArtifact(EclipathArtifact javadocArtifact) {
        this.javadocArtifact = javadocArtifact;
    }

    public ClasspathPolicy getClasspathPolicy() {
        return classpathPolicy;
    }

    public void setClasspathPolicy(ClasspathPolicy classpathPolicy) {
        this.classpathPolicy = classpathPolicy;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(256);
        if (libraryArtifact != null) {
            buf.append(libraryArtifact.toString());
        }
        if (sourceArtifact != null && sourceArtifact.isResolved()) {
            buf.append("\n  ");
            buf.append(sourceArtifact.toString());
        }
        if (javadocArtifact != null && javadocArtifact.isResolved()) {
            buf.append("\n  ");
            buf.append(javadocArtifact.toString());
        }
        buf.append("\n");
        return buf.toString();
    }
}
