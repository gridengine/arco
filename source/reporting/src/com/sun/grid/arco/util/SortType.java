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
package com.sun.grid.arco.util;

public class SortType{
   
  public static final SortType NOT_SORTED = new SortType("NOT_SORTED", "sorttype.notSorted");
  /**
   */
  public static final SortType ASC = new SortType("ASC", "sorttype.Ascending" );
  /**
   */
  public static final SortType DESC = new SortType("DESC", "sorttype.Descending");
  
  /**
   */
  private String name;
  private String symbol;
  
  /**
   * Creates new SortType
   */
  private SortType(String name, String symbol)  {
    this.name = name;
    this.symbol = symbol;
  }
  
  /**
   * @return
   */
  public String getName() {
    return name;
  }
  
  public String getSymbol() {
     return symbol;
  }
  
  public String toString(){return name;}

  
  /**
   * @param name
   * @return
   */  
  public static SortType getSortTypeByName(String name) {
    if(ASC.getName().equals(name)) {
      return ASC;
    }
    if (DESC.getName().equals(name)) {
      return DESC;
    }
    if( NOT_SORTED.getName().equals(name)) {
       return NOT_SORTED;
    }
    throw new IllegalArgumentException("No FilterType found for name " + name);
  }
  
  
} // end of class SortType
