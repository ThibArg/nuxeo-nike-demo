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

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

/**
 * @author Thibaud Arguillere
 */
@Operation(id=AggregateValuesInBlobOp.ID, category=Constants.CAT_SERVICES, label="Aggregate Values in Blob", description="")
public class AggregateValuesInBlobOp {

    public static final String ID = "AggregateValuesInBlob";

    @Context
    protected CoreSession session;

    // WARNING: The labels must fit what is used in AggregateValues
    // (but we can't use constants here, in the @Param declaration)
    /*
    @Param(name = "statsOnWhat", required = true, widget = Constants.W_OPTION, values = {"Seasons", "Events", "Categories", "Nike-Keywords", "Width Ranges", "Height Ranges", "All"})
    String statsOnWhat;
    */
    @Param(name = "statsOnWhat", required = true, widget = Constants.W_OPTION, values = {AggregateValues.kLABEL_ALL, AggregateValues.kLABEL_SEASONS, AggregateValues.kLABEL_EVENTS, AggregateValues.kLABEL_CATEGORIES, AggregateValues.kLABEL_KEYWORDS, AggregateValues.kLABEL_WIDHTS, AggregateValues.kLABEL_HEIGHTS})
    String statsOnWhat;

    @OperationMethod
    public Blob run() throws Exception {

        AggregateValues av = new AggregateValues();

        String jsonResult = av.run(statsOnWhat, session);

        return new StringBlob(jsonResult, "text/plain", "UTF-8");
    }

}
