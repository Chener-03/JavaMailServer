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
import org.apache.james.jspf.core.exceptions.*;

import java.util.List;

/**
 * This class represent the exists mechanism
 */
public class ExistsMechanism extends GenericMechanism implements SPFCheckerDNSResponseListener {

    private final class ExpandedChecker implements SPFChecker {
       
    	/*
    	 * (non-Javadoc)
    	 * @see org.apache.james.jspf.core.SPFChecker#checkSPF(org.apache.james.jspf.core.SPFSession)
    	 */
    	public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
                TempErrorException, NeutralException, NoneException {
            String host = expandHost(spfData);
            return new DNSLookupContinuation(new DNSRequest(host,DNSRequest.A), ExistsMechanism.this);
        }
    }

    /**
     * ABNF: exists = "exists" ":" domain-spec
     */
    public static final String REGEX = "[eE][xX][iI][sS][tT][sS]" + "\\:"
            + SPFTermsRegexps.DOMAIN_SPEC_REGEX;

    private SPFChecker expandedChecker = new ExpandedChecker();

    /**
     * @see SPFChecker#checkSPF(SPFSession)
     */
    public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
            TempErrorException, NeutralException, NoneException {
        // update currentDepth
        spfData.increaseCurrentDepth();

        spfData.pushChecker(expandedChecker);
        return macroExpand.checkExpand(getDomain(), spfData, MacroExpand.DOMAIN);
    }

    /**
     * @see SPFCheckerDNSResponseListener#onDNSResponse(DNSResponse, SPFSession)
     */
    public DNSLookupContinuation onDNSResponse(DNSResponse response, SPFSession spfSession) throws PermErrorException, TempErrorException {
        List aRecords;
        
        try {
            aRecords = response.getResponse();
        } catch (TimeoutException e) {
            spfSession.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.FALSE);
            return null;
        }
        
        if (aRecords != null && aRecords.size() > 0) {
            spfSession.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.TRUE);
            return null;
        }
        
        // No match found
        spfSession.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.FALSE);
        return null;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "exists:"+getDomain();
    }

}
