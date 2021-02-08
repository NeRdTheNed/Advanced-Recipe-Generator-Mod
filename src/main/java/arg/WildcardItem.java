package arg;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

public class WildcardItem extends Item {

    private static final String baseIconName = "arg:WildcardIcon";
    private IIcon[] iconArray;

    public WildcardItem() {
        setUnlocalizedName("WildcardItem");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        if (damage == 0) {
            return itemIcon;
        } else {
            return iconArray[MathHelper.clamp_int(damage, 1, 9)];
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iiconRegister) {
        itemIcon = iiconRegister.registerIcon(baseIconName + "Unique");
        iconArray = new IIcon[10];
        iconArray[0] = itemIcon;

        for (int i = 1; i <= 9; i++) {
            iconArray[i] = iiconRegister.registerIcon(baseIconName + i);
        }
    }

}
