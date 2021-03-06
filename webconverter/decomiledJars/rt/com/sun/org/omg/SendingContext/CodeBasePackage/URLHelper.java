package com.sun.org.omg.SendingContext.CodeBasePackage;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

public final class URLHelper
{
  private static String _id = "IDL:omg.org/SendingContext/CodeBase/URL:1.0";
  private static TypeCode __typeCode = null;
  
  public URLHelper() {}
  
  public static void insert(Any paramAny, String paramString)
  {
    OutputStream localOutputStream = paramAny.create_output_stream();
    paramAny.type(type());
    write(localOutputStream, paramString);
    paramAny.read_value(localOutputStream.create_input_stream(), type());
  }
  
  public static String extract(Any paramAny)
  {
    return read(paramAny.create_input_stream());
  }
  
  public static synchronized TypeCode type()
  {
    if (__typeCode == null)
    {
      __typeCode = ORB.init().create_string_tc(0);
      __typeCode = ORB.init().create_alias_tc(id(), "URL", __typeCode);
    }
    return __typeCode;
  }
  
  public static String id()
  {
    return _id;
  }
  
  public static String read(InputStream paramInputStream)
  {
    String str = null;
    str = paramInputStream.read_string();
    return str;
  }
  
  public static void write(OutputStream paramOutputStream, String paramString)
  {
    paramOutputStream.write_string(paramString);
  }
}
