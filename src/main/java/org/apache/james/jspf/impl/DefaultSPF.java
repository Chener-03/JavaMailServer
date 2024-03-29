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

package org.apache.james.jspf.impl;

import org.apache.james.jspf.core.Logger;

public class DefaultSPF extends SPF {

    
    /**
     * Uses default Log4JLogger and DNSJava based dns resolver
     */
    public DefaultSPF(String dnsAddress) {
        this(new Slf4JLogger("org.apache.james.jspf.impl.DefaultSPF"),dnsAddress);
    }
    
    /**
     * Uses passed logger and DNSJava based dns resolver
     * 
     * @param logger the logger to use
     */
    public DefaultSPF(Logger logger,String dnsAddress) {
        super(new DNSServiceXBillImpl(logger,dnsAddress), logger);
    }

}
