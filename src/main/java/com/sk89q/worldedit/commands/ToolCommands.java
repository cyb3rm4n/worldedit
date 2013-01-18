// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.tools.*;
import com.sk89q.worldedit.util.TreeGenerator;

public class ToolCommands {
    private final WorldEdit we;

    public ToolCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "none" },
        usage = "",
        desc = "Отвязывает данный инструмент от назначеного действия.",
        min = 0,
        max = 0
    )
    public void none(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), null);
        player.print("Инструмент отвязан.");
    }

    @Command(
        aliases = { "info" },
        usage = "",
        desc = "Информация о блоке",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.tool.info")
    public void info(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), new QueryTool());
        player.print("Информация о инструменте связана с "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "tree" },
        usage = "[type]",
        desc = "Генератор дереьев",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.tool.tree")
    @SuppressWarnings("deprecation")
    public void tree(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        TreeGenerator.TreeType type = args.argsLength() > 0 ?
                type = TreeGenerator.lookup(args.getString(0))
                : TreeGenerator.TreeType.TREE;

        if (type == null) {
            player.printError("Тип дерева '" + args.getString(0) + "' неизвестен.");
            return;
        }

        session.setTool(player.getItemInHand(), new TreePlanter(new TreeGenerator(type)));
        player.print("Функция генерации деревьев добавлена на предмет "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "repl" },
        usage = "<block>",
        desc = "Заменитель блоков",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.tool.replacer")
    public void repl(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        session.setTool(player.getItemInHand(), new BlockReplacer(targetBlock));
        player.print("Инструмент замены блоков поставлен на предмет "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "cycler" },
        usage = "",
        desc = "Генерирует цилиндры",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.tool.data-cycler")
    public void cycler(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), new BlockDataCyler());
        player.print("Функция генерации цилиндров установена на предмет "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "floodfill", "flood" },
        usage = "<pattern> <range>",
        desc = "Заполнитель пола",
        min = 2,
        max = 2
    )
    @CommandPermissions("worldedit.tool.flood-fill")
    public void floodFill(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        int range = args.getInteger(1);

        if (range > config.maxSuperPickaxeSize) {
            player.printError("Максимальный размер: " + config.maxSuperPickaxeSize);
            return;
        }

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        session.setTool(player.getItemInHand(), new FloodFillTool(range, pattern));
        player.print("Функция заполнения пола установлена на предмет "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "brush", "br" },
        desc = "Кисть"
    )
    @NestedCommand(BrushCommands.class)
    public void brush(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
    }

    @Command(
            aliases = { "deltree" },
            usage = "",
            desc = "Инструмент для удаления деревьев",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.tool.deltree")
    public void deltree(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

    session.setTool(player.getItemInHand(), new FloatingTreeRemover());
    player.print("Функция удаления предметов задана на предмет "
            + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
            aliases = { "farwand" },
            usage = "",
            desc = "Инструмент выделения на расстоянии",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.tool.farwand")
    public void farwand(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), new DistanceWand());
        player.print("Функция выделения на расстоянии задана на предмет " + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
            aliases = { "lrbuild", "/lrbuild" },
            usage = "<leftclick block> <rightclick block>",
            desc = "Строительный инструмент",
            min = 2,
            max = 2
    )
    @CommandPermissions("worldedit.tool.lrbuild")
    public void longrangebuildtool(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        BaseBlock secondary = we.getBlock(player, args.getString(0));
        BaseBlock primary = we.getBlock(player, args.getString(1));
        session.setTool(player.getItemInHand(), new LongRangeBuildTool(primary, secondary));
        player.print("Функция стройки задана на предмет " + ItemType.toHeldName(player.getItemInHand()) + ".");
        player.print("Левый клик задан на " + ItemType.toName(secondary.getType()) + "; Правый клик задан на "
                + ItemType.toName(primary.getType()) + ".");
    }
}
