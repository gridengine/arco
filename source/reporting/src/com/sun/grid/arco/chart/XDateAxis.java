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
package com.sun.grid.arco.chart;

import java.text.SimpleDateFormat;
import java.util.Locale;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;

/**
 * Customized DateAxis that uses a different date formatter for the standard tick units
 */
public class XDateAxis extends DateAxis
{
   private Locale local;
   
  public XDateAxis(String label)
  {
    this( label, Locale.getDefault() ); 
  }
  
  public XDateAxis( String label, Locale local ) {
     super( label );
     this.local = local;
     setStandardTickUnits(this.createStandardDateTickUnits2());
  }
  
  private SimpleDateFormat createDateFormat( String formatStr ) {
     return new SimpleDateFormat( formatStr, local );
  }
  /**
   * Uses a different formatter
   * @return a collection of standard date tick units.
   */
  private TickUnitSource createStandardDateTickUnits2() {
  
        TickUnits units = new TickUnits();

        // milliseconds
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 1,
                                   createDateFormat("HH:mm:ss.SSS")));
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 5, DateTickUnit.MILLISECOND, 1,
                                   createDateFormat("HH:mm:ss.SSS")));
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 10, DateTickUnit.MILLISECOND, 1,
                                   createDateFormat("HH:mm:ss.SSS")));
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 25, DateTickUnit.MILLISECOND, 5,
                                   createDateFormat("HH:mm:ss.SSS")));
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 50, DateTickUnit.MILLISECOND, 10,
                                   createDateFormat("HH:mm:ss.SSS")));
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 100, DateTickUnit.MILLISECOND, 10,
                                   createDateFormat("HH:mm:ss.SSS")));
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 250, DateTickUnit.MILLISECOND, 10,
                                   createDateFormat("HH:mm:ss.SSS")));
        units.add(new DateTickUnit(DateTickUnit.MILLISECOND, 500, DateTickUnit.MILLISECOND, 50,
                                   createDateFormat("HH:mm:ss.SSS")));

        // seconds
        units.add(new DateTickUnit(DateTickUnit.SECOND, 1, DateTickUnit.MILLISECOND, 50,
                                   createDateFormat("HH:mm:ss")));
        units.add(new DateTickUnit(DateTickUnit.SECOND, 5, DateTickUnit.SECOND, 1,
                                   createDateFormat("HH:mm:ss")));
        units.add(new DateTickUnit(DateTickUnit.SECOND, 10, DateTickUnit.SECOND, 1,
                                   createDateFormat("HH:mm:ss")));
        units.add(new DateTickUnit(DateTickUnit.SECOND, 30,
                                   DateTickUnit.SECOND, 5, createDateFormat("HH:mm:ss")));

        // minutes
        units.add(new DateTickUnit(DateTickUnit.MINUTE, 1, DateTickUnit.SECOND, 5,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.MINUTE, 2, DateTickUnit.SECOND, 10,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.MINUTE, 5, DateTickUnit.MINUTE, 1,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.MINUTE, 10, DateTickUnit.MINUTE, 1,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.MINUTE, 15, DateTickUnit.MINUTE, 5,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.MINUTE, 20, DateTickUnit.MINUTE, 5,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.MINUTE, 30, DateTickUnit.MINUTE, 5,
                                   createDateFormat("HH:mm")));

        // hours
        units.add(new DateTickUnit(DateTickUnit.HOUR, 1, DateTickUnit.MINUTE, 5,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.HOUR, 2, DateTickUnit.MINUTE, 10,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.HOUR, 4, DateTickUnit.MINUTE, 30,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.HOUR, 6, DateTickUnit.HOUR, 1,
                                   createDateFormat("HH:mm")));
        units.add(new DateTickUnit(DateTickUnit.HOUR, 12, DateTickUnit.HOUR, 1,
                                   createDateFormat("d-MMM, HH:mm")));

        // days
        units.add(new DateTickUnit(DateTickUnit.DAY, 1, DateTickUnit.HOUR, 1,
                                   createDateFormat("d-MMM")));
        units.add(new DateTickUnit(DateTickUnit.DAY, 2, DateTickUnit.HOUR, 1,
                                   createDateFormat("d-MMM")));
        units.add(new DateTickUnit(DateTickUnit.DAY, 7, DateTickUnit.DAY, 1,
                                   createDateFormat("d-MMM")));
        units.add(new DateTickUnit(DateTickUnit.DAY, 15, DateTickUnit.DAY, 1,
                                   createDateFormat("d-MMM")));

        // months
        units.add(new DateTickUnit(DateTickUnit.MONTH, 1, DateTickUnit.DAY, 1,
                                   createDateFormat("MMM-yyyy")));
        units.add(new DateTickUnit(DateTickUnit.MONTH, 2, DateTickUnit.DAY, 1,
                                   createDateFormat("MMM-yyyy")));
        units.add(new DateTickUnit(DateTickUnit.MONTH, 3, DateTickUnit.MONTH, 1,
                                   createDateFormat("MMM-yyyy")));
        units.add(new DateTickUnit(DateTickUnit.MONTH, 4,  DateTickUnit.MONTH, 1,
                                   createDateFormat("MMM-yyyy")));
        units.add(new DateTickUnit(DateTickUnit.MONTH, 6,  DateTickUnit.MONTH, 1,
                                   createDateFormat("MMM-yyyy")));

        // years
        units.add(new DateTickUnit(DateTickUnit.YEAR, 1,  DateTickUnit.MONTH, 1,
                                   createDateFormat("yyyy")));
        units.add(new DateTickUnit(DateTickUnit.YEAR, 2,  DateTickUnit.MONTH, 3,
                                   createDateFormat("yyyy")));
        units.add(new DateTickUnit(DateTickUnit.YEAR, 5,  DateTickUnit.YEAR, 1,
                                   createDateFormat("yyyy")));
        units.add(new DateTickUnit(DateTickUnit.YEAR, 10,  DateTickUnit.YEAR, 1,
                                   createDateFormat("yyyy")));
        units.add(new DateTickUnit(DateTickUnit.YEAR, 25, DateTickUnit.YEAR, 5,
                                   createDateFormat("yyyy")));
        units.add(new DateTickUnit(DateTickUnit.YEAR, 50, DateTickUnit.YEAR, 10,
                                   new SimpleDateFormat("yyyy")));
        units.add(new DateTickUnit(DateTickUnit.YEAR, 100, DateTickUnit.YEAR, 20,
                                   createDateFormat("yyyy")));

        return units;
  
  }
}
