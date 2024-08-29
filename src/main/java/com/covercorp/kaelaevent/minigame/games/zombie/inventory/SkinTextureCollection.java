package com.covercorp.kaelaevent.minigame.games.zombie.inventory;

import java.util.List;
import java.util.Random;

public final class SkinTextureCollection {
    public static final String OLLIE_SKIN = "http://textures.minecraft.net/texture/6ca1bc57669fe00632376f2b470544235cd61e517a42f188dd6edcc85fba4837";
    public static final String MOONA_SKIN = "http://textures.minecraft.net/texture/76d78b5184d23fbb417abb61977111f527d9716363f3300776804296cb7cc47a";
    public static final String RISU_SKIN = "http://textures.minecraft.net/texture/5fb7652cabb41bd559b19a01296a669f5065b4f254e3918f0b01d8f5c42ac9f4";
    public static final String AMELIA_SKIN = "http://textures.minecraft.net/texture/8fa0d99fc056e89e3c0ac1bdef5799a9b1e4695f33a9ed69ae070ad9fdb0af50";
    public static final String INA_SKIN = "http://textures.minecraft.net/texture/1cceccb07d3193ff10d067b3f334148e74544c96b48d7fcaf5afcccaa515b7da";
    public static final String KANADE_SKIN = "http://textures.minecraft.net/texture/d12fb718f17972ed560950cccf2e0b0bcf2f5d8763e95b723ba4d4fa0f47450a";
    public static final String KIARA_SKIN = "http://textures.minecraft.net/texture/641f55557b91f14979ccc4715f0691acbc57016aabe40f00318990f5604091d1";

    private static final List<String> SKIN_LIST = List.of(
            OLLIE_SKIN,
            MOONA_SKIN,
            RISU_SKIN,
            AMELIA_SKIN,
            INA_SKIN,
            KANADE_SKIN,
            KIARA_SKIN
    );

    public static List<String> getSkinList() {
        return SKIN_LIST;
    }

    public static String getRandomSkin() {
        return SKIN_LIST.get(new Random().nextInt(getSkinList().size()));
    }
}
