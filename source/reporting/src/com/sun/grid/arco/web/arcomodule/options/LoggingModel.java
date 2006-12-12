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
package com.sun.grid.arco.web.arcomodule.options;

import com.sun.grid.arco.web.arcomodule.util.AbstractObjectModel;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.model.Logging;
import java.util.logging.Level;
import java.util.*;

public class LoggingModel extends AbstractObjectModel {
   
   public static final String PROP_FILTER = "/filter";
   
   private Logging logging;
   
   /** Creates a new instance of LoggingModel */
   public LoggingModel() {
      logging = ArcoServlet.getCurrentInstance().getLogging();
      setObject(logging);      
   }
   
   public Logging getLogging() {
      return logging;
   }
   
   public void setLogging( Logging logging ) {
      this.logging = logging;
      setObject(logging);
   }
   
   public void addNewLogFilter() {
      
      try {
         com.sun.grid.arco.model.LoggingFilter filter = 
               ArcoServlet.getCurrentInstance().getQueryManager().getObjectFactory().createLoggingFilter();

         filter.setLevel(Level.INFO.toString());
         

         getLogging().getFilter().add(filter);

         fireValuesChanged(PROP_FILTER);
         
      } catch( javax.xml.bind.JAXBException jaxbe) {
         IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage());
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   public void removeLogFilter(Integer [] indizes) {
      
      List filterList = getLogging().getFilter();
      
      Arrays.sort(indizes);
      for(int i = indizes.length-1; i>=0 ; i--) {
         filterList.remove(i);
      }
      fireValuesChanged(PROP_FILTER);
   }
   
   public void setLogFilterActive( Integer indizes[], boolean active ) {
      List filterList = getLogging().getFilter();
      com.sun.grid.arco.model.LoggingFilter filter = null;
      for( int i = 0; i < indizes.length; i++ ) {
         filter = (com.sun.grid.arco.model.LoggingFilter)filterList.get(indizes[i].intValue());
         filter.setActive(active);
      }
   }
   
}
