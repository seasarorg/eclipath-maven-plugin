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
package org.seasar.uruma.eclipath.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.seasar.uruma.eclipath.ArtifactHelper;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.PluginInformation;
import org.seasar.uruma.eclipath.WorkspaceConfigurator;
import org.seasar.uruma.eclipath.classpath.CompilerConfiguration;
import org.seasar.uruma.eclipath.classpath.WstProjectFacet;
import org.seasar.uruma.eclipath.exception.ArtifactResolutionRuntimeException;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;
import org.seasar.uruma.eclipath.model.ClasspathPolicy;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.EclipathArtifact;
import org.seasar.uruma.eclipath.model.factory.DependencyFactory;
import org.seasar.uruma.eclipath.model.factory.LibraryLayout;
import org.seasar.uruma.eclipath.model.factory.LibraryLayoutFactory;
import org.seasar.uruma.eclipath.model.factory.ProjectBasedDependencyFactory;
import org.seasar.uruma.eclipath.model.factory.RepositoryBasedDependencyFactory;
import org.seasar.uruma.eclipath.util.ProjectUtil;

/**
 * Abstract Mojo for this plugin.
 *
 * @author y-komori
 */
public abstract class AbstractEclipathMojo extends AbstractMojo {
    /**
     * The POM
     */
    @Component
    protected MavenProject project;

    @Component
    protected RepositorySystem repoSystem;

    @Component
    protected ProjectDependenciesResolver projectDependenciesResolver;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    protected List<RemoteRepository> remoteRepos;

    /**
     * Classpath setting policy. Value must be {@code repository} or
     * {@code project}.
     */
    @Parameter(defaultValue = "project", readonly = true)
    protected String policy;

    /**
     * GroupId list to exclude.
     */
    @Parameter(readonly = true)
    protected List<String> excludeGroupIds;

    /**
     * Scope list to exclude.
     */
    @Parameter(readonly = true)
    protected List<String> excludeScopes;

    /**
     * Library layout.<br />
     * Value must be either {@code flat} or {@code stand-alone} or {@code web}.
     */
    @Parameter(readonly = true)
    protected String layout;

    /**
     * Enables/disables adjusting java version. Defaults to true. This option
     * affects 'JRE System Library' in the Java build path section and 'Java
     * version' in the Project facet section.
     */
    @Parameter(defaultValue = "true", readonly = true)
    protected boolean adjustJavaVersion;

    /**
     * Enables/disables the downloading of source attachments. Defaults to true.
     */
    @Parameter(defaultValue = "true", readonly = true)
    protected boolean downloadSources;

    /**
     * Enables/disables the downloading of javadoc attachments. Defaults to
     * true.
     */
    @Parameter(defaultValue = "true")
    protected boolean downloadJavadocs;

    /**
     * If true, refresh automatically project after executing sync goal.
     */
    @Parameter(defaultValue = "false", readonly = true)
    protected boolean autoRefresh;

    /**
     * ResourceSynchronizer's host name.
     */
    @Parameter(defaultValue = "localhost")
    protected String refreshHost;

    /**
     * ResourceSynchronizer's port number.
     */
    @Parameter(defaultValue = "8386")
    protected int refreshPort;

    protected ClasspathPolicy classpathPolicy;

    protected File eclipseProjectDir;

    protected LibraryLayout libraryLayout;

    protected ArtifactHelper artifactHelper;

    protected DependencyFactory dependencyFactory;

    protected WorkspaceConfigurator workspaceConfigurator;

    protected PluginInformation pluginInformation = new PluginInformation();

    protected CompilerConfiguration compilerConfiguration;

    protected WstProjectFacet wstProjectFacet;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Logger.initialize(getLog());
            checkParameters();
            prepare();
            doExecute();
        } catch (PluginRuntimeException e) {
            Logger.error(getErrorMessage(e), e.getCause());
            throw new MojoExecutionException(e.getMessage(), e.getCause());
        } catch (Throwable t) {
            Logger.error(getErrorMessage(t), t.getCause());
            throw new MojoExecutionException(t.getMessage(), t.getCause());
        }
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    protected void prepare() {
        LocalRepository localRepository = repoSession.getLocalRepository();

        // prepare WorkspaceConfigurator
        workspaceConfigurator = new WorkspaceConfigurator(project);
        workspaceConfigurator.loadConfiguration();
        workspaceConfigurator.setLocalRepositoryDir(localRepository.getBasedir());

        // prepare ArtifactHelper
        artifactHelper = new ArtifactHelper();
        artifactHelper.setRepositorySystem(repoSystem);
        artifactHelper.setRemoteRepositories(project.getRemoteArtifactRepositories());
        // TODO
        // artifactHelper.setLocalRepository(TODO);
        artifactHelper.setWorkspaceConfigurator(workspaceConfigurator);

        // get Eclipse project directory
        eclipseProjectDir = ProjectUtil.getProjectDir(project);

        // load compiler configuration
        compilerConfiguration = CompilerConfiguration.load(project);
        Logger.info(compilerConfiguration.toString());

        // load java project facet (if exists)
        wstProjectFacet = new WstProjectFacet(eclipseProjectDir);
        wstProjectFacet.load();
        Logger.info(wstProjectFacet.toString());

        // prepare DeoendencyFactory
        if (classpathPolicy == ClasspathPolicy.PROJECT) {
            dependencyFactory = new ProjectBasedDependencyFactory(eclipseProjectDir, workspaceConfigurator,
                    libraryLayout);
        } else if (classpathPolicy == ClasspathPolicy.REPOSITORY) {
            dependencyFactory = new RepositoryBasedDependencyFactory(eclipseProjectDir, workspaceConfigurator,
                    libraryLayout);
        }
        dependencyFactory.addExcludeGroupIds(excludeGroupIds);
        dependencyFactory.addExcludeScopes(excludeScopes);
    }

    protected void checkParameters() {
        Logger.info("[Version] " + pluginInformation.getVersion());
        if (ClasspathPolicy.REPOSITORY.confName().equals(policy)) {
            classpathPolicy = ClasspathPolicy.REPOSITORY;
        } else if (ClasspathPolicy.PROJECT.confName().equals(policy)) {
            classpathPolicy = ClasspathPolicy.PROJECT;
        } else {
            throw new PluginRuntimeException("Parameter policy must be \"repository\" or \"project\".");
        }
        Logger.info("[Parameter:policy] " + classpathPolicy.name());

        libraryLayout = LibraryLayoutFactory.getLibraryLayout(layout);
        if (libraryLayout == null) {
            // Create layout from packaging.
            libraryLayout = LibraryLayoutFactory.getLibraryLayoutFromPackaging(project.getPackaging());
        }
        Logger.info("[Parameter:layout] " + libraryLayout.getName());

        if (excludeGroupIds == null) {
            excludeGroupIds = new ArrayList<String>();
        }
        Logger.info("[Parameter:excludeGroupIds] " + excludeGroupIds.toString());

        if (excludeScopes == null) {
            excludeScopes = new ArrayList<String>();
        }
        Logger.info("[Parameter:excludeScopes] " + excludeScopes.toString());
        Logger.info("[Parameter:downloadSources] " + Boolean.toString(downloadSources));
        Logger.info("[Parameter:downloadJavaddocs] " + Boolean.toString(downloadJavadocs));
        Logger.info("[Parameter:autoRefresh] " + Boolean.toString(autoRefresh));
        Logger.info("[Parameter:refreshHost] " + refreshHost);
        Logger.info("[Parameter:refreshPort] " + refreshPort);
    }

    protected Set<Artifact> getArtifacts() {
        return project.getArtifacts();
    }

    protected Set<EclipathArtifact> getEclipathArtifacts() {
        Set<EclipathArtifact> result = new LinkedHashSet<EclipathArtifact>();
        for (Artifact artifact : project.getArtifacts()) {
            result.add(new EclipathArtifact(artifact));
        }
        return result;
    }

    protected List<Dependency> resolveArtifacts(Set<EclipathArtifact> artifacts) {
        return resolveArtifacts(artifacts, false);
    }

    protected List<Dependency> resolveArtifacts(Set<EclipathArtifact> artifacts, boolean forceResolve) {
        List<Dependency> dependencies = new ArrayList<Dependency>(artifacts.size());

        for (EclipathArtifact artifact : artifacts) {
            // Build dependency objects
            Dependency dependency = dependencyFactory.create(artifact);
            dependencies.add(dependency);

            // Get artifact
            if (!artifact.isResolved()) {
                try {
                    artifactHelper.resolve(artifact, true, forceResolve);
                } catch (ArtifactResolutionRuntimeException ex) {
                    Logger.error(ex.getLocalizedMessage(), ex.getCause());
                    continue;
                }
            }

            // Create source artifact
            if (downloadSources) {
                EclipathArtifact srcArtifact = artifactHelper.createSourceArtifact(artifact);
                artifactHelper.resolve(srcArtifact, false, forceResolve);
                dependency.setSourceArtifact(srcArtifact);
            }

            // Create Javadoc artifact
            if (downloadJavadocs) {
                EclipathArtifact javadocArtifact = artifactHelper.createJavadocArtifact(artifact);
                artifactHelper.resolve(javadocArtifact, false, forceResolve);
                dependency.setJavadocArtifact(javadocArtifact);
            }
        }

        return dependencies;
    }

    private String getErrorMessage(Throwable t) {
        String msg = t.getMessage();
        if (msg != null) {
            return msg;
        } else {
            return "";
        }
    }

}
