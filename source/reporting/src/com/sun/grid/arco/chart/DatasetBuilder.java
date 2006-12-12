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
package com.sun.grid.arco.chart;

import com.sun.grid.arco.ArcoConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.jfree.data.general.*;
import org.jfree.data.category.*;
import org.jfree.data.xy.*;

import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.model.Pie;
import com.sun.grid.arco.model.Bar;
import com.sun.grid.arco.model.Line;
import com.sun.grid.arco.model.StackedLine;
import com.sun.grid.arco.model.Chart;

/**
 * Description:<br>
 * Buids dataset for bar, pie and line and stacked line charts.<br>
 */
public class DatasetBuilder {

   
   // ---------------- XYDataset -----------------------------------------------
   
   public DefaultPieDataset buildPieDataset(QueryResult result, Chart chart) {
      DefaultPieDataset ret = new DefaultPieDataset();
      if( ArcoConstants.CHART_SERIES_FROM_COL.equals(chart.getSeriesType()) ) {
         buildFromColumns(result, chart, ret);
      } else if ( ArcoConstants.CHART_SERIES_FROM_ROW.equals(chart.getSeriesType()) ) {
         buildFromRows(result, chart, ret);
      } else {
         throw new IllegalStateException("unknown series type " + chart.getSeriesType());
      }
      return ret;
   }
   
   private void buildFromColumns(QueryResult result, Chart chart, DefaultPieDataset ds) {
      List columnList = chart.getSeriesFromColumns().getColumn();
      
      int [] columnIndex = new int[columnList.size()];

      int col = 0;
      
      for( col = 0; col < columnIndex.length; col++ ) {
         columnIndex[col] = result.getColumnIndex((String)columnList.get(col));
      }
      
      Object yValue = null;
      Number y = null;
      String seriesName = null;
      

      QueryResult.RowIterator iter = result.rowIterator();
      while( iter.next() ) {
         for(col = 0; col < columnIndex.length; col++ ) {
            yValue = (Comparable)iter.getValue(columnIndex[col]);
            y =  getValueForType(yValue);
            seriesName = (String)columnList.get(col);
            if( seriesName == null ) {
               seriesName = ArcoConstants.NULL_VALUE;
            }
            ds.setValue(seriesName,y);
         }
      }      
   }
   
   private void buildFromRows(QueryResult result, Chart chart, DefaultPieDataset ds) {
      String seriesCol = chart.getSeriesFromRow().getLabel();
      int seriesIndex = result.getColumnIndex(seriesCol);
      
      String valueCol = chart.getSeriesFromRow().getValue();
      int valueIndex = result.getColumnIndex(valueCol);
      
      Object series = null;      
      Object yValue = null;
      Number y = null;
      String seriesName = null;
      
      QueryResult.RowIterator iter = result.rowIterator();
      while( iter.next() ) {         
         yValue = iter.getValue(valueIndex);
         series = iter.getValue(seriesIndex);         
         y = getValueForType(yValue);
         if( series == null ) {
            seriesName = ArcoConstants.NULL_VALUE;
         } else {
            seriesName = series.toString();
         }
         ds.setValue(seriesName,y);
      }      
   }
   
   public CategoryTableXYDataset buildXYDataset(QueryResult result, Chart chart)
      throws ChartException {
      
      if( !chart.isSetXaxis() ) {
         throw new ChartException("chart.noXAxisDefined");
      }
      
      CategoryTableXYDataset ret  = new CategoryTableXYDataset();
      
      if( ArcoConstants.CHART_SERIES_FROM_COL.equals(chart.getSeriesType()) ) {
         buildFromColumns(result, chart, ret);
      } else if ( ArcoConstants.CHART_SERIES_FROM_ROW.equals(chart.getSeriesType()) ) {
         buildFromRows(result, chart, ret);
      } else {
         throw new IllegalStateException("unknown series type " + chart.getSeriesType());
      }
      return ret;
      
   }
   
   private void buildFromColumns(QueryResult result, Chart chart, CategoryTableXYDataset ds) {
      String xAxis = chart.getXaxis();
         
      int xAxisIndex = result.getColumnIndex(xAxis);
      
      List columnList = chart.getSeriesFromColumns().getColumn();
      
      int [] columnIndex = new int[columnList.size()];

      int col = 0;
      
      for( col = 0; col < columnIndex.length; col++ ) {
         columnIndex[col] = result.getColumnIndex((String)columnList.get(col));
      }
    
      Object xValue = null;
      Object yValue = null;
      Number x = null;
      Number y = null;
      String seriesName = null;
      

      QueryResult.RowIterator iter = result.rowIterator();
      while( iter.next() ) {
         xValue = (Comparable)iter.getValue(xAxisIndex );         
         x = getValueForType(xValue);
         for(col = 0; col < columnIndex.length; col++ ) {
            yValue = (Comparable)iter.getValue(columnIndex[col]);
            y =  getValueForType(yValue);
            seriesName = (String)columnList.get(col);
            if( seriesName == null ) {
               seriesName = ArcoConstants.NULL_VALUE;
            }
            ds.add(x,y, seriesName, false);
         }
      }      
   }
   
   private void buildFromRows(QueryResult result, Chart chart, CategoryTableXYDataset ds) {
      String xAxis = chart.getXaxis();
      int xAxisIndex = result.getColumnIndex(xAxis);
      
      String seriesCol = chart.getSeriesFromRow().getLabel();
      int seriesIndex = result.getColumnIndex(seriesCol);
      
      String valueCol = chart.getSeriesFromRow().getValue();
      int valueIndex = result.getColumnIndex(valueCol);
      
      Object series = null;      
      Object xValue = null;
      Object yValue = null;
      Number x = null;
      Number y = null;
      String seriesName = null;
      
      QueryResult.RowIterator iter = result.rowIterator();
      while( iter.next() ) {         
         xValue = iter.getValue(xAxisIndex);         
         yValue = iter.getValue(valueIndex);
         series = iter.getValue(seriesIndex);
         x = getValueForType(xValue);
         y = getValueForType(yValue);
         if( series == null ) {
            seriesName = ArcoConstants.NULL_VALUE;
         } else {
            seriesName = series.toString();
         }
         ds.add(x, y, seriesName, false);
      }      
   }
   
   // ---------------- CategoryDataSet -----------------------------------------

   public CategoryDataset buildCategoryDataset(QueryResult result, Chart chart ) 
     throws ChartException {
      
      if( !chart.isSetXaxis() ) {
         throw new ChartException("chart.noXAxisDefined");
      }
      
      DefaultCategoryDataset ret = new DefaultCategoryDataset();
      
      if( ArcoConstants.CHART_SERIES_FROM_COL.equals(chart.getSeriesType()) ) {
         buildFromColumns(result, chart, ret);
      } else if ( ArcoConstants.CHART_SERIES_FROM_ROW.equals(chart.getSeriesType()) ) {
         buildFromRows(result, chart, ret);
      } else {
         throw new IllegalStateException("Unknown series type " + chart.getSeriesType() );
      }
      return ret;
   }
   
   private void buildFromRows( QueryResult result, Chart chart, DefaultCategoryDataset ds) {
      String xAxis = chart.getXaxis();      
      
      int xAxisIndex = result.getColumnIndex(xAxis);
      
      String seriesCol = chart.getSeriesFromRow().getLabel();
      int seriesIndex = result.getColumnIndex(seriesCol);
      
      String valueCol = chart.getSeriesFromRow().getValue();
      int valueIndex = result.getColumnIndex(valueCol);
      
      Comparable rowKey = null;
      Comparable colKey = null;
      Object value = null;
      
      QueryResult.RowIterator iter = result.rowIterator();
      while( iter.next() ) {
         rowKey = (Comparable)iter.getValue(seriesIndex); 
         if( rowKey == null ) {
            rowKey = ArcoConstants.NULL_VALUE;
         }
         colKey = (Comparable)iter.getValue(xAxisIndex);
         if( colKey == null ) {
            colKey = ArcoConstants.NULL_VALUE;
         }
         value = iter.getValue(valueIndex );
         ds.addValue(getValueForType(value), rowKey, colKey);
      }      
   }
   
   private void buildFromColumns( QueryResult result, Chart chart, DefaultCategoryDataset ds)
     throws ChartException {
      
      
      String xAxis = chart.getXaxis();
         
      int xAxisIndex = result.getColumnIndex(xAxis);
      
      List columnList = chart.getSeriesFromColumns().getColumn();
      
      int [] columnIndex = new int[columnList.size()];

      int col = 0;
      
      for( col = 0; col < columnIndex.length; col++ ) {
         columnIndex[col] = result.getColumnIndex((String)columnList.get(col));
      }
      
      Comparable rowKey = null;
      Comparable colKey = null;
      Object value = null;

      QueryResult.RowIterator iter = result.rowIterator();
      while( iter.next() ) {
         rowKey = (Comparable)iter.getValue(xAxisIndex );  
         if(rowKey == null ) {
            rowKey = ArcoConstants.NULL_VALUE;
         }
         for(col = 0; col < columnIndex.length; col++ ) {
            value = (Comparable)iter.getValue(columnIndex[col]);
            colKey = (Comparable)columnList.get(col);
            if(colKey == null) {
               colKey = ArcoConstants.NULL_VALUE;
            }
            ds.addValue(getValueForType(value), rowKey, colKey);
         }
      }
   }
   
   
   
   
   /**
    * createLineChartCreates a dataset for a pie chart.
    * @param result result model
    * @param pie    the pie graph configuration
    * @return PieDataset
    */
   public PieDataset buildPieChartDataset(QueryResult result, Pie pie)
      throws ChartException {
      CategoryDataset d = buildBarChartDataset(result, pie.getYaxis(), pie.getXaxis());
      
      org.jfree.util.TableOrder extract = org.jfree.util.TableOrder.BY_COLUMN;
      PieDataset p = new CategoryToPieDataset(d, extract, 0);
      return p;
   }
   
   /**
    * Creates a dataset for a bar chart
    * @param result
    * @param bar the bar configuration
    * @return CategoryDataset
    */
   public CategoryDataset buildBarChartDataset(QueryResult result, Bar bar)
      throws ChartException {
      
      List xaxis = bar.getXaxis();
      
      
      switch( xaxis.size() ) {
         case 1:
            String type = (String)xaxis.get(0);
            return buildBarChartDataset(result, bar.getYaxis(), type);
         case 2:
         {
            String types [] = new String [] {
               (String)xaxis.get(0), (String)xaxis.get(1)
            };
            return buildBarChartDataset(result, types[0], bar.getYaxis(), types[1]);
         }
         case 0:
            throw new ChartException("A bar chart needs at least 1 type definition");
         default:
         {
            String types [] = new String[xaxis.size()];
            xaxis.toArray(types);
            return this.buildBarChartDataset(result,types,bar.getYaxis());
         }
      }
      
   }
   
   /**
    * Creates a dataset for a bar chart with 1 category (type).
    * @param result
    * @param ydata header for the value column
    * @param type header for the type column
    * @return CategoryDataset
    */
   private CategoryDataset buildBarChartDataset(QueryResult result, String ydata, String type)
      throws ChartException {
      List list = new ArrayList(result.getRowCount());
      updateList(result, ydata, type, list);
      CategoryDataset set = createDataset(list);
      return set;
   }
   
   /**
    * Creates a dataset for a bar chart with more then 1 category
    * @param result result model
    * @param xdata Column header for the categories
    * @param ydata Column header for the values
    * @param type Column header for the types
    * @return CategoryDataset
    */
   private  CategoryDataset buildBarChartDataset(QueryResult result, String xdata, String ydata, String type)
      throws ChartException {
      SGELog.fine( "entry xdata = {0}, ydata = {1}, type = {2}", xdata, ydata, type );
      List list = new ArrayList(result.getRowCount());
      updateList(result, xdata, ydata, type, list);
      CategoryDataset set = createDataset(list);
      SGELog.fine( "exit" );
      return set;
   }
   
   
   public CategoryDataset buildLineChartDataset(QueryResult result, Line line) 
      throws ChartException {
      return buildLineChartDataset(result, line.getXaxis(), line.getYaxis(),
                                   line.getType());
   }
   
   /**
    * Creates a dataset for a line chart
    * @param result result model
    * @param xdata Column header for x axis values
    * @param ydata Column header for y axis values
    * @param type Column header for types
    * @return CategoryDataset
    */
   private CategoryDataset buildLineChartDataset(QueryResult result, String xdata, String ydata, String type)
     throws ChartException  {
      SGELog.fine( "entry xdata = {0}, ydata = {1}, type = {2}", xdata, ydata, type  );
      CategoryDataset ret = buildBarChartDataset(result, xdata, ydata, type);
      SGELog.fine( "exit"  );
      return ret;
   }
   
   public TableXYDataset buildXYChartDataset(QueryResult result, Line line) {
      return buildXYChartDataset(result, line.getXaxis(), line.getYaxis(), line.getType() );
   }
   
   private TableXYDataset buildXYChartDataset( QueryResult result, String xdata, String ydata, String series ) {
      
      CategoryTableXYDataset ret  = new CategoryTableXYDataset();
      
      int xIndex = result.getColumnIndex( xdata );
      int yIndex = result.getColumnIndex( ydata );
      int seriesIndex = result.getColumnIndex( series );
      
      Number xValue = null;
      Number yValue = null;
      Comparable seriesObj = null;
      for( int i = 0; i < result.getRowCount(); i++ ) {
         xValue = getValueForType( result.getValue( i, xIndex ) );
         yValue = getValueForType( result.getValue( i, yIndex ) );
         seriesObj = (Comparable)result.getValue( i, seriesIndex );
         ret.add( xValue, yValue, seriesObj.toString(), false );
      }
      return ret;
   }
   
   
   /**
    * Creates a result set fore a bar chart. It may consist of more than 1 category
    * @param result result model
    * @param xdata Column header for x values
    * @param ydata Column header for y values
    * @param type Column header for type
    * @return CategoryDataset
    */
   private  CategoryDataset buildBarChartDataset(QueryResult result, String xdata[], String ydata)
      throws ChartException {
      
      List list = new ArrayList(result.getRowCount()*xdata.length);
      updateList(result, xdata, ydata, list);
      CategoryDataset set = createDataset(list);
      return set;
   }
   
   /**
    * Fills and sorts given list with result from given table model
    * Invoked for pie charts and bar charts with 1 category
    * Throws a ChartException in case of error
    */
   private void updateList(QueryResult result, String ydata, String type, List list)
     throws ChartException {
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "entry rowCount={0},ydata = {1}",
                 new Integer( result.getRowCount()), ydata );
      }
      
      int tc = result.getColumnIndex(type);
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "column index for type {0} = {1}", type, new Integer( tc ) );
      }
      
      int yc = result.getColumnIndex(ydata);
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "column index for ydata {0} = {1}", ydata, new Integer( yc ) );
      }
      
      Class datatype = result.getColumnClass(yc);
      SGELog.fine( "datatype for ydata {0} is {1}", ydata, datatype );
      
      int r = 0;
      
      try {
         for(; r < result.getRowCount(); r++) {
            //value must be a Number or a Date
            Object o = result.getValue(r, yc);
            Number n = getValueForType(o);
            if(n == null) //error
            {
               throw new ChartException("Wrong data type. Expected: Number or Date. Found: "
                       + datatype.getName()
                       + "\nRow=" + r
                       + " ydata=" + ydata
                       + "\ntype=" + type);
            }
            Comparable series = (Comparable)result.getValue(r, tc);
            list.add(new DataObject(n, null, series));
         }//end for
      } catch(Exception ex3) {
         String message = "DatasetBuilder:Failed creating chart dataset.\nRow=" + r + " xdata=" + type + " ydata=" + ydata;
         throw new ChartException(message, ex3);
      }
      SGELog.fine( "exit" );
   }
   
   
   
   /**
    * Fills and sorts given list with data from given table model
    * Invoked for bar charts with more than 1 category
    * Throws a ChartException in case of error
    */
   private void updateList(QueryResult result, String[] xdata, String ydata, List list)
     throws ChartException {
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "entry rowCount={0},ydata = {1}",
                 new Integer( result.getRowCount()), ydata );
      }
      
      int[] cc = new int[xdata.length];
          /*
           * JFreeChart Dataset expects Number
           */
      Number values[] = new Number[result.getRowCount()];
      
      for(int i=0; i < cc.length; i++) {
         cc[i] = result.getColumnIndex(xdata[i]);
         if( SGELog.isLoggable( Level.FINE ) ) {
            SGELog.fine( "column index for xdata {0} = {1}", xdata[i], new Integer(cc[i]) );
         }
      }
      
      int yc = result.getColumnIndex(ydata);
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "column index for ydata {0} = {1}", ydata, new Integer( yc ) );
      }
      
      //get the data type
      Class dataType = result.getColumnClass(yc);
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "datatype for ydata {0} is {1}", ydata, dataType.getName() );
      }
      
      int rr = 0;
      int ii = 0;
      String sc = "";
      try {
         //scan categories
         for(int i=0; i < cc.length; i++) {
            ii = i;
            //scan values for each category
            for(int r=0; r < result.getRowCount(); r++) {
               rr = r;
               //value must be a Number or a Date
               if(values[r] == null) {
                  Object o = result.getValue(r, yc);
                  Number n = getValueForType(o);
                  if(n == null)//error
                  {
                     throw new ChartException("Wrong data type. Expected: Number or Date. Found: "
                             + dataType.getName()
                             + "\nRow=" + r
                             + " xdata=" + xdata[ii]
                             + " ydata=" + ydata);
                  }
                  values[r] = n;
               }
               //series must be a Comparable
               Comparable series = (Comparable)result.getValue(r, cc[i]);
               sc = xdata[i];
                                        /*
                                         * Siehe BarRenderer ! der effekt, dass series je category sehr
                                         * gepresst aussehen und der abstand zwischen den categories sehr gross
                                         * ist, ergibt sich daraus wie BarRenderer diese werte berechnet.
                                         * alternativ alle series zu 1 category zusammenfassen, oder den renderer
                                         * anpassen
                                         */
               //list.add(new DataObject(n, sc, series));
               list.add(new DataObject(values[r], "", series));
            }
         }
      } catch(Exception ex3) {
         String message = "Failed creating chart dataset."
                 + "\nRow=" + rr
                 + "\nColumn=" + ii
                 + "\nxdata=" + sc
                 + "\nydata=" + ydata;
         throw new ChartException(message, ex3);
      }
      SGELog.fine( "exit" );
   }
   
   
   public static final Number NULL_NUMBER = new Integer(0);
   /**
    * Returns a value according to given data type. If data type is Number,
    * returns Number. If data type is Date, converts Date to milli sec.
    * and returns Number. Returns null in any other case.
    * @param o source value
    * @return Converted value, Number or null
    */
   private Number getValueForType(Object o) {
      Number n = null;
      if( o != null ) {
         if(Number.class.isAssignableFrom(o.getClass())) {
            n = (Number)o;
         } else if(Date.class.isAssignableFrom(o.getClass())) {
            Date d = (Date)o;
            n = new Long(d.getTime());
         } else {
            n = new ObjectNumber(o);
         }
      }
      return n;
   }
   
   static class ObjectNumber extends Number {
      Object obj;
      public ObjectNumber(Object obj) {
         this.obj = obj;
      }
      public String toString() {
         return obj.toString();
      }
      public double doubleValue() {
         return intValue();
      }

      public float floatValue() {
         return intValue();
      }

      public int intValue() {
         return obj.hashCode();
      }

      public long longValue() {
         return intValue();
      }
      
      
   }
   
   /**
    * Fills and sorts given list with data from given table model.
    *
    * Invoked for line charts and bar charts with more than 1 category
    * Throws a ChartException in case of an error
    */
   private  void updateList(QueryResult result, String xdata, String ydata, String type, List list)
     throws ChartException {
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "entry rowCount={0}, xdata = {1}, ydata = {2}",
                 new Integer( result.getRowCount() ), xdata, ydata );
      }
      //collect and sort data for each type
      //check categories
      int cc  = result.getColumnIndex(xdata);
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "category column index for xdata {0} is {1}", xdata, new Integer( cc ) );
      }
      
      //type column
      int tc = result.getColumnIndex(type);
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "type column index for type {0} is {1}", type, new Integer( tc ) );
      }
      
      //value column
      int vc = result.getColumnIndex(ydata);
      if( SGELog.isLoggable( Level.FINE ) ) {
         SGELog.fine( "value column index for ydata {0} is {1}", ydata, new Integer( vc ) );
      }
      
      
      //data type of the value axis
      Class dataType = result.getColumnClass(vc);
      
      SGELog.fine( "datatype is {0}", dataType.getName() );
      
      int r = 0;
      
      try {
         for(; r < result.getRowCount(); r++) {
            //value must represent a Number or a Date
            if( r % 1000 == 0 ) {
               if( SGELog.isLoggable( Level.FINE ) ) {
                  SGELog.fine( "process row " + r );
               }
            }
            Object o = result.getValue(r, vc);
            Number n = getValueForType(o);
            if(n == null)//error
            {
               throw new ChartException("Wrong data type. Expected: Number or Date. Found: "
                       + dataType.getName()
                       + "\nRow=" + r
                       + " xdata=" + xdata
                       + " ydata=" + ydata
                       + "\ntype=" + type);
            }
            
            //category, series must be a Comparable
            Comparable category = cc < 0 ? null : (Comparable)result.getValue(r, cc);
            Comparable series = (Comparable)result.getValue(r, tc);
            list.add(new DataObject(n, category, series));
         }//end for
         SGELog.fine( "exit" );
      } catch(Exception ex3) {
         String message = "Failed creating chart dataset.\nRow="
                 + "\nRow=" + r
                 + " cc=" +cc
                 + " tc=" + tc
                 + " vc=" + vc;
         throw new ChartException(message, ex3);
      }
   }
   
   //put values into category set
   private  DefaultCategoryDataset createDataset(List list) {
      SGELog.fine( "entry" );
      DefaultCategoryDataset dataset = new DefaultCategoryDataset();
      int r = 0;
      for(Iterator it=list.iterator();it.hasNext();) {
         if( r % 1000 == 0  ) {
            SGELog.fine( "process row " + r );
         }
         DataObject d = (DataObject)it.next();
         dataset.addValue(d.value, d.series, d.category == null ? "" : d.category);
         r++;
      }
      SGELog.fine( "exit" );
      return dataset;
   }
   
   
   public CategoryDataset buildStackedLineChart(QueryResult result, StackedLine stackedLine ) 
     throws ChartException {
      return buildStackedLineChart(result, stackedLine.getXaxis(), stackedLine.getYaxis(),
                                   stackedLine.getType());
   }
   
   /**
    * Creates a dataset for a stacked line chart. Line N ist "stacked" upon line N-1.
    * @param result result model
    * @param xdata Column header for x values
    * @param ydata Column header for y values
    * @param type Column header for types
    * @return CategoryDataset
    */
   private  CategoryDataset buildStackedLineChart(QueryResult result, String xdata, String ydata, String type)
      throws ChartException {
      List list = new ArrayList(result.getRowCount());
      updateList(result, xdata, ydata, type, list);
      DefaultCategoryDataset set = createDataset(list);
      
      //build stacked values
      int cols = set.getColumnCount();
      int rows = set.getRowCount();
      for(int c=0; c < cols; c++) {
         Comparable colKey = set.getColumnKey(c);
         for(int r=1; r < rows; r++) {
            Number o = set.getValue(r, c);
            if(o == null) {
               continue; //nothing to do
            }
            
            double n1 = o.doubleValue();
            o = set.getValue(r-1, c);
            if(o == null) {
               continue; //nothing to add
            }
            double n0 = o.doubleValue();
            n1 += n0;
            
            
            Comparable rowKey = set.getRowKey(r);
            set.setValue(n1, rowKey, colKey);
         }
      }
      return set;
   }
   
   /**
    * Returns whether the series are inverse sorted for a given chart type
    * @param type Chart type
    * @return <code>true</code> for stacked line chart, else <code>false</code>
    */
   public static boolean isInvert(ChartTypes type) {
      return type == ChartTypes.STACKED_LINE_CHART;
   }
   
   private static final class DataObject {
      Number value;
      Comparable category;
      Comparable series; //the type
      
      DataObject(Number value, Comparable category, Comparable series) {
         this.value = value;
         this.category = category;
         this.series = series;
      }
   }
   
}
