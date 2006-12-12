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

import com.sun.grid.arco.model.*;
import com.sun.grid.arco.sql.*;
import com.sun.grid.arco.*;
import java.util.*;
import javax.xml.bind.JAXBException;

/**
 * In the version 0 of queries the field list of advanced
 * queries are not stored in the xml file.
 * This Upgrader parse the sql string of the advanced query and build the
 * field list.
 */
public class AdvancedFieldListUpgrader extends AbstractUpgrader {
   
   /** Creates a new instance of AdvancedFieldListUpgrader */
   public AdvancedFieldListUpgrader() {
      super(1);
   }

   /**
    * update the named object
    * @param obj  the named object
    * @throws com.sun.grid.arco.upgrade.UpgraderException 
    */
   public void upgrade(NamedObject obj) throws UpgraderException {
      
      if( obj instanceof Query ) {
         Query query = (Query)obj;
         
         try {
            QueryResult.parseAdvancedSQL(query);
         } catch( java.text.ParseException pe ) {
            UpgraderException upex = new UpgraderException("Unable to parse sql");
            upex.initCause(pe);
            throw upex;
         }
      }
   }
   
}
