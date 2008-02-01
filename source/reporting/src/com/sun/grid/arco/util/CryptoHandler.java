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
package com.sun.grid.arco.util;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.*;

/**
 * <p><code>CryptoHandler</code>
 * </p>
 *
 */
public class CryptoHandler{
  
  private static final String algorithm = "DES";
  private static final byte[] key = new byte[] {7, -88, 48, -25, 17, -34, 59, -13};
  
  private static SecretKeySpec skeySpec;
  private static Cipher cipher;
  
  /**
   * Creates new CryptoHandler
   */
  public CryptoHandler()  {
  }
  
  /**
   */
  private static void generateKey() {
    try {
      skeySpec = new SecretKeySpec(key, algorithm);
      cipher = Cipher.getInstance(algorithm);
    } catch (NoSuchAlgorithmException nsaEx) {
      throw new IllegalStateException("Algorithm for encryption not found");
    }catch (Exception nsaEx) {
      throw new IllegalStateException("Unable to instanciate values for en-/decryption");
    }
  }
  
  /**
   * @param text
   * @return
   */
  public static String encrypt(String text) {
    String retVal = null;
    try {
      generateKey();
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
      
      byte[] encrypted = cipher.doFinal(text.getBytes());
      
      retVal = new String(asHex(encrypted));
//      retVal = new String(encrypted);
      
      return retVal;
    } catch (InvalidKeyException ikEx) {
      throw new IllegalStateException("The key for encryption might be invalid");
    } catch (Exception ibsEx) {
      throw new IllegalStateException("Blocksize for encryption is illegal");
    }
  }
  
  /**
   * @param encryptedText
   * @return
   */
  public static String decrypt(String encryptedText) {
    String retVal = null;
    try {
      generateKey();
      cipher.init(Cipher.DECRYPT_MODE, skeySpec);
      byte[] decrypted =  cipher.doFinal(fromHex(encryptedText));
      retVal = new String(decrypted);
      return retVal;
    } catch (InvalidKeyException ikEx) {
      throw new IllegalStateException("The key for encryption might be invalid");
    } catch (Exception ibsEx) {
      ibsEx.printStackTrace();
    }
    return "";
  }
  
  /**
   * Turns array of bytes into string
   *
   * @param buf	Array of bytes to convert to hex string
   * @return	Generated hex string
   */
  private static String asHex(byte buf[]) {
    StringBuffer strbuf = new StringBuffer(buf.length * 2);
    int i;
    
    for (i = 0; i < buf.length; i++) {
      if (((int) buf[i] & 0xff) < 0x10)
        strbuf.append("0");
      
      strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
    }
//    System.out.println(strbuf.toString().length());
    return strbuf.toString();
  }
  
  /**
   * @param buf
   * @return
   */  
  private static byte[] fromHex(String value) {
    if(value.length() % 2 != 0) {
      throw new IllegalArgumentException("The incoming Hex-value is corrupt");
    }
    
    byte[] retVal = new byte[value.length() / 2];
    for (int i=0; i<retVal.length ; i++) {
      retVal[i] = Integer.decode("0x"+value.substring(i*2,i*2+2)).byteValue();
//      System.out.print(retVal[i] + "\t");
    }
//    System.out.println();
    return retVal;
  }
  
  public static void usage() {
    System.out.println("usage:\n\tdbpwd <list of text>");
    System.out.println("The list of blank-separated text to encrypted");
  }
  
  public static void main( String [] args ) {
  
     try {
        if( args.length == 0 ) {
           BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
           
           System.err.println("Enter the password: ");
           String pw = in.readLine();
           args = new String[] { pw };
        }
        for( int i = 0; i< args.length; i++ ) {
           System.out.println( CryptoHandler.encrypt(args[i]) );
        }
     } catch( Exception e ) {
        System.err.println("ERROR: " + e.getMessage() );
     }
  }
  /**
   * @param args
   * @throws Exception
   */
  public static void OLDmain(String[] args) throws Exception {
    if (args.length < 1) {
      CryptoHandler.usage();
    }
    
    java.util.Vector decrypt = new java.util.Vector(0);
    java.util.Vector encrypt = new java.util.Vector(0);
    boolean encryption = true;
    
    for (int i=0; i<args.length; i++) {
      if(args[i].equals("-e")) {
        encryption = true;
      } else if(args[i].equals("-d")) {
        encryption = false;
      } else {
        if (encryption) {
          String value = 
            CryptoHandler.encrypt(args[i]);
//              CryptoHandler.asHex(args[i].getBytes());
          System.out.println(args[i] + "\t=>\t" + value);
          decrypt.add(value);
        } else {
          String value = 
            CryptoHandler.decrypt(args[i]);
//            new String(CryptoHandler.fromHex(args[i]));
          System.out.println(args[i] + "\t=>\t" + value);
          encrypt.add(value);
        }
      }
    }
    
//    java.util.Iterator undoIt = decrypt.iterator();
//    while (undoIt.hasNext()){
//      String value = undoIt.next().toString();
//      String result = 
//        CryptoHandler.decrypt(value);
////        new String(CryptoHandler.fromHex(value));
//      System.out.println(value + "\t=>\t" + result);
//    }
    
//    undoIt = encrypt.iterator();
//    while (undoIt.hasNext()){
//      String value = undoIt.next().toString();
//      String result = 
//        CryptoHandler.encrypt(value);
////        CryptoHandler.asHex(value.getBytes());
//      System.out.println(value + "=>" + result);
//    }
    
  }
} // end of class CryptoHandler
