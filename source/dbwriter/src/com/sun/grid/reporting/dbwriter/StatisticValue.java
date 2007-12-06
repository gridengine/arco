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

import java.sql.*;
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;

public class StatisticValue extends Record {
 
    public static final String TIME_START_FIELD = "sv_time_start";
    public static final String TIME_END_FIELD = "sv_time_end";
    public static final String VARIABLE_FIELD = "sv_variable";
    public static final String VALUE_FIELD = "sv_dvalue";
    
   /**
    * Creates a new instance of StatisticValue
    */
   public StatisticValue(RecordManager p_manager) {
      super(p_manager);
      
      Field myfields[] = {
         new DateField(TIME_START_FIELD),
         new DateField(TIME_END_FIELD),
         new StringField(VARIABLE_FIELD),
         new DoubleField(VALUE_FIELD)
      };
 
      super.setFields(myfields);
   }
   }
