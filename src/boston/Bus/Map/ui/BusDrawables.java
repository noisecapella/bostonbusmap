package boston.Bus.Map.ui;

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
         R.drawable.bus_statelist_0,
        R.drawable.bus_statelist_8,
        R.drawable.bus_statelist_16,
        R.drawable.bus_statelist_24,
        R.drawable.bus_statelist_32,
        R.drawable.bus_statelist_40,
        R.drawable.bus_statelist_48,
        R.drawable.bus_statelist_56,
        R.drawable.bus_statelist_64,
        R.drawable.bus_statelist_72,
        R.drawable.bus_statelist_80,
        R.drawable.bus_statelist_88,
        R.drawable.bus_statelist_96,
        R.drawable.bus_statelist_104,
        R.drawable.bus_statelist_112,
        R.drawable.bus_statelist_120,
        R.drawable.bus_statelist_128,
        R.drawable.bus_statelist_136,
        R.drawable.bus_statelist_144,
        R.drawable.bus_statelist_152,
        R.drawable.bus_statelist_160,
        R.drawable.bus_statelist_168,
        R.drawable.bus_statelist_176,
        R.drawable.bus_statelist_184,
        R.drawable.bus_statelist_192,
        R.drawable.bus_statelist_200,
        R.drawable.bus_statelist_208,
        R.drawable.bus_statelist_216,
        R.drawable.bus_statelist_224,
        R.drawable.bus_statelist_232,
        R.drawable.bus_statelist_240,
        R.drawable.bus_statelist_248,
        R.drawable.bus_statelist_256,
        R.drawable.bus_statelist_264,
        R.drawable.bus_statelist_272,
        R.drawable.bus_statelist_280,
        R.drawable.bus_statelist_288,
        R.drawable.bus_statelist_296,
        R.drawable.bus_statelist_304,
        R.drawable.bus_statelist_312,
        R.drawable.bus_statelist_320,
        R.drawable.bus_statelist_328,
        R.drawable.bus_statelist_336,
        R.drawable.bus_statelist_344,
        R.drawable.bus_statelist_352
        };
    

    public static int getIdFromAngle(int angle, boolean isSelected) {
        angle += (360 / idSelectedLookup.length) / 2;
        angle = angle % 360;
        if (angle < 0 || angle >= 360) {
            return R.drawable.bus_statelist_0;
        }
        return idSelectedLookup[angle/8];
    }
}
