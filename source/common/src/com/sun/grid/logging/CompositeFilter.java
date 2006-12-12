/*___INFO__MARK_BEGIN__*/
/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the Sun Industry Standards Source License Version 1.2
 *
 *  Sun Microsystems Inc., March, 2001
 *
 *
 *  Sun Industry Standards Source License Version 1.2
 *  =================================================
 *  The contents of this file are subject to the Sun Industry Standards
 *  Source License Version 1.2 (the "License"); You may not use this file
 *  except in compliance with the License. You may obtain a copy of the
 *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
 *
 *  Software provided under this License is provided on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
 *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 *  See the License for the specific provisions governing your rights and
 *  obligations concerning the Software.
 *
 *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
 *
 *   Copyright: 2001 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/
package com.sun.grid.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class CompositeFilter implements Filter {
    /** default initial size for the filter. */
    public static final int DEFAULT_SIZE  = 10;
    /** the list of filters. */
    private ArrayList filters;
    /** Creates a new instance of CompositeFilter. */
    public CompositeFilter() {
        this(DEFAULT_SIZE);
    }

    /**
     * Create a new instance of a CompositeFilter.
     * @param size initial size of the filter
     */
    public CompositeFilter(final int size) {
        filters = new ArrayList(size);
    }

    /**
     * add a filter to the composite.
     * @param filter  the filter
     */
    public final void addFilter(final Filter filter) {
        filters.add(filter);
    }

    /**
     * if the log recored loggable throught this filter.
     * @param logRecord  the log record
     * @return  true  the log record is loggable
     */
    public final boolean isLoggable(final LogRecord logRecord) {

        Iterator iter = filters.iterator();
        while (iter.hasNext()) {
            if (((Filter) iter.next()).isLoggable(logRecord)) {
                return true;
            }
        }
        return false;
    }

}
