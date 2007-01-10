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

import java.lang.reflect.*;
import java.beans.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.model.FormattedValue;
import com.sun.grid.arco.model.ViewConfiguration;
import com.sun.grid.arco.model.ViewElement;
import com.sun.grid.arco.model.Field;
import com.sun.grid.arco.model.QueryType;
import com.sun.grid.arco.util.FieldFunction;

/**
 * Helper class for manipulation JAXB object
 */
public class Util {
   
   /**
    * Determine if two object are equal. The 
    * objects can be null
    * @param obj1 the first object
    * @param obj2 the second object
    * @return true the objects are equals
    */
   public static boolean equals(Object obj1, Object obj2) {
      if( obj1 == null ) {
         if( obj2 == null ) {
            return true;
         } else {
            return false;
         }
      } else if( obj2 == null ) {
         return false;
      } else {
         return obj1.equals(obj2);
      }
   }
   
   /*
    * Replaces the special charachters that xml attribute cannot contain
    * @param str the String that may containt special characters
    * @return str the replaced String, where special characters are replaced
    * with entity names
    */
   public static String fixSpecialChar(String str) {  
       
      StringBuffer buffer = new StringBuffer(str.length());
      for(int i = 0; i < str.length(); i++) {
          char c = str.charAt(i);         
          switch(c) {
              case '&':
                  if(str.startsWith("&amp;", i)   || 
                     str.startsWith("&lt;", i)    || 
                     str.startsWith("&apos;", i)  ||
                     str.startsWith("&quot;", i)  || 
                     str.startsWith("&gt;", i) ) {                      
                      buffer.append(c);
                  } else {
                      buffer.append("&amp;");
                  }
                  break;
              case '<': buffer.append("&lt;"); break;
              case '>': buffer.append("&gt;"); break;
              case '\'': buffer.append("&apos;"); break;
              case '\"': 
              //if there are 2 next to each other it means one acts as the escape
              //character we need to append only one
              try {
                 if(str.charAt(i + 1) != '\"') {
                     buffer.append("&quot;");
                 }
              } 
              catch(IndexOutOfBoundsException e) {
                 buffer.append("&quot;");
              }
              break;
              default:
                  buffer.append(c);
          }
      }
      return buffer.toString();
   }
   
   public static void correctFieldNames(QueryType query) {
      
         List fieldList = query.getField();
         List columnList = new ArrayList(fieldList.size());
         Iterator iter = fieldList.iterator();
         Field field = null;
         String fieldName = null;
         
         while(iter.hasNext()) {
            field = (Field)iter.next();
            if( field.getReportName() != null ) {
               fieldName = field.getReportName();
            } else {
               
               FieldFunction fieldFunction = FieldFunction.VALUE;               
               if( field.getFunction() != null ) {                  
                   fieldFunction = FieldFunction.getFieldFunctionByName(field.getFunction());
               }
               if( fieldFunction == FieldFunction.VALUE ) {
                  fieldName = field.getDbName();
               } else if ( fieldFunction == FieldFunction.COUNT ) {
                  fieldName = "count_" + field.getDbName();
               } else if ( fieldFunction == FieldFunction.SUM ) {
                  fieldName = "avg_" + field.getDbName();
               } else if ( fieldFunction == FieldFunction.MAX ) {
                  fieldName = "max_" + field.getDbName();
               } else if ( fieldFunction == FieldFunction.MIN ) {
                  fieldName = "avg_" + field.getDbName();
               } else {
                  fieldName = "expr_" + field.getDbName();
               }
            }
            if( columnList.contains(fieldName)) {
               int counter = 0;
               String tmp = fieldName + "_" + counter;
               do {
                  counter++;
                  tmp = fieldName + "_" + counter;
               }
               while( columnList.contains(tmp));
               fieldName = tmp;
            }
            columnList.add(fieldName);
            field.setReportName(fieldName);
         }
      
      
   }
   public static java.text.Format createFormat(FormattedValue formatValue, java.util.Locale locale ) {
   
      String type = formatValue.getType();
      String mask = formatValue.getFormat();
      if( type == null || mask == null ) {
         return NULL_FORMAT;
      } else if ( ArcoConstants.COLUMN_TYPE_DATE.equals( type )) {
         return new java.text.SimpleDateFormat(mask,locale);
      } else if ( ArcoConstants.COLUMN_TYPE_DECIMAL.equals(type)) {
         return new java.text.DecimalFormat(mask, new java.text.DecimalFormatSymbols(locale));
      } else if ( ArcoConstants.COLUMN_TYPE_STRING.equals(type)) {
         return NULL_FORMAT;
      } else {
         throw new IllegalArgumentException("Format type " + mask + " is not supported");
      }
   }
   
   private final static NullFormat NULL_FORMAT = new NullFormat();
   
   static class NullFormat extends java.text.Format {
      
      public StringBuffer format(Object obj, StringBuffer stringBuffer, java.text.FieldPosition fieldPosition) {         
         if( obj != null ) {
            stringBuffer.append(obj.toString());
         }
         return stringBuffer;
      }

      public Object parseObject(String str, java.text.ParsePosition parsePosition) {
         throw new IllegalStateException("parseObject should never be invoked");
      }      
   }
   

   
   
   /**
    * Get a list of all view elements of a view sorted by
    * the order
    * @param view 
    * @return the lsit of all view elements
    */
   public static List getSortedViewElements(ViewConfiguration view) {
      ArrayList ret = new ArrayList(3);
      if( view.isSetTable() && view.getTable().isVisible() ) {
         ret.add(view.getTable());
      }
      if( view.isSetPivot() && view.getPivot().isVisible() ) {
         ret.add(view.getPivot());
      }
      if( view.isSetGraphic() && view.getGraphic().isVisible() ) {
         ret.add(view.getGraphic());
      }
      Collections.sort(ret, VIEW_ELEMENT_COMPARATOR);
      for( int i = 0; i < ret.size(); i++ ) {
         ((ViewElement)ret.get(i)).setOrder(i);
      }
      return ret;
   }
   
   private final static ViewElementComparator VIEW_ELEMENT_COMPARATOR = new ViewElementComparator();
   
   /**
    *  This comparator compares two view elements by its order
    */
   static class ViewElementComparator implements Comparator {
      public int compare(Object o1, Object o2) {
         ViewElement ve1 = (ViewElement)o1;
         ViewElement ve2 = (ViewElement)o2;
         int ret = ve1.getOrder() - ve2.getOrder();
         if( ret == 0 ) {
            ret = 1;
         }
         return ret;
      }      
   }
   
   /**
    * Adjuct the order of all view elements of a view
    * @param view the view
    */
   public static void adjustViewOrder(ViewConfiguration view) {
      List viewElements = getSortedViewElements(view);
      
      for( int i = 0; i < viewElements.size(); i++ ) {
         ((ViewElement)viewElements.get(i)).setOrder(i);
      }
   }
   
   /**
    * get the next view order of a view
    * @param view  the view
    * @return  the next view order
    */
   public static int getNextViewOrder(ViewConfiguration view) {
      int order = 0;
      if( view.isSetTable()&& view.getTable().isVisible() ) {
         order++;
      }
      if( view.isSetPivot()&& view.getPivot().isVisible() ) {
         order++;
      }
      if( view.isSetGraphic()&& view.getGraphic().isVisible() ) {
         order++;
      }
      return order;
   }
   

   
   
   /**
    * This method clones instances of classes which was generated by
    * JAXB.
    * The clone mechnism uses java.beans.Introspector class to find the read
    * and write methods of the classes.
    * @param obj 
    * @throws java.lang.CloneNotSupportedException 
    * @return 
    */
   public static Object clone(Object obj) throws CloneNotSupportedException {

      if( obj == null ) {
         return null;
      } else {
         CloneAction action = getCloneAction(obj);

         return action.clone(obj);
      }
   }
   
   /** This map contains all Clone action instances. The key if the class
    *  of the object which can be cloned by the action.
    */
   private static Map cloneActionMap = Collections.synchronizedMap(new HashMap());
   
   /**
    *   The clone method of collections only makes a shallow copies (the 
    *   elements of the list are not copied). This action creates a deep copy 
    *   of a collections.
    **/
   private static CloneAction collectionCloneAction = new CollectionCloneAction();
   
   
   static  {
      // all immutable objects are "cloned" with the ImmutableCloneAction
      
      ImmutableCloneAction imCloneAction = new ImmutableCloneAction();
      
      cloneActionMap.put( String.class, imCloneAction );
      cloneActionMap.put( Double.class, imCloneAction );
      cloneActionMap.put( Double.TYPE, imCloneAction );
      cloneActionMap.put( Float.class, imCloneAction );
      cloneActionMap.put( Float.TYPE, imCloneAction );
      cloneActionMap.put( Long.class, imCloneAction );
      cloneActionMap.put( Long.TYPE, imCloneAction );
      cloneActionMap.put( Integer.class, imCloneAction );
      cloneActionMap.put( Integer.TYPE, imCloneAction );
      cloneActionMap.put( Boolean.class, imCloneAction );
      cloneActionMap.put( Boolean.TYPE, imCloneAction );      
      
   }
   
   /**
    * get the Clone action for a object.
    * @param obj  the object
    * @throws java.lang.CloneNotSupportedException 
    * @return the clone action for the object
    */
   private static CloneAction getCloneAction(Object obj) 
      throws CloneNotSupportedException {
      
      Class clazz = obj.getClass();
      
      // First look in cache
      
      CloneAction ret = (CloneAction)cloneActionMap.get(clazz);
      
      if( ret == null ) {
         // Create a clone action
         // Since many collection implements the Cloneable interface,
         // but creates only a shallow copy, we have to ensure, that 
         // collections are cloned by the collectionCloneAction
         if( Collection.class.isAssignableFrom(clazz) ) {
            SGELog.fine("class {0} is a collection", clazz.getName() );
            ret = collectionCloneAction;
         } 
         // If the class implements Cloneable use the clone method of this
         // classes
         else if( Cloneable.class.isAssignableFrom(clazz) ) {
            SGELog.fine("class {0} is a Cloneable", clazz.getName() );
            ret = new CloneableCloneAction(clazz);
         } 
         // We assume, that the class if a java bean
         else {
            SGELog.fine("class {0} is a Bean", clazz.getName() );
            ret = new BeansCloneAction(clazz);
         }
         // Store the action in the cache
         cloneActionMap.put(clazz,ret);
      }
      
      return ret;
      
   }
   
   /**
    *  Interaface for all clone actions
    */
   interface CloneAction {
      
      /**
       * clone a object
       * @param obj the object
       * @throws java.lang.CloneNotSupportedException 
       * @return the clone
       */
      public Object clone(Object obj) throws CloneNotSupportedException;
   }
   
   
   static class BeansCloneAction implements CloneAction {
      
      BeanInfo beanInfo;
      BeanProperty props[];
      
      public BeansCloneAction(Class beanClass) 
         throws CloneNotSupportedException {    
         try {
            beanInfo = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor pd[] = beanInfo.getPropertyDescriptors();

            ArrayList propList = new ArrayList(pd.length);
            
            String name = null;
            String isSetName = null;            
            BeanProperty prop = null;
            
            for( int i = 0; i < pd.length; i++ ) {
               name = pd[i].getName();
               // each obj has the getClass property, this property
               // have not to be cloned
               if( name.equals( "class" ) ) {
                  continue;
               } 
               // If the write method is null and the type is no collection
               // the property can not be set
               else if ( pd[i].getWriteMethod() == null 
                          && !Collection.class.isAssignableFrom(pd[i].getPropertyType()) ) {
                  continue;
               }               
               // the Introspector believes, that the isSet<Property name> are the
               // properties, we skip this
               else if( name.startsWith("set") && pd[i].getWriteMethod() == null) {
                  // isSetProperty
                  continue;
               }
               
               prop = new BeanProperty();
               
               // find the isSet method
               
               isSetName = "isSet" + Character.toUpperCase(name.charAt(0))
                           + name.substring(1);
               
               try {
                  prop.isSetMethod = beanClass.getMethod(isSetName, (Class[])null);
               } catch( Exception e ) {
                  // the isSetMethod was not found
                  SGELog.fine("isSetMethod {2} for property {0} of class {1} not found",
                          name, beanClass.getName(), isSetName );
               }
               
               // initialize the property elements
               prop.readMethod = pd[i].getReadMethod();
               prop.writeMethod = pd[i].getWriteMethod();
               prop.type = pd[i].getPropertyType();
               // store the property in the list
               propList.add(prop);
            }
            
            props = new BeanProperty[propList.size()];
            propList.toArray(props);
            
         } catch( Exception ex ) {
            CloneNotSupportedException t = new CloneNotSupportedException(
                    "Error getting property descriptors of class" 
                    + beanClass.getName());
            t.initCause(ex);
            throw t;
         }
      }
      
      /**
       * clone a object
       * @param obj the object
       * @throws java.lang.CloneNotSupportedException 
       * @return the clone
       */
      public Object clone(Object obj) throws CloneNotSupportedException {  
         try {
            Object ret = obj.getClass().newInstance();

            for( int i = 0; i < props.length; i++ ) {
               props[i].clone(obj,ret);
            }
            
            return ret;
         } catch( Exception ex ) {
            CloneNotSupportedException t = new CloneNotSupportedException(
                    "error while cloneing object " + obj + ": "  
                    + ex.getMessage() );
            t.initCause(ex);
            throw t;
         }
      }
      
   }
   
   /**
    *  instances of the class describes a property of a bean
    */
   static class BeanProperty {
      /** type of the property. */
      Class  type;
      /** the write methode for this property. */
      Method writeMethod;
      /** the read method for this property. */
      Method readMethod;
      /** the is set methode. */
      Method isSetMethod;

      /**
       *  clone the property of src into target.
       * @param src      the source object
       * @param target   the target object
       * @throws java.lang.IllegalAccessException 
       * @throws java.lang.CloneNotSupportedException 
       * @throws java.lang.reflect.InvocationTargetException 
       */
      public void clone(Object src, Object target) 
        throws IllegalAccessException, CloneNotSupportedException, InvocationTargetException                 
        {

         Boolean isSet = null;
         Object [] args = new Object[1];
         Object value = null;
         Object clone = null;
         if( isSetMethod == null ) {
            isSet = Boolean.TRUE;
         } else {
            isSet = (Boolean)isSetMethod.invoke(src, (Object[])null);
         }
         if( isSet.booleanValue() ) {
            if( writeMethod != null ) {
               value = readMethod.invoke(src, (Object[])null);
               args[0] = Util.clone(value);
               writeMethod.invoke(target, args);
            } else if ( Collection.class.isAssignableFrom(type) ) {

               Collection srcList = (Collection)readMethod.invoke(src, (Object[])null);
               Collection targetList = (Collection)readMethod.invoke(target, (Object[])null);

               Iterator iter = srcList.iterator();
               while(iter.hasNext()) {
                  value = iter.next();
                  clone = Util.clone(value);
                  targetList.add(clone);
               }                  
            }
         }

      }
   }
   
   /**
    *  This clone action is responsible for "cloning" immutable objects
    *  The clone method simply returns the src object.
    */
   static class ImmutableCloneAction implements CloneAction {
      /**
       * Returns the object
       * @param obj the object
       * @throws java.lang.CloneNotSupportedException 
       * @return 
       */
      public Object clone(Object obj) throws CloneNotSupportedException {
         return obj;
      }      
   }
   
   /**
    *  This clone action is repsonsible for cloneing a collection
    *  (deep copy)
    */
   static class CollectionCloneAction implements CloneAction {

      
      /**
       * Create a deep copy of a collection
       * @param obj  the object must be a collection
       * @throws java.lang.CloneNotSupportedException 
       * @return the deep copy of the object
       */
      public Object clone(Object obj) throws CloneNotSupportedException {
         
         try {
            Collection l = (Collection)obj;
            Collection ret = (Collection)obj.getClass().newInstance();

            Iterator iter = l.iterator();
            Object value;

            while( iter.hasNext() ) {
                value = iter.next();
                ret.add( Util.clone(value) );
            }

            return ret;
         } catch( Exception ex ) {
            CloneNotSupportedException t = new CloneNotSupportedException(
                    "error while cloning collection of class " +
                    obj.getClass().getName() + ": " + ex.getMessage() );
            t.initCause(ex);
            throw t;
         }
      }      
   }
   
   /**
    *   This class is reponsible for cloneing an instance of cloneable
    *   Via reflection the clone method is invoked to clone the
    *   objects.
    */
   static class CloneableCloneAction implements CloneAction {
      
      /** the clone methode */
      private Method cloneMethod;
      
      
      /**
       * Creates a new clone action.
       * @param clazz  instances of this class can be cloned by this action
       * @throws java.lang.CloneNotSupportedException 
       */
      public CloneableCloneAction(Class clazz) throws CloneNotSupportedException {
         try {
            cloneMethod = clazz.getMethod( "clone", (Class[])null );
         } catch( Exception ex ) {
            CloneNotSupportedException t = new CloneNotSupportedException(
                    "error while getting clone method of class " +
                    clazz.getName() + ": " + ex.getMessage() );
            t.initCause(ex);
            throw t;
         }
      }
      
      /**
       * Clone a cloneable.
       * @param obj this object shoud be cloned.
       * @throws java.lang.CloneNotSupportedException 
       * @return the clone
       */
      public Object clone(Object obj) throws CloneNotSupportedException {
         try {
            return cloneMethod.invoke(obj, (Object[])null);
         } catch( Exception e ) {
            Throwable t = e;
            if( e instanceof InvocationTargetException ) {
               t = ((InvocationTargetException)e).getTargetException();               
            }
            if( t instanceof CloneNotSupportedException ) {
               throw (CloneNotSupportedException)t;
            } else {
               CloneNotSupportedException ce = new CloneNotSupportedException(
                       "error while invoking clone method of class " +
                       cloneMethod.getDeclaringClass() + ": " + t.getMessage() );
               ce.initCause(t);
               throw ce;
            }
         }
      }
   }
}
