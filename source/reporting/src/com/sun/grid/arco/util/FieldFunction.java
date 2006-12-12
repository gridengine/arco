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
 * <p><code>FieldFunction</code>
 * </p>
 *
 */
public class FieldFunction{
  /**Field funtion to get the value of a field
   */
  public static final FieldFunction VALUE = new FieldFunction("VALUE", "fieldFunction.value",1);
  /**Field funtion to get the number of value
   */
  public static final FieldFunction COUNT = new FieldFunction("COUNT","fieldFunction.count",1,true);
  /**Field funtion to get the minimum value of a field
   */
  public static final FieldFunction MIN = new FieldFunction("MIN","fieldFunction.min",1,true);
  /**Field funtion to get the maximum value of a field
   */
  public static final FieldFunction MAX = new FieldFunction("MAX","fieldFunction.max",1,true);
  /**field to get the sum of values of a field
   */
  public static final FieldFunction SUM = new FieldFunction("SUM","fieldFunction.sum",1,true);
  /** field to get the average value of a field
   */
  public static final FieldFunction AVG = new FieldFunction("AVG","fieldFunction.avg",1,true);
  /** function to add a value to the selected field-value
   */
  public static final FieldFunction ADDITION = new FieldFunction("+","fieldFunction.plus",2,false,true);
  /** function to substract a value from the selected field-value
   */
  public static final FieldFunction SUBSTRACTION = new FieldFunction("-","fieldFunction.minus",2,false,true);
  /** function to muliply the selected field with another value
   */
  public static final FieldFunction MULIPLY = new FieldFunction("*","fieldFunction.multiply",2,false,true);
  /** function to divide the selected field-value by another value
   */
  public static final FieldFunction DIVISION = new FieldFunction("/","fieldFunction.divide",2,false,true);
  /** name of the field function. For an international GUI
   * this value should be at least a part of the key
   */
  private String name;
  
  private String symbol;
  
  
  /** n umber of parameters allowed for this function
   */
  private int parameterCount;
  /**
   */
  private boolean isAggregate;
  /**
   */
  private boolean typeTestNeeded;
  /** a list of all created fieldfunctions
   */
  private static Vector selectableFieldFunctions;
  
  /**
   * Creates new FieldFunction
   */
  private FieldFunction(String name,String symbol, int paramCount,boolean isAggregate, boolean typeTest)  {
    this.name = name;
    this.symbol = symbol;
    this.parameterCount = paramCount;
    this.isAggregate = isAggregate;
    this.typeTestNeeded = typeTest;
    
    if (selectableFieldFunctions == null) {
      selectableFieldFunctions = new Vector();
    }
    selectableFieldFunctions.add(this);
  }
  
  private FieldFunction(String name, String symbol, int paramCount,boolean isAggregate)  {
    this(name,symbol,paramCount,isAggregate,false);
  }
  
  private FieldFunction(String name,String symbol,int paramCount)  {
    this(name,symbol,paramCount,false);
  }
  
  /** Getter for property name.
   * @return Value of property name.
   *
   */
  public String getName() {
    return name;
  }
  
  public String getSymbol() {
    return symbol;
  }
  
  
  /** Getter for property parameterCount.
   * @return Value of property parameterCount.
   *
   */
  public int getParameterCount() {
    return parameterCount;
  }

  /**
   */
  public boolean isAggreagate() {
    return isAggregate;
  }
  
  /** put the values of the class into a string
   * @return values of the class
   */
  public String toString() {
    StringBuffer msg = new StringBuffer("[Class: " + getClass());
    msg.append("], [Name: " + name);
    msg.append("], [No of parameters: " + parameterCount);
    msg.append("]");
    return msg.toString();
//    return super.toString();
  }
  
  /** compares this instance with another object. Two Objects are 
   * euqal if class and the name member are equal
   * @param obj instance to be compared, should be a FieldFunction
   * @return true if the given parameter is a fieldfunction and the 
   * name-members equals
   */
  public boolean equals(Object obj){
    try{
      FieldFunction ff = (FieldFunction)obj;
      
      if (!ff.name.equals(name)){
        return false;
      }
    } catch (Exception ex){
      return false;
    }
    return true;
  }
  
  /** returns the FilterType for the given name
   * @param name name of the filter
   * @return corresponding name
   */  
  public static FieldFunction getFieldFunctionByName(String name) {
    Iterator findIt = getSelectableFieldFunctions();
    while (findIt.hasNext()) {
      FieldFunction dummy = (FieldFunction) findIt.next();
      if (dummy.getName().equals(name)) {
        return dummy;
      }
    }
    throw new IllegalArgumentException("No FieldFunction found for name " + name);
  }

  
  /** returns a list of fieldfunctions
   * @return an itertor on the list of created FieldFunctions
   */  
  public static Iterator getSelectableFieldFunctions() {
    return selectableFieldFunctions.iterator();
  }
  
  /** Getter for property typeTestNeeded.
   * @return Value of property typeTestNeeded.
   *
   */
  public boolean isTypeTestNeeded() {
    return typeTestNeeded;
  }
  
} // end of class FieldFunction
