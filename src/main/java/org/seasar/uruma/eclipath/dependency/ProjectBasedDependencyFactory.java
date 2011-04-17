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
package org.seasar.uruma.eclipath.dependency;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.seasar.uruma.eclipath.Scope;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public class ProjectBasedDependencyFactory extends AbstractDependencyFactory {

    public ProjectBasedDependencyFactory(File projectDir, LibraryLayout layout) {
        super(projectDir, layout);
    }

    /*
     * @see org.seasar.uruma.eclipath.dependency.DependencyFactory#create(org.apache.maven.artifact.Artifact)
     */
    @Override
    public Dependency create(Artifact artifact) {
        Dependency dependency;
        if (!isExcluded(artifact)) {
            Scope scope = Scope.getScope(artifact.getScope());
            String libDir = layout.getLibDir(scope);
            dependency = new FileDependency(artifact, new File(libDir));
        } else {
            dependency = new M2Dependency(artifact);
        }
        return dependency;
    }
}
