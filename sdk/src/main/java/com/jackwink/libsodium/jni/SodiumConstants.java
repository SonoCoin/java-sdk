/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.jackwink.libsodium.jni;

public interface SodiumConstants {
  public final static int CRYPTO_SIGN_BYTES = 64;
  public final static int CRYPTO_SIGN_SEEDBYTES = 32;
  public final static int CRYPTO_SIGN_PUBLICKEYBYTES = 32;
  public final static int CRYPTO_SIGN_SECRETKEYBYTES = (32+32);
  public final static int CRYPTO_BOX_SEEDBYTES = 32;
  public final static int CRYPTO_BOX_PUBLICKEYBYTES = 32;
  public final static int CRYPTO_BOX_SECRETKEYBYTES = 32;
  public final static int CRYPTO_BOX_NONCEBYTES = 24;
  public final static int CRYPTO_BOX_MACBYTES = 16;
  public final static int CRYPTO_SECRETBOX_KEYBYTES = 32;
  public final static int CRYPTO_SECRETBOX_MACBYTES = 16;
  public final static int CRYPTO_SECRETBOX_NONCEBYTES = 24;
  public final static int CRYPTO_AUTH_BYTES = 32;
  public final static int CRYPTO_AUTH_KEYBYTES = 32;
  public final static int CRYPTO_AED_CHACHA20POLY1305_KEYBYTES = 32;
  public final static int CRYPTO_AED_CHACHA20POLY1305_NONCEBYTES = 32;
  public final static int CRYPTO_AED_CHACHA20POLY1305_MACBYTES = 16;
  public final static int CRYPTO_PWHASH_SALTBYTES = 32;
  public final static int CRYPTO_PWHASH_HASHBYTES = 102;
  public final static int CRYPTO_PWHASH_KEY_BYTES = 32;
  public final static int CRYPTO_PWHASH_OPSLIMIT_INTERACTIVE = 524288;
  public final static int CRYPTO_PWHASH_MEMLIMIT_INTERACTIVE = 16777216;
  public final static int CRYPTO_PWHASH_OPSLIMIT_SENSITIVE = 33554432;
  public final static int CRYPTO_PWHASH_MEMLIMIT_SENSITIVE = 1073741824;
  public final static int CRYPTO_SHORTHASH_BYTES = 8;
  public final static int CRYPTO_SHORTHASH_KEYBYTES = 16;
  public final static int CRYPTO_GENERICHASH_BYTES = 32;
  public final static int CRYPTO_GENERICHASH_KEYBYTES = 32;
}