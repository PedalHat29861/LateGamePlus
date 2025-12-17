/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public record DefaultPosArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) implements PosArgument
{
    public static final DefaultPosArgument DEFAULT_ROTATION = DefaultPosArgument.absolute(new Vec2f(0.0f, 0.0f));

    @Override
    public Vec3d getPos(ServerCommandSource source) {
        Vec3d vec3d = source.getPosition();
        return new Vec3d(this.x.toAbsoluteCoordinate(vec3d.x), this.y.toAbsoluteCoordinate(vec3d.y), this.z.toAbsoluteCoordinate(vec3d.z));
    }

    @Override
    public Vec2f getRotation(ServerCommandSource source) {
        Vec2f vec2f = source.getRotation();
        return new Vec2f((float)this.x.toAbsoluteCoordinate(vec2f.x), (float)this.y.toAbsoluteCoordinate(vec2f.y));
    }

    @Override
    public boolean isXRelative() {
        return this.x.isRelative();
    }

    @Override
    public boolean isYRelative() {
        return this.y.isRelative();
    }

    @Override
    public boolean isZRelative() {
        return this.z.isRelative();
    }

    public static DefaultPosArgument parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext((ImmutableStringReader)reader);
        }
        reader.skip();
        CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext((ImmutableStringReader)reader);
        }
        reader.skip();
        CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader);
        return new DefaultPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
    }

    public static DefaultPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
        int i = reader.getCursor();
        CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader, centerIntegers);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext((ImmutableStringReader)reader);
        }
        reader.skip();
        CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader, false);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext((ImmutableStringReader)reader);
        }
        reader.skip();
        CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader, centerIntegers);
        return new DefaultPosArgument(coordinateArgument, coordinateArgument2, coordinateArgument3);
    }

    public static DefaultPosArgument absolute(double x, double y, double z) {
        return new DefaultPosArgument(new CoordinateArgument(false, x), new CoordinateArgument(false, y), new CoordinateArgument(false, z));
    }

    public static DefaultPosArgument absolute(Vec2f vec) {
        return new DefaultPosArgument(new CoordinateArgument(false, vec.x), new CoordinateArgument(false, vec.y), new CoordinateArgument(true, 0.0));
    }
}

