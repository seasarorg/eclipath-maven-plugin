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
import java.io.IOException;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public interface Dependency {

    public EclipathArtifact getLibraryArtifact();

    public void setLibraryArtifact(EclipathArtifact artifact);

    public EclipathArtifact getSourceArtifact();

    public void setSourceArtifact(EclipathArtifact srcArtifact);

    public EclipathArtifact getJavadocArtifact();

    public void setJavadocArtifact(EclipathArtifact javadocArtifact);

    public String getLibraryPath();

    public String getSourcePath();

    public String getJavadocPath();

    public File copyLibraryArtifact() throws IOException;

    public File copySourceArtifact() throws IOException;

    public File copyJavadocArtifact() throws IOException;
}
