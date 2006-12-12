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
package com.sun.grid.arco.sql;

import java.util.*;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.util.FieldFunction;
import com.sun.grid.arco.util.LogicalConnection;
import com.sun.grid.arco.util.FilterType;
import com.sun.grid.arco.util.SortType;
import com.sun.grid.logging.SGELog;

/**
 * This class generates the SQL statements for queries
 *
 */
public abstract class AbstractSQLGenerator implements SQLGenerator {
   
   /**
    * The different SQL dialects has different where clauses for
    * limiting the row count.
    * This method returns this part of the where clause.
    *
    * @param query the query
    * @return the row limit where clause
    */
   protected abstract String generateRowLimit(QueryType query);
   
   protected abstract String getSubSelectAlias();
   
   /**
    * generate the sql-statement for a query. If the query is
    * an advanced query the sql string of the query is return.
    * Otherwise the sql string will be build by the field and
    * filter information of the query.
    * @param query 
    * @throws com.sun.grid.arco.sql.SQLGeneratorException 
    * @return 
    */
   public String generate(QueryType query, Map lateBindings) throws SQLGeneratorException {
      
      String type = query.getType();
      if(ArcoConstants.ADVANCED.equals(type)) {
         return generateAdvanced(query, lateBindings);
      } else if(ArcoConstants.SIMPLE.equals(type)) {
         String sql = generateSimple(query, lateBindings);
         query.setSql(sql);
         return sql;
      } else {
         throw new SQLGeneratorException("sqlgen.invalidQueryType"
                                         , new Object[] {type });
      }
   }
   
   protected String generateAdvanced(QueryType query, Map lateBindings) {
      
      String sql = query.getSql();
      
      StringBuffer buf = new StringBuffer();
      
      ArrayList sortedFilter = new ArrayList(query.getFilter());
      

      Collections.sort(sortedFilter, new Comparator() {
         
         public int compare(Object o1, Object o2) {
            return ((Filter)o1).getStartOffset() - 
                   ((Filter)o2).getStartOffset();     
         }
         
      } );
      
      Iterator iter = sortedFilter.iterator();
      Filter filter = null;
      
      int lastOffset = 0;
      Object lb = null;
      while(iter.hasNext()) {
         filter = (Filter)iter.next();
         
         if( filter.isActive() && filter.isLateBinding() ) {            
            buf.append( sql.substring(lastOffset, filter.getStartOffset() ) );
            if( lateBindings != null ) {
               lb = lateBindings.get(filter.getName());
            } else {
               lb = null;
            }
            if( lb != null ) {
               if( filter.isSetCondition() ) {
                  buf.append( filter.getCondition() );
                  buf.append(" ");
               }
               buf.append( lb );
            }
            lastOffset = filter.getEndOffset();
         }
      }
      
      if( lastOffset < sql.length() ) {
         buf.append( sql.substring(lastOffset));
      }
      
      return buf.toString();

   }
   
   
   /**
    * Generate the sql statement for a simple query
    * @param query   the simple query
    * @throws com.sun.grid.arco.sql.SQLGeneratorException 
    *         if the query contains insufficient information 
    * @return the sql statemet
    */
   protected String generateSimple(QueryType query, Map lateBindings ) throws SQLGeneratorException {
      
      // build the select statement
      StringBuffer select = new StringBuffer();
      StringBuffer from = new StringBuffer();
      StringBuffer where = new StringBuffer();
      StringBuffer group = new StringBuffer();
      StringBuffer order = new StringBuffer();
      StringBuffer limit = new StringBuffer();
      
      com.sun.grid.arco.Util.correctFieldNames(query);
      
      select.append("SELECT ");
      boolean hasAggregateFunction = false;
      String groupByValues = null;
      
      Iterator fieldIter = query.getField().iterator();
      Field field = null;
      
      FieldFunction fieldFunction = null;
      
      while( fieldIter.hasNext() && !hasAggregateFunction) {
         field = (Field)fieldIter.next();         
         fieldFunction = getFieldFunction(field);

         hasAggregateFunction |= fieldFunction.isAggreagate();         
      }
      
      fieldIter = query.getField().iterator();
      
      while (fieldIter.hasNext()) {
         field = (Field)fieldIter.next();         
         fieldFunction = getFieldFunction(field);

         select.append( generateFieldName(field, fieldFunction) );
         
         if (hasAggregateFunction && !fieldFunction.isAggreagate()) {
            if (groupByValues != null) {
               groupByValues += ",";
               groupByValues += generateFieldName(field, fieldFunction);
            } else {
               groupByValues = generateFieldName(field, fieldFunction);
            }
         }
         
         if( field.getReportName() != null ) {
            select.append( " AS \"" );
            select.append( field.getReportName() );
            select.append( "\"" );
         }
         
         if( fieldIter.hasNext() ) {
            select.append(", ");
         }
      }
      
      // build the from-clause and appen to sql-statement
      from.append("FROM");
      from.append(' ' );
      from.append( query.getTableName() );
      
      // group by ( field of values)
      if (hasAggregateFunction && groupByValues != null) {
         // the group statement is only necessary if there is at least
         // one aggregate function and one fieldvalue selected.
         group.append("GROUP BY ");
         group.append(groupByValues);
      }
      
      List extendsFilters = null;
      
      if (!query.getFilter().isEmpty()){
         // build the where-clause and append to sql-statement                  
         Iterator filterIter = query.getFilter().iterator();         
         boolean isFirstFilter = true;
         Filter  filter = null;
         LogicalConnection lc = null;
         FilterType ft = null;
         
         String param = null;
         String filterName = null;
         
         while (filterIter.hasNext()){
            filter = (Filter)filterIter.next();
      
            if( !filter.isActive() ) {
               continue;
            }
            
            // Test if the fiter filters a user defined field
            field = getFieldForFilter(query, filter);
            if( field != null  ) {
               fieldFunction = getFieldFunction(field);
               // If the field is function then we need a subselect               
               // the result if the subselect will be filtered by this
               // filter
               if( fieldFunction != FieldFunction.VALUE ) {
                  if( extendsFilters == null ) {
                     extendsFilters = new ArrayList();
                  }
                  extendsFilters.add(filter);
                  continue;
               } else {
                  filterName = field.getDbName();
               }
            } else {
               // We are filtering a column which is not defined
               // in the query
               filterName = filter.getName();
            }
            
            if( isFirstFilter ) {
               where.append("WHERE ");
               isFirstFilter = false;
            } else {
               lc = getLogicalConnection(filter);
               where.append( ' ' );
               where.append( lc.getSymbol() );
               where.append( ' ' );
            }
            
            buildFilterExpression(filter, filterName, lateBindings, where);

         } // end of while
      }
      
      
      // order by
      SortType sortType = null;
      fieldIter = query.getField().iterator();
      boolean isFirstSort = true;
      while(fieldIter.hasNext()) {
         field = (Field)fieldIter.next();
         
         if( field.isSetSort() && field.getSort() != null ) {            
            sortType = SortType.getSortTypeByName(field.getSort());
            if( sortType == null ) {
               throw new SQLGeneratorException("sqlgen.field.invalidSort",
                       new Object[] { field.getDbName(), field.getSort() } );
            } else if ( sortType != SortType.NOT_SORTED ) {
               if( isFirstSort ) {
                  order.append("ORDER BY ");
                  isFirstSort = false;
               } else {
                  order.append(", ");
               }
               order.append( generateFieldName(field) );
               order.append( ' ' );
               order.append( sortType.getName() );
            }
         }
      }
      
      // limit
      if (query.isSetLimit() && query.getLimit() > 0){
         
         if( where.length() == 0 ) {
            where.append( "WHERE ");
         } else {
            where.append( " AND ");
         }
         where.append( generateRowLimit(query) );
      }
      
      
      // build statement here
      StringBuffer sql = new StringBuffer();
      sql.append(select);
      sql.append( ' ' );
      sql.append( from );
      if( where.length() > 0 ) {
         SGELog.finer("where is ''{0}''", where);
         sql.append( ' ' );
         sql.append( where );
      }
      if( group.length() > 0 ) {
         SGELog.finer("group is ''{0}''", group);
         sql.append( ' ' );
         sql.append( group );
      }
      
      if( order.length() > 0 ) {
         SGELog.finer("order is ''{0}''", order );
         sql.append( ' ' );
         sql.append( order );
      }
      
      
      if( extendsFilters != null ) {
         String subselect = sql.toString();
         sql.setLength(0);
         sql.append( "SELECT * FROM ( ");
         sql.append( subselect );
         sql.append( ") " );
         sql.append( getSubSelectAlias() );
         sql.append( " WHERE " );
         
         Iterator filterIter = extendsFilters.iterator();
         Filter filter = null;
         LogicalConnection lc = null;
         boolean isFirstFilter = true;
         while( filterIter.hasNext() ) {
            filter = (Filter)filterIter.next();
            if( isFirstFilter ) {
               isFirstFilter = false;
            } else {
               lc = getLogicalConnection(filter);
               sql.append( ' ' );
               sql.append( lc.getSymbol() );
               sql.append( ' ' );
            }
            buildFilterExpression(filter, "\"" + filter.getName() + "\"", lateBindings, sql);
         }         
      }

      
      String ret = sql.toString();
      SGELog.fine( "ret = " , ret );
      return ret;
      
      
   }
   
   private void buildFilterExpression(Filter filter, String filterName, Map lateBindings, StringBuffer where ) 
      throws SQLGeneratorException {      
      FilterType ft = getFilterType(filter);
      String param = null;
      
      where.append( filterName );
      where.append(' ');
      
      if( filter.isLateBinding() ) {
         if( lateBindings != null ) {
            param = (String)lateBindings.get(filter.getName());
         } else {
            param = "";
         }
      } else {
         param = filter.getParameter();
      }
      where.append( ft.getSymbol() );
      if( ft.getParameterCount() > 0 ) {
         where.append( ' ' );
         if (ft == FilterType.IN) {
            where.append( '(' );
            where.append(param);
            where.append( ')' );
         } else if (ft == FilterType.BETWEEN) {
            where.append(param);
         } else {
            where.append( '\'' );
            where.append(param);
            where.append( '\'');
         }
      }
   }
   
   public static boolean hasActiveFilter(QueryType query) {
      Iterator iter = query.getFilter().iterator();
      Filter filter = null;
      while(iter.hasNext()) {
         filter = (Filter)iter.next();
         if( filter.isActive() ) {
            return true;
         }
      }
      return false;
   }
   
   private FilterType getFilterType(Filter filter)
      throws SQLGeneratorException {
      String type = filter.getCondition();
      if( type == null || type.length() == 0 ) {
         throw new SQLGeneratorException("sqlgen.filter.emptyCondition",
                 new Object[] { filter.getName() } );
      }
      FilterType ret = FilterType.getFilterTypeByName(type);
      if( ret == null ) {
         throw new SQLGeneratorException("sqlgen.filter.unknownCondition",
                 new Object[] { filter.getName(), type } );
      }
      return ret;
   }
   
   
   private Field getFieldForFilter(QueryType query, Filter filter) {
      Iterator iter = query.getField().iterator();
      Field field = null;
      while( iter.hasNext() ) {
         field = (Field)iter.next();
         if( field.getReportName().equals( filter.getName())) {
            return field;
         }
      }
      return null;
   }
   
   
   private LogicalConnection getLogicalConnection(Filter filter)
      throws SQLGeneratorException {
   
      String name = filter.getLogicalConnection();
      if( name == null || name.length() == 0 ) {
         throw new SQLGeneratorException("sqlgen.filter.emptyLC",
                 new Object[] { filter.getName() } );
      }
      LogicalConnection ret = LogicalConnection.getLogicalConnectionByName(name);
      if( ret == null ) {
         throw new SQLGeneratorException("sqlgen.filter.unknownLC",
                 new Object[] { filter.getName(), name } );         
      }
      return ret;
   }
   
   /**
    * The the FieldFunction object of a field
    * @param field  the field
    * @throws com.sun.grid.arco.sql.SQLGeneratorException if the field has
    *          no or an unknown function
    * @return  the FieldFunction object
    */
   private FieldFunction getFieldFunction(Field field)
      throws SQLGeneratorException {
      if( field.getFunction() == null ) {
         throw new SQLGeneratorException("sqlgen.field.emptyFunction", 
                    new Object [] { field.getDbName() } );
      }
      FieldFunction ret = FieldFunction.getFieldFunctionByName(field.getFunction());
      if( ret == null ) {
         throw new SQLGeneratorException("sqlgen.field.unknownFunction", 
                   new Object[] { field.getDbName(), field.getFunction() } );
      }
      return ret;
   }
   
   private String generateFieldName(Field field) throws SQLGeneratorException {      
      return generateFieldName(field, getFieldFunction(field));
   }
   
   private String generateFieldName(Field field, FieldFunction function) 
        throws SQLGeneratorException {
      String dbName = field.getDbName();
      if( dbName == null || dbName.length() == 0 ) {
         throw new SQLGeneratorException("sqlgen.field.emptyDbName");
      }
      
      if (FieldFunction.VALUE == function) {
         return dbName;
      } else {         
         if (FieldFunction.ADDITION == function ||
             FieldFunction.SUBSTRACTION == function ||
             FieldFunction.DIVISION == function ||
             FieldFunction.MULIPLY== function) {
            String parameter = field.getParameter();
            if( parameter == null || parameter.length() == 0 ) {
               throw new SQLGeneratorException("field {0} with function {1} requires a parameter", 
                       new Object[] { dbName, function.getName() } );
            }
            StringBuffer ret = new StringBuffer();
            ret.append("(");
            ret.append(dbName);
            ret.append(function.getName());
            ret.append(parameter);
            ret.append(")");
            return ret.toString();
         } else {
            StringBuffer ret = new StringBuffer();
            ret.append(function.getName());
            ret.append("(");
            ret.append(dbName);
            ret.append(")");
            return ret.toString();
         }
      }
   }
   
}
