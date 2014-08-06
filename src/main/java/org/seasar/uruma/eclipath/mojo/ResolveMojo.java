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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.EclipathArtifact;
import org.seasar.uruma.eclipath.model.FileDependency;
import org.seasar.uruma.eclipath.model.M2Dependency;

/**
 * @author y-komori
 */
@Mojo(name = "resolve", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.TEST)
public class ResolveMojo extends AbstractEclipathMojo {

    /*
     * @see org.seasar.uruma.eclipath.mojo.AbstractEclipathMojo#doExecute()
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        // Get dependencies
        Set<EclipathArtifact> dependingArtifacts = getEclipathArtifacts();
        List<Dependency> dependencies = resolveArtifacts(dependingArtifacts, true);
        List<Dependency> prjDependencies = new LinkedList<Dependency>();
        List<Dependency> repoDependencies = new LinkedList<Dependency>();
        for (Dependency dependency : dependencies) {
            if (dependency instanceof FileDependency) {
                prjDependencies.add(dependency);
            } else if (dependency instanceof M2Dependency) {
                repoDependencies.add(dependency);
            }
        }

        Logger.info(Logger.SEPARATOR);
        Logger.info(" Dependency report.  [R]:Resolved [N]:Not resolved");

        if (prjDependencies.size() > 0) {
            Logger.info(Logger.SEPARATOR);
            Logger.info(" PROJECT Dependencies");
            Logger.info(Logger.SEPARATOR);
            for (Dependency dependency : prjDependencies) {
                formatDependency(dependency);
            }
            Logger.info("");
        }

        if (repoDependencies.size() > 0) {
            Logger.info(Logger.SEPARATOR);
            Logger.info(" REPOSITORY Dependencies");
            Logger.info(Logger.SEPARATOR);
            for (Dependency dependency : repoDependencies) {
                formatDependency(dependency);
            }
        }
    }

    protected void formatDependency(Dependency dependency) {
        EclipathArtifact artifact = dependency.getLibraryArtifact();
        Logger.info(String.format(" %s  %s", formatResolveStatus(artifact), artifact));

        EclipathArtifact srcArtifact = dependency.getSourceArtifact();
        Logger.info(String.format("  %s %s", formatResolveStatus(srcArtifact), srcArtifact));

        EclipathArtifact javadocArtifact = dependency.getJavadocArtifact();
        Logger.info(String.format("  %s %s", formatResolveStatus(javadocArtifact), javadocArtifact));
        Logger.info("");
    }

    protected String formatResolveStatus(EclipathArtifact artifact) {
        return artifact.isResolved() ? "[R]" : "[N]";
    }
}
