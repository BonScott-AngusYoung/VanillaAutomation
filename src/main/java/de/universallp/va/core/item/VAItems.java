package de.universallp.va.core.item;

/**
 * Created by universallp on 21.03.2016 16:39.
 */
public class VAItems {

    public static ItemGuide itemGuide;
    public static ItemPokeStick itemPokeStick;
    public static ItemDescriptionTag itemDescriptionTag;

    public static void init() {
        itemGuide = new ItemGuide();
        itemPokeStick = new ItemPokeStick();
        itemDescriptionTag = new ItemDescriptionTag();

        ItemVA.items.add(itemDescriptionTag);
        ItemVA.items.add(itemGuide);
        ItemVA.items.add(itemPokeStick);
    }

    public static void register() {
        for (ItemVA item : ItemVA.items)
            item.register();
    }
}
