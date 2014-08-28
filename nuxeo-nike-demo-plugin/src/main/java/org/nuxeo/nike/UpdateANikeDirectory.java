/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     thibaud
 */

package org.nuxeo.nike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;

/**
 *
 * WARNING  WARNING  WARNING  WARNING
 * This is not ok if you create/modify hundred, thousands of documents and
 * update the vocabulary for their "doc created" of "doc modified" event.
 * It's ok for some (say 5-6) not for a lot. If you need to create a lot
 * then don't call this code, creat your stiff and then update the
 * vocabulary
 *
 * @author Thibaud Arguillere
 */
@Operation(id=UpdateANikeDirectory.ID, category=Constants.CAT_SERVICES, label="Update a Nike Directory", description="")
public class UpdateANikeDirectory {

    public static final String ID = "UpdateANikeDirectory";

    public static final Log log = LogFactory.getLog(UpdateANikeDirectory.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected DirectoryService directoryService;

    @Param(name = "directoryName", required = true)
    protected String directoryName;

    @Param(name = "docType", required = true)
    protected String docType;

    @OperationMethod
    public void run() {
        Session directorySession = null;
        CoreSession coreSession = null;

        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            directorySession = directoryService.open(directoryName);

            // Delete all entries in the directory
            DocumentModelList entries = directorySession.getEntries();
            for (DocumentModel entry : entries) {
                directorySession.deleteEntry(entry);
            }

            // Query the documents and create unique entries
            coreSession = ctx.getCoreSession();
            String nxql = "SELECT * FROM " + docType;
            nxql += " WHERE ecm:currentLifeCycleState != 'deleted'";
            DocumentModelList allDocs = coreSession.query(nxql);

            List<String> titles = new ArrayList<String>();
            for (DocumentModel oneDoc : allDocs) {
                String title = oneDoc.getTitle();
                if(!titles.contains(title)) {
                    titles.add(title);

                    Map<String, Object> entry = new HashMap<String, Object>();
                    entry.put("id", title);
                    entry.put("label", title);
                    entry.put("obsolete", 0);
                    entry.put("ordering", 10000);

                    directorySession.createEntry(entry);
                }
            }
            directorySession.close();

        } catch(Exception e) {
            log.error("Error creating the directory", e);
        } finally {
            lock.unlock();
        }
    }

}
