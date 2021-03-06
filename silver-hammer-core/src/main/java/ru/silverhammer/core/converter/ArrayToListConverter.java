/*
 * Copyright (c) 2017, Dmitriy Shchekotin
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
package ru.silverhammer.core.converter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import ru.silverhammer.core.converter.annotation.ArrayToList;

public class ArrayToListConverter implements IConverter<Object, Collection<?>, ArrayToList> {

	@Override
	public Collection<?> convertForward(Object source, ArrayToList annotation) {
		if (source != null && source.getClass().isArray()) {
			Collection<Object> result = new ArrayList<>();
			int length = Array.getLength(source);
			for (int i = 0; i < length; i++) {
		        result.add(Array.get(source, i));
			}
			return result;
		}
		return null;
	}

	@Override
	public Object convertBackward(Collection<?> destination, ArrayToList annotation) {
		if (destination != null) {
			Object array = Array.newInstance(annotation.value(), destination.size());
			int i = 0;
		    for (Object o : destination) {
		    	Array.set(array, i++, o);
		    }
		    return array;
		}
		return null;
	}
}
