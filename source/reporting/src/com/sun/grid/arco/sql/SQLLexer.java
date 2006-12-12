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

public class SQLLexer {
   
   public final static int TOKEN_STRING    = 1;
   public final static int TOKEN_SELECT    = 2;
   public final static int TOKEN_FROM      = 3;
   public final static int TOKEN_AS        = 4;
   public final static int TOKEN_COMMA     = 5;
   public final static int TOKEN_BRACKET_1 = 6;   // (
   public final static int TOKEN_BRACKET_2 = 7;   // )
   public static final int TOKEN_LATEBINDING = 8;   
   public static final int TOKEN_BRACKET_3 = 9;   // {
   public static final int TOKEN_BRACKET_4 = 10;  // }
   public static final int TOKEN_SEMICOLON = 11;
   public final static int TOKEN_EOF       = -1;
   public final static int TOKEN_ERROR     = -2;
   
   public int getToken() { return token; }
   public String getTokenText() {
      return tokenText.toString();
   }
   
   public int getOffset() {
      return pos;
   }
   
   public static String tokenToStr( int token ) {
      switch( token ) {
         case TOKEN_STRING: return "STR";
         case TOKEN_SELECT: return "SELECT";
         case TOKEN_FROM:   return "FROM";
         case TOKEN_AS:     return "AS";
         case TOKEN_COMMA:  return "','";
         case TOKEN_BRACKET_1: return "'('";
         case TOKEN_BRACKET_2: return "')'";
         case TOKEN_BRACKET_3: return "'{'";
         case TOKEN_BRACKET_4: return "'}'";
         case TOKEN_LATEBINDING: return "LATEBINDING";
         case TOKEN_SEMICOLON: return ";";
         case TOKEN_EOF: return "EOF";
         case TOKEN_ERROR: return "ERROR";
         default: return  "UNKNOWN(" + token + ")";
      }            
   }
   
   public String getTokenString() {
      return tokenToStr(token);
   }
   
   
   private String sql;
   private int pos;
   private int token = TOKEN_EOF;
   private char currentChar;
   private StringBuffer tokenText = new StringBuffer();
   
   /** Creates a new instance of SQLLexer */
   public SQLLexer( String sql ) {
      this.sql = sql;
      pos = -1;      
      
   }
   
   private boolean eof = false;
   
   private char nextChar() throws EOFException {
      pos++;
      if( eof ) {
         throw new EOFException();
      }
      if( eof() ) {
         eof = true;
         return (char)0;
      } else {
         currentChar = sql.charAt( pos );
         return currentChar;
      }
   }
   
   public boolean eof() {
      return pos >= sql.length();
   }
   
   
   public int scan() {
      
      try {
         tokenText.setLength( 0 );
         if( pos < 0 ) {
            nextChar();
         } 
         if ( eof() ) {
            token = TOKEN_EOF;
            return token;
         } 
         
         switch( currentChar ) {
            case ',':  token = TOKEN_COMMA; 
                       tokenText.append( currentChar );  
                       if( !eof() ) nextChar();
                       break;
            case ';':  token = TOKEN_SEMICOLON; 
                       tokenText.append( currentChar );  
                       if( !eof() ) nextChar();
                       break;
            case '(':  token = TOKEN_BRACKET_1;
                       tokenText.append( currentChar );                      
                       if( !eof() ) nextChar();
                       break;
            case ')':  token = TOKEN_BRACKET_2;
                       tokenText.append( currentChar );                      
                       if( !eof() ) nextChar();
                       break;
            case '{':  token = TOKEN_BRACKET_3;
                       tokenText.append( currentChar );                      
                       if( !eof() ) nextChar();
                       break;
            case '}':  token = TOKEN_BRACKET_4;
                       tokenText.append( currentChar );                      
                       if( !eof() ) nextChar();
                       break;
            case '\'': 
            case '\"': scanLiteral(); break;
            default:
            
            if( Character.isWhitespace( currentChar ) ) {
               while( Character.isWhitespace( currentChar ) ) {
                  nextChar();
               }
               return scan();
            } else {
               // Normal String
               try {
                  outer:
                  do {
                    tokenText.append( currentChar );
                    nextChar();
                    switch( currentChar ) {
                       case '(': break outer;
                       case ')': break outer;
                       case ',': break outer;
                       case ';': break outer;
                       case '}': break outer;
                       case '{': break outer;                       
                       default:
                          if( Character.isWhitespace( currentChar ) ) {
                             break outer;
                          }                          
                    }
                  } while( true );
               } catch( EOFException eofe ) {}
               
               String str = tokenText.toString();
               
               if( str.equalsIgnoreCase( "AS" ) ) {
                  token = TOKEN_AS;
               } else if ( str.equalsIgnoreCase( "SELECT" ) ) {
                  token = TOKEN_SELECT;
               } else if ( str.equalsIgnoreCase( "FROM" ) ) {
                  token = TOKEN_FROM;
               } else if ( str.equalsIgnoreCase( "LATEBINDING") ) {
                  token = TOKEN_LATEBINDING;
               } else {
                  token = TOKEN_STRING;
               }
            }
         }
      } catch( EOFException eofe ) {
         token = TOKEN_EOF;
      }
      return token;
   }
   
   
   private int scanLiteral() throws EOFException {
      
      char lit = currentChar;
      tokenText.append( lit );
      while( !eof() ) {
         nextChar();
         if( currentChar == lit ) {            
            if( eof() ) {
               tokenText.append( lit );
               break;
            } else {
               tokenText.append( lit );
               nextChar();
               if( currentChar != lit ) {
                  break;
               }
            } 
         } else {
            tokenText.append( currentChar );
         }
      }
      token = TOKEN_STRING;
      return token;
   }
   
   
   private static class EOFException extends Exception {
      public EOFException() {
         super("EOF");
      }
   }
   
   
   private static void scanRun( String sql ) {
      
      SQLLexer lexer = new SQLLexer(sql);
      System.out.println(sql);
      int token = 0;
      do {
         token = lexer.scan();
         System.out.print( lexer.getTokenString() );
         System.out.print( ' ' );
      } while( token > 0 );
      
      System.out.println();
   }
   
   public static void main( String[] args ) {
      
      scanRun( "SELECT  aa as a, bb as b from xxx ");
      
      scanRun( "SELECT  trunc( xx + yy, \n 'day''()' ) as \"aa\", bb as b from xxx ");
      
   }
   
   
}
