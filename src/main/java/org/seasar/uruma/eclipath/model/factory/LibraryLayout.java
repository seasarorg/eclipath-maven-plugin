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

import org.seasar.uruma.eclipath.model.Scope;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface LibraryLayout {
    public String getLibDir(Scope scope);

    public String getName();

    public String[] getAllRelativePath();

    public abstract class AbstractLibraryLayout implements LibraryLayout {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public class FlatLayout extends AbstractLibraryLayout {
        private static final String LIB = "lib";

        @Override
        public String getLibDir(Scope scope) {
            return LIB;
        }

        @Override
        public String getName() {
            return "flat";
        }

        @Override
        public String[] getAllRelativePath() {
            return new String[] { LIB };
        }
    }

    public class StandAloneLayout extends AbstractLibraryLayout {
        private static final String LIB = "lib";

        private static final String LIB_PROVIDED = "lib-provided";

        private static final String LIB_TEST = "lib-test";

        @Override
        public String getLibDir(Scope scope) {
            String dir = LIB;
            if (scope == Scope.PROVIDED) {
                dir = LIB_PROVIDED;
            } else if (scope == Scope.TEST) {
                dir = LIB_TEST;
            }
            return dir;
        }

        @Override
        public String getName() {
            return "stand-alone";
        }

        @Override
        public String[] getAllRelativePath() {
            return new String[] { LIB, LIB_PROVIDED, LIB_TEST };
        }
    }

    public class WebLayout extends AbstractLibraryLayout {
        private static final String WEBLIB = "src/main/webapp/WEB-INF/lib";

        private static final String LIB = "lib";

        @Override
        public String getLibDir(Scope scope) {
            String dir = WEBLIB;
            if (scope == Scope.PROVIDED || scope == Scope.TEST) {
                dir = LIB;
            }
            return dir;
        }

        @Override
        public String getName() {
            return "web";
        }

        @Override
        public String[] getAllRelativePath() {
            return new String[] { LIB, WEBLIB };
        }
    }
}
