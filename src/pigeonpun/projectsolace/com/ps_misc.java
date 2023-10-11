package pigeonpun.projectsolace.com;

import org.magiclib.util.MagicAnim;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ps_misc {
    public static final Color PROJECT_SOLACE = new Color(230,40,240,255);
    public static final Color PROJECT_SOLACE_JITTER = new Color(248,40,240,25);
    public static final Color PROJECT_SOLACE_JITTER_UNDER = new Color(145,255,214,245);
    public static final Color PROJECT_SOLACE_LIGHT = new Color(230,200,240,255);

    public static final Color PROJECT_SOLACE_NEBULA_COLOR_1 = new Color(255,0,255,255);
    public static final Color PROJECT_SOLACE_NEBULA_COLOR_2 = new Color(100,0,255,255);
    public static final Color PROJECT_SOLACE_NEBULA_COLOR_3 = new Color(255,100,100,255);
    public static final Color PROJECT_SOLACE_UP_STANDSTILL = new Color(241, 255, 48, 150);
    public static final Color PROJECT_SOLACE_UP_ACTIVATION = new Color(173, 0, 255  , 50);
    public static final Color ENMITY_SHIELD_PARTICLE = new Color(110,165,152,255);
    public static final Color ENMITY_SHIELD_EMP_CORE = new Color(250, 250, 0, 255);
    public static final Color ENMITY_SHIELD_EMP_FRINGE = new Color(255, 168, 35, 255);
    public static final Color ENMITY_JITTER = new Color(0, 181, 151, 250);
    public static final Color ENMITY_MAIN = new Color(182,201,199,255);
    public static final Color ENMITY_MAIN_AFTER_IMG = new Color(182,201,199,155);
    public static final List<Color> PROJECT_SOLACE_NEBULA_COLORs = new ArrayList<>();
    static {
        PROJECT_SOLACE_NEBULA_COLORs.add(PROJECT_SOLACE_NEBULA_COLOR_1);
        PROJECT_SOLACE_NEBULA_COLORs.add(PROJECT_SOLACE_NEBULA_COLOR_2);
        PROJECT_SOLACE_NEBULA_COLORs.add(PROJECT_SOLACE_NEBULA_COLOR_3);
    }
    public static final String SOLACE_CORE_DECORATIVE_MOUNT_ID = "ps_core_crystal";
    public static final List<String> ENMITY_SPECIAL_WEAPONS_LIST = new ArrayList<>();
    static {
        ENMITY_SPECIAL_WEAPONS_LIST.add("ps_acris");
        ENMITY_SPECIAL_WEAPONS_LIST.add("ps_anaxyr");
        ENMITY_SPECIAL_WEAPONS_LIST.add("ps_daemeli");
        ENMITY_SPECIAL_WEAPONS_LIST.add("ps_punctatus");
    }

    public static final List<String> SOLACE_SHIPS_LINEUP = new ArrayList<>();
    static {
        //FRIGATE
        SOLACE_SHIPS_LINEUP.add("ps_salawesi");
        SOLACE_SHIPS_LINEUP.add("ps_taliabu");
        SOLACE_SHIPS_LINEUP.add("ps_tanimbar");
        //DESTROYER
        SOLACE_SHIPS_LINEUP.add("ps_morepork");
        SOLACE_SHIPS_LINEUP.add("ps_ashy");
        SOLACE_SHIPS_LINEUP.add("ps_papuan");
        //CRUISER
        SOLACE_SHIPS_LINEUP.add("ps_barking");
        SOLACE_SHIPS_LINEUP.add("ps_rufous");
        SOLACE_SHIPS_LINEUP.add("ps_javanica");
        SOLACE_SHIPS_LINEUP.add("ps_aurantia");
        //CAPTIAL
        SOLACE_SHIPS_LINEUP.add("ps_camiguin");
        SOLACE_SHIPS_LINEUP.add("ps_nadhmor");
        SOLACE_SHIPS_LINEUP.add("ps_albertine");
    }
    public static final List<String> SOLACE_WEAPONS_LINEUP = new ArrayList<>();
    static {
        SOLACE_WEAPONS_LINEUP.add("ps_tragopo");
        SOLACE_WEAPONS_LINEUP.add("ps_carthami");
        SOLACE_WEAPONS_LINEUP.add("ps_porri");
    }
    public static final List<String> ENMITY_SHIPS_LINEUP = new ArrayList<>();
    static {
        //FRIGATE
        ENMITY_SHIPS_LINEUP.add("ps_laevis");
        //DESTROYER
        ENMITY_SHIPS_LINEUP.add("ps_pricei");
        //CRUISER
        ENMITY_SHIPS_LINEUP.add("ps_merkeri");
        //CAPTIAL
        ENMITY_SHIPS_LINEUP.add("ps_ravus");
    }
    public static final List<String> ENMITY_WEAPONS_LINEUP = new ArrayList<>();
    static {
        //ENMITY_WEAPONS_LINEUP.add("");
    }
    public static final List<String> ENMITY_FIGHTER_LINEUP = new ArrayList<>();
    static {
        ENMITY_FIGHTER_LINEUP.add("ps_riveti");
        ENMITY_FIGHTER_LINEUP.add("ps_dumerili");
        ENMITY_FIGHTER_LINEUP.add("ps_vudii");
        ENMITY_FIGHTER_LINEUP.add("ps_viridis");
        ENMITY_FIGHTER_LINEUP.add("ps_tigris");
    }
}
