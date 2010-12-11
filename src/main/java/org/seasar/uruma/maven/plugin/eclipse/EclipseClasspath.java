/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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
package org.seasar.uruma.maven.plugin.eclipse;

import static org.seasar.uruma.maven.plugin.eclipse.Constants.*;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * The class dealing with the Eclipse .classpath file.
 * 
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class EclipseClasspath {
    protected Logger logger;

    protected Document document;

    public File createDotClassPathFile(File projectBaseDir) {
        String filename = projectBaseDir.getAbsolutePath() + SEP + DOT_CLASSPATH_FILENAME;
        return new File(filename);
    }

    public Document createEmptyDotClassPath() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.appendChild(document.createElement(ELEMENT_CLASSPATH));
            return document;
        } catch (ParserConfigurationException ex) {
            throw new PluginRuntimeException(ex);
        }
    }

    public Document loadDotClassPath(File file) {
        if (!file.exists()) {
            document = createEmptyDotClassPath();
            return document;
        }

        logger.info("Loading " + file.getAbsolutePath() + " ...");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(file);

            NodeList nodeList = document.getElementsByTagName(ELEMENT_CLASSPATHENTRY);
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                Element element = (Element) nodeList.item(i);
                logger.debug("ClasspathEntry loaded.  path=" + element.getAttribute(ATTR_PATH));
            }
            return document;
        } catch (Exception ex) {
            throw new PluginRuntimeException(ex);
        }
    }

    public Element createClasspathEntry(String path, String sourcePath, String kind) {
        Element entry = document.createElement(ELEMENT_CLASSPATHENTRY);
        entry.setAttribute(ATTR_KIND, kind);
        entry.setAttribute(ATTR_PATH, path);
        if (sourcePath != null) {
            entry.setAttribute(ATTR_SOURCEPATH, sourcePath);
        }
        return entry;
    }

    public void addAttribute(Element element, String name, String value) {
        Element attributeElement = document.createElement(ELEMENT_ATTRIBUTE);
        attributeElement.setAttribute(ATTR_NAME, name);
        attributeElement.setAttribute(ATTR_VALUE, value);

        NodeList attributesList = element.getElementsByTagName(ELEMENT_ATTRIBUTES);
        Element attributesElement;
        if (attributesList.getLength() == 0) {
            attributesElement = document.createElement(ELEMENT_ATTRIBUTES);
            element.appendChild(attributesElement);
        } else {
            attributesElement = (Element) attributesList.item(0);
        }

        attributesElement.appendChild(attributeElement);
    }

    public List<Element> findClasspathEntry(Pattern pattern) {
        List<Element> result = new ArrayList<Element>();
        NodeList elements = document.getElementsByTagName(ELEMENT_CLASSPATHENTRY);
        int size = elements.getLength();
        for (int i = 0; i < size; i++) {
            Element element = (Element) (elements.item(i));
            String path = element.getAttribute(ATTR_PATH);
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                result.add(element);
            }
        }
        return result;
    }
    public void removeClasspathEntries(List<Element> entries) {
        Element classpathElement = document.getDocumentElement();
        for (Element entry : entries) {
            Node nextSibling = entry.getNextSibling();
            Node removed = classpathElement.removeChild(entry);
            if (removed != null) {
                if (isWhitespaceText(nextSibling)) {
                    classpathElement.removeChild(nextSibling);
                }
            }
        }
    }

    public boolean isWhitespaceText(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = ((Text) node).getData();
            return StringUtils.isWhitespace(text);
        } else {
            return false;
        }
    }

    public void writeDocument(OutputStream out) {
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            Source source = new DOMSource(document);
            Result result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (Exception ex) {
            throw new PluginRuntimeException(ex);
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
