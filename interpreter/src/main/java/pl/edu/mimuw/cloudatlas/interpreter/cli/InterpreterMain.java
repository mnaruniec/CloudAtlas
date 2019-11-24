/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.interpreter.cli;

import java.util.Scanner;

import pl.edu.mimuw.cloudatlas.model.ZMI;

import static pl.edu.mimuw.cloudatlas.interpreter.cli.InterpreterUtils.executeQueries;

public class InterpreterMain {
	private static ZMI root;

	public static void main(String[] args) throws Exception {
		root = InterpreterUtils.createAssignmentTestHierarchy();
		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter("\\n");
		while (scanner.hasNext()) {
			String[] next = scanner.next().split(": ", 2);
			if (next.length != 2) {
				System.out.println("Input lines should be '&<query_name>: <query>");
				System.exit(1);
			}
			executeQueries(root, next[1], false, next[0]);
		}
		scanner.close();
		InterpreterUtils.printHierarchy(root);
	}
}
