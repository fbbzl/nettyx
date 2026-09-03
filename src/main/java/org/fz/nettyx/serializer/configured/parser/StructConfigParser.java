package org.fz.nettyx.serializer.configured.parser;

import org.fz.nettyx.serializer.configured.ConfigStruct;

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