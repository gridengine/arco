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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p><code>FilterType</code> is designed as an enum-patter. Though 
 * you can handle the filtertype more easy as with simple static and 
 * it is also more flexible.
 * finals.
 * </p>
 *
 */
public class FilterType{
  /** definition for the filterType <B>equal</B> */
  public static final FilterType EQUAL = new FilterType("equal","=",1);
  /** definition for the filterType <B>not equal</B> */
  public static final FilterType NOT_EQUAL = new FilterType("not_equal","!=",1);
  /** definition for the filterType <B>less</B> */
  public static final FilterType LESS = new FilterType("less","<",1);
  /** definition for the filterType <B>less or equal</B> */
  public static final FilterType LESS_EQUAL = new FilterType("less_equal","<=",1);
  /** definition for the filterType <B>gerater</B> */
  public static final FilterType GREATER = new FilterType("greater",">",1);
  /** definition for the filterType <B>greater or equal</B> */
  public static final FilterType GREATER_EQUAL = new FilterType("greater_equal",">=",1);
  /** definition for the filterType <B>null</B> */
  public static final FilterType NULL = new FilterType("null","IS NULL",0);
  /** definition for the filterType <B>not null</B> */
  public static final FilterType NOT_NULL = new FilterType("not_null","IS NOT NULL",0);
  /** definition for the filterType <B>between</B> */
  public static final FilterType BETWEEN = new FilterType("between","BETWEEN",1);
  /** definition for the filterType <B>in</B> */
  public static final FilterType IN = new FilterType("in","IN",1);
  /** definition for the filterType <B>like</B> */
  public static final FilterType LIKE = new FilterType("like","LIKE",1);
  
  /** name of hte filter
   */
  private String name;
  /** symbol of the filter
   */
  private String symbol;
  /** number of needed parameters for the filter
   */
  private int parameterCount;
  /** list of created FilterTypes
   */
  private static Vector selectableFilterTypes;
  /**
   * Creates new FilterType
   */
  private FilterType(String name, String symbol, int paramCount)  {
    this.name = name;
    this.symbol = symbol;
    this.parameterCount = paramCount;
    
    if (selectableFilterTypes == null) {
      selectableFilterTypes = new Vector();
    }
    selectableFilterTypes.add(this);
  }
  
  /** Getter for property name.
   * @return Value of property name.
   *
   */
  public String getName() {
    return name;
  }
  
  
  /** Getter for property parameterCount.
   * @return Value of property parameterCount.
   *
   */
  public int getParameterCount() {
    return parameterCount;
  }
    
  /** Getter for property symbol.
   * @return Value of property symbol.
   *
   */
  public String getSymbol() {
    return symbol;
  }
    
  /** makes a deep compare of this instance an the given parameter
   * @param obj object to be compared 
   * @return true if the deep compare was successful
   */
  public boolean equals(Object obj){
    try
    {
      FilterType ft = (FilterType)obj;
      
      if (!ft.name.equals(name)){
        return false;
      }
      
      if (!ft.symbol.equals(symbol)){
        return false;
      }
      
      if (ft.parameterCount != parameterCount){
        return false;
      }
      
    }catch (Exception ex){
      return false;
    }
    return true;
  }
  
  /**
   * checks the regiosterd FiltetTypes if one has the same name as
   * specified in the parameter. It returns the <b>first</b> matching
   * FiltetType. If no FiltetType can be found an IllegalArgumentException
   * will be thrown
   * @param name name of the FilterType to look for
   * @return Instance of the firsat FilterType with the given name
   * @throws IllegalArgumentException will be thrown if no FilterType
   * with the specified name has been registered.
   */  
  public static FilterType getFilterTypeByName(String name) throws IllegalArgumentException {
    Iterator findIt = getSelectableFilterTypes();
    while (findIt.hasNext()) {
      FilterType dummy = (FilterType) findIt.next();
      if (dummy.getName().equals(name)) {
        return dummy;
      }
    }
    throw new IllegalArgumentException("No FilterType found for name " + name);
  }
  
  /**
   * checks the regiosterd FiltetTypes if one has the same symbol as
   * specified in the parameter. It returns the <b>first</b> matching
   * FiltetType. If no FiltetType can be found an IllegalArgumentException
   * will be thrown
   * @param symbol symbol of the FilterType to look for
   * @return Instance of the firsat FilterType with the given name
   * @throws IllegalArgumentException will be thrown if no FilterType
   * with the specified name has been registered.
   */  
  public static FilterType getFilterTypeBySymbol(String sybmol) throws IllegalArgumentException {
    Iterator findIt = getSelectableFilterTypes();
    while (findIt.hasNext()) {
      FilterType dummy = (FilterType) findIt.next();
      if (dummy.getSymbol().equals(sybmol)) {
        return dummy;
      }
    }
    throw new IllegalArgumentException("No FilterType found for sybmol " + sybmol);
  }
  
  /** puts the mambers into a string
   * @return members as a string
   */
  public String toString (){
    StringBuffer retVal = new StringBuffer();
    
    retVal.append("[Name: " + name + "], ");
    retVal.append("[Symbol: " + symbol + "], ");
    retVal.append("[Number of needed Parameters: " + Integer.toString(parameterCount) + "]");
    
    return retVal.toString();
  }
  
  /** returns a list of filtertypes
   * @return an itertor on the list of created FilterTypes
   */  
  public static Iterator getSelectableFilterTypes() {
    return selectableFilterTypes.iterator();
  }
  
} // end of class FilterType
