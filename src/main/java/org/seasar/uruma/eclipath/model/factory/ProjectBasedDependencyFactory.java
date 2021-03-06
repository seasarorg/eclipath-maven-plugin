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
package org.seasar.uruma.eclipath.model.factory;

import java.io.File;

import org.seasar.uruma.eclipath.WorkspaceConfigurator;
import org.seasar.uruma.eclipath.model.Dependency;
import org.seasar.uruma.eclipath.model.EclipathArtifact;

/**
 * @author y-komori
 */
public class ProjectBasedDependencyFactory extends AbstractDependencyFactory {

    public ProjectBasedDependencyFactory(File projectDir, WorkspaceConfigurator workspaceConfigurator,
            LibraryLayout layout) {
        super(projectDir, workspaceConfigurator, layout);
    }

    @Override
    public Dependency create(EclipathArtifact artifact) {
        Dependency dependency;
        if (!isExcluded(artifact)) {
            dependency = createFileDependency(artifact);
        } else {
            dependency = createM2Dependency(artifact);
        }
        return dependency;
    }
}
