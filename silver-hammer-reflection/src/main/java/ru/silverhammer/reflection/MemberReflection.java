/*
 * Copyright (c) 2018, Dmitriy Shchekotin
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package ru.silverhammer.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

abstract class MemberReflection<T extends AccessibleObject & Member> extends AnnotatedReflection<T> {

	public enum AccessType {
		Public,
		Protected,
		Private,
		Default
	}

	protected MemberReflection(T member) {
		super(member);
	}

	public AccessType getAccessType() {
		if (Modifier.isPublic(getElement().getModifiers())) {
			return AccessType.Public;
		} else if (Modifier.isProtected(getElement().getModifiers())) {
			return AccessType.Protected;
		} else if (Modifier.isPrivate(getElement().getModifiers())) {
			return AccessType.Private;
		} else {
			return AccessType.Default;
		}
	}

	@Override
	public String getName() {
		return getElement().getName();
	}

	protected <R> R forceAccess(Supplier<R> supplier) {
		T element = getElement();
		boolean accessible = element.isAccessible();
		try {
			if (!accessible) {
				element.setAccessible(true);
			}
			return supplier.get();
		} finally {
			if (!accessible) {
				element.setAccessible(false);
			}
		}
	}
}
