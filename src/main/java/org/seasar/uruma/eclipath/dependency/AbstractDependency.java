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
package org.seasar.uruma.eclipath.dependency;

import org.apache.maven.artifact.Artifact;
import org.seasar.uruma.eclipath.ClasspathPolicy;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 */
public abstract class AbstractDependency implements Dependency {
    private Artifact artifact;

    private Artifact srcArtifact;

    private Artifact javadocArtifact;

    private ClasspathPolicy classpathPolicy;

    /**
     * Constructs a new {@link AbstractDependency} object with {@link Artifact}
     * object.
     * 
     * @param artifact
     *        related {@link Artifact} object
     */
    public AbstractDependency(Artifact artifact) {
        super();
        this.artifact = artifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getArtifact()
     */
    @Override
    public Artifact getArtifact() {
        return artifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#setArtifact(org.apache.maven.artifact.Artifact)
     */
    @Override
    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getSrcArtifact()
     */
    @Override
    public Artifact getSrcArtifact() {
        return srcArtifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#setSrcArtifact(org.apache.maven.artifact.Artifact)
     */
    @Override
    public void setSrcArtifact(Artifact srcArtifact) {
        this.srcArtifact = srcArtifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#getJavadocArtifact()
     */
    @Override
    public Artifact getJavadocArtifact() {
        return javadocArtifact;
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.Dependency#setJavadocArtifact(org.apache.maven.artifact.Artifact)
     */
    @Override
    public void setJavadocArtifact(Artifact javadocArtifact) {
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
        if (artifact != null) {
            buf.append(artifact.toString());
        }
        if (srcArtifact != null && srcArtifact.isResolved()) {
            buf.append("\n  ");
            buf.append(srcArtifact.toString());
        }
        if (javadocArtifact != null && javadocArtifact.isResolved()) {
            buf.append("\n  ");
            buf.append(javadocArtifact.toString());
        }
        buf.append("\n");
        return buf.toString();
    }
}
