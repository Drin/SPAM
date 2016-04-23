package com.drin.util;

@SuppressWarnings("serial")
public class InvalidPropertyException extends Exception {
   public InvalidPropertyException(String property) {
      super(String.format("Invalid Property '%s'\n", property));
   }
}
