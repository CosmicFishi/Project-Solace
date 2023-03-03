package pigeonpun.projectsolace.com;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ps_misc {
    public static final Color PROJECT_SOLACE = new Color(230,40,240,255);
    public static final Color PROJECT_SOLACE_JITTER = new Color(230,40,240,25);
    public static final Color PROJECT_SOLACE_JITTER_UNDER = new Color(230,40,240,45);
    public static final Color PROJECT_SOLACE_LIGHT = new Color(230,200,240,255);

    public static final Color PROJECT_SOLACE_NEBULA_COLOR_1 = new Color(255,0,255,255);
    public static final Color PROJECT_SOLACE_NEBULA_COLOR_2 = new Color(100,0,255,255);
    public static final Color PROJECT_SOLACE_NEBULA_COLOR_3 = new Color(255,100,100,255);

    public static final List<Color> PROJECT_SOLACE_NEBULA_COLORs = new ArrayList<>();
    static {
        PROJECT_SOLACE_NEBULA_COLORs.add(PROJECT_SOLACE_NEBULA_COLOR_1);
        PROJECT_SOLACE_NEBULA_COLORs.add(PROJECT_SOLACE_NEBULA_COLOR_2);
        PROJECT_SOLACE_NEBULA_COLORs.add(PROJECT_SOLACE_NEBULA_COLOR_3);
    }
}
