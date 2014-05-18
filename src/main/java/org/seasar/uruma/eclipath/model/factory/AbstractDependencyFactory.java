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
package org.seasar.uruma.eclipath.model.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.WorkspaceConfigurator;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.EclipathArtifact;
import org.seasar.uruma.eclipath.model.FileDependency;
import org.seasar.uruma.eclipath.model.M2Dependency;
import org.seasar.uruma.eclipath.model.Scope;
import org.seasar.uruma.eclipath.util.AssertionUtil;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 */
public abstract class AbstractDependencyFactory implements DependencyFactory {
    protected File projectDir;

    protected WorkspaceConfigurator workspaceConfigurator;

    protected LibraryLayout layout;

    protected List<String> excludeGroupIds = new ArrayList<String>();

    protected List<String> excludeScopes = new ArrayList<String>();

    public AbstractDependencyFactory(File projectDir, WorkspaceConfigurator workspaceConfigurator, LibraryLayout layout) {
        AssertionUtil.assertNotNull("projectDir", projectDir);
        AssertionUtil.assertNotNull("workspaceConfigurator", workspaceConfigurator);
        AssertionUtil.assertNotNull("layout", layout);
        this.projectDir = projectDir;
        this.workspaceConfigurator = workspaceConfigurator;
        this.layout = layout;
    }

    /*
     * @see org.seasar.uruma.eclipath.model.factory.DependencyFactory#addExcludeGroupIds(java.util.List)
     */
    @Override
    public void addExcludeGroupIds(List<String> groupIds) {
        if (groupIds != null) {
            excludeGroupIds.addAll(groupIds);
        }
    }

    /*
     * @see org.seasar.uruma.eclipath.model.factory.DependencyFactory#addExcludeScopes(java.util.List)
     */
    @Override
    public void addExcludeScopes(List<String> scopes) {
        if (scopes != null) {
            excludeScopes.addAll(scopes);
        }
    }

    protected boolean isExcluded(EclipathArtifact artifact) {
        for (String scope : excludeScopes) {
            if (scope.equals(artifact.scope().toString())) {
                return true;
            }
        }
        for (String groupId : excludeGroupIds) {
            if (groupId.equals(artifact.groupId())) {
                return true;
            }
        }
        return false;
    }

    protected Dependency createFileDependency(EclipathArtifact artifact) {
        Scope scope = artifact.scope();
        String libDir = layout.getLibDir(scope);
        return new FileDependency(artifact, projectDir, libDir);
    }

    protected Dependency createM2Dependency(EclipathArtifact artifact) {
        if (workspaceConfigurator.isConfigured()) {
            return new M2Dependency(artifact, workspaceConfigurator.getClasspathVariableM2REPO());
        } else {
            Artifact a = artifact.getArtifact();
            Logger.warn("Workspace may be not configured for m2, use file dependency for " + a.getGroupId() + ":"
                    + a.getArtifactId());
            return createFileDependency(artifact);
        }
    }
}
