package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuint;

/**
 * The type Cuint.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class Cppuint extends Cuint {

	/**
	 * The constant MIN_VALUE.
	 */
	public static final Cppuint MIN_VALUE = new Cppuint(0L);

	/**
	 * The constant MAX_VALUE.
	 */
	public static final Cppuint MAX_VALUE = new Cppuint(Integer.MAX_VALUE * 2L);

	public Cppuint(Long value) {
		super(value);
	}

	public Cppuint(ByteBuf buf) {
		super(buf);
	}
}
