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
package org.seasar.uruma.maven.plugin.eclipse.exception;

/**
 * @author y-komori
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ArtifactResolutionRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 3640284464400677035L;

    /**
     * Constructs a new {@link ArtifactResolutionRuntimeException}.
     */
    public ArtifactResolutionRuntimeException() {
        super();
    }

    /**
     * Constructs a new {@link ArtifactResolutionRuntimeException} with specified detail
     * message and nested {@link Throwable}.
     * 
     * @param message
     *        the error message
     * @param cause
     *        the exception or error that caused this exception to be thrown
     */
    public ArtifactResolutionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link ArtifactResolutionRuntimeException} with specified detail
     * message.
     * 
     * @param message
     *        the error message
     */
    public ArtifactResolutionRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link ArtifactResolutionRuntimeException} with specified nested
     * {@link Throwable}.
     * 
     * @param cause
     *        the exception or error that caused this exception to be thrown
     */
    public ArtifactResolutionRuntimeException(Throwable cause) {
        super(cause);
    }
}
