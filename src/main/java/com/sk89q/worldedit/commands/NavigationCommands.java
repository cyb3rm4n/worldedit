// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

import static com.sk89q.minecraft.util.commands.Logging.LogMode.POSITION;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.util.StringUtil;

/**
 * Navigation commands.
 *
 * @author sk89q
 */
public class NavigationCommands {
    @SuppressWarnings("unused")
    private final WorldEdit we;

    public NavigationCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
            aliases = {"unstuck", "!"},
            usage = "",
            desc = "Выбраться из блока.",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.navigation.unstuck")
    public void unstuck(CommandContext args, LocalSession session, LocalPlayer player,
                        EditSession editSession) throws WorldEditException {

        player.print("Свободен!");
        player.findFreePosition();
    }

    @Command(
            aliases = {"ascend", "asc"},
            usage = "[# of levels]",
            desc = "Подняться на этаж выше.",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.navigation.ascend")
    public void ascend(CommandContext args, LocalSession session, LocalPlayer player,
                       EditSession editSession) throws WorldEditException {
        int levelsToAscend = 0;
        if (args.argsLength() == 0) {
            levelsToAscend = 1;
        } else {
            levelsToAscend = args.getInteger(0);
        }
        int ascentLevels = 1;
        while (player.ascendLevel() && levelsToAscend != ascentLevels) {
            ++ascentLevels;
        }
        if (ascentLevels == 0) {
            player.printError("Нет свободного места выше, которое вы нашли.");
        } else {
            player.print((ascentLevels != 1) ? "Вы поднялись на " + Integer.toString(ascentLevels) + " " + StringUtil.plural(ascentLevels, "уровень", "уровня", "уровней") + "." : "Подняться на уровни(вень).");
        }
    }

    @Command(
            aliases = {"descend", "desc"},
            usage = "[# of floors]",
            desc = "Подняться на этаж ниже",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.navigation.descend")
    public void descend(CommandContext args, LocalSession session, LocalPlayer player,
                        EditSession editSession) throws WorldEditException {
        int levelsToDescend = 0;
        if (args.argsLength() == 0) {
            levelsToDescend = 1;
        } else {
            levelsToDescend = args.getInteger(0);
        }
        int descentLevels = 1;
        while (player.descendLevel() && levelsToDescend != descentLevels) {
            ++descentLevels;
        }
        if (descentLevels == 0) {
            player.printError("Нет свободного места ниже, которое вы нашли.");
        } else {
            player.print((descentLevels != 1) ? "Вы опустились на " + Integer.toString(descentLevels) + " уровней(ня)." : "Опуститься на уровни(вень).");
        }
    }

    @Command(
            aliases = {"ceil"},
            usage = "[clearance]",
            desc = "Перейти на потолок",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.navigation.ceiling")
    @Logging(POSITION)
    public void ceiling(CommandContext args, LocalSession session, LocalPlayer player,
                        EditSession editSession) throws WorldEditException {

        int clearence = args.argsLength() > 0 ?
                Math.max(0, args.getInteger(0)) : 0;

        if (player.ascendToCeiling(clearence)) {
            player.print("Уииии!");
        } else {
            player.printError("Нет свободного места, которое вы нашли.");
        }
    }

    @Command(
            aliases = {"thru"},
            usage = "",
            desc = "Проходить сквозь стены",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.navigation.thru.command")
    public void thru(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        if (player.passThroughForwardWall(6)) {
            player.print("Уииии!");
        } else {
            player.printError("Свободного места перед Вами не найдено.");
        }
    }

    @Command(
            aliases = {"jumpto", "j"},
            usage = "",
            desc = "Телепорт на координаты",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.navigation.jumpto.command")
    public void jumpTo(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        WorldVector pos = player.getSolidBlockTrace(300);
        if (pos != null) {
            player.findFreePosition(pos);
            player.print("Бум!");
        } else {
            player.printError("Блоков в поле зрения нет!");
        }
    }

    @Command(
            aliases = {"up"},
            usage = "<block>",
            desc = "Подняться вверх на некоторое расстояние",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.navigation.up")
    @Logging(POSITION)
    public void up(CommandContext args, LocalSession session, LocalPlayer player,
                   EditSession editSession) throws WorldEditException {

        int distance = args.getInteger(0);

        if (player.ascendUpwards(distance)) {
            player.print("Уииии!");
        } else {
            player.printError("Вас ударило что-то выше Вас.");
        }
    }
}
