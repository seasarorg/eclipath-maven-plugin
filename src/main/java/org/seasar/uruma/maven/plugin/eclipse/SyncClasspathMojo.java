package org.seasar.uruma.maven.plugin.eclipse;

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

import static org.seasar.uruma.maven.plugin.eclipse.Constants.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.seasar.uruma.maven.plugin.eclipse.exception.ArtifactResolutionRuntimeException;
import org.seasar.uruma.maven.plugin.eclipse.exception.PluginRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @goal sync-classpath
 * @requiresDependencyResolution test
 * @phase process-sources
 */
public class SyncClasspathMojo extends AbstractMojo {
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
     * Destination directory.
     * 
     * @parameter default-value="lib"
     */
    protected String destdir;

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

    protected WorkspaceConfigurator workspaceConfigurator;

    protected Logger logger;

    protected PluginInformation pluginInformation = new PluginInformation();

    public void execute() throws MojoExecutionException {
        prepare();

        EclipseClasspath eclipseClasspath = new EclipseClasspath();
        eclipseClasspath.setLogger(logger);

        if (!checkParameters()) {
            return;
        }

        File basedir = project.getBasedir();

        File dotClassPathFile = eclipseClasspath.createDotClassPathFile(project.getBasedir());
        Document document = eclipseClasspath.loadDotClassPath(dotClassPathFile);
        Element root = document.getDocumentElement();

        // Get and filter dependencies
        Set<Artifact> repositoryArtifacts = new TreeSet<Artifact>();
        Set<Artifact> projectArtifacts = new TreeSet<Artifact>();
        if (classpathPolicy == ClasspathPolicy.REPOSITORY) {
            repositoryArtifacts = getArtifacts();
            projectArtifacts = artifactHelper.filterArtifacts(repositoryArtifacts, excludeGroupIds,
                    excludeScopes);
        } else if (classpathPolicy == ClasspathPolicy.PROJECT) {
            projectArtifacts = getArtifacts();
            repositoryArtifacts = artifactHelper.filterArtifacts(projectArtifacts, excludeGroupIds,
                    excludeScopes);
        }

        File targetDirFile = null;
        File sourcesDirFile = null;
        File javadocDirFile = null;
        // TODO Provided用を作成
        List<File> toDeleteFiles = new LinkedList<File>();
        if (projectArtifacts.size() > 0) {
            // Prepare directories
            targetDirFile = prepareDir(basedir, destdir);
            sourcesDirFile = prepareDir(basedir, destdir + "/" + sourcesDir);
            javadocDirFile = prepareDir(basedir, destdir + "/" + javadocDir);
            // TODO Provided用を作成

            // Get existing dependencies
            getExsistingLibraries(toDeleteFiles, targetDirFile);
            getExsistingLibraries(toDeleteFiles, sourcesDirFile);
            getExsistingLibraries(toDeleteFiles, javadocDirFile);
            // TODO Provided用を作成
        }

        // Resolve dependencies
        List<Dependency> repoDependencies = resolveArtifacts(repositoryArtifacts,
                ClasspathPolicy.REPOSITORY);
        List<Dependency> projDependencies = resolveArtifacts(projectArtifacts,
                ClasspathPolicy.PROJECT);
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
            Artifact artifact = dependency.getArtifact();
            File libFile = artifact.getFile();
            libPath = WorkspaceConfigurator.M2_REPO + "/"
                    + PathUtil.getRelativePath(m2repo, libFile);

            // Create source jar path
            Artifact srcArtifact = dependency.getSrcArtifact();
            if (srcArtifact != null && srcArtifact.isResolved()) {
                File srcFile = srcArtifact.getFile();
                srcPath = WorkspaceConfigurator.M2_REPO + "/"
                        + PathUtil.getRelativePath(m2repo, srcFile);
            }

            // Create javadoc jar path
            Artifact javadocArtifact = dependency.getJavadocArtifact();
            if (javadocArtifact != null && javadocArtifact.isResolved()) {
                File javadocFile = javadocArtifact.getFile();
                javadocPath = "jar:file:/" + PathUtil.normalizePath(javadocFile.getAbsolutePath())
                        + "!/";
            }

            // Remove existing class path entry
            List<Element> existenceEntries = eclipseClasspath.findClasspathEntry(artifactHelper
                    .getVersionIndependentFileNamePattern(artifact));
            eclipseClasspath.removeClasspathEntries(existenceEntries);

            // Add new class path entry
            Element classpathEntry = eclipseClasspath.createClasspathEntry(libPath, srcPath,
                    KIND_VAR);
            root.appendChild(classpathEntry);
            if (javadocPath != null) {
                eclipseClasspath.addAttribute(classpathEntry, ATTRNAME_JAVADOC_LOCATION,
                        javadocPath);
            }
        }

        // Copy and attach project dependencies
        for (Dependency dependency : projDependencies) {
            File libfile;
            File sourceFile = null;
            File javadocFile = null;

            try {
                // Copy dependency to specified directory
                Artifact artifact = dependency.getArtifact();
                libfile = copyDependency(artifact, targetDirFile);
                removeFile(toDeleteFiles, libfile);

                // Copy source to specified directory
                Artifact sourceArtifact = dependency.getSrcArtifact();
                if (sourceArtifact.isResolved()) {
                    sourceFile = copyDependency(sourceArtifact, sourcesDirFile);
                    removeFile(toDeleteFiles, sourceFile);
                }

                // Copy javadoc to specified directory
                Artifact javadocArtifact = dependency.getJavadocArtifact();
                if (javadocArtifact.isResolved()) {
                    javadocFile = copyDependency(javadocArtifact, javadocDirFile);
                    removeFile(toDeleteFiles, javadocFile);
                }

                // Acquire copied dependency's relative path
                String path = libfile.getAbsolutePath().substring(
                        basedir.getAbsolutePath().length() + 1).replace(SEP, "/");
                String srcPath = createSourcePath(sourcesDirFile, sourceFile, sourceArtifact);
                String javadocPath = createJavadocPath(javadocDirFile, javadocFile, javadocArtifact);

                // Remove existing class path entry
                List<Element> existenceEntries = eclipseClasspath.findClasspathEntry(artifactHelper
                        .getVersionIndependentFileNamePattern(artifact));
                eclipseClasspath.removeClasspathEntries(existenceEntries);

                // Add new class path entry
                Element classpathEntry = eclipseClasspath.createClasspathEntry(path, srcPath,
                        KIND_LIB);
                root.appendChild(classpathEntry);
                if (javadocPath != null) {
                    eclipseClasspath.addAttribute(classpathEntry, ATTRNAME_JAVADOC_LOCATION,
                            javadocPath);
                }
            } catch (IOException ex) {
                logger.error(ex.getLocalizedMessage(), ex);
            }
        }

        // Delete unnecessary dependencies
        deleteFiles(toDeleteFiles);

        // Output XML
        BufferedOutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(dotClassPathFile));
            eclipseClasspath.writeDocument(os);
            os.flush();
            logger.info(".classpath wrote : " + dotClassPathFile.getAbsolutePath());
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    protected void logArtifact(Artifact artifact, int indent) {
        if (artifact != null && artifact.isResolved()) {
            logger.info(StringUtils.repeat(" ", indent) + artifact.toString());
        }
    }

    protected File prepareDir(File projectBaseDir, String dirname) {
        String path = projectBaseDir.getAbsolutePath() + SEP + dirname;
        File dirfile = new File(path);
        if (!dirfile.exists()) {
            if (dirfile.mkdir()) {
                logger.info("Directory created. path:" + dirfile.getAbsolutePath());
            } else {
                throw new PluginRuntimeException("Unable to create directory. : "
                        + dirfile.getAbsolutePath());
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
        for (File file : files) {
            list.add(file);
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
                logger.info("File deleted : " + file.getAbsolutePath());
            } else {
                logger.warn("Failed to delete file : " + file.getAbsolutePath());
            }
        }
    }

    protected File copyDependency(Artifact artifact, File toDir) throws IOException {
        File srcFile = artifact.getFile();
        File destFile = new File(toDir.getAbsolutePath() + SEP + srcFile.getName());
        if (!destFile.exists() || srcFile.lastModified() > destFile.lastModified()) {
            FileUtils.copyFile(srcFile, destFile, true);
            logger.info("Dependency copied to " + destFile.getAbsolutePath());
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

    protected String createJavadocPath(File javadocDirFile, File javadocFile,
            Artifact javadocArtifact) {
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

    protected List<Dependency> resolveArtifacts(Set<Artifact> artifacts,
            ClasspathPolicy classpathPolicy) {
        List<Dependency> dependencies = new ArrayList<Dependency>();

        for (Artifact artifact : artifacts) {
            // Build dependency objects
            Dependency dependency = new Dependency(artifact);
            dependency.setClasspathPolicy(classpathPolicy);
            dependencies.add(dependency);

            // Get artifact
            if (!artifact.isResolved()) {
                try {
                    artifactHelper.resolve(artifact, true);
                } catch (ArtifactResolutionRuntimeException ex) {
                    logger.error(ex.getLocalizedMessage(), ex.getCause());
                    continue;
                }
            }

            // Create source artifact
            Artifact srcArtifact = artifactHelper.createSourceArtifact(artifact);
            artifactHelper.resolve(srcArtifact, false);
            dependency.setSrcArtifact(srcArtifact);

            // Create Javadoc artifact
            Artifact javadocArtifact = artifactHelper.createJavadocArtifact(artifact);
            artifactHelper.resolve(javadocArtifact, false);
            dependency.setJavadocArtifact(javadocArtifact);
        }

        return dependencies;
    }

    protected boolean checkDependencies(List<Dependency> repoDependencies,
            List<Dependency> projDependencies) {
        boolean valid = true;

        logger.info(Logger.SEPARATOR);
        logger.info(" Dependency report.  [R]:Resolved [N]:Not resolved");

        if (repoDependencies.size() > 0) {
            logger.info(Logger.SEPARATOR);
            logger.info(" REPOSITORY Dependencies");
            logger.info(Logger.SEPARATOR);
            valid &= doCheckDependencies(repoDependencies);
        }

        if (projDependencies.size() > 0) {
            logger.info(Logger.SEPARATOR);
            logger.info(" PROJECT Dependencies");
            logger.info(Logger.SEPARATOR);
            valid &= doCheckDependencies(projDependencies);
        }
        logger.info(Logger.SEPARATOR);

        return valid;
    }

    protected boolean doCheckDependencies(List<Dependency> dependencies) {
        boolean valid = true;

        for (Dependency dependency : dependencies) {
            Artifact artifact = dependency.getArtifact();
            logger.info(String.format(" %s   %s", formatResolveStatus(artifact), artifact));

            Artifact srcArtifact = dependency.getSrcArtifact();
            logger.info(String.format("   %s %s", formatResolveStatus(srcArtifact), srcArtifact));

            Artifact javadocArtifact = dependency.getJavadocArtifact();
            logger.info(String.format("   %s %s", formatResolveStatus(javadocArtifact),
                    javadocArtifact));
            logger.info("");
        }

        return valid;
    }

    protected String formatResolveStatus(Artifact artifact) {
        return artifact.isResolved() ? "[R]" : "[N]";
    }

    protected boolean checkParameters() {
        logger.info("[Version] " + pluginInformation.getVersion());
        if (ClasspathPolicy.REPOSITORY.confName().equals(policy)) {
            classpathPolicy = ClasspathPolicy.REPOSITORY;
        } else if (ClasspathPolicy.PROJECT.confName().equals(policy)) {
            classpathPolicy = ClasspathPolicy.PROJECT;
        } else {
            logger.error("Parameter policy must be \"repository\" or \"project\".");
            return false;
        }
        logger.info("[Parameter:policy] " + classpathPolicy.name());

        if (StringUtils.isEmpty(destdir)) {
            logger.error("Parameter destdir is not specified.");
            return false;
        } else {
            logger.info("[Parameter:destdir] " + destdir);
        }

        if (excludeGroupIds == null) {
            excludeGroupIds = new ArrayList<String>();
        }
        logger.info("[Parameter:excludeGroupIds] " + excludeGroupIds.toString());

        if (excludeScopes == null) {
            excludeScopes = new ArrayList<String>();
        }
        logger.info("[Parameter:excludeScopes] " + excludeScopes.toString());

        logger.info("[Parameter:forceResolve] " + forceResolve);
        return true;
    }

    @SuppressWarnings("unchecked")
    protected Set<Artifact> getArtifacts() {
        return project.getArtifacts();
    }

    protected void prepare() {
        logger = new Logger(getLog());

        workspaceConfigurator = new WorkspaceConfigurator(project);
        workspaceConfigurator.loadConfiguration();

        artifactHelper = new ArtifactHelper();
        artifactHelper.setFactory(artifactFactory);
        artifactHelper.setResolver(artifactResolver);
        artifactHelper.setRemoteRepositories(remoteArtifactRepositories);
        artifactHelper.setLocalRepository(localRepository);
        artifactHelper.setWorkspaceConfigurator(workspaceConfigurator);
        artifactHelper.setLogger(logger);
        artifactHelper.setForceResolve(forceResolve);

        eclipseProjectDir = ProjectUtil.getProjectDir(project);
    }

    public void setExcludeGroups(List<String> excludeGroups) {
        this.excludeGroupIds = excludeGroups;
    }

    public void setDestdir(String destdir) {
        this.destdir = destdir;
    }
}
