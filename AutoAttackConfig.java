// ##kazet (c) 2022-2026 All rights reserved##
package autoattack;

import eu.midnightdust.lib.config.MidnightConfig.Comment;
import eu.midnightdust.lib.config.MidnightConfig.Entry;

public class AutoAttackConfig extends AutoAttack {

    public enum TargetMode {
        HOSTILE,   // only hostile mobs
        EVERYONE,  // all living (hostile + passive + players)
        ALL        // everything attackable
    }

    @Comment
    public static Comment text1;

    @Entry
    public static boolean checkUpdate = true;

    @Entry
    public static TargetMode targetMode = TargetMode.HOSTILE;

    @Entry
    public static boolean preventsHittingBlocks = false;

    @Entry
    public static boolean preventsHittingBlocksSwords = true;

    @Entry
    public static boolean cleanCut = true;

    @Entry
    public static boolean autoBow = true;

    @Entry
    public static boolean autoCrossBow = true;

    @Entry
    public static boolean autoTrident = true;

    @Entry
    public static boolean afkAttack = false;
}
// ##kazet (c) 2022-2026 All rights reserved##
