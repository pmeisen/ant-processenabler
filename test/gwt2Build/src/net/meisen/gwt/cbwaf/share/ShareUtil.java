package net.meisen.gwt.cbwaf.share;

import net.meisen.gwt.cbwafshare.share.SharedStuff;

public class ShareUtil {

  public static String myName = "ShareUtil";
  
  public static String getSharedImport() {
    return SharedStuff.getMyName();
  }
  
  public static String getName() {
    return myName;
  }
}
