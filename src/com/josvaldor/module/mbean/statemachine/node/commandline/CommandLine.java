package com.josvaldor.module.mbean.statemachine.node.commandline;

import com.josvaldor.module.Module;
import com.josvaldor.module.mbean.statemachine.node.Container;
import com.josvaldor.module.mbean.statemachine.node.Input;
import com.josvaldor.module.mbean.statemachine.node.Node;
import com.josvaldor.module.mbean.statemachine.node.Output;
import com.josvaldor.utility.Utility;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class CommandLine extends Node {
	private List<String> alias;

	public static void main(String[] args) {
		CommandLine commandLine = new CommandLine(Integer.valueOf(0), new Module());
		CountDownLatch countDownLatch;
		commandLine.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		commandLine.start();
	}

	private final int WAIT_FOR_OUTPUT_INPUT = 2323;
	private double waitForOutputInputDelay;

	public CommandLine() {
	}

	public CommandLine(Integer id, Module module) {
		super(id, module);
	}

	public void initialize() {
		super.initialize();
//		this.alias = Utility.stringToStringList(getProperty("@aliasList"), ",");
		this.input = new CommandLineInput(this.id.intValue(), this, System.in);
		this.output = new CommandLineOutput(this.id.intValue(), this, System.out);
		moduleMapStart(this.moduleMap);
		this.waitForOutputInputDelay = Utility.stringToDouble(getProperty("@waitForOutputInput", "5"));
		setDelayExpiration(newDelayExpiration(this.waitForInputDelay));
		setState(2);
	}

	public void machine(int state, Object object) {
		switch (state) {
		case WAIT_FOR_OUTPUT_INPUT:
			waitForOutputInput(object);
			break;
		default:
			super.machine(state, object);
		}
	}

	public void waitForInput(Object object) {
		if ((this.input.getRun()) && (this.output.getRun())) {
			if ((object instanceof Container)) {
				Container container = (Container) object;
				Integer sourceID = Integer.valueOf(container.getSourceID());
				if (this.idSet.contains(sourceID)) {
					switch (container.getType()) {
					case 2:
						if (logger.isDebugEnabled()) {
							logger.debug("waitForInput(" + object + ") Container.INPUT");
						}
						input(container);
						setDelayExpiration(newDelayExpiration(this.waitForOutputInputDelay));
						setState(2323);
					}
				}
			}
		} else {
			setState(0);
		}
	}

	public void output(Object object) {
		if (logger.isDebugEnabled()) {
			logger.debug("output(" + object + ")");
		}
		if ((object instanceof String)) {
			String string = (String) object;
			if (StringUtils.isNotBlank(string)) {
				outputAlias(string);
				this.output.inputObjectListAdd(
						new Container(this.id.intValue(), this.id.intValue(), 1, 0.0D, string, null));
			} else {
				this.input.inputObjectListAdd(
						new Container(this.id.intValue(), this.id.intValue(), 14, 0.0D, string, null));
			}
		}
	}

	public void input(Object object) {
		if (logger.isDebugEnabled()) {
			logger.debug("input(" + object + ")");
		}
		Iterator<Integer> iterator = this.idSet.iterator();
		if ((object instanceof String)) {
			String string = (String) object;
			if (StringUtils.isNotBlank(string)) {
				string = inputAlias(string);
				while (iterator.hasNext()) {
					int id;
					if ((id = ((Integer) iterator.next()).intValue()) != this.id.intValue()) {
						outputObjectListAdd(
								new Container(id, this.id.intValue(), 1, 0.0D, string, this.inputObjectList));
					}
				}
			} else {
				this.input.inputObjectListAdd(
						new Container(this.id.intValue(), this.id.intValue(), 14, 0.0D, string, null));
			}
		}
	}

	private void waitForOutputInput(Object object) {
		Container container;
		if ((this.input.getRun()) && (this.output.getRun())) {
			if ((object instanceof Container)) {
				container = (Container) object;
				Integer sourceID = Integer.valueOf(container.getSourceID());
				if (!this.idSet.contains(sourceID)) {
				}
			}
		} else {
			if ((object instanceof Container)) {
				container = (Container) object;
				switch (container.getType()) {
				case 1:
					if (logger.isDebugEnabled()) {
						logger.debug("waitForInput(" + object + ") Container.OUTPUT");
					}
					output(container);
					break;
				case 2:
					if (logger.isDebugEnabled()) {
						logger.debug("waitForInput(" + object + ") Container.CONTENT");
					}
					this.input.inputObjectListAdd(container);
					setState(2);
				default:
					break;
//					setState(0);
				}
			}
		}
		if (delayExpired()) {
			this.input.inputObjectListAdd(new Container(this.id.intValue(), this.id.intValue(), 14, 0.0D, null, null));
			this.setDelayExpiration(this.newDelayExpiration(1));
		}
	}

	private String inputAlias(String string) {
		if (logger.isDebugEnabled()) {
			logger.debug("inputAlias(" + string + ")");
		}
		String[] stringArray = Utility.stringToStringArray(string, " ");
		for (int i = 0; i < stringArray.length; i++) {
			for (int j = 0; j < this.alias.size(); j++) {
				if ((stringArray[i].matches((String) this.alias.get(j) + "=\\S+"))
						|| (stringArray[i].matches((String) this.alias.get(j) + "$"))) {
					stringArray[i] = stringArray[i].replaceFirst("^" + (String) this.alias.get(j),
							getProperty((String) this.alias.get(j)));
				}
			}
		}
		string = Utility.stringArrayToString(stringArray);
		return string;
	}

	private String outputAlias(String string) {
		if (logger.isDebugEnabled()) {
			logger.debug("outputAlias(" + string + ")");
		}
		for (String keyAlias : this.alias) {
			string = string.replaceAll(" " + getProperty(keyAlias) + "=", " " + keyAlias + "=");
			string = string.replaceAll(" " + getProperty(keyAlias) + " ", " " + keyAlias + " ");
		}
		return string;
	}
}
