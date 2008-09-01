package com.meidusa.amoeba.net.packet;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.meidusa.amoeba.net.Connection;

/**
 * @author struct
 */
public abstract class AbstractPacket implements Packet {

    public void init(byte[] buffer, Connection conn) {
        AbstractPacketBuffer packetBuffer = constractorBuffer(buffer);
        packetBuffer.init(conn);
        init(packetBuffer);
    }

    /**
     * �������ݰ�(������ͷ+��������,�������ͷ�Ժ�Ӧ�ý�Buffer��postion���õ�������)
     */
    protected abstract void init(AbstractPacketBuffer buffer);

    public ByteBuffer toByteBuffer(Connection conn) {
        try {
            int bufferSize = calculatePacketSize();
            AbstractPacketBuffer packetBuffer = constractorBuffer(bufferSize);
            packetBuffer.init(conn);
            return toBuffer(packetBuffer).toByteBuffer();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private AbstractPacketBuffer constractorBuffer(int bufferSize) {
        AbstractPacketBuffer buffer = null;
        try {
            Constructor<? extends AbstractPacketBuffer> constractor = getPacketBufferClass().getConstructor(int.class);
            buffer = constractor.newInstance(bufferSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * <pre>
     *  �÷���������{@link #write2Buffer(PacketBuffer)} д�뵽ָ����buffer�� 
     *  ���ҵ�����{@link #afterPacketWritten(PacketBuffer)}
     * </pre>
     */
    private AbstractPacketBuffer toBuffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        write2Buffer(buffer);
        afterPacketWritten(buffer);
        return buffer;
    }

    /**
     * ����ͷ����Ϣ��װ
     */
    protected abstract void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException;

    /**
     * <pre>
     * д��֮��һ����Ҫ�������������buffer��ָ��λ��ָ��ĩβ����һ��λ�ã����ܳ���λ�ã���
     * ���һ���Ǽ������ݰ��ܳ���,����������Ҫ���ݰ�д�������ɵ�����
     * </pre>
     */
    protected abstract void afterPacketWritten(AbstractPacketBuffer buffer);

    /**
     * ����packet�Ĵ�С�������̫���˷��ڴ棬�����̫С��Ӱ������
     */
    protected abstract int calculatePacketSize();

    private AbstractPacketBuffer constractorBuffer(byte[] buffer) {
        AbstractPacketBuffer packetbuffer = null;
        try {
            Constructor<? extends AbstractPacketBuffer> constractor = getPacketBufferClass().getConstructor(byte[].class);
            packetbuffer = constractor.newInstance(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packetbuffer;
    }

    protected abstract Class<? extends AbstractPacketBuffer> getPacketBufferClass();

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
