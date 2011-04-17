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
package org.seasar.uruma.eclipath.model.factory;

import org.seasar.uruma.eclipath.Scope;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface LibraryLayout {
    public String getLibDir(Scope scope);

    public abstract class AbstractLibraryLayout implements LibraryLayout {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public class FlatLayout extends AbstractLibraryLayout {
        @Override
        public String getLibDir(Scope scope) {
            return "lib";
        }
    }

    public class StandAloneLayout extends AbstractLibraryLayout {
        @Override
        public String getLibDir(Scope scope) {
            String dir = "lib";
            if (scope == Scope.PROVIDED) {
                dir = "lib-provided";
            } else if (scope == Scope.TEST) {
                dir = "lib-test";
            }
            return dir;
        }
    }

    public class WebLayout extends AbstractLibraryLayout {
        @Override
        public String getLibDir(Scope scope) {
            String dir = "WEB-INF/lib";
            if (scope == Scope.PROVIDED || scope == Scope.TEST) {
                dir = "lib";
            }
            return dir;
        }

    }
}
