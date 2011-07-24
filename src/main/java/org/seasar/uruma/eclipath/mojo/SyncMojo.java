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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.classpath.ClasspathEntry;
import org.seasar.uruma.eclipath.classpath.EclipseClasspath;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.EclipathArtifact;
import org.w3c.dom.Element;

/**
 * @goal sync
 * @requiresDependencyResolution test
 * @phase process-sources
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SyncMojo extends AbstractEclipathMojo {
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
    }

    protected void removeDuplicatedClasspathEntry(EclipseClasspath eclipseClasspath, Dependency dependency) {
        Pattern pattern = dependency.getLibraryArtifact().getVersionIndependentFileNamePattern();
        List<Element> oldVersionEntries = eclipseClasspath.findClasspathEntry(pattern);
        for (Element entry : oldVersionEntries) {
            ClasspathEntry existingEntry = new ClasspathEntry(entry);
            ClasspathEntry newEntry = createClasspathEntry(dependency);
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
}
