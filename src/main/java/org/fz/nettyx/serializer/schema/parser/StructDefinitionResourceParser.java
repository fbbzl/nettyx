package org.fz.nettyx.serializer.schema.parser;

import org.fz.nettyx.serializer.schema.ConfigStruct;

import java.io.InputStream;
import java.util.Map;

/**
 * Parses one configured-struct definition resource.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-02
 */
public interface StructConfigParser
{

    Map<String, ConfigStruct> parse(String location, InputStream input);
}