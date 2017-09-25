/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.controller.nsc.restserver.api;

import static java.util.Collections.singletonMap;
import static org.osgi.service.jdbc.DataSourceFactory.*;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.restserver.SampleSdnServerRestConstants;
import org.osc.controller.nsc.utils.RedirectionApiUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;

@Component(service = InspectionApis.class)
@Path(SampleSdnServerRestConstants.SERVER_API_PATH_PREFIX + "/insport")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class InspectionApis {
    private static final Logger LOG = Logger.getLogger(InspectionApis.class);
    private EntityManager em;
    private RedirectionApiUtils utils;
    @Reference(target = "(osgi.local.enabled=true)")
    private TransactionControl txControl;
    @Reference(target = "(osgi.unit.name=nsc-mgr)")
    private EntityManagerFactoryBuilder builder;
    @Reference(target = "(osgi.jdbc.driver.class=org.h2.Driver)")
    private DataSourceFactory jdbcFactory;
    @Reference(target = "(osgi.local.enabled=true)")
    private JPAEntityManagerProviderFactory resourceFactory;
    private String ipAddr;

    public void init(String ipAddr) throws Exception {
        if (ipAddr != null) {
            Properties props = new Properties();
            props.setProperty(JDBC_URL,
                    "jdbc:h2:./nscPlugin_" + ipAddr + ";MVCC\\=TRUE;LOCK_TIMEOUT\\=10000;MV_STORE=FALSE;");
            props.setProperty(JDBC_USER, "admin");
            props.setProperty(JDBC_PASSWORD, "admin123");
            DataSource ds = null;
            try {
                ds = this.jdbcFactory.createDataSource(props);
            } catch (SQLException error) {
                LOG.error(error);
                throw new IllegalStateException(error.getMessage(), error);
            }
            EntityManager em = this.resourceFactory
                    .getProviderFor(this.builder, singletonMap("javax.persistence.nonJtaDataSource", (Object) ds), null)
                    .getResource(this.txControl);
            this.utils = new RedirectionApiUtils(em, this.txControl);
            this.ipAddr = ipAddr;
        }
    }

    @Path("/ipAddr/{ipAddr}")
    @POST
    public String createInspectionPort(@PathParam("ipAddr") String ipAddr, InspectionPortEntity entity)
            throws Exception {
        LOG.info(String.format("Creating inspection port for (ingress id %s ; egress id %s)",
                "" + entity.getIngressPort().getElementId(), "" + entity.getEgressPort().getElementId()));
        init(ipAddr);
        return null;
    }
}

