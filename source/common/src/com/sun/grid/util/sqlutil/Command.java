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
package com.sun.grid.util.sqlutil;

import com.sun.grid.util.SQLUtil;
import java.sql.Connection;

/**
 * Abstract base class for all commands of the SQLUtil.
 */
public abstract class Command {

   /** handle on the sql util. */
   private SQLUtil sqlUtil;

   /** the name of the command. */
   private String name;

   /**
    *  Create a new command.
    *  @param aSqlUtil  the SQLUtil which instantiates this command
    *  @param aName     the name of the command
    */
   public Command(final SQLUtil aSqlUtil, final String aName) {
      this.sqlUtil = aSqlUtil;
      this.name = aName;
   }

   /**
    *   Get the name of the command.
    *   @return the name
    */
   public final String getName() {
      return name;
   }

   /**
    *   run the command.
    *   @param  args argument string of the command
    *   @return 0    Command succuessfully executed
    *           else error
    */
   public abstract int run(final String args);

   /**
    *  print a usage message.
    *  @return the usages message of this command
    */
   public abstract String usage();

   /**
    *   Get the connection of the SQLUtil.
    *   @return the connection
    */
   public final Connection getConnection() {
      return sqlUtil.getConnection();
   }

   /**
    *   Get the secondary connection of the SQLUtil.
    *   can be used for creating/dropping synonyms.
    *   @return the connection
    */
   public final Connection getConnection2() {
      return sqlUtil.getConnection2();
   }
   
   /**
    * get the sql util of this command.
    * @return the sql util
    */
   protected final SQLUtil getSQLUtil() {
      return sqlUtil;
   }
}
