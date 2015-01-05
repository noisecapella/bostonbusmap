package boston.Bus.Map.ui;

import boston.Bus.Map.R;

public class BusDrawables {

    private static final int[] idLookup = new int[] {
         R.drawable.bus_0,
        R.drawable.bus_8,
        R.drawable.bus_16,
        R.drawable.bus_24,
        R.drawable.bus_32,
        R.drawable.bus_40,
        R.drawable.bus_48,
        R.drawable.bus_56,
        R.drawable.bus_64,
        R.drawable.bus_72,
        R.drawable.bus_80,
        R.drawable.bus_88,
        R.drawable.bus_96,
        R.drawable.bus_104,
        R.drawable.bus_112,
        R.drawable.bus_120,
        R.drawable.bus_128,
        R.drawable.bus_136,
        R.drawable.bus_144,
        R.drawable.bus_152,
        R.drawable.bus_160,
        R.drawable.bus_168,
        R.drawable.bus_176,
        R.drawable.bus_184,
        R.drawable.bus_192,
        R.drawable.bus_200,
        R.drawable.bus_208,
        R.drawable.bus_216,
        R.drawable.bus_224,
        R.drawable.bus_232,
        R.drawable.bus_240,
        R.drawable.bus_248,
        R.drawable.bus_256,
        R.drawable.bus_264,
        R.drawable.bus_272,
        R.drawable.bus_280,
        R.drawable.bus_288,
        R.drawable.bus_296,
        R.drawable.bus_304,
        R.drawable.bus_312,
        R.drawable.bus_320,
        R.drawable.bus_328,
        R.drawable.bus_336,
        R.drawable.bus_344,
        R.drawable.bus_352
        };

    private static final int[] idSelectedLookup = new int[] {
         R.drawable.bus_selected_0,
        R.drawable.bus_selected_8,
        R.drawable.bus_selected_16,
        R.drawable.bus_selected_24,
        R.drawable.bus_selected_32,
        R.drawable.bus_selected_40,
        R.drawable.bus_selected_48,
        R.drawable.bus_selected_56,
        R.drawable.bus_selected_64,
        R.drawable.bus_selected_72,
        R.drawable.bus_selected_80,
        R.drawable.bus_selected_88,
        R.drawable.bus_selected_96,
        R.drawable.bus_selected_104,
        R.drawable.bus_selected_112,
        R.drawable.bus_selected_120,
        R.drawable.bus_selected_128,
        R.drawable.bus_selected_136,
        R.drawable.bus_selected_144,
        R.drawable.bus_selected_152,
        R.drawable.bus_selected_160,
        R.drawable.bus_selected_168,
        R.drawable.bus_selected_176,
        R.drawable.bus_selected_184,
        R.drawable.bus_selected_192,
        R.drawable.bus_selected_200,
        R.drawable.bus_selected_208,
        R.drawable.bus_selected_216,
        R.drawable.bus_selected_224,
        R.drawable.bus_selected_232,
        R.drawable.bus_selected_240,
        R.drawable.bus_selected_248,
        R.drawable.bus_selected_256,
        R.drawable.bus_selected_264,
        R.drawable.bus_selected_272,
        R.drawable.bus_selected_280,
        R.drawable.bus_selected_288,
        R.drawable.bus_selected_296,
        R.drawable.bus_selected_304,
        R.drawable.bus_selected_312,
        R.drawable.bus_selected_320,
        R.drawable.bus_selected_328,
        R.drawable.bus_selected_336,
        R.drawable.bus_selected_344,
        R.drawable.bus_selected_352
        };
    

    public static int getIdFromAngle(int angle, boolean isSelected) {
        if (isSelected) {
            if (angle < 0 || angle >= 360) {
                return R.drawable.bus_selected_0;
            }
            return idSelectedLookup[angle/8];
        }
        else {
            if (angle < 0 || angle >= 360) {
                return R.drawable.bus_0;
            }
            return idLookup[angle/8];
        }
    }
}
