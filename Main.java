// ##kazet (c) 2022-2026 All rights reserved##
package com.autoattack;

import com.autoattack.config.AutoAttackConfig;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {

    @Override
    public void onInitialize() {
        MidnightConfig.init("autoattack", AutoAttackConfig.class);
    }
}
// ##kazet (c) 2022-2026 All rights reserved##
