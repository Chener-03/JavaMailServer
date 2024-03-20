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
 * This class represent the redirect modifier
 * 
 */
public class RedirectModifier extends GenericModifier implements
        SPFCheckEnabled, MacroExpandEnabled {

    private final class ExceptionCatcher implements SPFCheckerExceptionCatcher {
        private SPFChecker spfChecker;

        private SPFChecker finallyChecker;

        public ExceptionCatcher(SPFChecker spfChecker,
                SPFChecker finallyChecker) {
            this.spfChecker = spfChecker;
            this.finallyChecker = finallyChecker;
        }

        /**
         * @see SPFCheckerExceptionCatcher#onException(Exception, SPFSession)
         */
        public void onException(Exception exception, SPFSession session)
                throws PermErrorException, NoneException,
                TempErrorException, NeutralException {
            
            finallyChecker.checkSPF(session);

            // remove every checker until the initialized one
            SPFChecker checker;
            while ((checker = session.popChecker())!=spfChecker) {
                log.debug("Redirect resulted in exception. Removing checker: "+checker);
            }

            if (exception instanceof NeutralException) {
                throw new PermErrorException(
                "included checkSPF returned NeutralException");

            } else if (exception instanceof NoneException) {
                // no spf record assigned to the redirect domain
                throw new PermErrorException(
                        "included checkSPF returned NoneException");
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

    }

    private final class ExpandedChecker implements SPFChecker {
        
        /**
         * @see SPFChecker#checkSPF(SPFSession)
         */
        public DNSLookupContinuation checkSPF(SPFSession spfData)
                throws PermErrorException, NoneException,
                TempErrorException, NeutralException {
            String host = getHost();

            // throws a PermErrorException that we can pass
            // through
            host = macroExpand.expand(host, spfData,
                    MacroExpand.DOMAIN);

            spfData.setCurrentDomain(host);

            spfData.pushChecker(spfChecker);
            return null;
        }
    }

    private final class CleanupChecker implements SPFChecker {
      
        /**
        * @see SPFChecker#checkSPF(SPFSession)
        */
        public DNSLookupContinuation checkSPF(SPFSession spfData)
                throws PermErrorException, TempErrorException,
                NeutralException, NoneException {
            // After the redirect we should not use the
            // explanation from the orginal record
            spfData.setIgnoreExplanation(true);
            
            spfData.popExceptionCatcher();
            return null;
        }
    }

    /**
     * ABNF: redirect = "redirect" "=" domain-spec
     */
    public static final String REGEX = "[rR][eE][dD][iI][rR][eE][cC][tT]"
            + "\\=" + SPFTermsRegexps.DOMAIN_SPEC_REGEX;

    private SPFChecker spfChecker;

    private MacroExpand macroExpand;

    private SPFChecker cleanupChecker = new CleanupChecker();

    private SPFChecker expandedChecker = new ExpandedChecker();

    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(cleanupChecker, cleanupChecker);

    /**
     * Set the host which should be used for redirection and set it in SPF1Data
     * so it can be accessed easy later if needed
     * 
     * @param spfData
     *            The SPF1Data which should used
     * @return the result of this processing
     * @throws PermErrorException
     *             if an error is in the redirect modifier
     * @throws TempErrorException
     *             if an DNS problem accurred
     * @throws NoneException
     * @throws NeutralException
     */
    protected DNSLookupContinuation checkSPFLogged(SPFSession spfData)
            throws PermErrorException, TempErrorException, NeutralException,
            NoneException {
        // the redirect modifier is used only when we had no previous matches
        if (spfData.getCurrentResult() == null) {

            // update currentDepth
            spfData.increaseCurrentDepth();
            
            spfData.pushChecker(cleanupChecker);
            
            spfData.pushExceptionCatcher(exceptionCatcher);

            spfData.pushChecker(expandedChecker);
            return macroExpand.checkExpand(getHost(), spfData, MacroExpand.DOMAIN);
        }
        return null;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "redirect=" + getHost();
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
