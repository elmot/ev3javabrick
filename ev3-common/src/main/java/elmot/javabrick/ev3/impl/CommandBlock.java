package elmot.javabrick.ev3.impl;

import elmot.javabrick.ev3.EV3Brick;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author elmot
 */
public class CommandBlock {

    private static AtomicInteger seqCounter = new AtomicInteger(0);
    private List<Command> commands = new ArrayList<Command>();

    private byte commandType;

    public CommandBlock() {
    }

    public CommandBlock(Command... commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }

    public void addCommand(Command command) {
        if (commands.isEmpty()) {
            commandType = command.getType();
        } else if (commandType != command.getType()) {
            throw new RuntimeException("Commands of different types are not supported");
        }
        commands.add(command);
    }

    public Response run(EV3Brick brick, Class<?> outParametersTypes[]) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0); //here will be length
        baos.write(0);
        int seqNumber = seqCounter.incrementAndGet() % 65536;
        baos.write(seqNumber & 0xff);
        baos.write(seqNumber >> 8);
        baos.write(commandType);
        baos.write(0); //here will be var count
        baos.write(0);
        int globalVarCount = 0;// Local wars are not supported
        for (Command command : commands) {
            globalVarCount += command.getReplyByteCount();
            command.writeTo(baos);
        }
        byte[] bytes = baos.toByteArray();
        int len = bytes.length - 2;
        bytes[0] = (byte) (len & 0xff);
        bytes[1] = (byte) (len >> 8);
        bytes[5] = (byte) (globalVarCount & 0xff);
        bytes[6] = (byte) (globalVarCount >> 8);
        byte [] responseBytes = brick.dataExchange(bytes);
        int readSeqNo = responseBytes[3] << 8 | (0xff & (int)responseBytes[2]) ;
        if (readSeqNo != seqNumber) {
            throw new IOException("Unexpected Response seq. no.");
        }
        int status = responseBytes[4];
        if (outParametersTypes != null) {
            Response response = new Response(outParametersTypes.length, status);
            for (int i = 0, index = 5; i < outParametersTypes.length; i++) {
                Class<?> outParameterType = outParametersTypes[i];
                if (outParameterType == Integer.class || outParameterType == int.class) {
                    response.setOutParameter(i, readInt(responseBytes, index));
                    index += 4;
                } else if (outParameterType == Float.class || outParameterType == float.class) {
                    response.setOutParameter(i, readFloat(responseBytes, index));
                    index += 4;
                } else if (outParameterType == Byte.class || outParameterType == byte.class) {
                    response.setOutParameter(i, responseBytes[index]);
                    index += 1;
                } else {
                    throw new IllegalArgumentException("OUT parameter of type " + outParameterType + " is not supported");
                }
            }
            return response;
        } else {
            return new Response(0, status);
        }
    }

    private float readFloat(byte[] responsePart, int index) throws IOException {
        return Float.intBitsToFloat(readInt(responsePart,index));
    }


    private int readInt(byte[] bytes, int byteIndex) throws IOException {
        int result;
        result = bytes[byteIndex] & 0xff;
        result |= (bytes[byteIndex + 1] << 8) & 0xFF00;
        result |= (bytes[byteIndex + 2] << 16) & 0xFF0000;
        result |= bytes[byteIndex + 3] << 24;
        return result;
    }

}
