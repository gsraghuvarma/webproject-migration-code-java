package javax.tools;

import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.Callable;
import javax.annotation.processing.Processor;

public abstract interface JavaCompiler
  extends Tool, OptionChecker
{
  public abstract CompilationTask getTask(Writer paramWriter, JavaFileManager paramJavaFileManager, DiagnosticListener<? super JavaFileObject> paramDiagnosticListener, Iterable<String> paramIterable1, Iterable<String> paramIterable2, Iterable<? extends JavaFileObject> paramIterable);
  
  public abstract StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> paramDiagnosticListener, Locale paramLocale, Charset paramCharset);
  
  public static abstract interface CompilationTask
    extends Callable<Boolean>
  {
    public abstract void setProcessors(Iterable<? extends Processor> paramIterable);
    
    public abstract void setLocale(Locale paramLocale);
    
    public abstract Boolean call();
  }
}
