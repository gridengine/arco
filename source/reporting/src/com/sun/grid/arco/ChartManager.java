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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.PieDataset;
import org.jfree.chart.ChartPanel;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.chart.DatasetBuilder;
import com.sun.grid.arco.chart.ChartException;
import com.sun.grid.arco.chart.XDateAxis;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;

public class ChartManager {
   
   public static final int DEFAULT_CHART_WIDTH = 600;
   public static final int DEFAULT_CHART_HEIGHT = 400;
   
   private Map factoryMap = new HashMap();
   
   public ChartManager() {
      reg( ArcoConstants.CHART_TYPE_BAR, new BarGraphFactory());
      reg( ArcoConstants.CHART_TYPE_BAR_3D, new BarGraphFactory(TYPE_3D));
      reg( ArcoConstants.CHART_TYPE_BAR_STACKED, new BarGraphFactory(TYPE_STACKED));      
      reg( ArcoConstants.CHART_TYPE_LINE, new LineGraphFactory() );
      reg( ArcoConstants.CHART_TYPE_LINE_STACKED, new LineGraphFactory(true) );
      reg( ArcoConstants.CHART_TYPE_PIE, new PieGraphFactory() );
      reg( ArcoConstants.CHART_TYPE_PIE_3D, new PieGraphFactory(TYPE_3D) );
   }
   
   private void reg( String type, GraphFactory factory) {
      factoryMap.put(type, factory);
   }
   
   private GraphFactory getFactory(Chart chart) throws ChartException  {
      GraphFactory ret = (GraphFactory)factoryMap.get(chart.getType());      
      if( ret == null ) {
         throw new ChartException("Unknown chart type " + chart.getType());
      }
      return ret;
   }
   
   
   private DatasetBuilder dsBuilder = new DatasetBuilder();
   
   public void writeChartAsPNG(QueryResult result, java.io.OutputStream out, Locale locale )
      throws ChartException, IOException {
      
      try {
         JFreeChart chart = createChart(result, locale);

         ChartPanel chartPanel = new ChartPanel( chart );

         chartPanel.setOpaque(true);
         chartPanel.setBackground(java.awt.Color.WHITE );


         java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                 DEFAULT_CHART_WIDTH,DEFAULT_CHART_HEIGHT, 
                 java.awt.image.BufferedImage.TYPE_INT_BGR );

         chart.draw( bi.createGraphics() , 
                     new java.awt.Rectangle(0,0,DEFAULT_CHART_WIDTH,DEFAULT_CHART_HEIGHT));

         com.sun.jimi.core.Jimi.putImage("image/png", bi, out );
         
         out.flush();
      } catch( com.sun.jimi.core.JimiException je ) {
         throw new ChartException("Can't write png: " + je.getMessage(), je );         
      }
   }

   public void writeChartAsJPG(QueryResult result, java.io.OutputStream out, Locale locale ) 
      throws ChartException, IOException  {
      JFreeChart chart = createChart(result, locale);
      
      ChartPanel chartPanel = new ChartPanel( chart );

      chartPanel.setOpaque(true);
      chartPanel.setBackground(java.awt.Color.WHITE );
      
      java.awt.Dimension dim = chartPanel.getPreferredSize();

      org.jfree.chart.ChartUtilities.writeChartAsJPEG( out, chart, dim.width, dim.height );
      out.flush();
      
   }
   
   
   
   private JFreeChart createChart(QueryResult result, Locale locale) throws ChartException {
      
      Graphic graph = result.getQuery().getView().getGraphic();
      JFreeChart ret = null;
      
      boolean legend = graph.isLegendVisible();
      boolean tooltips = false;
      boolean urls = false;
           
      String title = null;
      
      if( graph.isSetChart() ) {

         Chart chart = graph.getChart();
         
         String type = chart.getType();
         
         GraphFactory factory = getFactory(chart);
         
         ret = factory.createChart(result, graph, locale);
         
      } else if( graph.isSetBar() ) {
         Bar bar = graph.getBar();
         List xAxis = bar.getXaxis();
         
         String categoryLabel = null;
         if( xAxis.size() == 1 ) {
            categoryLabel = xAxis.get(0).toString();
         } else {
            tooltips = true;
         }
         
         String valueAxisLabel = bar.getYaxis();
         CategoryDataset dataset = dsBuilder.buildBarChartDataset(result,bar);
         ret = ChartFactory.createBarChart(title,categoryLabel, valueAxisLabel, dataset
                 , PlotOrientation.VERTICAL, legend, tooltips, urls );         
      } else if ( graph.isSetLine() ) {
         Line line = graph.getLine();
         
         TableXYDataset dataset = dsBuilder.buildXYChartDataset(result, line );
         String xAxisLabel = line.getXaxis();
         String yAxisLabel = line.getYaxis();
         
         PlotOrientation orientation = PlotOrientation.VERTICAL;
         
         ret = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, 
                 orientation,  legend, tooltips, urls);

         int xAxisIndex = result.getColumnIndex(xAxisLabel);
         Class xAxisClass = result.getColumnClass(xAxisIndex);
         
         if( Date.class.isAssignableFrom(xAxisClass) ) {
            ret.getXYPlot().setDomainAxis( new XDateAxis( xAxisLabel, locale ) );
         }
         
      } else if ( graph.isSetPie() ) {
         Pie pie = graph.getPie();
         PieDataset dataset = dsBuilder.buildPieChartDataset(result, pie );
         
         ret = ChartFactory.createPieChart(title,dataset,legend, tooltips, urls);
         
      } else if ( graph.isSetStackedline() ) {
         
         StackedLine stackedLine = graph.getStackedline();
         
         CategoryDataset dataset = dsBuilder.buildStackedLineChart(result, stackedLine);
         
         PlotOrientation orientation = PlotOrientation.VERTICAL;
         
         String domainAxisLabel = stackedLine.getXaxis();
         String rangeAxisLabel = stackedLine.getYaxis();
         
         ret = ChartFactory.createStackedBarChart(title, domainAxisLabel, rangeAxisLabel, dataset, 
                 orientation, legend, tooltips, urls);
         
      } else {
         throw new IllegalStateException("Not graph type is set");
      }
      ret.setBackgroundPaint(java.awt.Color.WHITE);
      
      return ret;
   }
   
   interface GraphFactory {      
      
      
      public abstract JFreeChart createChart(QueryResult result, Graphic graph,  Locale locale)
        throws ChartException;
      
   }

   
   class LineGraphFactory implements GraphFactory {
      
      private boolean stacked;
      
      public LineGraphFactory() {
         this(false);
      }
      
      public LineGraphFactory(boolean stacked) {
         this.stacked = stacked;
      }
      
      public JFreeChart createChart(QueryResult result, Graphic graph, Locale locale)
        throws ChartException {
         
         Chart chart = graph.getChart();
         TableXYDataset ds = dsBuilder.buildXYDataset(result,chart);
         
         String xAxisLabel = chart.getXaxis();
         String yAxisLabel = null;
         
         if( ArcoConstants.CHART_SERIES_FROM_ROW.equals(chart.getSeriesType()) && chart.isSetSeriesFromRow() ) {
            yAxisLabel = chart.getSeriesFromRow().getValue();
         }
         JFreeChart ret = null;
         
         PlotOrientation or = PlotOrientation.VERTICAL;
         
         if( stacked ) {
            ret = ChartFactory.createStackedXYAreaChart(null, xAxisLabel, yAxisLabel, ds, or, 
                                                        graph.isLegendVisible(), false, false);
         } else {
            ret = ChartFactory.createXYLineChart(null, xAxisLabel, yAxisLabel, ds, 
                          or, graph.isLegendVisible(), false, false);         
         }
         
         int xAxisIndex = result.getColumnIndex(xAxisLabel);
         Class xAxisClass = result.getColumnClass(xAxisIndex);
         
         // Fix the axises
         if( Date.class.isAssignableFrom(xAxisClass) ) {
            ret.getXYPlot().setDomainAxis( new XDateAxis( xAxisLabel, locale ) );
         }
         
         int yAxisIndex = -1;
         if( ArcoConstants.CHART_SERIES_FROM_ROW.equals(chart.getSeriesType()) && chart.isSetSeriesFromRow() ) {
            yAxisIndex = result.getColumnIndex(chart.getSeriesFromRow().getValue());
         } else {            
            List columnList = chart.getSeriesFromColumns().getColumn();            
            yAxisIndex = result.getColumnIndex((String)columnList.get(0));
         }

         Class yAxisClass = result.getColumnClass(yAxisIndex);
         if( Date.class.isAssignableFrom(yAxisClass)) {
            ret.getXYPlot().setRangeAxis(new XDateAxis(yAxisLabel, locale));
         }         
         
         return ret;
      }
      
   }  
   
   public static final int TYPE_NORMAL  = 0;
   public static final int TYPE_STACKED = 1;
   public static final int TYPE_3D      = 2;
   
   class BarGraphFactory implements GraphFactory {

      private int type;
      
      public BarGraphFactory() {
         this( TYPE_NORMAL );
      }
      public BarGraphFactory(int type) {
         this.type = type;
      }

      
      public JFreeChart createChart(QueryResult result, Graphic graph, Locale locale)
        throws ChartException {
         
         Chart chart = graph.getChart();
         
         
         String xAxis = chart.getXaxis();
         Class xAxisClass = result.getColumnClass(xAxis);
         
         String categoryAxisLabel = null;
         String valueAxisLabel = null;
         
         if( ArcoConstants.CHART_SERIES_FROM_ROW.equals(chart.getSeriesType()) && chart.isSetSeriesFromRow() ) {
            categoryAxisLabel = chart.getSeriesFromRow().getLabel();
            valueAxisLabel = chart.getSeriesFromRow().getValue();
         }
         
         PlotOrientation or = PlotOrientation.VERTICAL;
         
         JFreeChart ret = null;
         
         switch( type ) {
            case TYPE_STACKED: {
               CategoryDataset ds = dsBuilder.buildCategoryDataset(result, chart);
               ret = ChartFactory.createStackedBarChart(null, categoryAxisLabel, valueAxisLabel, ds
                       , or, graph.isLegendVisible(), false, false);
            }
            break;
            case TYPE_3D: {
               CategoryDataset ds = dsBuilder.buildCategoryDataset(result, chart);
               ret = ChartFactory.createBarChart3D(null, categoryAxisLabel, valueAxisLabel, ds
                       , or, graph.isLegendVisible(), false, false );
            }
            break;
            default: {
               CategoryDataset ds = dsBuilder.buildCategoryDataset(result, chart);
               ret = ChartFactory.createBarChart(null, categoryAxisLabel, valueAxisLabel, ds
                       , or, graph.isLegendVisible(), false, false );
            }
         }
         return ret;
      }      
   }
   
   
   class PieGraphFactory implements GraphFactory {
      private int type;
              
      public PieGraphFactory() {
         this(TYPE_NORMAL);
      }
      
      public PieGraphFactory(int type) {
         this.type = type;
      }
             
      public JFreeChart createChart(QueryResult result, Graphic graph, Locale locale)
        throws ChartException {

         JFreeChart ret = null;
         Chart chart = graph.getChart();
         if( chart.isSetXaxis() && chart.getXaxis().length() > 0 ) {
            CategoryDataset ds = dsBuilder.buildCategoryDataset(result, chart);
            org.jfree.util.TableOrder order = org.jfree.util.TableOrder.BY_COLUMN;

            switch( type ) {
               case TYPE_3D:
               ret = ChartFactory.createMultiplePieChart3D(null, ds, order , 
                            graph.isLegendVisible(), false, false );
               break;
               default:
               ret = ChartFactory.createMultiplePieChart(null, ds, order , 
                            graph.isLegendVisible(), false, false );

            }
            if(!chart.isLabelsVisible()) {
               MultiplePiePlot mpp = (MultiplePiePlot)ret.getPlot();
               PiePlot pp = (PiePlot)mpp.getPieChart().getPlot();
               pp.setLabelGenerator(null);
            }
         } else {
            PieDataset ds = dsBuilder.buildPieDataset(result, chart);
            switch( type ) {
               case TYPE_3D:
               ret = ChartFactory.createPieChart3D(null,ds,
                            graph.isLegendVisible(), false, false);
               break;
               default:
               ret = ChartFactory.createPieChart(null, ds,
                            graph.isLegendVisible(), false, false);               

            }
            if(!chart.isLabelsVisible()) {
               PiePlot pp = (PiePlot)ret.getPlot();
               pp.setLabelGenerator(null);
            }
         }
         return ret;
      }
   }
   

   
}
