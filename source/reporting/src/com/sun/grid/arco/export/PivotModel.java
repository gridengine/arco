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
package com.sun.grid.arco.export;

import com.sun.grid.arco.ArcoConstants;
import java.util.*;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.model.Pivot;
import com.sun.grid.arco.model.FormattedValue;
import com.sun.grid.arco.model.PivotElement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import com.sun.grid.logging.SGELog;
import java.util.logging.Level;
import java.io.*;

public class PivotModel {
   
   private QueryResult result;
   private PivotTreeNode rowHierachy;
   private PivotTreeNode colHierachy;
   private Pivot         pivot;
   
   private List rowChildren;
   private List colChildren;
   
   private List rowList = new ArrayList();
   private List colList = new ArrayList();
   private List dataList = new ArrayList();
   
   private java.text.Format [] dataFormat;
   private Locale locale;
   
   /** Creates a new instance of PivotModel */
   public PivotModel(QueryResult result, java.util.Locale locale) {
      this.result = result;
      this.locale = locale;
      if( result.getQuery().getView().isSetPivot() ) {
         this.pivot = result.getQuery().getView().getPivot();
      } else {
         throw new IllegalStateException("no pivot element defined for obj" + 
                  result.getQuery().getName());
      }
      
      init();
   }
   
   private void init() {
      
      long start = System.currentTimeMillis();
      QueryResult.RowIterator iter = result.rowIterator();

      rowHierachy = new PivotTreeNode("Rowheader");
      colHierachy = new PivotTreeNode("Columnheader");
      
      
      rowList.clear();
      colList.clear();
      dataList.clear();
      
      Iterator elemIter = pivot.getElem().iterator();
      PivotElement elem = null;
      while(elemIter.hasNext()) {
         elem = (PivotElement)elemIter.next();
         if( ArcoConstants.PIVOT_TYPE_COLUMN.equals(elem.getPivotType() ) ) {
            colList.add(elem);
         } else if ( ArcoConstants.PIVOT_TYPE_ROW.equals(elem.getPivotType())) {
            rowList.add(elem);
         } else if ( ArcoConstants.PIVOT_TYPE_DATA.equals(elem.getPivotType())) {
            dataList.add(elem);
         }
      }
      
      while(iter.next()) {
         rowHierachy = buildTreeNode(rowHierachy, rowList, 0, iter );
         colHierachy = buildTreeNode(colHierachy, colList, 0, iter );
      }
      rowChildren = new ArrayList();
      rowHierachy.buildChildList(rowHierachy.getDepth()-1, rowChildren);
      colChildren = new ArrayList();
      colHierachy.buildChildList(colHierachy.getDepth()-1, colChildren);
      
      // Build the Formats for the data
      
      dataFormat = new java.text.Format[dataList.size()];
      
      FormattedValue formatValue = null;
      for( int i = 0; i < dataFormat.length; i++ ) {
         formatValue = (FormattedValue)dataList.get(i);
         dataFormat[i] = com.sun.grid.arco.Util.createFormat(formatValue, locale);
      }
      
      if( SGELog.isLoggable( Level.FINE)) {
         
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         
         rowHierachy.dump(pw);
         pw.flush();         
         SGELog.fine("rowHierachy ------\n{0}\n------", sw.getBuffer());
         sw = new StringWriter();
         pw = new PrintWriter(sw);
         colHierachy.dump(pw);
         pw.flush();
         SGELog.fine("colHierachy ------\n{0}\n------", sw.getBuffer());
      }
      
      if( SGELog.isLoggable( Level.CONFIG)) {
         double diff = ((double)System.currentTimeMillis() - start) / 1000;
         SGELog.config("pivot model initialized in " + diff + "s");
      }
      
   }
   
   public int getRowHierachyDepth() {
      return rowList.size();
   }
   
   public int getColumnHierarchyDepth() {
      return colList.size();
   }
   
   public int getDataHierachyDepth() {
      return dataList.size();
   }
   
   public PivotElement getDataElement(int index) {
      return (PivotElement)dataList.get(index);
   }
   
   public PivotTreeNode getColumnHeader() {
      return colHierachy;
   }

   public PivotTreeNode getRowHeader() {
      return rowHierachy;
   }
   
   private PivotTreeNode buildTreeNode(PivotTreeNode node, List attributes,  
                              int pos, QueryResult.RowIterator rowIter) {
      
      PivotElement attribute = (PivotElement)attributes.get(pos);
      String attributeName = attribute.getName();
      
      int index = result.getColumnIndex(attributeName);
      
      Object value = rowIter.getValue(index);
      java.text.Format format = com.sun.grid.arco.Util.createFormat( attribute, locale );
      
      boolean nodeExist = false;
      PivotTreeNode param;
      
      if (!node.isLeaf() && node.contains(value)) {
         param = (PivotTreeNode) node.getChild(value);
         nodeExist = true;
      } else {
         param = new PivotTreeNode(value);
         param.setFormat(format);
         nodeExist = false;
      }
      int nextPos = pos + 1;
      
      if (nextPos < attributes.size()) {
         param.setUserObject(value);
         param.setFormat(format);
         node.add(buildTreeNode(param, attributes, nextPos, rowIter ));
      } else {
         Integer rowObj = new Integer(rowIter.getRowIndex());
         if (!param.contains(rowObj)) {
            PivotTreeNode rowNode = new PivotTreeNode(rowObj);
            rowNode.setFormat(format);
            rowNode.setAllowsChildren(false);
            param.add(rowNode);
         }
      }
      if (!nodeExist) {
         node.add(param);
      }
      return node;
   }
   
    /** returns the number of rows the pivottable has
     * @return number of row of the pivot table
     */
    public int getRowCount() {
        return getChildCount(rowHierachy, rowHierachy.getDepth() - 2);
    }

    /** returns the number of columns the pivottable has
     * @return number of columns of the pivot table
     */
    public int getColumnCount() {
        return getChildCount(colHierachy, colHierachy.getDepth() - 2);
    }

    /** returns the number of childs of the specified Level
     * @param node the node to search in
     * @param level the level ot childs are below
     * return the number of childs
     */
    public int getChildCount(TreeNode node, int level) {
        if (null == node) {
           throw new IllegalArgumentException("node is null");
        }

        if (((PivotTreeNode)node).getLevel() == level) {
            return node.getChildCount();
        }

        int result = 0;
        int childCount = node.getChildCount();
        for (int childNo = 0; childNo < childCount; childNo++) {
            result += getChildCount(node.getChildAt(childNo), level);
        }
        return result;
    }
    
    public Object[] getValuesAt(int row, int column) {
        PivotTreeNode sqlRow = null;
        
        SGELog.fine("row=" + row+ ", col=" + column);
        
        // PivotTreeNode rowNode = rowHierachy.getChildAt(rowHierachy.getDepth()-1,row);
        PivotTreeNode rowNode = (PivotTreeNode)rowChildren.get(row);        
        SGELog.fine("rowNode is {0}", rowNode.getUserObject() );
        
        //PivotTreeNode columnNode = colHierachy.getChildAt(colHierachy.getDepth() - 1, column);
        PivotTreeNode columnNode = (PivotTreeNode)colChildren.get(column);
        SGELog.fine("columnNode is {0}", columnNode.getUserObject() );
        
        int childCount = rowNode.getChildCount();
        int counter = 0;

        while (sqlRow == null && counter < childCount) {
            PivotTreeNode dummy = (PivotTreeNode) rowNode.getChildAt(counter);
            Object userObject = dummy.getUserObject();
            SGELog.fine("search {0} in {1}", userObject, columnNode.getUserObject() );
            sqlRow = (PivotTreeNode)columnNode.getChild(userObject);
            counter++;
        }
        
        SGELog.fine("sqlRow is {0}", sqlRow);
        if( sqlRow != null ) {
           Iterator iter = dataList.iterator();
           FormattedValue columnObj = null;
           int    columnIndex = 0;
           Object[] values = new Object[dataList.size()];

           Object userObject = sqlRow.getUserObject();
           row = ((Integer)userObject).intValue();
           int i = 0;
           Object value = null;
           while( iter.hasNext() ) {
              columnObj = (FormattedValue)iter.next();
              columnIndex = result.getColumnIndex(columnObj.getName());              
              value = result.getValue(row, columnIndex);
              if( value == null ) {
                 values[i] = ArcoConstants.NULL_VALUE;
              } else if ( dataFormat[i] != null ) {
                 try {
                    values[i] = dataFormat[i].format(value);
                 } catch( IllegalArgumentException ilae ) {
                    values[i] = ArcoConstants.FORMAT_ERROR;
                 }
              } else {
                 values[i] = value;
              }
              i++;
           }
           return values;
        } else {
           return null;
        }
    }
    
   public static class PivotTreeNode extends DefaultMutableTreeNode {
      
      private java.text.Format format;
      
      /** Creates a new instance of ArcoPivotTreeNode */
      public PivotTreeNode() {
      }
      
      /** Creates a new instance of ArcoPivotTreeNode */
      public PivotTreeNode(Object userObject) {
         super(userObject);
      }
      
      public void add(MutableTreeNode newChild) {
         super.add(newChild);
      }
      
      public void setFormat(java.text.Format format) {
         this.format = format;
      }
      
      public Object getFormattedUserObject() {
         Object ret = getUserObject();
         if( ret == null ) {
            return ArcoConstants.NULL_VALUE;
         } else if ( format == null ) {
            return ret;
         } else {
            try {
               return format.format(ret);
            } catch( IllegalArgumentException ilge ) {
               return ArcoConstants.FORMAT_ERROR;
            }
         }
      }
      
      /**
       *  Test  wether a child has an user object
       *  @param  obj  the user object
       *  @return
       */
      public boolean contains(Object obj) {
         if (isLeaf()) {
            return com.sun.grid.arco.Util.equals(obj,userObject);
         } else {
            Iterator childIt = children.iterator();
            while (childIt.hasNext()) {
               PivotTreeNode node = (PivotTreeNode)childIt.next();
               if( com.sun.grid.arco.Util.equals( node.getUserObject(), obj ) ) {
                  return true;
               }
            }
         }
         return false;
      }
      
      /**
       *  get the child node for a user object
       *  @param  userObject  the user object
       *  @return the child node or <code>null</code>
       */
      public TreeNode getChild(Object userObject) {
         if (children != null) {
            Iterator findIt = children.iterator();
            while (findIt.hasNext()) {
               PivotTreeNode node = (PivotTreeNode) findIt.next();
               if( com.sun.grid.arco.Util.equals(node.getUserObject(), userObject ) ) {
                  return node;
               }
            }
         }
         return null;
      }
      
      public void buildChildList(int depth, List childList) {
         if( getLevel() < depth ) {
            // we have not arrived the necessary depth, dive in
            PivotTreeNode node = (PivotTreeNode)getChildAt(0);
            PivotTreeNode ret = null;
            while( node != null ) {
               node.buildChildList(depth, childList);
               node = (PivotTreeNode)node.getNextSibling();
            }
         } else {
            childList.add(this);
         }
      }
      
      public PivotTreeNode getChildAt(int depth, int index ) {
         int [] childIndex = new int[1];
         childIndex[0] = -1;
         return getChildAt(depth, childIndex, index);         
      }
      
      private PivotTreeNode getChildAt(int depth, int[] childIndex, int index) {
         
         if( getLevel() < depth ) {
            // we have not arrived the necessary depth, dive in
            PivotTreeNode node = (PivotTreeNode)getChildAt(0);
            PivotTreeNode ret = null;
            while( node != null ) {
               ret = node.getChildAt(depth, childIndex, index);
               if( ret != null ) {
                  return ret;
               }
               node = (PivotTreeNode)node.getNextSibling();
            }
            throw new IllegalArgumentException("No child[" + index + "] at depth" + depth + " available");
         } else {
            childIndex[0]++;
            if( childIndex[0] == index ) {
               return this;
            } else {
               return null;
            }
         }
       }
   
      private void dump(java.io.PrintWriter pw) {
         StringBuffer indent = new StringBuffer();

         dump(indent, pw);

      }

      public static final String INDENT = "  ";
      private void dump(StringBuffer indent, java.io.PrintWriter pw) {
         int orgIndentLength = indent.length();
         
         pw.print( indent );
         pw.print( "+- ");
         pw.println( getUserObject() );
         
         if( !isLeaf() ) {
            indent.insert(0,INDENT);
            PivotTreeNode node = (PivotTreeNode)getFirstChild();
            while( node != null ) {
               node.dump(indent, pw);
               node = (PivotTreeNode)node.getNextSibling();
            }         
            indent.setLength(orgIndentLength);
         }
      }
   }
}
