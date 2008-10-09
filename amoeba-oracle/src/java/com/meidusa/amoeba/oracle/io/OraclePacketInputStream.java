package com.meidusa.amoeba.oracle.io;

import com.meidusa.amoeba.net.io.PacketInputStream;

/**
 * @author struct
 */
public class OraclePacketInputStream extends PacketInputStream implements OraclePacketConstant {

    private boolean readPackedWithHead;

    public OraclePacketInputStream(boolean readPackedWithHead){
        this.readPackedWithHead = readPackedWithHead;
    }

    @Override
    protected int decodeLength() {
        // if we don't have enough bytes to determine our frame size, stop
        // here and let the caller know that we're not ready
        int head = getHeaderSize();
        if (_have < head) {
            return -1;
        }

        // decode the frame length
        _buffer.rewind();

        /**
         * length = ���ݲ��֣���ͷ=�������ݰ�����
         */

        int length = _buffer.get() & 0xff;
        length <<= 8;
        length |= _buffer.get() & 0xff;

        _buffer.position(_have);
        return length;
    }

    @Override
    public int getHeaderSize() {
        return HEADER_SIZE;
    }

    protected boolean checkForCompletePacket() {
        if (_length == -1 || _have < _length) {
            return false;
        }
        // ��buffer �����������ݰ���������ͷ����
        if (readPackedWithHead) {
            _buffer.position(0);
        } else {
            _buffer.position(this.getHeaderSize());
        }
        _buffer.limit(_length);
        return true;
    }

}
