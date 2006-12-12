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
package com.sun.grid.arco.validator.view;

import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.validator.QueryStateHandler;
import com.sun.grid.arco.validator.AbstractValidator;
import com.sun.grid.arco.model.QueryType;
import com.sun.grid.arco.model.ViewConfiguration;
import com.sun.grid.arco.model.Graphic;
import com.sun.grid.arco.model.Chart;
import com.sun.grid.arco.model.SeriesFromColumns;

public class GraphViewValidator extends AbstractValidator {
   
   public static final String PROPERTY_NAME = "view/graphic/chart";
   
   public void validate(QueryType query, QueryStateHandler handler) {
      
      if( query.isSetView() ) {
         ViewConfiguration view = query.getView();
         if( view.isSetGraphic() ) {
            Graphic graph = view.getGraphic();
            if( graph.isVisible() ) {

               if( graph.isSetChart() ) {
                  Chart chart = graph.getChart();
                  
                  String seriesType = chart.getSeriesType();
                  
                  if( seriesType == null ) {
                     handler.addError(PROPERTY_NAME, "query.view.noSeriesType", null);
                     return;
                  }
                  
                  String type = chart.getType();
                  if( ArcoConstants.CHART_TYPE_BAR.equals(type) || 
                      ArcoConstants.CHART_TYPE_BAR_3D.equals(type) ||
                      ArcoConstants.CHART_TYPE_BAR_STACKED.equals(type) ) {
                     validateBarChart(chart, query, handler);
                  } else if( ArcoConstants.CHART_TYPE_LINE.equals(type) ||
                             ArcoConstants.CHART_TYPE_LINE_STACKED.equals(type) ) {
                     validateLineChart(chart, query, handler);
                  } else if( ArcoConstants.CHART_TYPE_PIE.equals(type) ||
                             ArcoConstants.CHART_TYPE_PIE_3D.equals(type) ) {
                     validatePieChart(chart, query, handler);
                  }                  
               }              
            }
         }
      }
   }
   
   private void assertXAxis(Chart chart, QueryType query, QueryStateHandler handler, String chartType) { 
      if( !chart.isSetXaxis() || chart.getXaxis().length() == 0 ) {
         handler.addError(PROPERTY_NAME, "query.view.xAxisRequired", new Object[] { chartType } );
      }
   }
   
   private void assertColumn(Chart chart, QueryType query, QueryStateHandler handler ) {
      String seriesType = chart.getSeriesType();
      if( ArcoConstants.CHART_SERIES_FROM_COL.equals(seriesType) ) {

         boolean noColumns = true;
         if( chart.isSetSeriesFromColumns()  ) {
            SeriesFromColumns sfc  = chart.getSeriesFromColumns();
            noColumns = !sfc.isSetColumn() || sfc.getColumn().isEmpty();
         }
         if( noColumns ) {
            handler.addError(PROPERTY_NAME, "query.view.seriesFromColumnRequired", null);
         } 
      }
   }
   
   private void validateBarChart( Chart chart, QueryType query, QueryStateHandler handler) {
       assertXAxis(chart,query,handler, ArcoConstants.CHART_TYPE_BAR); 
       if( !handler.hasErrors() ) {
          assertColumn(chart,query,handler);
       }
   }

   private void validateLineChart( Chart chart, QueryType query, QueryStateHandler handler) {
       assertXAxis(chart,query,handler, ArcoConstants.CHART_TYPE_LINE);  
       if( !handler.hasErrors() ) {
          assertColumn(chart,query,handler);
       }
   }

   private void validatePieChart( Chart chart, QueryType query, QueryStateHandler handler) {
       if( !handler.hasErrors() ) {
          assertColumn(chart,query,handler);
       }
   }
   
}
