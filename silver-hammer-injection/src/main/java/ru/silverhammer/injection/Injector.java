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
package ru.silverhammer.injection;

import java.util.*;
import java.util.function.Supplier;

import ru.silverhammer.reflection.*;

// TODO: consider making thread-safe
public class Injector implements IInjector {

	private final Map<Class<?>, Map<String, Supplier<?>>> bindings = new HashMap<>();

	public Injector() {
		bind(IInjector.class, this);
	}

	@Override
	public <T> void bind(Class<T> type, String name, T implementation) {
		if (type == null || implementation == null || name == null) {
			throw new IllegalArgumentException();
		}
		getNamedBindings(type).put(name, () -> implementation);
	}

	@Override
	public <T> void bind(Class<T> type, T implementation) {
		bind(type, DEFAULT_NAME, implementation);
	}

	@Override
	public <T> void bind(Class<T> type, String name, Class<? extends T> implClass) {
		if (type == null || implClass == null || name == null) {
			throw new IllegalArgumentException();
		}
		// TODO: consider singleton implementation
		getNamedBindings(type).put(name, () -> instantiate(implClass));
	}

	private Map<String, Supplier<?>> getNamedBindings(Class<?> type) {
		return bindings.computeIfAbsent(type, k -> new HashMap<>());
	}

	@Override
	public <T> void bind(Class<T> type, Class<? extends T> implClass) {
		bind(type, DEFAULT_NAME, implClass);
	}

	@Override
	public void unbind(Class<?> type) {
		bindings.remove(type);
	}

	@Override
	public void unbindAll() {
		bindings.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInstance(Class<T> type, String name) {
		if (type == null || name == null) {
			throw new IllegalArgumentException();
		}
		Set<Class<?>> boundTypes = new HashSet<>(bindings.keySet());
		for (Class<?> boundType : boundTypes) {
			if (type.isAssignableFrom(boundType) || Primitive.exists(type, boundType) || Primitive.exists(boundType, type)) {
				Map<String, Supplier<?>> namedBindings = bindings.get(boundType);
				if (namedBindings != null) {
					Supplier<?> supplier = namedBindings.get(name);
					if (supplier != null) {
						return (T) supplier.get();
					}
				}
			}
		}
		return null;
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		return getInstance(type, DEFAULT_NAME);
	}

	@Override
	public <T> T instantiate(Class<T> type) {
		IConstructorReflection<T> least = new ClassReflection<>(type).findDefaultConstructor();
		return least == null ? null : invoke(least);
	}

	private <T> T invoke(IConstructorReflection<T> ctor) {
		Object[] args = createArguments(ctor);
		return ctor.invoke(args);
	}

	@Override
	public Object invoke(Object data, IMethodReflection method) {
		if (method == null) {
			throw new IllegalArgumentException();
		}
		Object[] args = createArguments(method);
		return method.invokeOn(data, args);
	}

	private Object[] createArguments(IExecutableReflection<?> executable) {
		List<IParameterReflection> params = executable.getParameters();
		Object[] result = new Object[params.size()];
		for (int i = 0; i < params.size(); i++) {
			IParameterReflection param = params.get(i);
			InjectNamed in = param.getAnnotation(InjectNamed.class);
			result[i] = getInstance(param.getType(), in == null ? DEFAULT_NAME : in.value());
		}
		return result;
	}
}
