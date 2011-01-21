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

import static org.seasar.uruma.maven.plugin.eclipse.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.seasar.uruma.maven.plugin.eclipse.exception.ArtifactResolutionRuntimeException;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 */
public class ArtifactHelper {

    protected static final String NOT_AVAILABLE_SUFFIX = "-not-available";

    protected ArtifactFactory factory;

    protected ArtifactResolver resolver;

    protected List<ArtifactRepository> remoteRepositories;

    protected ArtifactRepository localRepository;

    protected WorkspaceConfigurator workspaceConfigurator;

    protected Logger logger;

    protected boolean forceResolve;

    public Set<Artifact> filterArtifacts(Set<Artifact> artifacts, List<String> excludeGroups, List<String> excludeScopes) {
        Set<Artifact> excluded = new TreeSet<Artifact>();
        List<Artifact> removeArtifacts = new LinkedList<Artifact>();

        for (Artifact artifact : artifacts) {
            boolean matched = false;
            if (excludeGroups != null && excludeGroups.contains(artifact.getGroupId())) {
                matched = true;
            }
            if (excludeScopes != null && excludeScopes.contains(artifact.getScope())) {
                matched = true;
            }

            if (matched) {
                excluded.add(artifact);
                removeArtifacts.add(artifact);
            }
        }
        artifacts.removeAll(removeArtifacts);
        return excluded;
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
        return factory.createArtifactWithClassifier(baseArtifact.getGroupId(), baseArtifact.getArtifactId(),
                baseArtifact.getVersion(), baseArtifact.getType(), classifier);
    }

    public void resolve(Artifact artifact, boolean throwOnError) {
        // Check if jar is not available
        String notAvailablePath = workspaceConfigurator.getClasspathVariableM2REPO() + "/" + createJarPath(artifact)
                + NOT_AVAILABLE_SUFFIX;
        File notAvailableFile = new File(notAvailablePath);
        if (!forceResolve) {
            if (!throwOnError && notAvailableFile.exists()) {
                return;
            }
        } else {
            notAvailableFile.delete();
        }

        try {
            resolver.resolve(artifact, remoteRepositories, localRepository);
        } catch (ArtifactResolutionException ex) {
            try {
                FileUtils.touch(notAvailableFile);
            } catch (IOException ignore) {
            }
            if (throwOnError) {
                throw new ArtifactResolutionRuntimeException(ex.getLocalizedMessage(), ex);
            }
        } catch (ArtifactNotFoundException ex) {
            try {
                FileUtils.touch(notAvailableFile);
            } catch (IOException ignore) {
            }
            if (throwOnError) {
                throw new ArtifactResolutionRuntimeException(ex.getLocalizedMessage(), ex);
            }
        }
    }

    public String createJarPath(Artifact artifact) {
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

    public Pattern getVersionIndependentFileNamePattern(Artifact artifact) {
        StringBuilder regex = new StringBuilder();
        regex.append(".*");
        regex.append(artifact.getArtifactId().replace(".", "\\."));
        regex.append("-.+\\.");
        regex.append(artifact.getType());
        return Pattern.compile(regex.toString());
    }

    public boolean isCompileScope(Artifact artifact) {
        return "compile".equals(artifact.getScope());
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

    public void setWorkspaceConfigurator(WorkspaceConfigurator workspaceConfigurator) {
        this.workspaceConfigurator = workspaceConfigurator;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public boolean isForceResolve() {
        return forceResolve;
    }

    public void setForceResolve(boolean forceResolve) {
        this.forceResolve = forceResolve;
    }
}
