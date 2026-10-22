package com.bolgarka;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BolgarkaMod.MODID);

    /**
     * Сколько частей тела уже отпилено у моба (0..5). Хранится в NBT моба
     * и автоматически синхронизируется с клиентами, чтобы они могли скрыть части модели.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> CUT_STAGE =
            ATTACHMENT_TYPES.register("cut_stage", () ->
                    AttachmentType.builder(() -> 0)
                            .serialize(Codec.INT)
                            .sync(ByteBufCodecs.VAR_INT)
                            .build());
}
