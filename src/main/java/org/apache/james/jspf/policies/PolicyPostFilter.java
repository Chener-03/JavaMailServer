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

package org.apache.james.jspf.policies;


import org.apache.james.jspf.core.SPF1Record;
import org.apache.james.jspf.core.exceptions.NeutralException;
import org.apache.james.jspf.core.exceptions.NoneException;
import org.apache.james.jspf.core.exceptions.PermErrorException;
import org.apache.james.jspf.core.exceptions.TempErrorException;

/**
 * Return an spf record from a given domain. 
 */
public interface PolicyPostFilter {

    /**
     * Filter or replace a record for the given domain
     * 
     * @param currentDomain the domain to retrieve the SPFRecord for
     * @param record the previous record
     * @return the SPFRecord found
     * @throws PermErrorException exception
     * @throws TempErrorException exception
     * @throws NoneException exception
     * @throws NeutralException exception
     */
    public SPF1Record getSPFRecord(String currentDomain, SPF1Record record)
            throws PermErrorException, TempErrorException, NoneException,
            NeutralException;

}