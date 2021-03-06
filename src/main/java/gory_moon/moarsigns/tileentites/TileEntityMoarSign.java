package gory_moon.moarsigns.tileentites;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gory_moon.moarsigns.MoarSigns;
import gory_moon.moarsigns.api.SignInfo;
import gory_moon.moarsigns.api.SignRegistry;
import gory_moon.moarsigns.network.PacketHandler;
import gory_moon.moarsigns.network.message.MessageSignMainInfo;
import gory_moon.moarsigns.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;

public class TileEntityMoarSign extends TileEntitySign {

    private final int NBT_VERSION = 2;

    public int[] rowLocations = new int[4];
    public int[] rowSizes = {0,0,0,0};
    public boolean[] visibleRows = {true, true, true, true};
    public boolean lockedChanges;

    public boolean isMetal = false;
    public String texture_name;
    public boolean showInGui = false;
    public boolean isRemovedByPlayerAndCreative;
    private boolean isEditable = true;

    private EntityPlayer playerEditing;
    private ResourceLocation resourceLocation;
    private boolean textureReq = false;

    public TileEntityMoarSign() {
        super();
        for (int i = 0; i < 4; i++) {
            rowLocations[i] = 2 + 10 * i;
        }
    }

    @Override
    public void updateEntity() {

        if (worldObj.isRemote) {

            /*if (fontSize != oldFontSize) {
                rows = Utils.getRows(fontSize);
                maxLength = Utils.getMaxLength(fontSize);
                oldFontSize = fontSize;
            }*/
            if (!textureReq) {
                textureReq = true;
                Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
                worldObj.addBlockEvent(xCoord, yCoord, zCoord, block, 0, 0);
            }
            SignInfo sign = SignRegistry.get(texture_name);
            if (sign != null && sign.property != null) sign.property.onUpdate();
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("nbtVersion", NBT_VERSION);

        for (int i = 0; i < 4; i++) {
            compound.setString("Text" + (i + 1), signText[i]);
        }

        NBTTagList settings = new NBTTagList();

        int[] loc = new int[5];
        loc[0] = 0;
        System.arraycopy(rowLocations, 0, loc, 1, 4);

        int[] size = new int[5];
        size[0] = 1;
        System.arraycopy(rowSizes, 0, size, 1, 4);

        int[] visible = new int[5];
        visible[0] = 2;
        for (int i = 0; i < 4; i++) visible[i + 1] = visibleRows[i] ? 1: 0;

        NBTTagIntArray locations = new NBTTagIntArray(loc);
        NBTTagIntArray sizes = new NBTTagIntArray(size);
        NBTTagIntArray hidden = new NBTTagIntArray(visible);

        settings.appendTag(locations);
        settings.appendTag(sizes);
        settings.appendTag(hidden);

        compound.setTag("settings", settings);
        compound.setBoolean("lockedChanges", lockedChanges);
        compound.setBoolean("isMetal", isMetal);
        compound.setString("texture", texture_name);
    }

    public void readFromNBT(NBTTagCompound compound) {
        isEditable = false;
        super.readFromNBT(compound);

        int nbtVersion = compound.getInteger("nbtVersion");

        if (nbtVersion == 1) {
            int fontSize = compound.getInteger("fontSize");
            int rows = Utils.getRows(fontSize);
            int maxLength = Utils.getMaxLength(fontSize);

            rowSizes = new int[] {fontSize, fontSize, fontSize, fontSize};
            visibleRows = new boolean[] {false, false, false, false};
            for (int i = 0; i < rows; i++) {
                visibleRows[i] = true;
            }

            for (int i = 0; i < 4; ++i) {
                signText[i] = compound.getString("Text" + (i + 1));

                if (signText[i].length() > maxLength) {
                    signText[i] = FMLClientHandler.instance().getClient().fontRenderer.trimStringToWidth(signText[i], maxLength);
                }

                if (i > rows) {
                    signText[i] = "";
                }
            }

            int textOffset = compound.getInteger("textOffset");
            for (int i = 0; i < 4; i++) {
                int temp = textOffset + rowLocations[i];

                if (temp > 20) temp = 20;
                if (temp < 0) temp = 0;

                rowLocations[i] = temp;
            }


        } else if (nbtVersion == 2) {

            lockedChanges = compound.getBoolean("lockedChanges");

            NBTTagList settings = compound.getTagList("settings", 11);

            for (int i = 0; i < settings.tagCount(); i++) {
                int[] array = settings.func_150306_c(i);
                if (array[0] == 0) {
                    System.arraycopy(array, 1, rowLocations, 0, 4);
                } else if (array[0] == 1) {
                    System.arraycopy(array, 1, rowSizes, 0, 4);
                }else if (array[0] == 2) {
                    int[] hidden = new int[4];
                    System.arraycopy(array, 1, hidden, 0, 4);
                    for (int j = 0; j < 4; j++) visibleRows[j] = hidden[j] == 1;
                }

            }


            for (int i = 0; i < 4; ++i) {
                signText[i] = compound.getString("Text" + (i + 1));

                int maxLength = Utils.getMaxLength(rowSizes[i]);
                if (signText[i].length() > maxLength) {
                    signText[i] = FMLClientHandler.instance().getClient().fontRenderer.trimStringToWidth(signText[i], maxLength);
                }

                if (!visibleRows[i]) {
                    signText[i] = "";
                }
            }
        }

        isMetal = compound.getBoolean("isMetal");
        texture_name = compound.getString("texture");

    }

    @Override
    public Packet getDescriptionPacket() {
        return PacketHandler.INSTANCE.getPacketFrom(new MessageSignMainInfo(this));
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    @SideOnly(Side.CLIENT)
    public void setEditable(boolean state) {
        this.isEditable = state;

        if (!state) {
            playerEditing = null;
        }
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String texture) {
        if (!worldObj.isRemote) {
            texture_name = texture;
        } else if (resourceLocation == null) {
            texture_name = texture;
            resourceLocation = MoarSigns.instance.getResourceLocation(texture, isMetal);
        }
    }

    @Override
    public void func_145912_a(EntityPlayer par1EntityPlayer) {
        this.playerEditing = par1EntityPlayer;
    }

    @Override
    public EntityPlayer func_145911_b() {
        return this.playerEditing;
    }
}
