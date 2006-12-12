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

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class TimeSchedule {
   
   private final static int [] FIELDS = { Calendar.DAY_OF_MONTH, Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND };
   private boolean [] dynamic;
   private int [] values;
   /**
    *  "+1 0 +2 0 0"
    */
   public TimeSchedule(String schedule) {
      
      StringTokenizer st = new StringTokenizer(schedule, " ");
      
      if(st.countTokens() != FIELDS.length) {
         throw new IllegalArgumentException("need " + FIELDS.length + " fields");
      }
      
      values = new int[FIELDS.length];
      dynamic = new boolean[FIELDS.length];
      
      for(int i = 0; i < FIELDS.length; i++) {
         String value = st.nextToken();
         if(value.charAt(0) == '+') {
            dynamic[i] = true;
            values[i] = Integer.parseInt(value.substring(1));
         } else {
            dynamic[i] = false;
            values[i] = Integer.parseInt(value);            
         }
      }
      
   }
   
   public Date getNextTime(long startTime) {
      Calendar cal = Calendar.getInstance();   
      cal.setTimeInMillis(startTime);
      for(int i = 0; i < FIELDS.length; i++) {
         if(dynamic[i]) {
            cal.add(FIELDS[i], values[i]);
         } else {
            cal.set(FIELDS[i], values[i]);
         }
      }
      return cal.getTime();
   }
   
   
}
