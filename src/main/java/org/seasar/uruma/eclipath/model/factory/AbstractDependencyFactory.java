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

import org.seasar.uruma.eclipath.model.EclipathArtifact;
import org.seasar.uruma.eclipath.util.AssertionUtil;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public abstract class AbstractDependencyFactory implements DependencyFactory {
    protected File projectDir;

    protected LibraryLayout layout;

    protected List<String> excludeGroupIds = new ArrayList<String>();

    protected List<String> excludeScopes = new ArrayList<String>();

    public AbstractDependencyFactory(File projectDir, LibraryLayout layout) {
        AssertionUtil.assertNotNull("projectDir", projectDir);
        AssertionUtil.assertNotNull("layout", layout);
        this.projectDir = projectDir;
        this.layout = layout;
    }

    @Override
    public void addExcludeGroupIds(List<String> groupIds) {
        if (groupIds != null) {
            excludeGroupIds.addAll(groupIds);
        }
    }

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
}
