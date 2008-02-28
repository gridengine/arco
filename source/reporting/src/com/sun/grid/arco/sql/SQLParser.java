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
import java.text.ParseException;

public class SQLParser {
   
   SQLLexer scanner;
   
   ArrayList fieldList = new ArrayList();
   ArrayList lateBindingList = new ArrayList();
   
   /** Creates a new instance of SQLParser */
   public SQLParser( String sql ) {
      scanner = new SQLLexer( sql );   
   }
   
   private int scan() {
      return scanner.scan();
   }
   
   private void test( int token ) throws ParseException {      
      if( token() != token ) {
         throw error( token );
      }      
   }
   
   private void testAndScan( int token ) throws ParseException {
      test( token );
      scan();
   }
   
   private ParseException error( int expectedToken ) {
      return error( "Token '" + SQLLexer.tokenToStr( expectedToken ) + "' expected, but got " + scanner.getTokenString() );
   }
   
   private ParseException error( int[] tokens ) {
      
      StringBuffer msg = new StringBuffer();
      msg.append( "Token " );      
      for( int i = 0; i< tokens.length - 1; i++ ) {
         msg.append( scanner.tokenToStr( tokens[i] ) );
         msg.append( ' ');
      }
      msg.append( "or ");
      msg.append( scanner.tokenToStr( tokens[tokens.length - 1] ) );
      msg.append( " expected, but got ");
      msg.append( scanner.getTokenString() );
      return error( msg.toString() );
   }
   
   private ParseException error( String text ) {      
      return new ParseException( text, scanner.getOffset() );
   }
   
   public ArrayList getFieldList() {
      return fieldList;
   }
   
   public List getLateBindingList() {
      return lateBindingList;
   }
   
   private int token() {
      return scanner.getToken();
   }
   
   
   public void parse() throws ParseException {      
      scan();
      testAndScan( SQLLexer.TOKEN_SELECT );
      if( token() == SQLLexer.TOKEN_STRING &&
          scanner.getTokenText().equalsIgnoreCase("distinct") ) {
         scan();
      }
      parseFields();
      parseLateBindings();
   }
   
   private void parseFields() throws ParseException {
      
      Field field = parseField();
      fieldList.add( field );
      while( token() == SQLLexer.TOKEN_COMMA  ) {
         scan();
         field = parseField();
         fieldList.add( field );
      }
      
   }
   
   private void parseLateBindings() throws ParseException {
      
      while( token() != SQLLexer.TOKEN_EOF ) {
         if( token() == SQLLexer.TOKEN_LATEBINDING ) {
            lateBindingList.add(parseLateBinding());
         } else {
            scan();
         }
      }
      
   }
   private LateBinding parseLateBinding() throws ParseException {
      int start = scanner.getOffset() - 11;
      testAndScan(SQLLexer.TOKEN_LATEBINDING);      
      testAndScan( SQLLexer.TOKEN_BRACKET_3 );
      test( SQLLexer.TOKEN_STRING );
      LateBinding ret = new LateBinding(scanner.getTokenText());      
      scan();
      testAndScan( SQLLexer.TOKEN_SEMICOLON );
      if( token() == SQLLexer.TOKEN_STRING) {
         ret.setOperator(scanner.getTokenText());
         scan();
      }
      testAndScan( SQLLexer.TOKEN_SEMICOLON );
      
      StringBuffer buf = new StringBuffer();
      
      boolean notFirstParam = false;
      while( token() != SQLLexer.TOKEN_BRACKET_4 ) {
         if( token() == SQLLexer.TOKEN_EOF ) {
            throw error(SQLLexer.TOKEN_BRACKET_4);
         }
         if( notFirstParam ) {
            buf.append(' ');
         } else {
            notFirstParam = true;
         }
         buf.append( scanner.getTokenText() );
         scan();
      }
      ret.setParams( buf.toString() );
      ret.setStart( start );
      ret.setEnd( scanner.getOffset() );
      
      return ret;
   }
   
   private Field parseField()  throws ParseException {
      Field ret = new Field();
      StringBuffer expr = new StringBuffer();
      parseExpr( expr );
      ret.setExpr( expr.toString() );
      if( token() == SQLLexer.TOKEN_AS ) {
         scan();
         test( SQLLexer.TOKEN_STRING );
         ret.setName( scanner.getTokenText() );
         scan();
      }
      return ret;
   }
   
   private void parseExpr( StringBuffer buf ) throws ParseException {
      StringBuffer ret = new StringBuffer();
      
      switch ( token() ) {
         case SQLLexer.TOKEN_BRACKET_1:
            parseBracket( buf );
            break;
         case SQLLexer.TOKEN_STRING:
            parseStringList( buf );
            break;
         default:
            throw error( new int[] { SQLLexer.TOKEN_BRACKET_1, SQLLexer.TOKEN_STRING } );
      }
      
      outer:
      while( true )
      {
         switch ( token() ) {
            case SQLLexer.TOKEN_BRACKET_1:
               parseBracket( buf );
               break;
            case SQLLexer.TOKEN_STRING:
               parseStringList( buf );
               break;
            default:
              break outer;
         }
      }
   }
   
   private void parseBracket( StringBuffer buf ) throws ParseException {
      test( SQLLexer.TOKEN_BRACKET_1 );
      if( buf.length() > 0 ){
         buf.append( ' ' );
      }
      int offset = scanner.getOffset();
      buf.append( scanner.getTokenText() );
      scan();
      boolean first = true;
      outer:
      while( true ) {
         switch( token() ) {
            case SQLLexer.TOKEN_BRACKET_2:
               buf.append( scanner.getTokenText() );
               scan();
               break outer;
            case SQLLexer.TOKEN_EOF:
               throw error( "Unmatched " + scanner.tokenToStr( SQLLexer.TOKEN_BRACKET_1 ) + "" +
                            " at offset " + offset );
            case SQLLexer.TOKEN_BRACKET_1:
               parseBracket( buf );
               break;
            default:
               if( first ) {
                  first = false;
               } else {
                  buf.append( ' ' );
               }
               buf.append( scanner.getTokenText() );
               scan();
         }
      }
   }
   
   private void parseStringList( StringBuffer buf ) throws ParseException {
      test( SQLLexer.TOKEN_STRING );
      if( buf.length() > 0 ) {
         buf.append( ' ' );
      }
      buf.append( scanner.getTokenText() );
      scan();
      while( token() == SQLLexer.TOKEN_STRING ) {
         buf.append( ' ' );
         buf.append( scanner.getTokenText() );
         scan();
      }
   }
   
   
   public static class Field {
      private String expr;;
      private String name;
      
      public Field() {
         
      }
      
      public void setExpr( String s ) {
         expr = s;
      }
      
      public String getExpr()
      {
         return expr;
      }
      
      public void setName( String name ) {
         if( name.length() > 0 ) {
            int startIndex = 0;
            char c = name.charAt(0);
            if( c == '"' || c == '\'' ) {
               startIndex++;          
            }
            int endIndex = name.length();
            if( endIndex > 0 ) {
               c = name.charAt(endIndex-1);
               if( c == '"' || c == '\'' ) {
                  endIndex--;
               }
            }
            this.name = name.substring(startIndex,endIndex);
         } else {
            this.name = name;
         }
      }
      
      public String getName() {
          return name;
      }
      
   }
   
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {

      
      String sql = "SELECT (SELECT x , trunc( (xxx, yy ) from x) as 'a b', b as b , ( xxx + xc ) as s , c from xxx where sfasdfasdf sdfasf" +
              " WHERE x = LATEBINDING { LB1, bludere ( ( ( (, ass \"aaa\" ) }";
            
      SQLParser p = new SQLParser( sql );
      
      try {
         p.parse();

         Iterator iter = p.getFieldList().iterator();
         Field field = null;
         while( iter.hasNext() ) {
            field = (Field)iter.next();
            System.out.print( field.getName() );
            System.out.print( " -> ");
            System.out.println( field.getExpr() );
         }
         
         System.out.println( "LateBindings ----------------------------");
         LateBinding lb = null;
         iter = p.getLateBindingList().iterator();
         while( iter.hasNext() ) {
            lb = (LateBinding)iter.next();
            System.out.print( "[");
            System.out.print( lb.getStart() );
            System.out.print( ",");
            System.out.print( lb.getEnd() );
            System.out.print( "]: " );
            System.out.print( lb.getName() );
            System.out.print( ", " );
            System.out.print( lb.getParams() );
         }
         
      } catch( ParseException pe ) {
         System.err.println("parse error: " + pe.getMessage() );
         pe.printStackTrace();
      }
      
   }
   
}
