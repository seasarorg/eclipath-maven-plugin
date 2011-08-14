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
package org.seasar.uruma.eclipath.classpath;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.seasar.uruma.eclipath.Constants;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;

/**
 * The class dealing with the Eclipse .project file.
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class EclipseProject {
    protected static final String PROJECT_FILENAME = ".project";

    protected static final String PROJECT_NAME_PATH = "/projectDescription/name";

    protected final String projectPath;

    protected final String dotProjectFilePath;

    protected Document document;

    /**
     * Constructs new instance.
     * 
     * @param projectPath
     *        path to eclipse project
     */
    public EclipseProject(String projectPath) {
        this.projectPath = projectPath;
        this.dotProjectFilePath = projectPath + Constants.SEP + PROJECT_FILENAME;
        load();
    }

    protected void load() {
        Logger.info("Loading " + dotProjectFilePath);

        SAXReader reader = new SAXReader();
        try {
            document = reader.read(dotProjectFilePath);
        } catch (DocumentException ex) {
            throw new PluginRuntimeException("Failed to loading .project file : " + dotProjectFilePath, ex);
        }
    }

    public String getProjectName() {
        Node projectNameNode = document.selectSingleNode(PROJECT_NAME_PATH);
        if (projectNameNode == null) {
            throw new PluginRuntimeException("Couldn't find project name in " + dotProjectFilePath);
        }
        return projectNameNode.getText();
    }
}
