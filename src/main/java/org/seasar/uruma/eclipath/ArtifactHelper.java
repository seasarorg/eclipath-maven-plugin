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
package org.seasar.uruma.eclipath;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.repository.RepositorySystem;
import org.seasar.uruma.eclipath.exception.ArtifactResolutionRuntimeException;
import org.seasar.uruma.eclipath.model.EclipathArtifact;
import org.seasar.uruma.eclipath.model.Scope;

/**
 * Utility class for resolving artifacts.
 *
 * @author y-komori
 */
public class ArtifactHelper {
    public static final String SOURCES_CLASSIFIER = "sources";

    public static final String JAVADOC_CLASSIFIER = "javadoc";

    protected static final String NOT_AVAILABLE_SUFFIX = "-not-available";

    protected RepositorySystem repositorySystem;

    protected ArtifactRepository localRepository;

    protected List<ArtifactRepository> remoteRepositories;

    protected WorkspaceConfigurator workspaceConfigurator;

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

    public EclipathArtifact createSourceArtifact(EclipathArtifact baseArtifact) {
        return createArtifactWithClassifier(baseArtifact, SOURCES_CLASSIFIER);
    }

    public EclipathArtifact createJavadocArtifact(EclipathArtifact baseArtifact) {
        return createArtifactWithClassifier(baseArtifact, JAVADOC_CLASSIFIER);
    }

    private EclipathArtifact createArtifactWithClassifier(EclipathArtifact baseArtifact, String classifier) {
        String baseClassifier = baseArtifact.classifier();
        if (baseClassifier != null) {
            classifier = baseClassifier + "-" + classifier;
        }

        String scope = baseArtifact.scope() != null ? baseArtifact.scope().name() : null;
        Artifact artifact = new DefaultArtifact(baseArtifact.groupId(), baseArtifact.artifactId(),
                baseArtifact.version(), scope, baseArtifact.type(), classifier, baseArtifact.getArtifactHandler());

        return new EclipathArtifact(artifact);
    }

    public void resolve(EclipathArtifact artifact, boolean throwOnError) {
        resolve(artifact, throwOnError, false);
    }

    public void resolve(EclipathArtifact artifact, boolean throwOnError, boolean forceResolve) {
        // TODO Maven3 では必要?
        // Check if jar is not available
        File notAvailableFile = null;
        if (workspaceConfigurator.isConfigured()) {
            String notAvailablePath = workspaceConfigurator.getClasspathVariableM2REPO() + "/"
                    + artifact.getRepositoryPath() + NOT_AVAILABLE_SUFFIX;
            notAvailableFile = new File(notAvailablePath);
            if (!forceResolve) {
                if (!throwOnError && notAvailableFile.exists()) {
                    return;
                }
            } else {
                notAvailableFile.delete();
            }
        }

        // prepare artifact resolution request
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(artifact.getArtifact());
        request.setLocalRepository(localRepository);
        request.setRemoteRepositories(remoteRepositories);
        // TODO 有効性確認
        request.setForceUpdate(forceResolve);

        // do resolve
        ArtifactResolutionResult result = repositorySystem.resolve(request);
        if (result.isSuccess()) {
            for (Artifact resolvedSrcArtifact : result.getArtifacts()) {
                Logger.info("  resolved: " + resolvedSrcArtifact.toString());
            }
        } else {
            try {
                if (result.hasMissingArtifacts() && notAvailableFile != null) {
                    FileUtils.touch(notAvailableFile);
                }
            } catch (IOException ignore) {
            }

            if (throwOnError) {
                throw new ArtifactResolutionRuntimeException("artifact resolution failed.", result);
            } else {
                Logger.warn("artifact resolution failed. : " + artifact.toString());
            }
        }
    }

    public boolean isCompileScope(Artifact artifact) {
        return Scope.COMPILE.equalsString(artifact.getScope());
    }

    public RepositorySystem getRepositorySystem() {
        return repositorySystem;
    }

    public void setRepositorySystem(RepositorySystem repositorySystem) {
        this.repositorySystem = repositorySystem;
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
}
