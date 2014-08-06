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
package org.seasar.uruma.eclipath.classpath;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.seasar.uruma.eclipath.Constants;
import org.seasar.uruma.eclipath.Logger;
import org.seasar.uruma.eclipath.exception.PluginRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The class dealing with the Eclipse .project file.
 *
 * @author y-komori
 */
public class EclipseProject {
    protected static final String PROJECT_FILENAME = ".project";

    private final String dotProjectFilePath;

    private String projectName;

    /**
     * Constructs new instance.
     *
     * @param projectPath
     *        path to eclipse project
     */
    public EclipseProject(String projectPath) {
        this.dotProjectFilePath = projectPath + Constants.SEP + PROJECT_FILENAME;
    }

    public String getProjectName() {
        if (projectName == null) {
            load(dotProjectFilePath);
        }
        return projectName;
    }

    private void load(String path) {
        Logger.info("Loading " + dotProjectFilePath);

        BufferedInputStream is = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            is = new BufferedInputStream(new FileInputStream(path));
            Document doc = builder.parse(is);

            Node projectDescriptionNode = getProjectDescriptionNode(doc);
            if (projectDescriptionNode != null) {
                Node nameNode = getNameNode(projectDescriptionNode);
                if (nameNode != null) {
                    this.projectName = nameNode.getTextContent();
                }
            }
        } catch (FileNotFoundException e) {
            throw new PluginRuntimeException(".project file is not found : " + dotProjectFilePath, e);
        } catch (SAXException e) {
            throw new PluginRuntimeException("Couldn't find project name in " + dotProjectFilePath, e);
        } catch (IOException e) {
            throw new PluginRuntimeException("Failed to loading .project file : " + dotProjectFilePath, e);
        } catch (ParserConfigurationException e) {
            throw new PluginRuntimeException("Failed to loading .project file : " + dotProjectFilePath, e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        if (this.projectName == null) {
            throw new PluginRuntimeException("Couldn't find project name in " + dotProjectFilePath);
        }
    }

    private Node getProjectDescriptionNode(Document document) {
        NodeList children = document.getChildNodes();
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
            Node node = children.item(i);
            if ("projectDescription".equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }

    private Node getNameNode(Node projectDescriptionNode) {
        NodeList children = projectDescriptionNode.getChildNodes();
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
            Node node = children.item(i);
            if ("name".equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }

}
