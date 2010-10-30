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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @goal sync-dependencies
 * @requiresDependencyResolution test
 * @phase process-sources
 */
public class SyncDependenciesMojo extends AbstractMojo {
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
    protected List remoteArtifactRepositories;

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
     * Comma separated list of GroupId Names to exclude.
     * 
     * @parameter
     */
    protected List<String> excludeGroupIds;

    /**
     * Destination directory.
     * 
     * @parameter default-value="lib"
     */
    protected String destdir;

    protected ArtifactHelper artifactHelper;

    protected WorkspaceConfigurator workspaceConfigurator;

    protected Logger logger;

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

        File targetDir = prepareLibDir(basedir, destdir);
        List<File> toDeleteFiles = getExsistingLibraries(targetDir);
        Set<Artifact> artifacts = project.getArtifacts();
        artifacts = artifactHelper.filterArtifacts(artifacts, excludeGroupIds, "compile");

        List<Dependency> dependencies = new ArrayList<Dependency>();

        for (Artifact artifact : artifacts) {
            if (!artifact.isResolved()) {
                artifactHelper.resolve(artifact, true);
            }

            // 依存関係を表すオブジェクトの構築
            Dependency dependency = new Dependency(artifact);
            dependencies.add(dependency);

            // ソースアーティファクトの取得
            Artifact srcArtifact = artifactHelper.createSourceArtifact(artifact);
            artifactHelper.resolve(srcArtifact, false);
            dependency.setSrcArtifact(srcArtifact);

            // Javadocアーティファクトの取得
            Artifact javadocArtifact = artifactHelper.createJavadocArtifact(artifact);
            artifactHelper.resolve(javadocArtifact, false);
            dependency.setJavadocArtifact(javadocArtifact);

            File libfile;
            try {
                // 依存ライブラリを指定されたディレクトリにコピーする
                libfile = copyDependency(artifact, targetDir);
                removeFile(toDeleteFiles, libfile);

                // コピーした依存ライブラリの相対パスを取得する
                String path = libfile.getAbsolutePath().substring(
                        basedir.getAbsolutePath().length() + 1).replace(SEP, "/");

                // ソースパスの取得
                String srcPath = createSourcePath(srcArtifact);

                // Javadoc パスの取得
                String javadocPath = createJavadocPath(javadocArtifact);

                // 既存のクラスパスエントリがあれば削除する
                List<Element> existenceEntries = eclipseClasspath.findClasspathEntry(artifactHelper
                        .getFilename(artifact));
                eclipseClasspath.removeClasspathEntries(existenceEntries);

                // クラスパスエントリの追加
                Element classpathEntry = eclipseClasspath.createClasspathEntry(path, srcPath);
                root.appendChild(classpathEntry);
                if (javadocPath != null) {
                    eclipseClasspath.addAttribute(classpathEntry, ATTRNAME_JAVADOC_LOCATION,
                            javadocPath);
                }
            } catch (IOException ex) {
                logger.error(ex.getLocalizedMessage(), ex);
            }
        }

        // 同期されないライブラリの消去
        deleteFiles(toDeleteFiles);

        // XML 出力
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

        // 結果出力
        logger.info(Logger.SEPARATOR);
        logger.info("Sync results");
        logger.info(Logger.SEPARATOR);
        for (Dependency dependency : dependencies) {
            logArtifact(dependency.getArtifact(), 0);
            logArtifact(dependency.getSrcArtifact(), 2);
            logArtifact(dependency.getJavadocArtifact(), 2);
            logger.info("");
        }
        logger.info(Logger.SEPARATOR);
    }

    protected void logArtifact(Artifact artifact, int indent) {
        if (artifact != null && artifact.isResolved()) {
            logger.info(StringUtils.repeat(" ", indent) + artifact.toString());
        }
    }

    protected File prepareLibDir(File projectBaseDir, String libdir) {
        String dirname = projectBaseDir.getAbsolutePath() + SEP + libdir;
        File dir = new File(dirname);
        if (!dir.exists()) {
            if (dir.mkdir()) {
                logger.info("Directory created. path:" + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    protected List<File> getExsistingLibraries(File targetDir) {
        FileFilter filter = new SuffixFileFilter(".jar");
        File[] files = targetDir.listFiles(filter);
        List<File> result = new LinkedList<File>();
        for (File file : files) {
            result.add(file);
        }
        return result;
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

    protected String createSourcePath(Artifact srcArtifact) {
        if (srcArtifact.isResolved()) {
            return srcArtifact.getFile().getAbsolutePath().replace(SEP, "/");
        } else {
            return null;
        }
    }

    protected String createJavadocPath(Artifact javadocArtifact) {
        if (javadocArtifact.isResolved()) {
            return "jar:file:/" + javadocArtifact.getFile().getAbsolutePath().replace(SEP, "/")
                    + "!/";
        } else {
            return null;
        }
    }

    protected boolean checkParameters() {
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

        return true;
    }

    protected void prepare() {
        logger = new Logger(getLog());

        artifactHelper = new ArtifactHelper();
        artifactHelper.setFactory(artifactFactory);
        artifactHelper.setResolver(artifactResolver);
        artifactHelper.setRemoteRepositories(remoteArtifactRepositories);
        artifactHelper.setLocalRepository(localRepository);
        artifactHelper.setLogger(logger);

        workspaceConfigurator = new WorkspaceConfigurator(project);
        workspaceConfigurator.loadConfiguration();
    }

    public void setExcludeGroups(List<String> excludeGroups) {
        this.excludeGroupIds = excludeGroups;
    }

    public void setDestdir(String destdir) {
        this.destdir = destdir;
    }
}
