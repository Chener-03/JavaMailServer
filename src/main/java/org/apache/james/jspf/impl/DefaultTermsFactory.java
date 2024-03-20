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

import org.apache.james.jspf.core.LogEnabled;
import org.apache.james.jspf.core.Logger;
import org.apache.james.jspf.core.exceptions.PermErrorException;
import org.apache.james.jspf.parser.TermsFactory;
import org.apache.james.jspf.terms.Configuration;
import org.apache.james.jspf.terms.ConfigurationEnabled;
import org.apache.james.jspf.wiring.WiringService;
import org.apache.james.jspf.wiring.WiringServiceException;
import org.apache.james.jspf.wiring.WiringServiceTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * The default implementation of the TermsFactory
 */
public class DefaultTermsFactory implements TermsFactory {
    
    private String termFile = "org/apache/james/jspf/parser/jspf.default.terms";
    
    private Collection mechanismsCollection;

    private Collection modifiersCollection;

    private Logger log;
    
    private WiringService wiringService;

    public DefaultTermsFactory(Logger log) {
        this.log = log;
        this.wiringService = new WiringServiceTable();
        ((WiringServiceTable) this.wiringService).put(LogEnabled.class, log);
        init();
    }

    public DefaultTermsFactory(Logger log, WiringService wiringService) {
        this.log = log;
        this.wiringService = wiringService;
        init();
    }

    /**
     * Initialize the factory and the services
     */
    private void init() {
        try {
            Properties p = new Properties();
            p.setProperty("mechanisms","org.apache.james.jspf.terms.AllMechanism,org.apache.james.jspf.terms.AMechanism,org.apache.james.jspf.terms.ExistsMechanism,org.apache.james.jspf.terms.IncludeMechanism,org.apache.james.jspf.terms.IP4Mechanism,org.apache.james.jspf.terms.IP6Mechanism,org.apache.james.jspf.terms.MXMechanism,org.apache.james.jspf.terms.PTRMechanism");
            p.setProperty("modifiers","org.apache.james.jspf.terms.ExpModifier,org.apache.james.jspf.terms.RedirectModifier,org.apache.james.jspf.terms.UnknownModifier");

            String mechs = p.getProperty("mechanisms");
            String mods = p.getProperty("modifiers");
            String[] classes;
            classes = mechs.split(",");
            Class[] knownMechanisms = new Class[classes.length];
            for (int i = 0; i < classes.length; i++) {
                log.debug("Add following class as known mechanismn: "
                        + classes[i]);
                knownMechanisms[i] = Thread.currentThread()
                        .getContextClassLoader().loadClass(classes[i]);
            }
            mechanismsCollection = createTermDefinitionCollection(knownMechanisms);
            classes = mods.split(",");
            Class[] knownModifiers = new Class[classes.length];
            for (int i = 0; i < classes.length; i++) {
                log.debug("Add following class as known modifier: "
                        + classes[i]);
                knownModifiers[i] = Thread.currentThread()
                        .getContextClassLoader().loadClass(classes[i]);
            }
            modifiersCollection = createTermDefinitionCollection(knownModifiers);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Create a collection of term definitions supported by this factory.
     * 
     * @param classes
     *            classes to analyze
     * @param staticFieldName
     *            static field to concatenate
     * @return map <Class,Pattern>
     */
    private Collection createTermDefinitionCollection(Class[] classes) {
        Collection l = new ArrayList();
        for (int j = 0; j < classes.length; j++) {
            try {
                l.add(new DefaultTermDefinition(classes[j]));
            } catch (Exception e) {
                log.debug("Unable to create the term collection", e);
                throw new IllegalStateException(
                        "Unable to create the term collection");
            }
        }
        return Collections.synchronizedCollection(l);
    }


    /**
     * @see TermsFactory#createTerm(Class, Configuration)
     */
    public Object createTerm(Class termDef, Configuration subres) throws PermErrorException, InstantiationException {
        try {
            Object term = termDef.newInstance();
            
            try {
                wiringService.wire(term);
            } catch (WiringServiceException e) {
                throw new InstantiationException(
                        "Unexpected error adding dependencies to term: " + e.getMessage());
            }

            if (term instanceof ConfigurationEnabled) {
                if (subres == null || subres.groupCount() == 0) {
                    ((ConfigurationEnabled) term).config(null);
                } else {
                    ((ConfigurationEnabled) term).config(subres);
                }
            }
            return term;
        } catch (IllegalAccessException e) {
            throw new InstantiationException(
                    "Unexpected error creating term: " + e.getMessage());
        }
    }


    /**
     * @see TermsFactory#getMechanismsCollection()
     */
    public Collection getMechanismsCollection() {
        return mechanismsCollection;
    }


    /**
     * @see TermsFactory#getModifiersCollection()
     */
    public Collection getModifiersCollection() {
        return modifiersCollection;
    }

}
