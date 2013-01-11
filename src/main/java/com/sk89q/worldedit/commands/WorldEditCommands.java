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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;

public class WorldEditCommands {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    private final WorldEdit we;

    public WorldEditCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
            aliases = {"version", "ver"},
            usage = "",
            desc = "Показывает версию WorldEdit",
            min = 0,
            max = 0
    )
    @Console
    public void version(CommandContext args, LocalSession session, LocalPlayer player,
                        EditSession editSession) throws WorldEditException {

        player.print("WorldEdit version " + WorldEdit.getVersion());
        player.print("http://www.sk89q.com/projects/worldedit/");
        player.print("Перевод " + "A" + "l" + "e" + "x" + " " + "B" + "o" + "n" + "d" + " и " + "O" + "l" + "e" + "g" + "5" + "9" + "9");
    }

    @Command(
            aliases = {"reload"},
            usage = "",
            desc = "Перезагружает WorldEdit",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.reload")
    @Console
    public void reload(CommandContext args, LocalSession session, LocalPlayer player,
                       EditSession editSession) throws WorldEditException {

        we.getServer().reload();
        player.print("Конфигурация перезагружена!");
    }

    @Command(
            aliases = {"cui"},
            usage = "",
            desc = "Инициализирует соединение с CUI",
            min = 0,
            max = 0
    )
    public void cui(CommandContext args, LocalSession session, LocalPlayer player,
                    EditSession editSession) throws WorldEditException {
        session.setCUISupport(true);
        session.dispatchCUISetup(player);
    }

    @Command(
            aliases = {"tz"},
            usage = "[зона]",
            desc = "Указывает Ваш временной пояс",
            min = 1,
            max = 1
    )
    @Console
    public void tz(CommandContext args, LocalSession session, LocalPlayer player,
                   EditSession editSession) throws WorldEditException {
        TimeZone tz = TimeZone.getTimeZone(args.getString(0));
        session.setTimezone(tz);
        player.print("Для данной сессии указаная новая временная зона: " + tz.getDisplayName());
        player.print("Текущее время в Вашей временной зоне: "
                + dateFormat.format(Calendar.getInstance(tz).getTime()));
    }

    @Command(
            aliases = {"help"},
            usage = "[<команда>]",
            desc = "Показывает справку для указанной команды или выводит список команд.",
            min = 0,
            max = -1
    )
    @CommandPermissions("worldedit.help")
    @Console
    public void help(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        UtilityCommands.help(args, we, session, player, editSession);
    }
}
