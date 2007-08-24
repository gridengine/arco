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

import com.sun.grid.reporting.dbwriter.event.ParserEvent;
import java.sql.*;
import java.util.*;

import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;


public class DepartmentValueManager extends ValueRecordManager {
   /**
    * Creates a new instance of DepartmentValueManager
    */
   public DepartmentValueManager(Database p_database) 
      throws ReportingException {
      super(p_database, "sge_department_values", "dv_", true,
      new DepartmentValue(null));
   }
   
   public void handleNewSubObject(Record parent, ParserEvent e) throws ReportingException {
   }
   
   public void storeNewValue(Record parent, Field time, String variable, String value, String config,
                             java.sql.Connection connection ) throws ReportingException {
      try {
         Record obj = recordExecutor.newDBRecord();
         obj.setParent(parent.getId());
         obj.getField("dv_time_start").setValue(time);
         obj.getField("dv_time_end").setValue(time);
         obj.getField("dv_variable").setValue(variable);
         obj.getField("dv_svalue").setValue(value);
         obj.getField("dv_dvalue").setValue(value);
         if (config != null) {
            obj.getField("dv_dconfig").setValue(config);
         }
         obj.store( connection );
      } catch (Exception exception) {
         ReportingException ex = new ReportingException("ReportingDepartmentValueManager.createDBObjectError", exception.getMessage());
         ex.initCause( exception );
         throw ex;
      }
   }
   
   
   public void initRecordFromEvent(Record obj, ParserEvent e) {
   }
}
