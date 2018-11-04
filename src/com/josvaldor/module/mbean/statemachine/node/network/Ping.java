/*
Copyright 2018 Josvaldor

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.josvaldor.module.mbean.statemachine.node.network;

import com.josvaldor.module.Module;

public class Ping
  extends Network
{
  private String command;
  
  public Ping(int id, Module module)
  {
    super(Integer.valueOf(id), module);
  }
  
  public void initialize()
  {
    this.node = false;
    super.initialize();
    this.command = null;
    switch (this.operatingSystem)
    {
    case 1: 
      this.command = ("ping -c " + this.tryMax + " " + this.hostAddress);
      break;
    case 2: 
      this.command = ("ping -n " + this.tryMax + " " + this.hostAddress);
    }
  }
  
  public void waitForInput(Object object)
  {
    if (delayExpired())
    {
      if (execute(this.command) > 0) {
        setState(0);
      }
      setDelayExpiration(newDelayExpiration(this.waitForInputDelay));
    }
  }
}
