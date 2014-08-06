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

import static org.seasar.uruma.eclipath.classpath.EclipseClasspath.*;

import org.apache.commons.lang.StringUtils;
import org.seasar.uruma.eclipath.model.ClasspathKind;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A class which represents .classpath file's "classpathentry" element.
 *
 * @author y-komori
 */
public class ClasspathEntry {

    private ClasspathKind classpathKind;

    private String path;

    private String sourcePath;

    private String javadocLocation;

    public ClasspathEntry() {
    }

    /**
     * Create this object from specified {@link Element}.
     *
     * @param element
     *        {@link Element}
     */
    public ClasspathEntry(Element element) {
        if (!ELEMENT_CLASSPATHENTRY.equals(element.getNodeName())) {
            throw new IllegalArgumentException("Element name must be \"" + ELEMENT_CLASSPATHENTRY + "\"");
        }

        classpathKind = ClasspathKind.valueOf(element.getAttribute(ATTR_KIND).toUpperCase());
        path = element.getAttribute(ATTR_PATH);
        sourcePath = element.getAttribute(ATTR_SOURCEPATH);
        if (StringUtils.isEmpty(sourcePath)) {
            sourcePath = null;
        }
        NodeList attributes = element.getElementsByTagName(ELEMENT_ATTRIBUTE);
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            Element attribute = (Element) attributes.item(i);
            if (ATTRNAME_JAVADOC_LOCATION.equals(attribute.getAttribute(ATTR_NAME))) {
                javadocLocation = attribute.getAttribute(ATTR_VALUE);
                break;
            }
        }
    }

    public ClasspathKind getClasspathKind() {
        return classpathKind;
    }

    public void setClasspathKind(ClasspathKind classpathKind) {
        this.classpathKind = classpathKind;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getJavadocLocation() {
        return javadocLocation;
    }

    public void setJavadocLocation(String javadocLocation) {
        this.javadocLocation = javadocLocation;
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((classpathKind == null) ? 0 : classpathKind.hashCode());
        result = prime * result + ((javadocLocation == null) ? 0 : javadocLocation.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((sourcePath == null) ? 0 : sourcePath.hashCode());
        return result;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClasspathEntry other = (ClasspathEntry) obj;
        if (classpathKind != other.classpathKind)
            return false;
        if (javadocLocation == null) {
            if (other.javadocLocation != null)
                return false;
        } else if (!javadocLocation.equals(other.javadocLocation))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (sourcePath == null) {
            if (other.sourcePath != null)
                return false;
        } else if (!sourcePath.equals(other.sourcePath))
            return false;
        return true;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ClasspathEntry [classpathKind=" + classpathKind + ", path=" + path + ", sourcePath=" + sourcePath
                + ", javadocLocation=" + javadocLocation + "]";
    }

}
