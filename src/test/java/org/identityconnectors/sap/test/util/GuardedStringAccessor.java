/*
 * ***COPYRIGHT STARTS HERE***
 *  Copyright (C) 2009 Sun Microsystems, Inc. All rights reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 *
 *  U.S. Government Rights - Commercial software. Government users are subject
 *  to the Sun Microsystems, Inc. standard license agreement and applicable
 *  provisions of the FAR and its supplements.
 *
 *  Use is subject to license terms.
 *
 *  This distribution may include materials developed by third parties.Sun,
 *  Sun Microsystems, the Sun logo, Java, Solaris and Sun Identity Manager
 *  are trademarks or registered trademarks of Sun Microsystems, Inc.
 *  or its subsidiaries in the U.S. and other countries.
 *
 *  UNIX is a registered trademark in the U.S. and other countries, exclusively
 *  licensed through X/Open Company, Ltd.
 *                   ***COPYRIGHT ENDS HERE***
 */
package org.identityconnectors.sap.test.util;

import org.identityconnectors.common.security.GuardedString;

import java.util.Arrays;

/**
 * 
 * Helper imlementation of
 * {@link org.identityconnectors.common.security.GuardedString.Accessor}
 * 
 * @author George Hetrick
 */
public class GuardedStringAccessor implements GuardedString.Accessor {

	private char[] _array;

	/**
	 * comment for Check in test
	 * {@inheritDoc}
	 */
	public void access(char[] clearChars) {
		if (clearChars != null) {
			_array = new char[clearChars.length];
			System.arraycopy(clearChars, 0, _array, 0, _array.length);
		}
	}

	/**
	 * Gets array of the decoded string
	 * 
	 * @return char array
	 */
	public char[] getArray() {
		return _array;
	}

	/**
	 * Clears the decoded string stored inside the class
	 */
	public void clear() {
		if (_array != null) {
			Arrays.fill(_array, 0, _array.length, ' ');
		}
	}
}
