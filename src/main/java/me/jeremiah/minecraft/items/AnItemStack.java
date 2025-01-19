/*
 * This file is part of FastInv, licensed under the MIT License.
 *
 * Copyright (c) 2018-2021 MrMicky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.jeremiah.minecraft.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simple {@link ItemStack} builder.
 *
 * @author ImNotStable
 * @version 1.0.0
 */
public class AnItemStack {

    private @NotNull ItemStack item;

    public static AnItemStack copyOf(@NotNull ItemStack item) {
        return new AnItemStack(item.clone());
    }

    public AnItemStack(@NotNull Material material) {
        this(new ItemStack(material));
    }

    public AnItemStack(@NotNull ItemStack item) {
        this.item = item;
    }

    public AnItemStack edit(@NotNull Consumer<ItemStack> function) {
        function.accept(this.item);
        return this;
    }

    public AnItemStack meta(@NotNull Consumer<ItemMeta> metaConsumer) {
        return edit(item -> item.editMeta(metaConsumer));
    }

    public <T extends ItemMeta> AnItemStack meta(@NotNull Class<T> metaClass, @NotNull Consumer<T> metaConsumer) {
        return meta(meta -> {
            if (!metaClass.isInstance(meta))
                throw new IllegalStateException("Expected " + metaClass + ", got " + meta.getClass());
            metaConsumer.accept(metaClass.cast(meta));
        });
    }

    public AnItemStack type(@NotNull Material material) {
        item = item.withType(material);
        return this;
    }

    public AnItemStack amount(int amount) {
        return edit(item -> item.setAmount(amount));
    }

    public AnItemStack name(@NotNull Component name) {
        return meta(meta -> meta.displayName(name));
    }

    public AnItemStack lore(@NotNull Component lore) {
        return lore(Collections.singletonList(lore));
    }

    public AnItemStack lore(Component @NotNull ... lore) {
        return lore(Arrays.asList(lore));
    }

    public AnItemStack lore(List<Component> lore) {
        return meta(meta -> meta.lore(lore));
    }

    public AnItemStack addLore(Component line) {
        return addLore(Collections.singletonList(line));
    }

    public AnItemStack addLore(Component... lines) {
        return addLore(Arrays.asList(lines));
    }

    public AnItemStack addLore(List<Component> lines) {
        return meta(meta -> {
            List<Component> lore = meta.lore();

            if (lore == null) {
                meta.lore(lines);
                return;
            }

            lore.addAll(lines);
            meta.lore(lore);
        });
    }

    public AnItemStack enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public AnItemStack enchant(Enchantment enchantment, int level) {
        return meta(meta -> meta.addEnchant(enchantment, level, true));
    }

    public AnItemStack removeEnchant(Enchantment enchantment) {
        return meta(meta -> meta.removeEnchant(enchantment));
    }

    public AnItemStack removeEnchants() {
        return meta(m -> m.getEnchants().keySet().forEach(m::removeEnchant));
    }

    public AnItemStack flags(ItemFlag... flags) {
        return meta(meta -> meta.addItemFlags(flags));
    }

    public AnItemStack flags() {
        return flags(ItemFlag.values());
    }

    public AnItemStack removeFlags(ItemFlag... flags) {
        return meta(meta -> meta.removeItemFlags(flags));
    }

    public AnItemStack removeFlags() {
        return removeFlags(ItemFlag.values());
    }

    public ItemStack build() {
        return this.item;
    }

}
