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
package com.sun.grid.arco.web.arcomodule.util;

import com.iplanet.jato.model.object.PathEvaluatorException;
import com.iplanet.jato.util.TypeConverter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldAccessor {
   
   private String fieldName;
   private Class  clazz;
   
   private Method setter;
   private Method getter;
   private Field  field;
   private Class  fieldType;
   
   public FieldAccessor(Class clazz, String fieldName ) {
      this.clazz = clazz;
      this.fieldName = fieldName;
      this.getter = findGetter();
      if( getter == null ) {
         field = findField();
         if( field != null ) {
            fieldType = field.getType();
         }
      } else {
         fieldType = getter.getReturnType();
      }
      if( fieldType == null ) {
         throw new IllegalStateException("field " + fieldName + " not found in class " + clazz.getName());
      }
      this.setter = findSetter(fieldType);
   }
   
   
   private Method findGetter() {
      
      String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
      // First try to look for a getter method
      String getterName = "get" + suffix;
      try {
         return clazz.getMethod(getterName, new Class[] {});
      } catch (Exception ex) {
         try {
            getterName = "is" + suffix;
            return clazz.getMethod(getterName, new Class[] {});
         } catch (Exception ex2) {
            return null;
         }
      }
   }
   
   private Method findSetter(Class type) {
      String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
      String setterName = "set" + suffix;
      return findSetter(setterName, type);
   }
   
   private Method findSetter(String setterName, Class type) {
      Method setter = null;
      try {
         setter = clazz.getMethod(	setterName, new Class[] {type} );
      } catch( Exception e ) {
         Class [] interfaces = type.getInterfaces();
         if( interfaces != null ) {
            for(int i = 0; i < interfaces.length && setter == null; i++ ) {
               setter = findSetter(setterName, interfaces[i]);
            }
         }
         if( setter == null ) {
            type = type.getSuperclass();
            if( type != null ) {
               setter = findSetter(setterName, type);
            }
         }
      }
      return setter;
   }
   
   
   private Field findField() {
      try {
         return clazz.getField(fieldName);
      } catch (Exception ex) {
         return null;
      }
   }
   
   public Object getValue(Object obj) throws PathEvaluatorException {
      
      try {
         if( getter != null ) {
            return getter.invoke(obj, (Object[])null);
         } else if ( field != null ) {
            return field.get(obj);
         } else {
            throw new PathEvaluatorException(
                    "PathEvaluator.getFieldValue(Object, String): fieldName ('" + fieldName +
                    "') not found on '"+obj.getClass().getName()+"' object.");
         }
      } catch( Exception e ) {
         if( e instanceof PathEvaluatorException ) {
            throw (PathEvaluatorException)e;
         } else {
            PathEvaluatorException pe = new PathEvaluatorException(
                    "PathEvaluator.getFieldValue(Object, String): fieldName ('" + fieldName +
                    "') error in getter: " + e.getMessage() );
            pe.initCause(e);
            throw pe;
         }
      }
   }
   
   public void setValue(Object obj, Object value) throws PathEvaluatorException {
      if(setter != null ) {
         try {
            if( value != null ) {
               value = TypeConverter.asType(this.fieldType, value);
            }
            setter.invoke(obj,  new Object[] { value } );
         } catch( Exception e ) {
            PathEvaluatorException pe = new PathEvaluatorException(
                    "PathEvaluator.setFieldValue(Object, String, " +
                    "Class, Object): Field ('" + fieldName +
                    "') error in setter (" + clazz.getName() + ")!");
            pe.initCause(e);
            throw pe;
         }
      } else {
         throw new PathEvaluatorException(
                 "PathEvaluator.setFieldValue(Object, String, " +
                 "Class, Object): Field ('" + fieldName +
                 "') not found on object (" + clazz.getName() + ")!");
      }
   }
   
}
