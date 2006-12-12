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
package com.sun.grid.arco.upgrade;

import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.model.Bar;
import com.sun.grid.arco.model.Chart;
import com.sun.grid.arco.model.Graphic;
import com.sun.grid.arco.model.Line;
import com.sun.grid.arco.model.NamedObject;
import com.sun.grid.arco.model.ObjectFactory;
import com.sun.grid.arco.model.Pie;
import com.sun.grid.arco.model.ReportingObject;
import com.sun.grid.arco.model.SeriesFromRow;
import com.sun.grid.arco.model.StackedLine;
import com.sun.grid.arco.model.ViewConfiguration;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

public class GraphicUpgrader extends AbstractUpgrader {
   
   public GraphicUpgrader() {
      super(1);
   }
   
   public void upgrade(NamedObject obj) throws UpgraderException {
      
      if( obj instanceof ReportingObject ) {
         ReportingObject repObj = (ReportingObject)obj;
         
         if( repObj.isSetView() ) {
            ViewConfiguration view = repObj.getView();
            
            if( view.isSetGraphic() ) {
               Graphic graphic = view.getGraphic();
               graphic.setVisible(true);
               ObjectFactory faq = new ObjectFactory();
               try {
                  if( graphic.isSetBar() ) {
                     upgradeBar(graphic, faq);
                     graphic.unsetBar();
                  } else if( graphic.isSetLine() ) {
                     upgradeLine(graphic, faq);
                     graphic.unsetLine();
                  } else if( graphic.isSetPie() ) {
                     upgradePie(graphic, faq);
                     graphic.unsetPie();
                  } else if( graphic.isSetStackedline() ) {
                     upgradeStacked(graphic, faq);
                     graphic.unsetStackedline();
                  }
               } catch( JAXBException jaxbe ) {
                  IllegalStateException ilse = new IllegalStateException("JAXB Error: " + jaxbe.getMessage());
                  ilse.initCause(jaxbe);
                  throw ilse;
               }
            }
         }
      }
      
   }
   
   private void upgradeBar(Graphic graphic, ObjectFactory faq)
     throws JAXBException {
      
      Bar bar = graphic.getBar();
      
      Chart chart = faq.createChart();
      
      chart.setType( ArcoConstants.CHART_TYPE_BAR );
      chart.setSeriesType( ArcoConstants.CHART_SERIES_FROM_ROW );
      SeriesFromRow sfr = faq.createSeriesFromRow();
      chart.setSeriesFromRow(sfr);
      
      if( bar.isSetXaxis() ) {
         List xAxis = bar.getXaxis();
         Iterator iter = xAxis.iterator();
         if( iter.hasNext() ) {
            chart.setXaxis((String)iter.next());
         }
         if( iter.hasNext() ) {
            sfr.setLabel((String)iter.next());
         }
      }
      
      if( bar.isSetYaxis() ) {
         sfr.setValue(bar.getYaxis());
      }
      
      graphic.setChart(chart);
      
      graphic.unsetBar();      
   }
   
   private void upgradeLine(Graphic graphic, ObjectFactory faq) 
      throws JAXBException {
      Line line = graphic.getLine();
      
      Chart chart = faq.createChart();
      
      chart.setType(ArcoConstants.CHART_TYPE_LINE );
      chart.setSeriesType( ArcoConstants.CHART_SERIES_FROM_ROW );
      SeriesFromRow sfr = faq.createSeriesFromRow();
      chart.setSeriesFromRow(sfr);
      
      if( line.isSetXaxis() ) {
         chart.setXaxis(line.getXaxis());
      }
      if( line.isSetYaxis() ) {
         sfr.setValue( line.getYaxis()); 
      }
      if( line.isSetType() ) {
         sfr.setLabel( line.getType() );
      }
      
      graphic.unsetLine();
      graphic.setChart(chart);
   }
   
   private void upgradePie(Graphic graphic, ObjectFactory faq) 
      throws JAXBException {
      
      Pie pie = graphic.getPie();
      
      Chart chart = faq.createChart();
      
      chart.setType(ArcoConstants.CHART_TYPE_PIE );
      chart.setSeriesType( ArcoConstants.CHART_SERIES_FROM_ROW );
      SeriesFromRow sfr = faq.createSeriesFromRow();
      chart.setSeriesFromRow(sfr);
      
      if( pie.isSetXaxis() ) {
         sfr.setLabel(pie.getXaxis());
      }
      if( pie.isSetYaxis() ) {
         sfr.setValue( pie.getYaxis()); 
      }
      
      graphic.unsetPie();
      graphic.setChart(chart);
   }
   
   private void upgradeStacked( Graphic graphic, ObjectFactory faq) 
      throws JAXBException {
      StackedLine line = graphic.getStackedline();
      
      Chart chart = faq.createChart();
      
      chart.setType(ArcoConstants.CHART_TYPE_LINE );
      chart.setSeriesType( ArcoConstants.CHART_SERIES_FROM_ROW );
      SeriesFromRow sfr = faq.createSeriesFromRow();
      chart.setSeriesFromRow(sfr);
      
      if( line.isSetXaxis() ) {
         chart.setXaxis(line.getXaxis());
      }
      if( line.isSetYaxis() ) {
         sfr.setValue( line.getYaxis()); 
      }
      if( line.isSetType() ) {
         sfr.setLabel( line.getType() );
      }
      
      graphic.unsetStackedline();
      graphic.setChart(chart);
   }
   
}
