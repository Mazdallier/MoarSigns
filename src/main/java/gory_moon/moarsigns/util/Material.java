package gory_moon.moarsigns.util;

import net.minecraft.item.ItemStack;

public class Material {

    public Material(ItemStack block, String texture_name) {
        this.block = block;
        this.texture_name = texture_name;
    }

    public ItemStack block;
    public String texture_name;

}