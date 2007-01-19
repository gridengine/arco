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
package com.sun.grid.arco;

public interface ArcoConstants {
   
   public static final String ADVANCED = "advanced";
   public static final String SIMPLE    = "simple";
   
   public static final String COLUMN_TYPE_STRING  = "string";
   public static final String COLUMN_TYPE_DECIMAL = "decimal";
   public static final String COLUMN_TYPE_DATE    = "date";

   public static final String CHART_TYPE_BAR = "bar";
   public static final String CHART_TYPE_BAR_3D = "bar3d";
   public static final String CHART_TYPE_BAR_STACKED = "barStacked";
   
   public static final String CHART_TYPE_LINE = "line";
   public static final String CHART_TYPE_LINE_STACKED = "lineStacked";
   
   public static final String CHART_TYPE_PIE  = "pie";
   public static final String CHART_TYPE_PIE_3D = "pie3d";

   public static final String CHART_SERIES_FROM_ROW =  "row";
   public static final String CHART_SERIES_FROM_COL =  "col";
   
   public static final String PIVOT_TYPE_COLUMN = "col";
   public static final String PIVOT_TYPE_ROW = "row";
   public static final String PIVOT_TYPE_DATA = "data";
   
   public static final String DB_TYPE_POSTGRES = "postgresql";
   public static final String DB_TYPE_ORACLE = "oracle";
   public static final String DB_TYPE_MYSQL = "mysql";   
   
   public static final String NULL_VALUE = "-";
   public static final String FORMAT_ERROR = "Format Error";
}
