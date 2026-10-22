package com.bolgarka.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import com.bolgarka.BolgarkaMod;
import com.bolgarka.Limb;
import com.bolgarka.ModAttachments;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = BolgarkaMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {

    /** Как часто (в тиках) болгарка "пилит" при зажатой ЛКМ. 4 тика = 5 раз в секунду. */
    private static final int SAW_INTERVAL = 4;

    private static int heldTicks = 0;

    // ---------------------------------------------------------------------
    // 1. Зажатая ЛКМ = непрерывное пиление
    // ---------------------------------------------------------------------
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.gameMode == null || mc.screen != null || player.isSpectator()
                || !mc.options.keyAttack.isDown()
                || !player.getMainHandItem().is(BolgarkaMod.ANGLE_GRINDER.get())) {
            heldTicks = 0;
            return;
        }

        heldTicks++;
        // Самый первый клик обрабатывает сам Майнкрафт, дальше пилим автоматически
        if (heldTicks > SAW_INTERVAL && heldTicks % SAW_INTERVAL == 0
                && mc.hitResult instanceof EntityHitResult hit) {
            mc.gameMode.attack(player, hit.getEntity());
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    // ---------------------------------------------------------------------
    // 2. Скрываем отпиленные части модели моба
    // ---------------------------------------------------------------------
    private static final List<ModelPart> HIDDEN = new ArrayList<>();
    private static final Map<EntityModel<?>, EnumMap<Limb, List<ModelPart>>> PART_CACHE = new WeakHashMap<>();

    @SubscribeEvent
    public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        restoreHidden();

        LivingEntity entity = event.getEntity();
        if (!entity.hasData(ModAttachments.CUT_STAGE)) {
            return;
        }
        int stage = entity.getData(ModAttachments.CUT_STAGE);
        if (stage <= 0) {
            return;
        }

        EntityModel<?> model = event.getRenderer().getModel();
        EnumMap<Limb, List<ModelPart>> parts = PART_CACHE.computeIfAbsent(model, ClientEvents::scanModel);

        Limb[] limbs = Limb.values();
        for (int i = 0; i < Math.min(stage, limbs.length); i++) {
            for (ModelPart part : parts.getOrDefault(limbs[i], List.of())) {
                if (part.visible) {
                    part.visible = false;
                    HIDDEN.add(part);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        restoreHidden();
    }

    private static void restoreHidden() {
        if (HIDDEN.isEmpty()) {
            return;
        }
        for (ModelPart part : HIDDEN) {
            part.visible = true;
        }
        HIDDEN.clear();
    }

    /**
     * Ищем у модели поля-части (ModelPart) по названию: head, leftArm, right_leg, leftHindLeg и т.д.
     * Работает для зомби, скелетов, жителей-гуманоидов, криперов, свиней, коров, волков, пауков, големов...
     * Для моделей с нестандартными названиями просто ничего не скрывается (ничего не ломается).
     */
    private static EnumMap<Limb, List<ModelPart>> scanModel(EntityModel<?> model) {
        EnumMap<Limb, List<ModelPart>> result = new EnumMap<>(Limb.class);
        for (Class<?> c = model.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !ModelPart.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                Limb limb = classify(field.getName().toLowerCase(Locale.ROOT));
                if (limb == null) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart part) {
                        result.computeIfAbsent(limb, k -> new ArrayList<>()).add(part);
                    }
                } catch (Exception ignored) {
                    // поле недоступно - пропускаем
                }
            }
        }
        return result;
    }

    private static Limb classify(String name) {
        if (name.equals("hat") || name.contains("head")) {
            return Limb.HEAD;
        }
        boolean left = name.contains("left");
        boolean right = name.contains("right");
        if (left == right) {
            return null;
        }
        // У четвероногих передние лапы считаем "руками", задние - "ногами"
        if (name.contains("arm") || name.contains("front")) {
            return right ? Limb.RIGHT_ARM : Limb.LEFT_ARM;
        }
        if (name.contains("leg")) {
            return right ? Limb.RIGHT_LEG : Limb.LEFT_LEG;
        }
        return null;
    }
}
