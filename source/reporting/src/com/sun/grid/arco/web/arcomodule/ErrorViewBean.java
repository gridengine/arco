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

//java core
import java.io.*;
import java.util.Vector;
import java.util.Stack;
import java.util.logging.Level;
import javax.servlet.*;
import javax.servlet.http.*;

import com.sun.web.ui.view.alert.*;
import com.sun.web.ui.view.pagetitle.*;
import com.sun.web.ui.model.*;
//jato

import java.util.Locale;
import com.sun.web.ui.common.CCI18N;


import com.sun.web.ui.taglib.html.*;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.masthead.*;

import com.sun.web.ui.model.CCMastheadModel;
import com.sun.web.ui.view.breadcrumb.*;
import com.sun.web.ui.model.CCBreadCrumbsModel;
import com.iplanet.jato.RequestContext;
import com.iplanet.jato.command.CommandDescriptor;
import com.iplanet.jato.model.*;
import com.iplanet.jato.view.*;
import com.iplanet.jato.view.event.*;
import com.iplanet.jato.view.html.ComboBox;
import com.iplanet.jato.view.html.StaticTextField;
import com.iplanet.jato.view.html.OptionList;
import com.sun.web.ui.view.alert.*;

//reporting

import com.sun.grid.arco.util.SortType;

import com.sun.grid.logging.SGELog;

public class ErrorViewBean extends ViewBeanBase {

    private HttpSession httpSession;
   
    //private static final boolean bDebug = true;
    
    private static final String PAGE_NAME             = "Error";


    public static final String CHILD_ALERT      = "Alert";
    public static final String CHILD_OK_BUTTON  = "okButton";
    public static final String CHILD_MASTHEAD   = "Masthead";
    
    //#*************BreadCrumb*********************************
        
    public static final String CHILD_BREADCRUMB                = "BreadCrumb";
    private static String BREADCRUMB_NAME                      = "QueryList";
    
   public static final String ERROR_URL = "/jsp/arcomodule/Error.jsp";
    /**
     *
     *
     *
     *
     */
    public ErrorViewBean() {
		/* First we let the ViewBeanBase register us under our page name */
		super (PAGE_NAME);
		this.setDefaultDisplayURL (ERROR_URL);
		registerChildren ();
	}
	
    protected void registerChildren () {
       registerChild(CHILD_MASTHEAD, CCPrimaryMasthead.class);
       registerChild( CHILD_ALERT, CCAlertFullPage.class );
       registerChild(CHILD_BREADCRUMB, CCBreadCrumbs.class);
       registerChild(CHILD_OK_BUTTON, CCButton.class );
    }
    
	
	
        
        
    protected View createChild(String childName) {
       
      if ( childName.equals( CHILD_ALERT ) ) {
          CCAlertFullPage child = new CCAlertFullPage( this, childName, null );
          
          child.setType( CCAlert.TYPE_ERROR );
          child.setTitle( "Error" );
          child.setSummary( "Unknown" );
          child.setDetail( "Unknown" );
          return child;
       } else if ( childName.equals( CHILD_MASTHEAD )) {
          return new CCPrimaryMasthead(this, new CCMastheadModel(), childName);         
       } else if ( childName.equals( CHILD_OK_BUTTON ) ) {
          return new CCButton( this, childName, null );  
       }  else if (childName.equals(CHILD_BREADCRUMB)) {
          CCBreadCrumbs child = new CCBreadCrumbs(this, null, childName);
          return child;          
       } else {
          View ret = super.createChild( childName );
          if( ret == null ) {
            throw new IllegalArgumentException("child with name " + childName + " unknown" );
          } else {
             return ret;
          }
       }
    }
    
   
    public void setError( Throwable t ) {
       
       CCAlertFullPage alert = (CCAlertFullPage)getChild( CHILD_ALERT );
       
       alert.setType( CCAlert.TYPE_ERROR );
       
       StringBuffer buf = new StringBuffer();
       
       String msg = t.getLocalizedMessage();
       if( msg == null ) {
          msg = t.getMessage();
       }

       buf.append( msg );
       buf.append( " (");
       buf.append( t.getClass().getName() );
       buf.append( ")");

       
       alert.setSummary( buf.toString() );
       
       
       buf.setLength( 0 );
       buf.append( "<br>at<br>");       
       addStack( t, buf );
       
       while( t.getCause() != null ) {
          t = t.getCause();
          buf.append("caused by ");
          buf.append( t.toString() );
          buf.append( "<br>" );
          addStack( t, buf );
       }
       
       alert.setDetail( buf.toString() );
    }
    
    private void addStack( Throwable t , StringBuffer buf ) {
       StackTraceElement stack [] = t.getStackTrace();
       int len = Math.min( 20, stack.length );
       for( int i = 0; i < len; i++ ) {
            buf.append( stack[i].toString() );
            buf.append( "<br>");
       }
       if( len < stack.length ) {
          buf.append( "...<br>");
       }
    }
    

    
     public void handleOkButtonRequest (RequestInvocationEvent event) 
         throws ModelControlException,
                javax.servlet.ServletException, 
                IOException {
         getViewBean(IndexViewBean.class).forwardTo(getRequestContext());
     }
        
                
}

// #############################################################################
