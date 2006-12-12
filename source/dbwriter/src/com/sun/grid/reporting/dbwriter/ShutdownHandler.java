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
package com.sun.grid.reporting.dbwriter;

import com.sun.grid.logging.SGELog;

/**
 * This class acts as ShutdownHandler for the dbwriter
 *
 */
public class ShutdownHandler extends Thread {
    
   /** the dbwriter thread */
    private ReportingDBWriter reportingDBWriter;
    
    /**
     *  Create a new ShutdownHandler
     *  @param  writer   the dbwriter thread
     */
    public ShutdownHandler( ReportingDBWriter writer )
    {
       reportingDBWriter = writer; 
       setPriority( Thread.MAX_PRIORITY );
    }

    /**
     *   Interupts the dbwriter thread
     *   Waits for the end of the dbwriter thread
     *   closes the logging system of the dbwriter thread
     */
    public void run()
    {
        reportingDBWriter.stopProcessing();        
        reportingDBWriter.closeLogging();            
    }
}
