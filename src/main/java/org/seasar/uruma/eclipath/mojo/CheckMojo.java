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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.seasar.uruma.eclipath.EclipseClasspath;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.exception.ArtifactResolutionRuntimeException;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.EclipathArtifact;

/**
 * @goal check
 * @requiresDependencyResolution test
 * @phase process-sources
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CheckMojo extends AbstractEclipathMojo {
    /*
     * @see org.seasar.uruma.eclipath.mojo.AbstractEclipathMojo#doExecute()
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        // Load ".classpath" file
        EclipseClasspath eclipseClasspath = new EclipseClasspath(eclipseProjectDir);
        eclipseClasspath.load();

        // Get dependencies
        Set<EclipathArtifact> dependingArtifacts = getEclipathArtifacts();
        List<Dependency> dependencies = resolveArtifacts(dependingArtifacts);
        for (Dependency dependency : dependencies) {
            try {
                dependency.copyLibraryArtifact();
                dependency.copySourceArtifact();
                dependency.copyJavadocArtifact();

                String libraryPath = dependency.getLibraryPath();
                String sourcePath = dependency.getSourcePath();
                String javadocPath = dependency.getJavadocPath();
                eclipseClasspath.addClasspathEntry(dependency.getClasspathKind(), libraryPath, sourcePath, javadocPath);
            } catch (IOException ex) {
                // TODO: handle exception
            }
        }

        // Write ".classpath" file
        eclipseClasspath.write();
    }

    protected List<Dependency> resolveArtifacts(Set<EclipathArtifact> artifacts) {
        List<Dependency> dependencies = new ArrayList<Dependency>(artifacts.size());

        for (EclipathArtifact artifact : artifacts) {
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
            EclipathArtifact srcArtifact = artifactHelper.createSourceArtifact(artifact);
            artifactHelper.resolve(srcArtifact, false);
            dependency.setSourceArtifact(srcArtifact);

            // Create Javadoc artifact
            EclipathArtifact javadocArtifact = artifactHelper.createJavadocArtifact(artifact);
            artifactHelper.resolve(javadocArtifact, false);
            dependency.setJavadocArtifact(javadocArtifact);
        }

        return dependencies;
    }
}
