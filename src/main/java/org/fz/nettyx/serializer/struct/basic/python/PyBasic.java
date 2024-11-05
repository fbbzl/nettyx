package org.fz.nettyx.serializer.struct.basic.python;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.Basic;

/**
 * The type Py basic.
 *
 * @param <V> the type parameter
 */
public abstract class PyBasic<V extends Comparable<V>> extends Basic<V> {

	/**
	 * Instantiates a new Py basic.
	 *
	 * @param value the value
	 * @param size  the size
	 */
	protected PyBasic(V value, int size) {
		super(value, size);
	}

	protected PyBasic(ByteBuf buf, int size) {
		super(buf, size);
	}

	@Override
	public String toString() {
		return this.getValue().toString();
	}
}
