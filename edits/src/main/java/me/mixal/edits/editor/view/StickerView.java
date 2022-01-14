package me.mixal.edits.editor.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedHashMap;

public class StickerView extends View {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_MOVE = 1;
    private static final int STATUS_DELETE = 2;
    private static final int STATUS_ROTATE = 3;

    private int imageCount;
    private int currentStatus;
    private StickerItem currentItem;
    private float oldX, oldY;

    private final Paint rectPaint = new Paint();
    private final Paint boxPaint = new Paint();

    private final LinkedHashMap<Integer, StickerItem> bank = new LinkedHashMap<>();

    public StickerView(Context context) {
        super(context);
        init();
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        currentStatus = STATUS_IDLE;

        rectPaint.setColor(Color.RED);
        rectPaint.setAlpha(100);

    }

    public void addBitImage(final Bitmap addBit) {
        StickerItem item = new StickerItem(this.getContext());
        item.init(addBit, this);
        if (currentItem != null) {
            currentItem.isDrawHelpTool = false;
        }
        bank.put(++imageCount, item);
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Integer id : bank.keySet()) {
            StickerItem item = bank.get(id);
            assert item != null;
            item.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                int deleteId = -1;
                for (Integer id : bank.keySet()) {
                    StickerItem item = bank.get(id);
                    assert item != null;
                    if (item.detectDeleteRect.contains(x, y)) {
                        // ret = true;
                        deleteId = id;
                        currentStatus = STATUS_DELETE;
                    } else if (item.detectRotateRect.contains(x, y)) {
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_ROTATE;
                        oldX = x;
                        oldY = y;
                    } else if (item.dstRect.contains(x, y)) {
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_MOVE;
                        oldX = x;
                        oldY = y;
                    }
                }

                if (!ret && currentItem != null && currentStatus == STATUS_IDLE) {
                    currentItem.isDrawHelpTool = false;
                    currentItem = null;
                    invalidate();
                }

                if (deleteId > 0 && currentStatus == STATUS_DELETE) {
                    bank.remove(deleteId);
                    currentStatus = STATUS_IDLE;
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (currentStatus == STATUS_MOVE) {
                    float dx = x - oldX;
                    float dy = y - oldY;
                    if (currentItem != null) {
                        currentItem.updatePos(dx, dy);
                        invalidate();
                    }
                    oldX = x;
                    oldY = y;
                } else if (currentStatus == STATUS_ROTATE) {
                    float dx = x - oldX;
                    float dy = y - oldY;
                    if (currentItem != null) {
                        currentItem.updateRotateAndScale(oldX, oldY, dx, dy);
                        invalidate();
                    }
                    oldX = x;
                    oldY = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                ret = false;
                currentStatus = STATUS_IDLE;
                break;
        }
        return ret;
    }

    public LinkedHashMap<Integer, StickerItem> getBank() {
        return bank;
    }

    public void clear() {
        bank.clear();
        this.invalidate();
    }
}
