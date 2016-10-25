package com.shane.android.videoplayer.interf;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 16-10-25
 */

public interface IFileCoder {
    byte[] getEncodeBytes(byte[] data);
    byte[] getDecodeBytes(byte[] data);
}
