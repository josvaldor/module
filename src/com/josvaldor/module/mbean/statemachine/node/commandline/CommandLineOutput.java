package com.josvaldor.module.mbean.statemachine.node.commandline;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.josvaldor.module.Module;
import com.josvaldor.module.mbean.statemachine.node.Container;
import com.josvaldor.module.mbean.statemachine.node.Output;

public class CommandLineOutput
  extends Output
{
  private PrintWriter printWriter = null;
  
  public CommandLineOutput(int id, Module module, OutputStream outputStream)
  {
    super(id, module, outputStream);
    this.printWriter = new PrintWriter(this.outputStream);
  }
  
  public void initialize()
  {
    super.initialize();
    setState(2);
  }
  
  public void destroy()
  {
    super.destroy();
    if (this.printWriter != null) {
      this.printWriter.close();
    }
  }
  
  public void output(Object object)
  {
    super.output(object);
    String string = (String)object;
    this.printWriter.write(string);
    this.printWriter.flush();
    outputObjectListAdd(new Container(this.id.intValue(), this.id.intValue(), 2, 0.0D, null, null));
  }
}
