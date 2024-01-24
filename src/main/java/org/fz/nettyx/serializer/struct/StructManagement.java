package org.fz.nettyx.serializer.struct;

import lombok.Getter;

import static cn.hutool.core.util.ArrayUtil.distinct;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/24 15:54
 */

@Getter
public class StructManagement {

    private final String[] scanStructPackages;

    public StructManagement(String... scanStructPackages) {
        this.scanStructPackages = scanStructPackages;

        for (String scanStructPackage : distinct(this.scanStructPackages )) {
            StructCache.doScan(scanStructPackage);
        }
    }
}
