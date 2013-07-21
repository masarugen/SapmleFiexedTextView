
package me.gensan.android.sample.samplefixedtextview.ui;

import me.gensan.android.sample.samplefixedtextview.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

/**
 * android2.2以下でellipsizeが指定行数以下になるバグを修正したTextView
 */
public class FixedTextView extends TextView {

    @SuppressWarnings("unused")
    private static final String TAG = "FixedTextView";
    private final FixedTextView self = this;

    private static final String ELLIPSIS = "…";

    private int mFixedEllipsizeMaxLines = -1;

    /**
     * @param context
     */
    public FixedTextView(Context context) {
        super(context);
        fixEllipsize();
    }

    /**
     * @param context
     * @param attrs
     */
    public FixedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttribute(context, attrs);
        fixEllipsize();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public FixedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttribute(context, attrs);
        fixEllipsize();
    }

    /**
     * XMLでの設定の取得
     * 
     * @param context
     * @param attrs
     */
    @SuppressLint("Recycle")
    private void initAttribute(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.FixedTextView);
        mFixedEllipsizeMaxLines = typedArray.getInt(
                R.styleable.FixedTextView_fixedEllipsizeMaxLines,
                -1);
    }

    /**
     * 修正版の省略符号処理
     */
    private void fixEllipsize() {
        if (mFixedEllipsizeMaxLines <= 0) {
            return;
        }
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                removeGlobalOnLayoutListener(getViewTreeObserver(), this);
                // 表示行数以上の行数があった場合、省略処理を行う
                if (getLineCount() > mFixedEllipsizeMaxLines) {
                    final CharSequence text = getText();
                    final int lineEndIndex = getLayout().getLineEnd(mFixedEllipsizeMaxLines - 1);

                    // 行末が改行コードかどうかチェックする
                    CharSequence lastChar = text.subSequence(lineEndIndex - 1, lineEndIndex);
                    if (lastChar.charAt(0) == '\n') {
                        // 改行コードを省略文字に変更する
                        setText(text.subSequence(0, lineEndIndex - 1) + ELLIPSIS);
                    } else {
                        // 行末が改行コードでない場合、改行になるまで処理する
                        setFixedText(text, lineEndIndex);
                    }
                }
            }

            /**
             * 文字列を整形
             * 
             * @param text
             * @param lineEndIndex
             */
            private void setFixedText(final CharSequence text, final int lineEndIndex) {
                StringBuffer viewText = new StringBuffer(text.subSequence(0, lineEndIndex));
                final int lastIndex = text.length();
                int index = 0;
                // 制限の行数を越えるまで文字列を追加する
                do {
                    if ((lineEndIndex + index + 1) > lastIndex) {
                        break;
                    }
                    viewText.append(text
                            .subSequence(lineEndIndex + index, lineEndIndex + index + 1));
                    setText(viewText);
                    index++;
                } while (getLineCount() <= mFixedEllipsizeMaxLines);

                // 制限列数以内に収める
                index = 1;
                while (getLineCount() > mFixedEllipsizeMaxLines) {
                    setText(viewText.subSequence(0, viewText.length() - index) + ELLIPSIS);
                    index++;
                }
            }

            /**
             * 非推奨API対応したremoveGlobalOnLayoutListener
             * 
             * @param obs
             * @param listener
             */
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            public void removeGlobalOnLayoutListener(ViewTreeObserver observer,
                    OnGlobalLayoutListener listener) {
                if (observer == null) {
                    return;
                }
                if (Build.VERSION.SDK_INT < 16) {
                    observer.removeGlobalOnLayoutListener(listener);
                } else {
                    observer.removeOnGlobalLayoutListener(listener);
                }
            }
        });
    }
}
