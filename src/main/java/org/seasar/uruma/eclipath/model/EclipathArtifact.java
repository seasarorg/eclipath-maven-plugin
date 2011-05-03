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

import static org.seasar.uruma.eclipath.Constants.*;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.seasar.uruma.eclipath.util.AssertionUtil;

/**
 * A wrapper of {@link Artifact}
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 */
public class EclipathArtifact {
    private final Artifact artifact;

    public EclipathArtifact(Artifact artifact) {
        AssertionUtil.assertNotNull("artifact", artifact);
        this.artifact = artifact;
    }

    public String groupId() {
        return artifact.getGroupId();
    }

    public String artifactId() {
        return artifact.getArtifactId();
    }

    public String version() {
        return artifact.getVersion();
    }

    public Scope scope() {
        return Scope.getScope(artifact.getScope());
    }

    public String classifier() {
        return artifact.getClassifier();
    }

    public String type() {
        return artifact.getType();
    }

    public boolean isResolved() {
        return artifact.isResolved();
    }

    public File getFile() {
        return artifact.getFile();
    }

    public String getRepositoryPath() {
        StringBuilder path = new StringBuilder();
        String groupId = artifact.getGroupId();
        if (groupId != null) {
            path.append(groupId.replace(".", SEP));
            path.append(SEP);
        }

        String artifactId = artifact.getArtifactId();
        path.append(artifactId);
        path.append(SEP);

        String version = artifact.getVersion();
        if (version != null) {
            path.append(version);
            path.append(SEP);
        }

        path.append(artifactId);
        if (version != null) {
            path.append("-");
            path.append(version);
        }

        String classifier = artifact.getClassifier();
        if (classifier != null) {
            path.append("-");
            path.append(classifier);
        }

        path.append(".");
        path.append(artifact.getType());
        return path.toString();
    }

    public String getFileName() {
        StringBuilder buf = new StringBuilder(64);
        buf.append(artifact.getArtifactId());

        String version = artifact.getVersion();
        if (!StringUtils.isEmpty(version)) {
            buf.append("-");
            buf.append(version);
        }

        String classifier = artifact.getClassifier();
        if (!StringUtils.isEmpty(classifier)) {
            buf.append("-");
            buf.append(classifier);
        }

        buf.append(".");
        buf.append(artifact.getType());
        return buf.toString();
    }

    public Pattern getVersionIndependentFileNamePattern() {
        StringBuilder regex = new StringBuilder();
        regex.append(".*/");
        regex.append(artifact.getArtifactId().replace(".", "\\."));
        regex.append("-[0-9]+\\..+\\.");
        regex.append(artifact.getType());
        return Pattern.compile(regex.toString());
    }

    public Artifact getArtifact() {
        return artifact;
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return artifact.hashCode();
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EclipathArtifact other = (EclipathArtifact) obj;
        if (artifact == null) {
            if (other.artifact != null)
                return false;
        } else if (!artifact.equals(other.artifact))
            return false;
        return true;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return artifact.toString();
    }
}
