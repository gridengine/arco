
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
 *   Copyright: 2007 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/

package com.sun.grid.reporting.dbwriter;

import java.sql.Timestamp;
import java.util.List;


public interface DeleteManager {
   
   /** All ReportingObjectManager(s) that are also DeleteManagers need to
    *  implement this method. It should return DELETE sql statement that
    *  will be used to delete records based on the supplied delete rules.
    *
    *  Delete Rules are specified in dbwriter.xml
    *
    *  The delete SQL String returned by this method must contain the delete
    *  limit, so only certain amount of rows is deleted in one transaction.
    *
    *  To obtain the delete limit String SQL call super.getDeleteLimit() and
    *  append it to your delete statement
    *
    *
    *
    *  Example of Implementation (Oracle, Postgres):
    *  =============================================
    *
    *  StringBuffer sql = new StringBuffer("DELETE FROM sge_job_log WHERE jl_id IN " +
    *        "(SELECT jl_id from sge_job_log WHERE jl_time < ");
    *  sql.append(DateField.getValueString(time));
    *
    *  sql.append(super.getDeleteLimit());
    *
    *  sql.append(")");
    *
    *  String result[] = new String[1];
    *  result[0] = sql.toString();
    *  return result;
    *
    *  WARNING: There is a different implementation for MySQL. For MySQL this method must
    *           return a select statement. This is then handled in
    *           ReportingDBWriter.processMySQLDeletes()
    *
    *  MySQL does not allow the limit keyword in subqueries, so this will not work:
    *  SELECT * FROM t1 WHERE s1 IN (SELECT s2 FROM t2 ORDER BY s1 LIMIT 1)
    *
    *  Also MySQL does not store subqueries separately, so you cannot use the same
    *  table (in the case above sge_job_log) for both the subquery's FROM clause and
    *  the DELETE (UPDATE) target.
    *
    *  Example of Implementation (MySQL):
    *  =============================================
    *  StringBuffer sql = new StringBuffer("SELECT jl_id FROM sge_job_log WHERE jl_time < ");
    *  sql.append(DateField.getValueString(time));
    *
    *  sql.append(super.getDeleteLimit());
    *
    *  String result[] = new String[1];
    *  result[0] = sql.toString();
    *  return result;
    *
    */
   public String [] getDeleteRuleSQL(Timestamp time, List subScope);
   
}
