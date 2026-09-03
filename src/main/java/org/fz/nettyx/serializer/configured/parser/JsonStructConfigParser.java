package org.fz.nettyx.serializer.configured.parser;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.serializer.configured.ConfigStruct;

import java.io.InputStream;
import java.util.Map;

/**
 * Parses JSON configured-struct resources.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-02
 */
public final class JsonStructConfigParser extends StructuredStructConfigParser
{

    private static final ObjectMapper MAPPER = JsonMapper.builder()
                                                         .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                                                         .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                                                         .build();

    @Override
    public Map<String, ConfigStruct> parse(String location, InputStream input)
    {
        try {
            return parseTree(location, MAPPER.readTree(input));
        }
        catch (StructDefinitionException definitionError) {
            throw definitionError;
        }
        catch (Exception parseError) {
            throw new StructDefinitionException("failed to parse struct config, location: [" + location + "]", parseError);
        }
    }
}