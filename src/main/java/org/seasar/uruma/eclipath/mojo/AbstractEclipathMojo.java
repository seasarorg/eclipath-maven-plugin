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
package org.seasar.uruma.eclipath.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.seasar.uruma.eclipath.ArtifactHelper;
import org.seasar.uruma.eclipath.ClasspathPolicy;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.PluginInformation;
import org.seasar.uruma.eclipath.WorkspaceConfigurator;
import org.seasar.uruma.eclipath.dependency.factory.AbstractDependencyFactory;
import org.seasar.uruma.eclipath.dependency.factory.AbstractDependencyFactory.LibraryLayout;
import org.seasar.uruma.eclipath.dependency.factory.DependencyFactory;
import org.seasar.uruma.eclipath.dependency.factory.ProjectBasedDependencyFactory;
import org.seasar.uruma.eclipath.dependency.factory.RepositoryBasedDependencyFactory;
import org.seasar.uruma.eclipath.util.ProjectUtil;

/**
 * Abstract Mojo for this plugin.
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractEclipathMojo extends AbstractMojo {
    /**
     * POM
     * 
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * Local maven repository.
     * 
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * Remote repositories which will be searched for source attachments.
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    protected List<ArtifactRepository> remoteArtifactRepositories;

    /**
     * Artifact factory, needed to download source jars for inclusion in
     * classpath.
     * 
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory;

    /**
     * Artifact resolver, needed to download source jars for inclusion in
     * classpath.
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @required
     * @readonly
     */
    protected ArtifactResolver artifactResolver;

    /**
     * Classpath setting policy.<br />
     * Value must be {@code repository} or {@code project}.
     * 
     * @parameter default-value="repository"
     */
    protected String policy;

    /**
     * GroupId list to exclude.
     * 
     * @parameter
     */
    protected List<String> excludeGroupIds;

    /**
     * Scope list to exclude.
     * 
     * @parameter
     */
    protected List<String> excludeScopes;

    /**
     * Library directory.
     * 
     * @parameter default-value="lib"
     */
    protected String libDir;

    /**
     * Provided library directory.
     * 
     * @parameter default-value="lib"
     */
    protected String providedLibDir;

    /**
     * Sources destination directory.
     * 
     * @parameter default-value="sources"
     */
    protected String sourcesDir;

    /**
     * Javadoc destination directory.
     * 
     * @parameter default-value="javadoc"
     */
    protected String javadocDir;

    /**
     * If {@code true}, always try to resolve all sources and javadoc
     * dependencies.
     * 
     * @parameter expression="${forceResolve}" default-value="false"
     */
    protected boolean forceResolve;

    protected ClasspathPolicy classpathPolicy;

    protected File eclipseProjectDir;

    protected ArtifactHelper artifactHelper;

    protected DependencyFactory dependencyFactory;

    protected WorkspaceConfigurator workspaceConfigurator;

    protected PluginInformation pluginInformation = new PluginInformation();

    /*
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Logger.initialize(getLog());

        if (!checkParameters()) {
            return;
        }

        prepare();

        doExecute();
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    protected void prepare() {
        workspaceConfigurator = new WorkspaceConfigurator(project);
        workspaceConfigurator.loadConfiguration();

        artifactHelper = new ArtifactHelper();
        artifactHelper.setFactory(artifactFactory);
        artifactHelper.setResolver(artifactResolver);
        artifactHelper.setRemoteRepositories(remoteArtifactRepositories);
        artifactHelper.setLocalRepository(localRepository);
        artifactHelper.setWorkspaceConfigurator(workspaceConfigurator);
        artifactHelper.setForceResolve(forceResolve);

        eclipseProjectDir = ProjectUtil.getProjectDir(project);

        // TODO
        LibraryLayout layout = new AbstractDependencyFactory.FlatLayout();
        if (classpathPolicy == ClasspathPolicy.PROJECT) {
            dependencyFactory = new ProjectBasedDependencyFactory(eclipseProjectDir, layout);
        } else if (classpathPolicy == ClasspathPolicy.REPOSITORY) {
            dependencyFactory = new RepositoryBasedDependencyFactory(eclipseProjectDir, layout);
        }
    }

    protected boolean checkParameters() {
        Logger.info("[Version] " + pluginInformation.getVersion());
        if (ClasspathPolicy.REPOSITORY.confName().equals(policy)) {
            classpathPolicy = ClasspathPolicy.REPOSITORY;
        } else if (ClasspathPolicy.PROJECT.confName().equals(policy)) {
            classpathPolicy = ClasspathPolicy.PROJECT;
        } else {
            Logger.error("Parameter policy must be \"repository\" or \"project\".");
            return false;
        }
        Logger.info("[Parameter:policy] " + classpathPolicy.name());

        if (StringUtils.isEmpty(libDir)) {
            Logger.error("Parameter destdir is not specified.");
            return false;
        } else {
            Logger.info("[Parameter:libDir] " + libDir);
        }

        if (StringUtils.isEmpty(providedLibDir)) {
            Logger.error("Parameter providedLibDir is not specified.");
            return false;
        } else {
            Logger.info("[Parameter:providedLibDir] " + providedLibDir);
        }

        if (excludeGroupIds == null) {
            excludeGroupIds = new ArrayList<String>();
        }
        Logger.info("[Parameter:excludeGroupIds] " + excludeGroupIds.toString());

        if (excludeScopes == null) {
            excludeScopes = new ArrayList<String>();
        }
        Logger.info("[Parameter:excludeScopes] " + excludeScopes.toString());

        Logger.info("[Parameter:forceResolve] " + forceResolve);
        return true;
    }

    @SuppressWarnings("unchecked")
    protected Set<Artifact> getArtifacts() {
        return project.getArtifacts();
    }

    public void setLibDir(String libDir) {
        this.libDir = libDir;
    }

    public void setExcludeGroups(List<String> excludeGroups) {
        this.excludeGroupIds = excludeGroups;
    }

}