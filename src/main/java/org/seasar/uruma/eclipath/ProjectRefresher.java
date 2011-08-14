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
package org.seasar.uruma.eclipath;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.maven.project.MavenProject;
import org.seasar.uruma.eclipath.classpath.EclipseProject;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 *
 */
public class ProjectRefresher {
    protected int TIMEOUT_TIME = 3 * 1000;

    public void refresh(MavenProject project, String host, int port) {
        EclipseProject eclipseProject = new EclipseProject(project.getBasedir().getAbsolutePath());
        String projectName = eclipseProject.getProjectName();
        URL url = getRefleshRequestURL(host, port, projectName);
        Logger.info("Refreshing project : " + projectName);
        sendRequest(url);
    }

    protected URL getRefleshRequestURL(String hostName, int port, String projectName) {
        String url = "http://" + hostName + ":" + Integer.toString(port) + "/refresh?" + projectName + "=INFINITE";
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new PluginRuntimeException("Invalid URL : " + url);
        }
    }

    protected void sendRequest(URL url) {
        URLConnection conn;
        InputStream is = null;
        try {
            conn = url.openConnection();
            conn.setReadTimeout(TIMEOUT_TIME);
            conn.connect();
            is = conn.getInputStream();
        } catch (IOException ex) {
            throw new PluginRuntimeException("Failed to send request. : " + url.toExternalForm(), ex);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return;
    }

}
