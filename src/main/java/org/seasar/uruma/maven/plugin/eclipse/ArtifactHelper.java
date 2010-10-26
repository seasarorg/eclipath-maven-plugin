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
package org.seasar.uruma.maven.plugin.eclipse;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 */
public class ArtifactHelper {
    protected ArtifactFactory factory;

    protected ArtifactResolver resolver;

    protected List<ArtifactRepository> remoteRepositories;

    protected ArtifactRepository localRepository;

    protected Logger logger;

    public Set<Artifact> filterArtifacts(Set<Artifact> artifacts, List<String> excludeGroups,
            String scope) {
        Set<Artifact> result = new TreeSet<Artifact>();

        for (Artifact artifact : artifacts) {
            if (excludeGroups == null) {
                result.add(artifact);
            } else if (!excludeGroups.contains(artifact.getGroupId())) {
                if (scope == null) {
                    result.add(artifact);
                } else if (scope.equals(artifact.getScope())) {
                    result.add(artifact);
                }
            }
        }
        return result;
    }

    public Artifact createSourceArtifact(Artifact baseArtifact) {
        return createArtifactWithClassifier(baseArtifact, "sources");
    }

    public Artifact createJavadocArtifact(Artifact baseArtifact) {
        return createArtifactWithClassifier(baseArtifact, "javadoc");
    }

    public String getFilename(Artifact artifact) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(artifact.getArtifactId());
        buf.append("-");
        buf.append(artifact.getVersion());
        if (artifact.hasClassifier()) {
            buf.append("-");
            buf.append(artifact.getClassifier());
        }
        buf.append(".");
        buf.append(artifact.getType());
        return buf.toString();
    }

    private Artifact createArtifactWithClassifier(Artifact baseArtifact, String classifier) {
        String baseClassifier = baseArtifact.getClassifier();
        if (baseClassifier != null) {
            classifier = baseClassifier + "-" + classifier;
        }
        return factory.createArtifactWithClassifier(baseArtifact.getGroupId(), baseArtifact
                .getArtifactId(), baseArtifact.getVersion(), baseArtifact.getType(), classifier);
    }

    public void resolve(Artifact artifact, boolean logError) {
        try {
            resolver.resolve(artifact, remoteRepositories, localRepository);
            logger.info("Artifact resolved : " + artifact);
        } catch (Exception ex) {
            if (logError) {
                logger.warn(ex.getLocalizedMessage());
            }
        }
    }

    public void setFactory(ArtifactFactory factory) {
        this.factory = factory;
    }

    public void setResolver(ArtifactResolver resolver) {
        this.resolver = resolver;
    }

    public void setRemoteRepositories(List<ArtifactRepository> remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    public void setLocalRepository(ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
