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
import java.io.*;

import com.sun.grid.logging.SGELog;


/**
 */
public class PivotTableGenerator {
    private TablePrinter tablePrinter;
    public PivotTableGenerator( PivotModel  pivotModel, TablePrinter tablePrinter ) {
        this.pivotModel = pivotModel;
        this.tablePrinter = tablePrinter;
    }
    
    //needed to create the row header
    private Map rowHeaderObjects = new HashMap();
    
    private PivotModel pivotModel;
    
    
    protected PivotModel getPivotModel() {
        return pivotModel;
    }
    
    /**
     * Returns a new HTML view of the pivot table. Cell entries are right-aligned,
     * no cell padding.Header entries are centered. BorderAttributes decorate the table
     * border. If <code>null</code> the table has no border. Use html tags
     * assignable to the TABLE tag. User must ensure attributes consists of valid html code.
     *
     * @param pw The print writer where the output is written
     */
    public void print( PrintWriter pw ) {
        
        
        int rowMaxDepth = pivotModel.getRowHierachyDepth();
        int colMaxDepth = pivotModel.getColumnHierarchyDepth();
        
        
        SGELog.fine( "colMaxDepth = {0}", colMaxDepth );
        SGELog.fine( "rowMaxDepth = {0}", rowMaxDepth );
        
        
        //root nodes are not visible
        
        //columns--------------------------------------------
        PivotModel.PivotTreeNode  colHeader = pivotModel.getColumnHeader();
        int cs = getMaxCellSpan(colHeader, colMaxDepth);
        
        int dataHierachyDepth = getPivotModel().getDataHierachyDepth();
        if( dataHierachyDepth > 1 ) {
            if( cs == 0 ) {
                cs = dataHierachyDepth;
            } else {
                cs *= dataHierachyDepth;
            }
        }
        tablePrinter.printTableStart( pw, cs + rowMaxDepth );
        
        tablePrinter.printTableHeaderStart( pw );
        int lastNodeCount = 0;
        for(int i=1;  i <= colMaxDepth; i++) {
            //new row in column header
            List nodes = new ArrayList();
            getNodesForLevel(colHeader, i, true, nodes);
            lastNodeCount = nodes.size();
            tablePrinter.printRowStart( pw );
            
            for(int n=0; n < nodes.size(); n++) {
                //first line is different !
                if(i == 1 && n == 0) {
                    if( dataHierachyDepth == 1) {
                        tablePrinter.printHeaderCell( pw, rowMaxDepth, colMaxDepth + 1, pivotModel.getDataElement(0).getName() );
                    } else {
                        tablePrinter.printHeaderCell( pw, rowMaxDepth, colMaxDepth + 1, null );
                    }
                }
                PivotModel.PivotTreeNode  node = (PivotModel.PivotTreeNode )nodes.get(n);
                cs = getMaxCellSpan(node, colMaxDepth);
                if( dataHierachyDepth > 1 ) {
                    if( cs == 0 ) {
                        cs = dataHierachyDepth;
                    } else {
                        cs *= dataHierachyDepth;
                    }
                }
                tablePrinter.printHeaderCell( pw, cs, 1, node.getFormattedUserObject() );
            }
            tablePrinter.printRowEnd( pw );
        }
        tablePrinter.printRowStart( pw );
        if( dataHierachyDepth > 1) {
            for(int i = 0; i < lastNodeCount; i++ ) {
                for(int n = 0; n < dataHierachyDepth; n++ ) {
                    tablePrinter.printHeaderCell(pw, 1, 1, getPivotModel().getDataElement(n).getName());
                }
            }
        }
        tablePrinter.printRowEnd( pw );
        tablePrinter.printTableHeaderEnd( pw );
        colHeader = null;
        
        //rows----------------------------------------------------------------
        //build row headers and cells for the row in question
        PivotModel.PivotTreeNode  rowHeader = pivotModel.getRowHeader();
        //root not visible
        
        tablePrinter.printTableBodyStart( pw );
        int rows = pivotModel.getRowCount();
        for(int row=0; row < rows; row++) {
            tablePrinter.printRowStart(pw);
            //row header for this row
            for(int l=1; l <= rowMaxDepth; l++) {
                RowHeaderObject rho = getRowHeaderNodeForRow(row, l, rowMaxDepth);
                //add this if not null
                if(rho != null) {
                    tablePrinter.printHeaderCell( pw, 1, rho.rowSpan, rho.node.getFormattedUserObject() );
                }
            }
            //add table cells for this row
            printTableCellsForRow( pw, row);
            tablePrinter.printRowEnd(pw);
        }
        tablePrinter.printTableBodyEnd( pw );
        tablePrinter.printTableEnd(pw);
    }
    
    /**
     * Returns a RowHeadrObject for given row, level and max. row header depth.
     * Alert: Must only be invoked for ascending row, level, starting with row = 0, level = 1.
     *
     * @param row table row index
     * @param level row header level. &gt; 0
     * @param maxDepth max depth of the row header
     * @return RowHeaderObject
     */
    private RowHeaderObject getRowHeaderNodeForRow(int row, int level, int maxDepth) {
        //maintain a map for each column of the row header
        Integer iLevel = new Integer(level);
        Map column = (Map)rowHeaderObjects.get(iLevel);
        if(column == null) {
            column = new HashMap();
            rowHeaderObjects.put(iLevel, column);
            List result = new ArrayList();
            getNodesForLevel(pivotModel.getRowHeader(), level, false, result);
            //get row spans for the nodes of this level
            int lastAccumSpan = 0;
            for(int i=0; i < result.size(); i++) {
                PivotModel.PivotTreeNode  node = (PivotModel.PivotTreeNode )result.get(i);
                int span = getMaxCellSpan(node, maxDepth);
                RowHeaderObject rho = new RowHeaderObject();
                rho.node = node;
                rho.rowSpan = span;
                rho.accumSpans = span + lastAccumSpan;
                column.put(new Integer(lastAccumSpan), rho);
                lastAccumSpan = rho.accumSpans;
            }
        }
                /*
                 * try to get a node for the row index in the current level. if we get null
                 * we're done.
                 */
        Integer iRow = new Integer(row);
        //if o is null it was already be added to the html code
        Object o = column.get(iRow);
        return (RowHeaderObject)o;
    }
    
    /**
     * Append cells of given row to html code. Cell entries are right aligned
     * @param b StringBuffer with current html code
     * @param row current row
     */
    private void printTableCellsForRow(PrintWriter pw , int row) {
        int cols = pivotModel.getColumnCount();
        Object [] content = null;
        Object [] value = new Object[1];
        for(int c=0; c < cols; c++) {
            content = pivotModel.getValuesAt(row, c);
            for(int n = 0; n < pivotModel.getDataHierachyDepth(); n++ ) {
                if( content != null ) {
                    value[0] = content[n];
                } else {
                    value[0] = ArcoConstants.NULL_VALUE;
                }
                tablePrinter.printCell( pw, value );
            }
        }
    }
    
    /**
     * recursively calculates the cell span of given node
     * @param node node
     * @param csp cell span container
     * @param maxDepth max. depth
     */
    private void getMaxCellSpan(PivotModel.PivotTreeNode node, CellSpan csp, int maxDepth) {
                /*
                 * If node is on the deepest level, this node's child count
                 * is the value we're looking for
                 */
        int level = node.getLevel();
        //System.out.println("getMaxCellSpan: starting: node=" + node.getUserObject() + " csp=" + csp.span + " level=" + level + " maxDepth=" + maxDepth);
        
        if(level == maxDepth-1) {
            csp.span += node.getChildCount();
        } else {
            
            for(int c=0; c < node.getChildCount(); c++) {
                PivotModel.PivotTreeNode  n = (PivotModel.PivotTreeNode)node.getChildAt(c);
                //System.out.println("getMaxCellSpan recursion with node " + n.getUserObject());
                getMaxCellSpan(n, csp, maxDepth);
            }
        }
        //System.out.println("getMaxCellSpan leaving node=" + node.getUserObject() + " csp=" + csp.span);
    }
    
    /**
     * Returns the amount of cells this node spans. This is the
     * highest resolution of cells this node spans. If node is a
     * column header node returned value is the maximum resolution
     * of columns this node spans. If node is a row header node
     * returned value is the maxumum resolution of rows this
     * node spans. <b>Returned value is always &ge; 1.</b>
     *
     * @param node node to get the cell span for
     * @param max depth of the column and row hierarchy respectively
     * @return Maximum cell span
     */
    private int getMaxCellSpan(PivotModel.PivotTreeNode  node, int maxDepth) {
        //container for the cell span
        CellSpan csp = new CellSpan();
        getMaxCellSpan(node, csp, maxDepth);
        return csp.span == 0 ? 1: csp.span;
    }
    
    
    
    
    
    /**
     * Returns all nodes of given level. Passed node must be in a level less or equals level.
     * If node is null the header node is taken.
     * @param node
     * @param level
     * @param isCols true for column header
     * @return PivotModel.PivotTreeNode [], never null
     */
    private void getNodesForLevel(PivotModel.PivotTreeNode  node, int level, boolean isCols, List result) {
        if(node == null) {
            node = isCols ? pivotModel.getColumnHeader() : pivotModel.getRowHeader();
        }
        int l = node.getLevel();
        if(l == level) {
            PivotModel.PivotTreeNode  parent = (PivotModel.PivotTreeNode )node.getParent();
            if(parent == null) {
                result.add(node);
            } else {
                PivotModel.PivotTreeNode  pp = (PivotModel.PivotTreeNode )parent.getParent();
                if(pp == null) {
                    //just gather children of parent
                    for(int i=0; i < parent.getChildCount(); i++) {
                        result.add(parent.getChildAt(i));
                    }
                } else {
                    for(int i=0; i < pp.getChildCount(); i++) {
                        PivotModel.PivotTreeNode ch = (PivotModel.PivotTreeNode)pp.getChildAt(i);
                        Enumeration e = ch.children();
                        while(e.hasMoreElements()) {
                            result.add(e.nextElement());
                        }
                    }
                }
            }
            return;
        }//we assume current level < target level
        else {
            int cc = node.getChildCount();
            if(cc == 0) //error ?
            {
                SGELog.fine( "current level={0}, target level=1, node = {1} has no children",
                        new Integer( level ), node.getUserObject() );
                return;
            }
            PivotModel.PivotTreeNode  c0 = (PivotModel.PivotTreeNode )node.getChildAt(0);
            getNodesForLevel(c0, level, isCols, result);
        }
    }
    
    /**
     * Keeps usefull information for the html code: the node object, the row span of this
     * node and the accumulated span.
     */
    private static final class RowHeaderObject {
        PivotModel.PivotTreeNode  node;
        int rowSpan;
        int accumSpans;
    }
    
    /**
     * Container class for the cell span
     */
    private static final class CellSpan {
        int span;
    }
}
