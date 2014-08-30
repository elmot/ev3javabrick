package elmot.javabrick.ev3.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author elmot
 */
public class Command {
    public static final int PRAMETER_TYPE_VARIABLE = 0x40;
    public static final int VARIABLE_SCOPE_GLOBAL = 0x20;
    private final int byteCode;
    private final int replyByteCount;

    private final List<byte[]> params = new ArrayList<byte[]>();

    public Command(int byteCode, int replyByteCount) {
        this.byteCode = byteCode;
        this.replyByteCount = replyByteCount;
    }

    public Command(int byteCode) {
        this(byteCode, 0);
    }

    public int getReplyByteCount() {
        return replyByteCount;
    }

    public byte getType() {
        return 0;
    }

    public void writeTo(ByteBuffer buffer) throws IOException {
        buffer.put((byte)byteCode);
        for (byte[] param : params) {
            buffer.put(param);
        }
    }

    public Command addByte(int val) {
        params.add(new byte[]{(byte) val});
        return this;
    }

    public void addLongOneByte(int val) {
        params.add(new byte[]{(byte) 0x81, (byte) val});
    }

    @SuppressWarnings("UnusedDeclaration")
    public void addLongTwoBytes(int val) {
        params.add(new byte[]{(byte) 0x82, (byte) val, (byte) (val >> 8)});
    }

    public void addIntFourBytes(int val, byte modifiers) {
        params.add(new byte[]{(byte) 0x83, (byte) val, (byte) (val >> 8), (byte) (val >> 16), (byte) (val >> 24),});
    }

    public void addShortGlobalVariable(int val) {
        byte b = (byte) (PRAMETER_TYPE_VARIABLE | VARIABLE_SCOPE_GLOBAL | (val & 0x1f));
        addByte(b);
    }

    public void addIntConstantParam(int val) {
        addIntFourBytes(val, (byte) 0);
    }
}
