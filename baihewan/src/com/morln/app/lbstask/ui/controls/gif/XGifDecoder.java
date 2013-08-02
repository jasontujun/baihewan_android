package com.morln.app.lbstask.ui.controls.gif;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import java.io.InputStream;
import java.util.ArrayList;


/**
 * 16-bit的Gif图像的解析器。
 * Created by jasontujun.
 * Date: 12-11-29
 * Time: 下午1:46
 */
public class XGifDecoder {

    /**状态：正在解码中*/
    public static final int STATUS_PARSING = 0;
    /**状态：图片格式错误*/
    public static final int STATUS_FORMAT_ERROR = 1;
    /**状态：打开失败*/
    public static final int STATUS_OPEN_ERROR = 2;
    /**状态：解码成功*/
    public static final int STATUS_FINISH = -1;

    private InputStream in;
    private int status;

    public int width; // full image width
    public int height; // full image height
    private boolean gctFlag; // global color table used
    private int gctSize; // size of global color table
    private int loopCount; // iterations; 0 = repeat forever

    private int[] gct; // global color table
    private int[] lct; // local color table
    private int[] act; // active color table

    private int bgIndex; // background color index
    private int bgColor; // background color
    private int lastBgColor; // previous bg color
    private int pixelAspect; // pixel aspect ratio

    private boolean lctFlag; // local color table flag
    private boolean interlace; // interlace flag
    private int lctSize; // local color table size

    private int ix, iy, iw, ih; // current image rectangle
    private int lrx, lry, lrw, lrh;
    private Bitmap image; // current frame
    private Bitmap lastImage; // previous frame

    private byte[] block = new byte[256]; // current data block
    private int blockSize = 0; // block size

    // last graphic control extension info
    private int dispose = 0;
    // 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
    private int lastDispose = 0;
    private boolean transparency = false; // use transparent color
    private int delay = 0; // delay in milliseconds
    private int transIndex; // transparent color index

    private static final int MaxStackSize = 4096;
    // max decoder pixel stack size

    // LZW decoder working arrays (LZW压缩算法)
    private short[] prefix;
    private byte[] suffix;
    private byte[] pixelStack;
    private byte[] pixels;

    private int frameCount;// 帧总数
    private GifDecodeListener mDecodeListener;// 监听者
    private int[] mDelays;// 所有帧之间的间隔
    private ArrayList<XGifFrame> mFrameList = new ArrayList<XGifFrame>();


    public void setDecodeListener(GifDecodeListener decodeListener) {
        this.mDecodeListener = decodeListener;
    }

    /**
     * 开始解析gif图片。
     * @param is 待解析的Gif图片输入流
     */
    public void startDecode(InputStream is) {
        // 释放之前的资源
        status = STATUS_PARSING;
        free();

        // 加载输入流
        in = is;
        if(in == null) {
            status = STATUS_OPEN_ERROR;
            if(mDecodeListener != null) {
                mDecodeListener.onDecodeFinished(false);
            }
            return;
        }

        // 加载gif
        readHeader();
        if(!err()) {
            readContents();
            if(frameCount < 0) {
                status = STATUS_FORMAT_ERROR;
                if(mDecodeListener != null) {
                    mDecodeListener.onDecodeFinished(false);
                }
            } else {
                // TODO 进行图片压缩
                if(mDecodeListener != null) {
                    for(int i = 0; i<frameCount; i++) {
                        XGifFrame frame = mFrameList.get(i);
                        Bitmap tmp = mDecodeListener.onBitmapProcess(frame.image);
                        frame.image = tmp;
                    }
                }

                status = STATUS_FINISH;
                if(mDecodeListener != null) {
                    mDecodeListener.onDecodeFinished(true);
                }
            }
        } else {
            status = STATUS_FORMAT_ERROR;
            if(mDecodeListener != null) {
                mDecodeListener.onDecodeFinished(false);
            }
        }

        // 关闭输入流
        try {
            in.close();
            in = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
    public void free() {
        if(in != null) {
            try {
                in.close();
            } catch(Exception ex) {}
            in = null;
        }

        // 清空帧（图片资源）
        for(int i = 0; i < mFrameList.size(); i++) {
            XGifFrame tmp = mFrameList.get(i);
            if(tmp.image != null) {
                tmp.image.recycle();
                tmp.image = null;
            }
        }
        mFrameList.clear();

        // 还原参数
        loopCount = 0;
        frameCount = 0;
        mDelays = null;
        gct = null;
        lct = null;
        act = null;
        prefix = null;
        suffix = null;
        pixelStack = null;
        pixels = null;
    }

    /**
     * 当前状态
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * 解码是否成功，成功返回true
     * @return 成功返回true，否则返回false
     */
    public boolean isSucceed() {
        return status == STATUS_FINISH;
    }

    /**
     * 取某帧的延时时间
     * <strong>
     *     可选择性的在调用此方法之前调用computeDelays()，
     *     以便获得最新的间隔
     * </strong>
     * @see #computeDelays()
     * @param i 第几帧
     * @return 延时时间，毫秒
     */
    public int getDelay(int i) {
        if (mDelays != null && (0 <= i) && (i < frameCount)) {
            return mDelays[i];
        }
        return -1;
    }

    /**
     * 取所有帧的延时时间
     * <strong>
     *     可选择性的在调用此方法之前调用computeDelays()，
     *     以便获得最新的间隔
     * </strong>
     * @see #computeDelays()
     * @return
     */
    public int[] getDelays() {
        return mDelays;
    }

    /**
     * 计算所有帧之间的延时时间。<br>
     * <strong>
     *     可选择性的在获取延迟的方法之前调用，
     *     以便获得最新的间隔
     * </strong>
     * @see #getDelay(int)
     * @see #getDelays()
     */
    public void computeDelays() {
        mDelays = new int[frameCount];
        for(int i = 0; i < frameCount; i++) {
            mDelays[i] = mFrameList.get(i).delay;
        }
    }


    /**
     * 取总帧数
     * @return 图片的总帧数
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * gif图片，取第一帧图片
     * @return
     */
    public Bitmap getCoverImage() {
        if(status != STATUS_FORMAT_ERROR) {
            return getFrameImage(0);
        }
        return null;
    }

    public int getLoopCount() {
        return loopCount;
    }

    /**
     * 取第几帧的图片
     * @param i 帧数
     * @return 可画的图片，如果没有此帧或者出错，返回null
     */
    public Bitmap getFrameImage(int i) {
        if(0 <= i && i < frameCount) {
            return mFrameList.get(i).image;
        }
        return null;
    }


    private void decodeImageData() {
        int NullCode = -1;
        int npix = iw * ih;
        int available, clear, code_mask, code_size, end_of_information, in_code,
                old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

        if ((pixels == null) || (pixels.length < npix)) {
            pixels = new byte[npix]; // allocate new pixel array
        }
        if (prefix == null) {
            prefix = new short[MaxStackSize];
        }
        if (suffix == null) {
            suffix = new byte[MaxStackSize];
        }
        if (pixelStack == null) {
            pixelStack = new byte[MaxStackSize + 1];
        }
        // Initialize GIF data stream decoder.
        data_size = read();
        clear = 1 << data_size;
        end_of_information = clear + 1;
        available = clear + 2;
        old_code = NullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; code++) {
            prefix[code] = 0;
            suffix[code] = (byte) code;
        }

        // Decode GIF pixel stream.
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix;) {
            if (top == 0) {
                if (bits < code_size) {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock();
                        if (count <= 0) {
                            break;
                        }
                        bi = 0;
                    }
                    datum += (((int) block[bi]) & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                // Get the next code.
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;

                // Interpret the code
                if ((code > available) || (code == end_of_information)) {
                    break;
                }
                if (code == clear) {
                    // Reset decoder.
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = NullCode;
                    continue;
                }
                if (old_code == NullCode) {
                    pixelStack[top++] = suffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                in_code = code;
                if (code == available) {
                    pixelStack[top++] = (byte) first;
                    code = old_code;
                }
                while (code > clear) {
                    pixelStack[top++] = suffix[code];
                    code = prefix[code];
                }
                first = ((int) suffix[code]) & 0xff;
                // Add a new string to the string table,
                if (available >= MaxStackSize) {
                    break;
                }
                pixelStack[top++] = (byte) first;
                prefix[available] = (short) old_code;
                suffix[available] = (byte) first;
                available++;
                if (((available & code_mask) == 0)
                        && (available < MaxStackSize)) {
                    code_size++;
                    code_mask += available;
                }
                old_code = in_code;
            }

            // Pop a pixel off the pixel stack.
            top--;
            pixels[pi++] = pixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++) {
            pixels[i] = 0; // clear missing pixels
        }
    }

    private boolean err() {
        return status != STATUS_PARSING;
    }


    /**
     * 读取Gif文件的头块
     */
    private void readHeader() {
        String id = "";
        for (int i = 0; i < 6; i++) {
            id += (char) read();
        }
        if (!id.startsWith("GIF") && !id.startsWith("gif")) {
            status = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (gctFlag && !err()) {
            gct = readColorTable(gctSize);
            bgColor = gct[bgIndex];
        }
    }

    /**
     * 读取Gif文件的内容块
     * (核心方法)
     */
    private void readContents() {
        boolean done = false;
        while (!(done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: // image separator
                    readImage();
                    break;
                case 0x21: // extension
                    code = read();
                    switch (code) {
                        case 0xf9: // graphics control extension
                            readGraphicControlExt();
                            break;
                        case 0xff: // application extension
                            readBlock();
                            String app = "";
                            for (int i = 0; i < 11; i++) {
                                app += (char) block[i];
                            }
                            if (app.equals("NETSCAPE2.0")) {
                                readNetscapeExt();
                            } else {
                                skip(); // don't care
                            }
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;
                case 0x3b: // terminator
                    done = true;
                    break;
                case 0x00: // bad byte, but keep going and see what happens
                    break;
                default:
                    status = STATUS_FORMAT_ERROR;
            }
        }
    }

    /**
     * 读取图片。调用setPixels生成Bitmap
     */
    private void readImage() {
        ix = readShort(); // (sub)image position & size
        iy = readShort();
        iw = readShort();
        ih = readShort();
        int packed = read();
        lctFlag = (packed & 0x80) != 0; // 1 - local color table flag
        interlace = (packed & 0x40) != 0; // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        lctSize = 2 << (packed & 7); // 6-8 - local color table size
        if (lctFlag) {
            lct = readColorTable(lctSize); // read table
            act = lct; // make local table active
        } else {
            act = gct; // make global table active
            if (bgIndex == transIndex) {
                bgColor = 0;
            }
        }
        int save = 0;
        if (transparency) {
            save = act[transIndex];
            act[transIndex] = 0; // set transparent color if specified
        }
        if (act == null) {
            status = STATUS_FORMAT_ERROR; // no color table defined
        }
        if (err()) {
            return;
        }
        if (transparency) {
            act[transIndex] = save;
        }
        decodeImageData(); // decode pixel data
        skip();
        if (err()) {
            return;
        }
        // create new image to receive frame data
        image = Bitmap.createBitmap(width, height, Config.ARGB_4444);
        // transfer pixel data to image
        setPixels();
        // store into list
        mFrameList.add(new XGifFrame(image, delay));
        frameCount++;
        // resetFrame
        lastDispose = dispose;
        lrx = ix;
        lry = iy;
        lrw = iw;
        lrh = ih;
        lastImage = image;
        lastBgColor = bgColor;
        dispose = 0;
        transparency = false;
        delay = 0;
        lct = null;

        // 通知监听者
        if(mDecodeListener != null) {
            mDecodeListener.onDecoding(frameCount - 1);
        }
    }

    private void setPixels() {
        int[] dest = new int[width * height];
        // fill in starting image contents based on last image's dispose code
        if (lastDispose > 0) {
            if (lastDispose == 3) {
                // use image before last
                int n = frameCount - 2;
                if (n > 0) {
                    lastImage = getFrameImage(n - 1);
                } else {
                    lastImage = null;
                }
            }
            if (lastImage != null) {
                lastImage.getPixels(dest, 0, width, 0, 0, width, height);
                // copy pixels
                if (lastDispose == 2) {
                    // fill last image rect area with background color
                    int c = 0;
                    if (!transparency) {
                        c = lastBgColor;
                    }
                    for (int i = 0; i < lrh; i++) {
                        int n1 = (lry + i) * width + lrx;
                        int n2 = n1 + lrw;
                        for (int k = n1; k < n2; k++) {
                            dest[k] = c;
                        }
                    }
                }
            }
        }

        // copy each source line to the appropriate place in the destination
        int pass = 1;
        int inc = 8;
        int iline = 0;
        for (int i = 0; i < ih; i++) {
            int line = i;
            if (interlace) {
                if (iline >= ih) {
                    pass++;
                    switch (pass) {
                        case 2:
                            iline = 4;
                            break;
                        case 3:
                            iline = 2;
                            inc = 4;
                            break;
                        case 4:
                            iline = 1;
                            inc = 2;
                    }
                }
                line = iline;
                iline += inc;
            }
            line += iy;
            if (line < height) {
                int k = line * width;
                int dx = k + ix; // start of line in dest
                int dlim = dx + iw; // end of dest line
                if ((k + width) < dlim) {
                    dlim = k + width; // past dest edge
                }
                int sx = i * iw; // start of line in source
                while (dx < dlim) {
                    // map color and insert in destination
                    int index = ((int) pixels[sx++]) & 0xff;
                    int c = act[index];
                    if (c != 0) {
                        dest[dx] = c;
                    }
                    dx++;
                }
            }
        }
        image = Bitmap.createBitmap(dest, width, height, Config.ARGB_4444);
    }

    private int readBlock() {
        blockSize = read();
        int n = 0;
        if (blockSize > 0) {
            try {
                int count = 0;
                while (n < blockSize) {
                    count = in.read(block, n, blockSize - n);
                    if (count == -1) {
                        break;
                    }
                    n += count;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (n < blockSize) {
                status = STATUS_FORMAT_ERROR;
            }
        }
        return n;
    }

    private int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try {
            n = in.read(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (n < nbytes) {
            status = STATUS_FORMAT_ERROR;
        } else {
            tab = new int[256]; // max size to avoid bounds checks
            int i = 0;
            int j = 0;
            while (i < ncolors) {
                int r = ((int) c[j++]) & 0xff;
                int g = ((int) c[j++]) & 0xff;
                int b = ((int) c[j++]) & 0xff;
                tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
        return tab;
    }

    private void readGraphicControlExt() {
        read(); // block size
        int packed = read(); // packed fields
        dispose = (packed & 0x1c) >> 2; // disposal method
        if (dispose == 0) {
            dispose = 1; // elect to keep old image if discretionary
        }
        transparency = (packed & 1) != 0;
        delay = readShort() * 10; // delay in milliseconds
        transIndex = read(); // transparent color index
        read(); // block terminator
    }

    private void readLSD() {
        // logical screen size
        width = readShort();
        height = readShort();
        // packed fields
        int packed = read();
        gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
        // 2-4 : color resolution
        // 5 : gct sort flag
        gctSize = 2 << (packed & 7); // 6-8 : gct size
        bgIndex = read(); // background color index
        pixelAspect = read(); // pixel aspect ratio
    }

    private void readNetscapeExt() {
        do {
            readBlock();
            if (block[0] == 1) {
                // loop count sub-block
                int b1 = ((int) block[1]) & 0xff;
                int b2 = ((int) block[2]) & 0xff;
                loopCount = (b2 << 8) | b1;
            }
        } while ((blockSize > 0) && !err());
    }


    private int read() {
        try {
            if(in != null) {
                return in.read();
            }
        } catch (Exception e) {
            status = STATUS_FORMAT_ERROR;
        }
        return 0;
    }

    private int readShort() {
        // read 16-bit value, LSB first
        return read() | (read() << 8);
    }


    /**
     * Skips variable length blocks up to and including next zero length block.
     */
    private void skip() {
        do {
            readBlock();
        } while ((blockSize > 0) && !err());
    }


    public interface GifDecodeListener {

        /**
         * 压缩处理图片
         * @param src
         * @return
         */
        Bitmap onBitmapProcess(Bitmap src);

        /**
         * 正在解码
         * @param frameIndex 当前解码的第几帧
         */
        void onDecoding(int frameIndex);

        /**
         * 解码完成
         * @param success 解码是否成功，成功会为true
         */
        void onDecodeFinished(boolean success);
    }
}
