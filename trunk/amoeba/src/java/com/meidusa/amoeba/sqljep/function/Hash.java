package com.meidusa.amoeba.sqljep.function;

import com.meidusa.amoeba.sqljep.function.PostfixCommand;
import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;

/**
 * 
 * @author struct
 *
 */
public class Hash extends PostfixCommand {
	final public int getNumberOfParameters() {
		return 1;
	}
	
	public void evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
		node.childrenAccept(runtime.ev, null);
		Comparable<?>  param = runtime.stack.pop();
		runtime.stack.push(hash(param));		//push the result on the inStack
	}

	public static Integer hash(Comparable<?>  param) throws ParseException {
		if (param == null) {
			return null;
		}
		return param.hashCode();
	}
}

