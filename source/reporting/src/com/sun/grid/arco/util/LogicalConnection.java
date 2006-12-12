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
import java.util.Set;
/**
 * <p><code>LogicalConnation</code>
 * </p>
 *
 */
public class LogicalConnection{
   
  public static final LogicalConnection NONE = new LogicalConnection("","");
  
  /** connection of a logical and
   */
  public static final LogicalConnection AND = new LogicalConnection("and","AND");
  /** connection of logical or
   */
  public static final LogicalConnection OR = new LogicalConnection("or","OR");
  
  /** name of the class 
   */
  private String name;
  /** symbol to be used for sql
   */
  private String symbol;
  /** list of created LogicalConnections
   */
  private static Set selectableConection;
  /**
   * Creates new LogicalConnation
   */
  private LogicalConnection(String name, String symbol)  {
    this.name = name;
    this.symbol = symbol;
  }
  
  /** Getter for property name.
   * @return Value of property name.
   *
   */
  public String getName() {
    return name;
  }
  
  
  /** Getter for property symbol. This value is used to generate sql
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
    try{
      LogicalConnection lc = (LogicalConnection)obj;
      
      if (!lc.name.equals(name)){
        return false;
      }
      
      if (!lc.symbol.equals(symbol)){
        return false;
      }
    }catch(Exception ex){
      return false;
    }
    return true;
  }
  
  /** puts the mambers into a string
   * @return mambers as a string
   */
  public String toString (){
    StringBuffer retVal = new StringBuffer();
    
    retVal.append("[Name: " + name + "], ");
    retVal.append("[Symbol: " + symbol + "], ");
    
    return retVal.toString();
  }
  
  /**
   * @param name
   * @return
   */  
  public static LogicalConnection getLogicalConnectionByName(String name) {
    Iterator findIt = getSelectableConection();
    while (findIt.hasNext()) {
      LogicalConnection dummy = (LogicalConnection) findIt.next();
      if (dummy.getName().equals(name)) {
        return dummy;
      }
    }
    throw new IllegalArgumentException("No LogicalConnection found for name " + name);
  }
  
  /**
   * @param sybmol
   * @return
   */  
  public static LogicalConnection getLogicalConnectionBySymbol(String sybmol) {
    Iterator findIt = getSelectableConection();
    while (findIt.hasNext()) {
      LogicalConnection dummy = (LogicalConnection) findIt.next();
      if (dummy.getSymbol().equals(sybmol)) {
        return dummy;
      }
    }
    throw new IllegalArgumentException("No LogicalConnection found for sybmol " + sybmol);
  }
  
  /** returns a list of logical connections
   * @return an itertor on the list of created LogicalConnections
   */  
  public static Iterator getSelectableConection() {
     if( selectableConection == null ) {
        HashSet tmp = new HashSet();
        tmp.add(NONE);
        tmp.add(AND);
        tmp.add(OR);
        selectableConection = tmp;
     }
    return selectableConection.iterator();
  }
} // end of class LogicalConnation
