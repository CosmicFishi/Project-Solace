package pigeonpun.projectsolace.com;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import pigeonpun.projectsolace.scripts.projectsolaceplugin;

import static pigeonpun.projectsolace.com.ps_misc.MOD_ID;

//Modified LunaConfigHelper from Nex
public class lunaconfighelper implements LunaSettingsListener {
    public static Logger log = Global.getLogger(lunaconfighelper.class);

    public static void initLunaConfig() {
        String mid = MOD_ID;
        //List<String> tags = DEFAULT_TAGS;

        addHeader("defend", null);
        addSetting("ps_solaceDefend", "boolean", null, projectsolaceplugin.ps_solaceDefend);
        addSetting("ps_solaceDefendTimeoutPeriod", "int", null, Math.round(projectsolaceplugin.ps_solaceDefendTimeoutPeriod), 10, 200);
        addSetting("ps_defendPointEconomyMult", "int", null, Math.round(projectsolaceplugin.ps_defendPointEconomyMult), 0, 5);
        addSetting("ps_pointsRequiredForDefendFleet", "int", null, Math.round(projectsolaceplugin.ps_pointsRequiredForDefendFleet), 2000, 100000);

        addHeader("others", null);
        addSetting("ps_vagrantseerGenerateSalvage", "boolean", null, projectsolaceplugin.ps_vagrantseerGenerateSalvage);
        addSetting("ps_sodalityFleetAdjustmentActive", "boolean", null, projectsolaceplugin.ps_sodalityFleetAdjustmentActive);
        addSetting("ps_hardmodeSodalityActive", "boolean", null, projectsolaceplugin.ps_hardmodeSodalityActive);

        LunaSettings.SettingsCreator.refresh(mid);

        tryLoadLunaConfig();

        createListener();
    }

    public static void tryLoadLunaConfig() {
        try {
            loadConfigFromLuna();
        } catch (NullPointerException npe) {
            // config not created yet I guess, do nothing
        }
    }

    public static void loadConfigFromLuna() {
        projectsolaceplugin.ps_solaceDefendTimeoutPeriod = (float)loadSetting("ps_solaceDefendTimeoutPeriod", "float");
        projectsolaceplugin.ps_defendPointEconomyMult = (float)loadSetting("ps_defendPointEconomyMult", "float");
        projectsolaceplugin.ps_pointsRequiredForDefendFleet = (float)loadSetting("ps_pointsRequiredForDefendFleet", "float");
        projectsolaceplugin.ps_solaceDefend = (boolean)loadSetting("ps_solaceDefend", "boolean");
        projectsolaceplugin.ps_sodalityFleetAdjustmentActive = (boolean)loadSetting("ps_sodalityFleetAdjustmentActive", "boolean");
        projectsolaceplugin.ps_vagrantseerGenerateSalvage = (boolean)loadSetting("ps_vagrantseerGenerateSalvage", "boolean");
        projectsolaceplugin.ps_hardmodeSodalityActive = (boolean)loadSetting("ps_hardmodeSodalityActive", "boolean");
    }

    public static Object loadSetting(String var, String type) {
        String mid = MOD_ID;
        switch (type) {
            case "bool":
            case "boolean":
                return LunaSettings.getBoolean(mid, var);
            case "int":
            case "integer":
            case "key":
                return LunaSettings.getInt(mid, var);
            case "float":
                return (float)(double)LunaSettings.getDouble(mid, var);
            case "double":
                return LunaSettings.getDouble(mid, var);
            default:
                log.error(String.format("Setting %s has invalid type %s", var, type));
        }
        return null;
    }

    public static void addSetting(String var, String type, Object defaultVal) {
        addSetting(var, type, null, defaultVal, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void addSetting(String var, String type, @Nullable String tab, Object defaultVal) {
        addSetting(var, type, tab, defaultVal, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void addSetting(String var, String type, Object defaultVal, double min, double max) {
        addSetting(var, type, null, defaultVal, min, max);
    }

    public static void addSetting(String var, String type, @Nullable String tab, Object defaultVal, double min, double max) {
        String tooltip = getString("tooltip_" + var);
        if (tooltip.startsWith("Missing string:")) {
            tooltip = "";
        }
        String mid = MOD_ID;
        String name = getString("name_" + var);

        if (tab == null) tab = "";

        switch (type) {
            case "boolean":
                LunaSettings.SettingsCreator.addBoolean(mid, var, name, tooltip, (boolean)defaultVal, tab);
                break;
            case "int":
            case "integer":
                if (defaultVal instanceof Float) {
                    defaultVal = Math.round((float)defaultVal);
                }
                LunaSettings.SettingsCreator.addInt(mid, var, name, tooltip,
                        (int)defaultVal, (int)Math.round(min), (int)Math.round(max), tab);
                break;
            case "float":
                // fix float -> double conversion causing an unround number
                String floatStr = ((Float)defaultVal).toString();
                LunaSettings.SettingsCreator.addDouble(mid, var, name, tooltip,
                        Double.parseDouble(floatStr), min, max, tab);
                break;
            case "double":
                LunaSettings.SettingsCreator.addDouble(mid, var, name, tooltip,
                        (double)defaultVal, min, max, tab);
                break;
            case "key":
                LunaSettings.SettingsCreator.addKeybind(mid, var, name, tooltip, (int)defaultVal, tab);
            default:
                log.error(String.format("Setting %s has invalid type %s", var, type));
        }
    }

    public static void addHeader(String id, String tab) {
        addHeader(id, getString("header_" + id), tab);
    }

    public static void addHeader(String id, String title, String tab) {
        if (tab == null) tab = "";
        LunaSettings.SettingsCreator.addHeader(MOD_ID, id, title, tab);
    }

    public static lunaconfighelper createListener() {
        lunaconfighelper helper = new lunaconfighelper();
        LunaSettings.INSTANCE.addListener(helper);
        return helper;
    }

    @Override
    public void settingsChanged(String modId) {
        if (MOD_ID.equals(modId)) {
            loadConfigFromLuna();
        }
    }

    public static String getString(String id) {
        return Global.getSettings().getString("ps_lunasettings", id);
    }
}
