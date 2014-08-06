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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.seasar.uruma.eclipath.ProjectRefresher;

/**
 * @author y-komori
 */
@Mojo(name = "refresh")
public class RefreshMojo extends AbstractEclipathMojo {
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        ProjectRefresher refresher = new ProjectRefresher();
        refresher.refresh(project, refreshHost, refreshPort);
    }
}
