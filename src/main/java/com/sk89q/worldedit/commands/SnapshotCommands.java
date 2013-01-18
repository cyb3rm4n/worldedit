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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.data.MissingWorldException;
import com.sk89q.worldedit.snapshots.InvalidSnapshotException;
import com.sk89q.worldedit.snapshots.Snapshot;

/**
 * Snapshot commands.
 * 
 * @author sk89q
 */
public class SnapshotCommands {
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    
    private final WorldEdit we;

    public SnapshotCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
            aliases = { "list" },
            usage = "[num]",
            desc = "Список резервных копий",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.snapshots.list")
    public void list(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Резервная копия не настроена.");
            return;
        }

        try {
            List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, player.getWorld().getName());

            if (snapshots.size() > 0) {

                int num = args.argsLength() > 0 ? Math.min(40, Math.max(5, args.getInteger(0))) : 5;

                player.print("Резервные копии для мира: '" + player.getWorld().getName() + "'");
                for (byte i = 0; i < Math.min(num, snapshots.size()); i++) {
                    player.print((i + 1) + ". " + snapshots.get(i).getName());
                }

                player.print("Используйте /snap use [snapshot] или /snap.");
            } else {
                player.printError("Резервных копий не найдено. Смотрите консоль для подробных деталей.");

                // Okay, let's toss some debugging information!
                File dir = config.snapshotRepo.getDirectory();

                try {
                    logger.info("WorldEdit не нашел разервных копий: посмотрел в: "
                            + dir.getCanonicalPath());
                } catch (IOException e) {
                    logger.info("WorldEdit found no snapshots: looked in "
                            + "(NON-RESOLVABLE PATH - does it exist?): "
                            + dir.getPath());
                }
            }
        } catch (MissingWorldException ex) {
            player.printError("Резервных копий для этого мира нет.");
        }
    }

    @Command(
            aliases = { "use" },
            usage = "<snapshot>",
            desc = "Выбирает резервную копию для использования",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void use(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Резервная копия не настроена.");
            return;
        }

        String name = args.getString(0);

        // Want the latest snapshot?
        if (name.equalsIgnoreCase("latest")) {
            try {
                Snapshot snapshot = config.snapshotRepo.getDefaultSnapshot(player.getWorld().getName());

                if (snapshot != null) {
                    session.setSnapshot(null);
                    player.print("Используется новейшая резервная копия.");
                } else {
                    player.printError("Резервная копия не найдена.");
                }
            } catch (MissingWorldException ex) {
                player.printError("Резервных копий в этом мире нет.");
            }
        } else {
            try {
                session.setSnapshot(config.snapshotRepo.getSnapshot(name));
                player.print("Резервная копия загружена под именем: " + name);
            } catch (InvalidSnapshotException e) {
                player.printError("Эта резервная копия несуществует или недоступна.");
            }
        }
    }

    @Command(
            aliases = { "sel" },
            usage = "<index>",
            desc = "Выбирает резервную копию на основе списка",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void sel(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Резервная копия не настроена.");
            return;
        }

        int index = -1;
        try {
            index = Integer.parseInt(args.getString(0));
        } catch (NumberFormatException e) {
            player.printError("Неверный индекс, " + args.getString(0) + " не является целым числом.");
            return;
        }

        if (index < 1) {
            player.printError("Индекс должен быть равен или выше одного.");
            return;
        }

        try {
            List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, player.getWorld().getName());
            if (snapshots.size() < index) {
                player.printError("Неверный индекс, должен быть между 1 и " + snapshots.size() + ".");
                return;
            }
            Snapshot snapshot = snapshots.get(index - 1);
            if (snapshot == null) {
                player.printError("Эта резервная копия несуществует или повреждена.");
                return;
            }
            session.setSnapshot(snapshot);
            player.print("Резервная копия загружена под именем: " + snapshot.getName());
        } catch (MissingWorldException e) {
            player.printError("Резервных копий для этого мира нет.");
        }
    }

    @Command(
            aliases = { "before" },
            usage = "<date>",
            desc = "Выберает ближайшую резервную копию до даты",
            min = 1,
            max = -1
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void before(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Резервная копия не настроена.");
            return;
        }

        Calendar date = session.detectDate(args.getJoinedStrings(0));

        if (date == null) {
            player.printError("Невозможно найти введенную дату.");
        } else {
            try {
                Snapshot snapshot = config.snapshotRepo.getSnapshotBefore(date, player.getWorld().getName());

                if (snapshot == null) {
                    dateFormat.setTimeZone(session.getTimeZone());
                    player.printError("Не удалось найти резервную копию до "
                            + dateFormat.format(date.getTime()) + ".");
                } else {
                    session.setSnapshot(snapshot);
                    player.print("Резервная копия загружена под именем: " + snapshot.getName());
                }
            } catch (MissingWorldException ex) {
                player.printError("Резервных копий в этом мире нет.");
            }
        }
    }

    @Command(
            aliases = { "after" },
            usage = "<date>",
            desc = "Выбирает ближайшую резервную копию после даты.",
            min = 1,
            max = -1
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void after(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Резервная копия не настроена.");
            return;
        }

        Calendar date = session.detectDate(args.getJoinedStrings(0));

        if (date == null) {
            player.printError("Невозможно найти введенную дату.");
        } else {
            try {
                Snapshot snapshot = config.snapshotRepo.getSnapshotAfter(date, player.getWorld().getName());
                if (snapshot == null) {
                    dateFormat.setTimeZone(session.getTimeZone());
                    player.printError("Невозможно найти резервную копию после "
                            + dateFormat.format(date.getTime()) + ".");
                } else {
                    session.setSnapshot(snapshot);
                    player.print("Резервная копия загружена под именем: " + snapshot.getName());
                }
            } catch (MissingWorldException ex) {
                player.printError("Резервных копий в этом мире нет.");
            }
        }
    }
}
