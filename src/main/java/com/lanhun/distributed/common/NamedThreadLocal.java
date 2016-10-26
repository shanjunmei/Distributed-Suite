
package com.lanhun.distributed.common;

public class NamedThreadLocal<T> extends ThreadLocal<T> {

	private final String name;

	public NamedThreadLocal(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
