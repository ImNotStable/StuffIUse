package me.jeremiah.minecraft.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author ImNotStable
 * @version 1.0.0
 */
public class AnInventoryScheme {

    private final List<String> masks = new ArrayList<>();
    private final Map<Character, ItemStack> items = new HashMap<>();
    private final Map<Character, Consumer<InventoryClickEvent>> handlers = new HashMap<>();
    private char paginationChar;

    /**
     * Add a mask to this scheme including all sort of characters.
     * For example: "110101011"
     *
     * @param mask a 9 characters mask
     * @return this scheme instance
     */
    public AnInventoryScheme mask(String mask) {
        Objects.requireNonNull(mask);
        this.masks.add(mask.length() > 9 ? mask.substring(0, 10) : mask);

        return this;
    }

    /**
     * Add multiples masks to this scheme including all sort of characters.
     * For example: "111111111", "110101011", "111111111"
     *
     * @param masks multiple 9-characters masks
     * @return this scheme instance
     */
    public AnInventoryScheme masks(String... masks) {
        for (String mask : Objects.requireNonNull(masks))
            mask(mask);
        return this;
    }

    /**
     * Bind character to the corresponding item in the inventory.
     *
     * @param character the associated character in the mask
     * @param item      the item to use for this character
     * @param handler   consumer for the item
     * @return this scheme instance
     */
    public AnInventoryScheme bindItem(char character, ItemStack item, Consumer<InventoryClickEvent> handler) {
        this.items.put(character, Objects.requireNonNull(item));

        if (handler != null) {
            this.handlers.put(character, handler);
        }
        return this;
    }

    /**
     * Bind character to the corresponding item in the inventory.
     *
     * @param character the associated character in the mask
     * @param item      the item to use for this character
     * @return this scheme instance
     */
    public AnInventoryScheme bindItem(char character, ItemStack item) {
        return this.bindItem(character, item, null);
    }

    /**
     * Bind character for pagination content.
     *
     * @param character the associated character in the mask
     * @return this scheme instance
     */
    public AnInventoryScheme bindPagination(char character) {
        this.paginationChar = character;
        return this;
    }

    /**
     * Unbind any item from this character.
     *
     * @param character the character to unbind
     * @return this scheme instance
     */
    public AnInventoryScheme unbindItem(char character) {
        this.items.remove(character);
        this.handlers.remove(character);
        return this;
    }

    /**
     * Apply the current inventory scheme to the FastInv instance.
     *
     * @param inv the FastInv instance to apply this scheme to
     */
    public void apply(AnInventory inv) {
        List<Integer> paginationSlots = new ArrayList<>();

        for (int line = 0; line < this.masks.size(); line++) {
            String mask = this.masks.get(line);

            for (int slot = 0; slot < mask.length(); slot++) {
                char c = mask.charAt(slot);

                if (c == this.paginationChar) {
                    paginationSlots.add(9 * line + slot);
                    continue;
                }

                ItemStack item = this.items.get(c);
                Consumer<InventoryClickEvent> handler = this.handlers.get(c);

                if (item != null) {
                    inv.setItem(9 * line + slot, item, handler);
                }
            }
        }

        if (inv instanceof APaginatedInventory && !paginationSlots.isEmpty()) {
            ((APaginatedInventory) inv).setContentSlots(paginationSlots);
        }
    }
}
