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
import com.sun.grid.reporting.dbwriter.event.ParserListener;
import java.sql.*;
import java.util.*;

import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;


public class GeneralManager implements ParserListener {
   protected Map subscriptions;
   
   /** Creates a new instance of GeneralManager */
   public GeneralManager(Database p_database) {
      subscriptions = new HashMap();

   }
   
   public void addNewObjectListener(ParserListener l, String key) {
      List listeners = (List)subscriptions.get(key);
      if (listeners == null) {
         listeners = new ArrayList();
         subscriptions.put(key, listeners);
      }
      if (!listeners.contains(l)) {
         listeners.add(l);
      }
   }
   
   public void newLineParsed(ParserEvent e, java.sql.Connection connection ) throws ReportingException {
      Field field = (Field)e.data.get("type");
      String key = field.getValueString(false);
      List listeners = (List)subscriptions.get(key);
      if (listeners != null)  {
         for (int i = 0; i < listeners.size(); i++) {
            ParserListener listener = (ParserListener) listeners.get(i);
            listener.newLineParsed(e, connection);
         }
      }
   }
}
