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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandAlias;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.data.ChunkStore;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.EllipsoidRegionSelector;
import com.sk89q.worldedit.regions.ExtendingCuboidRegionSelector;
import com.sk89q.worldedit.regions.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.SphereRegionSelector;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.regions.CylinderRegionSelector;
import com.sk89q.worldedit.util.StringUtil;

/**
 * Selection commands.
 *
 * @author sk89q
 */
public class SelectionCommands {
    private final WorldEdit we;

    public SelectionCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
            aliases = {"/pos1"},
            usage = "[coordinates]",
            desc = "Задает первую позицию",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.selection.pos")
    public void pos1(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        Vector pos;

        if (args.argsLength() == 1) {
            if (args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
                String[] coords = args.getString(0).split(",");
                pos = new Vector(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
            } else {
                player.printError("Неверные координаты " + args.getString(0));
                return;
            }
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos)) {
            player.printError("Позиция уже задана.");
            return;
        }

        session.getRegionSelector(player.getWorld())
                .explainPrimarySelection(player, session, pos);
    }

    @Command(
            aliases = {"/pos2"},
            usage = "[coordinates]",
            desc = "Задает вторую позицию",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.selection.pos")
    public void pos2(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        Vector pos;
        if (args.argsLength() == 1) {
            if (args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
                String[] coords = args.getString(0).split(",");
                pos = new Vector(Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]));
            } else {
                player.printError("Неверные координаты " + args.getString(0));
                return;
            }
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos)) {
            player.printError("Позиция уже задана.");
            return;
        }

        session.getRegionSelector(player.getWorld())
                .explainSecondarySelection(player, session, pos);
    }

    @Command(
            aliases = {"/hpos1"},
            usage = "",
            desc = "Задает первую позицию на блок, на который Вы смотрите.",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos1(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {

        Vector pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld())
                    .selectPrimary(pos)) {
                player.printError("Позиция уже задана.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainPrimarySelection(player, session, pos);
        } else {
            player.printError("Нет блоков в поле зрения!");
        }
    }

    @Command(
            aliases = {"/hpos2"},
            usage = "",
            desc = "Задает вторую позицию на блок, на который Вы смотрите.",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos2(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {

        Vector pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld())
                    .selectSecondary(pos)) {
                player.printError("Позиция уже задана.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainSecondarySelection(player, session, pos);
        } else {
            player.printError("Нет блоков в поле зрения!");
        }
    }

    @Command(
            aliases = {"/chunk"},
            usage = "",
            flags = "s",
            desc = "Выделение текущего чанка.",
            help =
                    "Выделяет текущий чанк,в котором Вы находитесь.\n" +
                            "С флагом -s Ваше выделенная территория расширится\n" +
                            "чтобы охватить все чанки, которые являются его частью.",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.selection.chunk")
    public void chunk(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {

        final Vector min;
        final Vector max;
        final LocalWorld world = player.getWorld();
        if (args.hasFlag('s')) {
            Region region = session.getSelection(world);

            final Vector2D min2D = ChunkStore.toChunk(region.getMinimumPoint());
            final Vector2D max2D = ChunkStore.toChunk(region.getMaximumPoint());

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = new Vector(max2D.getBlockX() * 16 + 15, world.getMaxY(), max2D.getBlockZ() * 16 + 15);

            player.print("Чанк выделен: ("
                    + min2D.getBlockX() + ", " + min2D.getBlockZ() + ") - ("
                    + max2D.getBlockX() + ", " + max2D.getBlockZ() + ")");
        } else {
            final Vector2D min2D = ChunkStore.toChunk(player.getBlockIn());

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = min.add(15, world.getMaxY(), 15);

            player.print("Чанк выбран: "
                    + min2D.getBlockX() + ", " + min2D.getBlockZ());
        }

        final CuboidRegionSelector selector;
        if (session.getRegionSelector(world) instanceof ExtendingCuboidRegionSelector)
            selector = new ExtendingCuboidRegionSelector(world);
        else
            selector = new CuboidRegionSelector(world);
        selector.selectPrimary(min);
        selector.selectSecondary(max);
        session.setRegionSelector(world, selector);

        session.dispatchCUISelection(player);

    }

    @Command(
            aliases = {"/wand"},
            usage = "",
            desc = "Получение предмета для выделения позиций",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.wand")
    public void wand(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        player.giveItem(we.getConfiguration().wandItem, 1);
        player.print("Левый клик: выделение первой позиции; Правый клик: выделение второй точки");
    }

    @Command(
            aliases = {"toggleeditwand"},
            usage = "",
            desc = "Перекючает функциональность предмета для выделения позиций.",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.wand.toggle")
    public void toggleWand(CommandContext args, LocalSession session, LocalPlayer player,
                           EditSession editSession) throws WorldEditException {

        session.setToolControl(!session.isToolControlEnabled());

        if (session.isToolControlEnabled()) {
            player.print("Теперь предмет выделяет позиции.");
        } else {
            player.print("Теперь предмет не выделяет позиции и у него стандартное применение.");
        }
    }

    @Command(
            aliases = {"/expand"},
            usage = "<amount> [reverse-amount] <direction>",
            desc = "Расширяет выделенную территорию",
            min = 1,
            max = 3
    )
    @CommandPermissions("worldedit.selection.expand")
    public void expand(CommandContext args, LocalSession session, LocalPlayer player,
                       EditSession editSession) throws WorldEditException {

        // Special syntax (//expand vert) to expand the selection between
        // sky and bedrock.
        if (args.getString(0).equalsIgnoreCase("vert")
                || args.getString(0).equalsIgnoreCase("vertical")) {
            Region region = session.getSelection(player.getWorld());
            try {
                int oldSize = region.getArea();
                region.expand(
                        new Vector(0, (player.getWorld().getMaxY() + 1), 0),
                        new Vector(0, -(player.getWorld().getMaxY() + 1), 0));
                session.getRegionSelector(player.getWorld()).learnChanges();
                int newSize = region.getArea();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
                player.print("Регион расширен на " + (newSize - oldSize)
                        + " " + StringUtil.plural((newSize - oldSize), "блок", "блока", "блоков") + ".");
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }

            return;
        }

        Vector dir;
        int change = args.getInteger(0);
        int reverseChange = 0;

        switch (args.argsLength()) {
            case 2:
                // Either a reverse amount or a direction
                try {
                    reverseChange = args.getInteger(1);
                    dir = we.getDirection(player, "me");
                } catch (NumberFormatException e) {
                    dir = we.getDirection(player,
                            args.getString(1).toLowerCase());
                }
                break;

            case 3:
                // Both reverse amount and direction
                reverseChange = args.getInteger(1);
                dir = we.getDirection(player,
                        args.getString(2).toLowerCase());
                break;
            default:
                dir = we.getDirection(player, "me");
        }

        Region region = session.getSelection(player.getWorld());
        int oldSize = region.getArea();

        if (reverseChange == 0) {
            region.expand(dir.multiply(change));
        } else {
            region.expand(dir.multiply(change), dir.multiply(-reverseChange));
        }

        session.getRegionSelector(player.getWorld()).learnChanges();
        int newSize = region.getArea();

        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);

        player.print("Регион расширен на " + (newSize - oldSize) + " " + StringUtil.plural((newSize - oldSize), "блок", "блока", "блоков") + ".");
    }

    @Command(
            aliases = {"/contract"},
            usage = "<amount> [reverse-amount] [direction]",
            desc = "Уменьшает выделенную область в заданом направлении.",
            min = 1,
            max = 3
    )
    @CommandPermissions("worldedit.selection.contract")
    public void contract(CommandContext args, LocalSession session, LocalPlayer player,
                         EditSession editSession) throws WorldEditException {

        Vector dir;
        int change = args.getInteger(0);
        int reverseChange = 0;

        switch (args.argsLength()) {
            case 2:
                // Either a reverse amount or a direction
                try {
                    reverseChange = args.getInteger(1);
                    dir = we.getDirection(player, "me");
                } catch (NumberFormatException e) {
                    dir = we.getDirection(player, args.getString(1).toLowerCase());
                }
                break;

            case 3:
                // Both reverse amount and direction
                reverseChange = args.getInteger(1);
                dir = we.getDirection(player, args.getString(2).toLowerCase());
                break;
            default:
                dir = we.getDirection(player, "me");
        }

        try {
            Region region = session.getSelection(player.getWorld());
            int oldSize = region.getArea();
            if (reverseChange == 0) {
                region.contract(dir.multiply(change));
            } else {
                region.contract(dir.multiply(change), dir.multiply(-reverseChange));
            }
            session.getRegionSelector(player.getWorld()).learnChanges();
            int newSize = region.getArea();

            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);


            player.print("Регион уменьшен на " + (oldSize - newSize) + " " + StringUtil.plural((newSize - oldSize), "блок", "блока", "блоков") + ".");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
            aliases = {"/shift"},
            usage = "<amount> [direction]",
            desc = "Сдвигает выделенную территорию в заданном направлении.",
            min = 1,
            max = 2
    )
    @CommandPermissions("worldedit.selection.shift")
    public void shift(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {
        Vector dir;

        int change = args.getInteger(0);
        if (args.argsLength() == 2) {
            dir = we.getDirection(player, args.getString(1).toLowerCase());
        } else {
            dir = we.getDirection(player, "me");
        }

        try {
            Region region = session.getSelection(player.getWorld());
            region.shift(dir.multiply(change));
            session.getRegionSelector(player.getWorld()).learnChanges();

            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);

            player.print("Регион сдвинут.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
            aliases = {"/outset"},
            usage = "<amount>",
            desc = "Расширение выделеннрй области во всех направлениях.",
            help =
                    "Расширяет выделенную область во всех направлениях.\n" +
                            "Флаги:\n" +
                            "  -h только горизонтально\n" +
                            "  -v только вертикально\n",
            flags = "hv",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.selection.outset")
    public void outset(CommandContext args, LocalSession session, LocalPlayer player,
                       EditSession editSession) throws WorldEditException {
        Region region = session.getSelection(player.getWorld());
        region.expand(getChangesForEachDir(args));
        session.getRegionSelector(player.getWorld()).learnChanges();
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
        player.print("Регион расширен.");
    }

    @Command(
            aliases = {"/inset"},
            usage = "<amount>",
            desc = "Сузить выделенную территорию.",
            help =
                    "Сужает выделенну территорию во всех направлениях.\n" +
                            "Флаги:\n" +
                            "  -h тоько горизонтально\n" +
                            "  -v только вертикально\n",
            flags = "hv",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.selection.inset")
    public void inset(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {
        Region region = session.getSelection(player.getWorld());
        region.contract(getChangesForEachDir(args));
        session.getRegionSelector(player.getWorld()).learnChanges();
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
        player.print("Регион сужен.");
    }

    private Vector[] getChangesForEachDir(CommandContext args) {
        List<Vector> changes = new ArrayList<Vector>(6);
        int change = args.getInteger(0);

        if (!args.hasFlag('h')) {
            changes.add((new Vector(0, 1, 0)).multiply(change));
            changes.add((new Vector(0, -1, 0)).multiply(change));
        }

        if (!args.hasFlag('v')) {
            changes.add((new Vector(1, 0, 0)).multiply(change));
            changes.add((new Vector(-1, 0, 0)).multiply(change));
            changes.add((new Vector(0, 0, 1)).multiply(change));
            changes.add((new Vector(0, 0, -1)).multiply(change));
        }

        return changes.toArray(new Vector[0]);
    }

    @Command(
            aliases = {"/size"},
            flags = "c",
            usage = "",
            desc = "Получает информацию о выделенной территории",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.selection.size")
    public void size(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        if (args.hasFlag('c')) {
            CuboidClipboard clipboard = session.getClipboard();
            Vector size = clipboard.getSize();
            Vector offset = clipboard.getOffset();

            player.print("Размер: " + size);
            player.print("Смещение: " + offset);
            player.print("Диагональ: " + size.distance(new Vector(1, 1, 1)));
            player.print("Количество блоков: "
                    + (int) (size.getX() * size.getY() * size.getZ()));
            return;
        }

        Region region = session.getSelection(player.getWorld());
        Vector size = region.getMaximumPoint()
                .subtract(region.getMinimumPoint())
                .add(1, 1, 1);

        player.print("Тип: " + session.getRegionSelector(player.getWorld())
                .getTypeName());

        for (String line : session.getRegionSelector(player.getWorld())
                .getInformationLines()) {
            player.print(line);
        }

        player.print("Размер: " + size);
        player.print("Диагональ кубоида: " + region.getMaximumPoint()
                .distance(region.getMinimumPoint()));
        player.print("Количество блоков: " + region.getArea());
    }


    @Command(
            aliases = {"/count"},
            usage = "<block>",
            desc = "Подсчет количества определенного типа блока",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.analysis.count")
    public void count(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {

        Set<Integer> searchIDs = we.getBlockIDs(player,
                args.getString(0), true);
        int a = editSession.countBlocks(session.getSelection(player.getWorld()), searchIDs);
        player.print("В выделенной территории " +
                a + StringUtil.plural(a, " блок", " блока", " блоков") + " заданного типа.");
    }

    @Command(
            aliases = {"/distr"},
            usage = "",
            desc = "Подсчет колличества блоков в выделенной территории.",
            help =
                    "Считает колличество блоков в выделенной территории.\n" +
                            "Флаг -c считет блоки в буфере обмена.",
            flags = "c",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.analysis.distr")
    public void distr(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {

        List<Countable<Integer>> distribution;
        int size;

        if (args.hasFlag('c')) {
            CuboidClipboard clip = session.getClipboard();
            distribution = clip.getBlockDistribution();
            size = clip.getHeight() * clip.getLength() * clip.getWidth();
        } else {
            distribution = editSession
                    .getBlockDistribution(session.getSelection(player.getWorld()));
            size = session.getSelection(player.getWorld()).getArea();
        }

        if (distribution.size() <= 0) {  // *Should* always be true
            player.printError("В выделенной территории блоки не найдены.");
            return;
        }

        player.print("В выделенной территории " + size + StringUtil.plural(size, " блок", " блока", " блоков"));

        for (Countable<Integer> c : distribution) {
            BlockType block = BlockType.fromID(c.getID());
            String str = String.format("%-7s (%.3f%%) %s #%d",
                    String.valueOf(c.getAmount()),
                    c.getAmount() / (double) size * 100,
                    block == null ? "Неизвестно" : block.getName(), c.getID());
            player.print(str);
        }
    }

    @Command(
            aliases = {"/sel", ";"},
            usage = "[cuboid|extend|poly|ellipsoid|sphere|cyl]",
            desc = "Выбирает тип выделения региона",
            min = 0,
            max = 1
    )
    public void select(CommandContext args, LocalSession session, LocalPlayer player,
                       EditSession editSession) throws WorldEditException {

        final LocalWorld world = player.getWorld();
        if (args.argsLength() == 0) {
            session.getRegionSelector(world).clear();
            session.dispatchCUISelection(player);
            player.print("Выделение очищено.");
            return;
        }

        final String typeName = args.getString(0);
        final RegionSelector oldSelector = session.getRegionSelector(world);

        final RegionSelector selector;
        if (typeName.equalsIgnoreCase("cuboid")) {
            selector = new CuboidRegionSelector(oldSelector);
            player.print("Кубоид: левый клик для первой позиции,правый для второй");
        } else if (typeName.equalsIgnoreCase("extend")) {
            selector = new ExtendingCuboidRegionSelector(oldSelector);
            player.print("Кубоид: левый клик для стартовой точки,правый для конечной");
        } else if (typeName.equalsIgnoreCase("poly")) {
            int maxPoints = we.getMaximumPolygonalPoints(player);
            selector = new Polygonal2DRegionSelector(oldSelector, maxPoints);
            player.print("2D Полигон: левый/правый клик для добавление точки.");
            if (maxPoints > -1) {
                player.print(maxPoints + " точек(а) максимум.");
            }
        } else if (typeName.equalsIgnoreCase("ellipsoid")) {
            selector = new EllipsoidRegionSelector(oldSelector);
            player.print("Эллипсоид: левый клик=центр, правый клик для расширения");
        } else if (typeName.equalsIgnoreCase("sphere")) {
            selector = new SphereRegionSelector(oldSelector);
            player.print("Сфера: левый клик=центр, правый клик для расширения");
        } else if (typeName.equalsIgnoreCase("cyl")) {
            selector = new CylinderRegionSelector(oldSelector);
            player.print("Циллиндр: левый клик=центр, правый клик для расширения.");
        } else {
            player.printError("Досткпны только такие значения: cuboid(кубоид)|extend(расширение)|poly(полигон)|ellipsoid(эллипсоид)|sphere(сфера)|cyl(цилиндр).");
            return;
        }

        session.setRegionSelector(world, selector);
        session.dispatchCUISelection(player);
    }

    @Command(aliases = {"/desel", "/deselect"}, desc = "Сбрасывает выделение")
    @CommandAlias("/sel")
    public void deselect() {

    }
}
