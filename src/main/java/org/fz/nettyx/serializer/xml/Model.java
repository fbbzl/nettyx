package org.fz.nettyx.serializer.xml;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/29 18:03
 */
public class Model {

    private String namespace;










    static final class XmlScanner {

        static final String DEFAULT_PATH = "xml/";

        static {
            ClassPathResource classPathResource = new ClassPathResource(DEFAULT_PATH);

            ResourceUtil.getResource(classPathResource.getPath());
        }

        public void scanHandler() {


        }



    }

}
