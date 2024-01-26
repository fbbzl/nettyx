package org.fz.nettyx.serializer.xml;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/25 16:54
 */
public class XmlUtil {




    static final class XmlScanner {

        static final String DEFAULT_PATH = "xml/";

        static {
            ClassPathResource classPathResource = new ClassPathResource(DEFAULT_PATH);

            ResourceUtil.getResource(classPathResource.getPath());
        }

        public static void main(String[] args) {
            System.err.println(1);
        }



    }

}
