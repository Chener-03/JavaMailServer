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

import org.apache.james.jspf.core.DNSLookupContinuation;
import org.apache.james.jspf.core.LogEnabled;
import org.apache.james.jspf.core.Logger;
import org.apache.james.jspf.core.SPFSession;
import org.apache.james.jspf.core.exceptions.NeutralException;
import org.apache.james.jspf.core.exceptions.NoneException;
import org.apache.james.jspf.core.exceptions.PermErrorException;
import org.apache.james.jspf.core.exceptions.TempErrorException;

/**
 * This abstract class represent a gerneric modifier
 * 
 */
public abstract class GenericModifier implements Modifier, ConfigurationEnabled, LogEnabled {

    private String host;

    protected Logger log;

    /**
     * @see org.apache.james.jspf.core.SPFChecker#checkSPF(SPFSession)
     */
    public DNSLookupContinuation checkSPF(SPFSession spfData) throws PermErrorException,
            TempErrorException, NeutralException, NoneException {
        log.debug("Processing modifier: " + this);
        DNSLookupContinuation res = checkSPFLogged(spfData);
        log.debug("Processed modifier: " + this + " resulted in "
                + res == null ? spfData.getCurrentResult() : " dns continuation...");
        return res;
    }
    
    protected abstract DNSLookupContinuation checkSPFLogged(SPFSession spfData) throws PermErrorException,
        TempErrorException, NeutralException, NoneException;


    /**
     * @see Modifier#enforceSingleInstance()
     */
    public boolean enforceSingleInstance() {
        return true;
    }

    /**
     * @see ConfigurationEnabled#config(Configuration)
     */
    public synchronized void config(Configuration params) throws PermErrorException {
        if (params.groupCount() > 0) {
            this.host = params.group(1);
        }
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


}
