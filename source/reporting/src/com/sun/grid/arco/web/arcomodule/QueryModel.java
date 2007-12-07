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

import com.iplanet.jato.RequestManager;
import com.iplanet.jato.view.html.Option;
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.logging.SGELog;
import javax.xml.bind.*;
import java.util.*;
import com.sun.grid.arco.web.arcomodule.util.AbstractObjectModel;
import com.sun.grid.arco.sql.ArcoDbConnectionPool;
import com.sun.grid.arco.Util;
import com.sun.grid.arco.sql.ArcoClusterModel;
import com.sun.grid.arco.validator.DefaultQueryStateHandler;
import com.sun.grid.arco.validator.ValidatorError;

public class QueryModel extends AbstractObjectModel implements java.io.Serializable,
        javax.servlet.http.HttpSessionBindingListener {
   
   public static final String DEFAULT_IMAGE_URL = "/reporting/images/poppel.png";
   public static final String PROP_IMAGE_URL = "/imgURL";
   public static final String PROP_FIELD = "/field";
   public static final String PROP_FILTER = "/filter";
   public static final String PROP_VIEW = "/view";
   public static final String PROP_VIEW_TABLE_COL = "/view/table/columnWithFormat";
   public static final String PROP_VIEW_PIVOT_ELEM = "/view/pivot/elem";
   public static final String PROP_VIEW_PIVOT_ROW = "/view/pivot/rowWithFormat";
   public static final String PROP_VIEW_PIVOT_DATA = "/view/pivot/dataWithFormat";
   
   private transient DefaultQueryStateHandler queryStateHandler;
   
   /** Creates a new instance of QueryModel */
   public QueryModel() {
   }
   
   public Object getValue(String name) {
      
      Object retValue;
      
      retValue = super.getValue(name);
      if( retValue == null && PROP_IMAGE_URL.equals(name) ){
         retValue = DEFAULT_IMAGE_URL;
      }
      return retValue;
   }
   
   public void setQuery(QueryType query) {
      // Bug 6353541
      // The validators of the query can modify the query, but the model
      // listener are not informed about this modifications. The setObject 
      // method fires the event that the root object has been changed. All 
      // listeners are correctly informed, if the validation process has been 
      // finished before the new query is set into the model.
      validate(query);
      setObject(query);
      simpleQuery = null;
   }
   
   
   public QueryType getQuery()  {
      try {
         return (QueryType)getObject();
      } catch( com.iplanet.jato.model.ModelControlException mce ) {
         IllegalStateException ilse = new IllegalStateException("ModelControlException");
         ilse.initCause(mce);
         throw ilse;
      }
   }
   
   public boolean isResult() {
      return getQuery() instanceof Result;
   }
   
   public boolean isAdvanced() {
      return ArcoConstants.ADVANCED.equals( getQuery().getType() );
   }
   
   public boolean isSimple() {
      return ArcoConstants.SIMPLE.equals( getQuery().getType() );
   }
   
   private void initValidator() {
      if( this.queryStateHandler == null ) {
        queryStateHandler = new DefaultQueryStateHandler(); 
      } 
   }
   
   public void validate() {
      QueryType query = getQuery();
      validate(query);
      // The validators of the query can modify the query, but the model
      // listener are not informed about this modifications. The setObject 
      // method fires the event that the root object has been changed. All 
      // listeners are correctly informed, if the validation process has been 
      // finished before the new query is set into the model.
      setObject(query);
   }
   
   private void validate(QueryType query) {
      initValidator();
      queryStateHandler.clear();
      ArcoServlet.getInstance().getValidator().validate(query, queryStateHandler);
   }
   
   public boolean hasErrors() {
      return queryStateHandler != null && queryStateHandler.hasErrors();
   }
   
   public boolean isSaveable() {
      return queryStateHandler != null && queryStateHandler.isSaveable();
   }
   
   public boolean isRunnable() {
      return queryStateHandler != null && queryStateHandler.isRunnable();
   }
   
   public boolean isConvertable() {
      return !isResult() && queryStateHandler != null && queryStateHandler.isConvertable();
   }
   
   public boolean hasWarnings() {
      return queryStateHandler != null && queryStateHandler.hasWarnings();
   }
   
   public ValidatorError[] getErrors() {
      initValidator();
      return queryStateHandler.getErrors();
   }
   
   public ValidatorError[] getWarnings() {
      initValidator();
      return queryStateHandler.getWarnings();
   }   
   
   public void clearFilters() {
      if( !getQuery().getFilter().isEmpty() ) {
         getQuery().getFilter().clear();
         setDirty(true);
         fireValuesChanged(PROP_FILTER);
      }
   }
   
   public void clearFieldsAndFilters() {
      clearFilters();
      clearFields();
   }
   
   public void clearFields() {
      if( !getQuery().getField().isEmpty() ) {
         getQuery().getField().clear();
         setDirty(true);
         fieldOptionList = null;
         fireValuesChanged(PROP_FIELD);
      }
   }
   
   /**
    * remove the field a index
    * @param index the index of the field
    */
   public void removeField(int index) {
      List fieldList = getQuery().getField();
      Field field = (Field)fieldList.remove(index);
      if( field != null ) {
         setDirty(true);
         fieldOptionList = null;
         fireValuesChanged(PROP_FIELD);
         
         // Delete filters for the removed field
         List filterList = getQuery().getFilter();
         Filter filter = null;
         ListIterator iter = filterList.listIterator();
         boolean filterChanged = false;
         while( iter.hasNext() ) {
            filter = (Filter)iter.next();
            if( filter.getName().equals(field.getDbName())) {
               iter.remove();
               filterChanged = true;
            }
         }
         if( filterChanged ) {
            fireValuesChanged(PROP_FILTER);
         }
      }
   }
   
   public void removeFields(Integer[] indizes) {
      List fieldList = getQuery().getField();
      Arrays.sort(indizes);
      Field[] fields = new Field[indizes.length];
      for(int i = indizes.length-1; i >= 0; i--) {
         fields[i] = (Field)fieldList.remove(indizes[i].intValue());
      }
      fireValuesChanged(PROP_FIELD);
      // Delete filters for the removed fields
      List filterList = getQuery().getFilter();
      Filter filter = null;
      ListIterator iter = filterList.listIterator();
      boolean filterChanged = false;
      while( iter.hasNext() ) {
         filter = (Filter)iter.next();
         for( int i = 0; i < fields.length; i++) {
            if( filter.getName().equals(fields[i].getDbName()) ) {
               iter.remove();
               filterChanged = true;
               break;
            }
         }
      }
      if( filterChanged ) {
         fireValuesChanged(PROP_FILTER);
      }
      
   }
   
   private transient OptionList fieldOptionList;
   private transient String     tableName;
   
   public OptionList getFieldOptionList() {
      boolean dirty = false;
      if( tableName != null ) {
         dirty = !tableName.equals(getQuery().getTableName());
      } else {
         dirty = getQuery().getTableName() != null;
      }
      
      if( fieldOptionList == null || dirty ) {
         fieldOptionList = new OptionList();
         tableName = getQuery().getTableName();
         if( tableName != null ) {
            ArcoDbConnectionPool dp = ArcoServlet.getCurrentInstance().getConnectionPool();
            try {
               ArcoClusterModel acm = ArcoClusterModel.getInstance(RequestManager.getSession());               
               List fields = dp.getFieldList(getQuery().getTableName(),acm.getCurrentCluster());
               Iterator iter = fields.iterator();
               String field = null;
               while( iter.hasNext() ) {
                  field = (String)iter.next();
                  field = field.toLowerCase();
                  fieldOptionList.add(field,field);
               }
            } catch( java.sql.SQLException sqle ) {
               // TODO throw the SQLException, the view component has to handle this error
               SGELog.warning(sqle,"Can not load field list: {0}", sqle.getMessage() );
            }
         }
      }
      return fieldOptionList;
   }
   
   public static final String FILTER_FIELD_OPTION_LIST = QueryModel.class.getName() + ".FITLER_FIELD_OPTION_LIST";
   
   /**
    * Get a optionlist which contains fields of the selected database table/view
    * and the user defined fields of the query.
    * @return a option list with all fields which can be used for a filter
    */
   public OptionList getFilterFieldOptionList() {
      
      OptionList ret = (OptionList)getRequestContext().getRequest().getAttribute(FILTER_FIELD_OPTION_LIST);
      if( ret == null ) {
         
         ret = new OptionList();
         OptionList fieldOptionList = getFieldOptionList();
         Option option = null;
         for(int i = 0; i < fieldOptionList.size(); i++ ) {
            option = fieldOptionList.get(i);
            ret.add(option.getLabel(), option.getValue() );
         }
         
         QueryType query = getQuery();
         Iterator iter = query.getField().iterator();
         Field field = null;
         while( iter.hasNext() ) {
            field = (Field)iter.next();
            if( ret.getValueIndex(field.getReportName()) < 0 ) {
               ret.add(field.getReportName(), field.getReportName() );
            }
         }
         getRequestContext().getRequest().setAttribute(FILTER_FIELD_OPTION_LIST,ret);
      }
      return ret;
   }
   
   public Field addNewField() {
      
      try {
         Field field = getJAXBObjectFactory().createField();
         List fieldList = getQuery().getField();
         OptionList fieldOptionList = getFieldOptionList();
         
         field.setDbName(fieldOptionList.getValue(0));
         fieldList.add( field );
         Util.correctFieldNames(getQuery());
         setDirty(true);
         fieldOptionList = null;
         fireValuesChanged(PROP_FIELD);
         return field;
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("Can not create instanceof field");
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   
   /**
    * Remove the filter at index <code>index</code>
    * @param index the index
    */
   public void removeFilter(int index) {
      List filterList = getQuery().getFilter();
      filterList.remove(index);
      setDirty(true);
      fireValuesChanged(PROP_FILTER);
   }
   
   /**
    * activate/deactive some filter of a simple query
    * @param indizes indizes of the filters
    * @param active  active/deactive
    */
   public void setFilterActive(Integer [] indizes, boolean active ) {
      List filterList = getQuery().getFilter();
      Filter filter = null;
      for( int i = 0; i < indizes.length; i++ ) {
         filter = (Filter)filterList.get(indizes[i].intValue());
         filter.setActive(active);
      }
      setDirty(true);
      fireValuesChanged(PROP_FILTER);
   }
   
   
   /**
    * remove the filters which are specified be an array
    * of indizes
    * @param indizes  the array of indizes
    */
   public void removeFilters(Integer[] indizes) {
      List filterList = getQuery().getFilter();
      Arrays.sort(indizes);
      for( int i = indizes.length-1; i>=0; i--) {
         filterList.remove(indizes[i].intValue());
      }
      fireValuesChanged(PROP_FILTER);
   }
   
   /**
    * add a new filter to the query
    * @return the new filter
    */
   public Filter addNewFilter() {
      
      QueryType query = getQuery();
      
      List filterList = query.getFilter();
      
      try {
         Filter filter = getJAXBObjectFactory().createFilter();
         
         filter.setActive(true);
         filter.setLateBinding(false);
         filter.setCondition( com.sun.grid.arco.util.FilterType.EQUAL.getName() );

         if( filterList.isEmpty() ) {
            filter.setLogicalConnection(com.sun.grid.arco.util.LogicalConnection.NONE.getName());
         } else {
            filter.setLogicalConnection(com.sun.grid.arco.util.LogicalConnection.AND.getName());
         }
         
         List fieldList = query.getField();
         
         if( !fieldList.isEmpty() ) {
            Field field = (Field)fieldList.get(0);
            filter.setName( field.getDbName() );
         }
         filterList.add(filter);
         fireValuesChanged(PROP_FILTER);
         setDirty(true);
         return filter;
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException( "Can't create new filter" );
         ilse.initCause( jaxbe );
         throw ilse;
      }
   }
   
   private QueryType simpleQuery;
   
   public void toAdvanced() throws java.text.ParseException  {
      
      QueryType query = getQuery();
      
      Query advancedQuery = null;
      try {
         advancedQuery = (Query)com.sun.grid.arco.Util.clone(query);
      } catch( CloneNotSupportedException cnse ) {
         IllegalStateException ilse = new IllegalStateException("Can't clone query");
         ilse.initCause(cnse);
         throw ilse;
      }
      
      advancedQuery.unsetName();
      advancedQuery.setType(ArcoConstants.ADVANCED);
      setQuery(advancedQuery);
      simpleQuery = query;
   }
   
   public boolean isConvertedFromSimpleQuery() {
      return simpleQuery != null;
   }
   
   public void toSimple() {
      setQuery(simpleQuery);
   }
   
   /////////////////////////////////////////////////////////////////////////////
   // View Table section FIXME: add javadoc
   public static final String ATTRIBUTE_DEFINED_FIELDS ="definedFields";
   
   public OptionList getDefinedOptionList() {
      OptionList ret = (OptionList)RequestManager.getRequest().getAttribute(ATTRIBUTE_DEFINED_FIELDS);
      if ( ret == null ) {
         ret = new OptionList();
         List fieldList = getQuery().getField();
         Iterator iter = fieldList.iterator();
         Field field = null;
         String name = null;
         while( iter.hasNext() ) {
            field = (Field)iter.next();
            name = field.getReportName();
            if(name == null) {
               name = field.getDbName();
            }
            if( name != null ) {
               ret.add(name,name);
            }
         }
         RequestManager.getRequest().setAttribute(ATTRIBUTE_DEFINED_FIELDS,ret);
      }
      return ret;
   }
   
   private void cleasDefinedOptionList() {
      RequestManager.getRequest().removeAttribute(ATTRIBUTE_DEFINED_FIELDS);
   }
   
   /**
    * remove the field a index
    * @param index the index of the field
    */
   public void removeDefinedField(int index) {
      List fcl = getQuery().getView().getTable().getColumnWithFormat();
      FormattedValue fc = (FormattedValue)fcl.remove(index);
      if( fc != null ) {
         setDirty(true);
         cleasDefinedOptionList();
         fireValuesChanged(PROP_VIEW_TABLE_COL);
      }
   }
   
   public void removeDefinedFields(Integer[] indizes) {
      List fcl = getQuery().getView().getTable().getColumnWithFormat();
      Arrays.sort(indizes);
      FormattedValue[] fcs = new FormattedValue[indizes.length];
      for(int i = indizes.length-1; i >= 0; i--) {
         fcs[i] = (FormattedValue)fcl.remove(indizes[i].intValue());
      }
      fireValuesChanged(PROP_VIEW_TABLE_COL);
   }
   
   public FormattedValue addNewDefinedField() {
      try {
         List fcl = null;
         FormattedValue fc = getJAXBObjectFactory().createFormattedValue();
         Table table = getQuery().getView().getTable();
         if (table != null) {
            fcl = table.getColumnWithFormat();
         } else {
            table = getJAXBObjectFactory().createTable();
            getQuery().getView().setTable(table);
            fcl = getQuery().getView().getTable().getColumnWithFormat();
         }
         fcl.add( fc );
         setDirty(true);
         cleasDefinedOptionList();
         fireValuesChanged(PROP_VIEW_TABLE_COL);
         return fc;
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("Can not create instanceof field");
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   /**
    * remove the column with index
    * @param index the index of the column
    */
   public void removePivotElement(int index) {
      List fcl = getQuery().getView().getPivot().getElem();
      FormattedValue fc = (FormattedValue)fcl.remove(index);
      setDirty(true);
      fireValuesChanged(PROP_VIEW_PIVOT_ELEM);
   }
   
   public void removePivotElement(Integer[] indizes) {
      List fcl = getQuery().getView().getPivot().getElem();
      Arrays.sort(indizes);
      for(int i = indizes.length-1; i >= 0; i--) {
         fcl.remove(indizes[i].intValue());
      }
      fireValuesChanged(PROP_VIEW_PIVOT_ELEM);
   }
   
   public FormattedValue addNewPivotRow() {
      return addNewPivotEntry(ArcoConstants.PIVOT_TYPE_ROW);
   }
   
   public FormattedValue addNewPivotColumn() {
      return addNewPivotEntry(ArcoConstants.PIVOT_TYPE_COLUMN);
   }
   
   public FormattedValue addNewPivotData() {
      return addNewPivotEntry(ArcoConstants.PIVOT_TYPE_DATA);
   }
   
   private PivotElement addNewPivotEntry(String pivotType) {
      try {
         List fcl = null;
         PivotElement pe = getJAXBObjectFactory().createPivotElement();
         pe.setPivotType(pivotType);
         Pivot pivot = getQuery().getView().getPivot();
         if (pivot == null) {
            pivot = getJAXBObjectFactory().createPivot();
            getQuery().getView().setPivot(pivot);            
         }
         fcl = pivot.getElem();      
         fcl.add(pe);
         setDirty(true);
         fireValuesChanged(PROP_VIEW_PIVOT_ELEM);
         return pe;
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("Can not create instanceof pivot element");
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   /////////////////////////////////////////////////////////////////////////////
   // View Chart AddRemove FIXME: add javadoc
   
   public static final String ATTR_AVAILABLE_FIELDS = "availableFields";
   public static final String ATTR_AVAILABLE_FIELDS_WITH_NULL_VALUE = "availableFieldsWithNullValue";
   
   public OptionList getAvailableOptionList(boolean withNullValue) {
      
      String attrName = withNullValue ? ATTR_AVAILABLE_FIELDS_WITH_NULL_VALUE : ATTR_AVAILABLE_FIELDS;
      
      OptionList ret = (OptionList)RequestManager.getRequest().getAttribute(attrName);
      if ( ret == null ) {
         QueryType query = getQuery();
         ret = new OptionList();
         if( withNullValue ) {
            ret.add("","");
         }
         List fieldList = query.getField();
         Iterator iter = fieldList.iterator();
         Field field = null;
         String name = null;
         
         while( iter.hasNext() ) {
            field = (Field)iter.next();
            name = field.getReportName();
            if( name == null ) {
               name = field.getDbName();
            }
            ret.add(name,name);         
         }
         RequestManager.getRequest().setAttribute(attrName, ret);
      }
      return ret;
   }
   
   
   public void setViewChartSeriesFromColumns(OptionList selectedColumns) {
      try {
         QueryType query = getQuery();
         ViewConfiguration view = query.getView();
         Chart chart = view.getGraphic().getChart();

         SeriesFromColumns sfc = chart.getSeriesFromColumns();
         
         if( selectedColumns != null ) {     
            
            if( sfc == null ) {
               sfc = getJAXBObjectFactory().createSeriesFromColumns();
               chart.setSeriesFromColumns(sfc);
            }
            
            List seriesFromColumns = sfc.getColumn();
            List orgSeriesFromColumns = new ArrayList(seriesFromColumns);
            
            sfc.unsetColumn();
            
            boolean modified = false;            
            String column = null;
            
            for(int i = 0; i < selectedColumns.size(); i++ ) {
               column = selectedColumns.getValue(i);
               if( !orgSeriesFromColumns.remove(column) ) {
                  modified = true;
               }
               seriesFromColumns.add(column);
            }
            
            if( orgSeriesFromColumns.size() > 0 ) {
               modified = true;
            }
            if( modified ) {
               setDirty(true);
               fireValueChanged(PROP_VIEW);
            }
         } else {
            if( sfc != null && sfc.isSetColumn() ) {
               sfc.unsetColumn();
               setDirty(true);
               fireValueChanged(PROP_VIEW);
            }
         }
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage());
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   
   private void moveViewElementUp(ViewElement elem) {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      
      List elems = Util.getSortedViewElements(view);
      
      int pos = elem.getOrder();
      if( pos > 0 ) {
         ViewElement prev = (ViewElement)elems.get(pos-1);         
         elem.setOrder(pos-1);
         prev.setOrder(pos);
         fireValueChanged(PROP_VIEW);
      }
   }
   
   private void moveViewElementDown(int elemPosition) {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      
      List elems = Util.getSortedViewElements(view);
      if( elemPosition < elems.size() - 1 ) {
         
         ViewElement elem = (ViewElement)elems.get(elemPosition);
         
         ViewElement prev = (ViewElement)elems.get(elemPosition+1);
         elem.setOrder(elemPosition+1);
         prev.setOrder(elemPosition);
      }
   }
   
   
   /**
    *  Add a new table to the view configuration
    */
   public void addTableView() {
      
      try {
         QueryType query = getQuery();
         
         ViewConfiguration view = query.getView();
         
         Table table = null;
         if( view.isSetTable() ) {
            table = view.getTable();
         } else {
            table = getJAXBObjectFactory().createTable();
            view.setTable(table);
         }
         table.setOrder(Util.getNextViewOrder(view));
         table.setVisible(true);

         fireValueChanged(PROP_VIEW);
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error:" + jaxbe );
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   public void removeTableView() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      view.getTable().setVisible(false);
      Util.adjustViewOrder(view);
      fireValueChanged(PROP_VIEW);
   }
   
   public void moveTableViewDown() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      Table table = view.getTable();
      moveViewElementDown(table.getOrder());
      fireValueChanged(PROP_VIEW);
   }
   
   public void moveTableViewUp() {
      
      
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      Table table = view.getTable();
      
      moveViewElementUp(table);
   }
   
   
   
   public void addPivotView() {
      try {
         QueryType query = getQuery();
         
         ViewConfiguration view = query.getView();
         
         Pivot pivot = null;
         
         if( view.isSetPivot() ) {
            pivot = view.getPivot();
         } else {
            pivot = getJAXBObjectFactory().createPivot();
            view.setPivot(pivot);
         }
         pivot.setVisible(true);
         pivot.setOrder( Util.getNextViewOrder(view));

         fireValueChanged(PROP_VIEW);
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error:" + jaxbe );
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   public void removePivotView() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      view.getPivot().setVisible(false);
      Util.adjustViewOrder(view);
      fireValueChanged(PROP_VIEW);
   }
   
   public void movePivotViewDown() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      Pivot pivot = view.getPivot();
      moveViewElementDown(pivot.getOrder());
      fireValueChanged(PROP_VIEW);
   }
   
   public void movePivotViewUp() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      Pivot pivot = view.getPivot();
      
      moveViewElementUp(pivot);
   }
   
   
   public void addGraphicView() {
      try {
         QueryType query = getQuery();
         
         ViewConfiguration view = query.getView();
         
         Graphic graphic = null;
         if( view.isSetGraphic() ) {
            graphic = view.getGraphic();
         } else {
            graphic = getJAXBObjectFactory().createGraphic();
            view.setGraphic(graphic);
         }
         graphic.setVisible(true);
         graphic.setOrder(Util.getNextViewOrder(view));

         fireValueChanged(PROP_VIEW);
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error:" + jaxbe );
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   public void removeGraphicView() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      view.getGraphic().setVisible(false);
      Util.adjustViewOrder(view);
      fireValueChanged(PROP_VIEW);
   }
   
   public void moveGraphicViewDown() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      Graphic graphic = view.getGraphic();
      moveViewElementDown(graphic.getOrder());
      fireValueChanged(PROP_VIEW);
   }
   
   public void moveGraphicViewUp() {
      QueryType query = getQuery();
      ViewConfiguration view = query.getView();
      Graphic graphic = view.getGraphic();
      moveViewElementUp(graphic);
   }
   
   
   // --------- JAXB Helper methods --------------------------------------------
   
   private com.sun.grid.arco.model.ObjectFactory faq;
   
   public com.sun.grid.arco.model.ObjectFactory getJAXBObjectFactory() {
      
      if( faq == null ) {
         faq = new com.sun.grid.arco.model.ObjectFactory();
      }
      return faq;
   }
   
   
   public void valueBound(javax.servlet.http.HttpSessionBindingEvent httpSessionBindingEvent) {
      SGELog.fine("bound to session {0}", httpSessionBindingEvent.getSession().getId() );
   }
   
   public void valueUnbound(javax.servlet.http.HttpSessionBindingEvent httpSessionBindingEvent) {
      SGELog.fine("unbound from session {0}", httpSessionBindingEvent.getSession().getId() );
   }
   
}
