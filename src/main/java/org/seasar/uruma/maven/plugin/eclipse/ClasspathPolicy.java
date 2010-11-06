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

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public enum ClasspathPolicy {
    REPOSITORY("repository", "REPO"), PROJECT("project", "PROJ");

    private String name;

    private String shortName;

    private ClasspathPolicy(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    /**
     * Provide short name.
     * 
     * @return short name
     */
    public String shortName() {
        return shortName;
    }

    /**
     * Provide representation of configuration.
     * 
     * @return configuration name
     */
    public String confName() {
        return name;
    }
    /*
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
