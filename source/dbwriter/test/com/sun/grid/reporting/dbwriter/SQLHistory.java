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
package com.sun.grid.reporting.dbwriter;

import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.DatabaseListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * <P>This class makes it possible to watch the database activities of the dbwriter
 * Each sql statement which is executed by database object of the dbwriter is stored
 * this sql history.</P>
 * <P>An instance of this cache must be registerd as <code>DatebaseListener</code> of the
 * the dbwriters database:</P>
 * <P><B>Example:</B></P>
 * <pre>
 *   ReportingDBWriter dbw = ...;
 *   SQLHistory sqlh = new SQLHistory();
 *   ...
 *   dbw.initialize();
 *   dbw.getDatabase().addDatabaseListener(sqlh);
 *   dbw.start();
 *   ...
 *
 *   SQLException error [] = new SQLException[1];
 *   long timeout  = 1000;
 *
 *   boolean flag = sqlh.waitForSqlStatementAndClear("SELECT * FROM foo", timeout, error);
 *
 *   if( flag ) {
 *      System.out.println("sql has been executed");
 *      if( error[0] != null ) {
 *         System.out.println("sql produced an error");
 *      }
 *   }
 * </pre>
 *
 */
public class SQLHistory implements DatabaseListener {
  
  private LinkedList statements = new LinkedList();
  
  /** Creates a new instance of SQLCache */
  public SQLHistory() {
  }
  
  /**
   * is invoked if statement has been successfully executed
   * @param statement  the statement
   */
  public void sqlExecuted(String statement) {
    SGELog.fine("Statement executed: " + statement);
    synchronized(statements) {
      statements.add(new CacheElement(statement, System.currentTimeMillis()));
      statements.notifyAll();
    }
  }
  
  /**
   * is invoked if a statement has procued an error
   * @param statement  the statement
   * @param error      the error
   */
  public void sqlFailed(String statement, java.sql.SQLException error) {
    synchronized(statements) {
      SGELog.fine("Statement failed: {0}", statement);
      statements.add(new CacheElement(statement, System.currentTimeMillis(), error));
      statements.notifyAll();
    }
  }
  
  /**
   * Wait for a sql command. If it is found in the cache all statements which has
   * been executed before this statement and the statement itself is removed
   * from the cache.
   *
   * @param stmt     the sql statement
   * @param timeout  timeount in milliseconds
   * @param error    if the sql statement has produced an error this error is
   *                 stored in <code>error[0]</code>
   * @throws java.lang.InterruptedException if the is interrupted while waiting
   *                                        for the sql statement
   * @return true of the statement has been found in the cache
   */
  public boolean waitForSqlStatementAndClear(String stmt, long timeout, SQLException[] error) throws InterruptedException {
    return  processPatterns(new String[]{stmt}, timeout, error, false);
  }
  
  /**
   * Wait for a sql command. If it is found in the cache all statements which has
   * been executed before this statement and the statement itself is removed
   * from the cache.
   *
   * @param pat     the sql regular expresion pattern
   * @param timeout  timeount in milliseconds
   * @param error    if the sql statement has produced an error this error is
   *                 stored in <code>error[0]</code>
   * @throws java.lang.InterruptedException if the is interrupted while waiting
   *                                        for the sql statement
   * @return true of the statement has been found in the cache
   */
  public boolean waitForSqlPatternAndClear(String pat, long timeout, SQLException[] error) throws InterruptedException {
    return  processPatterns(new String[]{pat}, timeout, error, true);
  }
  
  /**
   * Wait for a sql command. If it is found in the cache all statements which has
   * been executed before this statement and the statement itself is removed
   * from the cache.
   *
   * @param patterns the array of sql regular expresion patterns
   * @param timeout  timeount in milliseconds
   * @param error    if the sql statement has produced an error this error is
   *                 stored in <code>error[0]</code>
   * @throws java.lang.InterruptedException if the is interrupted while waiting
   *                                        for the sql statement
   * @return true of the statement has been found in the cache
   */
  public boolean waitForSqlPatternsAndClear(String[] patterns, long timeout, SQLException[] error) throws InterruptedException {
    return  processPatterns(patterns, timeout, error, true);
  }
  
  private boolean processPatterns(String[] patterns, long timeout, SQLException[] error,boolean expr) throws InterruptedException {
    return  processPatternList(Arrays.asList(patterns), timeout, error, expr);
  }
  
  private boolean processPatternList(List patternList, long timeout, SQLException[] error,boolean expr) throws InterruptedException {
    ArrayList patterns= new ArrayList(patternList);    
    
    long endTime = System.currentTimeMillis() + timeout;
    synchronized(statements) {
      
      if(isSqlStatementAvailable(patterns, error, expr)) {
        return true;
      }
      
      while( System.currentTimeMillis() < endTime ) {
        statements.wait(timeout);
        if(isSqlStatementAvailable(patterns, error, expr)) {
          return true;
        }
      }
    }
    return false;
  }
  
  private boolean isSqlStatementAvailable(List patterns, SQLException[] error, boolean expr) {   
    synchronized(statements) {
      if( statements.isEmpty()) {
        SGELog.fine("No sql statement available");
        return false;
      }
      
      Iterator iter = statements.iterator();
      int index = 0;
      ArrayList clearList = new ArrayList(statements.size());
      while(iter.hasNext() && !patterns.isEmpty()) {
        CacheElement elem = (CacheElement)iter.next();
        clearList.add(elem);
        ArrayList clearPattternList = new ArrayList();
        for (Iterator it = patterns.iterator(); it.hasNext();) {
          String pat = (String) it.next();
          if(isMatched(elem.statement,pat,expr)) {
            SGELog.fine("Found sql pattern ''{0}'' match to''{1}''", pat, elem.statement);
            clearPattternList.add(pat);
            if( error != null && error.length > 0 && index < error.length) {
              error[index++] = elem.error;
            }
          } else {
            SGELog.finest("sql pattern ''{0}'' not match to''{1}''", pat, elem.statement );
          }
        } // end of patternList
        patterns.removeAll(clearPattternList);
        clearPattternList.clear();
      } // end of statements
      statements.removeAll(clearList);
      clearList.clear();
    }
    return patterns.isEmpty();
  }
  
  private boolean isMatched(String statement, String pattern, boolean expr) {
    if(expr) return statement.matches(pattern);
    return statement.indexOf(pattern) >= 0;
  }
  
  private static class CacheElement {
    
    private String statement;
    private long   ts;
    private SQLException error;
    
    public CacheElement(String statement, long ts, SQLException error) {
      this.statement = statement;
      this.ts = ts;
      this.error = error;
    }
    
    public CacheElement(String statement, long ts) {
      this(statement,ts,null);
    }
    
  }
  
}
