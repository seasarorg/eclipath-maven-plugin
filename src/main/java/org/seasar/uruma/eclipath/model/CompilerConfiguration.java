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
package org.seasar.uruma.eclipath.model;

import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CompilerConfiguration {
    private static final String COMPILER_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

    private static final String COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin";

    private static final String ELEMENT_SOURCE = "source";

    private static final String ELEMENT_TARGET = "target";

    private static final String JAVA_SE = "JavaSE-";

    private static final String J2SE = "J2SE-";

    private String sourceVersion;

    private String targetVersion;

    private CompilerConfiguration() {
    }

    public static CompilerConfiguration load(MavenProject project) {
        CompilerConfiguration conf = new CompilerConfiguration();
        Plugin plugin = findCompilerPlugin(project);
        if (plugin != null) {
            Xpp3Dom dom = getConfiguration(plugin);
            if (dom != null) {
                conf.sourceVersion = getValue(dom, ELEMENT_SOURCE);
                conf.targetVersion = getValue(dom, ELEMENT_TARGET);
            }
        }
        return conf;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public String getJreLibraryName() {
        if (targetVersion == null) {
            return null;
        }

        if (targetVersion.equals("1.7") || targetVersion.equals("1.6")) {
            return JAVA_SE + targetVersion;
        }

        if (targetVersion.equals("1.5") || targetVersion.equals("1.4") || targetVersion.equals("1.3")
                || targetVersion.equals("1.2")) {
            return J2SE + targetVersion;
        }

        return null;
    }

    private static String getValue(Xpp3Dom conf, String name) {
        Xpp3Dom[] children = conf.getChildren(name);
        if (children != null && children.length > 0) {
            return children[0].getValue();
        } else {
            return null;
        }
    }

    private static Xpp3Dom getConfiguration(Plugin compilerPlugin) {
        Object conf = compilerPlugin.getConfiguration();
        if (conf != null && conf instanceof Xpp3Dom) {
            return (Xpp3Dom) conf;
        } else {
            return null;
        }
    }

    private static Plugin findCompilerPlugin(MavenProject project) {
        Build build = project.getBuild();
        List<Plugin> plugins = build.getPlugins();
        for (Plugin plugin : plugins) {
            if (COMPILER_PLUGIN_GROUP_ID.equals(plugin.getGroupId())
                    && COMPILER_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
                return plugin;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CompilerConfiguration [sourceVersion=");
        builder.append(sourceVersion);
        builder.append(", targetVersion=");
        builder.append(targetVersion);
        builder.append("]");
        return builder.toString();
    }
}
