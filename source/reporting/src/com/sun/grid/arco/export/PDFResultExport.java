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

import java.io.*;
import java.util.*;
import java.awt.Color;

//JAXP
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.apache.avalon.framework.logger.*;

//Arco
import com.sun.grid.arco.model.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.ExportContext;
import com.sun.grid.arco.ChartManager;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.ResultConverter;
import com.sun.grid.arco.ResultExport;

//iText
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Cell;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;

public class PDFResultExport extends ResultExport {

   private File basedir;
   //needed to create the row header
   /** Creates a new instance of PDFResultExport */
   public PDFResultExport(File basedir) {
      super("application/pdf");
      this.basedir = basedir;
   }

   public void export(ExportContext ctx) throws IOException, TransformerException {
      final Rectangle page = PageSize.A4;

      Document document = new Document(page.rotate());
      try {
         PdfWriter.getInstance(document, ctx.getOutputStream());
         document.open();
         writePdfView(ctx, document);
      } catch (Exception e) {
         exceptionToPdf(e, document);
         throw new IOException(e.getMessage());
      } finally {
         document.close();
      }
   }

   private void exceptionToPdf(Exception e, Document document) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      try {
         final Paragraph pg1 = new Paragraph("Pdf generation exception");
         document.add(pg1);
         final Paragraph pg2 = new Paragraph();
         final Chunk ch = new Chunk(sw.toString());
         ch.setFont(new Font(Font.ITALIC));
         pg2.add(new Phrase(ch));
         pg2.setAlignment(Paragraph.ALIGN_CENTER);
         document.add(pg2);
      } catch (DocumentException de) {
         SGELog.warning(de.getMessage());
         throw new IllegalStateException(de);
      } finally {
         pw.close();
      }
   }

   private void writePdfView(ExportContext ctx, Document document) throws DocumentException, IOException {

      writePdfTitle(ctx, document);

      final com.sun.grid.arco.model.Result result = ctx.getXmlResult();

      ViewConfiguration view = result.getView();
      List visibleElements = null;
      if (view != null) {
         visibleElements = com.sun.grid.arco.Util.getSortedViewElements(view);
      }
      if (visibleElements != null && !visibleElements.isEmpty()) {
         Iterator iter = visibleElements.iterator();
         ViewElement elem = null;

         while (iter.hasNext()) {
            elem = (ViewElement) iter.next();
            if (elem instanceof com.sun.grid.arco.model.Table) {
               writePdfTable(ctx, document);
            } else if (elem instanceof Pivot) {
               writePdfPivot(ctx, document);
            } else if (elem instanceof Graphic) {
               writePdfChart(ctx, document);
            }
         }
      } else {
         writePdfTable(ctx, document);
      }
   }

   private void writePdfTitle(ExportContext ctx, Document document) throws DocumentException {
      final com.sun.grid.arco.model.Result result = ctx.getXmlResult();
      final Chunk chtitle = new Chunk(result.getName(), new Font(Font.BOLD, 20));
      final Phrase ps = new Phrase(chtitle);
      final Paragraph pg1 = new Paragraph();
      pg1.add(ps);
      pg1.setAlignment(Paragraph.ALIGN_CENTER);
      document.add(pg1);
   }

   // --------------------   Table ---------------------------------------------
   public void writePdfTable(ExportContext ctx, Document document) throws DocumentException {

      final com.sun.grid.arco.model.Result res = ctx.getXmlResult();

      final PdfPTable table = new PdfPTable(res.getColumn().size());
      // Write headers
      for (int c = 0; c < res.getColumn().size(); c++) {
         final ResultColumn column = (ResultColumn) res.getColumn().get(c);
         final Phrase phr = new Phrase(column.getName());
         final PdfPCell cell = new PdfPCell(phr);
         cell.setHorizontalAlignment(Element.ALIGN_CENTER);
         cell.setBackgroundColor(Color.LIGHT_GRAY);
         cell.setMinimumHeight(column.getName().length());
         table.addCell(cell);
      }

      for (int r = 0; r < res.getRowCount(); r++) {
         ResultRow row = (ResultRow) res.getRow().get(r);
         for (int c = 0; c < row.getValue().size(); c++) {
            final String value = (String) row.getValue().get(c);
            final Phrase phr = new Phrase(value);
            final PdfPCell cell = new PdfPCell(phr);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
         }
      }
      table.setWidthPercentage(100);
      table.setSplitRows(true);
      table.setSplitLate(false);
      table.setSpacingBefore(15f);
      table.setSpacingAfter(10f);
      table.setHorizontalAlignment(Element.ALIGN_LEFT);
      table.setHeaderRows(1);
      //table.setLockedWidth(true); //Must be set

      //Table is new paragraph
      final Paragraph p = new Paragraph();
      p.add(table);
      document.add(p);
   }

   private void writePdfChart(ExportContext ctx, Document document) throws DocumentException, IOException {

      ByteArrayOutputStream bous = null;
      try {
         ChartManager chartManager = new ChartManager();
         bous = new ByteArrayOutputStream(8192);
         chartManager.writeChartAsPNG(ctx.getQueryResult(), bous, ctx.getLocale());
         bous.flush();
         final Image img = Image.getInstance(bous.toByteArray());
         final Paragraph pg = new Paragraph();
         img.setWidthPercentage(100f);
         img.setXYRatio(1f);
         pg.add(img);
         pg.setAlignment(Paragraph.ALIGN_CENTER);
         document.add(pg);
      } catch (com.sun.grid.arco.chart.ChartException ce) {
         exceptionToPdf(ce, document);
      } finally {
         bous.close();
      }
   }

   private void writePdfPivot(ExportContext ctx, Document document) throws DocumentException {

      PivotModel pivotModel = ctx.getQueryResult().createPivotModel(ctx.getLocale());

      int rowMaxDepth = pivotModel.getRowHierachyDepth();
      int colMaxDepth = pivotModel.getColumnHierarchyDepth();

      //columns--------------------------------------------
      PivotModel.PivotTreeNode colHeader = pivotModel.getColumnHeader();
      int cs = getMaxCellSpan(colHeader, colMaxDepth);
      int dataHierachyDepth = pivotModel.getDataHierachyDepth();

      if (dataHierachyDepth > 1) {
         if (cs == 0) {
            cs = dataHierachyDepth;
         } else {
            cs *= dataHierachyDepth;
         }
      }

      final Table table = new Table(cs + 1);
      table.setPadding(5);

      int lastNodeCount = 0;
      for (int i = 1; i <= colMaxDepth; i++) {
         //new row in column header
         List nodes = new ArrayList();
         getNodesForLevel(colHeader, i, true, nodes, pivotModel);
         lastNodeCount = nodes.size();

         for (int n = 0; n < nodes.size(); n++) {
            //first line is different !
            if (i == 1 && n == 0) {
               if (dataHierachyDepth == 1) {
                  addHeaderCell(rowMaxDepth, colMaxDepth + 1, pivotModel.getDataElement(0).getName().toString(),
                        table);
               } else {
                  addHeaderCell(rowMaxDepth, colMaxDepth + 1, " ", table);
               }
            }

            PivotModel.PivotTreeNode node = (PivotModel.PivotTreeNode) nodes.get(n);
            cs = getMaxCellSpan(node, colMaxDepth);
            if (dataHierachyDepth > 1) {
               if (cs == 0) {
                  cs = dataHierachyDepth;
               } else {
                  cs *= dataHierachyDepth;
               }
            }
            addHeaderCell(cs, 1, node.getFormattedUserObject().toString(), table);
         }
      }

      if (dataHierachyDepth > 1) {
         for (int i = 0; i < lastNodeCount; i++) {
            for (int n = 0; n < dataHierachyDepth; n++) {
               addHeaderCell(1, 1, pivotModel.getDataElement(n).getName(), table);
            }
         }
      }
      colHeader = null;

      //rows----------------------------------------------------------------
      //build row headers and cells for the row in question
      int rows = pivotModel.getRowCount();
      for (int row = 0; row < rows; row++) {
         //row header for this row
         for (int l = 1; l <= rowMaxDepth; l++) {
            RowHeaderObject rho = getRowHeaderNodeForRow(row, l, rowMaxDepth, pivotModel);
            //add this if not null
            if (rho != null) {
               addHeaderCell(1, rho.rowSpan, rho.node.getFormattedUserObject().toString(), table);
            }
         }
         addTableCellsForRow(row, pivotModel, table);
      }

      //Table is new paragraph
      final Paragraph p = new Paragraph();
      p.add(table);
      document.add(p);
   }

   /**
    * Returns the amount of cells this node spans. This is the
    * highest resolution of cells this node spans. If node is a
    * column header, node returned value is the maximum resolution
    * of columns this node spans. If node is a row header node,
    * returned value is the maximum resolution of rows this
    * node spans. <b>Returned value is always &ge; 1.</b>
    *
    * @param node node to get the cell span for
    * @param max depth of the column, row hierarchy respectively
    * @return Maximum cell span
    */
   private int getMaxCellSpan(PivotModel.PivotTreeNode node, int maxDepth) {
      //container for the cell span
      CellSpan csp = new CellSpan();
      getMaxCellSpan(node, csp, maxDepth);
      return csp.span == 0 ? 1 : csp.span;
   }

   /**
    * Returns all nodes of the given level. Passed node must be in a level less or equal level.
    * If node is null the header node is taken.
    * @param node
    * @param level
    * @param isCols true for column header
    * @param result 
    * @param pivotModel the pivot model of the current context
    * @return PivotModel.PivotTreeNode [], never null
    */
   private void getNodesForLevel(PivotModel.PivotTreeNode node, int level, boolean isCols, List result, PivotModel pivotModel) {
      if (node == null) {
         node = isCols ? pivotModel.getColumnHeader() : pivotModel.getRowHeader();
      }
      int l = node.getLevel();
      if (l == level) {
         PivotModel.PivotTreeNode parent = (PivotModel.PivotTreeNode) node.getParent();
         if (parent == null) {
            result.add(node);
         } else {
            PivotModel.PivotTreeNode pp = (PivotModel.PivotTreeNode) parent.getParent();
            if (pp == null) {
               //just gather children of parent
               for (int i = 0; i < parent.getChildCount(); i++) {
                  result.add(parent.getChildAt(i));
               }
            } else {
               for (int i = 0; i < pp.getChildCount(); i++) {
                  PivotModel.PivotTreeNode ch = (PivotModel.PivotTreeNode) pp.getChildAt(i);
                  Enumeration e = ch.children();
                  while (e.hasMoreElements()) {
                     result.add(e.nextElement());
                  }
               }
            }
         }
         return;
      }//we assume current level < target level
      else {
         int cc = node.getChildCount();
         if (cc == 0) //error ?
         {
            SGELog.fine("current level={0}, target level=1, node = {1} has no children",
                  new Integer(level), node.getUserObject());
            return;
         }
         PivotModel.PivotTreeNode c0 = (PivotModel.PivotTreeNode) node.getChildAt(0);
         getNodesForLevel(c0, level, isCols, result, pivotModel);
      }
   }

   /**
    * Append cells of given row to pivot. 
    * @param row current row
    * @param pivotModel - pivotModel of the current context
    * @param table - the iText Table these cell should be added to
    */
   private void addTableCellsForRow(int row, PivotModel pivotModel, Table table) {
      int cols = pivotModel.getColumnCount();
      Object[] content = null;
      Object[] data = new Object[1];
      for (int c = 0; c < cols; c++) {
         content = pivotModel.getValuesAt(row, c);
         for (int n = 0; n < pivotModel.getDataHierachyDepth(); n++) {
            if (content != null) {
               data[0] = content[n];
            } else {
               data[0] = ArcoConstants.NULL_VALUE;
            }
            addCell(data, table);
         }
      }
   }
   
   /**
    * Append cells of given row to pivot table. The content of cells is right aligned.
    * @param content - array of Objects containing the data that should be displayed
    * @param table - the iText Table these cell should be added to
    */
   public void addCell(Object[] content, Table table) {
      String value = null;
      if (content != null) {
         for (int i = 0; i < content.length; i++) {
            value = ResultConverter.objToStr(content[i]);
         }
      } else {
         value = ArcoConstants.NULL_VALUE;
      }
      final Cell cell = new Cell(value);
      cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      table.addCell(cell);
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

      if (level == maxDepth - 1) {
         csp.span += node.getChildCount();
      } else {

         for (int c = 0; c < node.getChildCount(); c++) {
            PivotModel.PivotTreeNode n = (PivotModel.PivotTreeNode) node.getChildAt(c);
            getMaxCellSpan(n, csp, maxDepth);
         }
      }
   }

   /**
    * Returns a RowHeaderObject for given row, level and max. row header depth.
    * Alert: Must only be invoked for ascending row, level, starting with row = 0, level = 1.
    *
    * @param row table row index
    * @param level row header level. &gt; 0
    * @param maxDepth max depth of the row header
    * @param pivotModel - pivotModel of the current context
    * @return RowHeaderObject
    */
   private RowHeaderObject getRowHeaderNodeForRow(int row, int level, int maxDepth, PivotModel pivotModel) {
      Map rowHeaderObjects = new HashMap();
      //maintain a map for each column of the row header
      Integer iLevel = new Integer(level);
      Map column = (Map) rowHeaderObjects.get(iLevel);
      if (column == null) {
         column = new HashMap();
         rowHeaderObjects.put(iLevel, column);
         List result = new ArrayList();
         getNodesForLevel(pivotModel.getRowHeader(), level, false, result, pivotModel);
         //get row spans for the nodes of this level
         int lastAccumSpan = 0;
         for (int i = 0; i < result.size(); i++) {
            PivotModel.PivotTreeNode node = (PivotModel.PivotTreeNode) result.get(i);
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
      return (RowHeaderObject) o;
   }
 
   /**
    * Append header cells to pivot table. The content of cells is centered and background is light gray.
    * @param colSpan - how many columns this cell spans
    * @param rowSpan - how many rows this cell spans
    * @param content - array of Objects containing the data that should be displayed
    * @param table - the iText table that this cell should be added to
    * @param table - the iText Table these cell should be added to
    */
   private void addHeaderCell(int colSpan, int rowSpan, String content, Table table) {
      final Cell cell = new Cell(content);
      cell.setColspan(colSpan);
      cell.setRowspan(rowSpan);
      cell.setHeader(true);
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      cell.setBackgroundColor(Color.LIGHT_GRAY);
      table.addCell(cell);
   }

   /**
    * Keeps usefull information for the html code: the node object, the row span of this
    * node and the accumulated span.
    */
   private static final class RowHeaderObject {

      PivotModel.PivotTreeNode node;
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
