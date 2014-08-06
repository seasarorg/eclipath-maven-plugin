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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.ProjectRefresher;
import org.seasar.uruma.eclipath.PropertiesFile;
import org.seasar.uruma.eclipath.classpath.ClasspathEntry;
import org.seasar.uruma.eclipath.classpath.EclipseClasspath;
import org.seasar.uruma.eclipath.model.CompilerConfiguration;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.EclipathArtifact;
import org.seasar.uruma.eclipath.util.ProjectUtil;
import org.w3c.dom.Element;

/**
 *
 * @author y-komori
 */
@Mojo(name = "sync", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.TEST)
public class SyncMojo extends AbstractEclipathMojo {

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        // Load ".classpath" file
        EclipseClasspath eclipseClasspath = new EclipseClasspath(eclipseProjectDir);
        eclipseClasspath.load();

        // TODO パラメーターで有効/無効にする
        // Add JRE container
        eclipseClasspath.addJavaContainerClasspathEntry(compilerConfiguration);

        // Get dependencies
        Set<EclipathArtifact> dependingArtifacts = getEclipathArtifacts();
        List<Dependency> dependencies = resolveArtifacts(dependingArtifacts);
        for (Dependency dependency : dependencies) {
            try {
                // Copy artifacts
                dependency.copyLibraryArtifact();
                if (downloadSources) {
                    dependency.copySourceArtifact();
                }
                if (downloadJavadocs) {
                    dependency.copyJavadocArtifact();
                }

                // Remove old version libraries (if exists)
                removeDuplicatedClasspathEntry(eclipseClasspath, dependency);

                // Add to classpath
                String libraryPath = dependency.getLibraryPath();
                String sourcePath = dependency.getSourcePath();
                String javadocPath = dependency.getJavadocPath();
                if (eclipseClasspath.findClasspathEntry(libraryPath) == null) {
                    eclipseClasspath.addClasspathEntry(dependency.getClasspathKind(), libraryPath, sourcePath,
                            javadocPath);
                }
            } catch (IOException ex) {
                Logger.warn("Failed to copy artifact. : ", ex);
            }
        }

        // Remove classpathentries which doesn't exist in pom.xml
        Map<String, ClasspathEntry> entryMap = new HashMap<String, ClasspathEntry>();
        for (ClasspathEntry entry : eclipseClasspath.getAllClasspathEntries()) {
            entryMap.put(entry.getPath(), entry);
        }
        for (Dependency dependency : dependencies) {
            String libraryPath = dependency.getLibraryPath();
            if (entryMap.containsKey(libraryPath)) {
                entryMap.remove(libraryPath);
            }
        }
        for (ClasspathEntry entry : entryMap.values()) {
            eclipseClasspath.removeClasspathEntry(entry);
        }
        eclipseClasspath.removeClasspathEntries(entryMap.values());

        // Write ".classpath" file
        eclipseClasspath.write();

        // Adjust .settings/org.eclipse.jdt.core.prefs
        adjustJdtPrefs(compilerConfiguration);

        // Refresh project
        if (autoRefresh) {
            ProjectRefresher refresher = new ProjectRefresher();
            refresher.refresh(project, refreshHost, refreshPort);
        }
    }

    protected void removeDuplicatedClasspathEntry(EclipseClasspath eclipseClasspath, Dependency dependency) {
        Pattern pattern = dependency.getLibraryArtifact().getVersionIndependentFileNamePattern();
        List<Element> oldVersionEntries = eclipseClasspath.findClasspathEntry(pattern);
        ClasspathEntry newEntry = createClasspathEntry(dependency);
        for (Element entry : oldVersionEntries) {
            ClasspathEntry existingEntry = new ClasspathEntry(entry);
            if (!newEntry.equals(existingEntry)) {
                eclipseClasspath.removeClasspathEntryElement(entry);
            }
        }
    }

    protected ClasspathEntry createClasspathEntry(Dependency dependency) {
        ClasspathEntry entry = new ClasspathEntry();
        entry.setClasspathKind(dependency.getClasspathKind());
        entry.setPath(dependency.getLibraryPath());
        entry.setSourcePath(dependency.getSourcePath());
        entry.setJavadocLocation(dependency.getJavadocPath());
        return entry;
    }

    protected void adjustJdtPrefs(CompilerConfiguration conf) {
        File jdtPrefsFile = ProjectUtil.getJdtPrefsFile(project);
        if (!jdtPrefsFile.exists()) {
            return;
        }
        PropertiesFile jdtPrefs = new PropertiesFile(jdtPrefsFile);
        jdtPrefs.load();

        jdtPrefs.put("org.eclipse.jdt.core.compiler.compliance", conf.getTargetVersion());
        jdtPrefs.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", conf.getTargetVersion());
        jdtPrefs.put("org.eclipse.jdt.core.compiler.source", conf.getSourceVersion());
        jdtPrefs.store();
    }

    // protected void doExecute1() throws MojoExecutionException,
    // MojoFailureException {
    // Logger.info("===== Sync mojo executed. =====");
    // Logger.info(project.toString());
    // Logger.info(repoSystem.getClass().getName());
    // // resolveTest("org.apache.httpcomponents:httpclient:4.3.3");
    //
    // describeProject(project);
    // resolveProject(project, repoSession);
    // }
    //
    // protected void describeProject(MavenProject targetProject) {
    // // Artifact(pom.xmlのartifact)
    // Artifact artifact = targetProject.getArtifact();
    // Logger.info("artifact :" + artifact);
    //
    // // ArtifactMap(???)
    // Map<String, Artifact> artifactMap = targetProject.getArtifactMap();
    // for (Entry<String, Artifact> e : artifactMap.entrySet()) {
    // Logger.info(e.getKey() + " : " + e.getValue());
    // }
    //
    // // Dependencies(直接の依存を取得)
    // List<Dependency> dependencies = targetProject.getDependencies();
    // for (Dependency dependency : dependencies) {
    // Logger.info("dependency : " + dependency);
    // }
    // }
    //
    // protected void resolveProject(MavenProject targetProject,
    // RepositorySystemSession session) {
    // DefaultDependencyResolutionRequest request = new
    // DefaultDependencyResolutionRequest(targetProject, session);
    // DependencyResolutionResult result;
    //
    // try {
    // Logger.info(projectDependenciesResolver.getClass().getName());
    // result = projectDependenciesResolver.resolve(request);
    //
    // Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
    // if (result.getDependencyGraph() != null &&
    // !result.getDependencyGraph().getChildren().isEmpty()) {
    // RepositoryUtils.toArtifacts(artifacts,
    // result.getDependencyGraph().getChildren(),
    // Collections.singletonList(project.getArtifact().getId()), null);
    // }
    //
    // // 依存解決されたすべてのArtifactが得られる
    // for (Artifact artifact : artifacts) {
    // Logger.info(String.format("resolved: %s (%s)", artifact,
    // artifact.getClass().getName()));
    // resolveSource(project, artifact);
    // }
    // } catch (DependencyResolutionException e) {
    // Logger.error(e.getMessage(), e);
    // }
    // }
    //
    // protected void resolveSource(MavenProject project, Artifact artifact) {
    // // new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
    // // artifact.getClassifier(), null, artifact.getVersion());
    // Artifact srcArtifact = new
    // org.apache.maven.artifact.DefaultArtifact(artifact.getGroupId(),
    // artifact.getArtifactId(), artifact.getVersion(), artifact.getScope(),
    // "jar",
    // SOURCES_CLASSIFIER, artifact.getArtifactHandler());
    // Logger.info("resolving srcArtifact : " + srcArtifact.toString() +
    // " orgtype : " + artifact.getType());
    //
    // ArtifactResolutionRequest request = new ArtifactResolutionRequest();
    // request.setArtifact(srcArtifact);
    // request.setRemoteRepositories(project.getRemoteArtifactRepositories());
    // request.setForceUpdate(true);
    //
    // ArtifactResolutionResult result = repoSystem.resolve(request);
    // if (result.isSuccess()) {
    // for (Artifact resolvedSrcArtifact : result.getArtifacts()) {
    // Logger.info("  resolved src : " + resolvedSrcArtifact.toString() + "("
    // + resolvedSrcArtifact.getFile().getAbsolutePath() + ")");
    // }
    // } else if (result.hasMissingArtifacts()) {
    // Logger.warn("  missing : " + srcArtifact.toString());
    // } else {
    // Logger.error("  hasCircularDependencyExceptions : " +
    // result.hasCircularDependencyExceptions());
    // Logger.error("  hasErrorArtifactExceptions      : " +
    // result.hasErrorArtifactExceptions());
    // Logger.error("  hasExceptions                   : " +
    // result.hasExceptions());
    // Logger.error("  hasMetadataResolutionExceptions : " +
    // result.hasMetadataResolutionExceptions());
    // Logger.error("  hasMissingArtifacts             : " +
    // result.hasMissingArtifacts());
    // Logger.error("  hasVersionRangeViolations       : " +
    // result.hasVersionRangeViolations());
    // }
    // }
    //
    // protected void resolveTest(String artifactCoords) throws
    // MojoFailureException, MojoExecutionException {
    // // Artifact artifact;
    // // try {
    // // artifact = new DefaultArtifact(artifactCoords);
    // // } catch (IllegalArgumentException e) {
    // // throw new MojoFailureException(e.getMessage());
    // // }
    // Artifact artifact = repoSystem.createArtifact("jp.littleforest",
    // "lf-utils", "2.0", "jar");
    //
    // ArtifactResolutionRequest request = new ArtifactResolutionRequest();
    // // Artifact artifact =
    // // repoSystem.createArtifact("org.apache.httpcomponents", "httpclient",
    // // "4.3.3", "jar");
    // request.setArtifact(artifact);
    // request.setRemoteRepositories(project.getRemoteArtifactRepositories());
    // // request.setResolveTransitively(true);
    //
    // // ArtifactRequest request = new ArtifactRequest();
    // // request.setArtifact(artifact);
    // // request.setRepositories(remoteRepos);
    //
    // getLog().info("Resolving artifact " + artifact + " from " +
    // project.getRemoteProjectRepositories());
    //
    // // result = repoSystem.resolveArtifact(repoSession, request);
    // ArtifactResolutionResult result = repoSystem.resolve(request);
    // Set<Artifact> artifacts = result.getArtifacts();
    // for (Artifact a : artifacts) {
    // getLog().info(
    // "Resolved artifact " + artifact + " to " + a.getFile().getAbsolutePath()
    // + " from "
    // + result.getRepositories());
    // }
    // }
}
