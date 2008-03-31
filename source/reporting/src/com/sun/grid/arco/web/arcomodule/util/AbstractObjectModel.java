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

import com.iplanet.jato.RequestManager;
import com.iplanet.jato.model.ModelControlException;
import com.iplanet.jato.model.object.ObjectAdapterModel;
import com.iplanet.jato.model.object.KeyPath;
import com.iplanet.jato.model.object.ObjectPath;
import java.util.*;
import com.sun.grid.logging.SGELog;
import java.util.logging.Level;
import com.iplanet.jato.model.object.*;
import com.iplanet.jato.util.ClassUtil;

import com.sun.grid.arco.web.arcomodule.ArcoServlet;

public class AbstractObjectModel extends ObjectAdapterModel
        implements com.iplanet.jato.RequestCompletionListener {
   
   public static final String PROP_ROOT = "/";
   
   private boolean dirty;
   private transient Set modelListeners;
   
   protected AbstractObjectModel() {
      initTypeMapping();
      RequestManager.getRequestContext().addRequestCompletionListener(this);
      setTrace(true);
   }
   
   public void setObject(Object obj) {
      super.setObject(obj);
      fireValueChanged(PROP_ROOT);
   }
   
   public void setValues(String name, Object[] value) {
      Object orgValues[] = super.getValues(name);
      boolean modified = false;
      if( orgValues == null ||
              orgValues.length != value.length) {
         modified = true;
      } else {
         for( int i = 0; i < value.length;i++ ) {
            if( !equals(value[i],orgValues[i])) {
               modified = true;
               if( SGELog.isLoggable(Level.FINE)) {
                  SGELog.fine("{0}[{1}]={2}", name, new Integer(i), value[i]);
               } else {
                  break;
               }
            }
         }
      }
      if( modified ) {
         super.setValues(name, value);
         setDirty(true);
         fireValuesChanged(name);
      }
   }
   
   public void setValue(String name, Object value) {
      Object orgValue = getValue(name);
      if( !equals(value,orgValue) ) {
         setDirty(true);
         if( SGELog.isLoggable(Level.FINE)) {
            SGELog.fine("{0}={1}", name, value);
         }
         super.setValue(name, value);
         fireValueChanged(name);
      }
   }
   
   public Object[] getValues(String name) {
      
      Object[] retValue = null;
      
      try {
         retValue = super.getValues(name);
      } catch( IllegalArgumentException ilae) {
         checkParent(name);
         retValue = null;
      }
      if( SGELog.isLoggable(Level.FINE)) {
         if (retValue == null) {
            SGELog.fine("name[{0}]=null", name);
         } else {
            for( int i = 0; i < retValue.length; i++ ) {
               SGELog.fine("name[{0}][{1}]={2}", name, new Integer(i), retValue[i]);
            }
         }
      }
      
      return retValue==null ? new String[0]:retValue;
   }
   
   /**
    * Determine if a parent element of the expression
    * return a value;
    * @return IllegalArgumentException if no parent
    *         returns a result
    */
   private void checkParent(String name) {
      KeyPath kp = new KeyPath(name);
      ObjectPath op = kp.getPath();
      while (!op.isRoot()) {
         try {
            super.getValue(op.toString());
            return;
         } catch( IllegalArgumentException ilae) {
            op = op.getParent();
         }
      }
      throw new IllegalArgumentException("Invalid expression " + name);
   }
   
   
   public Object getValue(String name) {
      Object retValue = null;
      try {
         retValue = super.getValue(name);
         SGELog.fine("name[{0}]={1}", name, retValue);
      } catch (IllegalArgumentException ilae) {
         checkParent(name);
         retValue = null;
      }
      //return retValue==null ? "" : retValue;
      if( retValue instanceof Collection && ((Collection)retValue).isEmpty()) {
         return null;
      } else {
         return retValue;
      }
   }
   
   private static boolean equals( Object o1, Object o2) {
      if( o1 == null ) {
         if( o2 == null ) {
            return true;
         } else {
            return false;
         }
      } else if ( o2 == null ) {
         if( o1 instanceof String &&
                 ((String)o1).length() == 0 ) {
            return true;
         } else {
            return false;
         }
      } else {
         if( o1 instanceof Number ) {
            o1 = o1.toString();
         }
         if( o2 instanceof Number ) {
            o2 = o2.toString();
         }
         if( o1 instanceof String ) {
            if( o2 instanceof String ) {
               return equals((String)o1, (String)o2);
            } else {
               return false;
            }
         } else {
            return o1.equals(o2);
         }
      }
   }
   private static boolean equals( String s1, String s2) {
      
      SGELog.fine("\"{0}\" == \"{1}\"", s1, s2);
      int i1 = 0;
      int i2 = 0;
      char c1 = 0;
      char c2 = 0;
      
      if( s1.length() == 0 && s2.length() == 0 ) {
         return true;
      }
      
      while( i1 < s1.length() && i2 < s2.length() ) {
         c1 = s1.charAt(i1);
         switch(c1) {
            case '\n':
            case '\r':
               i1++;
               continue;
         }
         c2 = s2.charAt(i2);
         switch(c2) {
            case '\n':
            case '\r':
               i2++;
               continue;
         }
         if( c1 != c2 ) {
            return false;
         }
         i1++;
         i2++;
      }
      
      if( i1 < s1.length() ) {
         while( i1 < s1.length() ) {
            c1 = s1.charAt(i1);
            switch(c1) {
               case '\n':
               case '\r':
                  i1++;
                  continue;
               default:
                  return false;
            }
         }
      }
      if( i2 < s2.length() ) {
         while( i2 < s2.length() ) {
            c2 = s2.charAt(i2);
            switch(c2) {
               case '\n':
               case '\r':
                  i2++;   // CR 6356369: endless loop
                  continue;
               default:
                  return false;
            }
         }
      }
      return true;
   }
   
   
   public boolean isDirty() {
      return dirty;
   }
   
   public void setDirty(boolean dirty) {
      this.dirty = dirty;
      if( SGELog.isLoggable(Level.FINE)) {
         SGELog.fine("dirty = " + dirty );
      }
   }
   
   // Model Listener Support ---------------------------------------------------
   
   
   private void initModelListeners() {
      if( modelListeners == null ) {
         modelListeners = new HashSet();
      }
   }
   
   protected void fireValueChanged(String name ) {
      if( modelListeners != null ) {
         Iterator iter = modelListeners.iterator();
         ModelListener lis = null;
         while( iter.hasNext() ) {
            lis = (ModelListener)iter.next();
            lis.valueChanged(name);
         }
      }
   }
   
   protected void fireValuesChanged(String name ) {
      if( modelListeners != null ) {
         Iterator iter = modelListeners.iterator();
         ModelListener lis = null;
         while( iter.hasNext() ) {
            lis = (ModelListener)iter.next();
            lis.valueChanged(name);
         }
      }
   }
   
   public void addModelListener(ModelListener lis) {
      initModelListeners();
      modelListeners.add(lis);
   }
   
   public void removeModelListener(ModelListener lis) {
      if( modelListeners != null ) {
         modelListeners.remove(lis);
      }
   }
   
   public void clearModelListeners() {
      modelListeners = null;
   }
   
   public void requestComplete() {
      clearModelListeners();
   }
   
   private static TypeMappings typeMappings;
   
   // Initialize the JAXB type mapping
   private void initTypeMapping() {
      
      if( typeMappings == null ) {
         
         synchronized( AbstractObjectModel.class ) {
            
            if( typeMappings == null ) {
               Map typeMap = com.sun.grid.arco.model.ObjectFactory.defaultImplementations;
               
               Iterator iter = typeMap.keySet().iterator();
               Class orgClass = null;
               String newName = null;
               TypeMapping [] typeMapping = new TypeMapping[typeMap.size()];
               int i = 0;
               
               while(iter.hasNext()) {
                  orgClass = (Class)iter.next();
                  newName = (String)typeMap.get(orgClass);
                  SGELog.fine("Typemapping: {0} -> {1}", orgClass, newName);
                  typeMapping[i] = new TypeMapping();
                  typeMapping[i].setOriginalTypeName(orgClass.getName());
                  typeMapping[i++].setNewTypeName(newName);
                  
               }
               typeMappings = new TypeMappings();
               typeMappings.setTypeMappings(typeMapping);
            }
         }
         
         
      }
      super.setTypeMappings(typeMappings);
   }

   protected PathEvaluator createPathEvaluator(KeyPath keypath) throws ModelControlException {

		if (null == keypath)
		{
			throw new IllegalArgumentException("param keypath may not be null");
		}
		
		if(keypath.getKey() == null)
		{
			return new MyPathEvaluator(this, getObject());
		}
		else
		{
			Object localObj = getLocalValue(keypath.getKey());
			if(null == localObj)
			{
				Class localObjType =
					getJavaType(new KeyPath(keypath.getKey(),(String)null));
				
				if(null == localObjType)
				{
					throw new ModelControlException(
						"Unable to determine Class Type for localStorage key=" +
							keypath.getKey() + "   check that you have" +
							" all the necessary KeyTypeMappings");
				}
				try
				{
					localObj = ClassUtil.instantiate(localObjType);
					setLocalValue(keypath.getKey(),localObj);
				}
				catch (InstantiationException ex)
				{
					// We can't create the object to walk!
					throw new ModelControlException(
						"Unable to create local storage object.", ex);
				}
			}
			return new MyPathEvaluator(this,localObj, keypath.getKey());
		}
   }
   
   
   
   class MyPathEvaluator extends PathEvaluator {
      
      MyPathEvaluator(PathContext ctx, java.lang.Object obj)  {
         super(ctx, obj);
      }
      
      MyPathEvaluator(PathContext ctx, java.lang.Object obj, java.lang.String key) {
         super(ctx, obj, key);
      }
      
      protected FieldAccessor getFieldAccessor(Class clazz, String fieldName ) {         
         FieldAccessorCache cache = ArcoServlet.getInstance().getFieldAccessorCache();
         return cache.getFieldAccessor(clazz, fieldName);
      }
      
      protected Object getFieldValue(Object obj, String fieldName) {
         if( obj instanceof Map ) {
            return super.getFieldValue(obj,fieldName);
         }
         FieldAccessor fa = getFieldAccessor(obj.getClass(), fieldName);
         return fa.getValue(obj);
      }      

      protected void setFieldValue(Object parent, String fieldName, Class type, Object value) {
         if( parent instanceof Map ) {
            super.setFieldValue(parent,fieldName, type, value);
         } else {
            FieldAccessor fa = getFieldAccessor(parent.getClass(), fieldName);
            fa.setValue(parent, value);
         }
      }

//      protected void setFieldValue(Object parent, String fieldName, Class type, Object value) {
//
//            try {
//               super.setFieldValue(parent, fieldName, type, value);
//            } catch( com.iplanet.jato.model.object.PathEvaluatorException pee ) {
//               if( pee.getRootCause() instanceof java.lang.NoSuchFieldException ) {
//                  Class [] interfaces = type.getInterfaces();
//                  int i = 0;
//                  do {
//                     try {
//                        super.setFieldValue(parent,fieldName, interfaces[i], value );
//                        break;
//                     } catch( com.iplanet.jato.model.object.PathEvaluatorException pee1 ) {
//                        if( pee1.getRootCause() instanceof java.lang.NoSuchFieldException ) {
//                           i++;
//                        } else {
//                           throw pee1;
//                        }
//                     }
//                  } while( i < interfaces.length );
//               } else {
//                  throw pee;
//               }
//            }
//      }
      
   }
   

}

