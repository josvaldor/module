package com.josvaldor.module.mbean.statemachine.node.commandline;

import com.josvaldor.module.Module;
import com.josvaldor.module.mbean.statemachine.node.Container;
import com.josvaldor.module.mbean.statemachine.node.Input;
import com.josvaldor.utility.Utility;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.SimpleCompletor;
import org.apache.log4j.Logger;

public class CommandLineInput
  extends Input
{
  private static final int WAIT_FOR_UNBLOCK_INPUT = 5;
  private ConsoleReader consoleReader;
  private String prompt = Utility.getKeyClassName(getClass());
  
  public CommandLineInput(int id, Module module, InputStream inputStream)
  {
    super(id, module, inputStream);
  }
  
  public void initialize()
  {
    super.initialize();
    this.stateMap.put(Integer.valueOf(5), "WAIT_FOR_UNBLOCK_INPUT");
    this.prompt = getProperty("prompt", Utility.getKeyClassName(getClass()));
    try
    {
      this.consoleReader = new ConsoleReader();
      this.consoleReader.setBellEnabled(true);
      if (logger.isDebugEnabled()) {
        this.consoleReader.setDebug(new PrintWriter(new FileWriter("writer.debug", true)));
      }
      List<Completor> completorList = new LinkedList();
      completorList.add(new SimpleCompletor(new String[0]));
      this.consoleReader.addCompletor(new ArgumentCompletor(completorList));
      setState(2);
    }
    catch (IOException e)
    {
      setState(0);
    }
  }
  
  public void destroy()
  {
    super.destroy();
  }
  
  public void machine(int state, Object object)
  {
    switch (state)
    {
    case 5: 
      waitForUnblockInput(object);
      break;
    default: 
      super.machine(state, object);
    }
  }
  
  public void input(Object object)
  {
    try
    {
      String string;
      if ((string = this.consoleReader.readLine(this.prompt + ">")) != null)
      {
        outputObjectListAdd(new Container(this.id.intValue(), this.id.intValue(), 2, 0.0D, string, null));
        setState(5);
      }
    }
    catch (IOException e)
    {
      logger.warn("input() IOException");
    }
  }
  
  private void waitForUnblockInput(Object object)
  {
    if ((object instanceof Container))
    {
      Container container = (Container)object;
      switch (container.getType())
      {
      case 14: 
        try
        {
          this.consoleReader.beep();
        }
        catch (IOException e)
        {
          logger.error("waitForUnblockInput(" + object + ") IOException");
        }
        setState(2);
        break;
      case 2: 
        try
        {
          this.consoleReader.printNewline();
          this.consoleReader.printNewline();
        }
        catch (IOException e)
        {
          logger.error("waitForUnblockInput(" + object + ") IOException");
        }
        setState(2);
      }
    }
  }
}
