package org.seasar.uruma.eclipath.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.seasar.uruma.eclipath.Constants.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.seasar.uruma.eclipath.ClasspathKind;
import org.seasar.uruma.eclipath.ClasspathPolicy;
import org.seasar.uruma.eclipath.EclipseClasspath;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.WorkspaceConfigurator;
import org.seasar.uruma.eclipath.dependency.Dependency;
import org.seasar.uruma.eclipath.exception.ArtifactResolutionRuntimeException;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;
import org.seasar.uruma.eclipath.util.PathUtil;
import org.w3c.dom.Element;

/**
 * @goal sync-classpath
 * @requiresDependencyResolution test
 * @phase process-sources
 */
public class SyncClasspathMojo extends AbstractEclipathMojo {

    @Override
    public void doExecute() throws MojoExecutionException {
        File basedir = project.getBasedir();

        // Load ".classpath" file
        EclipseClasspath eclipseClasspath = new EclipseClasspath(basedir);
        eclipseClasspath.load();

        // Get and filter dependencies
        Set<Artifact> repositoryArtifacts = new TreeSet<Artifact>();
        Set<Artifact> projectArtifacts = new TreeSet<Artifact>();
        if (classpathPolicy == ClasspathPolicy.REPOSITORY) {
            repositoryArtifacts = getArtifacts();
            projectArtifacts = artifactHelper.filterArtifacts(repositoryArtifacts, excludeGroupIds, excludeScopes);
        } else if (classpathPolicy == ClasspathPolicy.PROJECT) {
            projectArtifacts = getArtifacts();
            repositoryArtifacts = artifactHelper.filterArtifacts(projectArtifacts, excludeGroupIds, excludeScopes);
        }

        File targetDirFile = null;
        File sourcesDirFile = null;
        File javadocDirFile = null;
        File providedLibDirFile = null;
        File providedSourcesDirFile = null;
        File providedJavadocDirFile = null;

        List<File> toDeleteFiles = new LinkedList<File>();
        if (projectArtifacts.size() > 0) {
            // Prepare directories
            targetDirFile = prepareDirFile(basedir, libDir);
            sourcesDirFile = prepareDirFile(basedir, libDir + "/" + sourcesDir);
            javadocDirFile = prepareDirFile(basedir, libDir + "/" + javadocDir);
            providedLibDirFile = prepareDirFile(basedir, providedLibDir);
            providedSourcesDirFile = prepareDirFile(basedir, providedLibDir + "/" + sourcesDir);
            providedJavadocDirFile = prepareDirFile(basedir, providedLibDir + "/" + javadocDir);

            // Get existing dependencies
            getExsistingLibraries(toDeleteFiles, targetDirFile);
            getExsistingLibraries(toDeleteFiles, sourcesDirFile);
            getExsistingLibraries(toDeleteFiles, javadocDirFile);
            getExsistingLibraries(toDeleteFiles, providedLibDirFile);
            getExsistingLibraries(toDeleteFiles, providedSourcesDirFile);
            getExsistingLibraries(toDeleteFiles, providedJavadocDirFile);
        }

        // Resolve dependencies
        List<Dependency> repoDependencies = resolveArtifacts(repositoryArtifacts, ClasspathPolicy.REPOSITORY);
        List<Dependency> projDependencies = resolveArtifacts(projectArtifacts, ClasspathPolicy.PROJECT);
        if (!checkDependencies(repoDependencies, projDependencies)) {
            throw new PluginRuntimeException("Required dependencies were not resolved.");
        }

        // Attach repository dependencies
        File m2repo = new File(workspaceConfigurator.getClasspathVariableM2REPO());
        for (Dependency dependency : repoDependencies) {
            String libPath;
            String srcPath = null;
            String javadocPath = null;

            // Create library jar path
            Artifact artifact = dependency.getLibraryArtifact();
            File libFile = artifact.getFile();
            libPath = WorkspaceConfigurator.M2_REPO + "/" + PathUtil.getRelativePath(m2repo, libFile);

            // Create source jar path
            Artifact srcArtifact = dependency.getSourceArtifact();
            if (srcArtifact != null && srcArtifact.isResolved()) {
                File srcFile = srcArtifact.getFile();
                srcPath = WorkspaceConfigurator.M2_REPO + "/" + PathUtil.getRelativePath(m2repo, srcFile);
            }

            // Create javadoc jar path
            Artifact javadocArtifact = dependency.getJavadocArtifact();
            if (javadocArtifact != null && javadocArtifact.isResolved()) {
                File javadocFile = javadocArtifact.getFile();
                javadocPath = "jar:file:/" + PathUtil.normalizePath(javadocFile.getAbsolutePath()) + "!/";
            }

            // Remove existing class path entry
            List<Element> existenceEntries = eclipseClasspath.findClasspathEntry(artifactHelper
                    .getVersionIndependentFileNamePattern(artifact));
            eclipseClasspath.removeClasspathEntries(existenceEntries);

            // Add new class path entry
            eclipseClasspath.addClasspathEntry(ClasspathKind.VAR, libPath, srcPath, javadocPath);
        }

        // Copy and attach project dependencies
        for (Dependency dependency : projDependencies) {
            File libfile;
            File sourceFile = null;
            File javadocFile = null;

            try {
                // Copy dependency to specified directory
                Artifact artifact = dependency.getLibraryArtifact();
                if (artifactHelper.isCompileScope(artifact)) {
                    libfile = copyDependency(artifact, targetDirFile);
                } else {
                    libfile = copyDependency(artifact, providedLibDirFile);
                }
                removeFile(toDeleteFiles, libfile);

                // Copy source to specified directory
                Artifact sourceArtifact = dependency.getSourceArtifact();
                if (sourceArtifact.isResolved()) {
                    if (artifactHelper.isCompileScope(sourceArtifact)) {
                        sourceFile = copyDependency(sourceArtifact, sourcesDirFile);
                    } else {
                        sourceFile = copyDependency(sourceArtifact, providedSourcesDirFile);
                    }
                    removeFile(toDeleteFiles, sourceFile);
                }

                // Copy javadoc to specified directory
                Artifact javadocArtifact = dependency.getJavadocArtifact();
                if (javadocArtifact.isResolved()) {
                    if (artifactHelper.isCompileScope(javadocArtifact)) {
                        javadocFile = copyDependency(javadocArtifact, javadocDirFile);
                    } else {
                        javadocFile = copyDependency(javadocArtifact, providedJavadocDirFile);
                    }
                    removeFile(toDeleteFiles, javadocFile);
                }

                // Acquire copied dependency's relative path
                String path = libfile.getAbsolutePath().substring(basedir.getAbsolutePath().length() + 1)
                        .replace(SEP, "/");

                String srcPath;
                if (artifactHelper.isCompileScope(sourceArtifact)) {
                    srcPath = createSourcePath(sourcesDirFile, sourceFile, sourceArtifact);
                } else {
                    srcPath = createSourcePath(providedSourcesDirFile, sourceFile, sourceArtifact);
                }

                String javadocPath;
                if (artifactHelper.isCompileScope(javadocArtifact)) {
                    javadocPath = createJavadocPath(javadocDirFile, javadocFile, javadocArtifact);
                } else {
                    javadocPath = createJavadocPath(providedJavadocDirFile, javadocFile, javadocArtifact);
                }

                // Remove existing class path entry
                List<Element> existenceEntries = eclipseClasspath.findClasspathEntry(artifactHelper
                        .getVersionIndependentFileNamePattern(artifact));
                eclipseClasspath.removeClasspathEntries(existenceEntries);

                // Add new class path entry
                eclipseClasspath.addClasspathEntry(ClasspathKind.LIB, path, srcPath, javadocPath);
            } catch (IOException ex) {
                Logger.error(ex.getLocalizedMessage(), ex);
            }
        }

        // Delete unnecessary dependencies
        deleteFiles(toDeleteFiles);

        // Output .classpath File
        eclipseClasspath.write();
    }

    protected void logArtifact(Artifact artifact, int indent) {
        if (artifact != null && artifact.isResolved()) {
            Logger.info(StringUtils.repeat(" ", indent) + artifact.toString());
        }
    }

    protected File prepareDirFile(File projectBaseDir, String dirname) {
        String path = projectBaseDir.getAbsolutePath() + SEP + dirname;
        File dirfile = new File(path);
        return dirfile;
    }

    protected File prepareDir(File dirfile) {
        if (!dirfile.exists()) {
            if (dirfile.mkdir()) {
                Logger.info("Directory created. path:" + dirfile.getAbsolutePath());
            } else {
                throw new PluginRuntimeException("Unable to create directory. : " + dirfile.getAbsolutePath());
            }
        }
        return dirfile;
    }

    protected List<File> getExsistingLibraries(List<File> list, File targetDir) {
        FileFilter filter = new SuffixFileFilter(".jar");
        File[] files = targetDir.listFiles(filter);

        if (list == null) {
            list = new LinkedList<File>();
        }
        if (files != null) {
            for (File file : files) {
                list.add(file);
            }
        }
        return list;
    }

    protected void removeFile(List<File> files, File target) {
        int removeIndex = -1;
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getAbsolutePath().equals(target.getAbsolutePath())) {
                removeIndex = i;
                break;
            }
        }

        if (removeIndex > -1) {
            files.remove(removeIndex);
        }
    }

    protected void deleteFiles(List<File> files) {
        for (File file : files) {
            if (file.delete()) {
                Logger.info("File deleted : " + file.getAbsolutePath());
            } else {
                Logger.warn("Failed to delete file : " + file.getAbsolutePath());
            }
        }
    }

    protected File copyDependency(Artifact artifact, File toDir) throws IOException {
        File srcFile = artifact.getFile();
        File destFile = new File(toDir.getAbsolutePath() + SEP + srcFile.getName());
        if (!destFile.exists() || srcFile.lastModified() > destFile.lastModified()) {
            if (!toDir.exists()) {
                prepareDir(toDir);
            }
            FileUtils.copyFile(srcFile, destFile, true);
            Logger.info("Dependency copied to " + destFile.getAbsolutePath());
        }
        return destFile;
    }

    protected String createSourcePath(File sourceDirFile, File sourceFile, Artifact srcArtifact) {
        if (srcArtifact.isResolved()) {
            String path = PathUtil.getRelativePath(eclipseProjectDir, sourceDirFile);
            path = PathUtil.concat(path, sourceFile.getName());
            return path;
        } else {
            return null;
        }
    }

    protected String createJavadocPath(File javadocDirFile, File javadocFile, Artifact javadocArtifact) {
        if (javadocArtifact.isResolved()) {
            String path = PathUtil.getRelativePath(eclipseProjectDir, javadocDirFile);
            String location = "jar:platform:/resource/" + eclipseProjectDir.getName();
            location = PathUtil.concat(location, path);
            location = PathUtil.concat(location, javadocFile.getName());
            location += "!/";
            return location;
        } else {
            return null;
        }
    }

    protected List<Dependency> resolveArtifacts(Set<Artifact> artifacts, ClasspathPolicy classpathPolicy) {
        List<Dependency> dependencies = new ArrayList<Dependency>();

        for (Artifact artifact : artifacts) {
            // Build dependency objects
            Dependency dependency = dependencyFactory.create(artifact);
            dependencies.add(dependency);

            // Get artifact
            if (!artifact.isResolved()) {
                try {
                    artifactHelper.resolve(artifact, true);
                } catch (ArtifactResolutionRuntimeException ex) {
                    Logger.error(ex.getLocalizedMessage(), ex.getCause());
                    continue;
                }
            }

            // Create source artifact
            Artifact srcArtifact = artifactHelper.createSourceArtifact(artifact);
            artifactHelper.resolve(srcArtifact, false);
            dependency.setSourceArtifact(srcArtifact);

            // Create Javadoc artifact
            Artifact javadocArtifact = artifactHelper.createJavadocArtifact(artifact);
            artifactHelper.resolve(javadocArtifact, false);
            dependency.setJavadocArtifact(javadocArtifact);
        }

        return dependencies;
    }

    protected boolean checkDependencies(List<Dependency> repoDependencies, List<Dependency> projDependencies) {
        boolean valid = true;

        Logger.info(Logger.SEPARATOR);
        Logger.info(" Dependency report.  [R]:Resolved [N]:Not resolved");

        if (repoDependencies.size() > 0) {
            Logger.info(Logger.SEPARATOR);
            Logger.info(" REPOSITORY Dependencies");
            Logger.info(Logger.SEPARATOR);
            valid &= doCheckDependencies(repoDependencies);
        }

        if (projDependencies.size() > 0) {
            Logger.info(Logger.SEPARATOR);
            Logger.info(" PROJECT Dependencies");
            Logger.info(Logger.SEPARATOR);
            valid &= doCheckDependencies(projDependencies);
        }
        Logger.info(Logger.SEPARATOR);

        return valid;
    }

    protected boolean doCheckDependencies(List<Dependency> dependencies) {
        boolean valid = true;

        for (Dependency dependency : dependencies) {
            Artifact artifact = dependency.getLibraryArtifact();
            Logger.info(String.format(" %s%s  %s", formatResolveStatus(artifact), formatScope(artifact), artifact));

            Artifact srcArtifact = dependency.getSourceArtifact();
            Logger.info(String.format("  %s    %s", formatResolveStatus(srcArtifact), srcArtifact));

            Artifact javadocArtifact = dependency.getJavadocArtifact();
            Logger.info(String.format("  %s    %s", formatResolveStatus(javadocArtifact), javadocArtifact));
            Logger.info("");
        }

        return valid;
    }

    protected String formatResolveStatus(Artifact artifact) {
        return artifact.isResolved() ? "[R]" : "[N]";
    }

    protected String formatScope(Artifact artifact) {
        String scope = artifact.getScope();
        if (Artifact.SCOPE_COMPILE.equals(scope)) {
            return "[C]";
        } else if (Artifact.SCOPE_PROVIDED.equals(scope)) {
            return "[P]";
        } else if (Artifact.SCOPE_RUNTIME.equals(scope)) {
            return "[R]";
        } else if (Artifact.SCOPE_TEST.equals(scope)) {
            return "[T]";
        } else if (Artifact.SCOPE_SYSTEM.equals(scope)) {
            return "[S]";
        } else if (Artifact.SCOPE_IMPORT.equals(scope)) {
            return "[I]";
        } else {
            return "[?]";
        }
    }
}