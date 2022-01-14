package me.mixal.edits.editor.widget;

import android.graphics.Bitmap;
import android.view.View;

import me.mixal.edits.R;
import me.mixal.edits.editor.EditImageActivity;

/**
 * Created by panyi on 2017/11/15.
 * <p>
 * 前一步 后一步操作类
 */
public class RedoUndoController implements View.OnClickListener {
    private final View mUndoBtn;//撤销按钮
    private final View mRedoBtn;//重做按钮
    private final EditImageActivity mActivity;
    private final EditCache mEditCache = new EditCache();//保存前一次操作内容 用于撤销操作

    private final EditCache.ListModify mObserver = cache -> updateBtns();

    public RedoUndoController(EditImageActivity activity, View panelView) {
        this.mActivity = activity;

        mUndoBtn = panelView.findViewById(R.id.undo_btn);
        mRedoBtn = panelView.findViewById(R.id.redo_btn);

        mUndoBtn.setOnClickListener(this);
        mRedoBtn.setOnClickListener(this);

        updateBtns();
        mEditCache.addObserver(mObserver);
    }

    public void switchMainBit(Bitmap mainBitmap, Bitmap newBit) {
        if (mainBitmap == null || mainBitmap.isRecycled())
            return;

        mEditCache.push(mainBitmap);
        mEditCache.push(newBit);
    }


    @Override
    public void onClick(View v) {
        if (v == mUndoBtn) {
            undoClick();
        } else if (v == mRedoBtn) {
            redoClick();
        }//end if
    }


    /**
     * 撤销操作
     */
    protected void undoClick() {
        //System.out.println("Undo!!!");
        Bitmap lastBitmap = mEditCache.getNextCurrentBit();
        if (lastBitmap != null && !lastBitmap.isRecycled()) {
            mActivity.changeMainBitmap(lastBitmap, false);
        }
    }

    /**
     * 取消撤销
     */
    protected void redoClick() {
        //System.out.println("Redo!!!");
        Bitmap preBitmap = mEditCache.getPreCurrentBit();
        if (preBitmap != null && !preBitmap.isRecycled()) {
            mActivity.changeMainBitmap(preBitmap, false);
        }
    }

    /**
     * 根据状态更新按钮显示
     */
    @SuppressWarnings("CommentedOutCode")
    public void updateBtns() {
        // System.out.println("缓存Size = " + mEditCache.getSize() + "  current = " + mEditCache.getCur());
        // System.out.println("content = " + mEditCache.debugLog());
        mUndoBtn.setVisibility(mEditCache.checkNextBitExist() ? View.VISIBLE : View.INVISIBLE);
        mRedoBtn.setVisibility(mEditCache.checkPreBitExist() ? View.VISIBLE : View.INVISIBLE);
    }

    public void onDestroy() {
        mEditCache.removeObserver(mObserver);
        mEditCache.removeAll();
    }

}
