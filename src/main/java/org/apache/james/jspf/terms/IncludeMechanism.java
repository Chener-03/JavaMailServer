/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/


package org.apache.james.jspf.terms;

import org.apache.james.jspf.core.*;
import org.apache.james.jspf.core.exceptions.NeutralException;
import org.apache.james.jspf.core.exceptions.NoneException;
import org.apache.james.jspf.core.exceptions.PermErrorException;
import org.apache.james.jspf.core.exceptions.TempErrorException;

/**
 * This class represent the incude mechanism
 * 
 */
public class IncludeMechanism implements Mechanism, ConfigurationEnabled, LogEnabled, SPFCheckEnabled, MacroExpandEnabled {

    private final class ExceptionCatcher implements SPFCheckerExceptionCatcher {
        private SPFChecker spfChecker;

        private SPFChecker finallyChecker;

        /**
         * @see SPFCheckerExceptionCatcher#onException(Exception, SPFSession)
         */
        public void onException(Exception exception, SPFSession session)
                throws PermErrorException, NoneException,
                TempErrorException, NeutralException {
            
            // remove every checker until the initialized one
            SPFChecker checker;
            while ((checker = session.popChecker())!=spfChecker) {
                log.debug("Include resulted in exception. Removing checker: "+checker);
            }
            
            finallyChecker.checkSPF(session);
            
            if (exception instanceof NeutralException) {
                throw new PermErrorException("included checkSPF returned NeutralException");
            } else if (exception instanceof NoneException) {
                throw new PermErrorException("included checkSPF returned NoneException");
            } else if (exception instanceof PermErrorException){
                throw (PermErrorException) exception;
            } else if (exception instanceof TempErrorException){
                throw (TempErrorException) exception;
            } else if (exception instanceof RuntimeException){
                throw (RuntimeException) exception;
            } else {
                throw new IllegalStateException(exception.getMessage());
            }
        }

        public SPFCheckerExceptionCatcher setExceptionHandlerChecker(
                SPFChecker checker, SPFChecker finallyChecker) {
            this.spfChecker = checker;
            this.finallyChecker = finallyChecker;
            return this;
        }
    }

    private final class ExpandedChecker implements SPFChecker {
      
        /**
        * @see SPFChecker#checkSPF(SPFSession)
        */
        public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
                TempErrorException {

            // throws a PermErrorException that we can pass through
            String host = macroExpand.expand(getHost(), spfData, MacroExpand.DOMAIN);
            
            spfData.setCurrentDomain(host);
            
            // On includes we should not use the explanation of the included domain
            spfData.setIgnoreExplanation(true);
            // set a null current result
            spfData.setCurrentResult(null);
            
            spfData.pushChecker(spfChecker);
            
            return null;
        }
    }

    private final class FinallyChecker implements SPFChecker {
        private String previousResult;

        private String previousDomain;

        /**
         * @see SPFChecker#checkSPF(SPFSession)
         */
        public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
                TempErrorException, NeutralException, NoneException {
            
            spfData.setIgnoreExplanation(false);
            spfData.setCurrentDomain(previousDomain);
            spfData.setCurrentResult(previousResult);

            spfData.popExceptionCatcher();
            
            return null;
        }

        public SPFChecker init(SPFSession spfSession) {

            // TODO understand what exactly we have to do now that spfData is a session
            // and contains much more than the input data.
            // do we need to create a new session at all?
            // do we need to backup the session attributes and restore them?
            this.previousResult = spfSession.getCurrentResult();
            this.previousDomain = spfSession.getCurrentDomain();
            return this;
        }
    }

    private final class CleanupAndResultChecker implements SPFChecker {
        private SPFChecker finallyChecker;

        /**
         * @see SPFChecker#checkSPF(SPFSession)
         */
        public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
                TempErrorException, NeutralException, NoneException {
            
            String currentResult = spfData.getCurrentResult();
            
            finallyChecker.checkSPF(spfData);
            
            if (currentResult == null) {
                throw new TempErrorException("included checkSPF returned null");
            } else if (currentResult.equals(SPF1Constants.PASS)) {
                // TODO this won't work asynchronously
                spfData.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.TRUE);
            } else if (currentResult.equals(SPF1Constants.FAIL) || currentResult.equals(SPF1Constants.SOFTFAIL) || currentResult.equals(SPF1Constants.NEUTRAL)) {
                // TODO this won't work asynchronously
                spfData.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.FALSE);
            } else {
                throw new TempErrorException("included checkSPF returned an Illegal result");
            }

            return null;
        }

        public SPFChecker init(SPFChecker finallyChecker) {
            this.finallyChecker = finallyChecker;
            return this;
        }
    }

    /**
     * ABNF: include = "include" ":" domain-spec
     */
    public static final String REGEX = "[iI][nN][cC][lL][uU][dD][eE]" + "\\:"
            + SPFTermsRegexps.DOMAIN_SPEC_REGEX;

    protected String host;
    
    protected Logger log;

    private SPFChecker spfChecker;

    private MacroExpand macroExpand;

    /**
     * @see SPFChecker#checkSPF(SPFSession)
     */
    public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException, TempErrorException, NoneException, NeutralException {
        // update currentDepth
        spfData.increaseCurrentDepth();
        
        SPFChecker finallyChecker = new FinallyChecker().init(spfData);
        
        SPFChecker cleanupAndResultHandler = new CleanupAndResultChecker().init(finallyChecker);
        spfData.pushChecker(cleanupAndResultHandler);
        spfData.pushExceptionCatcher(new ExceptionCatcher().setExceptionHandlerChecker(cleanupAndResultHandler, finallyChecker));
        
        spfData.pushChecker(new ExpandedChecker());
        return macroExpand.checkExpand(getHost(), spfData, MacroExpand.DOMAIN);
    }

    /**
     * @see ConfigurationEnabled#config(Configuration)
     */
    public synchronized void config(Configuration params) throws PermErrorException {
        if (params.groupCount() == 0) {
            throw new PermErrorException("Include mechanism without an host");
        }
        host = params.group(1);
    }

    /**
     * @return Returns the host.
     */
    protected synchronized String getHost() {
        return host;
    }

    /**
     * @see LogEnabled#enableLogging(Logger)
     */
    public void enableLogging(Logger logger) {
        this.log = logger;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "include:"+getHost();
    }

    /**
     * @see SPFCheckEnabled#enableSPFChecking(SPFChecker)
     */
    public void enableSPFChecking(SPFChecker checker) {
        this.spfChecker = checker;
    }

    /**
     * @see MacroExpandEnabled#enableMacroExpand(MacroExpand)
     */
    public void enableMacroExpand(MacroExpand macroExpand) {
        this.macroExpand = macroExpand;
    }
}
