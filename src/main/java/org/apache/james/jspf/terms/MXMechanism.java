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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent the mx mechanism
 * 
 */
public class MXMechanism extends AMechanism implements SPFCheckerDNSResponseListener {

    private final class ExpandedChecker implements SPFChecker {
        
        /**
         * @see SPFChecker#checkSPF(SPFSession)
         */
        public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
                TempErrorException, NeutralException, NoneException {

            // Get the right host.
            String host = expandHost(spfData);
            
            return new DNSLookupContinuation(new DNSRequest(host, DNSRequest.MX), MXMechanism.this);
        }
    }

    private static final String ATTRIBUTE_MX_RECORDS = "MXMechanism.mxRecords";
    private static final String ATTRIBUTE_CHECK_RECORDS = "MXMechanism.checkRecords";
    /**
     * ABNF: MX = "mx" [ ":" domain-spec ] [ dual-cidr-length ]
     */
    public static final String REGEX = "[mM][xX]" + "(?:\\:"
            + SPFTermsRegexps.DOMAIN_SPEC_REGEX + ")?" + "(?:"
            + DUAL_CIDR_LENGTH_REGEX + ")?";
    
    private SPFChecker expandedChecker = new ExpandedChecker();
    
    /**
     * @see AMechanism#checkSPF(SPFSession)
     */
    public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
            TempErrorException, NeutralException, NoneException{

        // update currentDepth
        spfData.increaseCurrentDepth();

        spfData.pushChecker(expandedChecker);
        return macroExpand.checkExpand(getDomain(), spfData, MacroExpand.DOMAIN);
    }

    /**
     * @see AMechanism#onDNSResponse(DNSResponse, SPFSession)
     */
    public DNSLookupContinuation onDNSResponse(DNSResponse response, SPFSession spfSession)
        throws PermErrorException, TempErrorException, NoneException, NeutralException {
        try {
            
            List records = (List) spfSession.getAttribute(ATTRIBUTE_CHECK_RECORDS);
            List mxR = (List) spfSession.getAttribute(ATTRIBUTE_MX_RECORDS);

            if (records == null) {
            
                records = response.getResponse();

                if (records == null) {
                    // no mx record found
                    spfSession.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.FALSE);
                    return null;
                }
                
                spfSession.setAttribute(ATTRIBUTE_CHECK_RECORDS, records);
                
            } else {
                
                List res = response.getResponse();

                if (res != null) {
                    if (mxR == null) {
                        mxR = new ArrayList();
                        spfSession.setAttribute(ATTRIBUTE_MX_RECORDS, mxR);
                    }
                    mxR.addAll(res);
                }
                
            }

            // if the remote IP is an ipv6 we check ipv6 addresses, otherwise ip4
            boolean isIPv6 = IPAddr.isIPV6(spfSession.getIpAddress());

            String mx;
            while (records.size() > 0 && (mx = (String) records.remove(0)) != null && mx.length() > 0) {
                log.debug("Add MX-Record " + mx + " to list");

                return new DNSLookupContinuation(new DNSRequest(mx, isIPv6 ? DNSRequest.AAAA : DNSRequest.A), MXMechanism.this);
                
            }
                
            // no mx record found
            if (mxR == null || mxR.size() == 0) {
                spfSession.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.FALSE);
                return null;
            }

            // get the ipAddress
            IPAddr checkAddress;
            checkAddress = IPAddr.getAddress(spfSession.getIpAddress(), isIPv6 ? getIp6cidr() : getIp4cidr());
            
            // clean up attributes
            spfSession.removeAttribute(ATTRIBUTE_CHECK_RECORDS);
            spfSession.removeAttribute(ATTRIBUTE_MX_RECORDS);
            spfSession.setAttribute(Directive.ATTRIBUTE_MECHANISM_RESULT, Boolean.valueOf(checkAddressList(checkAddress, mxR, getIp4cidr())));
            return null;
            
        } catch (TimeoutException e) {
            spfSession.setAttribute(ATTRIBUTE_CHECK_RECORDS, null);
            spfSession.setAttribute(ATTRIBUTE_MX_RECORDS, null);
            throw new TempErrorException("Timeout querying the dns server");
        }
    }

    /**
     * @see AMechanism#toString()
     */
    public String toString() {
        return super.toString("mx");
    }

}
