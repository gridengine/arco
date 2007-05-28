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
package com.sun.grid.arco.web.arcomodule;


import com.iplanet.jato.*;
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.QueryValidator;
import com.sun.grid.arco.web.ArcoServletBase;
import com.sun.grid.arco.web.arcomodule.util.FieldAccessorCache;
import javax.servlet.*;
import java.io.IOException;
import com.sun.grid.logging.SGELog;
import com.sun.web.ui.common.CCI18N;

import javax.servlet.http.HttpServletRequest;

public class ArcoServlet extends ArcoServletBase {

    private static final String DEFAULT_MODULE_URL = "../arcomodule";
    private static final String PACKAGE_NAME = "com.sun.grid.arco.web.arcomodule";
    
    
    /* Initializes the servlet.  Here we create the log file and add the log to
     * the servlet context.  It would probably be better to store the log in the
     * session so that every user has a different log, that was too much work for
     * this simple example. */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    protected void initializeRequestContext(RequestContext requestContext) {

        /* Always call super, unless specifically instructed not to. */
        super.initializeRequestContext(requestContext);        

        /* Create a ViewBeanManager with this context and the correct package. */
        ViewBeanManager vbm = new ViewBeanManager(requestContext, PACKAGE_NAME);
        /* Don't forget to store the ViewBeanManager in the request context! */
        ((RequestContextImpl) requestContext).setViewBeanManager(vbm);
    }

    public void onUncaughtException(RequestContext rc, Exception e)
            throws ServletException, IOException {
            Throwable t = e;
            Throwable lastT = null; 
          while( t != lastT ) {
             lastT = t;
             if( t instanceof com.iplanet.jato.util.WrapperRuntimeException ) {                
                t = ((com.iplanet.jato.util.WrapperRuntimeException)t).getException();                
             }
             else if( t instanceof java.lang.reflect.InvocationTargetException ) {
                t = ((java.lang.reflect.InvocationTargetException)t).getTargetException();
             } else if ( t instanceof com.iplanet.jato.command.CommandException ) {
                t = ((com.iplanet.jato.command.CommandException)t).getException();
             }
             else {
                break;
             }
          }
          SGELog.severe( t, "Encountered an internal exception: {0}", t );                         

          ErrorViewBean errorViewBean = (ErrorViewBean)rc.getViewBeanManager().getViewBean( ErrorViewBean.class );
          
          SGELog.info( "forward to {0}", errorViewBean.getName() ); 
          
          errorViewBean.setError( t );
          errorViewBean.forwardTo( rc );
    }



    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Accounting and Reporting Tool.";
    }


   public static final String ATTR_RESULT = ArcoServlet.class.getName() + ".QUERY_RESULT";
   public static final String ATTR_QUERY  =  ArcoServlet.class.getName() +".QUERY";
   
   
   public static ResultModel getResultModel(HttpServletRequest req) {
      
      ResultModel ret = (ResultModel)req.getSession().getAttribute(ATTR_RESULT);
      if( ret == null ) {
         synchronized(req.getSession()) {
            ret = (ResultModel)req.getSession().getAttribute(ATTR_RESULT);
            if( ret == null ) {
               ret = new ResultModel();
               req.getSession().setAttribute(ATTR_RESULT, ret );
            }
         }
      } 
      return ret;
   }
   
   public static ResultModel getResultModel() {
      return getResultModel(RequestManager.getRequest());
   }
   
   public static void clearResultModel() {
      ModelManager mm = RequestManager.getRequestContext().getModelManager();
      ResultModel resultModel = (ResultModel)mm.getModel(ResultModel.class,ATTR_RESULT,true);
      if( resultModel != null ) {
         mm.removeFromSession(resultModel);
      }
   }
   
   public static QueryModel getQueryModel() {
       
      ModelManager mm = RequestManager.getRequestContext().getModelManager();
      
      return (QueryModel)mm.getModel( QueryModel.class, ATTR_QUERY, true, true );
      
    }
    
   public static void clearQueryModel() {
      ModelManager mm = RequestManager.getRequestContext().getModelManager();
      QueryModel queryModel = (QueryModel)mm.getModel(QueryModel.class,ATTR_QUERY,true);
      if( queryModel != null ) {
         mm.removeFromSession(queryModel);
      }
   }

   /** Attribute name for the I18N component */
   public static final String ATTR_I18N = "I18N";
   
   /**
    * get the I18N component from the current request
    * @return  the I18N component
    */
   public static CCI18N getI18N() {
      HttpServletRequest req = RequestManager.getRequestContext().getRequest();
      CCI18N ret = (CCI18N)req.getAttribute(ATTR_I18N);
      
      if( ret == null ) {         
         String baseName = "com.sun.grid.arco.web.arcomodule.Resources";
         ret = new CCI18N(RequestManager.getRequest(),
              RequestManager.getResponse(), baseName, null, req.getLocale() );
         req.setAttribute(ATTR_I18N, ret );
      }
      return ret;
   }
   
   
   public static final String ATTR_FORMAT_HELPER = "FormatHelper";
   
   public FormatHelper getFormatHelper() {
      FormatHelper ret = (FormatHelper)this.getServletContext().getAttribute(ATTR_FORMAT_HELPER);
      if (ret == null) {
         synchronized (this) {
            ret = (FormatHelper)this.getServletContext().getAttribute(ATTR_FORMAT_HELPER);
            if( ret == null ) {
               ret = new FormatHelper();
               this.getServletContext().setAttribute(ATTR_FORMAT_HELPER, ret);
            }
         }
      }
      return ret;
   }

   public static final String ATTR_FORMAT_TYPE_OPTION_LIST = "FormatTypeOptionList";
   
   public OptionList getFormatTypeOptionList() {
      OptionList ret = (OptionList)this.getServletContext().getAttribute(ATTR_FORMAT_TYPE_OPTION_LIST);
      if (ret == null) {
         synchronized (this) {
            ret = (OptionList)this.getServletContext().getAttribute(ATTR_FORMAT_TYPE_OPTION_LIST);
            if( ret == null ) {
               ret = new OptionList();
               ret.add("formatType.string", ArcoConstants.COLUMN_TYPE_STRING);
               ret.add("formatType.decimal", ArcoConstants.COLUMN_TYPE_DECIMAL);
               ret.add("formatType.date", ArcoConstants.COLUMN_TYPE_DATE);
               this.getServletContext().setAttribute(ATTR_FORMAT_TYPE_OPTION_LIST, ret);
            }
         }
      }
      return ret;
   }

   public static final String ATTR_CHART_TYPE_OPTION_LIST = "ChartTypeOptionList";
   
   public OptionList getChartTypeOptionList() {
      OptionList ret = (OptionList)this.getServletContext().getAttribute(ATTR_CHART_TYPE_OPTION_LIST);
      if (ret == null) {
         synchronized (this) {
            ret = (OptionList)this.getServletContext().getAttribute(ATTR_CHART_TYPE_OPTION_LIST);
            if( ret == null ) {
               ret = new OptionList();
               ret.add("chartType.barchart", ArcoConstants.CHART_TYPE_BAR);
               ret.add("chartType.barChart3D", ArcoConstants.CHART_TYPE_BAR_3D );
               ret.add("chartType.barChartStacked", ArcoConstants.CHART_TYPE_BAR_STACKED );
               ret.add("chartType.piechart", ArcoConstants.CHART_TYPE_PIE);
               ret.add("chartType.piechart3D", ArcoConstants.CHART_TYPE_PIE_3D);
               ret.add("chartType.linechart", ArcoConstants.CHART_TYPE_LINE);
               ret.add("chartType.stackedlinechart", ArcoConstants.CHART_TYPE_LINE_STACKED);
               this.getServletContext().setAttribute(ATTR_CHART_TYPE_OPTION_LIST, ret);
            }
         }
      }
      return ret;
   }
    
   public static ArcoServlet getInstance() {
       return (ArcoServlet)getCurrentInstance();
   }
   
   public static final String ATTR_VALIDATOR = "queryValidator";
   
   public QueryValidator getValidator() {
      QueryValidator ret = (QueryValidator)getServletContext().getAttribute(ATTR_VALIDATOR);
      if( ret == null ) {
         synchronized(this) {
            ret = (QueryValidator)getServletContext().getAttribute(ATTR_VALIDATOR);
            if( ret == null ) {
               ret = new QueryValidator(getSQLGenerator());
               getServletContext().setAttribute(ATTR_VALIDATOR,ret);
            }
         }
      }
      return ret;
   }
   
   public static final String ATTR_FIELD_ACCESSOR_CACHE = "fieldAccessorCache";
   
   public FieldAccessorCache getFieldAccessorCache() {
      FieldAccessorCache ret = (FieldAccessorCache)getServletContext().getAttribute(ATTR_FIELD_ACCESSOR_CACHE);
      
      if( ret == null ) {
         synchronized(this) {
            ret = (FieldAccessorCache)getServletContext().getAttribute(ATTR_FIELD_ACCESSOR_CACHE);
            if( ret == null ) {
               ret = new FieldAccessorCache();
               getServletContext().setAttribute(ATTR_FIELD_ACCESSOR_CACHE, ret);
            }
         }
      }
      return ret;
   }
}

//------------------------------------------------------------------------------
